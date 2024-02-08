package com.lying.tricksy.model.entity;

import com.lying.tricksy.entity.projectile.EntityOnryojiFire;
import com.lying.tricksy.renderer.TFAnimations;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

public class ModelOnryojiFire<T extends EntityOnryojiFire> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart jaw;
	
	public ModelOnryojiFire(ModelPart data)
	{
		this.root = data.getChild(EntityModelPartNames.ROOT);
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.jaw = head.getChild(EntityModelPartNames.JAW);
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
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
			.uv(0, 0).cuboid(-4.0F, -6.0F, -1.0F, 8.0F, 6.0F, 6.0F, dilation)
			.uv(0, 12).cuboid(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 3.0F, dilation)
			.uv(26, 0).mirrored().cuboid(2.0F, -8.0F, 0.0F, 2.0F, 2.0F, 1.0F, dilation).mirrored(false)
			.uv(26, 0).cuboid(-4.0F, -8.0F, 0.0F, 2.0F, 2.0F, 1.0F, dilation), ModelTransform.pivot(0.0F, 22.0F, 0.0F));
		head.addChild("whiskers2_r1", ModelPartBuilder.create().uv(22, 3).cuboid(-3.0F, -3.0F, 0.0F, 3.0F, 3.0F, 0.0F, Dilation.NONE), ModelTransform.of(-4.0F, 0.0F, 0.5F, 0.0F, 0.6109F, 0.0F));
		head.addChild("whiskers1_r1", ModelPartBuilder.create().uv(22, 3).mirrored().cuboid(0.0F, -3.0F, 0.0F, 3.0F, 3.0F, 0.0F, Dilation.NONE).mirrored(false), ModelTransform.of(4.0F, 0.0F, 0.5F, 0.0F, -0.6109F, 0.0F));
		
		head.addChild(EntityModelPartNames.JAW, ModelPartBuilder.create().uv(16, 12).cuboid(-1.5F, 0.0F, -4.5F, 3.0F, 1.0F, 5.0F, dilation), ModelTransform.of(0.0F, 0.0F, 2.0F, 0.1745F, 0.0F, 0.0F));
		
		return TexturedModelData.of(meshdefinition, 32, 32);
	}
	
	public ModelPart getPart() { return root; }
	
	public void resetAllParts() { getPart().traverse().forEach(ModelPart::resetTransform); }
	
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
	{
		this.resetAllParts();
		this.updateAnimation(entity.animation_idle, TFAnimations.ONRYOJI_FIRE_IDLE, ageInTicks);
		this.updateAnimation(entity.animation_fuse, TFAnimations.ONRYOJI_FIRE_IGNITED, ageInTicks);
	}
	
	public void copyModelStateTo(ModelOnryojiFire<EntityOnryojiFire> maskModel)
	{
		maskModel.root.copyTransform(this.root);
		maskModel.head.copyTransform(this.head);
		maskModel.jaw.copyTransform(this.jaw);
	}
}