package com.lying.tricksy.entity.projectile;

import com.lying.tricksy.init.TFDamageTypes;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityOnryojiFire extends PersistentProjectileEntity
{
    public static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(EntityOnryojiFire.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> FUSE = DataTracker.registerData(EntityOnryojiFire.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> SHOT = DataTracker.registerData(EntityOnryojiFire.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    public static final int LIFE_TIME = Reference.Values.TICKS_PER_SECOND * 7;
    public static final int FUSE_TIME = Reference.Values.TICKS_PER_SECOND * 2;
    
	public final AnimationState animation_idle = new AnimationState();
	public final AnimationState animation_fuse = new AnimationState();
	
	private int index = 1;
	
	public EntityOnryojiFire(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(TFEntityTypes.ONRYOJI_FIRE, world);
		setNoGravity(true);
		this.animation_idle.startIfNotRunning(this.age);
	}
	
	protected void initDataTracker()
	{
		super.initDataTracker();
		getDataTracker().startTracking(SHOT, false);
		getDataTracker().startTracking(LIFESPAN, 0);
		getDataTracker().startTracking(FUSE, -1);
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("Shot", getDataTracker().get(SHOT));
		nbt.putInt("Index", this.index);
		nbt.putInt("Lifespan", getDataTracker().get(LIFESPAN));
		if(ignited())
			nbt.putInt("Fuse", ignitedTicks());
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		getDataTracker().set(SHOT, nbt.getBoolean("Shot"));
		this.index = nbt.getInt("Index");
		getDataTracker().set(LIFESPAN, nbt.getInt("Lifespan"));
		if(nbt.contains("Fuse", NbtElement.INT_TYPE))
		{
			ignite();
			getDataTracker().set(FUSE, nbt.getInt("Fuse"));
		}
	}
	
	public boolean canHit() { return false; }
	
	public boolean damage(DamageSource source, float amount) { return source.isOf(DamageTypes.OUT_OF_WORLD) ? super.damage(source, amount) : false; }
	
	public void tick()
	{
		if(getWorld().isClient())
		{
			if(wasShot())
			{
				Vec3d lookVec = getRotationVec(0F);
				Vec3d pos = new Vec3d(this.getParticleX(0.3F), this.getRandomBodyY(), this.getParticleZ(0.3F)).subtract(lookVec.multiply(0.3D));
				getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
			}
			
			if(ignited() && this.age%4 == 0)
			{
				int particles = ignitedTicks() / 5;
				Vec3d core = this.getPos().add(0D, getHeight() * 0.5D,  0D);
				double vel = 0.25D;
				for(int i=particles; i>0; --i)
				{
					double velX = (this.random.nextDouble() - 0.5D) * vel;
					double velY = (this.random.nextDouble() - 0.5D) * vel;
					double velZ = (this.random.nextDouble() - 0.5D) * vel;
					getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, core.x, core.y, core.z, velX, velY, velZ);
				}
			}
		}
		else
		{
			if(ignited())
			{
				int fuse = ignitedTicks();
				if(fuse < FUSE_TIME)
					getDataTracker().set(FUSE, fuse + 1);
				else
					explode();
			}
			else
			{
				// Despawn after 7 seconds of not hitting anything
				int lifespan = getDataTracker().get(LIFESPAN) + 1;
				if(wasShot() && lifespan > LIFE_TIME || lifespan > (LIFE_TIME * 3))
					discard();
				else
					getDataTracker().set(LIFESPAN, lifespan);
				
				if(wasShot())
					super.tick();
				else
				{
					Entity owner = getOwner();
					if(owner == null)
						return;
					
					setRotation(owner.getYaw(), 0F);
					updatePosition(owner);
				}
			}
		}
	}
	
	public void setOwner(Entity ownerIn, int indexIn)
	{
		super.setOwner(ownerIn);
		this.index = indexIn;
		if(ownerIn == null)
			return;
		setRotation(ownerIn.getYaw(), 0F);
		updatePosition(ownerIn);
		this.refreshPosition();
	}
	
	private void updatePosition(Entity owner)
	{
		Vec3d offset = new Vec3d(0, owner.getBodyY(1.2D) - owner.getY(), 0);
		switch(this.index % 3)
		{
			case 0:
				offset = offset.rotateZ((float)Math.toRadians(45D));
				break;
			case 1:
				break;
			case 2:
				offset = offset.rotateZ((float)Math.toRadians(-45D));
				break;
		}
		Vec3d pos = owner.getPos().add(offset.rotateY((float)Math.toRadians(owner.getYaw() - 90F)));
		this.setPos(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public boolean wasShot() { return getDataTracker().get(SHOT).booleanValue(); }
	
	public void shoot(LivingEntity target)
	{
		getDataTracker().set(LIFESPAN, 0);
		getDataTracker().set(SHOT, true);
		
		double offsetX = target.getX() - getX();
		double offsetY = target.getBodyY(0.5D) - getY();
		double offsetZ = target.getZ() - getZ();
		setVelocity(offsetX, offsetY, offsetZ, 0.8f, 0F);
	}
	
	public void explode()
	{
		playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1F, 0.6F + random.nextFloat() * 0.4F);
		for(LivingEntity hit : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(3D), EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)))
			explode(hit);
		
		discard();
	}
	
	public void explode(LivingEntity hit)
	{
		if(hit.damage(getWorld().getDamageSources().inFire(), 2F))
			hit.setOnFireFor(this.random.nextBetween(3, 5));
		hit.damage(TFDamageTypes.of(getWorld(), TFDamageTypes.FOXFIRE), 4F);
	}
	
	/** Returns the number of ticks since this entity ignited, or -1 if it hasn't */
	public int ignitedTicks() { return getDataTracker().get(FUSE); }
	
	public boolean ignited() { return ignitedTicks() >= 0; }
	
	public void ignite()
	{
		getDataTracker().set(FUSE, 0);
		setVelocity(Vec3d.ZERO);
		playSound(SoundEvents.ENTITY_TNT_PRIMED, 1F, 0.5F + random.nextFloat());
	}
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(FUSE.equals(data))
			if(ignited())
			{
				this.animation_idle.stop();
				this.animation_fuse.startIfNotRunning(this.age);
			}
			else
			{
				this.animation_idle.startIfNotRunning(this.age);
				this.animation_fuse.stop();
			}
	}
	
	public void setVelocity(Vec3d velocity)
	{
		if(ignited() || !wasShot())
			super.setVelocity(Vec3d.ZERO);
		else
			super.setVelocity(velocity);
	}
	
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		ignite();
	}
	
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		if(entityHitResult.getEntity() == getOwner())
			return;
		else if(entityHitResult.getEntity() instanceof LivingEntity)
			explode();
		else
			ignite();
	}
	
	protected ItemStack asItemStack() { return ItemStack.EMPTY; }
}
