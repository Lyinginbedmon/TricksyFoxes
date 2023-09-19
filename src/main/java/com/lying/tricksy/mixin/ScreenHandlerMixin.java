package com.lying.tricksy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.item.ItemPrescientNote;

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
	private static final List<Item> NOTE_ITEMS = List.of(TFItems.NOTE_POS, TFItems.NOTE_ENT, TFItems.NOTE_ITEM, TFItems.NOTE_INT, TFItems.NOTE_BOOL);
	private static final int RIGHT_CLICK = 1;
	
	@Shadow
	@Final
	public DefaultedList<Slot> slots;
	
	@Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
	private void cycleNoteType(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, final CallbackInfo ci)
	{
		if(slotIndex < 0 || slotIndex >= slots.size())
			return;
		
		if(button == RIGHT_CLICK && actionType == SlotActionType.QUICK_MOVE)
		{
			Slot slot = slots.get(slotIndex);
			ItemStack stack = slot.getStack();
			if(stack.getItem() == TFItems.NOTE)
			{
				ItemStack converted = convertTo(stack, TFItems.NOTE_POS);
				ItemPrescientNote.setVariable(TFObjType.BLOCK.create(new NbtCompound()), converted);
				
				slot.setStack(converted);
				ci.cancel();
			}
			else if(NOTE_ITEMS.contains(stack.getItem()))
			{
				int index = NOTE_ITEMS.indexOf(stack.getItem()) + 1;
				Item nextItem = NOTE_ITEMS.get(index % NOTE_ITEMS.size());
				ItemStack converted = convertTo(stack, nextItem);
				TFObjType<?> type = ((ItemPrescientNote.Typed<?>)nextItem).getType();
				ItemPrescientNote.setVariable(type.create(new NbtCompound()), converted);
				
				slot.setStack(converted);
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
