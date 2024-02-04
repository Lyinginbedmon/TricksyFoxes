package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.projectile.EntityOnryojiFire;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelOnryojiFire;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class OnryojiFireMaskLayer extends FeatureRenderer<EntityOnryojiFire, ModelOnryojiFire<EntityOnryojiFire>>
{
	public static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/onryoji_fire/onryoji_fire_mask.png");
	
	protected final ModelOnryojiFire<EntityOnryojiFire> maskModel;
	
	public OnryojiFireMaskLayer(FeatureRendererContext<EntityOnryojiFire, ModelOnryojiFire<EntityOnryojiFire>> context)
	{
		super(context);
		maskModel = new ModelOnryojiFire<EntityOnryojiFire>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.ONRYOJI_FIRE_MASK));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityOnryojiFire living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		getContextModel().copyModelStateTo(this.maskModel);
		maskModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
	}
}
