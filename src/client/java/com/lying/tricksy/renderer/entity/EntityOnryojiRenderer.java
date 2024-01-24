package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelOnryoji;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.renderer.layer.OnryojiMaskLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EntityOnryojiRenderer extends MobEntityRenderer<EntityOnryoji, ModelOnryoji<EntityOnryoji>>
{
	public static final Identifier TEXTURE_TRANSPARENT = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji/onryoji.png");
	public static final Identifier TEXTURE_OPAQUE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji/onryoji_overlay.png");
	
	public EntityOnryojiRenderer(Context ctx)
	{
		super(ctx, new ModelOnryoji<EntityOnryoji>(ctx.getModelLoader().getModelPart(TFModelParts.ONRYOJI)), 0F);
		this.addFeature(new EyesFeatureRenderer<EntityOnryoji, ModelOnryoji<EntityOnryoji>>(this)
				{
					public RenderLayer getEyesTexture()
					{
						return RenderLayer.getEyes(TEXTURE_TRANSPARENT);
					}
				});
		this.addFeature(new OnryojiMaskLayer(this));
	}
	
	public Identifier getTexture(EntityOnryoji entity)
	{
		return TEXTURE_OPAQUE;
	}
}
