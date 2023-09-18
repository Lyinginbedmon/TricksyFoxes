package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ConditionNode extends TreeNode<ConditionNode>
{
	public static final Identifier VARIANT_VALUE_TRUE = new Identifier(Reference.ModInfo.MOD_ID, "value_true");
	public static final Identifier VARIANT_VALUE_EXISTS = new Identifier(Reference.ModInfo.MOD_ID, "value_exists");
	public static final Identifier VARIANT_VALUE_EQUALS = new Identifier(Reference.ModInfo.MOD_ID, "value_equals");
	public static final Identifier VARIANT_CLOSER_THAN = new Identifier(Reference.ModInfo.MOD_ID, "closer_than");
	
	public ConditionNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONDITION, uuidIn);
	}
	
	public static ConditionNode fromData(UUID uuid, NbtCompound data)
	{
		return new ConditionNode(uuid);
	}
	
	public static void populateSubTypes(Collection<NodeSubType<ConditionNode>> set)
	{
		/** Returns SUCCESS if the boolean value of the given object is TRUE */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_TRUE, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR, NodeTickHandler.any());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR);
				return Whiteboard.get(reference, local, global).as(TFObjType.BOOL).get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		/** Returns SUCCESS if the given object is not considered empty (this differs from VALUE_TRUE for several data types) */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_EXISTS, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR, NodeTickHandler.any());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR);
				return Whiteboard.get(reference, local, global).isEmpty() ? Result.FAILURE : Result.SUCCESS;
			}
		}));
		/** Returns SUCCESS if the given objects match */
		set.add(new NodeSubType<ConditionNode>(VARIANT_VALUE_EQUALS, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_A, NodeTickHandler.any(), 
						CommonVariables.VAR_B, NodeTickHandler.any());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				WhiteboardRef referenceA = parent.variable(CommonVariables.VAR_A);
				WhiteboardRef referenceB = parent.variable(CommonVariables.VAR_B);
				
				IWhiteboardObject<?> objA = Whiteboard.get(referenceA, local, global);
				IWhiteboardObject<?> objB = Whiteboard.get(referenceB, local, global);
				return objA.type() == objB.type() && objA.get() == objB.get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		/** Performs a simple distance check from the mob to the given position and returns SUCCESS if the distance is less than a desired value */
		set.add(new NodeSubType<ConditionNode>(VARIANT_CLOSER_THAN, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, NodeTickHandler.ofType(TFObjType.BLOCK), 
						CommonVariables.VAR_DIS, NodeTickHandler.ofType(TFObjType.INT));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				WhiteboardRef referencePos = parent.variable(CommonVariables.VAR_POS);
				WhiteboardRef referenceDist = parent.variable(CommonVariables.VAR_DIS);
				
				IWhiteboardObject<?> objPos = Whiteboard.get(referencePos, local, global);
				if(objPos.isEmpty())
					return Result.FAILURE;
				
				BlockPos position = objPos.as(TFObjType.BLOCK).get();
				int dist = Whiteboard.get(referenceDist, local, global).as(TFObjType.INT).get();
				return Math.sqrt(tricksy.squaredDistanceTo(position.getX(), position.getY(), position.getZ())) < dist ? Result.SUCCESS : Result.FAILURE;
			}
		}));
	}
}
