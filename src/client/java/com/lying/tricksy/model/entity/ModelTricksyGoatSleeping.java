package com.lying.tricksy.model.entity;

import java.util.EnumSet;
import java.util.Set;

import com.lying.tricksy.entity.EntityTricksyGoat;

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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ModelTricksyGoatSleeping<T extends EntityTricksyGoat> extends ModelTricksyGoatBase<T>
{
	public ModelTricksyGoatSleeping(ModelPart root)
	{
		super(root);
	}
	
	public static TexturedModelData getModelData() { return TexturedModelData.of(getModelData(0F), 64, 64); }
	
	public static ModelData getModelData(float inflation)
	{
		Dilation dilation = new Dilation(inflation);
		ModelData meshdefinition = new ModelData();
		ModelPartData modelRoot = meshdefinition.getRoot();
		ModelPartData root = modelRoot.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0, 0, 0));
		
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
			.uv(23, 52).cuboid(0.0F, 2.0F, -8.0F, 0.0F, 7.0F, 5.0F, Dilation.NONE)
			.uv(2, 61).cuboid(-5.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation)
			.uv(2, 61).mirrored().cuboid(2.5F, -5.0F, -2.75F, 3.0F, 2.0F, 1.0F, dilation).mirrored(false), ModelTransform.pivot(0.0F, 7.0F, -1.0F));
			head.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(0.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
			head.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(12, 55).cuboid(-2.49F, -10.0F, -2.5F, 2.0F, 7.0F, 2.0F, dilation), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		
		head.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(34, 46).cuboid(-4.0F, -11.2426F, -3.8284F, 5.0F, 7.0F, 10.0F, dilation), ModelTransform.of(1.5F, 6.0F, 1.0F, 0.7854F, 0.0F, 0.0F));
		
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 14.0F, 3.5F));
			body.addChild("upper_body", ModelPartBuilder.create().uv(1, 1).cuboid(-8.0F, -6.0F, -2.0F, 9.0F, 11.0F, 16.0F, dilation), ModelTransform.of(3.5F, -4.0F, -0.5F, -1.5708F, 0.0F, 0.0F));
		// Main body fur minus south cube face
		body.addChild("fur_r1", cuboid(ModelPartBuilder.create().uv(0, 28), -5F, -2F, -6F, 11F, 14F, 11, dilation.add(0.5F), EnumSet.complementOf(EnumSet.of(Direction.SOUTH))), ModelTransform.of(0.5F, -5.0F, -0.5F, 0.0F, 3.1416F, 0.0F));
		// Main body fur replacement south cube face
		body.addChild("fur_front_r1", cuboid(ModelPartBuilder.create().uv(0, 39), -5.0F, -15.0F, -10.0F, 11.0F, 14.0F, 0.0F, dilation.add(0.5F), EnumSet.of(Direction.SOUTH)), ModelTransform.pivot(-0.5F, 8.0F, 3.5F));
		
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(36, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.of(3.5F, 24.0F, -0.5F, -1.6144F, 0.653F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(49, 29).cuboid(-1.5F, 0.0F, -2.5F, 3.0F, 6.0F, 3.0F, dilation), ModelTransform.of(-4.5F, 24.0F, -0.5F, -1.5708F, -0.7399F, 0.0F));
		
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(35, 2).cuboid(-3.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.of(-5.0F, 11.0F, -1.0F, -0.4363F, -0.2618F, -0.3491F));
		
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(49, 2).cuboid(0.0F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F, dilation), ModelTransform.of(4.0F, 11.0F, -1.0F, -0.4363F, 0.2618F, 0.3491F));
		
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
		this.leftHorn.visible = livingEntity.hasLeftHorn();
		this.rightHorn.visible = livingEntity.hasRightHorn();
		
		this.head.pitch = 0.0f;
		this.head.roll = MathHelper.cos((float)(ageInTicks * 0.027f)) / 22.0f;
	}
}
