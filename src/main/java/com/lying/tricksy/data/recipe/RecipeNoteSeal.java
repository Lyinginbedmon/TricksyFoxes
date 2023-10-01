package com.lying.tricksy.data.recipe;

import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeNoteSeal extends RecipeNote
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_sealing");
	
	public RecipeNoteSeal() { super(ID); }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack candle = ItemStack.EMPTY;
		for(int slot = 0; slot<var1.size(); slot++)
		{
			ItemStack stackInSlot = var1.getStack(slot);
			if(stackInSlot.isEmpty())
				continue;
			else if(canBeSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return false;
			}
			else if(stackInSlot.isIn(ItemTags.CANDLES))
			{
				if(candle.isEmpty())
					candle = stackInSlot;
				else
					return false;
			}
		}
		return !note.isEmpty() && !candle.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack candle = ItemStack.EMPTY;
		for(int slot = 0; slot<var1.size(); slot++)
		{
			ItemStack stackInSlot = var1.getStack(slot);
			if(stackInSlot.isEmpty())
				continue;
			else if(canBeSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else if(stackInSlot.isIn(ItemTags.CANDLES))
			{
				if(candle.isEmpty())
					candle = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
		}
		
		if(!note.isEmpty() && !candle.isEmpty())
		{
			ItemStack copy = note.copy();
			ISealableItem.seal(copy);
			return copy;
		}
		
		return ItemStack.EMPTY;
	}
	
	public static boolean canBeSealed(ItemStack stack)
	{
		return ISealableItem.isSealable(stack) && !ISealableItem.isSealed(stack);
	}
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_SEALING_SERIALIZER; }
}
