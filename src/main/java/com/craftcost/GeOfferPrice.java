package com.craftcost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.VarbitComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

/**
 * Reads the price-per-item currently showing on the Grand Exchange offer-setup
 * screen, while it's being typed and before the offer is confirmed.
 *
 * <p>This used to be a one-liner against {@code VarbitID.GE_NEWOFFER_PRICE}.
 * That constant disappeared in RuneLite 1.13.1 and the plugin stopped building,
 * so the price sync was ripped out as a stopgap. The constant going away is not
 * the same as the varbit going away, though: the {@code gameval} classes are
 * generated from the symbol names shipped in the game cache, and this varbit
 * simply lost its name there while 4396 and 4397 either side of it kept theirs.
 * So the id is read directly, guarded by a runtime check that it still exists,
 * which also means this file keeps compiling whatever the client does to its
 * generated constants next.
 */
@Slf4j
final class GeOfferPrice
{
	/**
	 * Varbit that holds the live price-per-item on the offer-setup screen.
	 * {@code VarbitID.GE_NEWOFFER_PRICE} in RuneLite 1.13.0 and earlier.
	 * <p>
	 * Unlike the quantity, this one never arrives as a varbit-flavoured
	 * {@code VarbitChanged} - see {@link #read} for what that means for how it
	 * has to be read.
	 */
	static final int NEWOFFER_PRICE_VARBIT = 4398;

	/** Cache archive holding varbit definitions - the id RuneLite's Var Inspector uses. */
	private static final int VARBITS_ARCHIVE = 14;

	/** Carries the in-game clock. It ticks every cycle, so it's pure noise in a diff. */
	private static final int MAP_CLOCK_VARP = 3079;

	/** Group id of the offer screens, {@code InterfaceID.GE_OFFERS}. */
	private static final int GE_OFFERS_GROUP = 465;

	/** How far to scan for children when dumping the offer screen's text. */
	private static final int MAX_CHILD_SCAN = 64;

	/** Ceiling on dump output, so a bad guess can't flood anybody's log. */
	private static final int MAX_DUMP_LINES = 150;

	/**
	 * Tri-state for whether the price varbit is still in the cache. Resolved
	 * once per session on the client thread, because {@link Client#getVarbit}
	 * reads the config index.
	 */
	private Boolean priceVarbitPresent;

	/** Whether the offer-setup screen was showing last time {@link #read} looked. */
	private boolean setupVisible;

	/** What the price box held when the setup screen opened, i.e. the guide price. */
	private Integer openingPrice;

	/** Set once the player has moved the price off {@link #openingPrice}. */
	private boolean priceEdited;

	/** Varp index to the varbits living inside it, built lazily for diagnostics. */
	private Map<Integer, List<Integer>> varbitsByVarp;

	/** Varps as they were when the offer screen opened, for diffing. */
	private int[] varpSnapshot;

	/**
	 * The price-per-item the player has entered on the offer-setup screen, or 0
	 * if there isn't one to trust yet. Must be called on the client thread.
	 *
	 * <p>Two things make this more involved than reading the varbit.
	 *
	 * <p>First, the quantity can be picked up by matching
	 * {@code VarbitChanged.getVarbitId()} against its varbit, but the price
	 * cannot - that event never carries this id. A price needs about 31 bits, so
	 * it occupies essentially the whole varp and the game writes the varp
	 * directly rather than packing bits into it, which is also the likeliest
	 * reason the name was dropped from the cache in the first place: as a
	 * "varbit" spanning its entire host, it is degenerate. So the value has to be
	 * polled rather than waited for.
	 *
	 * <p>Second, polling it is only safe with a guard. The offers interface is
	 * open the whole time the player is standing at a booth, and the varbit keeps
	 * the last offer's price, so a poll at the wrong moment reports a price from
	 * a completely unrelated offer. Hence the two conditions below: the
	 * offer-setup screen has to actually be on screen, and the value has to have
	 * moved off whatever it held when that screen opened. The opening value is
	 * the guide price the box is pre-filled with, which is what the card shows
	 * anyway, so nothing is lost by ignoring it until the player changes it.
	 */
	int read(Client client)
	{
		if (!isPriceVarbitPresent(client))
		{
			return 0;
		}

		if (!isOfferSetupVisible(client))
		{
			setupVisible = false;
			openingPrice = null;
			priceEdited = false;
			return 0;
		}

		int price = rawRead(client);
		if (price <= 0)
		{
			return 0;
		}

		if (!setupVisible)
		{
			// The setup screen has just appeared. Whatever is in the box now is
			// the pre-filled guide price, not something the player chose.
			setupVisible = true;
			openingPrice = price;
			return 0;
		}

		if (!priceEdited)
		{
			if (openingPrice != null && price == openingPrice)
			{
				return 0;
			}
			priceEdited = true;
		}

		return price;
	}

	/** The varbit's current value, or 0 if it can't be read. */
	private int rawRead(Client client)
	{
		try
		{
			int price = client.getVarbitValue(NEWOFFER_PRICE_VARBIT);
			// A negative value would mean we're reading bits that no longer mean
			// what we think they mean, and showing a nonsense price is worse
			// than showing the guide price.
			return price > 0 ? price : 0;
		}
		catch (RuntimeException e)
		{
			log.debug("Craft Cost: reading the GE offer price varbit failed", e);
			priceVarbitPresent = Boolean.FALSE;
			return 0;
		}
	}

	/**
	 * Whether the offer-setup screen - the one with the quantity and price boxes
	 * on it - is actually showing, as opposed to the offers list behind it.
	 */
	private boolean isOfferSetupVisible(Client client)
	{
		try
		{
			Widget setup = client.getWidget(InterfaceID.GeOffers.SETUP);
			return setup != null && !setup.isHidden();
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	/**
	 * Whether varbit {@value #NEWOFFER_PRICE_VARBIT} is still defined in the
	 * game cache. If it isn't, live price syncing is quietly unavailable and the
	 * cards fall back to the Grand Exchange guide price.
	 */
	private boolean isPriceVarbitPresent(Client client)
	{
		if (priceVarbitPresent != null)
		{
			return priceVarbitPresent;
		}

		boolean present;
		try
		{
			present = client.getVarbit(NEWOFFER_PRICE_VARBIT) != null;
		}
		catch (RuntimeException e)
		{
			present = false;
		}

		priceVarbitPresent = present;

		if (present)
		{
			log.debug("Craft Cost: live GE offer price available from varbit {}", NEWOFFER_PRICE_VARBIT);
		}
		else
		{
			log.info("Craft Cost: varbit {} is not in this client's cache, so the Grand Exchange"
				+ " price-per-item can't be synced live. Cards will use the guide price."
				+ " Enable \"Log GE offer screen (debug)\" to help track down its replacement.",
				NEWOFFER_PRICE_VARBIT);
		}

		return present;
	}

	/** Forget everything cached about the client's state. */
	void reset()
	{
		priceVarbitPresent = null;
		varbitsByVarp = null;
		varpSnapshot = null;
		setupVisible = false;
		openingPrice = null;
		priceEdited = false;
	}

	// ------------------------------------------------------------------
	// Diagnostics
	//
	// If the varbit above ever does disappear for real, the replacement has to
	// be found empirically. Doing that through RuneLite's Var Inspector means
	// picking the price changes out of the clock varps that tick every cycle,
	// and it's easy to screenshot a window that didn't contain a price change at
	// all. The two dumps below are the same idea scoped to the offer screen:
	// every var that moves while it's open, and every piece of text on it.
	// ------------------------------------------------------------------

	/**
	 * Called when the offer screen opens. Takes the baseline for
	 * {@link #logChangedVars} and dumps the screen's text once.
	 */
	void onScreenOpened(Client client, boolean debug)
	{
		int[] varps = client.getVarps();
		if (varpSnapshot == null || varpSnapshot.length != varps.length)
		{
			varpSnapshot = new int[varps.length];
		}
		System.arraycopy(varps, 0, varpSnapshot, 0, varps.length);

		// Resolve varbit availability now rather than on the first price read,
		// so the "not in cache" line lands before any confusion about why the
		// panel isn't following the offer.
		isPriceVarbitPresent(client);

		if (debug)
		{
			logOfferScreenText(client);
		}
	}

	/**
	 * Logs every varp that has changed since the last call, along with the
	 * varbits inside it - named or not. Run on each var change while the offer
	 * screen is open, with the debug toggle on.
	 */
	void logChangedVars(Client client)
	{
		if (varpSnapshot == null)
		{
			onScreenOpened(client, false);
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
			if (before == after || index == MAP_CLOCK_VARP)
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

	/**
	 * Dumps every bit of text on the offer screens, with the component id it
	 * came from. If the price is no longer in a var at all, this is where it
	 * will be, and the component id is what the replacement would read.
	 */
	private void logOfferScreenText(Client client)
	{
		log.info("Craft Cost debug: dumping text on group {} (Grand Exchange offers)", GE_OFFERS_GROUP);

		int lines = 0;
		for (int child = 0; child < MAX_CHILD_SCAN && lines < MAX_DUMP_LINES; child++)
		{
			Widget widget = client.getWidget(GE_OFFERS_GROUP, child);
			if (widget == null)
			{
				continue;
			}

			lines += logWidgetText(widget, GE_OFFERS_GROUP + "." + child, MAX_DUMP_LINES - lines);
		}

		if (lines == 0)
		{
			log.info("Craft Cost debug: no text found - is the offer-setup screen actually showing?");
		}
	}

	/**
	 * Logs this widget's text and its children's, returning how many lines were
	 * written so the caller can keep to its budget.
	 */
	private int logWidgetText(Widget widget, String path, int budget)
	{
		if (budget <= 0)
		{
			return 0;
		}

		int lines = 0;

		String text = widget.getText();
		if (text != null && !text.trim().isEmpty())
		{
			log.info("Craft Cost debug:   {} type={} text=\"{}\"", path, widget.getType(), text);
			lines++;
		}

		lines += logChildArray(widget.getStaticChildren(), path, "s", budget - lines);
		lines += logChildArray(widget.getDynamicChildren(), path, "", budget - lines);
		lines += logChildArray(widget.getNestedChildren(), path, "n", budget - lines);

		return lines;
	}

	private int logChildArray(Widget[] children, String path, String marker, int budget)
	{
		if (children == null || budget <= 0)
		{
			return 0;
		}

		int lines = 0;
		for (int i = 0; i < children.length && lines < budget; i++)
		{
			Widget child = children[i];
			if (child == null)
			{
				continue;
			}

			// Only one level down. The offer screen's text sits directly on the
			// setup component, and going deeper buries it in scrollbar parts.
			String text = child.getText();
			if (text != null && !text.trim().isEmpty())
			{
				log.info("Craft Cost debug:   {}[{}{}] type={} text=\"{}\"",
					path, marker, i, child.getType(), text);
				lines++;
			}
		}
		return lines;
	}
}
