package com.lying.tricksy.renderer;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class TFAnimations
{
	public static final Animation BLOCKADE = Animation.Builder.create(0.5F)
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(-15.0F, 0.0F, -5.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -10.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createTranslationalVector(0.5F, 0.0F, -2.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(-17.5F, 0.0F, 3.75F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 7.5F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(-29.7873F, -3.742F, -6.5045F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-60.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(-59.7864F, 6.4905F, 3.7661F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-90.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, -2.0F, 2.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, -2.0F, -1.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(-35.5839F, -5.8583F, -8.1186F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-60.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(-59.6187F, 8.6492F, 5.0384F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-60.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createTranslationalVector(1.0F, -1.2F, 1.2F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, -2.0F, 2.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(0.0F, -12.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, -0.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.build();
	public static final Animation CHARGE = Animation.Builder.create(0.5F).looping()
			.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(30.867F, -12.952F, -7.63F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(30.598F, 10.803F, -6.325F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(30.867F, -12.952F, -7.63F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, -1F, -5F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(17.5F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.04F, AnimationHelper.createTranslationalVector(0F, 0.10305F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.08F, AnimationHelper.createTranslationalVector(0F, 0.89695F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.13F, AnimationHelper.createTranslationalVector(0F, 1F, -3F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.17F, AnimationHelper.createTranslationalVector(0F, 0.89695F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createTranslationalVector(0F, 0.10305F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0F, 0F, -3F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.29F, AnimationHelper.createTranslationalVector(0F, 0.10305F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.33F, AnimationHelper.createTranslationalVector(0F, 0.89695F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.38F, AnimationHelper.createTranslationalVector(0F, 1F, -3F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.42F, AnimationHelper.createTranslationalVector(0F, 0.89695F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.46F, AnimationHelper.createTranslationalVector(0F, 0.10305F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0F, 0F, -3F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(-32.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(45F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-32.5F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, -4F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0F, 1F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0F, 0F, -4F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(45F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-32.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(45F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 1F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0F, 0F, -4F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0F, 1F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(-62.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-62.5F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, -1F, -6F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0F, -1F, -1F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0F, -1F, -6F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-62.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, -1F, -1F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0F, -1F, -6F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0F, -1F, -1F), Transformation.Interpolations.LINEAR)))
			.build();
	public static final Animation PRAYER = Animation.Builder.create(1.5F)
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createRotationalVector(22.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createRotationalVector(22.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createTranslationalVector(0F, -0.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createTranslationalVector(0F, -0.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createRotationalVector(40F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createRotationalVector(40F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1F, AnimationHelper.createRotationalVector(-7.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.33F, AnimationHelper.createRotationalVector(-7.5F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1F, AnimationHelper.createRotationalVector(-90F, 7.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.13F, AnimationHelper.createRotationalVector(-90F, -37.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.21F, AnimationHelper.createRotationalVector(-90F, 7.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.33F, AnimationHelper.createRotationalVector(-90F, -37.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1F, AnimationHelper.createTranslationalVector(0F, 0F, -2F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.33F, AnimationHelper.createTranslationalVector(0F, 0F, -2F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createRotationalVector(50F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1F, AnimationHelper.createRotationalVector(-90F, -7.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.13F, AnimationHelper.createRotationalVector(-90F, 37.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.21F, AnimationHelper.createRotationalVector(-90F, -7.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.33F, AnimationHelper.createRotationalVector(-90F, 37.5F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5F, AnimationHelper.createRotationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.21F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.58F, AnimationHelper.createTranslationalVector(0F, -1F, -3F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.79F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1F, AnimationHelper.createTranslationalVector(0F, 0F, -2F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.33F, AnimationHelper.createTranslationalVector(0F, 0F, -2F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0F, 0F, 0F), Transformation.Interpolations.LINEAR)))
			.build();
	public static final Animation FOXFIRE = Animation.Builder.create(1.5F)
			.addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.4583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.625F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.9167F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(1.0417F, AnimationHelper.createRotationalVector(-12.5F, -12.5F, 5.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("head", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.5F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.9583F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.0F, -22.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.125F, AnimationHelper.createRotationalVector(-15.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-5.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -1.5F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_leg", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(-0.5F, 0.0F, 0.5F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.1667F, AnimationHelper.createRotationalVector(-55.6802F, 6.2055F, 4.2203F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(-102.5F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.4583F, AnimationHelper.createRotationalVector(-102.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5417F, AnimationHelper.createRotationalVector(-109.17F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
				new Keyframe(0.625F, AnimationHelper.createRotationalVector(-90.0F, 0.0F, -17.5F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.9583F, AnimationHelper.createRotationalVector(-90.0F, 0.0F, -17.5F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-92.9303F, -15.4376F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-83.8809F, 21.6937F, -1.3265F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, -3.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, -3.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.625F, AnimationHelper.createTranslationalVector(1.0F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.9583F, AnimationHelper.createTranslationalVector(1.0F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.1667F, AnimationHelper.createTranslationalVector(0.5F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createRotationalVector(43.6776F, -32.1562F, 7.9652F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.9583F, AnimationHelper.createRotationalVector(43.6776F, -32.1562F, 7.9652F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.1667F, AnimationHelper.createRotationalVector(48.2493F, -23.1456F, -1.8366F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.25F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR)
			))
			.build();
}
