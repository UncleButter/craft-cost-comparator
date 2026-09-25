package com.craftcost;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;

/**
 * Every Herblore potion the plugin knows how to make.
 * <p>
 * Most follow the standard pattern - a grimy herb into a vial of water, then a
 * secondary ingredient, producing a 4-dose potion. The rest are variations:
 * some brew in coconut milk or a vial of blood instead of water, and some
 * upgrade an already-finished potion.
 * <p>
 * Items are referenced through RuneLite's own {@link ItemID} constants rather
 * than raw numbers, so a wrong item name fails the build instead of silently
 * pricing the wrong thing. Every id was resolved from RuneLite's ItemID table
 * and independently confirmed to be Grand Exchange tradeable; levels, XP and
 * ingredient quantities were cross-checked against the OSRS Wiki.
 * <p>
 * Some potions are deliberately absent because at least one ingredient has no
 * Grand Exchange price, which would make the cost meaningless: Relicym's balm
 * (rogue's purse, snake weed), Sanfew serum (snake weed), and all eight divine
 * potions (crystal dust, which is untradeable in every form).
 * <p>
 * The XP figure covers the steps this recipe's ingredients pay for - it does
 * not include experience for cleaning the herb or making the unfinished potion.
 * Potions upgraded dose-by-dose (stamina, extended antifire, anti-venom,
 * extended super antifire) consume one secondary per dose and take four
 * actions to fill a 4-dose potion, which is reflected in both their quantities
 * and their time estimate.
 */
final class HerbloreRecipes
{
	/** Herblore can't be trained at all until this quest is done. */
	private static final Quest UNLOCK_QUEST = Quest.DRUIDIC_RITUAL;

	static final List<Recipe> RECIPES = Collections.unmodifiableList(Arrays.asList(
		recipe(ItemID._4DOSE1ATTACK, "Attack potion(4)", 3, 25, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_GUAM, "Grimy guam leaf", 1),
			ingredient(ItemID.EYE_OF_NEWT, "Eye of newt", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEANTIPOISON, "Antipoison(4)", 5, 37.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_MARENTILL, "Grimy marrentill", 1),
			ingredient(ItemID.UNICORN_HORN_DUST, "Unicorn horn dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID.STRENGTH4, "Strength potion(4)", 12, 50, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TARROMIN, "Grimy tarromin", 1),
			ingredient(ItemID.LIMPWURT_ROOT, "Limpwurt root", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID.MORT_SERUM4, "Serum 207 (4)", 15, 50, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TARROMIN, "Grimy tarromin", 1),
			ingredient(ItemID.ASHES, "Ashes", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID.SUPERCOMPOST_POTION_4, "Compost potion(4)", 22, 60, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_HARRALANDER, "Grimy harralander", 1),
			ingredient(ItemID.FOSSIL_VOLCANIC_ASH, "Volcanic ash", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSESTATRESTORE, "Restore potion(4)", 22, 62.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_HARRALANDER, "Grimy harralander", 1),
			ingredient(ItemID.RED_SPIDERS_EGGS, "Red spiders' eggs", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE1ENERGY, "Energy potion(4)", 26, 67.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_HARRALANDER, "Grimy harralander", 1),
			ingredient(ItemID.CHOCOLATE_DUST, "Chocolate dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE1DEFENSE, "Defence potion(4)", 30, 75, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_RANARR, "Grimy ranarr weed", 1),
			ingredient(ItemID.WHITE_BERRIES, "White berries", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE1AGILITY, "Agility potion(4)", 34, 80, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TOADFLAX, "Grimy toadflax", 1),
			ingredient(ItemID.TOADS_LEGS, "Toad's legs", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSECOMBAT, "Combat potion(4)", 36, 84, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_HARRALANDER, "Grimy harralander", 1),
			ingredient(ItemID.GROUND_DESERT_GOAT_HORN, "Goat horn dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEPRAYERRESTORE, "Prayer potion(4)", 38, 87.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_RANARR, "Grimy ranarr weed", 1),
			ingredient(ItemID.SNAPE_GRASS, "Snape grass", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2ATTACK, "Super attack(4)", 45, 100, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_IRIT, "Grimy irit leaf", 1),
			ingredient(ItemID.EYE_OF_NEWT, "Eye of newt", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2ANTIPOISON, "Superantipoison(4)", 48, 106.3, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_IRIT, "Grimy irit leaf", 1),
			ingredient(ItemID.UNICORN_HORN_DUST, "Unicorn horn dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEFISHERSPOTION, "Fishing potion(4)", 50, 112.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_AVANTOE, "Grimy avantoe", 1),
			ingredient(ItemID.SNAPE_GRASS, "Snape grass", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2ENERGY, "Super energy(4)", 52, 117.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_AVANTOE, "Grimy avantoe", 1),
			ingredient(ItemID.MORTMYREMUSHROOM, "Mort myre fungus", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEHUNTING, "Hunter potion(4)", 53, 120, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_AVANTOE, "Grimy avantoe", 1),
			ingredient(ItemID.HUNTINGBEAST_SABRETEETH_DUST, "Kebbit teeth dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2STRENGTH, "Super strength(4)", 55, 125, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_KWUARM, "Grimy kwuarm", 1),
			ingredient(ItemID.LIMPWURT_ROOT, "Limpwurt root", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID.WEAPON_POISON, "Weapon poison", 60, 137.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_KWUARM, "Grimy kwuarm", 1),
			ingredient(ItemID.DRAGON_SCALE_DUST, "Dragon scale dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2RESTORE, "Super restore(4)", 63, 142.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_SNAPDRAGON, "Grimy snapdragon", 1),
			ingredient(ItemID.RED_SPIDERS_EGGS, "Red spiders' eggs", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE2DEFENSE, "Super defence(4)", 66, 150, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_CADANTINE, "Grimy cadantine", 1),
			ingredient(ItemID.WHITE_BERRIES, "White berries", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE1ANTIDRAGON, "Antifire potion(4)", 69, 157.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_LANTADYME, "Grimy lantadyme", 1),
			ingredient(ItemID.DRAGON_SCALE_DUST, "Dragon scale dust", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSERANGERSPOTION, "Ranging potion(4)", 72, 162.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_DWARF_WEED, "Grimy dwarf weed", 1),
			ingredient(ItemID.WINE_OF_ZAMORAK, "Wine of Zamorak", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSE1MAGIC, "Magic potion(4)", 76, 172.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_LANTADYME, "Grimy lantadyme", 1),
			ingredient(ItemID.CACTUS_POTATO, "Potato cactus", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEPOTIONOFZAMORAK, "Zamorak brew(4)", 78, 175, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TORSTOL, "Grimy torstol", 1),
			ingredient(ItemID.JANGERBERRIES, "Jangerberries", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID._4DOSEPOTIONOFSARADOMIN, "Saradomin brew(4)", 81, 180, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TOADFLAX, "Grimy toadflax", 1),
			ingredient(ItemID.CRUSHED_BIRD_NEST, "Crushed nest", 1),
			ingredient(ItemID.VIAL_WATER, "Vial of water", 1)),

		recipe(ItemID.ANTIDOTE_4, "Antidote+(4)", 68, 155, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_TOADFLAX, "Grimy toadflax", 1),
			ingredient(ItemID.VIAL_COCONUT_MILK, "Coconut milk", 1),
			ingredient(ItemID.YEW_ROOTS, "Yew roots", 1)),

		recipe(ItemID.ANTIDOTE__4, "Antidote++(4)", 79, 177.5, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_IRIT, "Grimy irit leaf", 1),
			ingredient(ItemID.VIAL_COCONUT_MILK, "Coconut milk", 1),
			ingredient(ItemID.MAGIC_ROOTS, "Magic roots", 1)),

		recipe(ItemID._4DOSEBASTION, "Bastion potion(4)", 80, 155, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_CADANTINE, "Grimy cadantine", 1),
			ingredient(ItemID.VIAL_BLOOD, "Vial of blood", 1),
			ingredient(ItemID.WINE_OF_ZAMORAK, "Wine of Zamorak", 1)),

		recipe(ItemID._4DOSEBATTLEMAGE, "Battlemage potion(4)", 80, 155, 2, 1,
			ingredient(ItemID.UNIDENTIFIED_CADANTINE, "Grimy cadantine", 1),
			ingredient(ItemID.VIAL_BLOOD, "Vial of blood", 1),
			ingredient(ItemID.CACTUS_POTATO, "Potato cactus", 1)),

		recipe(ItemID._4DOSE2COMBAT, "Super combat potion(4)", 90, 150, 2, 1,
			ingredient(ItemID._4DOSE2ATTACK, "Super attack(4)", 1),
			ingredient(ItemID._4DOSE2STRENGTH, "Super strength(4)", 1),
			ingredient(ItemID._4DOSE2DEFENSE, "Super defence(4)", 1),
			ingredient(ItemID.TORSTOL, "Torstol", 1)),

		recipe(ItemID._4DOSE3ANTIDRAGON, "Super antifire potion(4)", 92, 130, 2, 1,
			ingredient(ItemID._4DOSE1ANTIDRAGON, "Antifire potion(4)", 1),
			ingredient(ItemID.CRUSHED_DRAGON_BONES, "Crushed superior dragon bones", 1)),

		recipe(ItemID.ANTIVENOM_4, "Anti-venom+(4)", 94, 125, 2, 1,
			ingredient(ItemID.ANTIVENOM4, "Anti-venom(4)", 1),
			ingredient(ItemID.TORSTOL, "Torstol", 1)),

		recipe(ItemID._4DOSESTAMINA, "Stamina potion(4)", 77, 102, 8, 1,
			ingredient(ItemID._4DOSE2ENERGY, "Super energy(4)", 1),
			ingredient(ItemID.AMYLASE, "Amylase crystal", 4)),

		recipe(ItemID._4DOSE2ANTIDRAGON, "Extended antifire(4)", 84, 110, 8, 1,
			ingredient(ItemID._4DOSE1ANTIDRAGON, "Antifire potion(4)", 1),
			ingredient(ItemID.LAVA_SHARD, "Lava scale shard", 4)),

		recipe(ItemID.ANTIVENOM4, "Anti-venom(4)", 87, 120, 8, 1,
			ingredient(ItemID.ANTIDOTE__4, "Antidote++(4)", 1),
			ingredient(ItemID.SNAKEBOSS_SCALE, "Zulrah's scales", 20)),

		recipe(ItemID._4DOSE4ANTIDRAGON, "Extended super antifire(4)", 98, 160, 8, 1,
			ingredient(ItemID._4DOSE3ANTIDRAGON, "Super antifire potion(4)", 1),
			ingredient(ItemID.LAVA_SHARD, "Lava scale shard", 4)),

		recipe(ItemID.SALAMANDER_TAR_GREEN, "Guam tar", 19, 30, 2, 15,
			ingredient(ItemID.SWAMP_TAR, "Swamp tar", 15),
			ingredient(ItemID.GUAM_LEAF, "Guam leaf", 1)),

		recipe(ItemID.SALAMANDER_TAR_ORANGE, "Marrentill tar", 31, 42.5, 2, 15,
			ingredient(ItemID.SWAMP_TAR, "Swamp tar", 15),
			ingredient(ItemID.MARENTILL, "Marrentill", 1)),

		recipe(ItemID.SALAMANDER_TAR_RED, "Tarromin tar", 39, 55, 2, 15,
			ingredient(ItemID.SWAMP_TAR, "Swamp tar", 15),
			ingredient(ItemID.TARROMIN, "Tarromin", 1)),

		recipe(ItemID.SALAMANDER_TAR_BLACK, "Harralander tar", 44, 72.5, 2, 15,
			ingredient(ItemID.SWAMP_TAR, "Swamp tar", 15),
			ingredient(ItemID.HARRALANDER, "Harralander", 1)),

		recipe(ItemID.BRUTAL_2DOSE1ATTACK, "Attack mix(2)", 4, 8, 2, 1,
			ingredient(ItemID._2DOSE1ATTACK, "Attack potion(2)", 1),
			ingredient(ItemID.BRUT_ROE, "Roe", 1)),

		recipe(ItemID.BRUTAL_2DOSEANTIPOISON, "Antipoison mix(2)", 6, 12, 2, 1,
			ingredient(ItemID._2DOSEANTIPOISON, "Antipoison(2)", 1),
			ingredient(ItemID.BRUT_ROE, "Roe", 1)),

		recipe(ItemID.BRUTAL_RELICYMS_BALM2, "Relicym's mix(2)", 9, 14, 2, 1,
			ingredient(ItemID.RELICYMS_BALM2, "Relicym's balm(2)", 1),
			ingredient(ItemID.BRUT_ROE, "Roe", 1)),

		recipe(ItemID.BRUTAL_2DOSE1STRENGTH, "Strength mix(2)", 14, 17, 2, 1,
			ingredient(ItemID._2DOSE1STRENGTH, "Strength potion(2)", 1),
			ingredient(ItemID.BRUT_ROE, "Roe", 1)),

		recipe(ItemID.BRUTAL_2DOSESTATRESTORE, "Restore mix(2)", 24, 21, 2, 1,
			ingredient(ItemID._2DOSESTATRESTORE, "Restore potion(2)", 1),
			ingredient(ItemID.BRUT_ROE, "Roe", 1)),

		recipe(ItemID.BRUTAL_2DOSE1ENERGY, "Energy mix(2)", 29, 23, 2, 1,
			ingredient(ItemID._2DOSE1ENERGY, "Energy potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE1DEFENSE, "Defence mix(2)", 33, 25, 2, 1,
			ingredient(ItemID._2DOSE1DEFENSE, "Defence potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE1AGILITY, "Agility mix(2)", 37, 27, 2, 1,
			ingredient(ItemID._2DOSE1AGILITY, "Agility potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSECOMBAT, "Combat mix(2)", 40, 28, 2, 1,
			ingredient(ItemID._2DOSECOMBAT, "Combat potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSEPRAYERRESTORE, "Prayer mix(2)", 42, 29, 2, 1,
			ingredient(ItemID._2DOSEPRAYERRESTORE, "Prayer potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2ATTACK, "Superattack mix(2)", 47, 33, 2, 1,
			ingredient(ItemID._2DOSE2ATTACK, "Super attack(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2ANTIPOISON, "Anti-poison supermix(2)", 51, 35, 2, 1,
			ingredient(ItemID._2DOSE2ANTIPOISON, "Superantipoison(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSEFISHERSPOTION, "Fishing mix(2)", 53, 38, 2, 1,
			ingredient(ItemID._2DOSEFISHERSPOTION, "Fishing potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2ENERGY, "Super energy mix(2)", 56, 39, 2, 1,
			ingredient(ItemID._2DOSE2ENERGY, "Super energy(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE1HUNTING, "Hunting mix(2)", 58, 40, 2, 1,
			ingredient(ItemID._2DOSEHUNTING, "Hunter potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2STRENGTH, "Super str. mix(2)", 59, 42, 2, 1,
			ingredient(ItemID._2DOSE2STRENGTH, "Super strength(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2RESTORE, "Super restore mix(2)", 67, 48, 2, 1,
			ingredient(ItemID._2DOSE2RESTORE, "Super restore(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2DEFENSE, "Super def. mix(2)", 71, 50, 2, 1,
			ingredient(ItemID._2DOSE2DEFENSE, "Super defence(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_ANTIDOTE_2, "Antidote+ mix(2)", 74, 52, 2, 1,
			ingredient(ItemID.ANTIDOTE_2, "Antidote+(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE1ANTIDRAGON, "Antifire mix(2)", 75, 53, 2, 1,
			ingredient(ItemID._2DOSE1ANTIDRAGON, "Antifire potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSERANGERSPOTION, "Ranging mix(2)", 80, 54, 2, 1,
			ingredient(ItemID._2DOSERANGERSPOTION, "Ranging potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE1MAGIC, "Magic mix(2)", 83, 57, 2, 1,
			ingredient(ItemID._2DOSE1MAGIC, "Magic potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSEPOTIONOFZAMORAK, "Zamorak mix(2)", 85, 58, 2, 1,
			ingredient(ItemID._2DOSEPOTIONOFZAMORAK, "Zamorak brew(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSESTAMINA, "Stamina mix(2)", 86, 60, 2, 1,
			ingredient(ItemID._2DOSESTAMINA, "Stamina potion(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1)),

		recipe(ItemID.BRUTAL_2DOSE2ANTIDRAGON, "Extended antifire mix(2)", 91, 61, 2, 1,
			ingredient(ItemID._2DOSE2ANTIDRAGON, "Extended antifire(2)", 1),
			ingredient(ItemID.BRUT_CAVIAR, "Caviar", 1))
	));

	private static Recipe recipe(int itemId, String itemName, int levelRequired, double xpPerAction, int ticks,
		int outputQuantity, Ingredient... ingredients)
	{
		return new Recipe(itemId, itemName, Arrays.asList(ingredients), Skill.HERBLORE, levelRequired, xpPerAction,
			ticks, UNLOCK_QUEST, outputQuantity);
	}

	private static Ingredient ingredient(int itemId, String itemName, int quantity)
	{
		return new Ingredient(itemId, itemName, quantity);
	}

	private HerbloreRecipes()
	{
	}
}
