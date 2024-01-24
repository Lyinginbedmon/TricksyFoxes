package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyWolfBase;
import com.lying.tricksy.model.entity.ModelTricksyWolfMain;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TricksyWolfBookLayer extends FeatureRenderer<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>>
{
	public static final Identifier texture = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_wolf/holy_book.png");
	
	protected ModelTricksyWolfBase<EntityTricksyWolf> clothingModel;
	
	public TricksyWolfBookLayer(FeatureRendererContext<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>> context)
	{
		super(context);
		this.clothingModel = new ModelTricksyWolfMain<EntityTricksyWolf>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_WOLF_BOOK));
	}
	
	public boolean shouldRender(EntityTricksyWolf living) { return TricksyComponent.isMobMaster(living); }
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityTricksyWolf living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(living.isInvisible() || !shouldRender(living))
			return;
		
		getContextModel().copyModelStateTo(this.clothingModel);
		clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
	}
}
