package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public abstract class ModelTricksyFoxBase<T extends EntityTricksyFox> extends AnimatedBipedModel<T>
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
        
        Entry[] components = new Entry[] 
        		{
        			new Entry(this.head, model.head),
        			new Entry(this.hat, model.hat),
        			new Entry(this.body, model.body),
        			new Entry(this.tailRoot, model.tailRoot),
        			new Entry(this.tail0, model.tail0),
        			new Entry(this.tail1, model.tail1),
        			new Entry(this.leftArm, model.leftArm),
        			new Entry(this.rightArm, model.rightArm),
        			new Entry(this.leftLeg, model.leftLeg),
        			new Entry(this.rightLeg, model.rightLeg)
        		};
        
        for(Entry entry : components)
        {
        	entry.destination.copyTransform(entry.source);
        	entry.destination.visible = entry.source.visible;
        }
    }
    
    private static class Entry
    {
    	private final ModelPart source;
    	private final ModelPart destination;
    	
    	public Entry(ModelPart sourceIn, ModelPart destinationIn)
    	{
    		this.source = sourceIn;
    		this.destination = destinationIn;
    	}
    }
}
