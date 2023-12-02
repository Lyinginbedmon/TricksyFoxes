package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

public abstract class AbstractOffsetHeldItemLayer<T extends LivingEntity & ITricksyMob<?>, M extends EntityModel<T> & ModelWithArms> extends HeldItemFeatureRenderer<T, M>
{
	private final HeldItemRenderer heldItemRenderer;
	
	public AbstractOffsetHeldItemLayer(FeatureRendererContext<T, M> context, HeldItemRenderer heldItemRenderer)
	{
		super(context, heldItemRenderer);
		this.heldItemRenderer = heldItemRenderer;
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l)
	{
		if(!((ITricksyMob<?>)livingEntity).isTreeSleeping())
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
			translateToHand(matrices, isLeft);
			this.heldItemRenderer.renderItem(entity, stack, transformationMode, isLeft, matrices, vertexConsumers, light);
		matrices.pop();
	}
	
	public abstract void translateToHand(MatrixStack matrices, boolean isLeft);
}