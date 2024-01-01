package com.lying.tricksy.model.entity;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.utility.TricksyUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ModelTricksyFoxCrouching<T extends EntityTricksyFox> extends ModelTricksyFoxBase<T>
{
	public ModelTricksyFoxCrouching(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getMainModel() { return getTexturedModelData(0F); }
	
	public static TexturedModelData getOuterModel() { return getTexturedModelData(0.5F); }
	
	public static TexturedModelData getTexturedModelData(float inflation)
	{
		Dilation dilation = Dilation.NONE;
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(1, 5).cuboid(-4.0F, -6.0F, -6.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(15, 1).cuboid(2.0F, -8.0F, -5.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(8, 1).cuboid(-4.0F, -8.0F, -5.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(6, 18).cuboid(-2.0F, -2.01F, -9.0F, 4.0F, 2.0F, 3.0F, dilation), ModelTransform.pivot(0.0F, 19.0F, -5.5F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 16.0F, 0.0F));
		body.addChild("body_r1", ModelPartBuilder.create().uv(24, 15).cuboid(-4.0F, -5.5F, 6.0F, 6.0F, 11.0F, 6.0F, dilation), ModelTransform.of(1.0F, 8.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		
		ModelPartData tail = body.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(0.0F, -2.0F, 5.5F, 0.1745F, 0.0F, 0.0F));
			tail.addChild("tail0", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail1", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail2", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(13, 24).cuboid(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(-1.0F, 18.0F, 3.5F));
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(13, 24).mirrored().cuboid(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(1.0F, 18.0F, 3.5F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(4, 24).mirrored().cuboid(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(-1.0F, 18.0F, -3.5F));
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(4, 24).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(1.0F, 18.0F, -3.5F));
		
		return TexturedModelData.of(meshdefinition, 48, 32);
	}
	
	public static TexturedModelData getMaskModel()
	{
		Dilation dilation = new Dilation(0.25F);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 19.0F, -5.5F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create()
			.uv(1, 5).cuboid(-4.0F, -6.0F, -6.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(6, 18).cuboid(-2.0F, -2.01F, -9.0F, 4.0F, 2.0F, 3.0F, dilation), ModelTransform.pivot(0.0F, 19.0F, -5.5F));
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 16.0F, 0.0F));
		body.addChild("body_r1", ModelPartBuilder.create(), ModelTransform.of(1.0F, 8.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		
		ModelPartData tail = body.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(0.0F, -2.0F, 5.5F, 0.1745F, 0.0F, 0.0F));
			tail.addChild("tail0", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail1", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail2", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, 18.0F, 3.5F));
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.pivot(1.0F, 18.0F, 3.5F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, 18.0F, -3.5F));
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.pivot(1.0F, 18.0F, -3.5F));
		
		return TexturedModelData.of(meshdefinition, 48, 32);
	}
	
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		boolean bl = livingEntity.getRoll() > 4;
		this.head.yaw = headYaw * ((float)Math.PI / 180);
		this.head.pitch = 
				bl ? -0.7853982f : 
					(this.leaningPitch > 0.0f ? 
						TricksyUtils.lerpAngle(this.leaningPitch, this.head.pitch, headPitch * ((float)Math.PI / 180)) : headPitch * ((float)Math.PI / 180));
		this.head.roll = 0.0f;
		
		animateTails(TricksyComponent.isMobMaster(livingEntity) ? 3 : 2, ageInTicks);
		
		float k = 1.0f;
		if(bl)
		{
			k = (float)livingEntity.getVelocity().lengthSquared();
			k /= 0.2f;
			k *= k * k;
		}
		k = Math.min(k, 1F);
		
		this.rightLeg.pitch = MathHelper.cos((float)(limbSwing * 0.6662f)) * 1.4f * limbSwingAmount / k;
		this.leftLeg.pitch = MathHelper.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * 1.4f * limbSwingAmount / k;
		this.rightLeg.yaw = 0.005f;
		this.leftLeg.yaw = -0.005f;
		this.rightLeg.roll = 0.005f;
		this.leftLeg.roll = -0.005f;
		if(this.riding)
		{
			this.rightLeg.pitch = -1.4137167f;
			this.rightLeg.yaw = 0.31415927f;
			this.rightLeg.roll = 0.07853982f;
			this.leftLeg.pitch = -1.4137167f;
			this.leftLeg.yaw = -0.31415927f;
			this.leftLeg.roll = -0.07853982f;
		}
		
		if(this.leaningPitch > 0.0f)
		{
			float p = 0.33333334f;
			this.leftLeg.pitch = MathHelper.lerp((float)this.leaningPitch, (float)this.leftLeg.pitch, (float)(0.3f * MathHelper.cos((float)(limbSwing * p + (float)Math.PI))));
			this.rightLeg.pitch = MathHelper.lerp((float)this.leaningPitch, (float)this.rightLeg.pitch, (float)(0.3f * MathHelper.cos((float)(limbSwing * p))));
		}
		this.hat.copyTransform(this.head);
		
		copyAngles(this.leftLeg, this.rightArm);
		copyAngles(this.rightLeg, this.leftArm);
	}
	
	protected void copyAngles(ModelPart from, ModelPart to)
	{
		to.pitch = from.pitch;
		to.yaw = from.yaw;
		to.roll = from.roll;
	}
}
