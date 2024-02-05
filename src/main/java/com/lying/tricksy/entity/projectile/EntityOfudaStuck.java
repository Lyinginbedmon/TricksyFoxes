package com.lying.tricksy.entity.projectile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import com.lying.tricksy.block.BlockOfuda;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
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
    		Direction.UP, new Box(-WID, 0, -WID, WID, DEPTH, WID),
    		Direction.DOWN, new Box(-WID, 0, -WID, WID, DEPTH, WID),
    		Direction.NORTH, new Box(-WID, 0, 0, WID, HEIGHT, -DEPTH),
    		Direction.SOUTH, new Box(-WID, 0, 0, WID, HEIGHT, DEPTH),
    		Direction.EAST, new Box(0, 0, -WID, DEPTH, HEIGHT, WID),
    		Direction.WEST, new Box(0, 0, -WID, -DEPTH, HEIGHT, WID)
    		);
    
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
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 3D);
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
		{
			setYaw(0);
			setPitch(face == Direction.UP ? -90F : 90F);
		}
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
	
	@SuppressWarnings("deprecation")
	public boolean hasNoGravity()
	{
		return super.hasNoGravity() || TFBlocks.OFUDA.canPlaceAt(TFBlocks.OFUDA.getDefaultState().with(BlockOfuda.FACING, getFacing()), getEntityWorld(), getBlockPos());
	}
	
	public boolean isInvulnerableTo(DamageSource damageSource)
	{
		return 
				damageSource == getWorld().getDamageSources().inWall() ||
				damageSource == getWorld().getDamageSources().drown() ||
				damageSource == getWorld().getDamageSources().fall();
	}
	
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions)
	{
		return getFacing().getAxis() == Axis.Y ? 0.03125F : 0.4F;
	}
	
	public void calculateDimensions() { }
	
	protected boolean shouldSetPositionOnLoad() { return false; }
	
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
	
	public Iterable<ItemStack> getArmorItems() { return DefaultedList.ofSize(0); }
	
	public ItemStack getEquippedStack(EquipmentSlot var1) { return ItemStack.EMPTY; }
	
	public void equipStack(EquipmentSlot var1, ItemStack var2) { }
	
	public Arm getMainArm() { return Arm.RIGHT; }
}
