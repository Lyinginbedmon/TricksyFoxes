package com.lying.tricksy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.tricksy.config.ServerConfig;
import com.lying.tricksy.data.TFRecipeProvider;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.network.DeleteReferenceReceiver;
import com.lying.tricksy.network.SaveTreeReceiver;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerBus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class TricksyFoxes implements ModInitializer
{
	public static ServerConfig config;
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
	public void onInitialize()
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		config = new ServerConfig("config/TricksyFoxesServer.cfg");
		config.read();
		
		ServerBus.registerEventCallbacks();
		
		TFObjType.init();
		TFNodeTypes.init();
		TFBlocks.init();
		TFItems.init();
		TFEntityTypes.init();
		TFSpecialRecipes.init();
		TFRecipeProvider.addBrewingRecipes();
		
		ServerPlayNetworking.registerGlobalReceiver(SaveTreeReceiver.PACKET_ID, new SaveTreeReceiver());
		ServerPlayNetworking.registerGlobalReceiver(DeleteReferenceReceiver.PACKET_ID, new DeleteReferenceReceiver());
	}
}