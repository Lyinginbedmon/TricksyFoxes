package com.lying.tricksy.entity.ai.node.handler;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.text.Text;

public abstract class NodeInput implements INodeIO
{
	public final Type type() { return Type.INPUT; }
	
	/** Accept only values castable as the given type */
	public static Predicate<WhiteboardRef> ofType(TFObjType<?> typeIn, boolean filterAllowed) { return (ref) -> ref.type().castableTo(typeIn) && (filterAllowed || !ref.isFilter()); }
	
	/** Accept any value from anywhere */
	public static Predicate<WhiteboardRef> any() { return Predicates.alwaysTrue(); }
	
	/** Accept any value from the local whiteboard */
	public static Predicate<WhiteboardRef> anyLocal() { return (ref) -> ref.boardType() == BoardType.LOCAL; }
	
	public static INodeIO makeInput(Predicate<WhiteboardRef> predicateIn) { return makeInput(predicateIn, null); }
	
	public static INodeIO makeInput(Predicate<WhiteboardRef> predicateIn, @Nullable IWhiteboardObject<?> defaultVal)
	{
		return new NodeInput()
				{
					public Predicate<WhiteboardRef> predicate() { return predicateIn; }
					
					public Optional<IWhiteboardObject<?>> defaultValue() { return defaultVal == null ? Optional.empty() : Optional.of(defaultVal); }
				};
	}
	
	public static INodeIO makeInput(Predicate<WhiteboardRef> predicateIn, @Nullable IWhiteboardObject<?> defaultVal, @NotNull Text displayName)
	{
		return new NodeInput()
				{
					public Predicate<WhiteboardRef> predicate() { return predicateIn; }
					
					public Optional<IWhiteboardObject<?>> defaultValue() { return defaultVal == null ? Optional.empty() : Optional.of(defaultVal); }
					
					public Text describeValue() { return displayName; }
				};
	}
}
