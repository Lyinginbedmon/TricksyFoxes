package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.layer.ModelGoatPeriapt;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.GoatEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.util.Identifier;

public class GoatPeriaptLayer extends FeatureRenderer<GoatEntity, GoatEntityModel<GoatEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/periapt_goat.png");
	private final ModelGoatPeriapt<GoatEntity> periaptModel;
	
	public GoatPeriaptLayer(FeatureRendererContext<GoatEntity, GoatEntityModel<GoatEntity>> context)
	{
		super(context);
		this.periaptModel = new ModelGoatPeriapt<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.PERIAPT_GOAT));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, GoatEntity living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(!TFComponents.TRICKSY_TRACKING.get(living).hasPeriapt())
			return;
		render(this.getContextModel(), this.periaptModel, TEXTURE, matrices, vertexConsumers, light, living, limbAngle, limbDistance, age, headYaw, headPitch, tickDelta, 1, 1, 1);
	}
}
