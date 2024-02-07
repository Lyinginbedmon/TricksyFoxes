package com.lying.tricksy.entity.projectile;

import com.lying.tricksy.init.TFDamageTypes;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.AnimationState;
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
    
    public static final int FUSE_TIME = Reference.Values.TICKS_PER_SECOND * 2;
    
	public final AnimationState animation_idle = new AnimationState();
	public final AnimationState animation_fuse = new AnimationState();
	
	public EntityOnryojiFire(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(TFEntityTypes.ONRYOJI_FIRE, world);
		setNoGravity(true);
		this.animation_idle.startIfNotRunning(this.age);
	}
	
	protected void initDataTracker()
	{
		super.initDataTracker();
		getDataTracker().startTracking(LIFESPAN, Reference.Values.TICKS_PER_SECOND * 7);
		getDataTracker().startTracking(FUSE, -1);
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Lifespan", getDataTracker().get(LIFESPAN));
		if(ignited())
			nbt.putInt("Fuse", fuseTicks());
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
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
			Vec3d lookVec = getRotationVec(0F);
			Vec3d pos = new Vec3d(this.getParticleX(0.3F), this.getRandomBodyY(), this.getParticleZ(0.3F)).subtract(lookVec.multiply(0.3D));
			getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
			
			if(ignited())
			{
				int timeLit = FUSE_TIME - fuseTicks();
				Vec3d core = this.getPos().add(0D, getHeight() * 0.5D,  0D);
				double vel = 0.25D;
				for(int i=timeLit/5; i>0; --i)
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
				int fuse = fuseTicks();
				if(fuse > 0)
					getDataTracker().set(FUSE, fuse - 1);
				else
					explode();
			}
			else
			{
				// Despawn after 7 seconds of not hitting anything
				int lifespan = getDataTracker().get(LIFESPAN) - 1;
				if(lifespan < 0)
					discard();
				else
					getDataTracker().set(LIFESPAN, lifespan);
				
				super.tick();
			}
		}
	}
	
	public void explode()
	{
		for(LivingEntity hit : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(3D), EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)))
			explode(hit);
		
		playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1F, 0.6F + random.nextFloat() * 0.4F);
		discard();
	}
	
	public void explode(LivingEntity hit)
	{
		// FIXME Apply at least 1 damage type that bypasses damage cooldown ticks
		if(hit.damage(getWorld().getDamageSources().inFire(), 2F))
			hit.setOnFireFor(5);
		hit.damage(TFDamageTypes.of(getWorld(), TFDamageTypes.FOXFIRE), 4F);
	}
	
	public int fuseTicks() { return getDataTracker().get(FUSE); }
	
	public boolean ignited() { return fuseTicks() >= 0; }
	
	public void ignite()
	{
		getDataTracker().set(FUSE, FUSE_TIME);
		setVelocity(Vec3d.ZERO);
		
		this.animation_idle.stop();
		this.animation_fuse.start(this.age);
		playSound(SoundEvents.ENTITY_TNT_PRIMED, 1F, 0.5F + random.nextFloat());
	}
	
	public void setVelocity(Vec3d velocity)
	{
		if(ignited())
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
		if(entityHitResult.getEntity() instanceof LivingEntity)
			explode();
		else
			ignite();
	}
	
	protected ItemStack asItemStack() { return ItemStack.EMPTY; }
}
