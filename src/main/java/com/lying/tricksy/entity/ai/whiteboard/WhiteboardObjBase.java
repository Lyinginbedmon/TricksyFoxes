package com.lying.tricksy.entity.ai.whiteboard;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/** A whiteboard object which stores its values in a form different from how they are retreived */
public abstract class WhiteboardObjBase<T, N> implements IWhiteboardObject<T>
{
	/** Generic empty value, representing null in most contexts */
	public static final WhiteboardObj<Object> EMPTY = new WhiteboardObj<Object>(TFObjType.EMPTY, NbtElement.STRING_TYPE, null)
	{
		protected NbtElement valueToNbt(Object val){ return NbtString.of(""); }
		protected Object valueFromNbt(NbtElement nbt) { return null; }
		protected Text describeValue(Object value) { return Text.literal("N/A"); }
	};
	
	private final TFObjType<T> objType;
	protected List<N> value = Lists.newArrayList();
	
	private final byte nbtType;
	
	protected WhiteboardObjBase(TFObjType<T> typeIn, byte storageType)
	{
		this.objType = typeIn;
		this.nbtType = storageType;
	}
	
	protected WhiteboardObjBase(TFObjType<T> typeIn, byte storageType, T initialValue)
	{
		this(typeIn, storageType);
		this.value.add(storeValue(initialValue));
	}
	
	public final TFObjType<T> type() { return this.objType; }
	
	public List<Text> describe()
	{
		List<Text> description = Lists.newArrayList();
		description.add(Text.literal("Values: "+size()));
		if(isList())
			description.add(Text.literal("Contained values:")); // FIXME Should use translated text
		
		value.forEach((val) -> {
			Text entry = describeValue(val);
			description.add(isList() ? Text.literal(" * ").append(entry) : entry);
		});
		return description;
	}
	
	public final void add(WhiteboardObjBase<T,N> object)
	{
		this.value.addAll(object.value);
	}
	
	protected abstract Text describeValue(N value);
	
	protected abstract N storeValue(T val);
	
	protected abstract T getValue(N entry);
	
	protected abstract NbtElement valueToNbt(N val);
	
	protected abstract N valueFromNbt(NbtElement nbt);
	
	/** Returns the top-most value of this object */
	public T get() { return value.isEmpty() ? null : getValue(value.get(0)); }
	
	public final void recacheIfNecessary(World world)
	{
		value.forEach((val) -> recache(val, world));
	}
	
	protected void recache(N value, World world) { }
	
	public final void set(T val)
	{
		this.value.clear();
		this.value.add(storeValue(val));
	}
	
	public final void add(T val)
	{
		this.value.add(storeValue(val));
	}
	
	public final void addToStorage(N entry)
	{
		this.value.add(entry);
	}
	
	/** Moves the top value of this list to the bottom */
	public void cycle()
	{
		if(!isList())
			return;
		
		N first = value.remove(0);
		value.add(first);
	}
	
	public int size() { return this.value.size(); }
	
	public static IWhiteboardObject<?> createFromNbt(NbtCompound compound)
	{
		TFObjType<?> type = TFObjType.getType(new Identifier(compound.getString("Type")));
		if(type != null)
			return type.create(compound);
		return EMPTY;
	}
	
	public final NbtCompound writeToNbt(NbtCompound compound)
	{
		compound.putString("Type", this.objType.registryName().toString());
		
		if(size() > 0)
		{
			NbtList values = new NbtList();
			for(N val : value)
				values.add(valueToNbt(val));
			compound.put("Data", values);
		}
		
		return compound;
	}
	
	public final void readFromNbt(NbtCompound compound)
	{
		value.clear();
		if(compound.contains("Data", NbtElement.LIST_TYPE))
		{
			NbtList values = compound.getList("Data", nbtType);
			for(int i=0; i<values.size(); i++)
				value.add(valueFromNbt(values.get(i)));
		}
	}
}
