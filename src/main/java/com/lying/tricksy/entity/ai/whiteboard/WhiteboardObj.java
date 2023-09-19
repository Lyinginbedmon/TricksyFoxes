package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.text.Text;

/** A whiteboard object which stores its values in the same form they are retreived */
public abstract class WhiteboardObj<T> extends WhiteboardObjBase<T, T>
{
	protected WhiteboardObj(TFObjType<T> typeIn, byte dataType)
	{
		super(typeIn, dataType);
	}
	
	protected WhiteboardObj(TFObjType<T> typeIn, byte dataType, T initialValue)
	{
		this(typeIn, dataType);
		value.add(storeValue(initialValue));
	}
	
	protected T storeValue(T val) { return val; }
	
	protected T getValue(T entry) { return entry; }
	
	public static class Bool extends WhiteboardObj<Boolean>
	{
		public Bool()
		{
			super(TFObjType.BOOL, NbtElement.BYTE_TYPE);
		}
		
		public Bool(boolean bool)
		{
			super(TFObjType.BOOL, NbtElement.BYTE_TYPE, bool);
		}
		
		protected NbtElement valueToNbt(Boolean val) { return NbtByte.of(val); }
		protected Boolean valueFromNbt(NbtElement nbt) { return ((NbtByte)nbt).byteValue() > 0; }
		protected Text describeValue(Boolean value) { return Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean."+(value ? "true" : "false")); }
	}
	
	public static class Int extends WhiteboardObj<Integer>
	{
		public Int()
		{
			super(TFObjType.INT, NbtElement.INT_TYPE);
		}
		
		public Int(int intIn)
		{
			super(TFObjType.INT, NbtElement.INT_TYPE, Math.max(0, intIn));
		}
		
		protected NbtElement valueToNbt(Integer val) { return NbtInt.of(val); }
		protected Integer valueFromNbt(NbtElement nbt) { return ((NbtInt)nbt).intValue(); }
		protected Text describeValue(Integer value) { return Text.literal(String.valueOf(value)); }
	}
	
	public static class Item extends WhiteboardObj<ItemStack>
	{
		public Item()
		{
			super(TFObjType.ITEM, NbtElement.COMPOUND_TYPE);
		}
		
		public Item(ItemStack stackIn)
		{
			super(TFObjType.ITEM, NbtElement.COMPOUND_TYPE, stackIn.copy());
		}
		
		protected NbtElement valueToNbt(ItemStack val) { return val.writeNbt(new NbtCompound()); }
		protected ItemStack valueFromNbt(NbtElement nbt) { return ItemStack.fromNbt((NbtCompound)nbt); }
		protected Text describeValue(ItemStack value) { return value.getName(); }
	}
}
