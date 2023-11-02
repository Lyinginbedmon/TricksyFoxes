package com.lying.tricksy.renderer.block;

import com.lying.tricksy.block.entity.ClockworkFriarBlockEntity;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.block.ModelClockworkFriar;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ClockworkFriarBlockEntityRenderer implements BlockEntityRenderer<ClockworkFriarBlockEntity>
{
	private static final Identifier BASE_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/block/clockwork_friar.png");
	private final ItemRenderer itemRenderer;
	private final ModelClockworkFriar friarModel;
	
	public ClockworkFriarBlockEntityRenderer(BlockEntityRendererFactory.Context ctx)
	{
		this.friarModel = new ModelClockworkFriar(ctx.getLayerModelPart(TFModelParts.CLOCKWORK_FRIAR));
		this.itemRenderer = ctx.getItemRenderer();
	}
	
	@Override
	public void render(ClockworkFriarBlockEntity friar, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j)
	{
		int lightAbove = WorldRenderer.getLightmapCoordinates(friar.getWorld(), friar.getPos().up());
		matrixStack.push();
			matrixStack.translate(0.5D, 1.5D, 0.5D);
			matrixStack.multiply(friar.facing().getRotationQuaternion());
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90F));
			matrixStack.push();
				VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(getTexture(friar)));
				this.friarModel.setAngles(friar.isCrafting(), friar.ticksCrafting());
				this.friarModel.render(matrixStack, vertexConsumer, lightAbove, j);
			matrixStack.pop();
		matrixStack.pop();
		
		ItemStack product = friar.getCraftResult();
		if(!product.isEmpty())
		{
			matrixStack.push();
			matrixStack.translate(0.5D, 1D, 0.5D);
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F));
			matrixStack.multiply(friar.facing().getRotationQuaternion());
			matrixStack.scale(0.5F, 0.5F, 0.5F);
			itemRenderer.renderItem(product, ModelTransformationMode.FIXED, lightAbove, j, matrixStack, vertexConsumerProvider, friar.getWorld(), 0);
			matrixStack.pop();
		}
	}
	
	protected Identifier getTexture(ClockworkFriarBlockEntity friar)
	{
		return BASE_TEXTURE;
	}
}
