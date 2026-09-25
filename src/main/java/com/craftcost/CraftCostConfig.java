package com.craftcost;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CraftCostConfig.CONFIG_GROUP)
public interface CraftCostConfig extends Config
{
	String CONFIG_GROUP = "craftcost";

	@ConfigItem(
		keyName = "autoShowGeItem",
		name = "Auto-show GE item",
		description = "When you're looking at an item's price at the Grand Exchange and it's one of your known recipes, " +
			"automatically add it to the watchlist panel."
	)
	default boolean autoShowGeItem()
	{
		return false;
	}

	@ConfigItem(
		keyName = "countBank",
		name = "Count bank items",
		description = "Include items sitting in your bank (not just your inventory) as 'owned' when working out how many you'd still need to buy. " +
			"Note the client only knows your bank's contents once you've opened your bank at least once this session."
	)
	default boolean countBank()
	{
		return true;
	}

	@ConfigItem(
		keyName = "countInventory",
		name = "Count inventory items",
		description = "Include items in your inventory as 'owned' when working out how many you'd still need to buy."
	)
	default boolean countInventory()
	{
		return true;
	}
}
