package com.lying.tricksy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lying.tricksy.config.ServerConfig;
import com.lying.tricksy.data.TFRecipeProvider;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFCommands;
import com.lying.tricksy.init.TFDamageTypes;
import com.lying.tricksy.init.TFEnlightenmentPaths;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.init.TFRegistries;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.network.AddGlobalRefReceiver;
import com.lying.tricksy.network.AddLocalReferenceReceiver;
import com.lying.tricksy.network.DeleteReferenceReceiver;
import com.lying.tricksy.network.GiveOrderReceiver;
import com.lying.tricksy.network.OpenTreeScreenReceiver;
import com.lying.tricksy.network.RemoveUserReceiver;
import com.lying.tricksy.network.SaveTreeReceiver;
import com.lying.tricksy.network.TFPacketHandler;
import com.lying.tricksy.network.ToggleScriptureOverruleReceiver;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerBus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class TricksyFoxes implements ModInitializer
{
	public static ServerConfig config;
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.ModInfo.MOD_ID);
    
    // FIXME Final lang check before release
    
	public void onInitialize()
	{
		config = new ServerConfig("config/TricksyFoxesServer.cfg");
		config.read();
		
		ServerBus.registerEventCallbacks();
		TFRegistries.init();
		TFCommands.init();
		TFDamageTypes.init();
		TFEnlightenmentPaths.init();
		TFBlocks.init();
		TFBlockEntities.init();
		TFItems.init();
		TFEntityTypes.init();
		TFSpecialRecipes.init();
		TFRecipeProvider.addBrewingRecipes();
		TFScreenHandlerTypes.init();
		TFParticles.init();
		TFSoundEvents.init();
		
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.ADD_GLOBAL_REF_ID, new AddGlobalRefReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.ADD_LOCAL_REF_ID, new AddLocalReferenceReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.REMOVE_USER_ID, new RemoveUserReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.DELETE_REF_ID, new DeleteReferenceReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.OPEN_TREE_ID, new OpenTreeScreenReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.SAVE_TREE_ID, new SaveTreeReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.TOGGLE_SCRIPTURE_ID, new ToggleScriptureOverruleReceiver());
		ServerPlayNetworking.registerGlobalReceiver(TFPacketHandler.GIVE_ORDER_ID, new GiveOrderReceiver());
	}
}