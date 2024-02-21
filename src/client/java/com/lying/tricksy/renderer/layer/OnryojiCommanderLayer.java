package com.lying.tricksy.renderer.layer;

import org.joml.Math;
import org.joml.Matrix4f;

import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.model.entity.ModelOnryoji;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

public class OnryojiCommanderLayer extends FeatureRenderer<EntityOnryoji, ModelOnryoji<EntityOnryoji>>
{
	private static final Identifier[] ICONS = new Identifier[12];
	
	public OnryojiCommanderLayer(FeatureRendererContext<EntityOnryoji, ModelOnryoji<EntityOnryoji>> context)
	{
		super(context);
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, EntityOnryoji living, float limbSwing, float limbSwingAmount, float tickDelta, float ageInTicks, float netHeadYaw, float headPitch)
	{
		int icons = living.getCommanders();
		icons = 12;
		if(icons == 0)
			return;
		
		float turn = 360F / icons;
		Vec2f offset = rotateVec(new Vec2f(0F, 1.5F), turn * 0.5F + ((float)ageInTicks + tickDelta));
		
		float wide = 0.5F;
		wide *= 0.5F;
		Vec2f[] vertices = new Vec2f[]{
				new Vec2f(-wide, -wide),
				new Vec2f(+wide, -wide),
				new Vec2f(+wide, +wide),
				new Vec2f(-wide, +wide)};
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		for(int i=0; i<icons; i++)
		{
			Vec2f pos = offset.add(new Vec2f(0F, 1F));
			
			VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getText(ICONS[i]));
			matrixStack.push();
				buffer.vertex(matrix, vertices[0].add(pos).x, vertices[0].add(pos).y, 0F).color(255, 255, 255, 170).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[1].add(pos).x, vertices[1].add(pos).y, 0F).color(255, 255, 255, 170).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[2].add(pos).x, vertices[2].add(pos).y, 0F).color(255, 255, 255, 170).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[3].add(pos).x, vertices[3].add(pos).y, 0F).color(255, 255, 255, 170).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			matrixStack.pop();
			
			matrixStack.push();
				buffer.vertex(matrix, vertices[3].add(pos).x, vertices[3].add(pos).y, 0F).color(255, 255, 255, 170).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[2].add(pos).x, vertices[2].add(pos).y, 0F).color(255, 255, 255, 170).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[1].add(pos).x, vertices[1].add(pos).y, 0F).color(255, 255, 255, 170).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				buffer.vertex(matrix, vertices[0].add(pos).x, vertices[0].add(pos).y, 0F).color(255, 255,255, 170).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
			matrixStack.pop();
			
			offset = rotateVec(offset, turn);
		}
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
	
	static
	{
		for(int i=0; i<12; i++)
			ICONS[i] = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji/commander_"+i+".png");
	}
}
