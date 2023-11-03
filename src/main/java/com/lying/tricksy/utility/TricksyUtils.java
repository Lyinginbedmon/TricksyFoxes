package com.lying.tricksy.utility;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class TricksyUtils
{
	public static int stringComparator(String name1, String name2)
	{
		List<String> names = Lists.newArrayList(name1, name2);
		Collections.sort(names);
		int ind1 = names.indexOf(name1);
		int ind2 = names.indexOf(name2);
		return ind1 > ind2 ? 1 : ind1 < ind2 ? -1 : 0;
	}
	
	public static Text translateDirection(Direction dir)
	{
		return Text.translatable("enum."+Reference.ModInfo.MOD_ID+".direction."+dir.asString());
	}
	
	public static RecipeInputInventory ingredientsFromInventory(Inventory inventory)
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
			input.setStack(i, inventory.getStack(i));
		
		return input;
	}
}
