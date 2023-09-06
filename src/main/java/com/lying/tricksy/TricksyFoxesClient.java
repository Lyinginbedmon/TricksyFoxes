package com.lying.tricksy;

import com.lying.tricksy.client.TFModelParts;
import com.lying.tricksy.client.renderer.entity.EntityTricksyFoxRenderer;
import com.lying.tricksy.client.renderer.layer.SageHatRenderer;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TricksyFoxesClient implements ClientModInitializer
{
	public void onInitializeClient()
	{
		ArmorRenderer.register(new SageHatRenderer(), TFItems.SAGE_HAT);
		
		EntityRendererRegistry.register(TFEntityTypes.TRICKSY_FOX, EntityTricksyFoxRenderer::new);
		
		TFModelParts.init();
	}
}
