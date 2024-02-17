package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.EntitySeclusion;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EntitySeclusionRenderer extends EntityRenderer<EntitySeclusion>
{
	public EntitySeclusionRenderer(Context ctx)
	{
		super(ctx);
	}
	
	public Identifier getTexture(EntitySeclusion var1) { return null; }
	
	public void render(EntitySeclusion entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		// FIXME Render horizontal magic circle
	}
}
