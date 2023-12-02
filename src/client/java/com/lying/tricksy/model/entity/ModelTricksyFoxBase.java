package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyFoxBase<T extends EntityTricksyFox> extends BipedEntityModel<T>
{
	public final ModelPart tailRoot;
	public final ModelPart tail0, tail1;
	
	protected ModelTricksyFoxBase(ModelPart root)
	{
		super(root);
		this.tailRoot = this.body.getChild(EntityModelPartNames.TAIL);
		this.tail0 = this.tailRoot.getChild("tail0");
		this.tail1 = this.tailRoot.getChild("tail1");
	}
	
    public void copyModelStateTo(ModelTricksyFoxBase<T> model)
	{
        super.copyBipedStateTo(model);
        model.tailRoot.copyTransform(this.tailRoot);
        model.tail0.copyTransform(this.tail0);
        model.tail1.copyTransform(this.tail1);
        model.leftArm.visible = this.leftArm.visible;
        model.rightArm.visible = this.rightArm.visible;
        model.leftLeg.visible = this.leftLeg.visible;
        model.rightLeg.visible = this.rightLeg.visible;
    }
}
