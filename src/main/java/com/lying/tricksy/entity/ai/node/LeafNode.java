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
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * TODO NODE TYPES
 * Leaf	- Performs an action and has no child nodes
 * 		Action	- Performs a base singular action from a predefined set
 * 		SubTree	- Performs a predefined complex action that would otherwise necessitate multiple nodes, such as melee combat
 */
public class LeafNode extends TreeNode<LeafNode>
{
	public static final Identifier VARIANT_CYCLE = new Identifier(Reference.ModInfo.MOD_ID, "cycle_value");
	public static final Identifier VARIANT_SET = new Identifier(Reference.ModInfo.MOD_ID, "set_value");
	
	public static final Identifier VARIANT_GOTO = new Identifier(Reference.ModInfo.MOD_ID, "goto");
	public static final Identifier VARIANT_DROP = new Identifier(Reference.ModInfo.MOD_ID, "drop_item");
	public static final Identifier VARIANT_SWAP = new Identifier(Reference.ModInfo.MOD_ID, "swap_items");
	public static final Identifier VARIANT_SLEEP = new Identifier(Reference.ModInfo.MOD_ID, "sleep");
	
	public LeafNode(UUID uuidIn)
	{
		super(TFNodeTypes.LEAF, uuidIn);
	}
	
	public static LeafNode fromData(UUID uuid, NbtCompound data)
	{
		return new LeafNode(uuid);
	}
	
	public static void populateSubTypes(Collection<NodeSubType<LeafNode>> set)
	{
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeTickHandler.ofType(TFObjType.BLOCK));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR_POS);
				
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = Whiteboard.get(reference, local, global);
					if(targetObj.isEmpty())
						return Result.FAILURE;
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(navigator.findPathTo(dest, 20) == null)
						return Result.FAILURE;
					
					navigator.startMovingTo(dest.getX(), dest.getY(), dest.getZ(), 0.5D);
					return Result.RUNNING;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_DROP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty())
					return Result.FAILURE;
				
				tricksy.dropStack(heldStack);
				tricksy.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SWAP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				ItemStack mainStack = tricksy.getMainHandStack().copy();
				ItemStack offStack = tricksy.getOffHandStack().copy();
				
				tricksy.setStackInHand(Hand.MAIN_HAND, offStack);
				tricksy.setStackInHand(Hand.OFF_HAND, mainStack);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SLEEP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				if(tricksy.hurtTime > 0 || !tricksy.isOnGround())
					return Result.FAILURE;
				
				if(parent.ticksRunning > 0 && parent.ticksRunning%Reference.Values.TICKS_PER_SECOND == 0)
					tricksy.heal(1F);
				
				return tricksy.getHealth() >= tricksy.getMaxHealth() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_CYCLE, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(VAR_A, NodeTickHandler.anyLocal());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<?> value = Whiteboard.get(parent.variable(VAR_A), local, global);
				if(!value.isList())
					return Result.FAILURE;
				
				value.cycle();
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_copy", TFObjType.BOOL).displayName(CommonVariables.translate("to_copy"));
			public static final WhiteboardRef VAR_B = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(
						VAR_A, NodeTickHandler.any(),
						VAR_B, NodeTickHandler.anyLocal());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef from = parent.variable(VAR_A);
				WhiteboardRef to = parent.variable(VAR_B);
				
				/** Destination must be a cachable value in a local whiteboard of the same or castable data type */
				if(to.uncached() || to.boardType() != BoardType.LOCAL || !from.type().castableTo(to.type()))
					return Result.FAILURE;
				
				local.setValue(to, Whiteboard.get(from, local, global).as(to.type()));
				return Result.SUCCESS;
			}
		}));
	}
}
