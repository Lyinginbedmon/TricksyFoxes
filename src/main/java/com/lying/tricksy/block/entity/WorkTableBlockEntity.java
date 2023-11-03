package com.lying.tricksy.block.entity;

import java.util.Optional;

import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.WorkTableScreenHandler;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WorkTableBlockEntity extends LockableContainerBlockEntity implements SidedInventory
{
	/**
	 * Slots 0-8 = Crafting Inventory
	 * Slot 9 = Crafted output
	 * Slot 10 = Next craft result
	 */
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(11, ItemStack.EMPTY);
	private CraftingRecipe nextRecipe = null;
	
	public WorkTableBlockEntity(BlockPos pos, BlockState state)
	{
		super(TFBlockEntities.WORK_TABLE, pos, state);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, inventory);
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, inventory);
	}
	
	public void clear() { inventory.clear(); }
	
	public int size() { return 11; }
	
	public boolean isEmpty()
	{
		for(int i=0; i<10; i++)
			if(!getStack(i).isEmpty())
				return false;
		return true;
	}
	
	public ItemStack getStack(int slot) { return inventory.get(slot); }
	
	public ItemStack removeStack(int slot, int amount)
	{
		ItemStack stackInSlot = Inventories.splitStack(inventory, slot, amount);
		if(!stackInSlot.isEmpty())
			markDirty();
		return stackInSlot;
	}
	
	public ItemStack removeStack(int slot) { markDirty(); return Inventories.removeStack(inventory, slot); }
	
	public void setStack(int slot, ItemStack stack) { inventory.set(slot, stack); markDirty(); }
	
	public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(this, player); }
	
	public int[] getAvailableSlots(Direction face)
	{
		return face == Direction.DOWN ? new int[] {9} : new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
	}
	
	public boolean canInsert(int slot, ItemStack stack, Direction face)
	{
		if(slot >= 9 || face == Direction.DOWN)
			return false;
		
		ItemStack stackInSlot = getStack(slot);
		// If empty, permit insertion
		if(stackInSlot.isEmpty())
			return true;
		else if(!ItemStack.canCombine(stackInSlot, stack))
			return false;
		
		// Otherwise, ensure specified slot has the highest priority
		int smallestOf = -1;
		for(int i=0; i<9; i++)
		{
			ItemStack itemStack = getStack(i);
			// Prioritise empty slots
			if(itemStack.isEmpty())
			{
				smallestOf = i;
				break;
			}
			// Otherwise prioritise slots of the same item but smaller count
			else if(ItemStack.canCombine(itemStack, stackInSlot) && itemStack.getCount() < stackInSlot.getCount())
			{
				smallestOf = i;
				break;
			}
		}
		
		return smallestOf < 0 || slot == smallestOf;
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction face) { return slot == 9 && face == Direction.DOWN; }
	
	protected Text getContainerName() { return Text.translatable("block."+Reference.ModInfo.MOD_ID+".work_table"); }
	
	public void updateRecipeOutput()
	{
		RecipeInputInventory input = TricksyUtils.ingredientsFromInventory(this);
		nextRecipe = null;
		
		World world = getWorld();
		if(world == null || world.isClient())
		{
			inventory.set(10, ItemStack.EMPTY);
			return;
		}
		
		Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
		ItemStack result = ItemStack.EMPTY;
		if(optional.isPresent() && (result = optional.get().craft(input, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures()))
		{
			inventory.set(10, result);
			nextRecipe = optional.get();
		}
		else
			inventory.set(10, ItemStack.EMPTY);
	}
	
	public ItemStack nextRecipeOutput() { return inventory.get(10).copy(); }
	
	/** Updates the next recipe output and, if possible, crafts it */
	public void tryCraft(boolean setToOutput)
	{
		updateRecipeOutput();
		if(canCraft())
		{
			if(setToOutput)
				setStack(9, getStack(10));
			
			setStack(10, ItemStack.EMPTY);
			DefaultedList<ItemStack> remainders = nextRecipe.getRemainder(TricksyUtils.ingredientsFromInventory(this));
			for(int i=0; i<9; i++)
			{
				ItemStack original = getStack(i);
				ItemStack remainder = remainders.get(i);
				if(remainder.isEmpty())
				{
					original.decrement(1);
					setStack(i, original);
				}
				else
					setStack(i, remainder);
			}
		}
	}
	
	public boolean canCraft() { return getStack(9).isEmpty() && nextRecipe != null && !getStack(10).isEmpty(); }
	
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new WorkTableScreenHandler(syncId, playerInventory, this);
	}
}
