package com.lying.tricksy.entity.ai;

import com.lying.tricksy.entity.ai.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.WhiteboardObj.ObjectType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class WhiteboardRef
{
	private final String name;
	private final BoardType onBoard;
	private final ObjectType varType;
	
	/** True if this reference should never be cached */
	private boolean recalcNotCache = false;
	
	private Text displayName;
	
	public WhiteboardRef(String nameIn, ObjectType typeIn, BoardType global)
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
	
	public ObjectType type() { return this.varType; }
	
	public boolean shouldRecalc() { return this.recalcNotCache; }
	
	/**
	 * Sets this reference to ignore the whiteboard cache and recalculate each time it is needed.<br>
	 * This is most useful for values that change, like mob health or enemy position
	 */
	public WhiteboardRef setRecalc() { this.recalcNotCache = true; return this; }
	
	public NbtCompound writeToNbt(NbtCompound data)
	{
		data.putString("Name", name);
		data.putString("Board", onBoard.asString());
		data.putString("Type", varType.asString());
		if(recalcNotCache)
			data.putBoolean("Live", recalcNotCache);
		return data;
	}
	
	public static WhiteboardRef fromNbt(NbtCompound data)
	{
		String name = data.getString("Name");
		BoardType board = BoardType.fromString(data.getString("Board"));
		ObjectType type = ObjectType.fromString(data.getString("Type"));
		WhiteboardRef ref = new WhiteboardRef(name, type, board);
		if(data.contains("Live") && data.getBoolean("Live"))
			ref.setRecalc();
		return ref;
	}
}
