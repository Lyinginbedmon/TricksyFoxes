package com.lying.tricksy.entity.ai.node.subtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ITricksyMob.Bark;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerTyped;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.CandlePowers;
import com.lying.tricksy.utility.Region;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.task.PrepareRamTask.Ram;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
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
	public static final Identifier VARIANT_FOX_PRAY = ISubtypeGroup.variant("pray");
	public static final Identifier VARIANT_GOAT_RAM = ISubtypeGroup.variant("goat_ram");
	
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
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				int index = MathHelper.clamp(getOrDefault(BARK, parent, local, global).as(TFObjType.INT).get(), 0, Bark.values().length - 1);
				tricksy.bark(Bark.values()[index]);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, leafGoTo()));
		set.add(new NodeSubType<LeafNode>(VARIANT_STOP, leafStop()));
		set.add(new NodeSubType<LeafNode>(VARIANT_WANDER, leafWander()));
		set.add(new NodeSubType<LeafNode>(VARIANT_SLEEP, new INodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					if(canSleep(tricksy))
					{
						tricksy.setTreeSleeping(true);
						return Result.RUNNING;
					}
					else
						return Result.FAILURE;
				}
				else
				{
					Result end = Result.RUNNING;
					
					if(!canSleep(tricksy))
						end = Result.FAILURE;
					else if(parent.ticksRunning()%Reference.Values.TICKS_PER_SECOND == 0 && tricksy.getHealth() < tricksy.getMaxHealth())
						tricksy.heal(1F);
					
					tricksy.setTreeSleeping(!end.isEnd());
					return end;
				}
			}
			
			private <T extends PathAwareEntity & ITricksyMob<?>> boolean canSleep(T tricksy) { return tricksy.isOnGround() && tricksy.hurtTime <= 0; }
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_WAIT, new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1)));
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
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_HOME, new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock()));
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
		set.add(new NodeSubType<LeafNode>(VARIANT_FOX_PRAY, leafPray())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_FOX; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						MutableText exclusivity = Text.literal("Exclusive to ").append(TFEntityTypes.TRICKSY_FOX.getName()).styled(style -> style.withBold(true).withColor(Formatting.GOLD));
						list.add(exclusivity);
						list.addAll(super.fullDescription());
						return list;
					}
				});
		set.add(new NodeSubType<LeafNode>(VARIANT_GOAT_RAM, new INodeTickHandler<LeafNode>()
				{
					private static final int MIN_RAM_DISTANCE = 4;
					private static final int MAX_RAM_DISTANCE = 7;
					private static final TargetPredicate RAM_TARGET_PREDICATE = TargetPredicate.createAttackable().setPredicate(entity -> !entity.getType().equals(EntityType.GOAT) && entity.getWorld().getWorldBorder().contains(entity.getBoundingBox()));
					
					public Map<WhiteboardRef, INodeIO> ioSet()
					{
						return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
					}
					
					public <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy)
					{
						Random rand = tricksy.getRandom();
						if(tricksy.getType() == TFEntityTypes.TRICKSY_GOAT)
						{
							EntityTricksyGoat goat = (EntityTricksyGoat)tricksy;
							if(goat.isScreaming())
								return UniformIntProvider.create(100, 300).get(rand);
							else
								return UniformIntProvider.create(600, 6000).get(rand);
						}
						return UniformIntProvider.create(6000, 60000).get(rand);
					}
					
					public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
					{
						IWhiteboardObject<Entity> targetObj = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
						if(targetObj.isEmpty() || !targetObj.get().isAlive() || !(targetObj.get() instanceof LivingEntity))
							return Result.FAILURE;
						
						LivingEntity target = (LivingEntity)targetObj.get();
						EntityTricksyGoat goat = (EntityTricksyGoat)tricksy;
						
						EntityNavigation navigator = tricksy.getNavigation();
						ServerWorld world = (ServerWorld)tricksy.getWorld();
						tricksy.lookAtEntity(target, 10F, 20F);
						if(!parent.isRunning())
						{
							double dist = target.distanceTo(tricksy);
							if(dist < 4D || dist > 16D)
								return Result.FAILURE;
							
							Optional<Ram> ram = findRamStart(goat, target).map(start -> new Ram((BlockPos)start, target.getBlockPos(), target));
							if(ram.isPresent())
							{
								Path path = navigator.findPathTo(ram.get().getEnd(), 0);
								navigator.startMovingAlong(path, 3F);
								
								BlockPos goatPos = goat.getBlockPos();
								Vec3d targetVec = calculateRamTarget(ram.get().getStart(), ram.get().getEnd());
								Vec3d direction = new Vec3d((double)goatPos.getX() - targetVec.getX(), 0, (double)goatPos.getZ() - targetVec.getZ()).normalize();
								parent.nodeRAM.putDouble("X", direction.getX());
								parent.nodeRAM.putDouble("Z", direction.getZ());
								
								world.playSoundFromEntity(null, tricksy, goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM, SoundCategory.NEUTRAL, 1.0f, tricksy.getSoundPitch());
								return navigator.isFollowingPath() ? Result.RUNNING : Result.FAILURE;
							}
							else
								return Result.FAILURE;
						}
						else if(navigator.isFollowingPath())
						{
							List<LivingEntity> targets = world.getTargets(LivingEntity.class, RAM_TARGET_PREDICATE, tricksy, tricksy.getBoundingBox());
							if(!targets.isEmpty())
							{
								LivingEntity struckTarget = targets.get(0);
								struckTarget.damage(world.getDamageSources().mobAttackNoAggro(tricksy), (float)tricksy.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
								
								Vec3d direction = new Vec3d(parent.nodeRAM.getDouble("X"), 0D, parent.nodeRAM.getDouble("Z"));
								int speedBuff = goat.hasStatusEffect(StatusEffects.SPEED) ? goat.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
								int slowDebuff = goat.hasStatusEffect(StatusEffects.SLOWNESS) ? goat.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1 : 0;
								float impact = 0.25f * (float)(speedBuff - slowDebuff);
								float g = MathHelper.clamp(goat.getMovementSpeed() * 1.65f, 0.2f, 3.0f) + impact;
								float h = struckTarget.blockedByShield(world.getDamageSources().mobAttack(goat)) ? 0.5f : 1.0f;
								struckTarget.takeKnockback((double)(h * g) * 2.5D, direction.getX(), direction.getZ());
								
								world.playSoundFromEntity(null, tricksy, goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_RAM_IMPACT : SoundEvents.ENTITY_GOAT_RAM_IMPACT, SoundCategory.NEUTRAL, 1.0f, 1.0f);
								return Result.SUCCESS;
							}
							return Result.RUNNING;
						}
						return Result.FAILURE;
					}
					
					private Vec3d calculateRamTarget(BlockPos start, BlockPos end)
					{
						return Vec3d.ofBottomCenter(end).add(0.5D * MathHelper.sign(end.getX() - start.getX()), 0, 0.5D * MathHelper.sign(end.getZ() - start.getZ()));
					}
					
					private Optional<BlockPos> findRamStart(PathAwareEntity entity, LivingEntity target)
					{
						BlockPos pos = target.getBlockPos();
						if(!canReach(entity, pos))
							return Optional.empty();
						ArrayList<BlockPos> list = Lists.newArrayList();
						BlockPos.Mutable mutable = pos.mutableCopy();
						for(Direction direction : Direction.Type.HORIZONTAL)
						{
							mutable.set(pos);
							for(int i=0; i<MAX_RAM_DISTANCE; ++i)
							{
								if(canReach(entity, mutable.move(direction)))
									continue;
								mutable.move(direction.getOpposite());
								break;
							}
							if(mutable.getManhattanDistance(pos) < MIN_RAM_DISTANCE)
								continue;
							list.add(mutable.toImmutable());
						}
						EntityNavigation navigator = entity.getNavigation();
						return list.stream().sorted(Comparator.comparingDouble(entity.getBlockPos()::getSquaredDistance)).filter(start -> {
							Path path = navigator.findPathTo((BlockPos)start, 0);
							return path != null && path.reachesTarget();
						}).findFirst();
					}
					
					private boolean canReach(PathAwareEntity entity, BlockPos target)
					{
						return entity.getNavigation().isValidPosition(target) && entity.getPathfindingPenalty(LandPathNodeMaker.getLandNodeType(entity.getWorld(), target.mutableCopy())) == 0.0f;
					}
				})
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						MutableText exclusivity = Text.literal("Exclusive to ").append(TFEntityTypes.TRICKSY_GOAT.getName()).styled(style -> style.withBold(true).withColor(Formatting.GOLD));
						MutableText cooldown = Text.literal("Cooldown: "+"30-300 seconds, 5-15 seconds for screaming").styled(style -> style.withColor(Formatting.GRAY));
						list.add(exclusivity);
						list.addAll(super.fullDescription());
						list.add(cooldown);
						return list;
					}
				});
		return set;
	}
	
	public static INodeTickHandler<LeafNode> leafGoTo()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
					if(targetObj.isEmpty())
						return Result.FAILURE;
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(dest.getSquaredDistance(tricksy.getBlockPos()) <= 1D)
						return Result.SUCCESS;
					
					navigator.startMovingTo(dest.getX() + 0.5D, dest.getY(), dest.getZ() + 0.5D, 1D);
					if(navigator.isFollowingPath())
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
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
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
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, GetterHandlerTyped.POS_OR_REGION,
						CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> targetRange = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
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
	
	public static INodeTickHandler<LeafNode> leafLookAt()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(
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
	
	public static INodeTickHandler<LeafNode> leafLookAround()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal(String.valueOf(4))));
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
	
	public static INodeTickHandler<LeafNode> leafPray()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)),
						CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(1), Text.literal(String.valueOf(1))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> posIn = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(posIn.size() == 0)
					return Result.FAILURE;
				
				BlockPos pos = posIn.get();
				if(tricksy.getWorld().getBlockState(pos).getBlock() != TFBlocks.PRESCIENT_CANDLE || tricksy.squaredDistanceTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > (INTERACT_RANGE * INTERACT_RANGE))
					return Result.FAILURE;
				
				tricksy.getLookControl().lookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				if(parent.ticksRunning() < Reference.Values.TICKS_PER_SECOND)
					return Result.RUNNING;
				
				int power = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT).get();
				CandlePowers candles = CandlePowers.getCandlePowers(tricksy.getServer());
				candles.setPowerFor(tricksy.getUuid(), power);
				return Result.SUCCESS;
			}
		};
	}
}
