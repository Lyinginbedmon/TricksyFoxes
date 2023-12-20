package com.lying.tricksy.model.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.entity.IAnimatedBiped.BipedPart;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public abstract class AnimatedBipedModel<T extends LivingEntity> extends SinglePartEntityModel<T> implements ModelWithArms, ModelWithHead
{
	protected final ModelPart root;
	protected final ModelPart head, hat;
	protected final ModelPart body;
	protected final ModelPart leftArm, rightArm;
	protected final ModelPart leftLeg, rightLeg;
	
	public ArmPose leftArmPose = ArmPose.EMPTY;
	public ArmPose rightArmPose = ArmPose.EMPTY;
	protected boolean sneaking = false;
	protected float leaningPitch = 0f;
	
	protected final Map<BipedPart, ModelPart> partsMap = new HashMap<>();
	
	protected AnimatedBipedModel(ModelPart data)
	{
		this.root = data.getChild(EntityModelPartNames.ROOT);
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.hat = root.getChild(EntityModelPartNames.HAT);
		this.body = root.getChild(EntityModelPartNames.BODY);
		this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
		this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
		this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
		this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
		
		partsMap.put(BipedPart.HEAD, this.head);
		partsMap.put(BipedPart.BODY, this.body);
		partsMap.put(BipedPart.LEFT_ARM, this.leftArm);
		partsMap.put(BipedPart.RIGHT_ARM, this.rightArm);
		partsMap.put(BipedPart.LEFT_LEG, this.leftLeg);
		partsMap.put(BipedPart.RIGHT_LEG, this.rightLeg);
	}
	
	public ModelPart getPart() { return root; }
	
	public void copyBipedStateTo(AnimatedBipedModel<T> model)
	{
		super.copyStateTo(model);
		model.leftArmPose = this.leftArmPose;
		model.rightArmPose = this.rightArmPose;
		model.sneaking = this.sneaking;
		model.head.copyTransform(this.head);
		model.hat.copyTransform(this.hat);
		model.body.copyTransform(this.body);
		model.rightArm.copyTransform(this.rightArm);
		model.leftArm.copyTransform(this.leftArm);
		model.rightLeg.copyTransform(this.rightLeg);
		model.leftLeg.copyTransform(this.leftLeg);
		model.leftArm.visible = this.leftArm.visible;
		model.rightArm.visible = this.rightArm.visible;
		model.leftLeg.visible = this.leftLeg.visible;
		model.rightLeg.visible = this.rightLeg.visible;
	}
	
	public void animateModel(T livingEntity, float f, float g, float h)
	{
		this.leaningPitch = livingEntity.getLeaningPitch(h);
		super.animateModel(livingEntity, g, g, h);
	}
	
	protected void animateArms(T entity, float animationProgress)
	{
		if (this.handSwingProgress <= 0.0f)
			return;
		
		Arm arm = this.getPreferredArm(entity);
		ModelPart modelPart = this.getArm(arm);
		float f = this.handSwingProgress;
		this.body.yaw = MathHelper.sin((float)(MathHelper.sqrt((float)f) * ((float)Math.PI * 2))) * 0.2f;
		if (arm == Arm.LEFT)
			this.body.yaw *= -1.0f;
		
		this.rightArm.pivotZ = MathHelper.sin((float)this.body.yaw) * 5.0f;
		this.rightArm.pivotX = -MathHelper.cos((float)this.body.yaw) * 5.0f;
		this.leftArm.pivotZ = -MathHelper.sin((float)this.body.yaw) * 5.0f;
		this.leftArm.pivotX = MathHelper.cos((float)this.body.yaw) * 5.0f;
		this.rightArm.yaw += this.body.yaw;
		this.leftArm.yaw += this.body.yaw;
		this.leftArm.pitch += this.body.yaw;
		f = 1.0f - this.handSwingProgress;
		f *= f;
		f *= f;
		f = 1.0f - f;
		float g = MathHelper.sin((float)(f * (float)Math.PI));
		float h = MathHelper.sin((float)(this.handSwingProgress * (float)Math.PI)) * -(this.head.pitch - 0.7f) * 0.75f;
		modelPart.pitch -= g * 1.2f + h;
		modelPart.yaw += this.body.yaw * 2.0f;
		modelPart.roll += MathHelper.sin((float)(this.handSwingProgress * (float)Math.PI)) * -0.4f;
	}
	
	public ModelPart getHead() { return this.head; }
	
	public ModelPart getArm(Arm arm) { return arm == Arm.LEFT ? this.leftArm : this.rightArm; }
	
	public Arm getPreferredArm(T entity)
	{
		Arm arm = entity.getMainArm();
		return ((LivingEntity)entity).preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
	}
	
	public void setArmAngle(Arm arm, MatrixStack matrices) { getArm(arm).rotate(matrices); }
	
	public void resetAllParts() { getPart().traverse().forEach(ModelPart::resetTransform); }
	
	public void resetAnimatedParts(EnumSet<BipedPart> parts)
	{
		parts.stream().forEach(part -> partsMap.get(part).resetTransform());
	}
}
