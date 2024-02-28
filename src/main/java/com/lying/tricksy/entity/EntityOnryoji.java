package com.lying.tricksy.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.OnryojiTree;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
	
	public final AnimationManager<EntityOnryoji> animations = new AnimationManager<>(7);
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_BALANCE = 1;
	public static final int ANIM_OFUDA = 2;
	public static final int ANIM_FOXFIRE = 3;
	public static final int ANIM_SECLUSION = 4;
	public static final int ANIM_COMMANDERS = 5;
	public static final int ANIM_DEATH = 6;
	
	private static final TreeNode<?> TREE = OnryojiTree.get();
	private BehaviourTree behaviourTree = new BehaviourTree(TREE);
	
	@SuppressWarnings("unchecked")
	protected LocalWhiteboard<EntityOnryoji> boardLocal = (LocalWhiteboard<EntityOnryoji>)(new OnryojiTree.OnryojiWhiteboard(this)).build();
	
	public EntityOnryoji(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		animations.start(0, this.age);
		this.moveControl = new FlightMoveControl(this, 20, true);	// FIXME Replace move control with direct travel
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
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 300).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7f).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64D);
	}
	
	public boolean hasNoGravity() { return true; }
	
	public void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) { }
	
	protected EntityNavigation createNavigation(World world)
	{
		BirdNavigation birdNavigation = new BirdNavigation(this, world)
				{
					public boolean isValidPosition(BlockPos pos)
					{
						for(int i=3; i>0; i--)
							if(!world.getBlockState(pos.down(i)).isAir())
								return false;
						return true;
					}
				};
		birdNavigation.setCanPathThroughDoors(true);
		birdNavigation.setCanSwim(false);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}
	
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
	
	public void tick()
	{
		super.tick();
		if(!hasCustomer() && !isAiDisabled())
			ITricksyMob.updateBehaviourTree(this);
	}
	
	public static List<LivingEntity> getAttackTargets(LivingEntity tricksy, List<Entity> ignore)
	{
		List<LivingEntity> targets = Lists.newArrayList();
		
		World world = tricksy.getWorld();
		Box bounds = tricksy.getBoundingBox().expand(tricksy.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
		
		if(tricksy.getAttacking() != null && EntityPredicates.VALID_ENTITY.test(tricksy.getAttacking()))
			targets.add(tricksy.getAttacking());
		
		if(tricksy.getAttacker() != null && EntityPredicates.VALID_ENTITY.test(tricksy.getAttacker()))
			targets.add(tricksy.getAttacker());
		
		targets.addAll(world.getEntitiesByType(EntityType.PLAYER, bounds, EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)));
		targets.addAll(world.getEntitiesByType(EntityType.VILLAGER, bounds, EntityPredicates.VALID_ENTITY));
		
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
				case 6:
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
			case ANIM_OFUDA:		return EnumSet.of(BipedPart.BODY, BipedPart.LEFT_ARM, BipedPart.RIGHT_ARM);
			case ANIM_BALANCE:
			case ANIM_FOXFIRE:
			case ANIM_SECLUSION:
			case ANIM_COMMANDERS:
						return EnumSet.allOf(BipedPart.class);
			default:	return EnumSet.noneOf(BipedPart.class);
		}
	}
}
