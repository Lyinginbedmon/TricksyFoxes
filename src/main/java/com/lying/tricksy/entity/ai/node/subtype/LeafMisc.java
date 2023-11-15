package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ITricksyMob.Bark;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.GetterHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.Region;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class LeafMisc implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_BARK = ISubtypeGroup.variant("bark");
	public static final Identifier VARIANT_GOTO = ISubtypeGroup.variant("goto");
	public static final Identifier VARIANT_WAIT = ISubtypeGroup.variant("wait");
	public static final Identifier VARIANT_SLEEP = ISubtypeGroup.variant("sleep");
	public static final Identifier VARIANT_SET_HOME = ISubtypeGroup.variant("set_home");
	public static final Identifier VARIANT_LOOK_AROUND = ISubtypeGroup.variant("look_around");
	public static final Identifier VARIANT_LOOK_AT = ISubtypeGroup.variant("look_at");
	public static final Identifier VARIANT_WANDER = ISubtypeGroup.variant("wander");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_misc"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<LeafNode>(VARIANT_LOOK_AT, leafLookAt()));
		set.add(new NodeSubType<LeafNode>(VARIANT_LOOK_AROUND, leafLookAround()));
		set.add(new NodeSubType<LeafNode>(VARIANT_BARK, new NodeTickHandler<LeafNode>() 
		{
			private static final WhiteboardRef BARK = CommonVariables.VAR_NUM;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(BARK, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1), Text.literal(String.valueOf(1))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				int index = MathHelper.clamp(getOrDefault(BARK, parent, local, global).as(TFObjType.INT).get(), 0, Bark.values().length - 1);
				tricksy.bark(Bark.values()[index]);
				tricksy.logStatus(Text.literal("Arf! Arf!"));
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, leafGoTo()));
		set.add(new NodeSubType<LeafNode>(VARIANT_WANDER, leafWander()));
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
					else if(parent.ticksRunning()%Reference.Values.TICKS_PER_SECOND == 0 && tricksy.getHealth() < tricksy.getMaxHealth())
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
				return Map.of(CommonVariables.VAR_NUM, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				
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
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock()));
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
		return set;
	}
	
	public static NodeTickHandler<LeafNode> leafGoTo()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false)));
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
					
					navigator.startMovingTo(dest.getX() + 0.5D, dest.getY(), dest.getZ() + 0.5D, 1D);
					tricksy.logStatus(Text.literal(navigator.isFollowingPath() ? "Moving to "+dest.toShortString() : "No path found"));
					return navigator.isFollowingPath() ? Result.RUNNING : Result.FAILURE;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		};
	}
	
	public static NodeTickHandler<LeafNode> leafWander()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION,
						CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> targetRange = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				Region area = GetterHandler.getSearchArea(targetObj, targetRange, tricksy, (mob) -> 
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
					tricksy.logStatus(Text.literal(navigator.isFollowingPath() ? "Moving to destination" : "No path found"));
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
	
	public static NodeTickHandler<LeafNode> leafLookAt()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(
						(var) -> !var.isSameRef(LocalWhiteboard.SELF) && (var.type() == TFObjType.BLOCK || var.type() == TFObjType.ENT) && !var.isFilter(), 
						new WhiteboardObjEntity(), 
						LocalWhiteboard.ATTACK_TARGET.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global);
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
	
	public static NodeTickHandler<LeafNode> leafLookAround()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_NUM, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					int duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT).get();
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
