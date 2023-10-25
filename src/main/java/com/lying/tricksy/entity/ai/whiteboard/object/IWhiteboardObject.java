package com.lying.tricksy.entity.ai.whiteboard.object;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A polymorphic data object for use with whiteboards
 * @author Lying
 */
public interface IWhiteboardObject<T>
{
	public TFObjType<T> type();
	
	public T get();
	
	public default List<T> getAll()
	{
		List<T> set = Lists.newArrayList();
		for(int i=0; i<size(); i++)
		{
			set.add(get());
			cycle();
		}
		return set;
	}
	
	/** Clears all values from this object and adds the given one */
	public void set(T val);
	
	/** Adds the given value to this object */
	public void add(T val);
	
	public default void tryAdd(IWhiteboardObject<?> val)
	{
		if(val.type().castableTo(type()))
			add(val.as(type()).get());
	}
	
	/** Returns true if this object holds no appreciable value */
	public default boolean isEmpty() { return size() == 0 || type().isEmpty(this); }
	
	/** Converts this object into an object of the given type, or a blank one if not possible */
	public default <N> IWhiteboardObject<N> as(TFObjType<N> type)
	{
		return type().getAs(type, this);
	}
	
	public List<Text> describe();
	
	/** Attempts to recache this object, usually to refresh an entity reference */
	public default void refreshIfNecessary(World world) { }
	
	/** Moves the top value of this list to the bottom */
	public void cycle();
	
	/** Returns true if this object contains more than one value */
	public default boolean isList() { return size() > 1; }
	
	/** Returns the number of values in this object */
	public int size();
	
	public void readFromNbt(NbtCompound data);
	
	public NbtCompound writeToNbt(NbtCompound data);
	
	public default IWhiteboardObject<T> copy()
	{
		return type().create(writeToNbt(new NbtCompound()));
	}

	@Nullable
	static IWhiteboardObject<?> createFromNbt(NbtCompound compound)
	{
		TFObjType<?> type = TFObjType.getType(new Identifier(compound.getString("Type")));
		if(type != null)
			return type.create(compound);
		return null;
	}
}
