package com.lying.tricksy.entity;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.block.BlockFoxFire;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.reference.Reference;

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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EntityTricksyFox extends AbstractTricksyAnimal implements VariantHolder<Type>, IAnimatedBiped
{
    /**
     * 0 - Foxfire
     * 1 - Praying
     */
	public final AnimationManager<EntityTricksyFox> animations = new AnimationManager<>(2)
	{
		public void onUpdateAnim(int animation, int ticksRunning, EntityTricksyFox ent)
		{
			Random rand = ent.getRandom();
			switch(animation)
			{
				case 0:
					int snap = (int)(0.58F * Reference.Values.TICKS_PER_SECOND);
					int flameStart = (int)(0.63F * Reference.Values.TICKS_PER_SECOND);
					if(ticksRunning == snap)
						ent.playSound(TFSoundEvents.SNAP, getSoundVolume(), getSoundPitch());
					if(ticksRunning > flameStart && ticksRunning < Reference.Values.TICKS_PER_SECOND && rand.nextInt(3) == 0)
					{
						double vel = 0.03D;
						double velX = (rand.nextDouble() * (rand.nextBoolean() ? 1 : -1)) * vel;
						double velY = (rand.nextDouble() * (rand.nextBoolean() ? 1 : -1)) * vel;
						double velZ = (rand.nextDouble() * (rand.nextBoolean() ? 1 : -1)) * vel;
						ent.getWorld().addParticle(BlockFoxFire.PARTICLE, ent.getX(), ent.getEyeY() + 0.5D, ent.getZ(), velX, velY, velZ);
					}
					break;
				case 1:
					int clap1 = (int)(1.125F * Reference.Values.TICKS_PER_SECOND);
					int clap2 = (int)(1.333F * Reference.Values.TICKS_PER_SECOND);
					if(ticksRunning == clap1 || ticksRunning == clap2)
						ent.playSound(TFSoundEvents.CLAP, getSoundVolume(), getSoundPitch());
					break;
				default:
					return;
			}
		}
	};
	
	private static final TrackedData<Integer> TYPE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> STANCE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(TYPE, 0);
		this.getDataTracker().startTracking(STANCE, false);
		this.getDataTracker().startTracking(ANIMATING, -1);
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return FoxEntity.createFoxAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		setVariant(Type.byName(data.getString("Type")));
		setStance(data.getBoolean("Stance"));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.putString("Type", this.getVariant().asString());
		data.putBoolean("Stance", currentStance());
	}
	
	public void tick()
	{
		super.tick();
		if(isSprinting() != currentStance())
			setSprinting(currentStance());
		
		this.animations.tick(this);
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
	
	public EntityDimensions getDimensions(EntityPose pose)
	{
		switch(pose)
		{
			case SITTING:	return EntityDimensions.fixed(super.getDimensions(pose).width, 0.7F);
			case CROUCHING:	return EntityDimensions.fixed(0.6F, 0.7F);
			default:
				return super.getDimensions(pose);
		}
	}
	
	public void setVariant(Type type) { this.dataTracker.set(TYPE, type.getId()); }
	
	public Type getVariant() { return Type.fromId(this.dataTracker.get(TYPE)); }
	
	public int getDefaultColor() { return 12779520; }
	
	@Nullable
	protected SoundEvent getAmbientSound()
	{
		if(this.isTreeSleeping())
			return SoundEvents.ENTITY_FOX_SLEEP;
		if(!this.getWorld().isDay() && this.random.nextFloat() < 0.1f && getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR).isEmpty())
			return SoundEvents.ENTITY_FOX_SCREECH;
		return SoundEvents.ENTITY_FOX_AMBIENT;
	}
	
	@Nullable
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_FOX_HURT; }
	
	@Nullable
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_FOX_DEATH; }
	
	public void playSoundForBark(Bark bark)
	{
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
	
	public void setStance(boolean bool) { getDataTracker().set(STANCE, bool); }
	
	public boolean currentStance() { return this.getDataTracker().get(STANCE).booleanValue(); }
	
	public EntityPose defaultPose() { return currentStance() ? EntityPose.CROUCHING : EntityPose.STANDING; }
	
	public void setFoxfire() { this.getDataTracker().set(ANIMATING, 0); }
	
	public void setPraying() { this.getDataTracker().set(ANIMATING, 1); }
	
	public void clearAnimation(int index)
	{
		if(index < 0 || this.animations.currentAnim() == index)
			this.getDataTracker().set(ANIMATING, this.animations.stopAll());
	}
	
	public EnumSet<ActionFlag> getActiveFlags()
	{
		EnumSet<ActionFlag> flags = super.getActiveFlags();
		if(currentStance())
			flags.add(ActionFlag.HANDS);
		return flags;
	}
	
	public void onTrackedDataSet(TrackedData<?> data)
	{
		if(ANIMATING.equals(data))
			switch(getDataTracker().get(ANIMATING).intValue())
			{
				case -1:
					this.animations.stopAll();
					break;
				default:
					this.animations.start(getDataTracker().get(ANIMATING), this.age);
					break;
			}
	}
	
	public EnumSet<BipedPart> getPartsAnimating()
	{
		if(this.animations.currentAnim() >= 0)
			return EnumSet.allOf(BipedPart.class);
		return IAnimatedBiped.super.getPartsAnimating();
	}
}
