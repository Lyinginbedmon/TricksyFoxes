package com.lying.tricksy.renderer;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

public class TFAnimations
{
	public static final Animation BLOCKADE = Animation.Builder.create(1F)
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0.1667F, AnimationHelper.createRotationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createRotationalVector(0, 0, -10), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_LEG, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0.1667F, AnimationHelper.createTranslationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.375F, AnimationHelper.createTranslationalVector(1, 0, 0), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0.083F, AnimationHelper.createRotationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createRotationalVector(0, 0, 7.5F), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_LEG, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0.083F, AnimationHelper.createTranslationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createTranslationalVector(-1, 0, 0), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createRotationalVector(-60F, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.417F, AnimationHelper.createRotationalVector(-60F, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createRotationalVector(-90F, 0, 0), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.RIGHT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createTranslationalVector(0, -2, -2), Transformation.Interpolations.LINEAR),
				new Keyframe(0.417F, AnimationHelper.createTranslationalVector(0, -2, -2), Transformation.Interpolations.LINEAR),
				new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0, -2, -1), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.ROTATE,
				new Keyframe(0.0F, AnimationHelper.createRotationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createRotationalVector(-60F, 0, 0), Transformation.Interpolations.LINEAR)))
			.addBoneAnimation(EntityModelPartNames.LEFT_ARM, new Transformation(Transformation.Targets.TRANSLATE,
				new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0, 0, 0), Transformation.Interpolations.LINEAR),
				new Keyframe(0.333F, AnimationHelper.createTranslationalVector(0, -2, 2), Transformation.Interpolations.LINEAR)))
			.build();
	// TODO Port animation support to foxes and implement foxfire casting animation
	public static final Animation CHARGE = Animation.Builder.create(0.5F).build();
	public static final Animation FOXFIRE = Animation.Builder.create(1.25F).build();
}
