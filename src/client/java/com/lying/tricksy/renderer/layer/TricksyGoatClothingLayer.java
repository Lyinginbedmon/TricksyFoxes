package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyGoatBase;
import com.lying.tricksy.model.entity.ModelTricksyGoatMain;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TricksyGoatClothingLayer extends DyeableClothingLayer<EntityTricksyGoat, ModelTricksyGoatBase<EntityTricksyGoat>>
{
	public static final Identifier TEXTURE_CLOTHING = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_goat/clothing.png");
	public static final Identifier TEXTURE_CLOTHING_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_goat/clothing_overlay.png");
	
	public TricksyGoatClothingLayer(FeatureRendererContext<EntityTricksyGoat, ModelTricksyGoatBase<EntityTricksyGoat>> context)
	{
		super(context, 
				new ModelTricksyGoatMain<EntityTricksyGoat>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_GOAT_CLOTHING)), 
				TEXTURE_CLOTHING, 
				TEXTURE_CLOTHING_OVERLAY);
	}
	
	public void copyModelStateTo(ModelTricksyGoatBase<EntityTricksyGoat> contextModel, ModelTricksyGoatBase<EntityTricksyGoat> clothingModel)
	{
		contextModel.copyModelStateTo(clothingModel);
	}
}
