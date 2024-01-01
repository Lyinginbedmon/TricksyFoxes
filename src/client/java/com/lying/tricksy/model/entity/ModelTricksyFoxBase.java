package com.lying.tricksy.model.entity;

import com.google.common.base.Function;
import com.lying.tricksy.entity.EntityTricksyFox;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.random.Random;

public abstract class ModelTricksyFoxBase<T extends EntityTricksyFox> extends AnimatedBipedModel<T>
{
	private static final Random rand = Random.create();
	public final ModelPart tailRoot;
	public final ModelPart[] tails;
	
	protected final Entry[] components;
	
	protected ModelTricksyFoxBase(ModelPart root)
	{
		super(root);
		this.tailRoot = this.body.getChild(EntityModelPartNames.TAIL);
		this.tails = new ModelPart[] 
				{
						this.tailRoot.getChild("tail0"),
						this.tailRoot.getChild("tail1"),
						this.tailRoot.getChild("tail2")
				};
		
		components = new Entry[8 + tails.length];
        components[0] = new Entry(obj -> obj.head);
		components[1] = new Entry(obj -> obj.hat);
		components[2] = new Entry(obj -> obj.body);
		components[3] = new Entry(obj -> obj.tailRoot);
		components[4] = new Entry(obj -> obj.leftArm);
		components[5] = new Entry(obj -> obj.rightArm);
		components[6] = new Entry(obj -> obj.leftLeg);
		components[7] = new Entry(obj -> obj.rightLeg);
		for(int i=0; i<tails.length; i++)
			components[8 + i] = tailEntry(i);
	}
	
    public void copyModelStateTo(ModelTricksyFoxBase<T> model)
	{
        super.copyBipedStateTo(model);
        for(Entry entry : components)
        	entry.copy(this, model);
    }
    
    protected void animateTails(int tails, float ageInTicks)
    {
    	rand.setSeed(6881338);
		this.tailRoot.pitch = (float)Math.toRadians(10D);
		
		for(int i=0; i<this.tails.length; i++)
		{
			ModelPart tail = this.tails[i];
			tail.resetTransform();
			tail.visible = i<tails;
			if(!tail.visible)
				break;
			
			int pol = i%2 == 0 ? 1 : -1;
			
			if(tails > 2)
			{
				switch(i)
				{
					case 1:
						tail.yaw = 0F;
						tail.pitch = (float)Math.toRadians(120D);
						break;
					case 2:
						tail.yaw = (float)Math.toRadians(35D) * -1F;
						tail.pitch = (float)Math.toRadians(75D);
						break;
					case 0:
						tail.yaw = (float)Math.toRadians(35D);
						tail.pitch = (float)Math.toRadians(75D);
						break;
					default:
						tail.yaw = 0F;
						tail.pitch = (float)Math.toRadians(90D);
						break;
				}
			}
			else
			{
				tail.pitch = (float)Math.toRadians(90D);
				tail.yaw = (float)Math.toRadians(35D) * pol;
			}
			
			tail.pitch += (Math.sin(ageInTicks / rand.nextBetween(15, 35)) * 0.1F) * pol;
			tail.yaw += (Math.cos(ageInTicks / rand.nextBetween(15, 35)) * 0.1F) * pol;
		}
    }
    
    protected static Entry tailEntry(int i)
    {
    	return new Entry(obj -> obj.tails[i]);
    }
    
    protected static class Entry
    {
    	private final Function<ModelTricksyFoxBase<?>, ModelPart> getter;
    	
    	public Entry(Function<ModelTricksyFoxBase<?>, ModelPart> getterIn)
    	{
    		this.getter = getterIn;
    	}
    	
    	public void copy(ModelTricksyFoxBase<?> from, ModelTricksyFoxBase<?> to)
    	{
    		ModelPart source = getter.apply(from);
    		ModelPart destination = getter.apply(to);
    		destination.copyTransform(source);
    		destination.visible = source.visible;
    	}
    }
}
