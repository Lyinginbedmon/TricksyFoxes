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
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean canUse(PlayerEntity player) { return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 64D; }
	
	public void setPos(BlockPos posIn) { this.pos = posIn; }
	
	private static class CraftingSlot extends Slot
	{
		private final WorkTableScreenHandler handler;
		
		public CraftingSlot(Inventory inventory, int index, int x, int y, WorkTableScreenHandler handlerIn)
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
