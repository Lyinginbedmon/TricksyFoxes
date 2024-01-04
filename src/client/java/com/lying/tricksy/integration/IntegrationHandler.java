package com.lying.tricksy.integration;

import net.fabricmc.loader.api.FabricLoader;

public class IntegrationHandler
{
	public static final boolean REI = isLoaded("roughlyenoughitems");
	
	private static boolean isLoaded(String modID) { return FabricLoader.getInstance().isModLoaded(modID); }
}
