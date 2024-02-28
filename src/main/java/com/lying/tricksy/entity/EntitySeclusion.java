package com.lying.tricksy.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySeclusion extends Entity
{
    public static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(EntitySeclusion.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> AGE = DataTracker.registerData(EntitySeclusion.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Optional<UUID>> OWNER = DataTracker.registerData(EntitySeclusion.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private Entity boundOwner = null;
    
    public static final double RANGE = 6D;
    public static final int DEFAULT_LIFESPAN = Reference.Values.TICKS_PER_SECOND * 10;
    
	public EntitySeclusion(EntityType<? extends EntitySeclusion> entityType, World world)
	{
		super(TFEntityTypes.SECLUSION, world);
		this.setNoGravity(true);
		this.setInvulnerable(true);
	}
	
	protected void initDataTracker()
	{
		getDataTracker().startTracking(AGE, 0);
		getDataTracker().startTracking(LIFESPAN, -1);
		getDataTracker().startTracking(OWNER, Optional.empty());
	}
	
	public static EntitySeclusion makeFor(Entity owner)
	{
		EntitySeclusion seclusion = TFEntityTypes.SECLUSION.create(owner.getWorld());
		seclusion.setOwner(owner);
		seclusion.getDataTracker().set(LIFESPAN, DEFAULT_LIFESPAN);
		return seclusion;
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		nbt.putInt("Age", getDataTracker().get(AGE).intValue());
		if(lifespan() >= 0)
			nbt.putInt("Lifespan", lifespan());
		if(hasOwner())
			nbt.putUuid("Owner", getOwnerId());
		if(this.isInvisible())
			nbt.putBoolean("Invisible", true);
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		getDataTracker().set(AGE, nbt.getInt("Age"));
		if(nbt.contains("Lifespan", NbtElement.INT_TYPE))
			getDataTracker().set(LIFESPAN, nbt.getInt("Lifespan"));
		if(nbt.contains("Owner", NbtElement.INT_ARRAY_TYPE))
			setOwner(nbt.getUuid("Owner"));
		setInvisible(nbt.getBoolean("Invisible"));
	}
	
	public double currentRange() { return RANGE * Math.min(1F, age() / Reference.Values.TICKS_PER_SECOND); }
	
	public void tick()
	{
		super.tick();
		
		handleRepulsion(currentRange());
		if(getWorld().isClient())
			tickClient();
		else
			tickServer();
	}
	
	protected void tickServer()
	{
		getDataTracker().set(AGE, age() + 1);
		
		if(hasOwner())
		{
			Optional<Entity> optOwner = getOwner();
			if(optOwner.isEmpty())
				return;
			
			Entity bound = optOwner.get();
			if(!bound.isAlive())
				discard();
			updatePosition(bound);
			
			if(bound instanceof LivingEntity)
				((LivingEntity)bound).addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, Reference.Values.TICKS_PER_SECOND, 1));
		}
		
		int lifespan = lifespan();
		if(!getWorld().isClient() && lifespan >= 0)
		{
			if(lifespan == 0)
			{
				discard();
				return;
			}
			else
				getDataTracker().set(LIFESPAN, lifespan - 1);
		}
	}
	
	protected void tickClient()
	{
		double range = currentRange();
		if(range < 1D)
			return;
		
		if(random.nextInt(5) == 0)
			for(int i=this.random.nextBetween(2, 4); i>0; --i)
			{
				Vec3d pos = getPos().add(new Vec3d(this.random.nextDouble() - 0.5D, 0D, this.random.nextDouble() - 0.5D).normalize().multiply(this.random.nextDouble() * range * 0.8D));
				getWorld().addParticle(TFParticles.ENERGY, pos.x, pos.y + 0.01D, pos.z, 255, 255, 255);
			}
	}
	
	protected void handleRepulsion(double radius)
	{
		Predicate<Entity> isOwner = this::isOwner;
		Vec3d origin = getPos();
		Box bounds = getBoundingBox().expand(radius);
		
		getWorld().getEntitiesByClass(ProjectileEntity.class, bounds, EntityPredicates.VALID_ENTITY.and(isOwner.negate())).forEach(arrow -> 
		{
			Vec3d pos = arrow.getPos();
			Vec3d nextPos = pos.add(arrow.getVelocity());
			if(pos.distanceTo(origin) > nextPos.distanceTo(origin))
				arrow.setVelocity(arrow.getVelocity().multiply(-0.5D));
		});
		
		getWorld().getEntitiesByClass(LivingEntity.class, bounds, EntityPredicates.VALID_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).and(isOwner.negate())).forEach(mob -> 
		{
			Vec3d offset = origin.subtract(mob.getPos());
			if(offset.length() < RANGE)
			{
				Vec3d push = offset.normalize().multiply(Math.pow(offset.length() - RANGE, 3D)).multiply(0.05D);
				mob.addVelocity(push);
			}
		});
	}
	
	public boolean isOwner(Entity ent) { return hasOwner() && ent.getUuid().equals(getOwnerId()); }
	
	public int age() { return getDataTracker().get(AGE).intValue(); }
	
	public int lifespan() { return getDataTracker().get(LIFESPAN).intValue(); }
	
	public boolean collidesWith(Entity other) { return false; }
	
	private void updatePosition(Entity owner) { setPosition(owner.getPos()); }
	
	public boolean isInvulnerableTo(DamageSource damageSource)
	{
		return damageSource != getWorld().getDamageSources().outOfWorld() && super.isInvulnerableTo(damageSource);
	}
	
	protected boolean shouldSetPositionOnLoad() { return false; }
	
	public boolean isPushable() { return false; }
	
	public void setOwner(Entity ent)
	{
		setOwner(ent.getUuid());
		updatePosition(ent);
	}
	
	public void setOwner(UUID uuid) { getDataTracker().set(OWNER, uuid == null ? Optional.empty() : Optional.of(uuid)); }
	
	public boolean hasOwner() { return getDataTracker().get(OWNER).isPresent(); }
	
	public UUID getOwnerId() { return hasOwner() ? getDataTracker().get(OWNER).get() : null; }
	
	public boolean isOwner(UUID uuidIn) { return hasOwner() && getOwnerId().equals(uuidIn); }
	
	public Optional<Entity> getOwner()
	{
		if(!hasOwner())
			return Optional.empty();
		
		if(boundOwner == null)
		{
			List<Entity> candidates = getWorld().getEntitiesByClass(Entity.class, getBoundingBox().expand(32D), EntityPredicates.VALID_ENTITY.and(this::isOwner));
			if(candidates.isEmpty())
				return Optional.empty();
			else
				return Optional.of(boundOwner = candidates.get(0));
		}
		
		return Optional.of(boundOwner);
	}
}
