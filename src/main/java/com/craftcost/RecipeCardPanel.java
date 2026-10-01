package com.craftcost;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

/**
 * One compact, self-refreshing "card" in the watchlist: an item's GE price,
 * its ingredients (icon / owned / needed / price each), and a one-line
 * verdict on whether crafting or buying wins - all scaled by however many
 * of the finished item you tell it you want (the amount spinner).
 */
class RecipeCardPanel extends JPanel
{
	private static final NumberFormat GP_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
	private static final int HEADER_ICON_SIZE = 24;
	private static final int INGREDIENT_ICON_SIZE = 18;
	private static final int MAX_AMOUNT = 10_000;
	private static final String COLLAPSED_ARROW = "▸";
	private static final String EXPANDED_ARROW = "▾";

	private final Recipe recipe;
	private final CraftCostPlugin plugin;
	private final ItemManager itemManager;
	private final ClientThread clientThread;

	private final JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_AMOUNT, 1));
	private final JLabel priceLabel = new JLabel();
	private final JLabel verdictLabel = new JLabel();
	private final JLabel craftCostLabel = new JLabel();
	private final JLabel fromScratchLabel = new JLabel();
	private final JPanel ingredientsPanel = new JPanel();
	private final JPanel detailsPanel = new JPanel();
	private final JButton expandToggle = new JButton(COLLAPSED_ARROW);

	private boolean settingAmountProgrammatically;
	private Integer liveUnitPrice;

	RecipeCardPanel(Recipe recipe, CraftCostPlugin plugin, ItemManager itemManager, ClientThread clientThread, Runnable onRemove)
	{
		this.recipe = recipe;
		this.plugin = plugin;
		this.itemManager = itemManager;
		this.clientThread = clientThread;

		setLayout(new BorderLayout(0, 4));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(6, 6, 6, 6));
		setAlignmentX(LEFT_ALIGNMENT);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		top.add(buildHeader(onRemove));
		top.add(buildAmountRow());
		add(top, BorderLayout.NORTH);

		ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
		ingredientsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
		detailsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		detailsPanel.setBorder(new EmptyBorder(4, 0, 0, 0));
		detailsPanel.setVisible(false);

		JPanel centerColumn = new JPanel();
		centerColumn.setLayout(new BoxLayout(centerColumn, BoxLayout.Y_AXIS));
		centerColumn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		centerColumn.add(ingredientsPanel);
		centerColumn.add(detailsPanel);
		add(centerColumn, BorderLayout.CENTER);

		JPanel footer = new JPanel();
		footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
		footer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		footer.setBorder(new EmptyBorder(4, 0, 0, 0));
		verdictLabel.setFont(FontManager.getRunescapeSmallFont());
		craftCostLabel.setFont(FontManager.getRunescapeSmallFont());
		craftCostLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		fromScratchLabel.setFont(FontManager.getRunescapeSmallFont());
		fromScratchLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		footer.add(verdictLabel);
		footer.add(craftCostLabel);
		footer.add(fromScratchLabel);
		add(footer, BorderLayout.SOUTH);

		refresh();
	}

	private JPanel buildHeader(Runnable onRemove)
	{
		JPanel header = new JPanel(new GridBagLayout());
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 4);
		c.anchor = GridBagConstraints.WEST;

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(HEADER_ICON_SIZE, HEADER_ICON_SIZE));
		setScaledIcon(icon, recipe.getItemId(), HEADER_ICON_SIZE);
		c.gridx = 0;
		c.weightx = 0;
		header.add(icon, c);

		JLabel name = new JLabel(recipe.getItemName());
		name.setForeground(Color.WHITE);
		name.setFont(FontManager.getRunescapeBoldFont());
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		header.add(name, c);

		expandToggle.setToolTipText("Show XP, time, and requirements");
		expandToggle.setMargin(new Insets(0, 4, 0, 4));
		expandToggle.setFocusPainted(false);
		expandToggle.setFont(FontManager.getRunescapeSmallFont());
		expandToggle.addActionListener(e -> toggleDetails());
		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 4, 0, 0);
		header.add(expandToggle, c);

		JButton remove = new JButton("×");
		remove.setToolTipText("Remove from watchlist");
		remove.setMargin(new Insets(0, 4, 0, 4));
		remove.setFocusPainted(false);
		remove.setFont(FontManager.getRunescapeBoldFont());
		remove.addActionListener(e -> onRemove.run());
		c.gridx = 3;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 4, 0, 0);
		header.add(remove, c);

		return header;
	}

	private void toggleDetails()
	{
		boolean expanded = !detailsPanel.isVisible();
		detailsPanel.setVisible(expanded);
		expandToggle.setText(expanded ? EXPANDED_ARROW : COLLAPSED_ARROW);
		revalidate();
		repaint();
	}

	private JPanel buildAmountRow()
	{
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(2, 0, 0, 0));

		JLabel qtyLabel = new JLabel("Qty");
		qtyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		qtyLabel.setFont(FontManager.getRunescapeSmallFont());

		amountSpinner.setFont(FontManager.getRunescapeSmallFont());
		amountSpinner.setPreferredSize(new Dimension(60, 20));
		amountSpinner.addChangeListener(e ->
		{
			if (!settingAmountProgrammatically)
			{
				refresh();
			}
		});

		JPanel left = new JPanel(new BorderLayout(4, 0));
		left.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		left.add(qtyLabel, BorderLayout.WEST);
		left.add(amountSpinner, BorderLayout.CENTER);

		priceLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		priceLabel.setFont(FontManager.getRunescapeSmallFont());
		priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		row.add(left, BorderLayout.WEST);
		row.add(priceLabel, BorderLayout.EAST);
		return row;
	}

	private JPanel buildIngredientRow(Ingredient ingredient, int owned, long price, int actions)
	{
		JPanel row = new JPanel(new GridBagLayout());
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(2, 0, 2, 0));

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 4);

		JLabel icon = new JLabel();
		icon.setPreferredSize(new Dimension(INGREDIENT_ICON_SIZE, INGREDIENT_ICON_SIZE));
		setScaledIcon(icon, ingredient.getItemId(), INGREDIENT_ICON_SIZE);
		c.gridx = 0;
		c.weightx = 0;
		row.add(icon, c);

		JLabel name = new JLabel(ingredient.getItemName());
		name.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		name.setFont(FontManager.getRunescapeSmallFont());
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		row.add(name, c);

		JPanel stats = new JPanel();
		stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
		stats.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		int needed = ingredient.getQuantity() * actions;

		JLabel ownedNeeded = new JLabel(owned + " / " + needed);
		ownedNeeded.setFont(FontManager.getRunescapeSmallFont());
		ownedNeeded.setForeground(owned >= needed ? ColorScheme.GRAND_EXCHANGE_PRICE : ColorScheme.PROGRESS_ERROR_COLOR);
		ownedNeeded.setHorizontalAlignment(SwingConstants.RIGHT);
		ownedNeeded.setAlignmentX(RIGHT_ALIGNMENT);

		// The line total for this ingredient, not its unit price - the unit
		// price never changes with the amount, which makes the card look like
		// it isn't scaling. The unit price moves to the tooltip.
		long lineTotal = (long) price * needed;
		JLabel lineCost = new JLabel(GP_FORMAT.format(lineTotal) + " gp");
		lineCost.setFont(FontManager.getRunescapeSmallFont());
		lineCost.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		lineCost.setToolTipText(GP_FORMAT.format(price) + " gp each");
		lineCost.setHorizontalAlignment(SwingConstants.RIGHT);
		lineCost.setAlignmentX(RIGHT_ALIGNMENT);

		stats.add(ownedNeeded);
		stats.add(lineCost);

		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 0, 0, 0);
		row.add(stats, c);

		return row;
	}

	private void setScaledIcon(JLabel label, int itemId, int size)
	{
		AsyncBufferedImage async = itemManager.getImage(itemId);
		async.onLoaded(() -> SwingUtilities.invokeLater(() ->
		{
			label.setIcon(new ImageIcon(ImageUtil.resizeImage(async, size, size, true)));
		}));
	}

	int getItemId()
	{
		return recipe.getItemId();
	}

	/**
	 * Sets the amount without re-triggering our own change listener in a
	 * loop; used when the GE auto-sync pushes a new quantity in. A no-op if
	 * the card is already showing this amount.
	 */
	void setAmount(int amount)
	{
		int clamped = Math.max(1, Math.min(MAX_AMOUNT, amount));
		int current = (Integer) amountSpinner.getValue();
		if (current == clamped)
		{
			return;
		}

		settingAmountProgrammatically = true;
		amountSpinner.setValue(clamped);
		settingAmountProgrammatically = false;
		refresh();
	}

	/**
	 * Overrides the finished item's unit price with whatever price-per-item is
	 * currently typed into a live Grand Exchange offer for it, in place of the
	 * usual average GE price - used when the GE auto-sync pushes a new price
	 * in. A no-op if the card is already showing this price.
	 */
	void setLivePrice(int price)
	{
		if (price <= 0 || (liveUnitPrice != null && liveUnitPrice == price))
		{
			return;
		}

		liveUnitPrice = price;
		refresh();
	}

	/**
	 * Drops the live offer price and goes back to the usual Grand Exchange
	 * price. A no-op if there wasn't one.
	 */
	void clearLivePrice()
	{
		if (liveUnitPrice == null)
		{
			return;
		}

		liveUnitPrice = null;
		refresh();
	}

	/**
	 * Re-reads prices and owned counts and repaints. Safe to call from
	 * anywhere - the actual client/item lookups happen on the client thread.
	 */
	void refresh()
	{
		int amount = (Integer) amountSpinner.getValue();
		Integer offerPrice = liveUnitPrice;

		// Some recipes produce a batch per action (15 herb tars, say), so the
		// ingredients, XP and time are charged per action rather than per item.
		int perAction = Math.max(1, recipe.getOutputQuantity());
		int actions = (amount + perAction - 1) / perAction;

		clientThread.invoke(() ->
		{
			// ItemManager.getItemPrice returns a long as of RuneLite 1.13.0.
			long finishedUnitPrice = offerPrice != null ? offerPrice : itemManager.getItemPrice(recipe.getItemId());
			long finishedTotalPrice = finishedUnitPrice * amount;

			int count = recipe.getIngredients().size();
			long[] prices = new long[count];
			int[] owned = new int[count];
			long totalFromScratch = 0;
			long totalAccountingOwned = 0;

			for (int i = 0; i < count; i++)
			{
				Ingredient ingredient = recipe.getIngredients().get(i);
				prices[i] = itemManager.getItemPrice(ingredient.getItemId());
				owned[i] = plugin.getOwnedCount(ingredient.getItemId());
				int totalNeeded = ingredient.getQuantity() * actions;
				int stillToBuy = Math.max(0, totalNeeded - owned[i]);

				totalFromScratch += (long) totalNeeded * prices[i];
				totalAccountingOwned += (long) stillToBuy * prices[i];
			}

			int currentLevel = plugin.getSkillLevel(recipe.getSkill());
			QuestState questState = recipe.getRequiredQuest() != null
				? plugin.getQuestState(recipe.getRequiredQuest())
				: null;

			long finalFromScratch = totalFromScratch;
			long finalAccountingOwned = totalAccountingOwned;
			SwingUtilities.invokeLater(() -> render(finishedTotalPrice, prices, owned, amount, actions, finalFromScratch,
				finalAccountingOwned, currentLevel, questState));
		});
	}

	private void render(long finishedTotalPrice, long[] prices, int[] owned, int amount, int actions, long totalFromScratch,
		long totalAccountingOwned, int currentLevel, QuestState questState)
	{
		priceLabel.setText(GP_FORMAT.format(finishedTotalPrice) + " gp to buy");
		priceLabel.setToolTipText(liveUnitPrice != null
			? "Synced from the price you're setting in the Grand Exchange offer"
			: null);

		ingredientsPanel.removeAll();
		for (int i = 0; i < recipe.getIngredients().size(); i++)
		{
			ingredientsPanel.add(buildIngredientRow(recipe.getIngredients().get(i), owned[i], prices[i], actions));
		}

		renderDetails(amount, actions, currentLevel, questState);

		long diff = finishedTotalPrice - totalAccountingOwned;
		String verdict = diff > 0
			? "Craft saves " + GP_FORMAT.format(diff) + " gp"
			: diff < 0
				? "Buying saves " + GP_FORMAT.format(-diff) + " gp"
				: "Break even";
		verdictLabel.setText(verdict);
		verdictLabel.setForeground(diff >= 0 ? ColorScheme.GRAND_EXCHANGE_PRICE : ColorScheme.PROGRESS_ERROR_COLOR);

		// Absolute totals, so the actual numbers behind the verdict are visible.
		craftCostLabel.setText("Craft " + GP_FORMAT.format(totalAccountingOwned)
			+ " · buy " + GP_FORMAT.format(finishedTotalPrice));
		craftCostLabel.setToolTipText("What the ingredients you still need would cost, versus buying the finished item");
		fromScratchLabel.setText("(" + GP_FORMAT.format(totalFromScratch) + " gp ignoring what you own)");

		revalidate();
		repaint();
	}

	/**
	 * Rebuilds the (possibly hidden) details drawer: XP, estimated time, skill
	 * level, and quest requirements for making {@code amount} of this item.
	 * Rebuilt on every refresh regardless of whether it's currently visible,
	 * so expanding it never shows stale numbers.
	 */
	private void renderDetails(int amount, int actions, int currentLevel, QuestState questState)
	{
		detailsPanel.removeAll();

		int perAction = Math.max(1, recipe.getOutputQuantity());
		double totalXp = recipe.getXpPerAction() * actions;
		double totalSeconds = recipe.getTicksPerAction() * 0.6 * actions;

		detailsPanel.add(detailLabel(formatXp(recipe.getXpPerAction()) + " xp per make · " + formatXp(totalXp) + " xp total"));
		detailsPanel.add(detailLabel("≈ " + formatDuration(totalSeconds) + " to make " + GP_FORMAT.format(amount)));
		if (perAction > 1)
		{
			detailsPanel.add(detailLabel("makes " + perAction + " at a time · "
				+ GP_FORMAT.format(actions) + (actions == 1 ? " make" : " makes")));
		}
		detailsPanel.add(buildLevelRow(currentLevel));
		detailsPanel.add(buildQuestRow(questState));
	}

	private JLabel detailLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	private JLabel buildLevelRow(int currentLevel)
	{
		boolean met = currentLevel >= recipe.getLevelRequired();
		JLabel label = new JLabel(recipe.getSkill().getName() + " level " + recipe.getLevelRequired()
			+ " required (you: " + currentLevel + ")");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(met ? ColorScheme.GRAND_EXCHANGE_PRICE : ColorScheme.PROGRESS_ERROR_COLOR);
		return label;
	}

	/**
	 * Completed quests render green with a strikethrough; anything not
	 * finished (not started, or in progress) renders red.
	 */
	private JLabel buildQuestRow(QuestState questState)
	{
		Quest quest = recipe.getRequiredQuest();
		JLabel label = new JLabel();
		label.setFont(FontManager.getRunescapeSmallFont());

		if (quest == null)
		{
			label.setText("No quest required");
			label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			return label;
		}

		label.setText("Requires quest: " + quest.getName());
		if (questState == QuestState.FINISHED)
		{
			label.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
			label.setFont(strikeThrough(label.getFont()));
		}
		else
		{
			label.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		}
		return label;
	}

	private static Font strikeThrough(Font font)
	{
		return font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
	}

	private static String formatXp(double xp)
	{
		if (xp == Math.floor(xp))
		{
			return GP_FORMAT.format((long) xp);
		}
		return String.format(Locale.US, "%,.1f", xp);
	}

	private static String formatDuration(double totalSecondsRaw)
	{
		long totalSeconds = Math.round(totalSecondsRaw);
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		if (hours > 0)
		{
			return hours + "h " + minutes + "m";
		}
		if (minutes > 0)
		{
			return minutes + "m " + seconds + "s";
		}
		return seconds + "s";
	}
}
