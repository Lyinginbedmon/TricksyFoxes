package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.projectile.EntityOnryojiFire;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelOnryojiFire;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.renderer.layer.OnryojiFireMaskLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EntityOnryojiFireRenderer extends ModelledEntityRenderer<EntityOnryojiFire, ModelOnryojiFire<EntityOnryojiFire>>
{
	public static final Identifier TEXTURE_TRANSPARENT = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji_fire/onryoji_fire.png");
	public static final Identifier TEXTURE_OPAQUE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji_fire/onryoji_fire_overlay.png");
	
	public EntityOnryojiFireRenderer(Context ctx)
	{
		super(ctx, new ModelOnryojiFire<EntityOnryojiFire>(ctx.getModelLoader().getModelPart(TFModelParts.ONRYOJI_FIRE)));
		this.addFeature(new OnryojiFireMaskLayer(this));
		this.addFeature(new EyesFeatureRenderer<EntityOnryojiFire, ModelOnryojiFire<EntityOnryojiFire>>(this)
				{
					public RenderLayer getEyesTexture()
					{
						return RenderLayer.getEyes(TEXTURE_TRANSPARENT);
					}
				});
	}
	
	public Identifier getTexture(EntityOnryojiFire entity)
	{
		return TEXTURE_OPAQUE;
	}
}
