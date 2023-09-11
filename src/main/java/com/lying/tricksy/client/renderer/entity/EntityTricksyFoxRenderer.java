package com.lying.tricksy.client.renderer.entity;

import com.lying.tricksy.client.TFModelParts;
import com.lying.tricksy.client.model.entity.ModelTricksyFox;
import com.lying.tricksy.client.renderer.layer.TricksyFoxClothingLayer;
import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

public class EntityTricksyFoxRenderer extends BipedEntityRenderer<EntityTricksyFox, ModelTricksyFox<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_RED = new Identifier("textures/entity/fox/fox.png");
	public static final Identifier TEXTURE_SNOW = new Identifier("textures/entity/fox/snow_fox.png");
	
	public EntityTricksyFoxRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyFox<EntityTricksyFox>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_FOX)), 0.5F);
		this.addFeature(new TricksyFoxClothingLayer(this));
	}
	
	public Identifier getTexture(EntityTricksyFox entity)
	{
		switch(entity.getVariant())
		{
			case SNOW:
				return TEXTURE_SNOW;
			case RED:
			default:
				return TEXTURE_RED;
		}
	}
}
