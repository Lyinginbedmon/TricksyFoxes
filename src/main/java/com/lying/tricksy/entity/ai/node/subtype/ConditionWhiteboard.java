package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class ConditionWhiteboard implements ISubtypeGroup<ConditionNode>
{
	public static final Identifier VARIANT_VALUE_TRUE = ISubtypeGroup.variant("value_true");
	public static final Identifier VARIANT_VALUE_EXISTS = ISubtypeGroup.variant("value_exists");
	public static final Identifier VARIANT_VALUE_EQUALS = ISubtypeGroup.variant("value_equals");
	
	public void addActions(Collection<NodeSubType<ConditionNode>> set)
	{
		/** Returns SUCCESS if the boolean value of the given object is TRUE */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_TRUE, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR, INodeInput.makeInput(NodeTickHandler.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				return getOrDefault(CommonVariables.VAR, parent, local, global).as(TFObjType.BOOL).get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		/** Returns SUCCESS if the given object is not considered empty (this differs from VALUE_TRUE for several data types) */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_EXISTS, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR, INodeInput.makeInput(NodeTickHandler.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				return getOrDefault(CommonVariables.VAR, parent, local, global).isEmpty() ? Result.FAILURE : Result.SUCCESS;
			}
		}));
		/** Returns SUCCESS if the given objects match */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_EQUALS, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_A, INodeInput.makeInput(NodeTickHandler.any()), 
						CommonVariables.VAR_B, INodeInput.makeInput(NodeTickHandler.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				IWhiteboardObject<?> objA = getOrDefault(CommonVariables.VAR_A, parent, local, global);
				IWhiteboardObject<?> objB = getOrDefault(CommonVariables.VAR_B, parent, local, global);
				return objA.type() == objB.type() && objA.get() == objB.get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
	}
}
