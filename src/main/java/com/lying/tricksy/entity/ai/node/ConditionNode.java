package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.subtype.ConditionInventory;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * TODO Add more conditions
 */
public class ConditionNode extends TreeNode<ConditionNode>
{
	public static final Identifier VARIANT_CLOSER_THAN = ISubtypeGroup.variant("closer_than");
	public static final Identifier VARIANT_BLOCK_POWERED = ISubtypeGroup.variant("block_powered");
	public static final Identifier VARIANT_ON_FIRE = ISubtypeGroup.variant("on_fire");
	public static final Identifier VARIANT_IS_TYPE = ISubtypeGroup.variant("is_type");	// TODO Implement entity type check
	
	private static final Set<ISubtypeGroup<ConditionNode>> SUBTYPES = Set.of(new ConditionWhiteboard(), new ConditionInventory());
	
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
		SUBTYPES.forEach((group) -> group.addActions(set));
		
		/** Performs a simple distance check from the mob to the given position and returns SUCCESS if the distance is less than a desired value */
		set.add(new NodeSubType<ConditionNode>(VARIANT_CLOSER_THAN, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_POS_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)), 
						CommonVariables.VAR_POS_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()), 
						CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(8)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ConditionNode parent)
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
						posB = local.getValue(LocalWhiteboard.SELF).as(TFObjType.BLOCK).get();
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
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ConditionNode parent)
			{
				BlockPos position = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK).get();
				return tricksy.getEntityWorld().isReceivingRedstonePower(position) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_ON_FIRE, new NodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), TFObjType.ENT.blank(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ConditionNode parent)
			{
				IWhiteboardObject<Entity> var = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity ent = var.size() == 0 ? tricksy : var.get();
				if(ent == null || !(ent instanceof LivingEntity))
					return Result.FAILURE;
				
				return ((LivingEntity)ent).isOnFire() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
	}
}
