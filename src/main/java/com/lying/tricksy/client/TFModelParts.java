package com.lying.tricksy.client;

import com.lying.tricksy.client.model.armor.ModelSageHat;
import com.lying.tricksy.client.model.entity.ModelTricksyFox;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class TFModelParts
{
	public static EntityModelLayer TRICKSY_FOX	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "tricksy_fox"), "main");
	
	public static EntityModelLayer SAGE_HAT	= new EntityModelLayer(new Identifier(Reference.ModInfo.MOD_ID, "sage_hat"), "main");
	
	public static void init()
	{
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.TRICKSY_FOX, ModelTricksyFox::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(TFModelParts.SAGE_HAT, ModelSageHat::getTexturedModelData);
	}
}
