package com.lying.tricksy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.tricksy.utility.ClientBus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixinClient
{
	@Inject(method = "onMouseScroll(JDD)V", at = @At("HEAD"), cancellable = true)
	private void tricksy$onMouseScroll(long window, double horizontal, double vertical, final CallbackInfo ci)
	{
		if(ClientBus.MOUSE_SCROLL.invoker().onMouseScroll(MinecraftClient.getInstance(), vertical, horizontal))
			ci.cancel();
	}
}
