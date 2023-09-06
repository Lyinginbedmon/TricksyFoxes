package com.lying.tricksy.client.model;

import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class ModelTricksyFox extends BipedEntityModel<EntityTricksyFox>
{
	ModelPart tail;
	
	public ModelTricksyFox(ModelPart root)
	{
		super(root);
		this.tail = this.body.getChild(EntityModelPartNames.TAIL);
	}
	
    public static TexturedModelData getTexturedModelData()
    {
		ModelData model = new ModelData();
		ModelPartData root = model.getRoot();
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -21.0F, -5.0F, 8.0F, 6.0F, 6.0F, Dilation.NONE)
			.uv(29, 0).cuboid(1.0F, -23.0F, -4.0F, 2.0F, 2.0F, 1.0F, Dilation.NONE)
			.uv(22, 0).cuboid(-5.0F, -23.0F, -4.0F, 2.0F, 2.0F, 1.0F, Dilation.NONE)
			.uv(0, 12).cuboid(-3.0F, -17.01F, -8.0F, 4.0F, 2.0F, 3.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(8, 12).cuboid(-4.0F, -16.999F, -3.5F, 6.0F, 11.0F, 6.0F, Dilation.NONE), ModelTransform.NONE);
			body.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(30, 0).cuboid(-3.0F, 2.0F, 6.0F, 4.0F, 9.0F, 5.0F, Dilation.NONE), ModelTransform.rotation(1.57f, 0.0f, 0.0f));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 18).cuboid(-6.0F, -15.0F, -1.0F, 2.0F, 6.0F, 2.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(32, 18).cuboid(2.0F, -15.0F, -1.0F, 2.0F, 6.0F, 2.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 26).cuboid(-4.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, Dilation.NONE), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(32, 26).cuboid(0.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, Dilation.NONE), ModelTransform.NONE);
		
		return TexturedModelData.of(model, 48, 48);
    }
    
    public void setAngles(EntityTricksyFox livingEntity, float f, float g, float h, float i, float j) { }
}
