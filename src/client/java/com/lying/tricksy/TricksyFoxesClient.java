package com.lying.tricksy;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.renderer.entity.EntityTricksyFoxRenderer;
import com.lying.tricksy.renderer.layer.SageHatRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.item.DyeableItem;

@Environment(EnvType.CLIENT)
public class TricksyFoxesClient implements ClientModInitializer
{
	public void onInitializeClient()
	{
		ArmorRenderer.register(new SageHatRenderer(), TFItems.SAGE_HAT);
		
		EntityRendererRegistry.register(TFEntityTypes.TRICKSY_FOX, EntityTricksyFoxRenderer::new);
		
		ColorProviderRegistry.ITEM.register((stack, index) -> { return index == 1 ? ((DyeableItem)stack.getItem()).getColor(stack) : 0; }, TFItems.SAGE_HAT);
		
		TFModelParts.init();
	}
}
