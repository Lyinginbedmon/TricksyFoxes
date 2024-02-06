package com.lying.tricksy.entity.projectile;

import java.util.Optional;
import java.util.UUID;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class EntityOfudaThrown extends ThrownEntity
{
    public static final TrackedData<Optional<UUID>> TARGET = DataTracker.registerData(EntityOfudaThrown.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(EntityOfudaThrown.class, TrackedDataHandlerRegistry.INTEGER);
	
	public EntityOfudaThrown(EntityType<? extends EntityOfudaThrown> entityType, World world)
	{
		super(TFEntityTypes.OFUDA_THROWN, world);
		this.setNoGravity(true);
	}
	
	protected void initDataTracker()
	{
		getDataTracker().startTracking(LIFESPAN, Reference.Values.TICKS_PER_SECOND * 15);
		getDataTracker().startTracking(TARGET, Optional.empty());
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Lifespan", getDataTracker().get(LIFESPAN));
		if(hasTarget())
			nbt.putUuid("Target", getTargetID());
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		getDataTracker().set(LIFESPAN, nbt.getInt("Lifespan"));
		if(nbt.contains("Target", NbtElement.INT_ARRAY_TYPE))
			setTarget(nbt.getUuid("Target"));
	}
	
	public boolean hasTarget() { return getDataTracker().get(TARGET).isPresent(); }
	
	public void setTarget(UUID uuid) { getDataTracker().set(TARGET, uuid == null ? Optional.empty() : Optional.of(uuid)); }
	
	public UUID getTargetID() { return getDataTracker().get(TARGET).get(); }
	
	public boolean canHit() { return false; }
	
	public boolean damage(DamageSource source, float amount) { return source.isOf(DamageTypes.OUT_OF_WORLD) ? super.damage(source, amount) : false; }
	
	public void tick()
	{
		super.tick();
		
		int ticks = getDataTracker().get(LIFESPAN);
		if(--ticks <= 0)
			discard();
		else
			getDataTracker().set(LIFESPAN, ticks);
	}
	
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		Entity hit = entityHitResult.getEntity();
		if(!hasTarget() && hit instanceof LivingEntity && ((LivingEntity)hit).isMobOrPlayer())
			setTarget(hit.getUuid());
	}
	
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		super.onBlockHit(blockHitResult);
		EntityOfudaStuck ofuda = TFEntityTypes.OFUDA_STUCK.create(getWorld());
		Direction face = blockHitResult.getSide();
		ofuda.setFacing(face);
		if(face.getAxis() == Axis.Y)
			ofuda.setYaw(this.getYaw());
		if(face == Direction.DOWN)
			ofuda.setPosition(blockHitResult.getPos().add(0, face.getOffsetY() * ofuda.getBoundingBox().getYLength(), 0));
		else
			ofuda.setPosition(blockHitResult.getPos().add(face.getOffsetX() * 0.001D, face.getOffsetY() * 0.001D, face.getOffsetZ() * 0.001D));
		if(hasTarget())
			ofuda.setTarget(getTargetID());
		
		getWorld().spawnEntity(ofuda);
		discard();
	}
}
