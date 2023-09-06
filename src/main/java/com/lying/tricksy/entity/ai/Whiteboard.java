package com.lying.tricksy.entity.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

/**
 * Data storage object used by whiteboards
 * @author Lying
 */
public abstract class Whiteboard
{
	private final BoardType type;
	
	/**
	 * TODO Swap from strings to a known set of registries w/ custom entries permitted.<br>
	 * Players should be able to reference automated values in either whiteboard, as well as set ones with custom names
	 */
	protected Map<String, WhiteboardObject> values = new HashMap<>();
	
	protected Whiteboard(BoardType typeIn)
	{
		this.type = typeIn;
	}
	
	/** Recaches and updates the values stored in this whiteboard */
	public final <T extends LivingEntity & ITricksyMob> void update(T tricksy, World world)
	{
		for(WhiteboardObject value : this.values.values())
			value.recacheIfNecessary(tricksy, world);
		
		updateValues(tricksy, world);
	}
	
	protected <T extends LivingEntity & ITricksyMob> void updateValues(T tricksy, World world) { }
	
	/** Retrieves a value from this whiteboard */
	public WhiteboardObject getValue(String nameIn)
	{
		return values.getOrDefault(nameIn, WhiteboardObject.EMPTY);
	}
	
	public final NbtCompound storeToNbt(NbtCompound data)
	{
		data.putString("Type", this.type.asString());
		NbtList dataSet = new NbtList();
		for(Entry<String, WhiteboardObject> entry : values.entrySet())
		{
			NbtCompound nbt = new NbtCompound();
			nbt.putString("Name", entry.getKey());
			nbt.put("Value", entry.getValue().storeToNbt(new NbtCompound()));
			dataSet.add(nbt);
		}
		data.put("Values", dataSet);
		
		writeToNbt(data);
		
		return data;
	}
	
	protected void writeToNbt(NbtCompound data) { }
	
	private static enum BoardType implements StringIdentifiable
	{
		LOCAL,
		GLOBAL;
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static BoardType fromString(String nameIn)
		{
			for(BoardType type : values())
				if(nameIn.equals(type.asString()))
					return type;
			return null;
		}
	}
	
	/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
	public static class LocalWhiteboard extends Whiteboard
	{
		public LocalWhiteboard() { super(BoardType.LOCAL); }
	}
	
	/** A whiteboard containing globally-accessible values set by a tricksy mob's master */
	public static class WorldWhiteboard extends Whiteboard
	{
		public WorldWhiteboard() { super(BoardType.GLOBAL); }
	}
}
