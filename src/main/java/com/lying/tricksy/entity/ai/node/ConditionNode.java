package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * TODO Add more conditions
 */
public class ConditionNode extends TreeNode<ConditionNode>
{
	public static final Identifier VARIANT_VALUE_TRUE = new Identifier(Reference.ModInfo.MOD_ID, "value_true");
	public static final Identifier VARIANT_VALUE_EXISTS = new Identifier(Reference.ModInfo.MOD_ID, "value_exists");
	public static final Identifier VARIANT_VALUE_EQUALS = new Identifier(Reference.ModInfo.MOD_ID, "value_equals");
	public static final Identifier VARIANT_CLOSER_THAN = new Identifier(Reference.ModInfo.MOD_ID, "closer_than");
	
	public static final Identifier VARIANT_ITEM_EMPTY = new Identifier(Reference.ModInfo.MOD_ID, "item_empty");
	public static final Identifier VARIANT_BLOCK_POWERED = new Identifier(Reference.ModInfo.MOD_ID, "block_powered");
	
	public ConditionNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONDITION, uuidIn);
	}
	
	public static ConditionNode fromData(UUID uuid, NbtCompound data)
	{
		return new ConditionNode(uuid);
	}
	
	public final boolean canAddChild() { return false; }
	
	public static void populateSubTypes(Collection<NodeSubType<ConditionNode>> set)
	{
		addWhiteboardConditions(set);
		
		/** Performs a simple distance check from the mob to the given position and returns SUCCESS if the distance is less than a desired value */
		set.add(new NodeSubType<ConditionNode>(VARIANT_CLOSER_THAN, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_POS_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)), 
						CommonVariables.VAR_POS_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), Local.SELF.displayName()), 
						CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(8)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				// Value A - mandatory
				IWhiteboardObject<?> objPosA = getOrDefault(CommonVariables.VAR_POS_A, parent, local, global);
				if(objPosA.isEmpty())
					return Result.FAILURE;
				BlockPos posA = objPosA.as(TFObjType.BLOCK).get();
				
				// Value B - optional, defaults to mob's position
				IWhiteboardObject<?> objPosB = getOrDefault(CommonVariables.VAR_POS_B, parent, local, global);
				BlockPos posB;
				if(objPosB.isEmpty())
				{
					if(objPosB.size() == 0)
						posB = local.getValue(Local.SELF).as(TFObjType.BLOCK).get();
					else
						return Result.FAILURE;
				}
				else
					posB = objPosB.as(TFObjType.BLOCK).get();
				
				int dist = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT).get();
				return Math.sqrt(posA.getSquaredDistance(posB)) < dist ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_BLOCK_POWERED, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				BlockPos position = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK).get();
				return tricksy.getEntityWorld().isReceivingRedstonePower(position) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
	}
	
	private static void addWhiteboardConditions(Collection<NodeSubType<ConditionNode>> set)
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
