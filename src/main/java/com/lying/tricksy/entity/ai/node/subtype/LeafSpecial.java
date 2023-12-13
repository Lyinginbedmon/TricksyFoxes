package com.lying.tricksy.entity.ai.node.subtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.CandlePowers;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.task.PrepareRamTask.Ram;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class LeafSpecial implements ISubtypeGroup<LeafNode>
{
	/** TODO More special actions
	 * Foxfire
	 */
	public static final Identifier VARIANT_FOX_PRAY = ISubtypeGroup.variant("fox_pray");
	public static final Identifier VARIANT_FOX_STANCE = ISubtypeGroup.variant("fox_stance");
	public static final Identifier VARIANT_FOX_FIRE = ISubtypeGroup.variant("fox_fire");
	public static final Identifier VARIANT_GOAT_RAM = ISubtypeGroup.variant("goat_ram");
	public static final Identifier VARIANT_GOAT_BLOCKADE = ISubtypeGroup.variant("goat_blockade");
	public static final Identifier VARIANT_GOAT_JUMP = ISubtypeGroup.variant("goat_jump");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_special"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<LeafNode>(VARIANT_FOX_PRAY, leafFoxPray())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_FOX; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_FOX.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".fox_pray_cooldown")));
						return list;
					}
				});
		set.add(new NodeSubType<LeafNode>(VARIANT_GOAT_RAM, leafGoatRam())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".goat_ram_cooldown")));
						return list;
					}
				});
		set.add(new NodeSubType<LeafNode>(VARIANT_FOX_STANCE, leafFoxStance())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_FOX; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_FOX.getName()));
						list.addAll(super.fullDescription());
						return list;
					}
				});
		set.add(new NodeSubType<LeafNode>(VARIANT_GOAT_BLOCKADE, leafGoatBlockade())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						return list;
					}
				});
		set.add(new NodeSubType<LeafNode>(VARIANT_GOAT_JUMP, leafGoatJump())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						return list;
					}
				});
		return set;
	}
	
	public static INodeTickHandler<LeafNode> leafFoxPray()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
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
	

	public static INodeTickHandler<LeafNode> leafGoatRam()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int MIN_RAM_DISTANCE = 4;
			private static final int MAX_RAM_DISTANCE = 7;
			private static final TargetPredicate RAM_TARGET_PREDICATE = TargetPredicate.createAttackable().setPredicate(entity -> !entity.getType().equals(EntityType.GOAT) && entity.getWorld().getWorldBorder().contains(entity.getBoundingBox()));
			
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
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE, ActionFlag.LOOK); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> targetObj = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(targetObj.isEmpty() || !targetObj.get().isAlive() || !(targetObj.get() instanceof LivingEntity) || tricksy.hasVehicle())
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
		};
	}
	
	public static INodeTickHandler<LeafNode> leafFoxStance()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAL = CommonVariables.VAR;		
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(VAL, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(false), Text.literal("False")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
				{
					((EntityTricksyFox)tricksy).setStance(getOrDefault(VAL, parent, local, global).as(TFObjType.BOOL).get());
					return Result.SUCCESS;
				}
				return Result.FAILURE;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafGoatBlockade()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public static final int DURATION = Reference.Values.TICKS_PER_SECOND * 15;
			
			public <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy)
			{
				Random rand = tricksy.getRandom();
				return UniformIntProvider.create(6000, 12000).get(rand);
			}
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_GOAT && !tricksy.hasVehicle() && tricksy.isOnGround())
				{
					if(!parent.isRunning())
					{
						((EntityTricksyGoat)tricksy).setBlockading(true);
						tricksy.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, DURATION, 5));
						return Result.RUNNING;
					}
					else if(parent.ticksRunning() < DURATION)
						return Result.RUNNING;
					
					((EntityTricksyGoat)tricksy).setBlockading(false);
					tricksy.removeStatusEffect(StatusEffects.RESISTANCE);
					return Result.SUCCESS;
				}
				return Result.FAILURE;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				((EntityTricksyGoat)tricksy).setBlockading(false);
				tricksy.removeStatusEffect(StatusEffects.RESISTANCE);
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafGoatJump()
	{
		return new INodeTickHandler<LeafNode>()
				{
					private static final List<Integer> RAM_RANGES = Lists.newArrayList(80, 75, 70, 65);
					
					public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.LOOK, ActionFlag.MOVE); }
					
					public Map<WhiteboardRef, INodeIO> ioSet()
					{
						return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
					}
					
					public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
					{
						BlockPos target = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK).get();
						tricksy.getLookControl().lookAt(target.toCenterPos());
						if(!parent.isRunning())
						{
							if(!canRun(tricksy))
								return Result.FAILURE;
							
							if(target.getY() - tricksy.getBlockPos().getY() > 5)
								return Result.FAILURE;
							
							BlockPos offset = target.subtract(tricksy.getBlockPos());
							if((new Vec3i(offset.getX(), 0, offset.getZ())).getSquaredDistance(Vec3i.ZERO) > (5 * 5))
								return Result.FAILURE;
							
							Vec3d vel = getJumpingVelocity(tricksy, Vec3d.ofCenter(target));
							if(vel == null)
								return Result.FAILURE;
							
							parent.nodeRAM.putDouble("JumpX", vel.getX());
							parent.nodeRAM.putDouble("JumpY", vel.getY());
							parent.nodeRAM.putDouble("JumpZ", vel.getZ());
							
							parent.nodeRAM.putDouble("StartX", tricksy.getPos().getX());
							parent.nodeRAM.putDouble("StartY", tricksy.getPos().getY());
							parent.nodeRAM.putDouble("StartZ", tricksy.getPos().getZ());
						}
						else
						{
							Vec3d start = new Vec3d(parent.nodeRAM.getDouble("StartX"), parent.nodeRAM.getDouble("StartY"), parent.nodeRAM.getDouble("StartZ"));
							if(start.distanceTo(tricksy.getPos()) > 0)
							{
								if(!tricksy.isOnGround())
								{
									// Time in midair
									tricksy.setNoDrag(true);
									tricksy.setTreePose(EntityPose.LONG_JUMPING);
								}
								else
								{
									// Land on ground after launching
									tricksy.setVelocity(tricksy.getVelocity().multiply(0.1F, 1F, 0.1F));
									tricksy.getWorld().playSoundFromEntity(null, tricksy, SoundEvents.ENTITY_GOAT_STEP, SoundCategory.NEUTRAL, 2.0f, 1.0f);
									
									return Result.SUCCESS;
								}
							}
							// Launch after initial windup period
							else if(parent.ticksRunning() >= (Reference.Values.TICKS_PER_SECOND * 2))
							{
								((Entity)tricksy).setYaw(((MobEntity)tricksy).bodyYaw);
								tricksy.setNoDrag(true);
								tricksy.setTreePose(EntityPose.LONG_JUMPING);
								
								Vec3d vel = new Vec3d(parent.nodeRAM.getDouble("JumpX"), parent.nodeRAM.getDouble("JumpY"), parent.nodeRAM.getDouble("JumpZ"));
								double d = vel.length();
								double e = d + tricksy.getJumpBoostVelocityModifier();
								((Entity)tricksy).setVelocity(vel.multiply(e / d));
								
								if(tricksy.getType() == TFEntityTypes.TRICKSY_GOAT)
									tricksy.getWorld().playSoundFromEntity(null, tricksy, ((EntityTricksyGoat)tricksy).isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_LONG_JUMP : SoundEvents.ENTITY_GOAT_LONG_JUMP, SoundCategory.NEUTRAL, 1.0f, 1.0f);
							}
						}
						
						return Result.RUNNING;
					}
					
					private boolean canRun(LivingEntity tricksy)
					{
						return 
								tricksy.isOnGround() && 
								!tricksy.hasVehicle() && 
								!tricksy.isTouchingWater() && 
								!tricksy.isInLava() && 
								!tricksy.getWorld().getBlockState(tricksy.getBlockPos()).isOf(Blocks.HONEY_BLOCK);
					}
					
					public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
					{
						tricksy.setNoDrag(false);
						tricksy.setTreePose(tricksy.defaultPose());
					}
					
					@Nullable
					protected Vec3d getJumpingVelocity(MobEntity entity, Vec3d pos)
					{
						Iterator<Integer> iterator = RAM_RANGES.iterator();
						while(iterator.hasNext())
						{
							Vec3d vec3d = this.getJumpingVelocity(entity, pos, (Integer)iterator.next());
							if(vec3d != null)
								return vec3d;
						}
						return null;
					}
					
					@Nullable
					private Vec3d getJumpingVelocity(Entity entity, Vec3d pos, int range)
					{
						Vec3d vec3d = entity.getPos();
						Vec3d vec3d2 = new Vec3d(pos.x - vec3d.x, 0.0, pos.z - vec3d.z).normalize().multiply(0.5D);
						pos = pos.subtract(vec3d2);
						Vec3d vec3d3 = pos.subtract(vec3d);
				        float f = (float)range * (float)Math.PI / 180.0f;
				        double d = Math.atan2(vec3d3.z, vec3d3.x);
				        double e = vec3d3.subtract(0.0, vec3d3.y, 0.0).lengthSquared();
				        double g = Math.sqrt(e);
				        double h = vec3d3.y;
				        double i = Math.sin(2.0f * f);
				        double k = Math.pow(Math.cos(f), 2.0);
				        double l = Math.sin(f);
				        double m = Math.cos(f);
				        double n = Math.sin(d);
				        double o = Math.cos(d);
				        
				        double p = e * 0.08 / (g * i - 2.0 * h * k);
				        if(p < 0.0)
				            return null;
				        
				        double q = Math.sqrt(p);
				        if(q > 1.5D)
				            return null;
				        
				        double r = q * m;
				        double s = q * l;
				        int t = MathHelper.ceil(g / r) * 2;
				        double u = 0.0;
				        Vec3d vec3d4 = null;
				        EntityDimensions entityDimensions = entity.getDimensions(EntityPose.LONG_JUMPING);
				        for(int v = 0; v < t - 1; ++v)
				        {
				            double w = l / m * (u += g / (double)t) - Math.pow(u, 2.0) * 0.08 / (2.0 * p * Math.pow(m, 2.0));
				            double x = u * o;
				            double y = u * n;
				            Vec3d vec3d5 = new Vec3d(vec3d.x + x, vec3d.y + w, vec3d.z + y);
				            if(vec3d4 != null && !this.canReach(entity, entityDimensions, vec3d4, vec3d5))
				                return null;
				            
				            vec3d4 = vec3d5;
				        }
				        
				        return new Vec3d(r * o, s, r * n).multiply(1.25f);
					}
					
					private boolean canReach(Entity entity, EntityDimensions dimensions, Vec3d vec3d, Vec3d vec3d2)
					{
						Vec3d vec3d3 = vec3d2.subtract(vec3d);
						double d = Math.min(dimensions.width, dimensions.height);
						int i = MathHelper.ceil(vec3d3.length() / d);
						Vec3d vec3d4 = vec3d3.normalize();
						Vec3d vec3d5 = vec3d;
						for (int j = 0; j < i; ++j)
						{
							vec3d5 = j == i - 1 ? vec3d2 : vec3d5.add(vec3d4.multiply(d * (double)0.9f));
							if(!entity.getWorld().isSpaceEmpty(entity, dimensions.getBoxAt(vec3d5)))
								return false;
						}
						return true;
					}
				};
	}
}
