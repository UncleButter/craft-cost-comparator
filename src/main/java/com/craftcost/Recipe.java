package com.craftcost;

import java.util.List;
import lombok.Value;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Describes how to make one tradeable item (the "output") from a fixed list
 * of other tradeable items (the "ingredients"), plus what it costs to actually
 * make it in-game: the skill and level it trains, the XP per action, roughly
 * how long one action takes, and any quest gating that skill entirely.
 * <p>
 * Only items that RuneLite can quote a Grand Exchange price for should be used
 * as ingredients - if an ingredient itself needs to be crafted from something
 * else (e.g. an unfinished potion), either add its own {@link Recipe} to
 * {@link RecipeDatabase} and let the panel flatten the two, or just list its
 * own base materials directly here instead.
 */
@Value
public class Recipe
{
	int itemId;
	String itemName;
	List<Ingredient> ingredients;
	Skill skill;
	int levelRequired;
	double xpPerAction;
	int ticksPerAction;
	Quest requiredQuest;

	/**
	 * How many of the finished item a single action produces. Usually 1, but
	 * some recipes yield a batch - making herb tar, for instance, turns 15
	 * swamp tar and one herb into 15 tars in one go. Ingredient costs, XP and
	 * time are all charged per action, not per finished item.
	 */
	int outputQuantity;

	@Override
	public String toString()
	{
		// used by the JComboBox renderer
		return itemName;
	}
}
