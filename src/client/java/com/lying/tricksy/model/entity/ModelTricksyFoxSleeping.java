package com.lying.tricksy.model.entity;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.EntityTricksyFox;

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
public class ModelTricksyFoxSleeping<T extends EntityTricksyFox> extends ModelTricksyFoxBase<T>
{
	public ModelTricksyFoxSleeping(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getMainModel() { return getTexturedModelData(0F); }
	
	public static TexturedModelData getTexturedModelData(float inflation)
	{
		Dilation dilation = new Dilation(inflation);
		ModelData meshDefinition = new ModelData();
		ModelPartData modelRoot = meshDefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		root.addChild("head", ModelPartBuilder.create().uv(1, 5).cuboid(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(15, 1).cuboid(2.0F, -8.0F, -2.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(8, 1).cuboid(-4.0F, -8.0F, -2.0F, 2.0F, 2.0F, 1.0F, dilation)
			.uv(6, 18).cuboid(-2.0F, -2.01F, -6.0F, 4.0F, 2.0F, 3.0F, dilation), ModelTransform.pivot(-6.0F, 23.5F, -1.0F));
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(24, 15).cuboid(-3.0F, -8.999F, -3.5F, 6.0F, 11.0F, 6.0F, dilation), ModelTransform.of(3.5F, 21.0F, 2.0F, 0.0F, 0.0F, -1.5708F));
		
		ModelPartData tail = body.addChild("tail", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, -2.5F, -3.1416F, 0.2618F, -1.5708F));
			tail.addChild("tail0", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail1", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
			tail.addChild("tail2", ModelPartBuilder.create().uv(30, 0).cuboid(-2.0F, -0.5F, -3.0F, 4.0F, 9.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.1736F, 0.9848F, 0.0F, 0.0F, 0.0F));
		
		root.addChild("left_leg", ModelPartBuilder.create().uv(13, 24).cuboid(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(-1.0F, 18.0F, 0.0F));
		root.addChild("right_leg", ModelPartBuilder.create().uv(13, 24).mirrored().cuboid(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(1.0F, 18.0F, 0.0F));
		
		root.addChild("left_arm", ModelPartBuilder.create().uv(4, 24).mirrored().cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation).mirrored(false), ModelTransform.pivot(-3.0F, 10.0F, 0.0F));
		root.addChild("right_arm", ModelPartBuilder.create().uv(4, 24).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, dilation), ModelTransform.pivot(3.0F, 10.0F, 0.0F));
		
		return TexturedModelData.of(meshDefinition, 48, 32);
	}
	
	public void animateModel(T livingEntity, float f, float g, float h)
	{
		super.animateModel(livingEntity, f, g, h);
		this.leftArm.visible = this.rightArm.visible = false;
		this.leftLeg.visible = rightLeg.visible = false;
		this.tailRoot.yaw = MathHelper.cos((float)(f * 0.6662f)) * 1.4f * g;
	}
	
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		this.head.pitch = 0.0f;
		this.head.roll = MathHelper.cos((float)(ageInTicks * 0.027f)) / 22.0f;
		
		animateTails(TricksyComponent.isMobMaster(livingEntity) ? 3 : 2, ageInTicks);
	}
}