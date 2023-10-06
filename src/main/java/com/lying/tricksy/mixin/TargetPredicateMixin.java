package com.lying.tricksy.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

@Mixin(TargetPredicate.class)
public class TargetPredicateMixin
{
	@Inject(method = "test", at = @At("INVOKE"), cancellable = true)
	private void tricksy$test(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, final CallbackInfoReturnable<Boolean> ci)
	{
		if((Object)this instanceof ServerFakePlayer)
			ci.setReturnValue(false);
	}
}
