package com.lying.tricksy.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

class CraftingSlot extends Slot
{
	private final ScreenHandler handler;
	
	public CraftingSlot(Inventory inventory, int index, int x, int y, ScreenHandler handlerIn)
	{
		super(inventory, index, x, y);
		this.handler = handlerIn;
	}
	
	public void markDirty()
	{
		super.markDirty();
		handler.onContentChanged(inventory);
	}
}