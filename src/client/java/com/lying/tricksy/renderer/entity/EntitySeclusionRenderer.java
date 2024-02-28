package com.lying.tricksy.renderer.entity;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Random;

import com.lying.tricksy.entity.EntitySeclusion;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

public class EntitySeclusionRenderer extends EntityRenderer<EntitySeclusion>
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final Identifier TEXTURE_CIRCLE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/magic_circle.png");
	
	public EntitySeclusionRenderer(Context ctx)
	{
		super(ctx);
	}
	
	public Identifier getTexture(EntitySeclusion var1) { return TEXTURE_CIRCLE; }
	
	public boolean shouldRender(EntitySeclusion entity, Frustum frustum, double x, double y, double z) { return true; }
	
	public void render(EntitySeclusion entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		if(entity.isInvisible() || entity.isInvisibleTo(mc.player))
			return;
		
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getText(TEXTURE_CIRCLE));
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		
		float radius = 6F * Math.min((entity.age() + tickDelta) / Reference.Values.TICKS_PER_SECOND, 1F);
		Vec2f[] vertices = new Vec2f[]{
				new Vec2f(-radius, -radius),
				new Vec2f(+radius, -radius),
				new Vec2f(+radius, +radius),
				new Vec2f(-radius, +radius)};
		
		matrices.push();
			buffer.vertex(matrix, vertices[0].x, 0.01F, vertices[0].y).color(255, 255, 255, 255).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[1].x, 0.01F, vertices[1].y).color(255, 255, 255, 255).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[2].x, 0.01F, vertices[2].y).color(255, 255, 255, 255).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[3].x, 0.01F, vertices[3].y).color(255, 255, 255, 255).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
		matrices.pop();
		
		matrices.push();
			buffer.vertex(matrix, vertices[3].x, 0.01F, vertices[3].y).color(255, 255, 255, 255).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[2].x, 0.01F, vertices[2].y).color(255, 255, 255, 255).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[1].x, 0.01F, vertices[1].y).color(255, 255, 255, 255).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			buffer.vertex(matrix, vertices[0].x, 0.01F, vertices[0].y).color(255, 255,255, 255).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
		matrices.pop();
		
		renderBoundary(matrices, vertexConsumers, radius * 0.98F, (float)entity.age, tickDelta, entity.getUuid().getLeastSignificantBits());
	}
	
	private void renderBoundary(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float radius, float ageInTicks, float tickDelta, long seed)
	{
		Random rand = new Random(seed);
		int slices = 180;
		
		float circumference = 2F * (float)(Math.PI * radius);
		float turn = 360F / slices;
		float width = circumference / slices;
		
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		matrices.push();
			Vec2f vecA = new Vec2f(0F, radius);
			Vec2f vecB = new Vec2f(width, 0F).add(vecA);
			
			float out = 1.025F;
			
			for(int i=0; i<slices; i++)
			{
				vecA = vecB;
				vecB = rotateVec(vecB, turn);
				
				float height = 0.6F + (Math.abs(Math.sin((ageInTicks + tickDelta + i)/28F)) * rand.nextFloat() * 0.4F);
				
				buffer.vertex(matrix, vecA.x, 0F, vecA.y).color(255, 255, 255, 255).next();
				buffer.vertex(matrix, vecA.x * out, height, vecA.y * out).color(255, 255, 255, 0).next();
				buffer.vertex(matrix, vecB.x * out, height, vecB.y * out).color(255, 255, 255, 0).next();
				buffer.vertex(matrix, vecB.x, 0F, vecB.y).color(255, 255, 255, 255).next();
			}
		matrices.pop();
	}
	
	private static Vec2f rotateVec(Vec2f vec, float degrees)
	{
		float rads = Math.toRadians(degrees);
		float cos = (float)Math.cos(rads);
		float sin = (float)Math.sin(rads);
		
		float x = (float)(vec.x * cos) - (float)(vec.y * sin);
		float y = (float)(vec.x * sin) + (float)(vec.y * cos);
		return new Vec2f(x, y);
	}
}
