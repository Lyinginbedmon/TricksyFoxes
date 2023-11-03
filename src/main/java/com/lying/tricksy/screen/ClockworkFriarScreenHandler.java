package com.lying.tricksy.screen;

import com.lying.tricksy.block.entity.ClockworkFriarBlockEntity;
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

public class ClockworkFriarScreenHandler extends ScreenHandler
{
	private final World world;
	private BlockPos pos;
	private final Inventory friarInventory;
	private final Inventory resultInventory = new SimpleInventory(1);
	
	public ClockworkFriarScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		this(syncId, playerInventory, playerInventory.player.getBlockPos(), new SimpleInventory(9));
	}
	
	public ClockworkFriarScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos posIn, Inventory friarInventoryIn)
	{
		super(TFScreenHandlerTypes.CLOCKWORK_FRIAR_SCREEN_HANDLER, syncId);
		this.world = playerInventory.player.getWorld();
		this.pos = posIn;
		this.friarInventory = friarInventoryIn;
		this.addSlot(new Slot(this.resultInventory, 0, 124, 35)
				{
					public boolean canInsert(ItemStack stack) { return false; }
					public boolean canTakeItems(PlayerEntity playerEntity) { return false; }
				});
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				this.addSlot(new CraftingSlot(this.friarInventory, j + i * 3, 30 + j * 18, 17 + i * 18, this) 
				{
					public int getMaxItemCount(ItemStack stack) { return 1; }
				});
		onContentChanged(this.friarInventory);
		
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		
		for (int i = 0; i < 9; ++i)
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
	}
	
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = (Slot)this.slots.get(slot);
		if(slot2 != null && slot2.hasStack())
		{
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			if (slot >= 10 && slot < 46 ? !this.insertItem(itemStack2, 1, 10, false) && (slot < 37 ? !this.insertItem(itemStack2, 37, 46, false) : !this.insertItem(itemStack2, 10, 37, false)) : !this.insertItem(itemStack2, 10, 46, false))
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
	
	public void onContentChanged(Inventory inv)
	{
		if(world == null || world.isClient())
			return;
		
		BlockEntity ent = world.getBlockEntity(pos);
		if(ent instanceof ClockworkFriarBlockEntity)
		{
			ClockworkFriarBlockEntity friar = (ClockworkFriarBlockEntity)ent;
			friar.findAndSetRecipe();
			resultInventory.setStack(0, friar.getCraftResult());
		}
	}
}
