package com.lying.tricksy.entity.projectile;

import java.util.Optional;
import java.util.UUID;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityOfudaThrown extends ProjectileEntity
{
    public static final TrackedData<Optional<UUID>> TARGET = DataTracker.registerData(EntityOfudaThrown.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	
	public EntityOfudaThrown(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(TFEntityTypes.OFUDA_THROWN, world);
		setNoGravity(true);
	}
	
	protected void initDataTracker()
	{
		
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
	}
	
	public boolean canHit() { return false; }
	
	public boolean damage(DamageSource source, float amount) { return source.isOf(DamageTypes.OUT_OF_WORLD) ? super.damage(source, amount) : false; }
	
	public void tick()
	{
		super.tick();
	}
	
	protected boolean canHit(Entity entity) { return entity == null; }
	
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		super.onBlockHit(blockHitResult);
		Vec3d vel = getVelocity();
		Vec3d ref = TricksyUtils.reflect(vel, Vec3d.of(blockHitResult.getSide().getVector()));
		setVelocity(ref.normalize().multiply(vel.length()));
	}
}
