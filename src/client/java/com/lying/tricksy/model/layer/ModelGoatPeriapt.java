package com.lying.tricksy.model.layer;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.GoatEntityModel;
import net.minecraft.entity.passive.GoatEntity;

public class ModelGoatPeriapt<T extends GoatEntity> extends GoatEntityModel<T>
{
	public ModelGoatPeriapt(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(1.0f, 14.0f, 0.0f));
		modelPartData2.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
		modelPartData2.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
		modelPartData2.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create(), ModelTransform.of(0.0f, -8.0f, -8.0f, 0.9599f, 0.0f, 0.0f));
		
		modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
				.uv(0, 0).cuboid(-5.0f, -18.0f, -8.0f, 11.0f, 14.0f, 11.0f, new Dilation(0.01F))
				.uv(0, 0).cuboid(-0.5F, -12.0F, -10F, 2.0F, 2.0F, 2.0F, Dilation.NONE), ModelTransform.pivot(0.0f, 24.0f, 0.0f));
		
		modelPartData.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.pivot(1.0f, 14.0f, 4.0f));
		modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-3.0f, 14.0f, 4.0f));
		modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(1.0f, 14.0f, -6.0f));
		modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-3.0f, 14.0f, -6.0f));
		return TexturedModelData.of(modelData, 64, 32);
	}
}
