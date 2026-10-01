package com.craftcost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every recipe the plugin knows about, gathered from one list per skill.
 * <p>
 * Each skill lives in its own file (e.g. {@link HerbloreRecipes}) so no single
 * file grows unmanageable as coverage expands, and so a skill can be worked on
 * without touching the others. To add a skill, create its
 * {@code XxxRecipes.RECIPES} list and add one line to {@link #RECIPES} below -
 * nothing else in the plugin needs to change, since the panel's search box and
 * the Grand Exchange auto-detect both read straight from this list.
 * <p>
 * Recipes reference items through RuneLite's {@code ItemID} constants rather
 * than raw numbers, so a mistyped item is a compile error instead of a silently
 * wrong price. Only items that are Grand Exchange tradeable belong here: an
 * untradeable ingredient has no price for the plugin to total up.
 */
final class RecipeDatabase
{
	static final List<Recipe> RECIPES;

	static
	{
		List<Recipe> all = new ArrayList<>();
		all.addAll(HerbloreRecipes.RECIPES);
		all.addAll(CookingRecipes.RECIPES);
		RECIPES = Collections.unmodifiableList(all);
	}

	/**
	 * @return the recipe whose finished item matches this id, or null if
	 * it's not one we know how to make.
	 */
	static Recipe findByItemId(int itemId)
	{
		for (Recipe recipe : RECIPES)
		{
			if (recipe.getItemId() == itemId)
			{
				return recipe;
			}
		}
		return null;
	}

	private RecipeDatabase()
	{
	}
}
