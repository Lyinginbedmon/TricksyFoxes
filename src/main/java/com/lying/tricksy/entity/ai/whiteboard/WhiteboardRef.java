package com.lying.tricksy.entity.ai.whiteboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WhiteboardRef
{
	private final String name;
	private final BoardType onBoard;
	private final TFObjType<?> varType;
	
	/** True if this reference should never be cached */
	private boolean noCache = false;
	
	private Text displayName = null;
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn) { this(nameIn, typeIn, BoardType.CONSTANT); }
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn, BoardType global)
	{
		this.name = nameIn;
		this.displayName = Text.literal(nameIn);
		this.onBoard = global;
		this.varType = typeIn;
	}
	
	public Text displayName() { return displayName == null ? Text.literal(name) : displayName; }
	
	public WhiteboardRef displayName(Text nameIn) { this.displayName = nameIn; return this; }
	
	@Override
	public boolean equals(Object refB) { return refB instanceof WhiteboardRef ? isSameRef((WhiteboardRef)refB) : false; }
	
	public static List<WhiteboardRef> sortByDisplayName(List<WhiteboardRef> set)
	{
		Map<String, WhiteboardRef> valueMap = new HashMap<>();
		set.forEach((val) -> valueMap.put(val.displayName.getString(), val));
		List<String> nameSet = Lists.newArrayList();
		nameSet.addAll(valueMap.keySet());
		Collections.sort(nameSet);
		
		List<WhiteboardRef> sorted = Lists.newArrayList();
		nameSet.forEach((name) -> sorted.add(valueMap.get(name)));
		
		return sorted;
	}
	
	public boolean isSameRef(WhiteboardRef refB) { return refB.name.equals(this.name) && refB.varType == this.varType && refB.onBoard == this.onBoard; }
	
	public String name() { return this.name; }
	
	public BoardType boardType() { return this.onBoard; }
	
	public TFObjType<?> type() { return this.varType; }
	
	/** True if this value should not be stored in cache memory */
	public boolean uncached() { return this.noCache; }
	
	/**
	 * Sets this reference to ignore the whiteboard cache and recalculate each time it is needed.<br>
	 * This is most useful for values that change, like mob health or enemy position
	 */
	public WhiteboardRef noCache() { this.noCache = true; return this; }
	
	public NbtCompound writeToNbt(NbtCompound data)
	{
		data.putString("Name", name);
		data.putString("Board", onBoard.asString());
		data.putString("Type", varType.registryName().toString());
		if(noCache)
			data.putBoolean("Live", noCache);
		if(displayName != null)
			data.putString("DisplayName", Text.Serializer.toJson(displayName));
		return data;
	}
	
	public static WhiteboardRef fromNbt(NbtCompound data)
	{
		String name = data.getString("Name");
		BoardType board = BoardType.fromString(data.getString("Board"));
		TFObjType<?> type = TFObjType.getType(new Identifier(data.getString("Type")));
		WhiteboardRef ref = new WhiteboardRef(name, type, board);
		if(data.contains("Live") && data.getBoolean("Live"))
			ref.noCache();
		if(data.contains("DisplayName", NbtElement.STRING_TYPE))
		{
			String string = data.getString("DisplayName");
			try
			{
				ref.displayName = Text.Serializer.fromJson(string);
			}
			catch(Exception e)
			{
				TricksyFoxes.LOGGER.warn("Failed to parse whiteboard reference custom name {}", (Object)string, (Object)e);
			}
		}
		return ref;
	}
}
