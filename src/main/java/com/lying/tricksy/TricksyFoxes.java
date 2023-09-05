package com.lying.tricksy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.ModInitializer;

public class TricksyFoxes implements ModInitializer
{
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
	public void onInitialize()
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		LOGGER.info("Hello Fabric world!");
		
		TFItems.init();
		TFEntityTypes.init();
	}
}