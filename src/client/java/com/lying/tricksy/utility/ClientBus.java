package com.lying.tricksy.utility;

import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.init.TFKeybinds;
import com.lying.tricksy.renderer.OrderOverlay;
import com.lying.tricksy.renderer.layer.FoxPeriaptLayer;
import com.lying.tricksy.renderer.layer.GoatPeriaptLayer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.entity.model.GoatEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.GoatEntity;

public class ClientBus
{
	/** Fired by MouseMixinClient before normal scroll handling is applied */
	public static final Event<MouseScroll> MOUSE_SCROLL = EventFactory.createArrayBacked(MouseScroll.class, callbacks -> (client,vert,hori) -> 
	{
		for(MouseScroll event : callbacks)
			if(event.onMouseScroll(client, vert, hori))
				return true;
		return false;
	});
	
	@SuppressWarnings("unchecked")
	public static void registerEventCallbacks()
	{
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> 
		{
			if(entityType == EntityType.FOX)
				registrationHelper.register(new FoxPeriaptLayer((FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>>)entityRenderer));
			else if(entityType == EntityType.GOAT)
				registrationHelper.register(new GoatPeriaptLayer((FeatureRendererContext<GoatEntity, GoatEntityModel<GoatEntity>>)entityRenderer));
		});
		
		ClientBus.MOUSE_SCROLL.register((client, vert, hori) -> 
		{
			if(TricksyOrders.shouldRenderOrders())
			{
				TricksyOrders.incOrder((int)vert * (TricksyFoxesClient.config.scrollInverted() ? -1 : 1));
				return true;
			}
			return false;
		});
		
		HudRenderCallback.EVENT.register((context, partialTicks) -> OrderOverlay.drawHud(context, partialTicks));
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> 
		{
			while(TFKeybinds.keyIncOrder.wasPressed() && TricksyOrders.shouldRenderOrders())
			{
				TricksyOrders.incOrder(1);
			}
			
			while(TFKeybinds.keyDecOrder.wasPressed() && TricksyOrders.shouldRenderOrders())
			{
				TricksyOrders.incOrder(-1);
			}
		});
	}
}
