package com.lying.tricksy.entity.projectile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.lying.tricksy.block.BlockOfuda;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/** Placed by {@link EntityOfudaThrown} on impact with a solid block */
public class EntityOfudaStuck extends LivingEntity
{
    public static final TrackedData<Direction> FACING = DataTracker.registerData(EntityOfudaStuck.class, TrackedDataHandlerRegistry.FACING);
    public static final TrackedData<Optional<UUID>> TARGET = DataTracker.registerData(EntityOfudaStuck.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    
    public static final float WIDTH = 6F / 16F;
    protected static final float WID = WIDTH * 0.5F;
    public static final float HEIGHT = 10F / 16F;
    public static final float DEPTH = 1F / 16F;
    private static final Map<Direction, Box> BOUNDS = Map.of(
    		Direction.UP, new Box(-WID, -0.001, -WID, WID, DEPTH, WID),
    		Direction.DOWN, new Box(-WID, -0.001, -WID, WID, DEPTH, WID),
    		Direction.NORTH, new Box(-WID, 0, 0.001, WID, HEIGHT, -DEPTH),
    		Direction.SOUTH, new Box(-WID, 0, -0.001, WID, HEIGHT, DEPTH),
    		Direction.EAST, new Box(-0.001, 0, -WID, DEPTH, HEIGHT, WID),
    		Direction.WEST, new Box(0.001, 0, -WID, -DEPTH, HEIGHT, WID)
    		);
    
    private Entity boundTarget = null;
    
	public EntityOfudaStuck(EntityType<? extends EntityOfudaStuck> entityType, World world)
	{
		super(TFEntityTypes.OFUDA_STUCK, world);
	}
	
	protected void initDataTracker()
	{
		super.initDataTracker();
		getDataTracker().startTracking(FACING, Direction.UP);
		getDataTracker().startTracking(TARGET, Optional.empty());
		updateFacing(Direction.UP);
	}
	
	public static DefaultAttributeContainer.Builder createOfudaAttributes()
	{
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 3D).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1D);
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putString("Facing", getFacing().asString());
		if(hasTarget())
			nbt.putUuid("Target", getTargetId());
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		setFacing(Direction.byName(nbt.getString("Facing")));
		if(nbt.contains("Target", NbtElement.INT_ARRAY_TYPE))
			setTarget(nbt.getUuid("Target"));
	}
	
	public void setFacing(Direction face)
	{
		Validate.notNull(face);
		getDataTracker().set(FACING, face);
	}
	
	public void updateFacing(Direction face)
	{
		Validate.notNull(face);
		if(face.getAxis() == Axis.Y)
			setPitch(face == Direction.UP ? -90F : 90F);
		else
		{
			setPitch(0);
			setYaw(face.getHorizontal() * 90F);
		}
		this.prevYaw = this.getYaw();
		double e = getX();
		double f = getY();
		double g = getZ();
		setPos(e, f, g);
		
		setBoundingBox(BOUNDS.getOrDefault(getFacing(), new Box(-0.25, 0, 0, 0.25, 0.5, -0.25)).offset(getPos()));
	}
	
	public void tick()
	{
		super.tick();
		
		if(!isStuck() && getFacing() != Direction.UP)
			setFacing(Direction.UP);
		
		if(hasTarget())
		{
			Optional<Entity> target = getTarget();
			if(target.isEmpty())
				return;
			
			Entity bound = target.get();
			if(!bound.isAlive())
				setTarget(null);
			else if(getWorld().isClient() && bound instanceof LivingEntity || bound instanceof PlayerEntity)
			{
				Vec3d origin = getPos();
				Vec3d dest = bound.getPos();
				Vec3d offset = origin.subtract(dest);
				if(offset.length() > 6D)
				{
					setTarget(null);
					this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1F, getSoundPitch());
					return;
				}
				else if(offset.length() > 0.5D)
					bound.addVelocity(offset.normalize().multiply(offset.length() * 0.05D));
				
				if(this.age%Reference.Values.TICKS_PER_SECOND == 0)
				{
					((LivingEntity)bound).addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, Reference.Values.TICKS_PER_SECOND * 3, 0, false, false));
					((LivingEntity)bound).addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, Reference.Values.TICKS_PER_SECOND * 3));
				}
			}
		}
	}
	
	public boolean hasNoGravity()
	{
		return super.hasNoGravity() || isStuck();
	}
	
	/** Returns true if a stuck ofuda within 20 blocks of the entity is bound to it */
	public static boolean isBoundToOfuda(Entity ent)
	{
		return ent.getWorld().getEntitiesByType(TFEntityTypes.OFUDA_STUCK, ent.getBoundingBox().expand(20D), EntityPredicates.VALID_ENTITY.and(ofuda -> ((EntityOfudaStuck)ofuda).hasTarget())).stream()
				.anyMatch(ofuda -> ofuda.getTargetId().equals(ent.getUuid()));
	}
	
	public boolean isStuck()
	{
		BlockState state = TFBlocks.OFUDA.getDefaultState().with(BlockOfuda.FACING, getFacing());
		if(state.canPlaceAt(getWorld(), getBlockPos()))
		{
			Box box = getBoundingBox();
			if(getFacing().getAxis().getType() == Type.HORIZONTAL)
				box = box.expand(0.01D, 0D, 0.01D);
			else
				box = box.expand(0D, 0.01D, 0D);
			
			List<VoxelShape> blockCollisions = Lists.newArrayList();
			getWorld().getBlockCollisions(this, box).forEach(shape -> blockCollisions.add(shape));
			return !blockCollisions.isEmpty();
		}
		return false;
	}
	
	public boolean isInvulnerableTo(DamageSource damageSource)
	{
		DamageSources sources = getWorld().getDamageSources();
		return 
				damageSource == sources.inWall() ||
				damageSource == sources.drown() ||
				damageSource == sources.fall();
	}
	
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions)
	{
		return getFacing().getAxis() == Axis.Y ? 0.03125F : 0.4F;
	}
	
	public void calculateDimensions() { }
	
	protected boolean shouldSetPositionOnLoad() { return false; }
	
	public boolean collidesWith(Entity other) { return false; }
	
	public boolean isPushable() { return false; }
	
	public void pushAwayFrom(Entity entity) { }
	
	public void pushAway(Entity entity) { }
	
	public boolean isMobOrPlayer() { return false; }
	
	public void setPosition(double x, double y, double z)
	{
		super.setPosition(x, y, z);
		this.updateFacing(getFacing());
		this.velocityDirty = true;
	}
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(FACING.equals(data))
			updateFacing(getFacing());
		super.onTrackedDataSet(data);
	}
	
	public Direction getFacing() { return getDataTracker().get(FACING); }
	
	public void setTarget(UUID uuid) { getDataTracker().set(TARGET, uuid == null ? Optional.empty() : Optional.of(uuid)); }
	
	public boolean hasTarget() { return getDataTracker().get(TARGET).isPresent(); }
	
	public UUID getTargetId() { return hasTarget() ? getDataTracker().get(TARGET).get() : null; }
	
	public Optional<Entity> getTarget()
	{
		if(boundTarget == null)
		{
			List<Entity> candidates = getWorld().getEntitiesByClass(Entity.class, getBoundingBox().expand(32D), EntityPredicates.VALID_ENTITY.and(ent -> ent.getUuid().equals(getTargetId())));
			if(candidates.isEmpty())
				return Optional.empty();
			else
				return Optional.of(boundTarget = candidates.get(0));
		}
		else if(boundTarget.isAlive())
			return Optional.of(boundTarget);
		return Optional.empty();
	}
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(0); }
	
	public ItemStack getEquippedStack(EquipmentSlot var1) { return ItemStack.EMPTY; }
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
}
