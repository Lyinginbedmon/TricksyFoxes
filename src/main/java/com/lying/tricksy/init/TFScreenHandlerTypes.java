package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.ScriptureScreenHandler;
import com.lying.tricksy.screen.TreeScreenHandler;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class TFScreenHandlerTypes
{
	private static final Map<Identifier, ScreenHandlerType<?>> HANDLERS = new HashMap<>();
	
	public static final ScreenHandlerType<TreeScreenHandler> TREE_SCREEN_HANDLER = register("tree_screen", new ScreenHandlerType<>((syncId, playerInventory) -> new TreeScreenHandler(syncId, null), FeatureFlags.VANILLA_FEATURES));
	public static final ScreenHandlerType<ScriptureScreenHandler> SCRIPTURE_SCREEN_HANDLER = register("scripture_screen", new ScreenHandlerType<>((syncId, playerInventory) -> new ScriptureScreenHandler(syncId, null), FeatureFlags.VANILLA_FEATURES));
	
	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String nameIn, ScreenHandlerType<T> typeIn)
	{
		HANDLERS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), typeIn);
		return typeIn;
	}
	
	public static void init()
	{
		HANDLERS.forEach((name,handler) -> Registry.register(Registries.SCREEN_HANDLER, name, handler));
	}
}