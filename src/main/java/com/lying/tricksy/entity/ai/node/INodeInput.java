package com.lying.tricksy.entity.ai.node;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;

public interface INodeInput
{
	public Predicate<WhiteboardRef> predicate();
	
	public Optional<IWhiteboardObject<?>> defaultValue();
	
	/** Returns true if this input has a default value */
	public default boolean isOptional() { return defaultValue().isPresent(); }
	
	public static INodeInput makeInput(Predicate<WhiteboardRef> predicateIn)
	{
		return makeInput(predicateIn, null);
	}
	
	public static INodeInput makeInput(Predicate<WhiteboardRef> predicateIn, @Nullable IWhiteboardObject<?> defaultVal)
	{
		return new INodeInput()
				{
					public Predicate<WhiteboardRef> predicate() { return predicateIn; }
					
					public Optional<IWhiteboardObject<?>> defaultValue() { return defaultVal == null ? Optional.empty() : Optional.of(defaultVal); }
				};
	}
}
