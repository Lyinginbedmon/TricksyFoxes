package com.lying.tricksy.renderer.layer;

import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.init.TFSpecialVisual;
import com.lying.tricksy.utility.ClientBus;
import com.lying.tricksy.utility.SpecialVisuals;
import com.lying.tricksy.utility.SpecialVisuals.ActiveVisual;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class SpecialVisualsLayer<T extends Entity, M extends EntityModel<T>> extends FeatureRenderer<T, M>
{
	private static final Map<TFSpecialVisual, SpecialVisualRender> VISUAL_RENDERERS = new HashMap<>();
	
	public SpecialVisualsLayer(FeatureRendererContext<T, M> context)
	{
		super(context);
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T living, float limbSwing, float limbSwingAmount, float tickDelta, float ageInTicks, float netHeadYaw, float headPitch)
	{
		SpecialVisuals visuals = ClientBus.getSpecialVisuals();
		if(!visuals.hasActiveVisuals(living.getUuid()))
			return;
		
		long currentTime = living.getWorld().getTime();
		for(ActiveVisual visual : visuals.getActiveVisuals(living.getUuid()))
			if(VISUAL_RENDERERS.containsKey(visual.visual()))
				VISUAL_RENDERERS.get(visual.visual()).render(matrixStack, vertexConsumerProvider, light, living, tickDelta, ageInTicks, visual.ticksActive(currentTime), visual.progress(currentTime));
	}
	
	@FunctionalInterface
	public interface SpecialVisualRender
	{
		public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, Entity entity, float tickDelta, float ageInTicks, int ticksActive, float progress);
	}
	
	private static void register(TFSpecialVisual visual, SpecialVisualRender renderer)
	{
		VISUAL_RENDERERS.put(visual, renderer);
	}
	
	static
	{
		register(TFSpecialVisual.WOLF_BLESS, (matrixStack, vertexConsumerProvider, light, entity, tickDelta, ageInTicks, ticksActive, progress) -> 
		{
			if(ticksActive%10 == 0)
				entity.getWorld().addParticle(TFParticles.ENERGY_EMITTER, entity.getX(), entity.getY() + 0.5D, entity.getZ(), 252, 248, 205);
		});
	}
}
