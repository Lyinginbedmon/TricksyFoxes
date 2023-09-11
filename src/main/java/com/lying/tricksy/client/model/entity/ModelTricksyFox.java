package com.lying.tricksy.client.model.entity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ModelTricksyFox<T extends LivingEntity> extends BipedEntityModel<T>
{
	public final ModelPart tail;
	
	public ModelTricksyFox(ModelPart root)
	{
		super(root);
		this.tail = this.body.getChild(EntityModelPartNames.TAIL);
	}
	
	public static TexturedModelData getMainModel() { return getTexturedModelData(0F); }
	
	public static TexturedModelData getOuterModel() { return getTexturedModelData(0.5F); }
	
    public static TexturedModelData getTexturedModelData(float inflation)
    {
		Dilation dilation = new Dilation(inflation);
		ModelData meshdefinition = new ModelData();
		ModelPartData root = meshdefinition.getRoot();
		
		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(1, 5).cuboid(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(15, 1).cuboid(2.0F, -8.0F, -2.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(8, 1).cuboid(-4.0F, -8.0F, -2.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(6, 18).cuboid(-2.0F, -2.01F, -6.0F, 4.0F, 2.0F, 3.0F, dilation), ModelTransform.pivot(0.0F, 7.0F, -2.0F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(24, 15).cuboid(-3.0F, -8.999F, -3.5F, 6.0F, 11.0F, 6.0F, dilation), ModelTransform.pivot(0.0F, 16.0F, 0.0F));
		
		ModelPartData tail = body.addChild("tail", ModelPartBuilder.create(), ModelTransform.of(0.0F, -1.0F, 2.5F, 0.1745F, 0.0F, 0.0F));
			tail.addChild("tail1", ModelPartBuilder.create().uv(30, 0).mirrored().cuboid(-3.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, -0.6109F, 0.0F));
			tail.addChild("tail0", ModelPartBuilder.create().uv(30, 0).cuboid(-1.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.6109F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(13, 24).cuboid(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(-1.0F, 18.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(13, 24).mirrored().cuboid(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(1.0F, 18.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(4, 24).mirrored().cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(-3.0F, 10.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(4, 24).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(3.0F, 10.0F, 0.0F));
		
		return TexturedModelData.of(meshdefinition, 48, 32);
	}
    
    public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
    {
    	boolean bl = livingEntity.getRoll() > 4;
    	boolean bl2 = livingEntity.isInSwimmingPose();
    	
    	this.head.yaw = headYaw * ((float)Math.PI / 180);
    	this.head.pitch = bl ? -0.7853982f : (this.leaningPitch > 0.0f ? (bl2 ? this.lerpAngle(this.leaningPitch, this.head.pitch, -0.7853982f) : this.lerpAngle(this.leaningPitch, this.head.pitch, headPitch * ((float)Math.PI / 180))) : headPitch * ((float)Math.PI / 180));
    	
    	float k = 1F;
    	if(bl)
    	{
    		k = (float)livingEntity.getVelocity().lengthSquared();
    		k /= 0.2F;
    		k *= k * k;
    	}
    	if(k < 1F)
    		k = 1F;
    	
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
    }
}
