package com.craftcost;

import lombok.Value;

/**
 * A single input required by a {@link Recipe}, e.g. "1x Grimy ranarr weed".
 */
@Value
public class Ingredient
{
	int itemId;
	String itemName;
	int quantity;
}
