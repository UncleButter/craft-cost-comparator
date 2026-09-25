package com.craftcost;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Launches a real RuneLite client with this plugin already loaded, for local
 * testing. Run via the Gradle "run" task (or straight from your IDE).
 */
public class CraftCostPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CraftCostPlugin.class);
		RuneLite.main(args);
	}
}
