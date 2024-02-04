package com.lying.tricksy.init;

import com.lying.tricksy.model.armor.ModelSageHat;
import com.lying.tricksy.model.block.ModelClockworkFriar;
import com.lying.tricksy.model.entity.ModelOnryoji;
import com.lying.tricksy.model.entity.ModelOnryojiFire;
import com.lying.tricksy.model.entity.ModelTricksyFoxCrouching;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.model.entity.ModelTricksyFoxSleeping;
import com.lying.tricksy.model.entity.ModelTricksyGoatMain;
import com.lying.tricksy.model.entity.ModelTricksyGoatSleeping;
import com.lying.tricksy.model.entity.ModelTricksyWolfMain;
import com.lying.tricksy.model.layer.ModelFoxPeriapt;
import com.lying.tricksy.model.layer.ModelGoatPeriapt;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.TexturedModelDataProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TFModelParts
{
	public static final EntityModelLayer PERIAPT_FOX	= ofName("fox_periapt", "main");
	public static final EntityModelLayer PERIAPT_GOAT	= ofName("goat_periapt", "main");
	
	public static final EntityModelLayer TRICKSY_FOX					= ofName("tricksy_fox", "main");
	public static final EntityModelLayer TRICKSY_FOX_MASK				= ofName("tricksy_fox", "mask");
	public static final EntityModelLayer TRICKSY_FOX_CLOTHING			= ofName("tricksy_fox", "clothing");
	public static final EntityModelLayer TRICKSY_FOX_CROUCHING			= ofName("tricksy_fox", "crouching");
	public static final EntityModelLayer TRICKSY_FOX_CROUCHING_MASK		= ofName("tricksy_fox", "crouching_mask");
	public static final EntityModelLayer TRICKSY_FOX_CROUCHING_CLOTHING	= ofName("tricksy_fox", "crouching_clothing");
	public static final EntityModelLayer TRICKSY_FOX_SLEEPING			= ofName("tricksy_fox", "sleeping");
	
	public static final EntityModelLayer TRICKSY_GOAT			= ofName("tricksy_goat", "main");
	public static final EntityModelLayer TRICKSY_GOAT_CLOTHING	= ofName("tricksy_goat", "clothing");
	public static final EntityModelLayer TRICKSY_GOAT_SLEEPING	= ofName("tricksy_goat", "sleeping");
	
	public static final EntityModelLayer TRICKSY_WOLF			= ofName("tricksy_wolf", "main");
	public static final EntityModelLayer TRICKSY_WOLF_CLOTHING	= ofName("tricksy_wolf", "clothing");
	public static final EntityModelLayer TRICKSY_WOLF_BOOK		= ofName("tricksy_wolf", "book");
	
	public static final EntityModelLayer ONRYOJI		= ofName("onryoji", "main");
	public static final EntityModelLayer ONRYOJI_MASK	= ofName("onryoji", "mask");
	
	public static final EntityModelLayer ONRYOJI_FIRE		= ofName("onryoji_fire", "main");
	public static final EntityModelLayer ONRYOJI_FIRE_MASK	= ofName("onryoji_fire", "mask");
	
	public static final EntityModelLayer SAGE_HAT	= ofName("sage_hat", "main");
	
	public static final EntityModelLayer CLOCKWORK_FRIAR = ofName("clockwork_friar", "main");
	
	private static EntityModelLayer ofName(String main, String part)
	{
		return new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, main), part);
	}
	
	public static void init()
	{
		register(TFModelParts.TRICKSY_FOX, ModelTricksyFoxMain::getMainModel);
		register(TFModelParts.TRICKSY_FOX_MASK, ModelTricksyFoxMain::getMaskModel);
		register(TFModelParts.TRICKSY_FOX_CLOTHING, ModelTricksyFoxMain::getOuterModel);
		register(TFModelParts.TRICKSY_FOX_CROUCHING, ModelTricksyFoxCrouching::getMainModel);
		register(TFModelParts.TRICKSY_FOX_CROUCHING_MASK, ModelTricksyFoxCrouching::getMaskModel);
		register(TFModelParts.TRICKSY_FOX_CROUCHING_CLOTHING, ModelTricksyFoxCrouching::getOuterModel);
		register(TFModelParts.TRICKSY_FOX_SLEEPING, ModelTricksyFoxSleeping::getMainModel);
		
		register(TFModelParts.TRICKSY_GOAT, ModelTricksyGoatMain::getMainModel);
		register(TFModelParts.TRICKSY_GOAT_CLOTHING, ModelTricksyGoatMain::getOuterModel);
		register(TFModelParts.TRICKSY_GOAT_SLEEPING, ModelTricksyGoatSleeping::getModelData);
		
		register(TFModelParts.TRICKSY_WOLF, ModelTricksyWolfMain::getMainModel);
		register(TFModelParts.TRICKSY_WOLF_CLOTHING, ModelTricksyWolfMain::getOuterModel);
		register(TFModelParts.TRICKSY_WOLF_BOOK, ModelTricksyWolfMain::getBookModel);
		
		register(TFModelParts.ONRYOJI, ModelOnryoji::getMainModel);
		register(TFModelParts.ONRYOJI_MASK, ModelOnryoji::getMaskModel);
		
		register(TFModelParts.ONRYOJI_FIRE, ModelOnryojiFire::getMainModel);
		register(TFModelParts.ONRYOJI_FIRE_MASK, ModelOnryojiFire::getMaskModel);
		
		register(TFModelParts.SAGE_HAT, ModelSageHat::getTexturedModelData);
		register(TFModelParts.PERIAPT_FOX, ModelFoxPeriapt::getTexturedModelData);
		register(TFModelParts.PERIAPT_GOAT, ModelGoatPeriapt::getTexturedModelData);
		
		register(TFModelParts.CLOCKWORK_FRIAR, ModelClockworkFriar::getTexturedModelData);
	}
	
	private static void register(EntityModelLayer layer, TexturedModelDataProvider func)
	{
		EntityModelLayerRegistry.registerModelLayer(layer, func);
	}
}
