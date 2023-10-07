package com.lying.tricksy.entity.ai.node.handler;

import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/** Variant of NodeTickHandler specifically for LeafNode subtypes that deal with inventories */
public interface InventoryHandler extends NodeTickHandler<LeafNode>
{
	public static final WhiteboardRef FILTER = new WhiteboardRef("item_filter", TFObjType.ITEM).displayName(CommonVariables.translate("item_filter"));
	public static final WhiteboardRef FACE = new WhiteboardRef("face", TFObjType.BLOCK).displayName(CommonVariables.translate("face"));
	
	public static ItemStack insertStackIntoTile(ItemStack insertStack, BlockEntity tile, Direction face)
	{
		if(tile == null || !(tile instanceof Inventory))
			return insertStack;
		
		Inventory inv = (Inventory)tile;
		if(tile instanceof SidedInventory)
		{
			SidedInventory sidedInv = (SidedInventory)inv;
			for(int slot : sidedInv.getAvailableSlots(face))
				if(!insertStack.isEmpty() && sidedInv.isValid(slot, insertStack))
					insertStack = insertStackInto(sidedInv, slot, insertStack);
		}
		else
			for(int slot=0; slot < inv.size(); slot++)
				if(!insertStack.isEmpty() && inv.isValid(slot, insertStack))
					insertStack = insertStackInto(inv, slot, insertStack);
		
		return insertStack;
	}
	
	public static ItemStack insertStackInto(Inventory inv, int slot, ItemStack insertStack)
	{
		if(insertStack.isEmpty())
			return insertStack;
		
		ItemStack stackInSlot = inv.getStack(slot);
		if(stackInSlot.isEmpty())
		{
			inv.setStack(slot, insertStack.copy());
			return ItemStack.EMPTY;
		}
		else if(canMergeStacks(stackInSlot, insertStack))
		{
			inv.setStack(slot, mergeStacks(stackInSlot, insertStack));
			return insertStack;
		}
		return insertStack;
	}
	
	/** Extracts one (1) item from the contents of the given slot, or all available slots, merging with the given stack. Returns the extracted stack */
	public static ItemStack extractItemFrom(Inventory inv, ItemStack heldStack)
	{
		if(!heldStack.isEmpty() && heldStack.getCount() == heldStack.getMaxCount())
			return ItemStack.EMPTY;
		
		for(int i=0; i<inv.size(); i++)
		{
			ItemStack extracted = extractItemFrom(inv, i, heldStack);
			if(!extracted.isEmpty())
				return extracted;
		}
		return ItemStack.EMPTY;
	}
	
	/** Extracts one item from the given slot, returning the extracted stack */
	public static ItemStack extractItemFrom(Inventory inv, int slot, ItemStack heldStack)
	{
		if(!heldStack.isEmpty() && heldStack.getCount() == heldStack.getMaxCount())
			return ItemStack.EMPTY;
		
		ItemStack stackInSlot = inv.getStack(slot);
		if(stackInSlot.isEmpty())
			return ItemStack.EMPTY;
		else if(heldStack.isEmpty() || canMergeStacks(heldStack, stackInSlot))
			return stackInSlot.split(1);
		return ItemStack.EMPTY;
	}
	
	public static ItemStack mergeStacks(ItemStack stackA, ItemStack stackB)
	{
		if(stackA.isEmpty())
			return stackB.copyAndEmpty();
		if(canMergeStacks(stackA, stackB))
		{
			int amount = Math.min(stackA.getMaxCount() - stackA.getCount(), stackB.getCount());
			stackA.increment(amount);
			stackB.decrement(amount);
		}
		return stackA;
	}
	
	/** Returns true if the given stacks can be merged together */
	public static boolean canMergeStacks(ItemStack a, ItemStack b) { return a.getCount() < a.getMaxCount() && ItemStack.canCombine(a, b); }
	
	/** Returns true if isMatch returns true for any item in the filter, or if the filter is empty */
	public static boolean matchesFilter(ItemStack stack, IWhiteboardObject<ItemStack> filter)
	{
		if(filter.size() == 0)
			return true;
		
		for(ItemStack option : filter.getAll())
			if(isMatch(stack, option))
				return true;
		return false;
	}
	
	public static boolean isMatch(ItemStack stack, ItemStack pair)
	{
		// XXX Add NBT filtering?
		return ItemStack.areItemsEqual(stack, pair);
	}
}