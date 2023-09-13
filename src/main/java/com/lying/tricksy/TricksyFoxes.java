package com.lying.tricksy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.tricksy.data.TFRecipeProvider;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerBus;

import net.fabricmc.api.ModInitializer;

public class TricksyFoxes implements ModInitializer
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
	public void onInitialize()
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerBus.registerEventCallbacks();
		
		TFNodeTypes.init();
		TFBlocks.init();
		TFItems.init();
		TFEntityTypes.init();
		TFRecipeProvider.addBrewingRecipes();
	}
}