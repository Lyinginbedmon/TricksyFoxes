package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.init.TFObjType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.math.BlockPos;

/** A whiteboard object which stores its values in the same form they are retreived */
public abstract class WhiteboardObj<T> extends WhiteboardObjBase<T, T>
{
	protected WhiteboardObj(TFObjType<T> typeIn, byte dataType, T initialValue)
	{
		super(typeIn, dataType, initialValue);
	}
	
	protected T storeValue(T val) { return val; }
	
	protected T getValue(T entry) { return entry; }
	
	public static class Bool extends WhiteboardObj<Boolean>
	{
		public Bool(boolean bool)
		{
			super(TFObjType.BOOL, NbtElement.BYTE_TYPE, bool);
		}
		
		protected NbtElement valueToNbt(Boolean val) { return NbtByte.of(val); }
		protected Boolean valueFromNbt(NbtElement nbt) { return ((NbtByte)nbt).byteValue() > 0; }
	}
	
	public static class Int extends WhiteboardObj<Integer>
	{
		public Int(int intIn)
		{
			super(TFObjType.INT, NbtElement.INT_TYPE, intIn);
		}
		
		protected NbtElement valueToNbt(Integer val) { return NbtInt.of(val); }
		protected Integer valueFromNbt(NbtElement nbt) { return ((NbtInt)nbt).intValue(); }
	}
	
	public static class Pos extends WhiteboardObj<BlockPos>
	{
		public Pos(BlockPos posIn)
		{
			super(TFObjType.BLOCK, NbtElement.COMPOUND_TYPE, posIn);
		}
		
		protected NbtElement valueToNbt(BlockPos val) { return NbtHelper.fromBlockPos(val); }
		protected BlockPos valueFromNbt(NbtElement nbt) { return NbtHelper.toBlockPos((NbtCompound)nbt); }
	}
	
	public static class Item extends WhiteboardObj<ItemStack>
	{
		public Item(ItemStack stackIn)
		{
			super(TFObjType.ITEM, NbtElement.COMPOUND_TYPE, stackIn.copy());
		}
		
		protected NbtElement valueToNbt(ItemStack val) { return val.writeNbt(new NbtCompound()); }
		protected ItemStack valueFromNbt(NbtElement nbt) { return ItemStack.fromNbt((NbtCompound)nbt); }
	}
}
