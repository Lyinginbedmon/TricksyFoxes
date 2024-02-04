package com.lying.tricksy.entity.projectile;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityOnryojiFire extends ProjectileEntity
{
	public final AnimationState animation_idle = new AnimationState();
	
	public EntityOnryojiFire(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(TFEntityTypes.ONRYOJI_FIRE, world);
		setNoGravity(true);
		this.animation_idle.startIfNotRunning(this.age);
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
		if(getWorld().isClient())
		{
			Vec3d lookVec = getRotationVec(0F);
			Vec3d pos = new Vec3d(this.getParticleX(0.3F), this.getRandomBodyY(), this.getParticleZ(0.3F)).subtract(lookVec.multiply(0.3D));
			getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
		}
		
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
