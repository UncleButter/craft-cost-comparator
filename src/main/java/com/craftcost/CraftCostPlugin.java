package com.craftcost;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Craft Cost Comparator",
	description = "Compares an item's Grand Exchange price against the cost of its ingredients, counting what you already have banked (Herblore potions)",
	tags = {"grand", "exchange", "ge", "herblore", "potion", "profit", "cost", "craft", "skilling"}
)
public class CraftCostPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private CraftCostConfig config;

	@Inject
	private ConfigManager configManager;

	private static final String WATCHLIST_KEY = "watchlist";

	private final Map<Integer, Integer> inventoryCounts = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> bankCounts = new ConcurrentHashMap<>();

	private boolean geOfferScreenOpen;

	private CraftCostPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp()
	{
		panel = new CraftCostPanel(this, itemManager, clientThread);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Craft Cost Comparator")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// If the plugin is enabled while already logged in, grab whatever
		// container contents the client already has cached.
		clientThread.invoke(() ->
		{
			refreshContainer(InventoryID.INV, inventoryCounts);
			refreshContainer(InventoryID.BANK, bankCounts);
		});
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		inventoryCounts.clear();
		bankCounts.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(() ->
			{
				refreshContainer(InventoryID.INV, inventoryCounts);
				refreshContainer(InventoryID.BANK, bankCounts);
			});
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			inventoryCounts.clear();
			bankCounts.clear();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INV)
		{
			rebuildCounts(event.getItemContainer(), inventoryCounts);
			panel.onOwnedItemsChanged();
		}
		else if (event.getContainerId() == InventoryID.BANK)
		{
			rebuildCounts(event.getItemContainer(), bankCounts);
			panel.onOwnedItemsChanged();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.GE_OFFERS)
		{
			geOfferScreenOpen = true;
			checkGeItem();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.GE_OFFERS)
		{
			geOfferScreenOpen = false;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!geOfferScreenOpen)
		{
			return;
		}

		if (event.getVarpId() == VarPlayerID.TRADINGPOST_SEARCH)
		{
			checkGeItem();
		}
		else if (event.getVarbitId() == VarbitID.GE_NEWOFFER_QUANTITY)
		{
			checkGeQuantity();
		}
	}

	private void checkGeItem()
	{
		if (!config.autoShowGeItem())
		{
			return;
		}

		// TRADINGPOST_SEARCH tracks the item you're currently viewing/buying at
		// the GE live, including while the offer-setup screen is open - unlike
		// the GE_LAST_OFFER_* varps, which only update once an offer is submitted.
		int itemId = client.getVarpValue(VarPlayerID.TRADINGPOST_SEARCH);
		Recipe recipe = RecipeDatabase.findByItemId(itemId);
		if (recipe != null)
		{
			panel.autoAddFromGe(recipe);
			// pick up whatever quantity is already showing, not just future changes
			checkGeQuantity();
		}
	}

	private void checkGeQuantity()
	{
		if (!config.autoShowGeItem())
		{
			return;
		}

		int itemId = client.getVarpValue(VarPlayerID.TRADINGPOST_SEARCH);
		Recipe recipe = RecipeDatabase.findByItemId(itemId);
		if (recipe == null)
		{
			return;
		}

		// GE_NEWOFFER_QUANTITY is a varbit that updates live, tick by tick, as
		// the quantity box on the offer-setup screen is typed into or clicked -
		// confirmed by watching it in RuneLite's Var Inspector while adjusting
		// the quantity before submitting.
		int quantity = client.getVarbitValue(VarbitID.GE_NEWOFFER_QUANTITY);
		if (quantity > 0)
		{
			panel.autoSetAmount(recipe, quantity);
		}
	}

	// Live price-per-item syncing used to read varbit GE_NEWOFFER_PRICE (4398)
	// alongside GE_NEWOFFER_QUANTITY. That varbit no longer exists - RuneLite
	// generates VarbitID from the game cache, and 4398 was dropped after
	// 1.13.0 - so there is nothing left to read and the feature is disabled.
	// Quantity syncing is unaffected; GE_NEWOFFER_QUANTITY (4396) is still
	// present. CraftCostPanel.autoSetPrice and RecipeCardPanel.setLivePrice are
	// deliberately left in place, ready to be rewired if a replacement source
	// for the live offer price is found.

	/**
	 * Whether the panel's "Auto-show GE item" checkbox should be ticked -
	 * lets the panel stay in sync with the plugin's own config panel entry.
	 */
	boolean isAutoShowGeItem()
	{
		return config.autoShowGeItem();
	}

	void setAutoShowGeItem(boolean value)
	{
		configManager.setConfiguration(CraftCostConfig.CONFIG_GROUP, "autoShowGeItem", String.valueOf(value));
	}

	/**
	 * Item ids of whatever's currently in the watchlist, persisted across
	 * client restarts.
	 */
	List<Integer> loadWatchlist()
	{
		String csv = configManager.getConfiguration(CraftCostConfig.CONFIG_GROUP, WATCHLIST_KEY);
		List<Integer> ids = new ArrayList<>();
		if (csv != null && !csv.isEmpty())
		{
			for (String part : csv.split(","))
			{
				try
				{
					ids.add(Integer.parseInt(part.trim()));
				}
				catch (NumberFormatException e)
				{
					// ignore a corrupted entry rather than losing the whole list
				}
			}
		}
		return ids;
	}

	void saveWatchlist(List<Integer> itemIds)
	{
		String csv = itemIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		configManager.setConfiguration(CraftCostConfig.CONFIG_GROUP, WATCHLIST_KEY, csv);
	}

	private void refreshContainer(int containerId, Map<Integer, Integer> target)
	{
		ItemContainer container = client.getItemContainer(containerId);
		if (container != null)
		{
			rebuildCounts(container, target);
		}
	}

	private void rebuildCounts(ItemContainer container, Map<Integer, Integer> target)
	{
		target.clear();
		for (Item item : container.getItems())
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			target.merge(item.getId(), item.getQuantity(), Integer::sum);
		}
	}

	/**
	 * How many of the given item Chris currently owns, per the "count bank" /
	 * "count inventory" config toggles.
	 */
	int getOwnedCount(int itemId)
	{
		int total = 0;
		if (config.countInventory())
		{
			total += inventoryCounts.getOrDefault(itemId, 0);
		}
		if (config.countBank())
		{
			total += bankCounts.getOrDefault(itemId, 0);
		}
		return total;
	}

	/**
	 * Chris's current (unboosted) level in the given skill. Must be called on
	 * the client thread.
	 */
	int getSkillLevel(Skill skill)
	{
		return client.getRealSkillLevel(skill);
	}

	/**
	 * Whether the given quest is finished, in progress, or not started yet.
	 * Runs a client script under the hood, so - like the skill level above -
	 * this must be called on the client thread.
	 */
	QuestState getQuestState(Quest quest)
	{
		return quest.getState(client);
	}

	@Provides
	CraftCostConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CraftCostConfig.class);
	}
}
