package com.craftcost;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;

/**
 * Every Fletching recipe the plugin knows how to make.
 * <p>
 * Items are referenced through RuneLite's own {@link ItemID} constants rather
 * than raw numbers, so a wrong item name fails the build instead of silently
 * pricing the wrong thing. Every id was resolved from RuneLite's ItemID table
 * and independently confirmed to be Grand Exchange tradeable; levels, XP,
 * ingredients and batch sizes were read from the OSRS Wiki one item page at a
 * time.
 * <p>
 * Fletching is full of names that look derivable and are not. The unfinished
 * adamant bolt is {@code Adamant bolts(unf)} with no space before the bracket,
 * alone among its tier. Arrowtips are one word and plural, dart tips are two
 * words and singular, javelin tips are two words and plural, and the finished
 * ammunition is singular throughout. Runite limbs and a Runite crossbow (u)
 * string into a Rune crossbow, while Adamantite limbs make an Adamant one. All
 * of these are written out in full rather than built from a metal name.
 * <p>
 * The blurite line is absent: blurite limbs, bolts and crossbows are
 * untradeable and gated behind The Knight's Sword, so there is no Grand
 * Exchange price to compare against. That also removes jade bolts, the one gem
 * bolt tipped onto blurite rather than a mainstream metal.
 * <p>
 * Only one arrow shaft recipe exists, from plain logs. Every log type cuts into
 * the same Arrow shaft item, and the database looks recipes up by their
 * finished item, so the others would be unreachable.
 * <p>
 * XP is for the step these ingredients pay for. Cutting a bow and stringing it
 * are separate recipes with separate figures, and those figures are not always
 * equal - the wiki rounds the same underlying value differently for willow
 * shortbows (33.3 cutting, 33.2 stringing) and maple longbows (58.3 / 58.2).
 * Two figures the wiki's own Fletching calculator gets wrong are taken from the
 * item pages instead: a bronze arrow set is 19.5 xp, not 20, and steel darts
 * need level 37, not 38.
 */
final class FletchingRecipes
{
	static final List<Recipe> RECIPES = Collections.unmodifiableList(Arrays.asList(
		recipe(ItemID.ARROW_SHAFT, "Arrow shaft", 1, 5, 3, 15,
			ingredient(ItemID.LOGS, "Logs", 1)),
		recipe(ItemID.UNSTRUNG_SHORTBOW, "Shortbow (u)", 5, 5, 3, 1,
			ingredient(ItemID.LOGS, "Logs", 1)),
		recipe(ItemID.SHORTBOW, "Shortbow", 5, 5, 2, 1,
			ingredient(ItemID.UNSTRUNG_SHORTBOW, "Shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_LONGBOW, "Longbow (u)", 10, 10, 3, 1,
			ingredient(ItemID.LOGS, "Logs", 1)),
		recipe(ItemID.LONGBOW, "Longbow", 10, 10, 2, 1,
			ingredient(ItemID.UNSTRUNG_LONGBOW, "Longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_OAK_SHORTBOW, "Oak shortbow (u)", 20, 16.5, 3, 1,
			ingredient(ItemID.OAK_LOGS, "Oak logs", 1)),
		recipe(ItemID.OAK_SHORTBOW, "Oak shortbow", 20, 16.5, 2, 1,
			ingredient(ItemID.UNSTRUNG_OAK_SHORTBOW, "Oak shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_OAK_LONGBOW, "Oak longbow (u)", 25, 25, 3, 1,
			ingredient(ItemID.OAK_LOGS, "Oak logs", 1)),
		recipe(ItemID.OAK_LONGBOW, "Oak longbow", 25, 25, 2, 1,
			ingredient(ItemID.UNSTRUNG_OAK_LONGBOW, "Oak longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_WILLOW_SHORTBOW, "Willow shortbow (u)", 35, 33.3, 3, 1,
			ingredient(ItemID.WILLOW_LOGS, "Willow logs", 1)),
		recipe(ItemID.WILLOW_SHORTBOW, "Willow shortbow", 35, 33.2, 2, 1,
			ingredient(ItemID.UNSTRUNG_WILLOW_SHORTBOW, "Willow shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_WILLOW_LONGBOW, "Willow longbow (u)", 40, 41.5, 3, 1,
			ingredient(ItemID.WILLOW_LOGS, "Willow logs", 1)),
		recipe(ItemID.WILLOW_LONGBOW, "Willow longbow", 40, 41.5, 2, 1,
			ingredient(ItemID.UNSTRUNG_WILLOW_LONGBOW, "Willow longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_MAPLE_SHORTBOW, "Maple shortbow (u)", 50, 50, 3, 1,
			ingredient(ItemID.MAPLE_LOGS, "Maple logs", 1)),
		recipe(ItemID.MAPLE_SHORTBOW, "Maple shortbow", 50, 50, 2, 1,
			ingredient(ItemID.UNSTRUNG_MAPLE_SHORTBOW, "Maple shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_MAPLE_LONGBOW, "Maple longbow (u)", 55, 58.3, 3, 1,
			ingredient(ItemID.MAPLE_LOGS, "Maple logs", 1)),
		recipe(ItemID.MAPLE_LONGBOW, "Maple longbow", 55, 58.2, 2, 1,
			ingredient(ItemID.UNSTRUNG_MAPLE_LONGBOW, "Maple longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_YEW_SHORTBOW, "Yew shortbow (u)", 65, 67.5, 3, 1,
			ingredient(ItemID.YEW_LOGS, "Yew logs", 1)),
		recipe(ItemID.YEW_SHORTBOW, "Yew shortbow", 65, 67.5, 2, 1,
			ingredient(ItemID.UNSTRUNG_YEW_SHORTBOW, "Yew shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_YEW_LONGBOW, "Yew longbow (u)", 70, 75, 3, 1,
			ingredient(ItemID.YEW_LOGS, "Yew logs", 1)),
		recipe(ItemID.YEW_LONGBOW, "Yew longbow", 70, 75, 2, 1,
			ingredient(ItemID.UNSTRUNG_YEW_LONGBOW, "Yew longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_MAGIC_SHORTBOW, "Magic shortbow (u)", 80, 83.3, 3, 1,
			ingredient(ItemID.MAGIC_LOGS, "Magic logs", 1)),
		recipe(ItemID.MAGIC_SHORTBOW, "Magic shortbow", 80, 83.3, 2, 1,
			ingredient(ItemID.UNSTRUNG_MAGIC_SHORTBOW, "Magic shortbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.UNSTRUNG_MAGIC_LONGBOW, "Magic longbow (u)", 85, 91.5, 3, 1,
			ingredient(ItemID.MAGIC_LOGS, "Magic logs", 1)),
		recipe(ItemID.MAGIC_LONGBOW, "Magic longbow", 85, 91.5, 2, 1,
			ingredient(ItemID.UNSTRUNG_MAGIC_LONGBOW, "Magic longbow (u)", 1),
			ingredient(ItemID.BOW_STRING, "Bow string", 1)),
		recipe(ItemID.HEADLESS_ARROW, "Headless arrow", 1, 15, 2, 15,
			ingredient(ItemID.ARROW_SHAFT, "Arrow shaft", 15),
			ingredient(ItemID.FEATHER, "Feather", 15)),
		recipe(ItemID.BRONZE_ARROW, "Bronze arrow", 1, 19.5, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.BRONZE_ARROWHEADS, "Bronze arrowtips", 15)),
		recipe(ItemID.IRON_ARROW, "Iron arrow", 15, 37.5, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.IRON_ARROWHEADS, "Iron arrowtips", 15)),
		recipe(ItemID.STEEL_ARROW, "Steel arrow", 30, 75, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.STEEL_ARROWHEADS, "Steel arrowtips", 15)),
		recipe(ItemID.MITHRIL_ARROW, "Mithril arrow", 45, 112.5, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.MITHRIL_ARROWHEADS, "Mithril arrowtips", 15)),
		recipe(ItemID.ADAMANT_ARROW, "Adamant arrow", 60, 150, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.ADAMANT_ARROWHEADS, "Adamant arrowtips", 15)),
		recipe(ItemID.RUNE_ARROW, "Rune arrow", 75, 187.5, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.RUNE_ARROWHEADS, "Rune arrowtips", 15)),
		recipe(ItemID.AMETHYST_ARROW, "Amethyst arrow", 82, 202.5, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.AMETHYST_ARROWHEADS, "Amethyst arrowtips", 15)),
		recipe(ItemID.DRAGON_ARROW, "Dragon arrow", 90, 225, 2, 15,
			ingredient(ItemID.HEADLESS_ARROW, "Headless arrow", 15),
			ingredient(ItemID.DRAGON_ARROWHEADS, "Dragon arrowtips", 15)),
		recipe(ItemID.BRONZE_DART, "Bronze dart", 10, 18, 2, 10,
			ingredient(ItemID.BRONZE_DART_TIP, "Bronze dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.IRON_DART, "Iron dart", 22, 38, 2, 10,
			ingredient(ItemID.IRON_DART_TIP, "Iron dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.STEEL_DART, "Steel dart", 37, 75, 2, 10,
			ingredient(ItemID.STEEL_DART_TIP, "Steel dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.MITHRIL_DART, "Mithril dart", 52, 112, 2, 10,
			ingredient(ItemID.MITHRIL_DART_TIP, "Mithril dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.ADAMANT_DART, "Adamant dart", 67, 150, 2, 10,
			ingredient(ItemID.ADAMANT_DART_TIP, "Adamant dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.RUNE_DART, "Rune dart", 81, 188, 2, 10,
			ingredient(ItemID.RUNE_DART_TIP, "Rune dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.AMETHYST_DART, "Amethyst dart", 90, 210, 2, 10,
			ingredient(ItemID.AMETHYST_DART_TIP, "Amethyst dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.DRAGON_DART, "Dragon dart", 95, 250, 2, 10,
			ingredient(ItemID.DRAGON_DART_TIP, "Dragon dart tip", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.BRONZE_JAVELIN, "Bronze javelin", 3, 15, 1, 15,
			ingredient(ItemID.BRONZE_JAVELIN_HEAD, "Bronze javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.IRON_JAVELIN, "Iron javelin", 17, 30, 1, 15,
			ingredient(ItemID.IRON_JAVELIN_HEAD, "Iron javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.STEEL_JAVELIN, "Steel javelin", 32, 75, 1, 15,
			ingredient(ItemID.STEEL_JAVELIN_HEAD, "Steel javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.MITHRIL_JAVELIN, "Mithril javelin", 47, 120, 1, 15,
			ingredient(ItemID.MITHRIL_JAVELIN_HEAD, "Mithril javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.ADAMANT_JAVELIN, "Adamant javelin", 62, 150, 1, 15,
			ingredient(ItemID.ADAMANT_JAVELIN_HEAD, "Adamant javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.RUNE_JAVELIN, "Rune javelin", 77, 186, 1, 15,
			ingredient(ItemID.RUNE_JAVELIN_HEAD, "Rune javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.AMETHYST_JAVELIN, "Amethyst javelin", 84, 202.5, 1, 15,
			ingredient(ItemID.AMETHYST_JAVELIN_HEAD, "Amethyst javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.DRAGON_JAVELIN, "Dragon javelin", 92, 225, 1, 15,
			ingredient(ItemID.DRAGON_JAVELIN_HEAD, "Dragon javelin tips", 15),
			ingredient(ItemID.JAVELIN_SHAFT, "Javelin shaft", 15)),
		recipe(ItemID.BOLT, "Bronze bolts", 9, 5, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_BRONZE_UNFEATHERED, "Bronze bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_IRON, "Iron bolts", 39, 15, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_IRON_UNFEATHERED, "Iron bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_STEEL, "Steel bolts", 46, 35, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_STEEL_UNFEATHERED, "Steel bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL, "Mithril bolts", 54, 50, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_UNFEATHERED, "Mithril bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE, "Adamant bolts", 61, 70, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_UNFEATHERED, "Adamant bolts(unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE, "Runite bolts", 69, 100, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_UNFEATHERED, "Runite bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.DRAGON_BOLTS, "Dragon bolts", 84, 120, 2, 10,
			ingredient(ItemID.DRAGON_BOLTS_UNFEATHERED, "Dragon bolts (unf)", 10),
			ingredient(ItemID.FEATHER, "Feather", 10)),
		recipe(ItemID.OPAL_BOLT, "Opal bolts", 11, 16, 2, 10,
			ingredient(ItemID.BOLT, "Bronze bolts", 10),
			ingredient(ItemID.OPAL_BOLTTIPS, "Opal bolt tips", 10)),
		recipe(ItemID.PEARL_BOLT, "Pearl bolts", 41, 32, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_IRON, "Iron bolts", 10),
			ingredient(ItemID.PEARL_BOLTTIPS, "Pearl bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_STEEL_TIPPED_REDTOPAZ, "Topaz bolts", 48, 39, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_STEEL, "Steel bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_REDTOPAZ, "Topaz bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_SAPPHIRE, "Sapphire bolts", 56, 47, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL, "Mithril bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_SAPPHIRE, "Sapphire bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_EMERALD, "Emerald bolts", 58, 55, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL, "Mithril bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_EMERALD, "Emerald bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_RUBY, "Ruby bolts", 63, 63, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE, "Adamant bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_RUBY, "Ruby bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_DIAMOND, "Diamond bolts", 65, 70, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE, "Adamant bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_DIAMOND, "Diamond bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_DRAGONSTONE, "Dragonstone bolts", 71, 82, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE, "Runite bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_DRAGONSTONE, "Dragonstone bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_ONYX, "Onyx bolts", 73, 94, 2, 10,
			ingredient(ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE, "Runite bolts", 10),
			ingredient(ItemID.XBOWS_BOLT_TIPS_ONYX, "Onyx bolt tips", 10)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_WOOD, "Wooden stock", 9, 6, 3, 1,
			ingredient(ItemID.LOGS, "Logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_OAK, "Oak stock", 24, 16, 3, 1,
			ingredient(ItemID.OAK_LOGS, "Oak logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_WILLOW, "Willow stock", 39, 22, 3, 1,
			ingredient(ItemID.WILLOW_LOGS, "Willow logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_TEAK, "Teak stock", 46, 27, 3, 1,
			ingredient(ItemID.TEAK_LOGS, "Teak logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_MAPLE, "Maple stock", 54, 32, 3, 1,
			ingredient(ItemID.MAPLE_LOGS, "Maple logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_MAHOGANY, "Mahogany stock", 61, 41, 3, 1,
			ingredient(ItemID.MAHOGANY_LOGS, "Mahogany logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_YEW, "Yew stock", 69, 50, 3, 1,
			ingredient(ItemID.YEW_LOGS, "Yew logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STOCK_MAGIC, "Magic stock", 78, 70, 3, 1,
			ingredient(ItemID.MAGIC_LOGS, "Magic logs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_BRONZE, "Bronze crossbow (u)", 9, 12, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_WOOD, "Wooden stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_BRONZE, "Bronze limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_BRONZE, "Bronze crossbow", 9, 6, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_BRONZE, "Bronze crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_IRON, "Iron crossbow (u)", 39, 44, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_WILLOW, "Willow stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_IRON, "Iron limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_IRON, "Iron crossbow", 39, 22, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_IRON, "Iron crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_STEEL, "Steel crossbow (u)", 46, 54, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_TEAK, "Teak stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_STEEL, "Steel limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_STEEL, "Steel crossbow", 46, 27, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_STEEL, "Steel crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_MITHRIL, "Mithril crossbow (u)", 54, 64, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAPLE, "Maple stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_MITHRIL, "Mithril limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_MITHRIL, "Mithril crossbow", 54, 32, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_MITHRIL, "Mithril crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_ADAMANTITE, "Adamant crossbow (u)", 61, 82, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAHOGANY, "Mahogany stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_ADAMANTITE, "Adamantite limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_ADAMANTITE, "Adamant crossbow", 61, 41, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_ADAMANTITE, "Adamant crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_RUNITE, "Runite crossbow (u)", 69, 100, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_YEW, "Yew stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_RUNITE, "Runite limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_RUNITE, "Rune crossbow", 69, 50, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_RUNITE, "Runite crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_UNSTRUNG_DRAGON, "Dragon crossbow (u)", 78, 135, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_STOCK_MAGIC, "Magic stock", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_LIMBS_DRAGON, "Dragon limbs", 1)),
		recipe(ItemID.XBOWS_CROSSBOW_DRAGON, "Dragon crossbow", 78, 70, 2, 1,
			ingredient(ItemID.XBOWS_CROSSBOW_UNSTRUNG_DRAGON, "Dragon crossbow (u)", 1),
			ingredient(ItemID.XBOWS_CROSSBOW_STRING, "Crossbow string", 1))
	));

	private static Recipe recipe(int itemId, String itemName, int levelRequired, double xpPerAction, int ticks,
		int outputQuantity, Ingredient... ingredients)
	{
		// Fletching has no quest gate and nothing here can fail, so no failure note.
		return new Recipe(itemId, itemName, Arrays.asList(ingredients), Skill.FLETCHING, levelRequired, xpPerAction,
			ticks, null, outputQuantity, null, 0);
	}

	private static Ingredient ingredient(int itemId, String itemName, int quantity)
	{
		return new Ingredient(itemId, itemName, quantity);
	}

	private FletchingRecipes()
	{
	}
}
