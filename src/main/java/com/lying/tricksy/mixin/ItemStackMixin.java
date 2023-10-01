package com.lying.tricksy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.tricksy.item.ISealableItem;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin 
{
	@Inject(method = "getName", at = @At("RETURN"), cancellable = true)
	public void tricksy$getName(final CallbackInfoReturnable<Text> ci)
	{
		ItemStack stack = (ItemStack)(Object)this;
		if(stack.getItem() instanceof ISealableItem && ISealableItem.isSealed(stack))
			ci.setReturnValue(ISealableItem.getSealedName(ci.getReturnValue()));
	}
}
