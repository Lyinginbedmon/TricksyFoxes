package com.lying.tricksy.renderer;

import com.lying.tricksy.model.entity.ModelOnryoji;

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
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.625F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.0417F, AnimationHelper.createRotationalVector(-12.5F, -12.5F, 5.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.5F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9583F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.0F, -22.5F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.125F, AnimationHelper.createRotationalVector(-15.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.25F, AnimationHelper.createRotationalVector(-5.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -1.5F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, -27.5F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createTranslationalVector(-0.5F, 0.0F, 0.5F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
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
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, -3.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, -3.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.625F, AnimationHelper.createTranslationalVector(1.0F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9583F, AnimationHelper.createTranslationalVector(1.0F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.1667F, AnimationHelper.createTranslationalVector(0.5F, 0.5F, -3.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createRotationalVector(43.6776F, -32.1562F, 7.9652F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9583F, AnimationHelper.createRotationalVector(43.6776F, -32.1562F, 7.9652F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.1667F, AnimationHelper.createRotationalVector(48.2493F, -23.1456F, -1.8366F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.25F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
	
	public static final Animation BLESS = Animation.Builder.create(1.5F)
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.375F, AnimationHelper.createRotationalVector(20.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.625F, AnimationHelper.createRotationalVector(25.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(20.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createRotationalVector(-52.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.375F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -1.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -1.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createTranslationalVector(0.0F, -0.75F, 3.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createRotationalVector(-73.5297F, 24.0929F, 6.8817F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.5417F, AnimationHelper.createRotationalVector(-70.5068F, 21.7738F, 14.628F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(-73.5297F, 24.0929F, 6.8817F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createRotationalVector(-132.2263F, -46.6018F, 33.4027F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, -1.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createTranslationalVector(0.0F, -1.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createRotationalVector(-73.5297F, -24.0929F, -6.8817F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.5417F, AnimationHelper.createRotationalVector(-69.7451F, -26.4764F, -16.4899F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(-73.5297F, -24.0929F, -6.8817F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createRotationalVector(-132.2263F, 46.6018F, -33.4027F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, -1.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createTranslationalVector(0.0F, -1.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.2917F, AnimationHelper.createRotationalVector(-7.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
	
	public static final Animation HOWL = Animation.Builder.create(4.0833F)
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(30.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(-65.7F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.1667F, AnimationHelper.createRotationalVector(-85.7F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(-65.7F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -1.0F, -3.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createTranslationalVector(0.0F, -1.56F, 5.38F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(0.0F, -1.56F, 5.38F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(12.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(-21.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(-21.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.BODY, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(-41.5123F, 10.0703F, 11.1751F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(-42.7103F, -42.125F, -12.6842F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.1667F, AnimationHelper.createRotationalVector(-40.9757F, -43.7931F, -15.229F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(-42.7103F, -42.125F, -12.6842F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -2.0F, -3.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createTranslationalVector(-0.5F, -1.09F, 2.45F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(-0.5F, -1.09F, 2.45F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(-41.5123F, -10.0703F, -11.1751F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(-42.7103F, 42.125F, 12.6842F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.1667F, AnimationHelper.createRotationalVector(-40.9757F, 43.7931F, 15.229F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(-42.7103F, 42.125F, 12.6842F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, -2.0F, -3.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createTranslationalVector(-0.5F, -1.09F, 2.45F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(-0.5F, -1.09F, 2.45F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(4.9811F, 0.4352F, -4.9811F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(4.9811F, 0.4352F, -4.9811F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.4583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createRotationalVector(-9.9627F, 0.8672F, 4.9244F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(-9.9627F, 0.8672F, 4.9244F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.4583F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.9167F, AnimationHelper.createTranslationalVector(0.0F, 0.5F, -1.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(0.0F, 0.5F, -1.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0833F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
	
	public static final Animation ONRYOJI_IDLE = Animation.Builder.create(2.0F).looping()
		.addBoneAnimation(EntityModelPartNames.ROOT, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.7083F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.UPPER_BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(5.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.UPPER_BODY, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.25F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(17.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.5417F, AnimationHelper.createRotationalVector(17.6F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.0417F, AnimationHelper.createRotationalVector(15.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.625F, AnimationHelper.createRotationalVector(17.6F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(17.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LOWER_BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(-2.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LEGS, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(7.4299F, -1.7082F, 4.6999F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(2.9194F, 0.6591F, -2.9736F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.TAIL, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.0417F, AnimationHelper.createRotationalVector(7.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, -35.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, 35.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
	
	public static final Animation ONRYOJI_OFUDA = Animation.Builder.create(4.25F)
		.addBoneAnimation(ModelOnryoji.UPPER_BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(0.0F, -12.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.25F, AnimationHelper.createRotationalVector(0.0F, -12.5F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.4583F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, -35.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createRotationalVector(-58.4673F, 26.7952F, 6.4285F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.75F, AnimationHelper.createRotationalVector(-93.4673F, 26.7952F, 6.4285F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createRotationalVector(-20.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(-20.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.4583F, AnimationHelper.createRotationalVector(-20.0F, 0.0F, -90.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.75F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, 35.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createRotationalVector(-58.4673F, -26.7952F, -6.4285F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.75F, AnimationHelper.createRotationalVector(-93.4673F, -26.7952F, -6.4285F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 90.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(-20.0F, 0.0F, 90.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.2917F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.75F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation("shoulders", new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createRotationalVector(14.1614F, 37.8095F, 22.3717F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(14.1614F, 37.8095F, 22.3717F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(14.16F, -37.81F, -22.37F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.2917F, AnimationHelper.createRotationalVector(14.16F, -37.81F, -22.37F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(14.1614F, 37.8095F, 22.3717F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.ROOT, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.7083F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.7083F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(4.25F, AnimationHelper.createTranslationalVector(0.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LOWER_BODY, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 7.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 7.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(0.0F, -12.5F, -7.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.2917F, AnimationHelper.createRotationalVector(0.0F, -12.5F, -7.5F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(0.0F, 12.5F, 7.5F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LOWER_BODY, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(1.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 0.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 0.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, 0.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.2917F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, 0.25F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 0.25F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LEGS, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.8333F, AnimationHelper.createRotationalVector(7.4299F, -1.7082F, 4.6999F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 20.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 25.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 20.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -20.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.875F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -25.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.2917F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -20.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 20.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.9583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 25.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(ModelOnryoji.LEGS, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(1.5417F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(1.75F, AnimationHelper.createTranslationalVector(-2.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.2917F, AnimationHelper.createTranslationalVector(-2.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.5F, AnimationHelper.createTranslationalVector(2.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.2917F, AnimationHelper.createTranslationalVector(2.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(3.5F, AnimationHelper.createTranslationalVector(-2.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
	
	public static final Animation ONRYOJI_FOXFIRE = Animation.Builder.create(2.0F)
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, -35.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(-120.6672F, 46.038F, -13.2898F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.875F, AnimationHelper.createRotationalVector(-118.1672F, -46.038F, 13.2898F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.25F, AnimationHelper.createRotationalVector(-85.62F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.4583F, AnimationHelper.createRotationalVector(-85.62F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5417F, AnimationHelper.createRotationalVector(-72.5F, 0.0F, 17.5F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("left_arm", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.875F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, -2.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("right_arm", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, 35.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.7083F, AnimationHelper.createRotationalVector(-49.7574F, 5.7385F, 39.8371F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.0F, AnimationHelper.createRotationalVector(-50.0F, 0.0F, 35.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("root", new Transformation(Transformation.Targets.TRANSLATE, 
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.7083F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("head", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(17.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(-21.7847F, 32.1586F, -4.9731F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.875F, AnimationHelper.createRotationalVector(-21.7847F, -32.1586F, 4.9731F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.25F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.4583F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5417F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("legs", new Transformation(Transformation.Targets.ROTATE, 
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
				new Keyframe(0.8333F, AnimationHelper.createRotationalVector(7.4299F, -1.7082F, 4.6999F), Transformation.Interpolations.LINEAR),
				new Keyframe(1.5417F, AnimationHelper.createRotationalVector(2.9194F, 0.6591F, -2.9736F), Transformation.Interpolations.LINEAR),
				new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.build();
	
	public static final Animation ONRYOJI_FIRE_IDLE = Animation.Builder.create(3.0F).looping()
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.5833F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 5.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.0833F, AnimationHelper.createRotationalVector(6.0996F, 0.0F, 1.9773F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.3333F, AnimationHelper.createRotationalVector(-7.5372F, 0.0F, 0.2529F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.6667F, AnimationHelper.createRotationalVector(-7.5372F, 0.0F, 0.2529F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.9583F, AnimationHelper.createRotationalVector(5.75F, 0.0F, -3.9674F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -7.5F), Transformation.Interpolations.CUBIC),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC)
		))
		.addBoneAnimation(EntityModelPartNames.JAW, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(-10.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.2917F, AnimationHelper.createRotationalVector(-5.29F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-8.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.3333F, AnimationHelper.createRotationalVector(37.5F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.6667F, AnimationHelper.createRotationalVector(37.5F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.3333F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(3.0F, AnimationHelper.createRotationalVector(-10.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC)
		))
		.addBoneAnimation(EntityModelPartNames.ROOT, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(0.7083F, AnimationHelper.createTranslationalVector(-1.0F, 1.06F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.3333F, AnimationHelper.createTranslationalVector(0.0F, 2.0F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.125F, AnimationHelper.createTranslationalVector(1.0F, 1.05F, 0.0F), Transformation.Interpolations.CUBIC),
			new Keyframe(3.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.CUBIC)
		))
		.build();
	
	public static final Animation ONRYOJI_FIRE_IGNITED = Animation.Builder.create(2.0F)
		.addBoneAnimation(EntityModelPartNames.ROOT, new Transformation(Transformation.Targets.TRANSLATE, 
			new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 5.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.ROOT, new Transformation(Transformation.Targets.SCALE, 
			new Keyframe(0.0F, AnimationHelper.createScalingVector(1.0F, 1.0F, 1.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createScalingVector(1.3F, 1.3F, 1.3F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.HEAD, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(0.625F, AnimationHelper.createRotationalVector(-7.9315F, 9.9062F, -1.373F), Transformation.Interpolations.CUBIC),
			new Keyframe(1.375F, AnimationHelper.createRotationalVector(-17.5971F, -12.2063F, 4.608F), Transformation.Interpolations.CUBIC),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(-25.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.addBoneAnimation(EntityModelPartNames.JAW, new Transformation(Transformation.Targets.ROTATE, 
			new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
			new Keyframe(2.0F, AnimationHelper.createRotationalVector(42.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
		))
		.build();
}
