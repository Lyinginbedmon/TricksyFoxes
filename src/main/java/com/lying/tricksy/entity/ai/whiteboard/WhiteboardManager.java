package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;

import net.minecraft.entity.mob.PathAwareEntity;

/** Handler object for passing all available whiteboards into tree nodes */
public class WhiteboardManager<T extends PathAwareEntity & ITricksyMob<?>>
{
	private final OrderWhiteboard command;
	private final GlobalWhiteboard global;
	private final LocalWhiteboard<T> local;
	
	public WhiteboardManager(LocalWhiteboard<T> local, GlobalWhiteboard global, OrderWhiteboard command)
	{
		this.command = command;
		this.global = global;
		this.local = local;
	}
	
	public LocalWhiteboard<T> local() { return this.local; }
	
	public GlobalWhiteboard global() { return this.global; }
	
	public OrderWhiteboard command() { return this.command; }
	
	public Whiteboard<?> get(BoardType type)
	{
		switch(type)
		{
			case CONSTANT:	return ConstantsWhiteboard.CONSTANTS;
			case ORDER:	return command();
			case GLOBAL:	return global();
			case LOCAL:	return local();
		}
		return null;
	}
}
