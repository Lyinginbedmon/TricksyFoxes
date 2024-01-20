package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.renderer.TFAnimations;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class ModelTricksyWolfMain<T extends EntityTricksyWolf> extends ModelTricksyWolfBase<T>
{
	public ModelTricksyWolfMain(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getMainModel() { return TexturedModelData.of(getModelData(0F), 64, 32); }
	
	public static TexturedModelData getOuterModel()
	{
		Dilation dilation = new Dilation(0.5F);
		ModelData meshdefinition = getModelData(0.5F);
		ModelPartData root = meshdefinition.getRoot().getChild(EntityModelPartNames.ROOT);
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 4.0F, dilation)
			.uv(21, 0).cuboid(-1.5F, 0.0F, -6.0F, 3.0F, 3.0F, 4.0F, dilation)
			.uv(37, 4).cuboid(1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(37, 4).cuboid(-3.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, dilation), ModelTransform.pivot(0.0F, -0.5F, -1.5F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);

		ModelPartData torso = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 8).cuboid(-3.0F, -6.0F, -4.0F, 6.0F, 9.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 13.0F, 1.0F));
			torso.addChild("cowl_r1", ModelPartBuilder.create().uv(31, 24).cuboid(-4.0F, -6.0F, -6.5F, 8.0F, 6.0F, 7.0F, dilation.add(0.15F))
				.uv(0, 24).cuboid(-4.0F, -6.0F, -6.5F, 8.0F, 6.0F, 7.0F, dilation), ModelTransform.of(0.0F, -6.0F, 2.0F, 0.2618F, 0.0F, 0.0F));

		ModelPartData tail = torso.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(0.0F, 2.0F, 1.0F, -0.7854F, 0.0F, 0.0F));
			tail.addChild("tail_r1", ModelPartBuilder.create().uv(9, 12).cuboid(-1.0F, -1.0F, -2.0F, 2.0F, 8.0F, 2.0F, dilation), ModelTransform.of(0.0F, -1.0F, 1.0F, 0.7854F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(43, 12).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation)
			.uv(52, 12).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation.add(0.15F)), ModelTransform.pivot(4.5F, 3.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(43, 12).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation).mirrored(false)
			.uv(52, 12).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation.add(0.15F)).mirrored(false), ModelTransform.pivot(-4.5F, 3.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 12).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation), ModelTransform.pivot(1.5F, 16.0F, 0.0F));
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 12).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(-1.5F, 16.0F, 0.0F));
		return TexturedModelData.of(meshdefinition, 64, 64);
	}
	
	public static ModelData getModelData(float inflation)
	{
		Dilation dilation = new Dilation(inflation);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 4.0F, dilation)
			.uv(0, 10).cuboid(-1.5F, 0.0F, -6.0F, 3.0F, 3.0F, 4.0F, dilation)
			.uv(16, 14).cuboid(1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(16, 14).cuboid(-3.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, dilation), ModelTransform.pivot(0.0F, -0.5F, -1.5F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData torso = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 14).cuboid(-3.0F, -6.0F, -4.0F, 6.0F, 9.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 13.0F, 1.0F));
			torso.addChild("upper_r1", ModelPartBuilder.create().uv(21, 0).cuboid(-4.0F, -6.0F, -6.5F, 8.0F, 6.0F, 7.0F, dilation), ModelTransform.of(0.0F, -6.0F, 2.0F, 0.2618F, 0.0F, 0.0F));
		ModelPartData tail = torso.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(0.0F, 2.0F, 1.0F, -0.7854F, 0.0F, 0.0F));
			tail.addChild("tail_r1", ModelPartBuilder.create().uv(9, 18).cuboid(-1.0F, -1.0F, -2.0F, 2.0F, 8.0F, 2.0F, dilation), ModelTransform.of(0.0F, -1.0F, 1.0F, 0.7854F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 18).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation), ModelTransform.pivot(4.5F, 3.0F, 0.0F));
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(-4.5F, 3.0F, 0.0F));
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 18).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation), ModelTransform.pivot(1.5F, 16.0F, 0.0F));
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(-1.5F, 16.0F, 0.0F));
		
		return meshdefinition;
	}
	
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		this.getPart().traverse().forEach(ModelPart::resetTransform);
		
		boolean isRightHanded;
		boolean bl = livingEntity.getRoll() > 4;
		this.head.yaw = headYaw * ((float)Math.PI / 180);
		this.head.pitch = 
				bl ? -0.7853982f : 
					(this.leaningPitch > 0.0f ? 
						TricksyUtils.lerpAngle(this.leaningPitch, this.head.pitch, headPitch * ((float)Math.PI / 180)) : headPitch * ((float)Math.PI / 180));
		this.head.roll = 0.0f;
		
//		this.rightArm.pivotZ = 2.0f;
		this.rightArm.pivotX = -5.0f;
//		this.leftArm.pivotZ = 2.0f;
		this.leftArm.pivotX = 4.0f;
		
		float k = 1.0f;
		if(bl)
		{
			k = (float)livingEntity.getVelocity().lengthSquared();
			k /= 0.2f;
			k *= k * k;
		}
		k = Math.min(k, 1F);
		
		this.rightArm.pitch = MathHelper.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * 2.0f * limbSwingAmount * 0.5f / k;
		this.leftArm.pitch = MathHelper.cos((float)(limbSwing * 0.6662f)) * 2.0f * limbSwingAmount * 0.5f / k;
		this.rightArm.roll = 0.0f;
		this.leftArm.roll = 0.0f;
		this.rightLeg.pitch = MathHelper.cos((float)(limbSwing * 0.6662f)) * 1.4f * limbSwingAmount / k;
		this.leftLeg.pitch = MathHelper.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * 1.4f * limbSwingAmount / k;
		this.rightLeg.yaw = 0.005f;
		this.leftLeg.yaw = -0.005f;
		this.rightLeg.roll = 0.005f;
		this.leftLeg.roll = -0.005f;
		if(this.riding)
		{
			this.rightArm.pitch += -0.62831855f;
			this.leftArm.pitch += -0.62831855f;
			this.rightLeg.pitch = -1.4137167f;
			this.rightLeg.yaw = 0.31415927f;
			this.rightLeg.roll = 0.07853982f;
			this.leftLeg.pitch = -1.4137167f;
			this.leftLeg.yaw = -0.31415927f;
			this.leftLeg.roll = -0.07853982f;
		}
		
		this.rightArm.yaw = 0.0f;
		this.leftArm.yaw = 0.0f;
		boolean bl4 = isRightHanded = livingEntity.getMainArm() == Arm.RIGHT;
		if(livingEntity.isUsingItem())
		{
			bl4 = livingEntity.getActiveHand() == Hand.MAIN_HAND;
			if(bl4 == isRightHanded)
				this.positionRightArm(livingEntity);
			else
				this.positionLeftArm(livingEntity);
		}
		else
		{
			bl4 = isRightHanded ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
			if(isRightHanded != bl4)
			{
				this.positionLeftArm(livingEntity);
				this.positionRightArm(livingEntity);
			}
			else
			{
				this.positionRightArm(livingEntity);
				this.positionLeftArm(livingEntity);
			}
		}
		this.animateArms(livingEntity, ageInTicks);
		
		if(this.rightArmPose != ArmPose.SPYGLASS)
			CrossbowPosing.swingArm(this.rightArm, ageInTicks, 1.0f);
		
		if(this.leftArmPose != ArmPose.SPYGLASS)
			CrossbowPosing.swingArm(this.leftArm, ageInTicks, -1.0f);
		
		if(this.leaningPitch > 0.0f)
		{
			float o;
			float l = limbSwing % 26.0f;
			Arm arm = livingEntity.getMainArm();
			float n = arm == Arm.LEFT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
			float m = arm == Arm.RIGHT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
			if(!livingEntity.isUsingItem())
			{
				if(l < 14.0f)
				{
					this.leftArm.pitch = TricksyUtils.lerpAngle(n, this.leftArm.pitch, 0.0f);
					this.rightArm.pitch = MathHelper.lerp((float)m, (float)this.rightArm.pitch, (float)0.0f);
					this.leftArm.yaw = TricksyUtils.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp((float)m, (float)this.rightArm.yaw, (float)((float)Math.PI));
					this.leftArm.roll = TricksyUtils.lerpAngle(n, this.leftArm.roll, (float)Math.PI + 1.8707964f * this.method_2807(l) / this.method_2807(14.0f));
					this.rightArm.roll = MathHelper.lerp((float)m, (float)this.rightArm.roll, (float)((float)Math.PI - 1.8707964f * this.method_2807(l) / this.method_2807(14.0f)));
				}
				else if(l >= 14.0f && l < 22.0f)
				{
					o = (l - 14.0f) / 8.0f;
					this.leftArm.pitch = TricksyUtils.lerpAngle(n, this.leftArm.pitch, 1.5707964f * o);
					this.rightArm.pitch = MathHelper.lerp((float)m, (float)this.rightArm.pitch, (float)(1.5707964f * o));
					this.leftArm.yaw = TricksyUtils.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp((float)m, (float)this.rightArm.yaw, (float)((float)Math.PI));
					this.leftArm.roll = TricksyUtils.lerpAngle(n, this.leftArm.roll, 5.012389f - 1.8707964f * o);
					this.rightArm.roll = MathHelper.lerp((float)m, (float)this.rightArm.roll, (float)(1.2707963f + 1.8707964f * o));
				}
				else if(l >= 22.0f && l < 26.0f)
				{
					o = (l - 22.0f) / 4.0f;
					this.leftArm.pitch = TricksyUtils.lerpAngle(n, this.leftArm.pitch, 1.5707964f - 1.5707964f * o);
					this.rightArm.pitch = MathHelper.lerp((float)m, (float)this.rightArm.pitch, (float)(1.5707964f - 1.5707964f * o));
					this.leftArm.yaw = TricksyUtils.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
					this.rightArm.yaw = MathHelper.lerp((float)m, (float)this.rightArm.yaw, (float)((float)Math.PI));
					this.leftArm.roll = TricksyUtils.lerpAngle(n, this.leftArm.roll, (float)Math.PI);
					this.rightArm.roll = MathHelper.lerp((float)m, (float)this.rightArm.roll, (float)((float)Math.PI));
				}
			}
			o = 0.3f;
			float p = 0.33333334f;
			this.leftLeg.pitch = MathHelper.lerp((float)this.leaningPitch, (float)this.leftLeg.pitch, (float)(0.3f * MathHelper.cos((float)(limbSwing * p + (float)Math.PI))));
			this.rightLeg.pitch = MathHelper.lerp((float)this.leaningPitch, (float)this.rightLeg.pitch, (float)(0.3f * MathHelper.cos((float)(limbSwing * p))));
		}
		
		this.resetAnimatedParts(livingEntity.getPartsAnimating());
		this.updateAnimation(livingEntity.animations.get(0), TFAnimations.BLESS, ageInTicks);
		
		this.hat.copyTransform(this.head);
		this.tail.pitch += (livingEntity.getHealth() / 20F) * Math.toRadians(45D);
	}
	
	protected float method_2807(float f) { return -65.0f * f + f * f; }
	
	protected void positionRightArm(T entity)
	{
		switch (this.rightArmPose)
		{
			case EMPTY:
				this.rightArm.yaw = 0.0f;
				break;
			case BLOCK:
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - 0.9424779f;
				this.rightArm.yaw = -0.5235988f;
				break;
			case ITEM:
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - 0.31415927f;
				this.rightArm.yaw = 0.0f;
				break;
			case THROW_SPEAR:
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - (float)Math.PI;
				this.rightArm.yaw = 0.0f;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yaw = -0.1f + this.head.yaw;
				this.leftArm.yaw = 0.1f + this.head.yaw + 0.4f;
				this.rightArm.pitch = -1.5707964f + this.head.pitch;
				this.leftArm.pitch = -1.5707964f + this.head.pitch;
				break;
			case CROSSBOW_CHARGE:
				CrossbowPosing.charge(this.rightArm, this.leftArm, entity, true);
				break;
			case CROSSBOW_HOLD:
				CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
				break;
			case BRUSH:
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - 0.62831855f;
				this.rightArm.yaw = 0.0f;
				break;
			case SPYGLASS:
				this.rightArm.pitch = MathHelper.clamp((float)(this.head.pitch - 1.9198622f - (entity.isInSneakingPose() ? 0.2617994f : 0.0f)), (float)-2.4f, (float)3.3f);
				this.rightArm.yaw = this.head.yaw - 0.2617994f;
				break;
			case TOOT_HORN:
				this.rightArm.pitch = MathHelper.clamp((float)this.head.pitch, (float)-1.2f, (float)1.2f) - 1.4835298f;
				this.rightArm.yaw = this.head.yaw - 0.5235988f;
		}
	}
	
	protected void positionLeftArm(T entity)
	{
		switch (this.leftArmPose)
		{
			case EMPTY:
				this.leftArm.yaw = 0.0f;
				break;
			case BLOCK:
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - 0.9424779f;
				this.leftArm.yaw = 0.5235988f;
				break;
			case ITEM:
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - 0.31415927f;
				this.leftArm.yaw = 0.0f;
				break;
			case THROW_SPEAR:
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - (float)Math.PI;
				this.leftArm.yaw = 0.0f;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yaw = -0.1f + this.head.yaw - 0.4f;
				this.leftArm.yaw = 0.1f + this.head.yaw;
				this.rightArm.pitch = -1.5707964f + this.head.pitch;
				this.leftArm.pitch = -1.5707964f + this.head.pitch;
				break;
			case CROSSBOW_CHARGE:
				CrossbowPosing.charge(this.rightArm, this.leftArm, entity, false);
				break;
			case CROSSBOW_HOLD:
				CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, false);
				break;
			case BRUSH:
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - 0.62831855f;
				this.leftArm.yaw = 0.0f;
				break;
			case SPYGLASS:
				this.leftArm.pitch = MathHelper.clamp((float)(this.head.pitch - 1.9198622f - (entity.isInSneakingPose() ? 0.2617994f : 0.0f)), (float)-2.4f, (float)3.3f);
				this.leftArm.yaw = this.head.yaw + 0.2617994f;
				break;
			case TOOT_HORN:
				this.leftArm.pitch = MathHelper.clamp((float)this.head.pitch, (float)-1.2f, (float)1.2f) - 1.4835298f;
				this.leftArm.yaw = this.head.yaw + 0.5235988f;
		}
	}
}
