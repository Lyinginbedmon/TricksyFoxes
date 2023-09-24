package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyFox;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TricksyFoxClothingLayer extends FeatureRenderer<EntityTricksyFox, ModelTricksyFox<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_CLOTHING = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox.png");
	public static final Identifier TEXTURE_CLOTHING_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox_overlay.png");
	
	private final ModelTricksyFox<EntityTricksyFox> clothingModel;
	
	public TricksyFoxClothingLayer(FeatureRendererContext<EntityTricksyFox, ModelTricksyFox<EntityTricksyFox>> context)
	{
		super(context);
		this.clothingModel = new ModelTricksyFox<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_CLOTHING));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityTricksyFox living, float var5, float var6, float partialTicks, float var8, float var9, float var10)
	{
		if(living.isInvisible())
			return;
		
		this.getContextModel().copyBipedStateTo(clothingModel);
		
        clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE_CLOTHING)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        
        float s, t, u;
        if (living.hasCustomName() && "jeb_".equals(living.getName().getString()))
        {
            int n = living.age / 25 + living.getId();
            int o = DyeColor.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = ((float)(living.age % 25) + partialTicks) / 25.0f;
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
        clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE_CLOTHING_OVERLAY)), light, OverlayTexture.DEFAULT_UV, s, t, u, 1F);
	}
	
}
