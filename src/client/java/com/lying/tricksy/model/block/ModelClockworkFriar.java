package com.lying.tricksy.model.block;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;

public class ModelClockworkFriar extends Model
{
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	
	private final ModelPart jacket;
	private final ModelPart hat;
	private final ModelPart rightSleeve;
	private final ModelPart leftSleeve;
	
	public ModelClockworkFriar(ModelPart root)
	{
		super(RenderLayer::getEntityCutoutNoCull);
		this.torso = root.getChild(EntityModelPartNames.BODY);
		this.head = torso.getChild(EntityModelPartNames.HEAD);
		this.rightArm = torso.getChild(EntityModelPartNames.RIGHT_ARM);
		this.leftArm = torso.getChild(EntityModelPartNames.LEFT_ARM);
		
		this.jacket = root.getChild(EntityModelPartNames.JACKET);
		this.hat = torso.getChild(EntityModelPartNames.HAT);
		this.rightSleeve = torso.getChild("right_sleeve");
		this.leftSleeve = torso.getChild("left_sleeve");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		
		ModelPartData torso = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, Dilation.NONE), ModelTransform.pivot(0.0F, 13.0F, 7.0F));
		root.addChild(EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(16, 32).cuboid(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.5F)), ModelTransform.pivot(0.0F, 13.0F, 7.0F));
		
		torso.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE), ModelTransform.of(-4.0F, -10.0F, 0.0F, -1.2654F, 0.0F, 0.0F));
		torso.addChild("left_sleeve", ModelPartBuilder.create().uv(0, 32).cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(-4.0F, -10.0F, 0.0F, -1.2654F, 0.0F, 0.0F));
		
		torso.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, Dilation.NONE), ModelTransform.of(4.0F, -10.0F, 0.0F, -1.2654F, 0.0F, 0.0F));
		torso.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(4.0F, -10.0F, 0.0F, -1.2654F, 0.0F, 0.0F));
		
		torso.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation.NONE), ModelTransform.of(0.0F, -12.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
		torso.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)), ModelTransform.of(0.0F, -12.0F, 0.0F, 0.3927F, 0.0F, 0.0F));
		
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay)
	{
		render(matrices, vertices, light, overlay, 1F, 1F, 1F, 1F);
	}
	
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha)
	{
		updateParenting();
		this.torso.render(matrices, vertices, light, overlay);
	}
	
	public void setAngles(boolean isCrafting, int ticksCrafting)
	{
		if(!isCrafting)
		{
			this.leftArm.pitch = this.rightArm.pitch = (float)Math.toRadians(-72.5D);
			this.leftArm.yaw = this.rightArm.yaw = 0F;
			this.head.pitch = (float)Math.toRadians(22.5D);
			this.head.yaw = 0F;
			this.torso.yaw = 0F;
			return;
		}
		
		// Modulus removed to make animation more stuttered
		ticksCrafting -= ticksCrafting % 3;
		
		this.torso.yaw = (float)(Math.sin(-ticksCrafting) * Math.toRadians(5D));
		
		this.head.pitch = (float)Math.toRadians(25D) + (float)(Math.sin(ticksCrafting * 5) * Math.toRadians(1D));
		this.head.yaw = -this.torso.yaw;
		
		this.leftArm.pitch = (float)Math.toRadians(-72.5D) + (float)(Math.sin(ticksCrafting) * Math.toRadians(3D));
		this.leftArm.yaw = (float)Math.toRadians(-25D);
		
		this.rightArm.pitch = (float)Math.toRadians(-72.5D) - (float)(Math.sin(ticksCrafting) * Math.toRadians(3D));
		this.rightArm.yaw = (float)Math.toRadians(25D);
	}
	
	private void updateParenting()
	{
		this.jacket.copyTransform(torso);
		this.hat.copyTransform(head);
		this.leftSleeve.copyTransform(leftArm);
		this.rightSleeve.copyTransform(rightArm);
	}
}