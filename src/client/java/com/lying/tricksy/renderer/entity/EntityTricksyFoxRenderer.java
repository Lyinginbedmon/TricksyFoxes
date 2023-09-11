package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.TFModelParts;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.model.entity.ModelTricksyFox;
import com.lying.tricksy.renderer.layer.TricksyFoxClothingLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
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
