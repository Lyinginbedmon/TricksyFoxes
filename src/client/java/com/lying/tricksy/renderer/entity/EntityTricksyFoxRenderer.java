package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyFoxBase;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.model.entity.ModelTricksyFoxSleeping;
import com.lying.tricksy.renderer.layer.TricksyFoxHeldItemLayer;
import com.lying.tricksy.renderer.layer.TricksyFoxClothingLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EntityTricksyFoxRenderer extends MobEntityRenderer<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_RED = new Identifier("textures/entity/fox/fox.png");
	public static final Identifier TEXTURE_SNOW = new Identifier("textures/entity/fox/snow_fox.png");
	
	public static final Identifier TEXTURE_RED_SLEEPING = new Identifier("textures/entity/fox/fox_sleep.png");
	public static final Identifier TEXTURE_SNOW_SLEEPING = new Identifier("textures/entity/fox/snow_fox_sleep.png");
	
	private final ModelTricksyFoxBase<EntityTricksyFox> standing;
	private final ModelTricksyFoxBase<EntityTricksyFox> sleeping;
	
	public EntityTricksyFoxRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyFoxMain<EntityTricksyFox>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_FOX)), 0.5F);
		this.addFeature(new TricksyFoxClothingLayer(this));
		this.addFeature(new TricksyFoxHeldItemLayer(this, ctx.getHeldItemRenderer()));
		
		this.standing = this.model;
		this.sleeping = new ModelTricksyFoxSleeping<EntityTricksyFox>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_SLEEPING));
	}
	
	@Override
	public void render(EntityTricksyFox mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		this.model = mobEntity.isSleeping() ? this.sleeping : this.standing;
		super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}
	
	public Identifier getTexture(EntityTricksyFox entity)
	{
		switch(entity.getVariant())
		{
			case SNOW:
				return entity.isSleeping() ? TEXTURE_SNOW_SLEEPING : TEXTURE_SNOW;
			case RED:
			default:
				return entity.isSleeping() ? TEXTURE_RED_SLEEPING : TEXTURE_RED;
		}
	}
}
