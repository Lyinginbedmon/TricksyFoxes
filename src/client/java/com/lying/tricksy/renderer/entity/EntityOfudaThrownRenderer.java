package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.projectile.EntityOfudaThrown;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EntityOfudaThrownRenderer extends EntityRenderer<EntityOfudaThrown>
{
	private static final ModelIdentifier model = new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=up");
	private final BlockRenderManager blockRenderManager;
	
	public EntityOfudaThrownRenderer(Context ctx)
	{
		super(ctx);
		this.blockRenderManager = ctx.getBlockRenderManager();
	}
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(EntityOfudaThrown var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void render(EntityOfudaThrown entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		super.render(entity, g, i, matrixStack, vertexConsumerProvider, i);
		matrixStack.push();
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			matrixStack.translate(-0.5F, 0F, -0.5F);
			this.blockRenderManager.getModelRenderer().render(matrixStack.peek(), vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntitySolid()), null, bakedModelManager.getModel(model), 1F, 1F, 1F, i, OverlayTexture.DEFAULT_UV);
		matrixStack.pop();
	}
}
