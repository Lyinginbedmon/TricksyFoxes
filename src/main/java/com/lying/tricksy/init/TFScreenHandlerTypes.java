package com.lying.tricksy.init;

import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.TreeScreenHandler;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class TFScreenHandlerTypes
{
	public static final ScreenHandlerType<TreeScreenHandler> TREE_SCREEN_HANDLER = new ScreenHandlerType<>((syncId, playerInventory) -> new TreeScreenHandler(syncId, null), FeatureFlags.VANILLA_FEATURES);
	
	public static void init()
	{
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(Reference.ModInfo.MOD_ID, "tree_screen"), TREE_SCREEN_HANDLER);
	}
}