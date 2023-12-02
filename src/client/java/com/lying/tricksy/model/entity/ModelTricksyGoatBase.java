package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyGoat;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyGoatBase<T extends EntityTricksyGoat> extends BipedEntityModel<T>
{
	protected final ModelPart leftHorn;
	protected final ModelPart rightHorn;
	
	protected ModelTricksyGoatBase(ModelPart root)
	{
		super(root);
		this.leftHorn = head.getChild(EntityModelPartNames.LEFT_HORN);
		this.rightHorn = head.getChild(EntityModelPartNames.RIGHT_HORN);
	}
	
    public void copyModelStateTo(ModelTricksyGoatBase<T> model)
	{
        super.copyBipedStateTo(model);
        model.leftArm.visible = this.leftArm.visible;
        model.rightArm.visible = this.rightArm.visible;
        model.leftLeg.visible = this.leftLeg.visible;
        model.rightLeg.visible = this.rightLeg.visible;
        model.leftHorn.visible = this.leftHorn.visible;
        model.rightHorn.visible = this.rightHorn.visible;
    }
}
