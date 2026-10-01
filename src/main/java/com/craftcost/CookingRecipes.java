package com.craftcost;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;

/**
 * Every Cooking recipe the plugin knows how to make.
 * <p>
 * Items are referenced through RuneLite's own {@link ItemID} constants rather
 * than raw numbers, so a wrong item name fails the build instead of silently
 * pricing the wrong thing. Every id was resolved from RuneLite's ItemID table
 * and independently confirmed to be Grand Exchange tradeable; levels, XP,
 * ingredients and failure levels were read from the OSRS Wiki one item page at
 * a time.
 * <p>
 * Multi-step foods are listed by their base ingredients rather than their
 * intermediates. A garden pie really passes through three "part garden pie"
 * items, but none of them has a Grand Exchange price, so the shell, tomato,
 * onion and cabbage - what you actually buy - are listed instead. Where an
 * intermediate is itself tradeable and is what a player would really buy, such
 * as a pie shell, pizza base or bread dough, it is used directly.
 * <p>
 * XP is for the step these ingredients pay for, not the whole chain from
 * scratch. Adding meat to a plain pizza is 26 xp; the 143 for baking the base
 * belongs to the plain pizza's own recipe, and counting it twice would
 * overstate both.
 * <p>
 * Costs assume nothing burns. Where that assumption has a level attached, the
 * recipe carries a failure note saying so, which the details drawer highlights
 * until you are past it. There is no single level at which food stops burning -
 * it depends on the cooking source, cooking gauntlets and Hosidius favour - so
 * each note states the condition it applies to. Bass carries no note because
 * the wiki contradicts itself about it (80 on the item page, 79 on the burn
 * table), and a number we cannot settle is not worth showing.
 * <p>
 * Cooked meat is generated once, from raw beef. Five other raw meats cook into
 * the same item for the same xp, but the database looks recipes up by their
 * finished item, so a second entry for Cooked meat would never be reached.
 */
final class CookingRecipes
{
	static final List<Recipe> RECIPES = Collections.unmodifiableList(Arrays.asList(
		recipe(ItemID.SHRIMP, "Shrimps", 1, 30, 4, 1,
			"Burns below level 34 on a range", 34,
			ingredient(ItemID.RAW_SHRIMP, "Raw shrimps", 1)),
		recipe(ItemID.ANCHOVIES, "Anchovies", 1, 30, 4, 1,
			"Burns below level 34 on a range", 34,
			ingredient(ItemID.RAW_ANCHOVIES, "Raw anchovies", 1)),
		recipe(ItemID.SARDINE, "Sardine", 1, 40, 4, 1,
			"Burns below level 38 on a range", 38,
			ingredient(ItemID.RAW_SARDINE, "Raw sardine", 1)),
		recipe(ItemID.HERRING, "Herring", 5, 50, 4, 1,
			"Burns below level 41 on a range", 41,
			ingredient(ItemID.RAW_HERRING, "Raw herring", 1)),
		recipe(ItemID.MACKEREL, "Mackerel", 10, 60, 4, 1,
			"Burns below level 45 on a range", 45,
			ingredient(ItemID.RAW_MACKEREL, "Raw mackerel", 1)),
		recipe(ItemID.TROUT, "Trout", 15, 70, 4, 1,
			"Burns below level 49 on a range", 49,
			ingredient(ItemID.RAW_TROUT, "Raw trout", 1)),
		recipe(ItemID.COD, "Cod", 18, 75, 4, 1,
			"Burns below level 49 on a range", 49,
			ingredient(ItemID.RAW_COD, "Raw cod", 1)),
		recipe(ItemID.PIKE, "Pike", 20, 80, 4, 1,
			"Burns below level 54 on a range", 54,
			ingredient(ItemID.RAW_PIKE, "Raw pike", 1)),
		recipe(ItemID.SALMON, "Salmon", 25, 90, 4, 1,
			"Burns below level 58 on a range", 58,
			ingredient(ItemID.RAW_SALMON, "Raw salmon", 1)),
		recipe(ItemID.TUNA, "Tuna", 30, 100, 4, 1,
			"Burns below level 63 on a range", 63,
			ingredient(ItemID.RAW_TUNA, "Raw tuna", 1)),
		recipe(ItemID.TBWT_COOKED_KARAMBWAN, "Cooked karambwan", 30, 190, 4, 1,
			"Burns below level 99 on a range", 99,
			ingredient(ItemID.TBWT_RAW_KARAMBWAN, "Raw karambwan", 1)),
		recipe(ItemID.LOBSTER, "Lobster", 40, 120, 4, 1,
			"Burns until level 64 with cooking gauntlets", 64,
			ingredient(ItemID.RAW_LOBSTER, "Raw lobster", 1)),
		recipe(ItemID.BASS, "Bass", 43, 130, 4, 1,
			null, 0,
			ingredient(ItemID.RAW_BASS, "Raw bass", 1)),
		recipe(ItemID.SWORDFISH, "Swordfish", 45, 140, 4, 1,
			"Burns until level 80 with cooking gauntlets", 80,
			ingredient(ItemID.RAW_SWORDFISH, "Raw swordfish", 1)),
		recipe(ItemID.MONKFISH, "Monkfish", 62, 150, 4, 1,
			"Burns until level 86 with cooking gauntlets", 86,
			ingredient(ItemID.RAW_MONKFISH, "Raw monkfish", 1)),
		recipe(ItemID.SHARK, "Shark", 80, 210, 4, 1,
			"Burns until level 94 with cooking gauntlets", 94,
			ingredient(ItemID.RAW_SHARK, "Raw shark", 1)),
		recipe(ItemID.SEATURTLE, "Sea turtle", 82, 211.3, 4, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.RAW_SEATURTLE, "Raw sea turtle", 1)),
		recipe(ItemID.ANGLERFISH, "Anglerfish", 84, 230, 4, 1,
			"Burns until level 97 with cooking gauntlets", 97,
			ingredient(ItemID.RAW_ANGLERFISH, "Raw anglerfish", 1)),
		recipe(ItemID.DARK_CRAB, "Dark crab", 90, 215, 4, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.RAW_DARK_CRAB, "Raw dark crab", 1)),
		recipe(ItemID.MANTARAY, "Manta ray", 91, 216.3, 4, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.RAW_MANTARAY, "Raw manta ray", 1)),
		recipe(ItemID.COOKED_MEAT, "Cooked meat", 1, 30, 4, 1,
			"Burns below level 34 on a range", 34,
			ingredient(ItemID.RAW_BEEF, "Raw beef", 1)),
		recipe(ItemID.COOKED_CHICKEN, "Cooked chicken", 1, 30, 4, 1,
			"Burns below level 34 on a range", 34,
			ingredient(ItemID.RAW_CHICKEN, "Raw chicken", 1)),
		recipe(ItemID.REDBERRY_PIE, "Redberry pie", 10, 78, 6, 1,
			"Burns below level 45 on a range", 45,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.REDBERRIES, "Redberries", 1)),
		recipe(ItemID.MEAT_PIE, "Meat pie", 20, 110, 6, 1,
			"Burns below level 54 on a range", 54,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.COOKED_MEAT, "Cooked meat", 1)),
		recipe(ItemID.MUD_PIE, "Mud pie", 29, 128, 10, 1,
			"Burns below level 63", 63,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.BUCKET_COMPOST, "Compost", 1),
			ingredient(ItemID.BUCKET_WATER, "Bucket of water", 1),
			ingredient(ItemID.CLAY, "Clay", 1)),
		recipe(ItemID.APPLE_PIE, "Apple pie", 30, 130, 6, 1,
			"Burns below level 63", 63,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.COOKING_APPLE, "Cooking apple", 1)),
		recipe(ItemID.GARDEN_PIE, "Garden pie", 34, 138, 10, 1,
			"Burns below level 68", 68,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.TOMATO, "Tomato", 1),
			ingredient(ItemID.ONION, "Onion", 1),
			ingredient(ItemID.CABBAGE, "Cabbage", 1)),
		recipe(ItemID.FISH_PIE, "Fish pie", 47, 164, 10, 1,
			"Burns below level 74", 74,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.TROUT, "Trout", 1),
			ingredient(ItemID.COD, "Cod", 1),
			ingredient(ItemID.POTATO, "Potato", 1)),
		recipe(ItemID.BOTANICAL_PIE, "Botanical pie", 52, 180, 6, 1,
			"Burns below level 84", 84,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.GOLOVANOVA_TOP, "Golovanova fruit top", 1)),
		recipe(ItemID.MUSHROOM_PIE, "Mushroom pie", 60, 200, 6, 1,
			"Burns below level 89", 89,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.FOSSIL_SULLIUSCEP_CAP, "Sulliuscep cap", 1)),
		recipe(ItemID.ADMIRAL_PIE, "Admiral pie", 70, 210, 10, 1,
			"Burns below level 94", 94,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.SALMON, "Salmon", 1),
			ingredient(ItemID.TUNA, "Tuna", 1),
			ingredient(ItemID.POTATO, "Potato", 1)),
		recipe(ItemID.DRAGONFRUIT_PIE, "Dragonfruit pie", 73, 220, 6, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.DRAGONFRUIT, "Dragonfruit", 1)),
		recipe(ItemID.WILD_PIE, "Wild pie", 85, 240, 10, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.RAW_BEAR_MEAT, "Raw bear meat", 1),
			ingredient(ItemID.RAW_CHOMPY, "Raw chompy", 1),
			ingredient(ItemID.RAW_RABBIT, "Raw rabbit", 1)),
		recipe(ItemID.SUMMER_PIE, "Summer pie", 95, 260, 10, 1,
			"Burns at any level without a Cooking cape", 0,
			ingredient(ItemID.PIE_SHELL, "Pie shell", 1),
			ingredient(ItemID.STRAWBERRY, "Strawberry", 1),
			ingredient(ItemID.WATERMELON, "Watermelon", 1),
			ingredient(ItemID.COOKING_APPLE, "Cooking apple", 1)),
		recipe(ItemID.CAKE, "Cake", 40, 180, 10, 1,
			"Burns below level 74", 74,
			ingredient(ItemID.CAKE_TIN, "Cake tin", 1),
			ingredient(ItemID.EGG, "Egg", 1),
			ingredient(ItemID.POT_FLOUR, "Pot of flour", 1),
			ingredient(ItemID.BUCKET_MILK, "Bucket of milk", 1)),
		recipe(ItemID.CHOCOLATE_CAKE, "Chocolate cake", 50, 30, 2, 1,
			null, 0,
			ingredient(ItemID.CAKE, "Cake", 1),
			ingredient(ItemID.CHOCOLATE_BAR, "Chocolate bar", 1)),
		recipe(ItemID.BREAD, "Bread", 1, 40, 4, 1,
			"Burns below level 38 on a range", 38,
			ingredient(ItemID.BREAD_DOUGH, "Bread dough", 1)),
		recipe(ItemID.PLAIN_PIZZA, "Plain pizza", 35, 143, 8, 1,
			"Burns below level 68", 68,
			ingredient(ItemID.PIZZA_BASE, "Pizza base", 1),
			ingredient(ItemID.TOMATO, "Tomato", 1),
			ingredient(ItemID.CHEESE, "Cheese", 1)),
		recipe(ItemID.MEAT_PIZZA, "Meat pizza", 45, 26, 2, 1,
			null, 0,
			ingredient(ItemID.PLAIN_PIZZA, "Plain pizza", 1),
			ingredient(ItemID.COOKED_MEAT, "Cooked meat", 1)),
		recipe(ItemID.ANCHOVIE_PIZZA, "Anchovy pizza", 55, 39, 2, 1,
			null, 0,
			ingredient(ItemID.PLAIN_PIZZA, "Plain pizza", 1),
			ingredient(ItemID.ANCHOVIES, "Anchovies", 1)),
		recipe(ItemID.PINEAPPLE_PIZZA, "Pineapple pizza", 65, 45, 2, 1,
			null, 0,
			ingredient(ItemID.PLAIN_PIZZA, "Plain pizza", 1),
			ingredient(ItemID.PINEAPPLE_RING, "Pineapple ring", 1)),
		recipe(ItemID.STEW, "Stew", 25, 117, 8, 1,
			"Burns below level 58 on a range", 58,
			ingredient(ItemID.BOWL_WATER, "Bowl of water", 1),
			ingredient(ItemID.POTATO, "Potato", 1),
			ingredient(ItemID.COOKED_MEAT, "Cooked meat", 1)),
		recipe(ItemID.CURRY, "Curry", 60, 280, 10, 1,
			"Burns below level 74", 74,
			ingredient(ItemID.BOWL_WATER, "Bowl of water", 1),
			ingredient(ItemID.POTATO, "Potato", 1),
			ingredient(ItemID.COOKED_MEAT, "Cooked meat", 1),
			ingredient(ItemID.CURRY_LEAF, "Curry leaf", 3)),
		recipe(ItemID.JUG_WINE, "Jug of wine", 35, 200, 2, 1,
			"Can turn to bad wine below level 68", 68,
			ingredient(ItemID.GRAPES, "Grapes", 1),
			ingredient(ItemID.JUG_WATER, "Jug of water", 1))
	));

	private static Recipe recipe(int itemId, String itemName, int levelRequired, double xpPerAction, int ticks,
		int outputQuantity, String failureNote, int safeLevel, Ingredient... ingredients)
	{
		// Cooking has no quest gate.
		return new Recipe(itemId, itemName, Arrays.asList(ingredients), Skill.COOKING, levelRequired, xpPerAction,
			ticks, null, outputQuantity, failureNote, safeLevel);
	}

	private static Ingredient ingredient(int itemId, String itemName, int quantity)
	{
		return new Ingredient(itemId, itemName, quantity);
	}

	private CookingRecipes()
	{
	}
}
