package com.lying.tricksy.entity.ai.node.subtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.entity.EntitySeclusion;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.entity.projectile.EntityFoxFire;
import com.lying.tricksy.entity.projectile.EntityOfudaStuck;
import com.lying.tricksy.entity.projectile.EntityOfudaThrown;
import com.lying.tricksy.entity.projectile.EntityOnryojiFire;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.CandlePowers;
import com.lying.tricksy.utility.Howls;
import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.block.BlockState;
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
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;

public class LeafSpecial extends NodeGroupLeaf
{
	public static NodeSubType<LeafNode> FOX_PRAY;
	public static NodeSubType<LeafNode> FOX_STANCE;
	public static NodeSubType<LeafNode> FOX_FIRE;
	
	public static NodeSubType<LeafNode> GOAT_RAM;
	public static NodeSubType<LeafNode> GOAT_BLOCKADE;
	public static NodeSubType<LeafNode> GOAT_JUMP;
	
	public static NodeSubType<LeafNode> WOLF_LEAD;
	public static NodeSubType<LeafNode> WOLF_UNLEAD;
	public static NodeSubType<LeafNode> WOLF_BLESS;
	public static NodeSubType<LeafNode> WOLF_HOWL;
	
	public static NodeSubType<LeafNode> ONRYOJI_BALANCE;	// TODO Needs animation & affected visuals
	public static NodeSubType<LeafNode> ONRYOJI_OFUDA;		// TODO Needs finalised ammo visuals
	public static NodeSubType<LeafNode> ONRYOJI_FOXFIRE;
	public static NodeSubType<LeafNode> ONRYOJI_SECLUSION;	// TODO Needs animation
	public static NodeSubType<LeafNode> ONRYOJI_COMMANDERS;	// TODO Needs animation & stage display
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_special"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(FOX_PRAY = new NodeSubType<LeafNode>(ISubtypeGroup.variant("fox_pray"), TFNodeTypes.LEAF, leafFoxPray(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND))
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
		set.add(GOAT_RAM = new NodeSubType<LeafNode>(ISubtypeGroup.variant("goat_ram"), TFNodeTypes.LEAF, leafGoatRam())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
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
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".goat_ram_cooldown")));
						return list;
					}
				});
		set.add(FOX_STANCE = new NodeSubType<LeafNode>(ISubtypeGroup.variant("fox_stance"), TFNodeTypes.LEAF, leafFoxStance())
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
		set.add(FOX_FIRE = new NodeSubType<LeafNode>(ISubtypeGroup.variant("fox_fire"), TFNodeTypes.LEAF, leafFoxFire())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_FOX; }
					public <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy)
					{
						Random rand = tricksy.getRandom();
						return UniformIntProvider.create(60, 120).get(rand);
					}
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_FOX.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".fox_fire_cooldown")));
						return list;
					}
				});
		set.add(GOAT_BLOCKADE = new NodeSubType<LeafNode>(ISubtypeGroup.variant("goat_blockade"), TFNodeTypes.LEAF, leafGoatBlockade())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy)
					{
						Random rand = tricksy.getRandom();
						return UniformIntProvider.create(6000, 12000).get(rand);
					}
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".goat_blockade_cooldown")));
						return list;
					}
				});
		set.add(GOAT_JUMP = new NodeSubType<LeafNode>(ISubtypeGroup.variant("goat_jump"), TFNodeTypes.LEAF, leafGoatJump(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND))
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_GOAT; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_GOAT.getName()));
						list.addAll(super.fullDescription());
						list.add(cooldownDesc(Text.translatable("info."+Reference.ModInfo.MOD_ID+".goat_jump_cooldown")));
						return list;
					}
				});
		set.add(WOLF_LEAD = new NodeSubType<LeafNode>(ISubtypeGroup.variant("wolf_lead"), TFNodeTypes.LEAF, leafWolfLead(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND))
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_WOLF; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_WOLF.getName()));
						list.addAll(super.fullDescription());
						return list;
					}
				});
		set.add(WOLF_UNLEAD = new NodeSubType<LeafNode>(ISubtypeGroup.variant("wolf_unlead"), TFNodeTypes.LEAF, leafWolfUnlead())
				{
					public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_WOLF; }
					public List<MutableText> fullDescription()
					{
						List<MutableText> list = Lists.newArrayList();
						list.add(exclusivityDesc(TFEntityTypes.TRICKSY_WOLF.getName()));
						list.addAll(super.fullDescription());
						return list;
					}
				});
		set.add(WOLF_BLESS = new NodeSubType<LeafNode>(ISubtypeGroup.variant("wolf_bless"), TFNodeTypes.LEAF, leafWolfBless(), UniformIntProvider.create(5 * Reference.Values.TICKS_PER_SECOND, 10 * Reference.Values.TICKS_PER_SECOND))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_WOLF; }
			public List<MutableText> fullDescription()
			{
				List<MutableText> list = Lists.newArrayList();
				list.add(exclusivityDesc(TFEntityTypes.TRICKSY_WOLF.getName()));
				list.addAll(super.fullDescription());
				return list;
			}
		});
		set.add(WOLF_HOWL = new NodeSubType<LeafNode>(ISubtypeGroup.variant("wolf_howl"), TFNodeTypes.LEAF, leafWolfHowl(), ConstantIntProvider.create(Reference.Values.TICKS_PER_DAY / 2))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.TRICKSY_WOLF; }
			public List<MutableText> fullDescription()
			{
				List<MutableText> list = Lists.newArrayList();
				list.add(exclusivityDesc(TFEntityTypes.TRICKSY_WOLF.getName()));
				list.addAll(super.fullDescription());
				return list;
			}
		});
		set.add(ONRYOJI_BALANCE = new NodeSubType<LeafNode>(ISubtypeGroup.variant("onryoji_balance"), TFNodeTypes.LEAF, leafOnryojiBalance(), ConstantIntProvider.create(Reference.Values.TICKS_PER_MINUTE))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.ONRYOJI; }
		});
		set.add(ONRYOJI_OFUDA = new NodeSubType<LeafNode>(ISubtypeGroup.variant("onryoji_ofuda"), TFNodeTypes.LEAF, leafOnryojiSealingOfuda(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND * 10))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.ONRYOJI; }
		});
		set.add(ONRYOJI_FOXFIRE = new NodeSubType<LeafNode>(ISubtypeGroup.variant("onryoji_fireball"), TFNodeTypes.LEAF, leafOnryojiFoxfire(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND * 7))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.ONRYOJI; }
		});
		set.add(ONRYOJI_SECLUSION = new NodeSubType<LeafNode>(ISubtypeGroup.variant("onryoji_seclusion"), TFNodeTypes.LEAF, leafOnryojiSeclusion(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND * 15))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.ONRYOJI; }
		});
		set.add(ONRYOJI_COMMANDERS = new NodeSubType<LeafNode>(ISubtypeGroup.variant("onryoji_commanders"), TFNodeTypes.LEAF, leafOnryojiCommanders(), ConstantIntProvider.create(Reference.Values.TICKS_PER_SECOND * 5))
		{
			public boolean isValidFor(EntityType<?> typeIn) { return typeIn == TFEntityTypes.ONRYOJI; }
		});
		return set;
	}
	
	public static INodeTickHandler<LeafNode> leafFoxPray()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int ANIMATION_END_TICK = (int)(1.5F * Reference.Values.TICKS_PER_SECOND);
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)),
						CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(1), Text.literal(String.valueOf(1))));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> posIn = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				if(posIn.size() == 0)
					return Result.FAILURE;
				
				BlockPos pos = posIn.get();
				if(tricksy.getWorld().getBlockState(pos).getBlock() != TFBlocks.PRESCIENT_CANDLE || tricksy.squaredDistanceTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > (INTERACT_RANGE * INTERACT_RANGE))
					return Result.FAILURE;
				
				tricksy.getLookControl().lookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
					((EntityTricksyFox)tricksy).setPraying();
				
				if(parent.ticksRunning() == ANIMATION_END_TICK)
				{
					CandlePowers candles = CandlePowers.getCandlePowers(tricksy.getServer());
					candles.setPowerFor(tricksy.getUuid(), getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT).get());
					return Result.SUCCESS;
				}
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
					((EntityTricksyFox)tricksy).clearAnimation(1);
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
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE, ActionFlag.LOOK); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> targetObj = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
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
						goat.setCharging();
						
						BlockPos goatPos = goat.getBlockPos();
						Vec3d targetVec = calculateRamTarget(ram.get().getStart(), ram.get().getEnd());
						Vec3d direction = new Vec3d((double)goatPos.getX() - targetVec.getX(), 0, (double)goatPos.getZ() - targetVec.getZ()).normalize();
						parent.nodeRAM.putDouble("X", direction.getX());
						parent.nodeRAM.putDouble("Z", direction.getZ());
						
						parent.playSoundFromEntity(null, tricksy, goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM, SoundCategory.NEUTRAL, 1.0f, tricksy.getSoundPitch());
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
						
						parent.playSoundFromEntity(null, tricksy, goat.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_RAM_IMPACT : SoundEvents.ENTITY_GOAT_RAM_IMPACT, SoundCategory.NEUTRAL, 1.0f, 1.0f);
						return Result.SUCCESS;
					}
					return Result.RUNNING;
				}
				return Result.FAILURE;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				((EntityTricksyGoat)tricksy).clearAnimation(1);
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
				return Map.of(VAL, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(false), (new WhiteboardObj.Bool(false)).describe().get(0)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
				{
					((EntityTricksyFox)tricksy).setStance(getOrDefault(VAL, parent, whiteboards).as(TFObjType.BOOL).get());
					parent.playSoundFromEntity(null, tricksy, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.NEUTRAL, 1.0f, 1.0f);
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
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_GOAT && !tricksy.hasVehicle() && tricksy.isOnGround())
				{
					if(!parent.isRunning())
					{
						((EntityTricksyGoat)tricksy).setBlockading();
						tricksy.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, DURATION, 5));
						return Result.RUNNING;
					}
					else if(parent.ticksRunning() < DURATION)
						return Result.RUNNING;
					return Result.SUCCESS;
				}
				return Result.FAILURE;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				((EntityTricksyGoat)tricksy).clearAnimation(0);
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
					
					// FIXME Resolve pathfinding break after landing
					public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
					{
						BlockPos target = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK).get();
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
									parent.playSoundFromEntity(null, tricksy, SoundEvents.ENTITY_GOAT_STEP, SoundCategory.NEUTRAL, 2.0f, 1.0f);
									
									return Result.SUCCESS;
								}
							}
							// Launch after initial windup period
							else if(parent.ticksRunning() >= (Reference.Values.TICKS_PER_SECOND * 2))
							{
								((Entity)tricksy).setYaw(((MobEntity)tricksy).bodyYaw);
								tricksy.setNoDrag(true);
								tricksy.setTreePose(EntityPose.LONG_JUMPING);
								
								for(int i=0; i<5; i++)
								{
									Random rand = tricksy.getRandom();
									double x = rand.nextDouble() - 0.5D;
									double z = rand.nextDouble() - 0.5D;
									Vec3d vel = (new Vec3d(x, 0, z)).normalize();
									((ServerWorld)tricksy.getWorld()).spawnParticles(ParticleTypes.CLOUD, start.getX(), start.getY(), start.getZ(), 1, vel.x, 0, vel.z, 0.1D);
								}
								
								Vec3d vel = new Vec3d(parent.nodeRAM.getDouble("JumpX"), parent.nodeRAM.getDouble("JumpY"), parent.nodeRAM.getDouble("JumpZ"));
								double d = vel.length();
								double e = d + tricksy.getJumpBoostVelocityModifier();
								((Entity)tricksy).setVelocity(vel.multiply(e / d));
								
								if(tricksy.getType() == TFEntityTypes.TRICKSY_GOAT)
									parent.playSoundFromEntity(null, tricksy, ((EntityTricksyGoat)tricksy).isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_LONG_JUMP : SoundEvents.ENTITY_GOAT_LONG_JUMP, SoundCategory.NEUTRAL, 1.0f, 1.0f);
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
	
	public static INodeTickHandler<LeafNode> leafFoxFire()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int SPAWN_FIRE_TICK = Reference.Values.TICKS_PER_SECOND;
			private static final int ANIMATION_END_TICK = (int)(1.5F * Reference.Values.TICKS_PER_SECOND);
			public static final BlockState FIRE = TFBlocks.FOX_FIRE.getDefaultState();
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.LOOK, ActionFlag.MOVE); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> posIn = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				if(posIn.size() == 0)
				{
					parent.logStatus(TFNodeStatus.INPUT_ERROR, Text.literal("No target"));
					return Result.FAILURE;
				}
				
				World world = tricksy.getEntityWorld();
				BlockPos pos = posIn.get();
				if(!tricksy.getBlockPos().isWithinDistance(pos, 16D) || !(FIRE.canPlaceAt(world, pos) || EntityFoxFire.canIgnite(world.getBlockState(pos))) || !INodeTickHandler.canSee(tricksy, pos.toCenterPos()))
				{
					parent.logStatus(TFNodeStatus.INPUT_ERROR);
					return Result.FAILURE;
				}
				
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
					((EntityTricksyFox)tricksy).setFoxfire();
				tricksy.getLookControl().lookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				
				if(parent.ticksRunning() == SPAWN_FIRE_TICK)
					EntityFoxFire.spawnFlameTargeting(tricksy, pos);
				
				return parent.ticksRunning() < ANIMATION_END_TICK ? Result.RUNNING : Result.SUCCESS;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_FOX)
					((EntityTricksyFox)tricksy).clearAnimation(0);
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafWolfLead()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_wolf_lead");
			private static final WhiteboardRef TARGET = CommonVariables.TARGET_ENT;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						TARGET, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(TARGET, parent, whiteboards).as(TFObjType.ENT);
				if(target.isEmpty() || !target.get().isAlive() || target.get().isSpectator() || (target.get().getType() == EntityType.PLAYER && ((PlayerEntity)target.get()).isCreative()) || !(target.get() instanceof MobEntity))
					return Result.FAILURE;
				
				MobEntity mob = (MobEntity)target.get();
				if(mob == tricksy || !INodeTickHandler.canInteractWithEntity(tricksy, mob) || !isLeashableMob(mob, tricksy))
					return Result.FAILURE;
				
				Entity holder = mob.getHoldingEntity();
				mob.attachLeash(tricksy, true);
				if(holder != null && holder.getType() == EntityType.LEASH_KNOT)
					if(tricksy.getWorld().getNonSpectatingEntities(MobEntity.class, (new Box(holder.getBlockPos())).expand(7D)).stream().noneMatch(held -> held.getHoldingEntity() == holder))
						holder.discard();
				
				return mob.isLeashed() && mob.getHoldingEntity() == tricksy ? Result.SUCCESS : Result.FAILURE;
			}
			
			private static <T extends PathAwareEntity & ITricksyMob<?>> boolean isLeashableMob(MobEntity mob, T tricksy)
			{
				if(mob.isLeashed() && mob.getHoldingEntity().getType() == EntityType.LEASH_KNOT)
					return true;
				else
					return mob.canBeLeashedBy(ServerFakePlayer.makeForMob(tricksy, BUILDER_ID));
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafWolfUnlead()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final WhiteboardRef FENCE = CommonVariables.VAR_POS;
			private static final WhiteboardRef ENTITY = CommonVariables.TARGET_ENT;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						FENCE, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock()),
						ENTITY, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				BlockPos origin = tricksy.getBlockPos();
				IWhiteboardObject<BlockPos> target = getOrDefault(FENCE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<Entity> mobList = getOrDefault(ENTITY, parent, whiteboards).as(TFObjType.ENT);
				if(!target.isEmpty())
					origin = target.get();
				
				if(!INodeTickHandler.canInteractWithBlock(tricksy, origin))
					return Result.FAILURE;
				
				if(parent.isIOAssigned(ENTITY) && !mobList.isEmpty())
					handleUnleashing(tricksy.getWorld(), origin, tricksy, mobList.getAll());
				else
					handleUnleashing(tricksy.getWorld(), origin, tricksy, tricksy.getWorld().getNonSpectatingEntities(MobEntity.class, (new Box(origin)).expand(7)));
				return Result.SUCCESS;
			}
			
			private static <C extends Entity> void handleUnleashing(World world, BlockPos origin, @Nullable Entity tricksy, List<C> mobs)
			{
				boolean isFence = world.getBlockState(origin).isIn(BlockTags.FENCES);
				LeashKnotEntity knot = null;
				for(C ent : mobs)
				{
					if(!(ent instanceof MobEntity)) continue;
					
					MobEntity mob = (MobEntity)ent;
					if(!mob.isLeashed() || mob.getHoldingEntity() != tricksy) continue;
					
					if(isFence)
					{
						if(knot == null)
						{
							knot = LeashKnotEntity.getOrCreate(world, origin);
							knot.onPlace();
						}
						mob.attachLeash(knot, true);
					}
					else
						mob.detachLeash(true, false);
				}
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafWolfBless()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int ANIMATION_END_TICK = (int)(Reference.Values.TICKS_PER_SECOND * 1.5F);
			private static final WhiteboardRef TARGET = CommonVariables.TARGET_ENT;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						TARGET, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
			}
			
			// TODO Add particles and SFX to highlight effect
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(TARGET, parent, whiteboards).as(TFObjType.ENT);
				if(target.isEmpty() || !target.get().isAlive() || target.get().isSpectator() || !(target.get() instanceof LivingEntity))
					return Result.FAILURE;
				
				LivingEntity ent = (LivingEntity)target.get();
				if(parent.ticksRunning() >= ANIMATION_END_TICK)
					return Result.SUCCESS;
				else
				{
					StatusEffectInstance blessing = new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1);
					if(parent.ticksRunning() < Reference.Values.TICKS_PER_SECOND)
					{
						if(!ent.canHaveStatusEffect(blessing) || ent.distanceTo(tricksy) > 16D || !tricksy.canSee(ent))
							return Result.FAILURE;
					}
					else if(parent.ticksRunning() == Reference.Values.TICKS_PER_SECOND)
					{
						ent.addStatusEffect(blessing, tricksy);
					}
					
					tricksy.getLookControl().lookAt(ent);
					if(!parent.isRunning() && tricksy.getType() == TFEntityTypes.TRICKSY_WOLF)
						((EntityTricksyWolf)tricksy).setBlessing();
				}
				
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_WOLF)
					((EntityTricksyWolf)tricksy).clearBlessing();
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafWolfHowl()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int ANIMATION_END_TICK = (int)(Reference.Values.TICKS_PER_SECOND * 4.0833F);
			private static final int HOWL_ISSUE_TICK = (int)(Reference.Values.TICKS_PER_SECOND * 0.9167F);
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(parent.ticksRunning() >= ANIMATION_END_TICK)
					return Result.SUCCESS;
				else if(parent.ticksRunning() == HOWL_ISSUE_TICK)
				{
					parent.playSound(tricksy, SoundEvents.ENTITY_WOLF_HOWL, 1F, tricksy.getSoundPitch());
					Howls.getHowls((ServerWorld)tricksy.getWorld()).startHowl(tricksy);
				}
				
				if(!parent.isRunning() && tricksy.getType() == TFEntityTypes.TRICKSY_WOLF)
					((EntityTricksyWolf)tricksy).setHowling();
				
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.TRICKSY_WOLF)
					((EntityTricksyWolf)tricksy).clearHowling();
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafOnryojiBalance()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.MOVE, ActionFlag.HANDS); }
			
			public int castingTime() { return Reference.Values.TICKS_PER_SECOND; }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				List<LivingEntity> mobs = tricksy.getWorld().getEntitiesByClass(LivingEntity.class, tricksy.getBoundingBox().expand(16D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
				mobs.removeIf(living -> !living.isAlive() || living.getHealth() < 1F);
				return mobs.size() >= 2;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doCasting(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
				{
					// TODO Add visual flair to telegraph effect
					((EntityOnryoji)tricksy).setAnimationBalance();
				}
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				List<LivingEntity> mobs = tricksy.getWorld().getEntitiesByClass(LivingEntity.class, tricksy.getBoundingBox().expand(16D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
				mobs.removeIf(living -> !living.isAlive() || living.getHealth() < 1F);
				
				LivingEntity mobA, mobB;
				switch(mobs.size())
				{
					case 2:
						mobA = tricksy;
						mobs.remove(tricksy);
						mobB = mobs.get(0);
						
						// Never balance with self if we'd lose health in the exchange
						if(tricksy.getHealth() >= mobB.getHealth())
							return Result.FAILURE;
						break;
					default:
						mobs.remove(tricksy);
						mobs.sort((a,b) -> a.getHealth() < b.getHealth() ? -1 : a.getHealth() > b.getHealth() ? 1 : 0);
						mobA = mobs.get(0);
						mobB = mobs.get(mobs.size() - 1);
				}
				
				int dif = Math.abs((int)mobA.getHealth() - (int)mobB.getHealth());
				if(dif == 0)
					return Result.FAILURE;
				else if(mobA.getHealth() < mobB.getHealth())
				{
					mobA.heal(dif);
					mobB.damage(tricksy.getWorld().getDamageSources().indirectMagic(tricksy, tricksy), dif);
				}
				else
				{
					mobB.heal(dif);
					mobA.damage(tricksy.getWorld().getDamageSources().indirectMagic(tricksy, tricksy), dif);
				}
				
				return Result.SUCCESS;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).clearAnimation();
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafOnryojiSealingOfuda()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final int FIRE_RATE = (int)(Reference.Values.TICKS_PER_SECOND * 0.75D);
			private static final Predicate<Entity> IS_SEALED = EntityOfudaStuck::isBoundToOfuda;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS, ActionFlag.LOOK); }
			
			public int castingTime() { return Reference.Values.TICKS_PER_SECOND; }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				return !validTargets(tricksy, Lists.newArrayList()).isEmpty();
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doCasting(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(parent.ticksRunning() == 0)
				{
					int ofuda = tricksy.getRandom().nextBetween(1, 3);
					parent.nodeRAM.putInt("Count", ofuda);
					
					if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					{
						((EntityOnryoji)tricksy).setAnimationOfuda();
						((EntityOnryoji)tricksy).setOfuda(ofuda);
					}
				}
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				List<Entity> prevTargets = previousTargets(parent.nodeRAM, tricksy);
				
				// Shooting phase
				if((parent.ticksRunning() - castingTime())%FIRE_RATE == 0)
				{
					int count = parent.nodeRAM.getInt("Count");
					if(count == 0)
						return Result.SUCCESS;
					
					List<LivingEntity> targets = validTargets(tricksy, prevTargets);
					if(!targets.isEmpty())
						for(LivingEntity target : targets)
							if(bindTarget(target, tricksy))
							{
								prevTargets.add(target);
								
								parent.nodeRAM.putInt("Count", --count);
								storeTargets(prevTargets, parent.nodeRAM);
								
								tricksy.getLookControl().lookAt(target);
								parent.logStatus(TFNodeStatus.RUNNING, Text.literal("Targeted "+target.getName().getString()));
								parent.playSound(tricksy, SoundEvents.ENTITY_SNOWBALL_THROW, 1F, tricksy.getSoundPitch());
								break;
							}
					
					if(tricksy.getType() == TFEntityTypes.ONRYOJI)
						((EntityOnryoji)tricksy).setOfuda(count);
				}
				
				if(!prevTargets.isEmpty())
				{
					Entity target = prevTargets.get(prevTargets.size() - 1);
					tricksy.getLookControl().lookAt(target);
				}
				
				// Finish after 4 seconds in shooting phase regardless of outstanding shots
				if((parent.ticksRunning() + castingTime()) > (Reference.Values.TICKS_PER_SECOND * 4))
				{
					parent.logStatus(TFNodeStatus.FAILURE, Text.literal("Couldn't find enough targets in time"));
					return Result.FAILURE;
				}
				else
					return Result.RUNNING;
			}
			
			private static List<LivingEntity> validTargets(LivingEntity tricksy, List<Entity> ignore)
			{
				List<LivingEntity> targets = EntityOnryoji.getAttackTargets(tricksy, ignore);
				
				// Don't bother targeting something that's already been sealed
				targets.removeIf(IS_SEALED.or(ent -> !tricksy.canSee(ent)));
				
				if(targets.size() > 1)
					targets.sort((a,b) -> 
					{
						double distA = a.distanceTo(tricksy);
						double distB = b.distanceTo(tricksy);
						return distA < distB ? -1 : distA > distB ? 1 : 0;
					});
				return targets;
			}
			
			private static List<Entity> previousTargets(NbtCompound nodeRam, Entity tricksy)
			{
				if(!nodeRam.contains("Targets", NbtElement.LIST_TYPE))
					return Lists.newArrayList();
				
				World world = tricksy.getWorld();
				Box bounds = tricksy.getBoundingBox().expand(45D);
				
				List<Entity> targets = Lists.newArrayList();
				NbtList list = nodeRam.getList("Targets", NbtElement.INT_ARRAY_TYPE);
				for(int i=0; i<list.size(); i++)
				{
					UUID id = NbtHelper.toUuid(list.get(i));
					List<Entity> ents = world.getEntitiesByClass(Entity.class, bounds, ent -> ent.getUuid().equals(id));
					if(!ents.isEmpty())
						targets.add(ents.get(0));
				}
				
				return targets;
			}
			
			private static void storeTargets(List<Entity> targets, NbtCompound nodeRam)
			{
				NbtList list = new NbtList();
				targets.forEach(ent -> list.add(NbtHelper.fromUuid(ent.getUuid())));
				nodeRam.put("Targets", list);
			}
			
			private boolean bindTarget(LivingEntity target, Entity tricksy)
			{
				EntityOfudaThrown ofuda = TFEntityTypes.OFUDA_THROWN.create(tricksy.getWorld());
				ofuda.setPosition(tricksy.getX(), tricksy.getEyeY() - 0.1D, tricksy.getZ());
				
				double offsetX = target.getX() - ofuda.getX();
				double offsetY = target.getBodyY(0.25D) - ofuda.getY();
				double offsetZ = target.getZ() - ofuda.getZ();
				ofuda.setVelocity(offsetX, offsetY, offsetZ, 1.6f, 0F);
				
				tricksy.getWorld().spawnEntity(ofuda);
				return true;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).clearAnimation();
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafOnryojiFoxfire()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.complementOf(EnumSet.of(ActionFlag.MOVE)); }
			
			public int castingTime() { return (int)(Reference.Values.TICKS_PER_SECOND * 1.5F); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				return !validTargets(tricksy).isEmpty();
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doCasting(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!validityCheck(tricksy, whiteboards, parent))
					return Result.FAILURE;
				
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).setAnimationFoxfire();
				
				// Periodically spawn a new fireball to shoot, up to 3 total
				List<EntityOnryojiFire> fireballs = getFireballs(tricksy, parent.nodeRAM.getList("Fireballs", NbtElement.INT_ARRAY_TYPE));
				if(parent.ticksRunning()%(castingTime() / 3) == 0 && fireballs.size() < 3)
				{
					EntityOnryojiFire fireball = TFEntityTypes.ONRYOJI_FIRE.create(tricksy.getWorld());
					fireball.setPosition(tricksy.getX(), tricksy.getY(), tricksy.getZ());
					fireball.setOwner(tricksy, fireballs.size());
					
					parent.playSound(tricksy, SoundEvents.ENTITY_BLAZE_SHOOT, 1F, tricksy.getSoundPitch());
					tricksy.getWorld().spawnEntity(fireball);
					
					fireballs.add(fireball);
					storeFireballs(fireballs, parent.nodeRAM);
				}
				
				return INodeTickHandler.super.doCasting(tricksy, whiteboards, parent);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onCast(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!validityCheck(tricksy, whiteboards, parent))
					return Result.FAILURE;
				
				List<LivingEntity> targets = validTargets(tricksy);
				List<EntityOnryojiFire> fireballs = getFireballs(tricksy, parent.nodeRAM.getList("Fireballs", NbtElement.INT_ARRAY_TYPE));
				int targetIndex = 0;
				for(EntityOnryojiFire fireball : fireballs)
					fireball.shoot(targets.get(targetIndex++%targets.size()));
				
				return Result.SUCCESS;
			}
			
			private static List<EntityOnryojiFire> getFireballs(LivingEntity tricksy, NbtList ids)
			{
				List<EntityOnryojiFire> fireballs = Lists.newArrayList();
				if(ids.isEmpty())
					return fireballs;
				
				World world = tricksy.getWorld();
				Box bounds = tricksy.getBoundingBox().expand(16D);
				for(int i=0; i<ids.size(); i++)
				{
					UUID id = NbtHelper.toUuid(ids.get(i));
					List<EntityOnryojiFire> candidates = world.getEntitiesByType(TFEntityTypes.ONRYOJI_FIRE, bounds, EntityPredicates.VALID_ENTITY.and(ent -> ent.getUuid().equals(id)));
					if(!candidates.isEmpty())
						fireballs.add(candidates.get(0));
				}
				
				return fireballs;
			}
			
			private static void storeFireballs(List<EntityOnryojiFire> fireballs, NbtCompound nbt)
			{
				NbtList list = new NbtList();
				fireballs.forEach(ent -> list.add(NbtHelper.fromUuid(ent.getUuid())));
				nbt.put("Fireballs", list);
			}
			
			private static List<LivingEntity> validTargets(LivingEntity tricksy)
			{
				List<LivingEntity> targets = EntityOnryoji.getAttackTargets(tricksy, Lists.newArrayList());
				
				targets.removeIf(ent -> !tricksy.canSee(ent));
				if(targets.size() > 1)
					targets.sort((a,b) -> 
					{
						double distA = a.distanceTo(tricksy);
						double distB = b.distanceTo(tricksy);
						return distA < distB ? -1 : distA > distB ? 1 : 0;
					});
				return targets;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).clearAnimation();
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafOnryojiSeclusion()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public static final int DURATION = Reference.Values.TICKS_PER_SECOND * 12;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public int castingTime() { return Reference.Values.TICKS_PER_SECOND * 2; }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				return tricksy.getHealth() <= (tricksy.getMaxHealth() * 0.8F);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doCasting(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!parent.isRunning() && tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).setAnimationSeclusion();
				
				return INodeTickHandler.super.doCasting(tricksy, whiteboards, parent);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onCast(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				// Spawn seclusion entity attached to mob
				EntitySeclusion zone = EntitySeclusion.makeFor(tricksy);
				zone.setPosition(tricksy.getPos());
				zone.setOwner(tricksy);
				tricksy.getWorld().spawnEntity(zone);
				
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				// Check for seclusion entity, fail if missing
				if(tricksy.getWorld().getEntitiesByType(TFEntityTypes.SECLUSION, tricksy.getBoundingBox().expand(32D), (field) -> field.getOwnerId().equals(tricksy.getUuid())).isEmpty())
				{
					parent.logStatus(TFNodeStatus.BAD_RESULT, Text.literal("Zone missing or destroyed"));
					return Result.FAILURE;
				}
				
				// Fail out of action if any damage taken
				if(tricksy.getDamageTracker().getTimeSinceLastAttack() == 0)
				{
					parent.logStatus(TFNodeStatus.FAILURE, Text.literal("Concentration broken!"));
					return Result.FAILURE;
				}
				
				if(parent.ticksRunning() > DURATION)
					return Result.SUCCESS;
				
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).clearAnimation();
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				if(tricksy.getType() == TFEntityTypes.ONRYOJI)
					((EntityOnryoji)tricksy).clearAnimation();
				
				// Kill seclusion entity if still present
				tricksy.getWorld().getEntitiesByType(TFEntityTypes.SECLUSION, tricksy.getBoundingBox().expand(32D), (field) -> field.getOwnerId().equals(tricksy.getUuid())).forEach(Entity::discard);
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafOnryojiCommanders()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public static final int RANGE = 1000;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.allOf(ActionFlag.class); }
			
			public int castingTime() { return Reference.Values.TICKS_PER_SECOND * 2; }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				return 
						tricksy.getType() == TFEntityTypes.ONRYOJI && 
						!tricksy.getWorld().getEntitiesByType(EntityType.PLAYER, tricksy.getBoundingBox().expand(32D), EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)).isEmpty();
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doCasting(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!validityCheck(tricksy, whiteboards, parent))
					return Result.FAILURE;
				
				((EntityOnryoji)tricksy).setAnimationCommanders();
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				if(!validityCheck(tricksy, whiteboards, parent))
					return Result.FAILURE;
				
				int stage = tricksy.getDataTracker().get(EntityOnryoji.COMM) + 1;
				if(stage == 12)
				{
					// Scatter
					tricksy.getDataTracker().set(EntityOnryoji.COMM, 0);
					whiteboards.local().setNodeCooldown(parent.getSubType(), Reference.Values.TICKS_PER_MINUTE * 5);
					
					BlockPos origin = tricksy.getBlockPos();
					Random rand = tricksy.getRandom();
					World world = tricksy.getWorld();
					world.getEntitiesByType(EntityType.PLAYER, tricksy.getBoundingBox().expand(32D), EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)).forEach(player -> 
					{
						int x = rand.nextBetween((int)(RANGE * 0.5D), RANGE) * (rand.nextBoolean() ? 1 : -1);
						int z = rand.nextBetween((int)(RANGE * 0.5D), RANGE) * (rand.nextBoolean() ? 1 : -1);
						BlockPos destination = origin.add(x, 0, z);
						BlockPos dest = world.getTopPosition(Type.WORLD_SURFACE, destination);
						player.teleport(dest.getX() + 0.5D, dest.getY(), dest.getZ() + 0.5D);
					});
					return Result.SUCCESS;
				}
				else
				{
					parent.logStatus(TFNodeStatus.SUCCESS, Text.literal(String.valueOf(stage)));
					tricksy.getDataTracker().set(EntityOnryoji.COMM, stage);
					return Result.SUCCESS;
				}
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				((EntityOnryoji)tricksy).clearAnimation();
			}
		};
	}
}
