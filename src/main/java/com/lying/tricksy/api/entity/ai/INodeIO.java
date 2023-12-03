package com.lying.tricksy.api.entity.ai;

import java.util.Optional;
import java.util.function.Predicate;

import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import net.minecraft.text.Text;

public interface INodeIO
{
	public Type type();
	
	/** Returns a predicate defining what whiteboard references are valid for this input */
	public Predicate<WhiteboardRef> predicate();
	
	/** Returns true if this IO can be assigned a static value instead of a whiteboard reference */
	public default boolean allowStatic() { return true; }
	
	/** Returns true if this IO has a default value, usually for inputs */
	public default boolean isOptional() { return defaultValue().isPresent(); }
	
	public default Optional<IWhiteboardObject<?>> defaultValue() { return Optional.empty(); }
	
	public default Text describeValue()
	{
		if(defaultValue().isPresent())
			return defaultValue().get().size() > 0 ? defaultValue().get().describe().get(0) : Text.empty();
		return Text.empty();
	}
	
	public static enum Type
	{
		INPUT,
		OUTPUT;
	}
}
