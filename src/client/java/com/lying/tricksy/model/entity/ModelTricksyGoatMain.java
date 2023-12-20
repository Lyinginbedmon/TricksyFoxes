package com.lying.tricksy.model.entity;

import java.util.EnumSet;
import java.util.Set;

import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.renderer.TFAnimations;
import com.lying.tricksy.utility.TricksyUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ModelTricksyGoatMain<T extends EntityTricksyGoat> extends ModelTricksyGoatBase<T>
{
	public ModelTricksyGoatMain(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getMainModel() { return TexturedModelData.of(getModelData(0F), 64, 64); }
	
	public static TexturedModelData getOuterModel()
	{
		Dilation dilation = new Dilation(0.5F);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
				.uv(2, 61).cuboid(-5.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation)
				.uv(2, 61).mirrored().cuboid(2.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation).mirrored(false), ModelTransform.pivot(0.0F, 1.0F, -1.0F));
				head.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(0.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
				head.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-2.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		
		head.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(34, 46).cuboid(-4.0F, -11.2426F, -3.8284F, 5.0F, 7.0F, 10.0F, dilation), ModelTransform.of(1.5F, 6.0F, 1.0F, 0.7854F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 8.0F, 3.5F));
		body.addChild("upper_body", ModelPartBuilder.create().uv(1, 1).cuboid(-8.0F, -6.0F, -2.0F, 9.0F, 11.0F, 16.0F, dilation), ModelTransform.of(3.5F, -4.0F, -0.5F, -1.5708F, 0.0F, 0.0F));
		body.addChild("fur", ModelPartBuilder.create()
				.uv(0, 28).cuboid(-5.5F, -7.0F, -5.5F, 11.0F, 14.0F, 11.0F, dilation.add(0.5F))
				.uv(44, 39).cuboid(2.5F, 9.0F, -5.5F, 3.0F, 4.0F, 3.0F, dilation.add(0.5F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(36, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(2.5F, 18.0F, 4.5F));
		
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(49, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(-3.5F, 18.0F, 4.5F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(35, 2).cuboid(-3.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(-5.0F, 4.0F, 2.0F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(49, 2).cuboid(0.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(4.0F, 4.0F, 2.0F));
		
		return TexturedModelData.of(meshdefinition, 64, 64);
	}
	
	public static ModelData getModelData(float inflation)
	{
		Dilation dilation = new Dilation(inflation);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
			.uv(23, 52).cuboid(0.0F, 2.0F, -8.0F, 0.0F, 7.0F, 5.0F, Dilation.NONE)
			.uv(2, 61).cuboid(-5.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation)
			.uv(2, 61).mirrored().cuboid(2.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation).mirrored(false), ModelTransform.pivot(0.0F, 1.0F, -1.0F));
			head.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(0.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
			head.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-2.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		
		head.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(34, 46).cuboid(-4.0F, -11.2426F, -3.8284F, 5.0F, 7.0F, 10.0F, dilation), ModelTransform.of(1.5F, 6.0F, 1.0F, 0.7854F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 8.0F, 3.5F));
			body.addChild("upper_body", ModelPartBuilder.create().uv(1, 1).cuboid(-8.0F, -6.0F, -2.0F, 9.0F, 11.0F, 16.0F, dilation), ModelTransform.of(3.5F, -4.0F, -0.5F, -1.5708F, 0.0F, 0.0F));
		// Main body fur minus south cube face
		body.addChild("fur_r1", cuboid(ModelPartBuilder.create().uv(0, 28), -5F, -2F, -6F, 11F, 14F, 11, dilation.add(0.5F), EnumSet.complementOf(EnumSet.of(Direction.SOUTH))), ModelTransform.of(0.5F, -5.0F, -0.5F, 0.0F, 3.1416F, 0.0F));
		// Main body fur replacement south cube face
		body.addChild("fur_front_r1", cuboid(ModelPartBuilder.create().uv(0, 39), -5.0F, -15.0F, -10.0F, 11.0F, 14.0F, 0.0F, dilation.add(0.5F), EnumSet.of(Direction.SOUTH)), ModelTransform.pivot(-0.5F, 8.0F, 3.5F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(36, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(2.5F, 18.0F, 4.5F));
		
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(49, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.pivot(-3.5F, 18.0F, 4.5F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(35, 2).cuboid(-3.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(-5.0F, 4.0F, 2.0F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(49, 2).cuboid(0.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.pivot(4.0F, 4.0F, 2.0F));
		
		return meshdefinition;
	}
	
	/** Adds a dilated cuboid with specified faces to the given part builder */
	public static ModelPartBuilder cuboid(ModelPartBuilder builder, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra, Set<Direction> directions)
	{
		builder.cuboidData.add(new ModelCuboidData(null, builder.textureX, builder.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, builder.mirror, 1.0f, 1.0f, directions));
		return builder;
	}
	
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		this.getPart().traverse().forEach(ModelPart::resetTransform);
		this.leftHorn.visible = livingEntity.hasLeftHorn();
		this.rightHorn.visible = livingEntity.hasRightHorn();
		
		boolean isRightHanded;
		boolean bl = livingEntity.getRoll() > 4;
		this.head.yaw = headYaw * ((float)Math.PI / 180);
		this.head.pitch = 
				bl ? -0.7853982f : 
					(this.leaningPitch > 0.0f ? 
						TricksyUtils.lerpAngle(this.leaningPitch, this.head.pitch, headPitch * ((float)Math.PI / 180)) : headPitch * ((float)Math.PI / 180));
		this.head.roll = 0.0f;
		
		this.rightArm.pivotZ = 2.0f;
		this.rightArm.pivotX = -5.0f;
		this.leftArm.pivotZ = 2.0f;
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
		this.updateAnimation(livingEntity.blockadeAnimationState, TFAnimations.BLOCKADE, ageInTicks);
		this.updateAnimation(livingEntity.chargeAnimationState, TFAnimations.CHARGE, ageInTicks);
		
		this.hat.copyTransform(this.head);
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
