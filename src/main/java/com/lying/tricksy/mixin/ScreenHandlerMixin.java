package com.lying.tricksy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.item.ItemSageHat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin
{
	private static final int LEFT_CLICK = 0;
	private static final int RIGHT_CLICK = 1;
	
	@Shadow
	@Final
	public DefaultedList<Slot> slots;
	
	@Shadow
	private ItemStack cursorStack = ItemStack.EMPTY;
	
	@Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
	private void tricksy$cycleNoteType(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, final CallbackInfo ci)
	{
		if(slotIndex < 0 || slotIndex >= slots.size())
			return;
		
		Slot slot = slots.get(slotIndex);
		ItemStack stack = slot.getStack();
		
		// Note handling
		if(button == RIGHT_CLICK && actionType == SlotActionType.QUICK_MOVE)
		{
			if(stack.getItem() == TFItems.NOTE)
			{
				ItemStack converted = convertTo(stack, TFItems.NOTE_POS);
				ItemPrescientNote.setVariable(TFObjType.BLOCK.create(new NbtCompound()), converted);
				
				slot.setStack(converted);
				ci.cancel();
			}
			else if(TFItems.NOTES_CYCLE.contains(stack.getItem()))
			{
				int index = TFItems.NOTES_CYCLE.indexOf(stack.getItem()) + 1;
				Item nextItem = TFItems.NOTES_CYCLE.get(index % TFItems.NOTES_CYCLE.size());
				ItemStack converted = convertTo(stack, nextItem);
				TFObjType<?> type = ((ItemPrescientNote.Typed<?>)nextItem).getType();
				ItemPrescientNote.setVariable(type.create(new NbtCompound()), converted);
				
				slot.setStack(converted);
				ci.cancel();
			}
		}
		
		// Sage Hat handling
		if(button == LEFT_CLICK && actionType != SlotActionType.QUICK_MOVE)
		{
			if(stack.getItem() == TFItems.SAGE_HAT && TFItems.NOTES.contains(cursorStack.getItem()))
			{
				if(ItemSageHat.getSageID(stack) == null || !ItemPrescientNote.isFinalised(cursorStack))
					return;
				
				// More behaviour in ScreenHandlerMixinClient
				
				if(!ISealableItem.isSealed(cursorStack) && !player.isCreative())
					cursorStack.decrement(1);
				
				ci.cancel();
			}
		}
	}
	
	private static ItemStack convertTo(ItemStack start, Item end)
	{
		ItemStack converted = new ItemStack(end, start.getCount());
		if(start.hasCustomName())
			converted.setCustomName(start.getName());
		return converted;
	}
}
