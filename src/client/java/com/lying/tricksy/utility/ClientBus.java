package com.lying.tricksy.utility;

import com.lying.tricksy.renderer.layer.FoxPeriaptLayer;
import com.lying.tricksy.renderer.layer.GoatPeriaptLayer;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.render.entity.model.GoatEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.GoatEntity;

public class ClientBus
{
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
	}
}
