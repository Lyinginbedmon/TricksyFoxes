package com.lying.tricksy.entity.ai.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicates;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;

public interface NodeTickHandler<M extends TreeNode<?>>
{
	/** Accept only values of the given type */
	public static Predicate<WhiteboardRef> ofType(TFObjType<?> typeIn) { return (ref) -> ref.type().castableTo(typeIn); }
	/** Accept any value from anywhere */
	public static Predicate<WhiteboardRef> any() { return Predicates.alwaysTrue(); }
	/** Accept any value from the local whiteboard */
	public static Predicate<WhiteboardRef> anyLocal() { return (ref) -> ref.boardType() == BoardType.LOCAL; }
	
	/** Returns a map containing all necessary variables of this behaviour and predicates defining their needs */
	@NotNull
	public default Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet(){ return new HashMap<>(); }
	
	public default boolean variablesSufficient(M parent) { return !noVariableMissing(parent); }
	
	/**
	 * Returns true if any variable in {@link variableSet} is unassigned in the given parent node<br>
	 * Note: This does NOT account for whether the target value is empty or not.
	 */
	public default boolean noVariableMissing(M parent)
	{
		if(variableSet().isEmpty())
			return false;
		
		for(Entry<WhiteboardRef, Predicate<WhiteboardRef>> entry : variableSet().entrySet())
			if(!parent.variableAssigned(entry.getKey()) || !entry.getValue().test(parent.variable(entry.getKey())))
				return true;
		
		return false;
	}
	
	/** Performs a single tick of this node */
	@NotNull
	public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, M parent);
}
