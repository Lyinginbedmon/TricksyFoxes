package com.lying.tricksy.renderer.entity;

import java.util.Map;
import java.util.Optional;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.lying.tricksy.entity.projectile.EntityOfudaStuck;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
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
	private static final Identifier EXPLOSION_BEAM_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID,"textures/chain.png");
	private static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE);
	private final BlockRenderManager blockRenderManager;
	
	public EntityOfudaStuckRenderer(Context ctx)
	{
		super(ctx);
		this.blockRenderManager = ctx.getBlockRenderManager();
	}
	
	public Identifier getTexture(EntityOfudaStuck var1) { return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE; }
	
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
	
	public void renderBinding(Entity boundTarget, EntityOfudaStuck ofuda, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrixStack.push();
			Vec3d dest = boundTarget.getPos().add(0D, boundTarget.getBoundingBox().getYLength() * 0.5F, 0D);
			Vec3d origin = ofuda.getEyePos();
			if(ofuda.getFacing().getAxis() != Axis.Y)
				origin = origin.add(0, 0.3D, 0D);
			matrixStack.translate(origin.getX() - ofuda.getX(), origin.getY() - ofuda.getY(), origin.getZ() - ofuda.getZ());
			renderChain(origin, dest, 0F, matrixStack, vertexConsumerProvider, LAYER, 1, 212, 175, 55);
		matrixStack.pop();
	}
	
	public static void renderChain(Vec3d origin, Vec3d dest, float roll, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, RenderLayer layer, int textureHeight, int red, int green, int blue)
	{
		Vec3d offset = dest.subtract(origin);
        matrixStack.push();
	        float chainLength = (float)offset.length();
	        offset = offset.normalize();
	        float n = (float)Math.acos(offset.y);
	        float o = (float)Math.atan2(offset.z, offset.x);
	        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - o) * 57.295776f));
	        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * 57.295776f));
	        float af = MathHelper.cos((float)(roll + (float)Math.PI)) * 0.2f;
	        float ag = MathHelper.sin((float)(roll + (float)Math.PI)) * 0.2f;
	        float ah = MathHelper.cos((float)(roll + 0.0f)) * 0.2f;
	        float ai = MathHelper.sin((float)(roll + 0.0f)) * 0.2f;
	        float aj = MathHelper.cos((float)(roll + 1.5707964f)) * 0.2f;
	        float ak = MathHelper.sin((float)(roll + 1.5707964f)) * 0.2f;
	        float al = MathHelper.cos((float)(roll + 4.712389f)) * 0.2f;
	        float am = MathHelper.sin((float)(roll + 4.712389f)) * 0.2f;
	        float an = chainLength;
	        float textureStart = 0F;
	        float textureEnd = chainLength / (float)textureHeight;
	        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(layer);
	        MatrixStack.Entry entry = matrixStack.peek();
	        Matrix4f matrix4f = entry.getPositionMatrix();
	        Matrix3f matrix3f = entry.getNormalMatrix();
	        vertex(vertexConsumer, matrix4f, matrix3f, af, an, ag, red, green, blue, 0.4999f, textureStart);
	        vertex(vertexConsumer, matrix4f, matrix3f, af, 0.0f, ag, red, green, blue, 0.4999f, textureEnd);
	        vertex(vertexConsumer, matrix4f, matrix3f, ah, 0.0f, ai, red, green, blue, 0.0f, textureEnd);
	        vertex(vertexConsumer, matrix4f, matrix3f, ah, an, ai, red, green, blue, 0.0f, textureStart);
	        
	        vertex(vertexConsumer, matrix4f, matrix3f, aj, an, ak, red, green, blue, 0.4999f, textureStart);
	        vertex(vertexConsumer, matrix4f, matrix3f, aj, 0.0f, ak, red, green, blue, 0.4999f, textureEnd);
	        vertex(vertexConsumer, matrix4f, matrix3f, al, 0.0f, am, red, green, blue, 0.0f, textureEnd);
	        vertex(vertexConsumer, matrix4f, matrix3f, al, an, am, red, green, blue, 0.0f, textureStart);
	    matrixStack.pop();
	}
	
	private static void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, float z, int red, int green, int blue, float u, float v)
	{
			vertexConsumer.vertex(positionMatrix, x, y, z).color(red, green, blue, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0.0f, 1.0f, 0.0f).next();
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
