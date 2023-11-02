package com.lying.tricksy.init;

import com.lying.tricksy.model.armor.ModelSageHat;
import com.lying.tricksy.model.block.ModelClockworkFriar;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.model.entity.ModelTricksyFoxSleeping;
import com.lying.tricksy.model.layer.ModelFoxPeriapt;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TFModelParts
{
	public static final EntityModelLayer PERIAPT_FOX	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "fox_periapt"), "main");
	
	public static final EntityModelLayer TRICKSY_FOX	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "tricksy_fox"), "main");
	public static final EntityModelLayer TRICKSY_FOX_SLEEPING	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "tricksy_fox"), "sleeping");
	public static final EntityModelLayer TRICKSY_FOX_CLOTHING	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "tricksy_fox"), "clothing");
	
	public static final EntityModelLayer SAGE_HAT	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "sage_hat"), "main");
	
	public static final EntityModelLayer CLOCKWORK_FRIAR = new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "clockwork_friar"), "main");
	
	public static void init()
	{
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.TRICKSY_FOX, ModelTricksyFoxMain::getMainModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.TRICKSY_FOX_SLEEPING, ModelTricksyFoxSleeping::getMainModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.TRICKSY_FOX_CLOTHING, ModelTricksyFoxMain::getOuterModel);
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.SAGE_HAT, ModelSageHat::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.PERIAPT_FOX, ModelFoxPeriapt::getTexturedModelData);
		
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.CLOCKWORK_FRIAR, ModelClockworkFriar::getTexturedModelData);
	}
}
