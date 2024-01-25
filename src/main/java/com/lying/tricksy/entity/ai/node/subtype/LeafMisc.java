package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ITricksyMob.Bark;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerTyped;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.Region;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;

public class LeafMisc implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_BARK = ISubtypeGroup.variant("bark");
	public static final Identifier VARIANT_GOTO = ISubtypeGroup.variant("goto");
	public static final Identifier VARIANT_STOP = ISubtypeGroup.variant("stop");
	public static final Identifier VARIANT_WAIT = ISubtypeGroup.variant("wait");
	public static final Identifier VARIANT_SLEEP = ISubtypeGroup.variant("sleep");
	public static final Identifier VARIANT_SET_HOME = ISubtypeGroup.variant("set_home");
	public static final Identifier VARIANT_LOOK_AROUND = ISubtypeGroup.variant("look_around");
	public static final Identifier VARIANT_LOOK_AT = ISubtypeGroup.variant("look_at");
	public static final Identifier VARIANT_WANDER = ISubtypeGroup.variant("wander");
	public static final Identifier VARIANT_ORDER_COMPLETE = ISubtypeGroup.variant("complete_order");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_misc"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<LeafNode>(VARIANT_LOOK_AT, leafLookAt()));
		set.add(new NodeSubType<LeafNode>(VARIANT_LOOK_AROUND, leafLookAround()));
		set.add(new NodeSubType<LeafNode>(VARIANT_BARK, new INodeTickHandler<LeafNode>() 
		{
			private static final WhiteboardRef BARK = CommonVariables.VAR_NUM;
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(BARK, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1), Text.literal(String.valueOf(1))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				int index = MathHelper.clamp(getOrDefault(BARK, parent, whiteboards).as(TFObjType.INT).get(), 0, Bark.values().length - 1);
				tricksy.bark(Bark.values()[index]);
				return Result.SUCCESS;
			}
		}, ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND / 2)));
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, leafGoTo()));
		set.add(new NodeSubType<LeafNode>(VARIANT_STOP, leafStop()));
		set.add(new NodeSubType<LeafNode>(VARIANT_WANDER, leafWander()));
		set.add(new NodeSubType<LeafNode>(VARIANT_SLEEP, leafSleep(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND)));
		set.add(new NodeSubType<LeafNode>(VARIANT_WAIT, new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return Result.SUCCESS;
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_HOME, new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				if(value.size() == 0)
					tricksy.clearPositionTarget();
				else
					tricksy.setPositionTarget(value.get(), 6);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_ORDER_COMPLETE, new INodeTickHandler<LeafNode>()
				{
					public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
					{
						whiteboards.order().setValue(OrderWhiteboard.ACTIVE, new WhiteboardObj.Bool(false));
						return Result.SUCCESS;
					}
				}));
		return set;
	}
	
	public static INodeTickHandler<LeafNode> leafGoTo()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards);
					if(targetObj.isEmpty())
						return Result.FAILURE;
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(dest.getSquaredDistance(tricksy.getBlockPos()) <= 0.3D)
						return Result.SUCCESS;
					
					Path path = navigator.findPathToAny(ImmutableSet.of(dest), 100, false, 1, 128F);
					if(path != null && navigator.startMovingAlong(path, 1D))
						return Result.RUNNING;
					else
						return Result.FAILURE;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafStop()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				tricksy.getNavigation().stop();
				return Result.SUCCESS;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafWander()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, GetterHandlerTyped.POS_OR_REGION,
						CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards);
				IWhiteboardObject<Integer> targetRange = getOrDefault(CommonVariables.VAR_DIS, parent, whiteboards).as(TFObjType.INT);
				Region area = GetterHandlerTyped.getSearchArea(targetObj, targetRange, tricksy, (mob) -> 
				{
					// Wander area based on region -> home position -> current position
					if(mob.hasPositionTarget())
						return mob.getPositionTarget();
					else
						return mob.getBlockPos();
				});
				
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					Random rand = tricksy.getRandom();
					
					int attempts = 50;
					BlockPos dest;
					Path path;
					do
					{
						dest = getWanderTarget(area, rand, tricksy.getWorld().getBottomY());
						path = navigator.findPathTo(dest, 20);
					}
					while(--attempts > 0 && path == null);
					
					if(path == null)
						return Result.FAILURE;
					else if(dest.getSquaredDistance(tricksy.getBlockPos()) <= 1D)
						return Result.SUCCESS;
					
					navigator.startMovingTo(dest.getX(), dest.getY(), dest.getZ(), 1D);
					return navigator.isFollowingPath() ? Result.RUNNING : Result.FAILURE;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
			
			private BlockPos getWanderTarget(Region area, Random rand, int bottomY)
			{
				BlockPos dest = area.findRandomWithin(rand);
				if(dest.getY() < bottomY)
					dest = new BlockPos(dest.getX(), bottomY, dest.getZ());
				return dest;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafSleep()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					if(canSleep(tricksy))
					{
						tricksy.setTreePose(EntityPose.SITTING);
						return Result.RUNNING;
					}
					else
						return Result.FAILURE;
				}
				else
				{
					Result result = Result.RUNNING;
					
					if(!canSleep(tricksy))
						result = Result.FAILURE;
					else if(parent.ticksRunning()%Reference.Values.TICKS_PER_SECOND == 0 && tricksy.getHealth() < tricksy.getMaxHealth())
						tricksy.heal(1F);
					
					tricksy.setTreePose(result.isEnd() ? tricksy.defaultPose() : EntityPose.SITTING);
					return result;
				}
			}
			
			private <T extends PathAwareEntity & ITricksyMob<?>> boolean canSleep(T tricksy) { return tricksy.isOnGround() && tricksy.hurtTime <= 0; }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				tricksy.setTreePose(tricksy.defaultPose());
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafLookAt()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.LOOK); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(
						(var) -> !var.isSameRef(LocalWhiteboard.SELF) && (var.type() == TFObjType.BLOCK || var.type() == TFObjType.ENT) && !var.isFilter(), 
						new WhiteboardObjEntity(), 
						LocalWhiteboard.ATTACK_TARGET.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> target = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards);
				if(target.size() > 0)
				{
					if(target.type() == TFObjType.BLOCK)
					{
						BlockPos pos = target.as(TFObjType.BLOCK).get();
						tricksy.getLookControl().lookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
					}
					else if(target.type() == TFObjType.ENT)
					{
						Entity ent = target.as(TFObjType.ENT).size() == 0 ? tricksy.getAttacking() : target.as(TFObjType.ENT).get();
						if(ent != null)
							tricksy.getLookControl().lookAt(ent);
					}
					return Result.SUCCESS;
				}
				return Result.FAILURE;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafLookAround()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.LOOK); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					int duration = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT).get();
					parent.ticks = (duration + 1) * 20;
					
					Random rand = tricksy.getRandom();
					double d = Math.PI * 2 * rand.nextDouble();
					parent.nodeRAM.putDouble("DeltaX", Math.cos(d));
					parent.nodeRAM.putDouble("DeltaZ", Math.sin(d));
				}
				else if(--parent.ticks == 0)
					return Result.SUCCESS;
				
				tricksy.getLookControl().lookAt(tricksy.getX() + parent.nodeRAM.getDouble("DeltaX"), tricksy.getEyeY(), tricksy.getZ() + parent.nodeRAM.getDouble("DeltaZ"));
				return Result.RUNNING;
			}
		};
	}
}
