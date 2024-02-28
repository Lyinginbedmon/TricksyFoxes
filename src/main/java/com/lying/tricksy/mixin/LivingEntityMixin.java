package com.lying.tricksy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.tricksy.utility.ServerBus;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	@Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("RETURN"), cancellable = false)
	public void damage(DamageSource source, float amount, final CallbackInfoReturnable<Boolean> ci)
	{
		ServerBus.AFTER_DAMAGE.invoker().afterDamage((LivingEntity)(Object)this, source, amount);
	}
}
