package com.lying.tricksy.entity.ai.node.handler;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.text.Text;

public interface INodeInput
{
	public default Text describeValue()
	{
		if(defaultValue().isPresent())
			return defaultValue().get().size() > 0 ? defaultValue().get().describe().get(0) : Text.empty();
		return Text.empty();
	}
	
	/** Returns a predicate defining what whiteboard references are valid for this input */
	public Predicate<WhiteboardRef> predicate();
	
	/** Returns true if this input has a default value */
	public default boolean isOptional() { return defaultValue().isPresent(); }
	
	public default Optional<IWhiteboardObject<?>> defaultValue() { return Optional.empty(); }
	
	/** Returns true if this input can be assigned a static value instead of a whiteboard reference */
	public default boolean allowStatic() { return true; }
	
	/** Accept any value from the local whiteboard */
	public static Predicate<WhiteboardRef> anyLocal() { return (ref) -> ref.boardType() == BoardType.LOCAL; }
	
	/** Accept any value from anywhere */
	public static Predicate<WhiteboardRef> any() { return Predicates.alwaysTrue(); }
	
	/** Accept only values castable as the given type */
	public static Predicate<WhiteboardRef> ofType(TFObjType<?> typeIn, boolean filterAllowed) { return (ref) -> ref.type().castableTo(typeIn) && (filterAllowed || !ref.isFilter()); }
	
	public static INodeInput outputRefOnly(TFObjType<?>... typesIn)
	{
		return new INodeInput()
				{
					public Predicate<WhiteboardRef> predicate()
					{
						return (ref) -> 
						{
							for(TFObjType<?> type : typesIn)
							if(type == ref.type())
								return ref.boardType() == BoardType.LOCAL && !ref.uncached();
							return false;
						};
					}
					
					public boolean allowStatic() { return false; }
				};
	}
	
	public static INodeInput makeInput(Predicate<WhiteboardRef> predicateIn) { return makeInput(predicateIn, null); }
	
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
