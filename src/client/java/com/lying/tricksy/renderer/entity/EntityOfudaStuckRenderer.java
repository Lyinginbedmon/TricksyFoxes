package com.lying.tricksy.renderer.entity;

import java.util.Map;
import java.util.Optional;

import com.lying.tricksy.entity.projectile.EntityOfudaStuck;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class EntityOfudaStuckRenderer extends EntityRenderer<EntityOfudaStuck>
{
	private static final Map<Direction, ModelIdentifier> MODELS = Map.of(
			Direction.UP, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=up"),
			Direction.DOWN, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=down"),
			Direction.NORTH, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=north"),
			Direction.EAST, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=east"),
			Direction.SOUTH, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=south"),
			Direction.WEST, new ModelIdentifier(Reference.ModInfo.MOD_ID, "ofuda", "facing=west"));
	private static final Identifier EXPLOSION_BEAM_TEXTURE = new Identifier("textures/entity/guardian_beam.png");
	 private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE);
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
		
		Optional<Entity> boundTarget = entity.getTarget();
		if(entity.hasTarget() && boundTarget.isPresent())
			renderBinding(boundTarget.get(), entity, g, matrixStack, vertexConsumerProvider, i);
		
		ModelIdentifier model = MODELS.getOrDefault(entity.getFacing(), null);
		if(model == null)
			return;
		
		matrixStack.push();
			BakedModelManager bakedModelManager = this.blockRenderManager.getModels().getModelManager();
			if(entity.getFacing().getAxis() == Axis.Y)
				matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - entity.getYaw()));
			matrixStack.push();
				setupTransforms(entity, matrixStack);
				this.blockRenderManager.getModelRenderer().render(matrixStack.peek(), vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntitySolid()), null, bakedModelManager.getModel(model), 1F, 1F, 1F, i, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
		matrixStack.pop();
	}
	
	public void renderBinding(Entity boundTarget, Entity ofuda, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrixStack.push();
			matrixStack.translate(0, -2, 0);
			Vec3d dest = boundTarget.getPos().add(0D, boundTarget.getBoundingBox().getYLength() * 0.5F, 0D);
			Vec3d origin = ofuda.getEyePos();
			Vec3d offset = dest.subtract(origin);
			if(offset.length() > 1D)
			{
				offset = offset.normalize().multiply(Math.min(4D, offset.length() - 1D));
				float p = (float)offset.getX();
				float q = (float)offset.getY();
				float r = (float)offset.getZ();
				EnderDragonEntityRenderer.renderCrystalBeam(p, q, r, g, ofuda.age / 4, matrixStack, vertexConsumerProvider, i);
			}
		matrixStack.pop();
	}
	
	public void setupTransforms(EntityOfudaStuck entity, MatrixStack matrixStack)
	{
		Direction face = entity.getFacing();
		if(face.getAxis() == Axis.Y)
			matrixStack.translate(-0.5F, face == Direction.DOWN ? -0.93F : 0F, -0.5F);
		else
		{
			matrixStack.translate(-0.5F, -0.1875F, -0.5F);
			matrixStack.translate(face.getOffsetX() * 0.5D, 0D, face.getOffsetZ() * 0.5D);
		}
	}
}
