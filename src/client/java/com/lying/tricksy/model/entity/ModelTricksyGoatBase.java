package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyGoat;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyGoatBase<T extends EntityTricksyGoat> extends AnimatedBipedModel<T>
{
	protected static final String APPRENTICE_HORNS = "apprentice_horns";
	protected static final String MASTER_HORNS = "master_horns";
	
	protected final ModelPart apprenticeHorns, masterHorns;
	protected final ModelPart apprenticeHornsLeft, apprenticeHornsRight;
	protected final ModelPart masterHornsLeft, masterHornsRight;
	
	protected ModelTricksyGoatBase(ModelPart root)
	{
		super(root);
		
		this.apprenticeHorns = head.getChild(APPRENTICE_HORNS);
		this.apprenticeHornsLeft = apprenticeHorns.getChild(EntityModelPartNames.LEFT_HORN);
		this.apprenticeHornsRight = apprenticeHorns.getChild(EntityModelPartNames.RIGHT_HORN);
		
		this.masterHorns = head.getChild(MASTER_HORNS);
		this.masterHornsLeft = apprenticeHorns.getChild(EntityModelPartNames.LEFT_HORN);
		this.masterHornsRight = apprenticeHorns.getChild(EntityModelPartNames.RIGHT_HORN);
	}
	
    public void copyModelStateTo(ModelTricksyGoatBase<T> model)
	{
        super.copyBipedStateTo(model);
        
        model.apprenticeHornsLeft.visible = this.apprenticeHornsLeft.visible;
        model.apprenticeHornsRight.visible = this.apprenticeHornsRight.visible;
        
        model.masterHornsLeft.visible = this.masterHornsLeft.visible;
        model.masterHornsRight.visible = this.masterHornsRight.visible;
    }
    
    protected void setVisibleHorns(boolean left, boolean right, boolean isMaster)
    {
    	apprenticeHorns.visible = !isMaster;
    	masterHorns.visible = isMaster;
    	
    	apprenticeHornsLeft.visible = masterHornsLeft.visible = left;
    	apprenticeHornsRight.visible = masterHornsRight.visible = right;
    }
}
