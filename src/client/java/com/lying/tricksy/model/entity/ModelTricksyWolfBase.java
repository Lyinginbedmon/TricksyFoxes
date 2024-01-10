package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyWolf;

import net.minecraft.client.model.ModelPart;

public abstract class ModelTricksyWolfBase<T extends EntityTricksyWolf> extends AnimatedBipedModel<T>
{
	protected ModelTricksyWolfBase(ModelPart root)
	{
		super(root);
	}
	
    public void copyModelStateTo(ModelTricksyWolfBase<T> model)
	{
        super.copyBipedStateTo(model);
    }
}
