package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.api.entity.ITricksyMob;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class DyeableClothingLayer<T extends PathAwareEntity & ITricksyMob<?>, M extends EntityModel<T>> extends FeatureRenderer<T, M>
{
	public final Identifier texture;
	public final Identifier textureOverlay;
	
	protected M clothingModel;
	
	public DyeableClothingLayer(FeatureRendererContext<T, M> context, M clothingModel, Identifier textureA, Identifier textureB)
	{
		super(context);
		this.clothingModel = clothingModel;
		this.texture = textureA;
		this.textureOverlay = textureB;
	}
	
	public boolean shouldRender(T living) { return true; }
	
	public Identifier getTextureFor(T living, boolean overlay)
	{
		return overlay ? textureOverlay : texture;
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(living.isInvisible() || !shouldRender(living))
			return;
		
		copyModelStateTo(getContextModel(), clothingModel);
		
        clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getTextureFor(living, false))), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        
        float s, t, u;
        if (living.hasCustomName() && "jeb_".equals(living.getName().getString()))
        {
            int n = living.age / 25 + living.getId();
            int o = DyeColor.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = ((float)(living.age % 25) + age) / 25.0f;
            float[] fs = SheepEntity.getRgbColor((DyeColor)DyeColor.byId((int)p));
            float[] gs = SheepEntity.getRgbColor((DyeColor)DyeColor.byId((int)q));
            s = fs[0] * (1.0f - r) + gs[0] * r;
            t = fs[1] * (1.0f - r) + gs[1] * r;
            u = fs[2] * (1.0f - r) + gs[2] * r;
        }
        else
        {
        	int color = living.getColor();
            s = ((color & 0xFF0000) >> 16) / 255F;
            t = ((color & 0xFF00) >> 8) / 255F;
            u = ((color & 0xFF) >> 0) / 255F;
        }
        
        clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getTextureFor(living, true))), light, OverlayTexture.DEFAULT_UV, s, t, u, 1F);
	}
	
	public abstract void copyModelStateTo(M contextModel, M clothingModel);
}
