package com.lying.tricksy.client.renderer.entity;

import com.lying.tricksy.client.TFModelParts;
import com.lying.tricksy.client.model.entity.ModelTricksyFox;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

public class EntityTricksyFoxRenderer extends BipedEntityRenderer<EntityTricksyFox, ModelTricksyFox<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_RED = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox.png");
	public static final Identifier TEXTURE_SNOW = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox_snow.png");
	
	public EntityTricksyFoxRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyFox<EntityTricksyFox>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_FOX)), 0.5F);
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
