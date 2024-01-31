package com.lying.tricksy.entity.ai.node.handler;

import java.util.function.Predicate;

import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

public class NodeOutput implements INodeIO
{
	private final Predicate<WhiteboardRef> typePredicate;
	
	public NodeOutput(TFObjType<?>... typesIn)
	{
		typePredicate = (ref) -> 
		{
			for(TFObjType<?> type : typesIn)
				if(type == ref.type())
					return true;
			return false;
		};
	}
	
	public final Type type() { return Type.OUTPUT; }
	
	public final Predicate<WhiteboardRef> predicate() { return ref -> !ref.boardType().isReadOnly() && !ref.uncached() && typePredicate.test(ref); }
	
	/** Output IOs can only receive whiteboard references */
	public final boolean allowStatic() { return false; }
	
	public boolean isOptional() { return false; }
}
