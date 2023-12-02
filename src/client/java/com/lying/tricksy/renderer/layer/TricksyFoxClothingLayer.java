package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyFoxBase;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TricksyFoxClothingLayer extends DyeableClothingLayer<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>>
{
	public static final Identifier TEXTURE_CLOTHING = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox.png");
	public static final Identifier TEXTURE_CLOTHING_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox_overlay.png");
	
	public TricksyFoxClothingLayer(FeatureRendererContext<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>> context)
	{
		super(context, 
				new ModelTricksyFoxMain<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_CLOTHING)), 
				TEXTURE_CLOTHING, 
				TEXTURE_CLOTHING_OVERLAY);
	}
	public void copyModelStateTo(ModelTricksyFoxBase<EntityTricksyFox> contextModel, ModelTricksyFoxBase<EntityTricksyFox> clothingModel)
	{
		contextModel.copyModelStateTo(clothingModel);
	}
}
