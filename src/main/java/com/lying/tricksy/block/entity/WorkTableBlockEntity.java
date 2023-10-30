package com.lying.tricksy.block.entity;

import java.util.List;
import java.util.Optional;

import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.reference.Reference;

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
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WorkTableBlockEntity extends LockableContainerBlockEntity implements SidedInventory
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
	private ItemStack nextOutput = ItemStack.EMPTY;
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
	
	public int getMaxCountPerStack() { return 1; }
	
	public void clear() { inventory.clear(); }
	
	public int size() { return 10; }
	
	public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
	
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
		return stackInSlot.isEmpty() || stackInSlot.getCount() < getMaxCountPerStack() && ItemStack.canCombine(stackInSlot, stack);
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction face) { return slot == 9 && face == Direction.DOWN; }
	
	protected Text getContainerName() { return Text.translatable("block."+Reference.ModInfo.MOD_ID+".work_table"); }
	
	public RecipeInputInventory inputInventory()
	{
		RecipeInputInventory input = new RecipeInputInventory()
		{
			private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
			
			public int size() { return 9; }
			
			public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
			
			public ItemStack getStack(int var1) { return inventory.get(var1); }
			
			public ItemStack removeStack(int var1, int var2) { return Inventories.splitStack(inventory, var1, var2); }
			
			public ItemStack removeStack(int var1) { return Inventories.removeStack(inventory, var1); }
			
			public void setStack(int var1, ItemStack var2) { inventory.set(var1, var2); }
			
			public void markDirty() { }
			
			public boolean canPlayerUse(PlayerEntity var1) { return true; }
			
			public void clear() { inventory.clear(); }
			
			public void provideRecipeInputs(RecipeMatcher var1)
			{
				for(ItemStack stack : inventory)
					var1.addUnenchantedInput(stack);
			}
			
			public int getWidth() { return 3; }
			
			public int getHeight() { return 3; }
			
			public List<ItemStack> getInputStacks() { return inventory; }
		};
		for(int i=0; i<9; i++)
			input.setStack(i, getStack(i));
		
		return input;
	}
	
	public void updateRecipeOutput()
	{
		RecipeInputInventory input = inputInventory();
		nextRecipe = null;
		
		World world = getWorld();
		if(world == null || world.isClient())
		{
			System.out.println("No world to get recipes from!");
			nextOutput = ItemStack.EMPTY;
			return;
		}
		
		Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
		ItemStack result = ItemStack.EMPTY;
		if(optional.isPresent() && (result = optional.get().craft(input, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures()))
		{
			nextOutput = result;
			nextRecipe = optional.get();
			System.out.println("Updated output to: "+result.getName().getString());
		}
		else
		{
			nextOutput = ItemStack.EMPTY;
			System.out.println("No match from provided ingredients");
		}
	}
	
	/** Updates the next recipe output and, if possible, crafts it */
	public void tryCraft()
	{
		System.out.println("Attempting to craft");
		updateRecipeOutput();
		if(canCraft())
		{
			setStack(9, nextOutput);
			DefaultedList<ItemStack> remainders = nextRecipe.getRemainder(inputInventory());
			for(int i=0; i<9; i++)
				setStack(i, remainders.get(i));
		}
		else
			System.out.println("Not able to craft right now");
	}
	
	public boolean canCraft() { return getStack(9).isEmpty() && nextRecipe != null && !nextOutput.isEmpty(); }
	
	protected ScreenHandler createScreenHandler(int var1, PlayerInventory var2)
	{
		return null;
	}
}
