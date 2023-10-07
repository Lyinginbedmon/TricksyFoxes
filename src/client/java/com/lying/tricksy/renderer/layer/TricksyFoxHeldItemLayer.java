package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.model.entity.ModelTricksyFoxBase;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

public class TricksyFoxHeldItemLayer extends HeldItemFeatureRenderer<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>>
{
	private final HeldItemRenderer heldItemRenderer;
	
	public TricksyFoxHeldItemLayer(FeatureRendererContext<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>> context, HeldItemRenderer heldItemRenderer)
	{
		super(context, heldItemRenderer);
		this.heldItemRenderer = heldItemRenderer;
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, EntityTricksyFox livingEntity, float f, float g, float h, float j, float k, float l)
	{
		if(!livingEntity.isSleeping())
			super.render(matrixStack, vertexConsumerProvider, i, livingEntity, f, g, h, j, k, l);
	}
	
	protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		if(stack.isEmpty())
			return;
		
		matrices.push();
			((ModelWithArms)this.getContextModel()).setArmAngle(arm, matrices);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
			boolean isLeft = arm == Arm.LEFT;
			matrices.translate((float)(isLeft ? -1 : 1) / 16.0f, 0.025f, -0.325f);
			this.heldItemRenderer.renderItem(entity, stack, transformationMode, isLeft, matrices, vertexConsumers, light);
		matrices.pop();
	}
}
