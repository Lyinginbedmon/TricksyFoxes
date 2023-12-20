package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyGoat;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyGoatBase<T extends EntityTricksyGoat> extends AnimatedBipedModel<T>
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
        model.leftHorn.visible = this.leftHorn.visible;
        model.rightHorn.visible = this.rightHorn.visible;
    }
}
