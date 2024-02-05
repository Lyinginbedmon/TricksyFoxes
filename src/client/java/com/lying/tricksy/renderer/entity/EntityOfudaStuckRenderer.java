package com.lying.tricksy.renderer.entity;

import java.util.Map;

import com.lying.tricksy.entity.projectile.EntityOfudaStuck;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class EntityOfudaStuckRenderer extends EntityRenderer<EntityOfudaStuck>
{
	private static final Map<Direction, ModelIdentifier> MODELS = Map.of(
			Direction.UP, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=up"),
			Direction.DOWN, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=down"),
			Direction.NORTH, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=north"),
			Direction.EAST, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=east"),
			Direction.SOUTH, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=south"),
			Direction.WEST, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=west"));
	private final BlockRenderManager blockRenderManager;
	
	public EntityOfudaStuckRenderer(Context ctx)
	{
		super(ctx);
		this.blockRenderManager = ctx.getBlockRenderManager();
	}
	
	@SuppressWarnings("deprecation")
	public Identifier getTexture(EntityOfudaStuck var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	public void render(EntityOfudaStuck entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		super.render(entity, g, i, matrixStack, vertexConsumerProvider, i);
		ModelIdentifier model = MODELS.getOrDefault(entity.getFacing(), null);
		if(model == null)
			return;
		
		matrixStack.push();
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			Direction face = entity.getFacing();
			if(face.getAxis() == Axis.Y)
				matrixStack.translate(-0.5F, face == Direction.DOWN ? -0.93F : 0F, -0.5F);
			else
			{
				matrixStack.translate(-0.5F, -0.1875F, -0.5F);
				matrixStack.translate(face.getOffsetX() * 0.5D, 0D, face.getOffsetZ() * 0.5D);
			}
			this.blockRenderManager.getModelRenderer().render(matrixStack.peek(), vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntitySolid()), null, bakedModelManager.getModel(model), 1F, 1F, 1F, i, OverlayTexture.DEFAULT_UV);
		matrixStack.pop();
	}
}
