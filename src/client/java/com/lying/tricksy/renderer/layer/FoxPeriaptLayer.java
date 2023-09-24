package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.layer.ModelFoxPeriapt;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.Identifier;

public class FoxPeriaptLayer extends FeatureRenderer<FoxEntity, FoxEntityModel<FoxEntity>>
{
	private final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/periapt_fox.png");
	private final ModelFoxPeriapt<FoxEntity> periaptModel;
	
	public FoxPeriaptLayer(FeatureRendererContext<FoxEntity, FoxEntityModel<FoxEntity>> context)
	{
		super(context);
		this.periaptModel = new ModelFoxPeriapt<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.PERIAPT_FOX));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, FoxEntity living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(!TFComponents.TRICKSY_TRACKING.get(living).hasPeriapt())
			return;
		render(this.getContextModel(), this.periaptModel, TEXTURE, matrices, vertexConsumers, light, living, limbAngle, limbDistance, age, headYaw, headPitch, tickDelta, 1, 1, 1);
	}
}
