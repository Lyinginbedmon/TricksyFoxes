package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ITricksyMob.Bark;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.LeafCombat;
import com.lying.tricksy.entity.ai.node.subtype.LeafGetter;
import com.lying.tricksy.entity.ai.node.subtype.LeafInteraction;
import com.lying.tricksy.entity.ai.node.subtype.LeafInventory;
import com.lying.tricksy.entity.ai.node.subtype.LeafWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * TODO Add more actions
 * NODE TYPES
 * Leaf	- Performs an action and has no child nodes
 * 		Action	- Performs a base singular action from a predefined set
 * 		SubTree	- Performs a predefined complex action that would otherwise necessitate multiple nodes, such as melee combat
 */
public class LeafNode extends TreeNode<LeafNode>
{
	public static final Identifier VARIANT_BARK = ISubtypeGroup.variant("bark");
	public static final Identifier VARIANT_GOTO = ISubtypeGroup.variant("goto");
	public static final Identifier VARIANT_WAIT = ISubtypeGroup.variant("wait");
	public static final Identifier VARIANT_SLEEP = ISubtypeGroup.variant("sleep");
	public static final Identifier VARIANT_SET_HOME = ISubtypeGroup.variant("set_home");
	
	protected int ticks = 20;
	
	private static final Set<ISubtypeGroup<LeafNode>> SUBTYPES = Set.of(new LeafWhiteboard(), new LeafInventory(), new LeafInteraction(), new LeafCombat(), new LeafGetter());
	
	public LeafNode(UUID uuidIn)
	{
		super(TFNodeTypes.LEAF, uuidIn);
	}
	
	public static LeafNode fromData(UUID uuid, NbtCompound data)
	{
		return new LeafNode(uuid);
	}
	
	public final boolean canAddChild() { return false; }
	
	public static void populateSubTypes(Collection<NodeSubType<LeafNode>> set)
	{
		SUBTYPES.forEach((group) -> group.addActions(set));
		
		set.add(new NodeSubType<LeafNode>(VARIANT_BARK, new NodeTickHandler<LeafNode>() 
		{
			private static final WhiteboardRef BARK = new WhiteboardRef("bark", TFObjType.INT).displayName(CommonVariables.translate("bark"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(BARK, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(1), ConstantsWhiteboard.NUM_1.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				int index = MathHelper.clamp(getOrDefault(BARK, parent, local, global).as(TFObjType.INT).get(), 0, Bark.values().length - 1);
				tricksy.bark(Bark.values()[index]);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
					if(targetObj.isEmpty())
					{
						tricksy.logStatus(Text.literal("No destination to go to"));
						return Result.FAILURE;
					}
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(dest.getSquaredDistance(tricksy.getBlockPos()) <= 1D)
						return Result.SUCCESS;
					
					navigator.startMovingTo(dest.getX(), dest.getY(), dest.getZ(), 1D);
					tricksy.logStatus(Text.literal(navigator.isFollowingPath() ? "Moving to destination" : "No path found"));
					return navigator.isFollowingPath() ? Result.RUNNING : Result.FAILURE;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SLEEP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					if(canSleep(tricksy))
					{
						tricksy.logStatus(Text.literal("zzZZzzzZZ"));
						tricksy.setSleeping(true);
						return Result.RUNNING;
					}
					else
					{
						tricksy.logStatus(Text.literal("I can't sleep now"));
						return Result.FAILURE;
					}
				}
				else
				{
					Result end = Result.RUNNING;
					
					if(!canSleep(tricksy))
						end = Result.FAILURE;
					else if(parent.ticksRunning%Reference.Values.TICKS_PER_SECOND == 0 && tricksy.getHealth() < tricksy.getMaxHealth())
						tricksy.heal(1F);
					
					tricksy.setSleeping(!end.isEnd());
					return end;
				}
			}
			
			private <T extends PathAwareEntity & ITricksyMob<?>> boolean canSleep(T tricksy) { return tricksy.isOnGround() && tricksy.hurtTime <= 0; }
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_WAIT, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return Result.SUCCESS;
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_HOME, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(value.size() == 0)
					tricksy.clearPositionTarget();
				else
					tricksy.setPositionTarget(value.get(), 6);
				return Result.SUCCESS;
			}
		}));
	}
}
