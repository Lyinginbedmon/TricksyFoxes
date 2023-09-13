package com.lying.tricksy.utility;

import com.lying.tricksy.renderer.layer.FoxPeriaptLayer;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;

public class ClientBus
{
	@SuppressWarnings("unchecked")
	public static void registerEventCallbacks()
	{
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> 
		{
			if(entityType == EntityType.FOX)
				registrationHelper.register(new FoxPeriaptLayer((FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>>)entityRenderer));
		});
	}
}
