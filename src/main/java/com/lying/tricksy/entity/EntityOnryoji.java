package com.lying.tricksy.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.OnryojiTree;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;
import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.init.TFSoundEvents;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EntityOnryoji extends HostileEntity implements ITricksyMob<EntityOnryoji>, IAnimatedBiped
{
	public static final TrackedData<Integer> ANIMATING = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<NbtCompound> LOG_NBT = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<EntityPose> TREE_POSE = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.ENTITY_POSE);
	public static final TrackedData<Integer> BARK = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> OFUDA = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> COMM = DataTracker.registerData(EntityOnryoji.class, TrackedDataHandlerRegistry.INTEGER);
	
	private final ServerBossBar bossBar = (ServerBossBar)new ServerBossBar(this.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
	
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_BALANCE = 1;
	public static final int ANIM_OFUDA = 2;
	public static final int ANIM_FOXFIRE = 3;
	public static final int ANIM_SECLUSION = 4;
	public static final int ANIM_COMMANDERS = 5;
	// TODO Implement death animation
	public static final int ANIM_DEATH = 6;
	public final AnimationManager<EntityOnryoji> animations = new AnimationManager<>(7)
		{
			public void onUpdateAnim(int animation, int ticksRunning, EntityOnryoji ent)
			{
				Random rand = ent.getRandom();
				switch(animation)
				{
					case ANIM_IDLE:
						break;
					case ANIM_BALANCE:
						break;
					case ANIM_OFUDA:
						if(ticksRunning == 15)
							ent.playSound(SoundEvents.ENTITY_FOX_SCREECH, ent.getSoundVolume(), ent.getSoundPitch());
						break;
					case ANIM_FOXFIRE:
						if(ticksRunning == 10)
							ent.playSound(SoundEvents.ENTITY_FOX_SCREECH, ent.getSoundVolume(), ent.getSoundPitch());
						else if(ticksRunning == 29)
							ent.playSound(TFSoundEvents.SNAP, ent.getSoundVolume(), ent.getSoundPitch());
						break;
					case ANIM_SECLUSION:
						if(ticksRunning == 10)
							ent.playSound(TFSoundEvents.CLAP, ent.getSoundVolume(), ent.getSoundPitch());
						else if(ticksRunning == 15)
							ent.playSound(SoundEvents.ENTITY_FOX_SCREECH, ent.getSoundVolume(), ent.getSoundPitch());
						
						if(ticksRunning < 15 && rand.nextInt(8) == 0)
							ent.getWorld().addParticle(TFParticles.ENERGY_EMITTER, ent.getX(), ent.getEyeY() + 0.5D, ent.getZ(), 255, 255, 255);
						break;
					case ANIM_COMMANDERS:
						if(ticksRunning == 9)
							ent.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, ent.getSoundVolume(), ent.getSoundPitch());
						else if(ticksRunning == 28)
							ent.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, ent.getSoundVolume(), ent.getSoundPitch());
						
						if(ticksRunning == 30)
							ent.getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER, ent.getX(), ent.getEyeY() + 0.5D, ent.getZ(), 0, 0, 0);
						break;
					case ANIM_DEATH:
						if(ticksRunning == 0)
							ent.playSound(SoundEvents.ENTITY_ENDER_DRAGON_DEATH, ent.getSoundVolume(), ent.getSoundPitch());
						break;
					default:
						return;
				}
			}
		};
	
	private static final TreeNode<?> TREE = OnryojiTree.get();
	private BehaviourTree behaviourTree = new BehaviourTree(TREE);
	
	@SuppressWarnings("unchecked")
	protected LocalWhiteboard<EntityOnryoji> boardLocal = (LocalWhiteboard<EntityOnryoji>)(new OnryojiTree.OnryojiWhiteboard(this)).build();
	
	public EntityOnryoji(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		animations.start(0, this.age);
		this.moveControl = new OnryojiMoveControl(this);
		this.experiencePoints = 50;
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(ANIMATING, 0);
		this.getDataTracker().startTracking(OFUDA, 0);
		this.getDataTracker().startTracking(COMM, 0);
		
		this.getDataTracker().startTracking(TREE_NBT, TREE.write(new NbtCompound()));
		this.getDataTracker().startTracking(LOG_NBT, new NbtCompound());
		this.getDataTracker().startTracking(TREE_POSE, EntityPose.STANDING);
		this.getDataTracker().startTracking(BARK, Bark.NONE.ordinal());
	}
	
	public static DefaultAttributeContainer.Builder createOnryojiAttributes()
	{
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 500).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7f).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64D);
	}
	
	public void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) { }
	
	protected EntityNavigation createNavigation(World world)
	{
		BirdNavigation birdNavigation = new BirdNavigation(this, world)
				{
					public boolean isValidPosition(BlockPos pos) { return world.getFluidState(pos).isEmpty() && world.getBlockState(pos).getCollisionShape(world, pos).isEmpty(); }
				};
		birdNavigation.setCanPathThroughDoors(true);
		birdNavigation.setCanSwim(true);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}
	
	protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_FOX_SCREECH; }
	
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_FOX_HURT; }
	
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_FOX_DEATH; }
	
	public ItemStack getStack(int slot) { return getEquippedStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot)); }
	
	public void setStack(int slot, ItemStack stack) { equipStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot), stack); }
	
	public boolean canPlayerUse(PlayerEntity var1) { return false; }
	
	public Optional<UUID> getSage() { return Optional.empty(); }
	
	public void setSage(@Nullable UUID uuidIn) { }
	
	public int getColor() { return 0; }
	
	public boolean hasColor() { return false; }
	
	public BehaviourTree getBehaviourTree() { return getWorld().isClient() ? BehaviourTree.create(getDataTracker().get(TREE_NBT)) : this.behaviourTree; }
	
	public void setLatestLog(NodeStatusLog logIn) { this.getDataTracker().set(LOG_NBT, logIn.writeToNbt(new NbtCompound())); }
	
	public NodeStatusLog getLatestLog() { return NodeStatusLog.fromNbt(this.getDataTracker().get(LOG_NBT)); }
	
	public LocalWhiteboard<EntityOnryoji> getLocalWhiteboard() { return this.boardLocal; }
	
	public GlobalWhiteboard getGlobalWhiteboard() { return new GlobalWhiteboard(getEntityWorld()); }
	
	public void setBehaviourTree(NbtCompound data) { }
	
	public void giveCommand(OrderWhiteboard command) { }
	
	public boolean hasCustomer() { return false; }
	
	public void setCustomer(@Nullable PlayerEntity player) { }
	
	public void setTreePose(EntityPose pose)
	{
		getDataTracker().set(TREE_POSE, pose);
		setPose(pose);
	}
	
	public EntityPose getTreePose() { return this.getDataTracker().get(TREE_POSE); }
	
	public Inventory getMainInventory() { return this; }
	
	public boolean canUsePortals() { return false; }
	
	public boolean canStartRiding(Entity entity) { return false; }
	
	public EntityGroup getGroup() { return EntityGroup.UNDEAD; }
	
	public void tick()
	{
		this.noClip = true;
		super.tick();
		this.noClip = false;
		this.setNoGravity(true);
		if(!hasCustomer() && !isAiDisabled())
			ITricksyMob.updateBehaviourTree(this);
		this.animations.tick(this);
	}
	
	public void mobTick()
	{
		super.mobTick();
		this.bossBar.setPercent(getHealth() / getMaxHealth());
	}
	
	protected void updatePostDeath()
	{
		setAnimationDeath();
		super.updatePostDeath();
	}
	
	public void onStartedTrackingBy(ServerPlayerEntity player)
	{
		super.onStartedTrackingBy(player);
		this.bossBar.addPlayer(player);
	}
	
	public void onStoppedTrackingBy(ServerPlayerEntity player)
	{
		super.onStoppedTrackingBy(player);
		this.bossBar.removePlayer(player);
	}
	
	public static List<LivingEntity> getAttackTargets(LivingEntity tricksy, List<Entity> ignore)
	{
		return getAttackTargets(tricksy, ignore, Predicates.alwaysTrue());
	}
	
	/**
	 * Returns a list of viable attack targets for this mob
	 * @param tricksy The mob to center the search on, excluded from the resulting list
	 * @param ignore A list of entities to ignore in the search
	 * @param predicate A predicate to apply to valid returned entities, according to usage context
	 * @return
	 */
	public static List<LivingEntity> getAttackTargets(@NotNull LivingEntity tricksy, @NotNull List<Entity> ignore, @NotNull Predicate<Entity> predicate)
	{
		Predicate<Entity> fullPredicate = EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).and(ent -> !ent.equals(tricksy)).and(predicate);
		List<LivingEntity> targets = Lists.newArrayList();
		
		World world = tricksy.getWorld();
		double range = tricksy.getAttributes().hasAttribute(EntityAttributes.GENERIC_FOLLOW_RANGE) ? tricksy.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE) : 32D;
		Box bounds = tricksy.getBoundingBox().expand(range);
		
		LivingEntity attacking = tricksy.getAttacking();
		LivingEntity attacker = tricksy.getAttacker();
		if(attacking != null && fullPredicate.test(attacking))
			targets.add(attacking);
		
		if(attacker != null && attacker != attacking && fullPredicate.test(attacker))
			targets.add(attacker);
		
		world.getEntitiesByType(EntityType.PLAYER, bounds, fullPredicate).forEach(ent -> { if(!targets.contains(ent)) targets.add(ent); });
		world.getEntitiesByClass(MobEntity.class, bounds, fullPredicate).forEach(ent -> { if(!targets.contains(ent)) targets.add(ent); });
		
		if(!ignore.isEmpty())
			targets.removeIf(ent -> ignore.contains(ent));
		
		return targets;
	}
	
	public void clearAnimation() { this.getDataTracker().set(ANIMATING, ANIM_IDLE); }
	public void setAnimationBalance() { this.getDataTracker().set(ANIMATING, ANIM_BALANCE); }
	public void setAnimationOfuda() { this.getDataTracker().set(ANIMATING, ANIM_OFUDA); }
	public void setAnimationFoxfire() { this.getDataTracker().set(ANIMATING, ANIM_FOXFIRE); }
	public void setAnimationSeclusion() { this.getDataTracker().set(ANIMATING, ANIM_SECLUSION); }
	public void setAnimationCommanders() { this.getDataTracker().set(ANIMATING, ANIM_COMMANDERS); }
	public void setAnimationDeath() { this.getDataTracker().set(ANIMATING, ANIM_DEATH); }
	
	public void setOfuda(int count) { this.getDataTracker().set(OFUDA, count); }
	public int getCommanders() { return this.getDataTracker().get(COMM); }
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(ANIMATING.equals(data))
			switch(getDataTracker().get(ANIMATING).intValue())
			{
				case -1:
				case ANIM_IDLE:
					this.animations.stopAll();
					this.animations.start(0, this.age);
					break;
				case ANIM_BALANCE:
				case ANIM_OFUDA:
				case ANIM_FOXFIRE:
				case ANIM_SECLUSION:
				case ANIM_COMMANDERS:
				case ANIM_DEATH:
				default:
					this.animations.stopAll();
					this.animations.start(getDataTracker().get(ANIMATING), this.age);
					break;
			}
	}
	
	public EnumSet<BipedPart> getPartsAnimating()
	{
		switch(this.animations.currentAnim())
		{
			case ANIM_IDLE:		return EnumSet.of(BipedPart.BODY, BipedPart.LEFT_ARM, BipedPart.RIGHT_ARM, BipedPart.HEAD);
			case ANIM_OFUDA:	return EnumSet.of(BipedPart.BODY, BipedPart.LEFT_ARM, BipedPart.RIGHT_ARM);
			case ANIM_BALANCE:
			case ANIM_FOXFIRE:
			case ANIM_SECLUSION:
			case ANIM_COMMANDERS:
						return EnumSet.allOf(BipedPart.class);
			default:	return EnumSet.noneOf(BipedPart.class);
		}
	}
	
	public class OnryojiMoveControl extends MoveControl
	{
		public OnryojiMoveControl(EntityOnryoji owner) { super(owner); }
		
		public void tick()
		{
			if(this.state != MoveControl.State.MOVE_TO)
				return;
			
			MobEntity mob = this.entity;
			Vec3d target = new Vec3d(this.targetX, this.targetY, this.targetZ);
			Vec3d offset = new Vec3d(target.x - mob.getX(), target.y - mob.getY(), target.z - mob.getZ());
			double dist = offset.length();
			if(dist < 0.1D)
			{
				this.state = MoveControl.State.WAIT;
				mob.setVelocity(mob.getVelocity().multiply(0.5D));
			}
			else
				mob.setVelocity(offset.normalize().multiply(this.speed * dist * 0.5D));
		}
	}
}
