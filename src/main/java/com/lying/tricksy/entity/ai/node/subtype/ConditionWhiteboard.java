package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class ConditionWhiteboard implements ISubtypeGroup<ConditionNode>
{
	public static final Identifier VARIANT_VALUE_TRUE = ISubtypeGroup.variant("value_true");
	public static final Identifier VARIANT_VALUE_EXISTS = ISubtypeGroup.variant("value_exists");
	public static final Identifier VARIANT_EQUALS = ISubtypeGroup.variant("value_equals");
	public static final Identifier VARIANT_LESS_THAN = ISubtypeGroup.variant("less_than");
	public static final Identifier VARIANT_GREATER_THAN = ISubtypeGroup.variant("greater_than");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "condition_whiteboard"); }
	
	public Collection<NodeSubType<ConditionNode>> getSubtypes()
	{
		List<NodeSubType<ConditionNode>> set = Lists.newArrayList();
		/** Returns SUCCESS if the boolean value of the given object is TRUE */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_TRUE, new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR, NodeInput.makeInput(NodeInput.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				return getOrDefault(CommonVariables.VAR, parent, whiteboards).as(TFObjType.BOOL).get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		/** Returns SUCCESS if the given object is not considered empty (this differs from VALUE_TRUE for several data types) */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_EXISTS, new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR, NodeInput.makeInput(NodeInput.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(CommonVariables.VAR, parent, whiteboards);
				return (value == null || value.isEmpty()) ? Result.FAILURE : Result.SUCCESS;
			}
		}));
		/** Returns SUCCESS if the given objects match */
		set.add(new NodeSubType<ConditionNode>(VARIANT_EQUALS, new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_A, NodeInput.makeInput(NodeInput.any()), 
						CommonVariables.VAR_B, NodeInput.makeInput(NodeInput.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<?> objA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards);
				IWhiteboardObject<?> objB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards);
				return objA.type() == objB.type() && objA.get() == objB.get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_LESS_THAN, new INodeTickHandler<ConditionNode>() 
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true)), 
						CommonVariables.VAR_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				int objA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards).as(TFObjType.INT).get();
				int objB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards).as(TFObjType.INT).get();
				return objA < objB ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_GREATER_THAN, new INodeTickHandler<ConditionNode>() 
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true)), 
						CommonVariables.VAR_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				int objA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards).as(TFObjType.INT).get();
				int objB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards).as(TFObjType.INT).get();
				return objA > objB ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		return set;
	}
}
