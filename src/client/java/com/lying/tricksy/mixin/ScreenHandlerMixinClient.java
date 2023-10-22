package com.lying.tricksy.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.network.AddGlobalRefPacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixinClient
{
	private static final int LEFT_CLICK = 0;
	
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
		// Sage Hat handling
		if(button == LEFT_CLICK && actionType != SlotActionType.QUICK_MOVE)
		{
			if(stack.getItem() == TFItems.SAGE_HAT && TFItems.NOTES.contains(cursorStack.getItem()))
			{
				if(ItemSageHat.getSageID(stack) == null || !ItemPrescientNote.isFinalised(cursorStack))
					return;
				
				if(player.getWorld().isClient())
				{
					UUID sageID = ItemSageHat.getSageID(stack);
					IWhiteboardObject<?> value = ItemPrescientNote.getVariable(cursorStack);
					WhiteboardRef name = ItemPrescientNote.createReference(cursorStack, BoardType.GLOBAL);
					AddGlobalRefPacket.send(player, sageID, name, value);
				}
				
				if(!ISealableItem.isSealed(cursorStack) && !player.isCreative())
					cursorStack.decrement(1);
				
				ci.cancel();
			}
		}
	}
}
