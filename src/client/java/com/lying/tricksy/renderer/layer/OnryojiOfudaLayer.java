package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.model.entity.ModelOnryoji;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;

public class OnryojiOfudaLayer extends FeatureRenderer<EntityOnryoji, ModelOnryoji<EntityOnryoji>>
{
	private static final ModelIdentifier model = new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=north");
	private final BlockRenderManager blockRenderManager;
	
	public OnryojiOfudaLayer(FeatureRendererContext<EntityOnryoji, ModelOnryoji<EntityOnryoji>> context)
	{
		super(context);
		this.blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, EntityOnryoji living, float limbSwing, float limbSwingAmount, float tickDelta, float ageInTicks, float netHeadYaw, float headPitch)
	{
		int count = living.getDataTracker().get(EntityOnryoji.OFUDA).intValue();
		if(living.animations.currentAnim() != EntityOnryoji.ANIM_OFUDA || count == 0)
			return;
		
		BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
		matrixStack.push();
			matrixStack.translate(-0.5D, 0D, -1.5D);
			if(count > 1)
				matrixStack.translate(((float)count * -0.5F), 0F, 0F);
			
			for(int i=count; i>0; i--)
			{
				renderOfuda(matrixStack, vertexConsumerProvider, bakedModelManager, light);
				matrixStack.translate(1F, 0F, 0F);
			}
		matrixStack.pop();
	}
	
	private void renderOfuda(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, BakedModelManager bakedModelManager, int light)
	{
		this.blockRenderManager.getModelRenderer().render(matrixStack.peek(), vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntitySolid()), null, bakedModelManager.getModel(model), 1F, 1F, 1F, light, OverlayTexture.DEFAULT_UV);
	}
}
