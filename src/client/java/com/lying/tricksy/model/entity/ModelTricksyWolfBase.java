package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyWolf;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyWolfBase<T extends EntityTricksyWolf> extends AnimatedBipedModel<T>
{
	protected final ModelPart tail;
	
	protected ModelTricksyWolfBase(ModelPart root)
	{
		super(root);
		this.tail = this.body.getChild(EntityModelPartNames.TAIL);
	}
	
    public void copyModelStateTo(ModelTricksyWolfBase<T> model)
	{
        super.copyBipedStateTo(model);
    }
}
