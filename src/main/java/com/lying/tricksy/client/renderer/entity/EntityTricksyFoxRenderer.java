package com.lying.tricksy.client.renderer.entity;

import com.lying.tricksy.client.TFModelParts;
import com.lying.tricksy.client.model.ModelTricksyFox;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

public class EntityTricksyFoxRenderer extends BipedEntityRenderer<EntityTricksyFox, ModelTricksyFox>
{
	public static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox.png");
	
	public EntityTricksyFoxRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyFox(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_FOX)), 0.5F);
	}
	
	public Identifier getTexture(EntityTricksyFox entity)
	{
		return TEXTURE;
	}
}
