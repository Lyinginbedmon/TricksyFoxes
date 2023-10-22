package com.lying.tricksy.entity.ai.node.handler;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import net.minecraft.text.Text;

public interface INodeInput
{
	public Predicate<WhiteboardRef> predicate();
	
	public Optional<IWhiteboardObject<?>> defaultValue();
	
	public default Text describeValue()
	{
		if(defaultValue().isPresent())
			return defaultValue().get().size() > 0 ? defaultValue().get().describe().get(0) : Text.empty();
		return Text.empty();
	}
	
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
	
	public static INodeInput makeInput(Predicate<WhiteboardRef> predicateIn, @Nullable IWhiteboardObject<?> defaultVal, @NotNull Text displayName)
	{
		return new INodeInput()
				{
					public Predicate<WhiteboardRef> predicate() { return predicateIn; }
					
					public Optional<IWhiteboardObject<?>> defaultValue() { return defaultVal == null ? Optional.empty() : Optional.of(defaultVal); }
					
					public Text describeValue() { return displayName; }
				};
	}
}
