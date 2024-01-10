package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyWolfBase;
import com.lying.tricksy.model.entity.ModelTricksyWolfMain;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

public class TricksyWolfClothingLayer extends DyeableClothingLayer<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>>
{
	public static final Identifier TEXTURE_CLOTHING = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_wolf/clothing.png");
	public static final Identifier TEXTURE_CLOTHING_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_wolf/clothing_overlay.png");
	
	public TricksyWolfClothingLayer(FeatureRendererContext<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>> context)
	{
		this(context, TEXTURE_CLOTHING, TEXTURE_CLOTHING_OVERLAY);
	}
	
	public TricksyWolfClothingLayer(FeatureRendererContext<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>> context, Identifier textureA, Identifier textureB)
	{
		super(context, 
				new ModelTricksyWolfMain<EntityTricksyWolf>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_WOLF_CLOTHING)), textureA, textureB);
	}
	
	public void copyModelStateTo(ModelTricksyWolfBase<EntityTricksyWolf> contextModel, ModelTricksyWolfBase<EntityTricksyWolf> clothingModel)
	{
		contextModel.copyModelStateTo(clothingModel);
	}
}
