package com.lying.tricksy.model.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.entity.IAnimatedBiped.BipedPart;
import com.lying.tricksy.renderer.TFAnimations;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.random.Random;

public class ModelOnryoji<T extends EntityOnryoji> extends SinglePartEntityModel<T> implements ModelWithHead
{
	public static final String UPPER_BODY = "upper_body";
	public static final String LOWER_BODY = "lower_body";
	public static final String SHOULDERS = "shoulders";
	public static final String LEGS = "legs";
	private static final Random rand = Random.create();
	
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body, upperBody, lowerBody;
	private final ModelPart shoulders, leftArm, rightArm;
	private final ModelPart legs;
	private final ModelPart tailRoot;
	private final ModelPart[] tails = new ModelPart[3];
	
	protected final Map<BipedPart, ModelPart> partsMap = new HashMap<>();
	
	public ModelOnryoji(ModelPart data)
	{
		this.root = data.getChild(EntityModelPartNames.ROOT);
		this.body = root.getChild(EntityModelPartNames.BODY);
		this.upperBody = body.getChild(ModelOnryoji.UPPER_BODY);
		this.lowerBody = body.getChild(ModelOnryoji.LOWER_BODY);
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.shoulders = root.getChild(SHOULDERS);
		this.leftArm = shoulders.getChild(EntityModelPartNames.LEFT_ARM);
		this.rightArm = shoulders.getChild(EntityModelPartNames.RIGHT_ARM);
		this.legs = root.getChild(ModelOnryoji.LEGS);
		this.tailRoot = lowerBody.getChild(EntityModelPartNames.TAIL);
		this.tails[0] = this.tailRoot.getChild("tail0");
		this.tails[1] = this.tailRoot.getChild("tail1");
		this.tails[2] = this.tailRoot.getChild("tail2");
		
		partsMap.put(BipedPart.HEAD, this.head);
		partsMap.put(BipedPart.BODY, this.body);
		partsMap.put(BipedPart.LEFT_ARM, this.leftArm);
		partsMap.put(BipedPart.RIGHT_ARM, this.rightArm);
	}
	
	public static TexturedModelData getMainModel()
	{
		return createBodyLayer(0F);
	}
	
	public static TexturedModelData getMaskModel()
	{
		return createBodyLayer(0.25F);
	}
	
	public static TexturedModelData createBodyLayer(float dil)
	{
		Dilation dilation = new Dilation(dil);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 30.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(28, 0).cuboid(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 6.0F, dilation.add(0.5F))
			.uv(50, 0).cuboid(-2.0F, -2.0F, -7.0F, 4.0F, 2.0F, 3.0F, dilation)
			.uv(0, 0).mirrored().cuboid(2.0F, -8.0F, -3.0F, 2.0F, 2.0F, 1.0F, dilation).mirrored(false)
			.uv(0, 0).cuboid(-4.0F, -8.0F, -3.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(48, 12).cuboid(-2.0F, -13.0F, -2.5F, 4.0F, 7.0F, 4.0F, dilation), ModelTransform.pivot(0.0F, -18.0F, 0.0F));
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -18.0F, 0.0F));
			body.addChild(UPPER_BODY, ModelPartBuilder.create().uv(0, 12).cuboid(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, dilation)
				.uv(0, 35).cuboid(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, dilation.add(0.5F)), ModelTransform.of(0.0F, 5.0F, 1.0F, 0.0873F, 0.0F, 0.0F));

		ModelPartData lower = body.addChild(LOWER_BODY, ModelPartBuilder.create().uv(0, 23).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, dilation)
			.uv(0, 46).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, dilation.add(0.25F)), ModelTransform.pivot(0.0F, 11.0F, 1.0F));

		ModelPartData tails = lower.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 2.0F));
			tails.addChild("tail2", ModelPartBuilder.create().uv(28, 20).cuboid(-2.0F, -0.0711F, -1.8507F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 0.5F, 1.2216F, -0.4971F, 0.1719F));
			tails.addChild("tail1", ModelPartBuilder.create().uv(28, 20).cuboid(-2.0F, -0.0711F, -1.8507F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 0.5F, 1.2216F, 0.4971F, -0.1719F));
			tails.addChild("tail0", ModelPartBuilder.create().uv(28, 20).cuboid(-2.0F, -0.0711F, -1.8507F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 0.5F, 2.0508F, 0.0F, 0.0F));
		
		ModelPartData shoulders = root.addChild(SHOULDERS, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -16.0F, 0.0F));
			shoulders.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(24, 12).mirrored().cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation).mirrored(false)
				.uv(32, 12).mirrored().cuboid(0.0F, -1.0F, -0.75F, 2.0F, 6.0F, 2.0F, dilation.add(0.5F)).mirrored(false), ModelTransform.pivot(3.0F, 0.0F, 0.5F));
			shoulders.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(24, 12).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation)
				.uv(32, 12).cuboid(-2.0F, -1.0F, -0.75F, 2.0F, 6.0F, 2.0F, dilation.add(0.5F)), ModelTransform.pivot(-3.0F, 0.0F, 0.5F));
		
		ModelPartData legs = root.addChild(LEGS, ModelPartBuilder.create(), ModelTransform.of(0.0F, -6.5F, 0.5F, 0.2182F, 0.0F, 0.0F));
		legs.addChild("cube_r1", ModelPartBuilder.create().uv(28, 35).cuboid(-5.0F, -4.0F, -7.0F, 10.0F, 3.0F, 8.0F, dilation), ModelTransform.of(0.0F, 1.5F, 1.5F, -0.0873F, 0.0F, 0.0F));
		
		return TexturedModelData.of(meshdefinition, 64, 64);
	}
	
	public ModelPart getHead() { return this.head; }
	
	public ModelPart getPart() { return this.root; }
	
	public void resetAllParts() { getPart().traverse().forEach(ModelPart::resetTransform); }
	
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		this.resetAllParts();
		this.head.yaw = headYaw * ((float)Math.PI / 180);
		this.head.pitch = headPitch * ((float)Math.PI / 180);
		animateTails(ageInTicks);
		
		this.resetAnimatedParts(livingEntity.getPartsAnimating());
		this.updateAnimation(livingEntity.animations.get(EntityOnryoji.ANIM_IDLE), TFAnimations.ONRYOJI_IDLE, ageInTicks);
//		this.updateAnimation(livingEntity.animations.get(1), TFAnimations.ONRYOJI_BALANCE, ageInTicks);
		this.updateAnimation(livingEntity.animations.get(EntityOnryoji.ANIM_OFUDA), TFAnimations.ONRYOJI_OFUDA, ageInTicks);
		this.updateAnimation(livingEntity.animations.get(EntityOnryoji.ANIM_FOXFIRE), TFAnimations.ONRYOJI_FOXFIRE, ageInTicks);
		this.updateAnimation(livingEntity.animations.get(EntityOnryoji.ANIM_SECLUSION), TFAnimations.ONRYOJI_SECLUSION, ageInTicks);
		this.updateAnimation(livingEntity.animations.get(EntityOnryoji.ANIM_COMMANDERS), TFAnimations.ONRYOJI_COMMANDERS, ageInTicks);
//		this.updateAnimation(livingEntity.animations.get(6), TFAnimations.ONRYOJI_DEATH, ageInTicks);
	}
	
	public void resetAnimatedParts(EnumSet<BipedPart> parts)
	{
		parts.stream().forEach(part -> { if(partsMap.containsKey(part)) partsMap.get(part).resetTransform(); });
	}
    
    protected void animateTails(float ageInTicks)
    {
    	rand.setSeed(6881338);
		this.tailRoot.pitch = (float)Math.toRadians(10D);
		
		for(int i=0; i<this.tails.length; i++)
		{
			ModelPart tail = this.tails[i];
			tail.resetTransform();
			
			int pol = i%2 == 0 ? 1 : -1;
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
			
			tail.pitch += (Math.sin(ageInTicks / rand.nextBetween(15, 35)) * 0.1F) * pol;
			tail.yaw += (Math.cos(ageInTicks / rand.nextBetween(15, 35)) * 0.1F) * pol;
		}
    }
    
    public void copyModelStateTo(ModelOnryoji<?> model)
    {
    	model.root.copyTransform(this.root);
    	model.head.copyTransform(this.head);
    	model.body.copyTransform(this.body);
    	model.upperBody.copyTransform(this.upperBody);
    	model.lowerBody.copyTransform(this.lowerBody);
    	model.shoulders.copyTransform(this.shoulders);
    	model.leftArm.copyTransform(this.leftArm);
    	model.rightArm.copyTransform(this.rightArm);
    	model.legs.copyTransform(this.legs);
    	model.tailRoot.copyTransform(this.tailRoot);
    	for(int i=0; i<3; i++)
    		model.tails[i].copyTransform(tails[i]);
    }
}