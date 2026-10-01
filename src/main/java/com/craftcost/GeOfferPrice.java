package com.craftcost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.VarbitComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;

/**
 * Reads the price-per-item showing on the Grand Exchange offer-setup screen,
 * while it's being set and before the offer is confirmed.
 *
 * <p>This reads the number out of the box on screen rather than out of a game
 * variable, which took an embarrassing number of attempts to arrive at. The
 * short version: there is no variable to read. RuneLite exposed one as
 * {@code VarbitID.GE_NEWOFFER_PRICE} up to 1.13.0, the constant disappeared in
 * 1.13.1, and watching every var the client writes while the price is adjusted
 * shows nothing moving but the clock. The varbit isn't renamed or relocated -
 * {@code Client.getVarbit} reports it isn't in the cache at all. The quantity is
 * still a varbit ({@link VarbitID#GE_NEWOFFER_QUANTITY}, and the offer item is
 * still a varp, but the price lives only in the interface until the offer is
 * confirmed.
 *
 * <p>Reading the box has a second advantage worth more than the varbit ever was:
 * it's the number the player can see. The minus and plus buttons, both pairs of
 * percentage buttons, whatever percentages they've configured, and a typed-in
 * value all go through it, so none of them need to be understood separately.
 */
@Slf4j
final class GeOfferPrice
{
	/** Group id of the offer screens, {@code InterfaceID.GE_OFFERS}. */
	private static final int GE_OFFERS_GROUP = 465;

	/**
	 * The label sitting immediately above the price box. The box itself is found
	 * by looking just past this rather than by a fixed index, so that a layout
	 * change shifts both together instead of silently moving the price somewhere
	 * else - the failure mode that has cost the most time here.
	 */
	private static final String PRICE_LABEL = "price per item:";

	/** Where the price box sat when this was written, if the label isn't found. */
	private static final int PRICE_VALUE_FALLBACK_INDEX = 41;

	/** How far past the label to look before giving up. */
	private static final int MAX_LABEL_GAP = 12;

	/**
	 * Marks the price box apart from the quantity box just above it, which holds
	 * a bare number. Anything offered as the price has to be denominated.
	 */
	private static final String COINS = "coin";

	/** Cache archive holding varbit definitions - the id RuneLite's Var Inspector uses. */
	private static final int VARBITS_ARCHIVE = 14;

	/** Carries the map clock. It ticks constantly, so it's pure noise in a diff. */
	private static final int MAP_CLOCK_VARP = 3079;

	/**
	 * Varbits that tick on their own and drown out everything else in a var diff.
	 * The varp they live in is resolved at runtime rather than hardcoded - an
	 * earlier guess at that number was wrong, and the log filled with clock spam.
	 */
	private static final int[] CLOCK_VARBITS = {
		VarbitID.DATE_MILLISECONDS_PAST_MINUTE,
		VarbitID.DATE_SECONDS_PAST_MINUTE,
	};

	/** Most watches per session, so debug mode can't run away with the log. */
	private static final int MAX_WATCHES = 3;

	/** Change lines per watch, same reason. */
	private static final int MAX_DIFF_LINES = 120;

	/** Text entries collected per pass. */
	private static final int MAX_TEXT_ENTRIES = 150;

	/** How deep into the component tree the text walk goes. */
	private static final int MAX_TEXT_DEPTH = 3;

	/** How far to scan for children when walking the group. */
	private static final int MAX_CHILD_SCAN = 64;

	/** Whether the screen's text is being watched for changes. */
	private boolean watchingText;

	/** Last seen text on the offer screens, keyed by component path. */
	private Map<String, String> textSnapshot;

	/** Change lines logged for the current watch, against {@link #MAX_DIFF_LINES}. */
	private int diffLines;

	/** Watches started this session, against {@link #MAX_WATCHES}. */
	private int watchCount;

	/** Varps that tick on their own, resolved once from {@link #CLOCK_VARBITS}. */
	private int[] clockVarps;

	/** Varp index to the varbits packed inside it, built lazily for diagnostics. */
	private Map<Integer, List<Integer>> varbitsByVarp;

	/** Varps as they were at the last diff, for the debug var watch. */
	private int[] varpSnapshot;

	/**
	 * The price-per-item currently showing on the offer-setup screen, or 0 if
	 * that screen isn't up. Must be called on the client thread.
	 *
	 * <p>No staleness guard is needed, and none should be added: unlike a
	 * variable, which holds the last offer's price indefinitely, a hidden screen
	 * has no price box to read.
	 */
	int read(Client client)
	{
		Widget[] children = offerSetupChildren(client);
		if (children == null)
		{
			return 0;
		}

		int start = PRICE_VALUE_FALLBACK_INDEX;
		for (int i = 0; i < children.length; i++)
		{
			String text = plainText(children[i]);
			if (text != null && PRICE_LABEL.equals(text.toLowerCase().trim()))
			{
				start = i + 1;
				break;
			}
		}

		for (int i = start; i < children.length && i < start + MAX_LABEL_GAP; i++)
		{
			int price = parseCoins(plainText(children[i]));
			if (price > 0)
			{
				return price;
			}
		}

		return 0;
	}

	/**
	 * The offer-setup screen's children, or null if that screen isn't showing.
	 */
	private Widget[] offerSetupChildren(Client client)
	{
		try
		{
			Widget setup = client.getWidget(InterfaceID.GeOffers.SETUP);
			if (setup == null || setup.isHidden())
			{
				return null;
			}
			return setup.getDynamicChildren();
		}
		catch (RuntimeException e)
		{
			log.debug("Craft Cost: couldn't read the GE offer-setup screen", e);
			return null;
		}
	}

	/**
	 * A widget's text with any colour tags removed, or null if it has none.
	 * Interface text is marked up - the confirm button reads
	 * {@code <col=ffffff>Confirm</col>} - so the tags have to come off before
	 * anything is matched against it.
	 */
	private static String plainText(Widget widget)
	{
		if (widget == null)
		{
			return null;
		}

		String text = widget.getText();
		if (text == null || text.isEmpty())
		{
			return null;
		}

		return text.replaceAll("<[^>]*>", "");
	}

	/**
	 * Parses a coins amount such as {@code "1,922 coins"}, or 0 if this isn't
	 * one. Requiring the denomination is what keeps the quantity box - a bare
	 * number sitting a few components above - from being read as a price.
	 */
	static int parseCoins(String text)
	{
		if (text == null || !text.toLowerCase().contains(COINS))
		{
			return 0;
		}

		long value = 0;
		boolean anyDigits = false;
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c >= '0' && c <= '9')
			{
				anyDigits = true;
				value = value * 10 + (c - '0');
				if (value > Integer.MAX_VALUE)
				{
					return Integer.MAX_VALUE;
				}
			}
			else if (c != ',' && c != ' ')
			{
				// Hit the start of the word "coins" - or something unexpected,
				// in which case whatever has been read so far is all there is.
				break;
			}
		}

		return anyDigits ? (int) value : 0;
	}

	/** Forget everything cached about the client's state. */
	void reset()
	{
		watchingText = false;
		textSnapshot = null;
		diffLines = 0;
		watchCount = 0;
		clockVarps = null;
		varbitsByVarp = null;
		varpSnapshot = null;
	}

	// ------------------------------------------------------------------
	// Diagnostics
	//
	// These found the price box, and are kept for the next time the interface
	// moves. They are deliberately independent of everything above: an earlier
	// build gated them behind the price varbit, so in the one case worth
	// diagnosing - the varbit being gone - nothing was logged at all.
	// ------------------------------------------------------------------

	/** Called when the offer interface loads; takes the baseline for the var watch. */
	void onScreenOpened(Client client)
	{
		int[] varps = client.getVarps();
		if (varpSnapshot == null || varpSnapshot.length != varps.length)
		{
			varpSnapshot = new int[varps.length];
		}
		System.arraycopy(varps, 0, varpSnapshot, 0, varps.length);
	}

	/**
	 * Starts watching the offer screen's text for changes.
	 *
	 * <p>An earlier attempt dumped the screen a fixed number of var changes after
	 * an item was picked. That never worked: the game clock ticks several times a
	 * second, so the delay elapsed in milliseconds and the dump caught the screen
	 * still reading "Choose an item...". Taking a baseline and reporting only
	 * what changes needs no delay at all, and the price box identifies itself.
	 */
	void requestWatch()
	{
		if (watchCount >= MAX_WATCHES)
		{
			return;
		}

		watchCount++;
		textSnapshot = null;
		diffLines = 0;
		watchingText = true;
	}

	/** The debug hook, run on every var change while the offer screens are open. */
	void onVarChanged(Client client, boolean debug)
	{
		if (!debug)
		{
			return;
		}

		logChangedVars(client);

		if (watchingText)
		{
			watchScreenText(client);
		}
	}

	/**
	 * Takes a baseline of every piece of text on the offer screens, then on each
	 * later call logs only what has changed since.
	 */
	private void watchScreenText(Client client)
	{
		Map<String, String> current = new LinkedHashMap<>();
		for (int child = 0; child < MAX_CHILD_SCAN; child++)
		{
			Widget widget = client.getWidget(GE_OFFERS_GROUP, child);
			if (widget != null)
			{
				collectText(widget, GE_OFFERS_GROUP + "." + child, current, 0);
			}
		}

		if (textSnapshot == null)
		{
			logOfferScreenState(client);
			log.info("Craft Cost debug: watching {} pieces of text on group {} - adjust the price now",
				current.size(), GE_OFFERS_GROUP);
			textSnapshot = current;
			return;
		}

		for (Map.Entry<String, String> entry : current.entrySet())
		{
			if (diffLines >= MAX_DIFF_LINES)
			{
				watchingText = false;
				log.info("Craft Cost debug: stopping after {} changes", diffLines);
				return;
			}

			String before = textSnapshot.get(entry.getKey());
			if (!entry.getValue().equals(before))
			{
				log.info("Craft Cost debug:   {} : \"{}\" -> \"{}\"",
					entry.getKey(), before == null ? "" : before, entry.getValue());
				diffLines++;
			}
		}

		textSnapshot = current;
	}

	/**
	 * Gathers this widget's text and its children's into {@code into}, keyed by
	 * the component path it came from.
	 */
	private void collectText(Widget widget, String path, Map<String, String> into, int depth)
	{
		if (into.size() >= MAX_TEXT_ENTRIES)
		{
			return;
		}

		String text = widget.getText();
		if (text != null && !text.trim().isEmpty())
		{
			into.put(path, text);
		}

		if (depth >= MAX_TEXT_DEPTH)
		{
			return;
		}

		collectChildren(widget.getStaticChildren(), path, "s", into, depth);
		collectChildren(widget.getDynamicChildren(), path, "", into, depth);
		collectChildren(widget.getNestedChildren(), path, "n", into, depth);
	}

	private void collectChildren(Widget[] children, String path, String marker, Map<String, String> into, int depth)
	{
		if (children == null)
		{
			return;
		}

		for (int i = 0; i < children.length; i++)
		{
			if (children[i] != null)
			{
				collectText(children[i], path + "[" + marker + i + "]", into, depth + 1);
			}
		}
	}

	/**
	 * Logs what the plugin can see of the offer interface, so that a watch which
	 * reports nothing still says why.
	 */
	private void logOfferScreenState(Client client)
	{
		Widget setup = null;
		try
		{
			setup = client.getWidget(InterfaceID.GeOffers.SETUP);
		}
		catch (RuntimeException e)
		{
			// reported as absent below
		}

		log.info("Craft Cost debug: GeOffers.SETUP (465.26) is {}",
			setup == null ? "null" : (setup.isHidden() ? "present but hidden" : "present and showing"));
		log.info("Craft Cost debug: price read from the screen is {}", read(client));

		StringBuilder visible = new StringBuilder();
		for (int child = 0; child < MAX_CHILD_SCAN; child++)
		{
			Widget widget = client.getWidget(GE_OFFERS_GROUP, child);
			if (widget != null && !widget.isHidden())
			{
				visible.append(visible.length() == 0 ? "" : ", ").append(child);
			}
		}
		log.info("Craft Cost debug: showing children of 465: [{}]", visible);
	}

	/**
	 * Logs every varp that has changed since the last call, along with the
	 * varbits inside it - named or not.
	 */
	private void logChangedVars(Client client)
	{
		if (varpSnapshot == null)
		{
			onScreenOpened(client);
			return;
		}

		int[] varps = client.getVarps();
		if (varps.length != varpSnapshot.length)
		{
			varpSnapshot = new int[varps.length];
			System.arraycopy(varps, 0, varpSnapshot, 0, varps.length);
			return;
		}

		Map<Integer, List<Integer>> hosted = varbitsByVarp(client);

		for (int index = 0; index < varps.length; index++)
		{
			int before = varpSnapshot[index];
			int after = varps[index];
			if (before == after || isClockVarp(client, index))
			{
				continue;
			}

			log.info("Craft Cost debug: varp {} changed {} -> {}", index, before, after);

			for (int varbitId : hosted.getOrDefault(index, java.util.Collections.emptyList()))
			{
				int varbitBefore = client.getVarbitValue(varpSnapshot, varbitId);
				int varbitAfter = client.getVarbitValue(varps, varbitId);
				if (varbitBefore != varbitAfter)
				{
					log.info("Craft Cost debug:   varbit {} changed {} -> {}",
						varbitId, varbitBefore, varbitAfter);
				}
			}
		}

		System.arraycopy(varps, 0, varpSnapshot, 0, varps.length);
	}

	/**
	 * Whether this varp is one of the self-ticking clocks, whose constant churn
	 * would otherwise bury everything else in the diff.
	 */
	private boolean isClockVarp(Client client, int index)
	{
		if (clockVarps == null)
		{
			List<Integer> found = new ArrayList<>();
			found.add(MAP_CLOCK_VARP);
			for (int varbitId : CLOCK_VARBITS)
			{
				try
				{
					VarbitComposition varbit = client.getVarbit(varbitId);
					if (varbit != null)
					{
						found.add(varbit.getIndex());
					}
				}
				catch (RuntimeException e)
				{
					// nothing to filter if it can't be resolved
				}
			}

			clockVarps = new int[found.size()];
			for (int i = 0; i < found.size(); i++)
			{
				clockVarps[i] = found.get(i);
			}
		}

		for (int varp : clockVarps)
		{
			if (varp == index)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Varp index to the ids of the varbits packed inside it, read from the cache
	 * exactly the way the Var Inspector does. Built once per session.
	 */
	private Map<Integer, List<Integer>> varbitsByVarp(Client client)
	{
		if (varbitsByVarp != null)
		{
			return varbitsByVarp;
		}

		Map<Integer, List<Integer>> byVarp = new HashMap<>();
		try
		{
			IndexDataBase index = client.getIndexConfig();
			for (int id : index.getFileIds(VARBITS_ARCHIVE))
			{
				VarbitComposition varbit = client.getVarbit(id);
				if (varbit != null)
				{
					byVarp.computeIfAbsent(varbit.getIndex(), k -> new ArrayList<>()).add(id);
				}
			}
		}
		catch (RuntimeException e)
		{
			log.debug("Craft Cost: couldn't build the varp -> varbit map", e);
		}

		varbitsByVarp = byVarp;
		return byVarp;
	}
}
