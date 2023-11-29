package com.lying.tricksy;

import com.lying.tricksy.config.ClientConfig;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.network.RefAddedReceiver;
import com.lying.tricksy.network.SyncInventoryScreenReceiver;
import com.lying.tricksy.network.SyncPrescientCandleScreenReceiver;
import com.lying.tricksy.network.SyncScriptureScreenReceiver;
import com.lying.tricksy.network.SyncTreeScreenReceiver;
import com.lying.tricksy.network.SyncWorkTableScreenReceiver;
import com.lying.tricksy.network.TFPacketHandler;
import com.lying.tricksy.particle.PaperParticle;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.renderer.block.ClockworkFriarBlockEntityRenderer;
import com.lying.tricksy.renderer.entity.EntityTricksyFoxRenderer;
import com.lying.tricksy.renderer.entity.EntityTricksyGoatRenderer;
import com.lying.tricksy.renderer.layer.SageHatRenderer;
import com.lying.tricksy.screen.ClockworkFriarScreen;
import com.lying.tricksy.screen.PrescientCandleScreen;
import com.lying.tricksy.screen.ScriptureScreen;
import com.lying.tricksy.screen.TreeScreen;
import com.lying.tricksy.screen.TricksyInventoryScreen;
import com.lying.tricksy.screen.WorkTableScreen;
import com.lying.tricksy.utility.ClientBus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TricksyFoxesClient implements ClientModInitializer
{
	public static MinecraftClient mc;
	public static ClientConfig config;
	
	public void onInitializeClient()
	{
		mc = MinecraftClient.getInstance();
		config = new ClientConfig(mc.runDirectory.getAbsolutePath() + "/config/TricksyFoxesClient.cfg");
		config.read();
		
		BlockRenderLayerMap.INSTANCE.putBlock(TFBlocks.PRESCIENCE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(TFBlocks.CLOCKWORK_FRIAR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(TFBlocks.PRESCIENT_CANDLE, RenderLayer.getCutout());
		
		ClientBus.registerEventCallbacks();
		
		registerRenderers();
		registerPacketReceivers();
		registerScreens();
	}
	
	private static void registerRenderers()
	{
		TFModelParts.init();
		
		ArmorRenderer.register(new SageHatRenderer(), TFItems.SAGE_HAT);
		EntityRendererRegistry.register(TFEntityTypes.TRICKSY_FOX, EntityTricksyFoxRenderer::new);
		EntityRendererRegistry.register(TFEntityTypes.TRICKSY_GOAT, EntityTricksyGoatRenderer::new);
		BlockEntityRendererFactories.register(TFBlockEntities.CLOCKWORK_FRIAR, ClockworkFriarBlockEntityRenderer::new);
		
		ColorProviderRegistry.ITEM.register((stack, index) -> { return index == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, TFItems.SAGE_HAT);
		
		ParticleFactoryRegistry.getInstance().register(TFParticles.PAPER, PaperParticle.Factory::new);
		
		Identifier note_sealed = new Identifier(Reference.ModInfo.MOD_ID, "sealed");
		for(Item scroll : TFItems.SEALABLES)
			ModelPredicateProviderRegistry.register(scroll, note_sealed, (itemStack, clientWorld, livingEntity, seed) -> { return ISealableItem.isSealed(itemStack) ? 1F : 0F; });
	}
	
	private static void registerScreens()
	{
		TFScreenHandlerTypes.init();
		
		HandledScreens.register(TFScreenHandlerTypes.SCRIPTURE_SCREEN_HANDLER, ScriptureScreen::new);
		HandledScreens.register(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, TreeScreen::new);
		HandledScreens.register(TFScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, TricksyInventoryScreen::new);
		HandledScreens.register(TFScreenHandlerTypes.WORK_TABLE_SCREEN_HANDLER, WorkTableScreen::new);
		HandledScreens.register(TFScreenHandlerTypes.CLOCKWORK_FRIAR_SCREEN_HANDLER, ClockworkFriarScreen::new);
		HandledScreens.register(TFScreenHandlerTypes.PRESCIENT_CANDLE_SCREEN_HANDLER, PrescientCandleScreen::new);
	}
	
	private static void registerPacketReceivers()
	{
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.SYNC_SCRIPTURE_ID, new SyncScriptureScreenReceiver());
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.SYNC_TREE_ID, new SyncTreeScreenReceiver());
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.SYNC_INVENTORY_ID, new SyncInventoryScreenReceiver());
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.REF_ADDED_ID, new RefAddedReceiver());
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.SYNC_WORK_TABLE_ID, new SyncWorkTableScreenReceiver());
		ClientPlayNetworking.registerGlobalReceiver(TFPacketHandler.SYNC_PRESCIENT_CANDLE_ID, new SyncPrescientCandleScreenReceiver());
	}
}
