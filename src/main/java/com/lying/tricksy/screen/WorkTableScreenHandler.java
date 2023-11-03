package com.lying.tricksy.screen;

import com.lying.tricksy.block.entity.WorkTableBlockEntity;
import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorkTableScreenHandler extends ScreenHandler
{
	private final World world;
	private BlockPos pos;
	
	private Inventory tileInventory;
	
	public WorkTableScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, new SimpleInventory(11));
	}
	
	public WorkTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory workTable)
	{
		super(TFScreenHandlerTypes.WORK_TABLE_SCREEN_HANDLER, syncId);
		this.tileInventory = workTable;
		
		PlayerEntity player = playerInventory.player;
		this.world = player.getWorld();
		this.pos = workTable instanceof WorkTableBlockEntity ? ((WorkTableBlockEntity)workTable).getPos() : player.getBlockPos();
		this.addSlot(new CraftingResultSlot(this.tileInventory, 10, 124, 35, this));
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				this.addSlot(new CraftingSlot(this.tileInventory, j + i * 3, 30 + j * 18, 17 + i * 18, this));
		onContentChanged(this.tileInventory);
		
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		
		for (int i = 0; i < 9; ++i)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
	}
	
	public void onContentChanged(Inventory inventory)
	{
		if(world.isClient())
			return;
		
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile != null && tile instanceof WorkTableBlockEntity)
		{
			WorkTableBlockEntity workTable = (WorkTableBlockEntity)tile;
			workTable.updateRecipeOutput();
			getSlot(0).setStack(workTable.nextRecipeOutput());
		}
	}
	
	public void onResultTaken()
	{
		if(world.isClient())
			return;
		
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile != null && tile instanceof WorkTableBlockEntity)
		{
			((WorkTableBlockEntity)tile).tryCraft(false);
			onContentChanged(this.tileInventory);
		}
	}
	
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = (Slot)this.slots.get(slot);
		if(slot2 != null && slot2.hasStack())
		{
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			if(slot == 0)
			{
				onResultTaken();
				if (!this.insertItem(itemStack2, 10, 46, true)) {
					return ItemStack.EMPTY;
				}
				slot2.onQuickTransfer(itemStack2, itemStack);
			}
			else if (slot >= 10 && slot < 46 ? !this.insertItem(itemStack2, 1, 10, false) && (slot < 37 ? !this.insertItem(itemStack2, 37, 46, false) : !this.insertItem(itemStack2, 10, 37, false)) : !this.insertItem(itemStack2, 10, 46, false))
				return ItemStack.EMPTY;
			
			if(itemStack2.isEmpty())
				slot2.setStack(ItemStack.EMPTY);
			else
				slot2.markDirty();
			
			if(itemStack2.getCount() == itemStack.getCount())
				return ItemStack.EMPTY;
			
			slot2.onTakeItem(player, itemStack2);
			if(slot == 0)
				player.dropItem(itemStack2, false);
		}
		return itemStack;
	}
	
	public boolean canUse(PlayerEntity player) { return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 64D; }
	
	public void setPos(BlockPos posIn) { this.pos = posIn; }

	private static class CraftingResultSlot extends Slot
	{
		private final WorkTableScreenHandler handler;
		
		public CraftingResultSlot(Inventory inventory, int index, int x, int y, WorkTableScreenHandler handlerIn)
		{
			super(inventory, index, x, y);
			this.handler = handlerIn;
		}
		
		public boolean canInsert(ItemStack stack) { return false; }
		
		public void onTakeItem(PlayerEntity player, ItemStack stack)
		{
			super.onTakeItem(player, stack);
			handler.onResultTaken();
		}
	}
}
