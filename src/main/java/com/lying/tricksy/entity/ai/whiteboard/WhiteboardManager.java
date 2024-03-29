package com.lying.tricksy.entity.ai.whiteboard;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.init.TFWhiteboards.BoardType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtList;

/** Handler object for passing all available whiteboards into tree nodes */
public class WhiteboardManager<T extends PathAwareEntity & ITricksyMob<?>>
{
	private final Map<BoardType, Whiteboard<?>> whiteboards = new HashMap<>();
	
	public WhiteboardManager(LocalWhiteboard<T> local, GlobalWhiteboard global, Whiteboard<?>... additional)
	{
		whiteboards.put(TFWhiteboards.LOCAL, local);
		whiteboards.put(TFWhiteboards.GLOBAL, global);
		
		for(Whiteboard<?> board : additional)
			if(board != null)
				add(board);
	}
	
	public WhiteboardManager<T> add(Whiteboard<?> board) { whiteboards.put(board.type, board); return this; }
	
	@Nullable
	public Whiteboard<?> get(BoardType type)
	{
		return type == TFWhiteboards.CONSTANT ? ConstantsWhiteboard.CONSTANTS : whiteboards.getOrDefault(type, null);
	}
	
	@SuppressWarnings("unchecked")
	public LocalWhiteboard<T> local() { return (LocalWhiteboard<T>)get(TFWhiteboards.LOCAL); }
	
	public GlobalWhiteboard global() { return (GlobalWhiteboard)get(TFWhiteboards.GLOBAL); }
	
	public OrderWhiteboard order() { return (OrderWhiteboard)get(TFWhiteboards.ORDER); }
	
	public NbtList addToList(NbtList list)
	{
		whiteboards.values().forEach(board -> board.addReferencesToList(list));
		return list;
	}
}
