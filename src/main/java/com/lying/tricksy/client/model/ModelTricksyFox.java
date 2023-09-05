package com.lying.tricksy.client.model;

import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;

public class ModelTricksyFox extends BipedEntityModel<EntityTricksyFox>
{
	public ModelTricksyFox(ModelPart root)
	{
		super(root);
	}
	
    public static TexturedModelData getTexturedModelData()
    {
        ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0F);
        return TexturedModelData.of(modelData, 32, 32);
    }
}
