package com.craftcost;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

class CraftCostPanel extends PluginPanel
{
	private static final int MAX_SEARCH_RESULTS = 30;

	private final CraftCostPlugin plugin;
	private final ItemManager itemManager;
	private final ClientThread clientThread;

	private final JTextField searchField = new JTextField();
	private final DefaultListModel<Recipe> searchResultsModel = new DefaultListModel<>();
	private final JList<Recipe> searchResultsList = new JList<>(searchResultsModel);
	private final JScrollPane searchResultsScroll = new JScrollPane(searchResultsList);
	private final JCheckBox autoShowCheckbox = new JCheckBox("Auto-show GE item");
	private final JPanel cardsContainer = new JPanel();
	private final JLabel emptyStateLabel = new JLabel(
		"<html><center>Nothing in your watchlist yet.<br>Search for an item above, "
			+ "or turn on auto-show and browse one at the GE.</center></html>");

	private final Map<Integer, RecipeCardPanel> cardsByItemId = new LinkedHashMap<>();
	private boolean loading;

	CraftCostPanel(CraftCostPlugin plugin, ItemManager itemManager, ClientThread clientThread)
	{
		super();
		this.plugin = plugin;
		this.itemManager = itemManager;
		this.clientThread = clientThread;

		setLayout(new BorderLayout(0, 6));

		add(buildControls(), BorderLayout.NORTH);

		cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
		cardsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(cardsContainer, BorderLayout.CENTER);

		emptyStateLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		emptyStateLabel.setFont(FontManager.getRunescapeSmallFont());
		emptyStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
		emptyStateLabel.setBorder(new EmptyBorder(16, 4, 0, 4));

		loadPersistedWatchlist();
		updateEmptyState();
	}

	private JPanel buildControls()
	{
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel searchLabel = new JLabel("Add to watchlist");
		searchLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		searchLabel.setFont(FontManager.getRunescapeSmallFont());
		searchLabel.setAlignmentX(LEFT_ALIGNMENT);
		controls.add(searchLabel);
		controls.add(Box.createVerticalStrut(2));

		searchField.setFont(FontManager.getRunescapeSmallFont());
		searchField.setToolTipText("Type an item name to search everything this plugin knows how to make");
		searchField.setAlignmentX(LEFT_ALIGNMENT);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateSearchResults();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateSearchResults();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				updateSearchResults();
			}
		});
		searchField.addActionListener(e -> addFirstSearchResult());
		controls.add(searchField);

		searchResultsList.setFont(FontManager.getRunescapeSmallFont());
		searchResultsList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchResultsList.setVisibleRowCount(6);
		searchResultsList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int index = searchResultsList.locationToIndex(e.getPoint());
				if (index >= 0)
				{
					addFromSearch(searchResultsModel.getElementAt(index));
				}
			}
		});

		searchResultsScroll.setAlignmentX(LEFT_ALIGNMENT);
		searchResultsScroll.setBorder(null);
		searchResultsScroll.setVisible(false);
		controls.add(searchResultsScroll);

		controls.add(Box.createVerticalStrut(4));

		autoShowCheckbox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		autoShowCheckbox.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		autoShowCheckbox.setFont(FontManager.getRunescapeSmallFont());
		autoShowCheckbox.setToolTipText("While on, browsing a known recipe's price at the Grand Exchange adds it here automatically.");
		autoShowCheckbox.setSelected(plugin.isAutoShowGeItem());
		autoShowCheckbox.addActionListener(e -> plugin.setAutoShowGeItem(autoShowCheckbox.isSelected()));

		JButton refreshAll = new JButton("⟳");
		refreshAll.setToolTipText("Refresh all prices and owned counts");
		refreshAll.addActionListener(e -> onOwnedItemsChanged());

		JPanel toggleRow = new JPanel(new BorderLayout(4, 0));
		toggleRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		toggleRow.add(autoShowCheckbox, BorderLayout.CENTER);
		toggleRow.add(refreshAll, BorderLayout.EAST);
		controls.add(toggleRow);

		return controls;
	}

	/**
	 * Filters {@link RecipeDatabase#RECIPES} by whatever's typed in the search
	 * box (case-insensitive substring match on item name) and shows the
	 * matches below it, capped at {@link #MAX_SEARCH_RESULTS} so one very
	 * broad query (e.g. "potion") doesn't dump hundreds of rows at once.
	 */
	private void updateSearchResults()
	{
		String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
		searchResultsModel.clear();

		if (!query.isEmpty())
		{
			for (Recipe recipe : RecipeDatabase.RECIPES)
			{
				if (recipe.getItemName().toLowerCase(Locale.ROOT).contains(query))
				{
					searchResultsModel.addElement(recipe);
					if (searchResultsModel.size() >= MAX_SEARCH_RESULTS)
					{
						break;
					}
				}
			}
		}

		searchResultsScroll.setVisible(!searchResultsModel.isEmpty());
		revalidate();
		repaint();
	}

	private void addFromSearch(Recipe recipe)
	{
		addRecipe(recipe);
		searchField.setText("");
		searchResultsModel.clear();
		searchResultsScroll.setVisible(false);
		revalidate();
		repaint();
	}

	private void addFirstSearchResult()
	{
		if (!searchResultsModel.isEmpty())
		{
			addFromSearch(searchResultsModel.getElementAt(0));
		}
	}

	private void loadPersistedWatchlist()
	{
		loading = true;
		for (int itemId : plugin.loadWatchlist())
		{
			Recipe recipe = RecipeDatabase.findByItemId(itemId);
			if (recipe != null)
			{
				addRecipe(recipe);
			}
		}
		loading = false;
	}

	/**
	 * Called by the plugin when a known recipe is spotted at the GE while
	 * "Auto-show GE item" is on. Runs on the client thread, so hop to the EDT.
	 */
	void autoAddFromGe(Recipe recipe)
	{
		SwingUtilities.invokeLater(() -> addRecipe(recipe));
	}

	/**
	 * Called by the plugin when the Grand Exchange offer quantity changes for
	 * a recipe already known to the panel, while auto-show is on. Runs on
	 * the client thread, so hop to the EDT. A no-op if that recipe isn't
	 * (yet) in the watchlist.
	 */
	void autoSetAmount(Recipe recipe, int amount)
	{
		SwingUtilities.invokeLater(() ->
		{
			RecipeCardPanel card = cardsByItemId.get(recipe.getItemId());
			if (card != null)
			{
				card.setAmount(amount);
			}
		});
	}

	/**
	 * Called by the plugin when the Grand Exchange offer price-per-item
	 * changes for a recipe already known to the panel, while auto-show is on.
	 * Runs on the client thread, so hop to the EDT. A no-op if that recipe
	 * isn't (yet) in the watchlist.
	 */
	void autoSetPrice(Recipe recipe, int price)
	{
		SwingUtilities.invokeLater(() ->
		{
			RecipeCardPanel card = cardsByItemId.get(recipe.getItemId());
			if (card != null)
			{
				card.setLivePrice(price);
			}
		});
	}

	private void addRecipe(Recipe recipe)
	{
		if (cardsByItemId.containsKey(recipe.getItemId()))
		{
			return;
		}

		RecipeCardPanel card = new RecipeCardPanel(recipe, plugin, itemManager, clientThread, () -> removeRecipe(recipe));
		cardsByItemId.put(recipe.getItemId(), card);
		cardsContainer.add(card);
		cardsContainer.add(Box.createVerticalStrut(6));

		persistWatchlist();
		updateEmptyState();
		cardsContainer.revalidate();
		cardsContainer.repaint();
	}

	private void removeRecipe(Recipe recipe)
	{
		RecipeCardPanel card = cardsByItemId.remove(recipe.getItemId());
		if (card == null)
		{
			return;
		}

		int index = cardsContainer.getComponentZOrder(card);
		cardsContainer.remove(card);
		// remove the spacer strut that follows the card, if it's still there
		if (index >= 0 && index < cardsContainer.getComponentCount())
		{
			cardsContainer.remove(index);
		}

		persistWatchlist();
		updateEmptyState();
		cardsContainer.revalidate();
		cardsContainer.repaint();
	}

	private void persistWatchlist()
	{
		if (loading)
		{
			return;
		}
		plugin.saveWatchlist(new ArrayList<>(cardsByItemId.keySet()));
	}

	private void updateEmptyState()
	{
		boolean empty = cardsByItemId.isEmpty();
		if (empty && emptyStateLabel.getParent() == null)
		{
			cardsContainer.add(emptyStateLabel);
		}
		else if (!empty && emptyStateLabel.getParent() != null)
		{
			cardsContainer.remove(emptyStateLabel);
		}
	}

	/**
	 * Called by the plugin when the Grand Exchange offer screen closes, so no
	 * card is left showing a price from an offer that isn't on screen any more.
	 */
	void autoClearLivePrices()
	{
		SwingUtilities.invokeLater(() ->
		{
			for (RecipeCardPanel card : cardsByItemId.values())
			{
				card.clearLivePrice();
			}
		});
	}

	/**
	 * Called by the plugin whenever inventory/bank contents change, so every
	 * card in the watchlist stays up to date without a manual refresh.
	 */
	void onOwnedItemsChanged()
	{
		SwingUtilities.invokeLater(() ->
		{
			for (RecipeCardPanel card : cardsByItemId.values())
			{
				card.refresh();
			}
		});
	}
}
