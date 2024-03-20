package com.lying.tricksy.renderer.layer;

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
	public SpecialVisualsLayer(FeatureRendererContext<T, M> context)
	{
		super(context);
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T living, float limbSwing, float limbSwingAmount, float tickDelta, float ageInTicks, float netHeadYaw, float headPitch)
	{
		SpecialVisuals visuals = ClientBus.getSpecialVisuals();
		if(!visuals.hasActiveVisuals(living.getUuid()))
			return;
		
		SpecialVisualsRegistry registry = SpecialVisualsRegistry.instance();
		long currentTime = living.getWorld().getTime();
		matrixStack.push();
			matrixStack.translate(0F, 1.5F, 0F);
			for(ActiveVisual visual : visuals.getActiveVisuals(living.getUuid()))
				if(registry.hasRender(visual.visual()))
					registry.getRender(visual.visual()).render(matrixStack, vertexConsumerProvider, light, living, tickDelta, ageInTicks, visual.ticksActive(currentTime), visual.progress(currentTime));
		matrixStack.pop();
	}
	
	@FunctionalInterface
	public interface SpecialVisualRender
	{
		public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, Entity entity, float tickDelta, float ageInTicks, int ticksActive, float progress);
	}
}
