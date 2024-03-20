package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.layer.ModelWolfPeriapt;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;

public class WolfPeriaptLayer extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>>
{
	private static final Identifier TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/periapt_wolf.png");
	private final ModelWolfPeriapt<WolfEntity> periaptModel;
	
	public WolfPeriaptLayer(FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context)
	{
		super(context);
		this.periaptModel = new ModelWolfPeriapt<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.PERIAPT_WOLF));
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, WolfEntity living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(!TFComponents.TRICKSY_TRACKING.get(living).hasPeriapt())
			return;
		render(this.getContextModel(), this.periaptModel, TEXTURE, matrices, vertexConsumers, light, living, limbAngle, limbDistance, age, headYaw, headPitch, tickDelta, 1, 1, 1);
	}
}
