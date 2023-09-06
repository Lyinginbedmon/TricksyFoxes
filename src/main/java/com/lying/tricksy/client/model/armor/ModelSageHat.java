package com.lying.tricksy.client.model.armor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;

public class ModelSageHat<T extends LivingEntity> extends BipedEntityModel<T>
{
	public ModelSageHat(ModelPart root)
	{
		super(root);
	}
	
    public static TexturedModelData getTexturedModelData()
    {
		ModelData meshdefinition = BipedEntityModel.getModelData(Dilation.NONE, 0F);
		ModelPartData partdefinition = meshdefinition.getRoot();
		
		ModelPartData head = partdefinition.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0F, 0F, 0F));
		
		head.addChild("hat_inner1_r1", ModelPartBuilder.create().uv(24, 30).cuboid(-2.0F, -11.0F, -3.0F, 4.0F, 2.0F, 4.0F, Dilation.NONE)
		.uv(0, 30).cuboid(-4.0F, -9.0F, -5.0F, 8.0F, 3.0F, 8.0F, new Dilation(0.7F))
		.uv(0, 15).cuboid(-5.0F, -10.0F, -6.0F, 10.0F, 5.0F, 10.0F, new Dilation(-0.1F))
		.uv(1, 0).cuboid(-5.0F, -11.0F, -6.0F, 10.0F, 6.0F, 9.0F, Dilation.NONE), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));
		
		return TexturedModelData.of(meshdefinition, 64, 64);
	}
}
