package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyFoxBase;
import com.lying.tricksy.model.entity.ModelTricksyFoxCrouching;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TricksyFoxClothingLayer extends DyeableClothingLayer<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_CLOTHING = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox.png");
	public static final Identifier TEXTURE_CLOTHING_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox_overlay.png");
	
	private final ModelTricksyFoxBase<EntityTricksyFox> standing;
	private final ModelTricksyFoxBase<EntityTricksyFox> crouching;
	
	public TricksyFoxClothingLayer(FeatureRendererContext<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>> context)
	{
		super(context, 
				new ModelTricksyFoxMain<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_CLOTHING)), 
				TEXTURE_CLOTHING, 
				TEXTURE_CLOTHING_OVERLAY);
		
		this.standing = this.clothingModel;
		this.crouching = new ModelTricksyFoxCrouching<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_CROUCHING_CLOTHING));
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityTricksyFox living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		switch(living.getTreePose())
		{
			case CROUCHING:
				this.clothingModel = this.crouching;
				break;
			case STANDING:
			default:
				this.clothingModel = this.standing;
				break;
		}
		super.render(matrices, vertexConsumers, light, living, limbAngle, limbDistance, age, headYaw, headPitch, tickDelta);
	}
	
	public void copyModelStateTo(ModelTricksyFoxBase<EntityTricksyFox> contextModel, ModelTricksyFoxBase<EntityTricksyFox> clothingModel)
	{
		contextModel.copyModelStateTo(clothingModel);
	}
}
