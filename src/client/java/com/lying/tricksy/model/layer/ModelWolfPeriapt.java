package com.lying.tricksy.model.layer;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.entity.passive.WolfEntity;

public class ModelWolfPeriapt<T extends WolfEntity> extends WolfEntityModel<T>
{
	public ModelWolfPeriapt(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData meshdefinition = new ModelData();
		ModelPartData root = meshdefinition.getRoot();
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.5F, -7.0F));
		head.addChild("real_head", ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData torso = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 13.0F, 1.0F));
		torso.addChild("lower_body", ModelPartBuilder.create(), ModelTransform.of(0.0F, -3.0F, -2.0F, 1.5708F, 0.0F, 0.0F));
		
		root.addChild("upper_body", ModelPartBuilder.create()
			.uv(1, 0).cuboid(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, new Dilation(0.01F))
			.uv(0, 0).cuboid(0.0F, -4.0F, -4.25F, 2.0F, 2.0F, 2.0F, Dilation.NONE), ModelTransform.of(-1.0f, 14.0f, -3.0f, 1.5707964f, 0.0f, 0.0f));
		
		ModelPartData tail = root.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(0.0F, -2.0F, 6.0F, -0.7854F, 0.0F, 0.0F));
		tail.addChild("real_tail", ModelPartBuilder.create(), ModelTransform.of(0.0F, -1.0F, 1.0F, 1.5708F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(1.5F, 16.0F, -5.0F));
		root.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-1.5F, 16.0F, -5.0F));
		root.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.pivot(1.5F, 16.0F, 6.0F));
		root.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-1.5F, 16.0F, 6.0F));
		
		return TexturedModelData.of(meshdefinition, 64, 32);
	}
}
