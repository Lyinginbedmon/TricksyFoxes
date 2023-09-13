package com.lying.tricksy.model.layer;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.FoxEntityModel;
import net.minecraft.entity.passive.FoxEntity;

public class ModelFoxPeriapt<T extends FoxEntity> extends FoxEntityModel<T>
{
	public ModelFoxPeriapt(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		
		ModelPartData head = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.NONE);
			head.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create(), ModelTransform.NONE);
			head.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create(), ModelTransform.NONE);
			head.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create(), ModelTransform.NONE);
		
		modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		modelPartData.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3.0f, 3.999f, -3.5f, 6.0f, 11.0f, 6.0f, new Dilation(0.01F))
				.uv(18, 0).cuboid(-1.0F, 3.0F, -5.25F, 2.0F, 2.0F, 2.0F, Dilation.NONE), ModelTransform.of(0f, 16f, -6f, 1.5707964f, 0f, 0f));
		
		body.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 32, 32);
	}
}
