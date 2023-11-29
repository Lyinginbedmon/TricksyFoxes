package com.lying.tricksy.entity;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FoxEntity.Type;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class EntityTricksyFox extends AbstractTricksyAnimal implements VariantHolder<Type>
{
	private static final TrackedData<Integer> TYPE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> SLEEPING = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(TYPE, 0);
		this.getDataTracker().startTracking(SLEEPING, false);
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return FoxEntity.createFoxAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		setVariant(Type.byName(data.getString("Type")));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.putString("Type", this.getVariant().asString());
	}
	
	@Nullable
	public PassiveEntity createChild(ServerWorld arg0, PassiveEntity arg1)
	{
		/**
		 * Tricksy mobs never create tricksy offspring, only their nascent form.
		 * Everyone has to find enlightenment on their own.
		 */
		if(arg1.getType() == getType())
		{
			FoxEntity child = EntityType.FOX.create(arg0);
			child.setVariant(this.random.nextBoolean() ? this.getVariant() : ((FoxEntity)arg1).getVariant());
			return child;
		}
		
		return null;
	}
	
	public boolean isSleeping() { return this.getDataTracker().get(SLEEPING).booleanValue(); }
	
	public void setSleeping(boolean var)
	{
		this.getDataTracker().set(SLEEPING, var);
		if(var)
			setPose(EntityPose.SITTING);
		else
			setPose(EntityPose.STANDING);
	}
	
	public EntityDimensions getDimensions(EntityPose pose)
	{
		switch(pose)
		{
			case SITTING:	return EntityDimensions.fixed(super.getDimensions(pose).width, 0.7F);
			default:
				return super.getDimensions(pose);
		}
	}
	
	public void setVariant(Type type) { this.dataTracker.set(TYPE, type.getId()); }
	
	public Type getVariant() { return Type.fromId(this.dataTracker.get(TYPE)); }
	
	@Nullable
	protected SoundEvent getAmbientSound()
	{
		if(this.isSleeping())
			return SoundEvents.ENTITY_FOX_SLEEP;
		if(!this.getWorld().isDay() && this.random.nextFloat() < 0.1f && getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR).isEmpty())
			return SoundEvents.ENTITY_FOX_SCREECH;
		return SoundEvents.ENTITY_FOX_AMBIENT;
	}
	
	@Nullable
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_FOX_HURT; }
	
	@Nullable
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_FOX_DEATH; }
	
	public void bark(Bark bark)
	{
		super.bark(bark);
		if(bark == null)
			bark = Bark.NONE;
		switch(bark)
		{
			case HAPPY:
				this.playSound(SoundEvents.ENTITY_FOX_AMBIENT, 1F, 1F);
				break;
			case CURIOUS:
				this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1F, 1F);
				break;
			case CONFUSED:
				this.playSound(SoundEvents.ENTITY_FOX_SCREECH, 2F, 1F);
				break;
			case ALERT:
				this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 5F, 1F);
				break;
			case NONE:
			default:
				break;
		}
	}
}
