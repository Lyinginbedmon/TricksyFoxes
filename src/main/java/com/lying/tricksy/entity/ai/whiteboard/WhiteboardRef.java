package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WhiteboardRef
{
	private final String name;
	private final BoardType onBoard;
	private final TFObjType<?> varType;
	
	/** True if this reference should never be cached */
	private boolean noCache = false;
	
	private Text displayName;
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn) { this(nameIn, typeIn, BoardType.CONSTANT); }
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn, BoardType global)
	{
		this.name = nameIn;
		this.displayName = Text.literal(nameIn);
		this.onBoard = global;
		this.varType = typeIn;
	}
	
	public Text displayName() { return displayName; }
	
	public WhiteboardRef displayName(Text nameIn) { this.displayName = nameIn; return this; }
	
	public boolean equals(WhiteboardRef refB) { return refB.name.equals(this.name) && refB.varType == this.varType; }
	
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
		return ref;
	}
}
