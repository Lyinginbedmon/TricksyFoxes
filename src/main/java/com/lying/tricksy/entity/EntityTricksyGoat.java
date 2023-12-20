package com.lying.tricksy.entity;

import java.util.EnumSet;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityTricksyGoat extends AbstractTricksyAnimal implements IAnimatedBiped
{
    private static final TrackedData<Integer> ANIMATING = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.INTEGER);
    /**
     * 0 - Blockading
     * 1 - Charging
     */
    public final AnimationManager<EntityTricksyGoat> animations = new AnimationManager<>(2);
	
	public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.changing(0.8F, 1.85F).scaled(0.7f);
	public static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.8F, 1.4F);
    private static final TrackedData<Boolean> SCREAMING = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LEFT_HORN = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_HORN = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
    
    private static final UUID BLOCKADE_KNOCKBACK_ID = UUID.fromString("ce647cae-a269-4b88-970d-49a32e51cb73");
    private static final EntityAttributeModifier BLOCKADE_KNOCKBACK_BUFF = new EntityAttributeModifier(BLOCKADE_KNOCKBACK_ID, "Blockading knockback buff", 1.0, EntityAttributeModifier.Operation.ADDITION);
    
	public EntityTricksyGoat(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_GOAT, world);
		getNavigation().setCanSwim(true);
		setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1F);
		setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1F);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
        this.getDataTracker().startTracking(SCREAMING, false);
        this.getDataTracker().startTracking(LEFT_HORN, true);
        this.getDataTracker().startTracking(RIGHT_HORN, true);
        this.getDataTracker().startTracking(ANIMATING, -1);
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return GoatEntity.createGoatAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
	}
	
	public int getMaxHeadRotation() { return 15; }
	
	public void setHeadYaw(float headYaw)
	{
		int i = getMaxHeadRotation();
		float f = MathHelper.subtractAngles(this.bodyYaw, headYaw);
		float g = MathHelper.clamp(f, (float)-i, (float)i);
		super.setHeadYaw(this.bodyYaw + g);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		setScreaming(data.getBoolean("IsScreamingGoat"));
		setHorns(data.getBoolean("HasLeftHorn"), data.getBoolean("HasRightHorn"));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		data.putBoolean("IsScreamingGoat", isScreaming());
		data.putBoolean("HasLeftHorn", hasLeftHorn());
		data.putBoolean("HasRightHorn", hasRightHorn());
	}
	
	public EntityDimensions getDimensions(EntityPose pose)
	{
		switch(pose)
		{
			case LONG_JUMPING:	return LONG_JUMPING_DIMENSIONS;
			case SITTING:		return SLEEPING_DIMENSIONS;
			default:	return super.getDimensions(pose);
		}
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
			GoatEntity child = EntityType.GOAT.create(arg0);
			child.getDataTracker().set(SCREAMING, ((GoatEntity)arg1).isScreaming() || arg0.getRandom().nextDouble() < 0.02D);
			return child;
		}
		
		return null;
	}
	
	public int getDefaultColor() { return 9647415; }
	
	public boolean hasLeftHorn() { return getDataTracker().get(LEFT_HORN).booleanValue(); }
	
	public boolean hasRightHorn() { return getDataTracker().get(RIGHT_HORN).booleanValue(); }
	
	public void setHorns(boolean left, boolean right)
	{
		getDataTracker().set(LEFT_HORN, left);
		getDataTracker().set(RIGHT_HORN, right);
	}
	
	public boolean isScreaming() { return getDataTracker().get(SCREAMING).booleanValue(); }
	
	public void setScreaming(boolean par1Bool) { getDataTracker().set(SCREAMING, par1Bool); }
	
	@Nullable
	protected SoundEvent getAmbientSound() { return isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT : SoundEvents.ENTITY_GOAT_AMBIENT; }
	
	@Nullable
	protected SoundEvent getHurtSound(DamageSource source) { return isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_HURT : SoundEvents.ENTITY_GOAT_HURT; }
	
	@Nullable
	protected SoundEvent getDeathSound() { return isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_DEATH : SoundEvents.ENTITY_GOAT_DEATH; }
	
	public void playSoundForBark(Bark bark)
	{
		switch(bark)
		{
			case HAPPY:
				this.playSound(isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT : SoundEvents.ENTITY_GOAT_AMBIENT, 1F, 1F);
				break;
			case CURIOUS:
				break;
			case CONFUSED:
				break;
			case ALERT:
				this.playSound(isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM, 5F, 1F);
				break;
			case NONE:
			default:
				break;
		}
	}
	
	protected int computeFallDamage(float fallDistance, float damageMultiplier) { return super.computeFallDamage(fallDistance, damageMultiplier) - 10; }
	
	public void tick()
	{
		super.tick();
		this.animations.tick(this);
		EntityAttributeInstance attribute = this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if(isBlockading() != attribute.hasModifier(BLOCKADE_KNOCKBACK_BUFF))
			if(isBlockading())
				attribute.addTemporaryModifier(BLOCKADE_KNOCKBACK_BUFF);
			else
				attribute.removeModifier(BLOCKADE_KNOCKBACK_BUFF);
	}
	
	public boolean isBlockading() { return this.getDataTracker().get(ANIMATING).intValue() == 0; }
	
	public void setBlockading() { this.getDataTracker().set(ANIMATING, 0); }
	
	public void setCharging() { this.getDataTracker().set(ANIMATING, 1); }
	
	public void clearAnimation(int index)
	{
		if(index < 0 || this.animations.currentAnim() == index)
			this.getDataTracker().set(ANIMATING, this.animations.stopAll());
	}
	
	public boolean isCollidable() { return isBlockading() && isAlive() || super.isCollidable(); }
	
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
		switch(this.animations.currentAnim())
		{
			case 1:	return EnumSet.of(BipedPart.LEFT_ARM, BipedPart.LEFT_LEG, BipedPart.RIGHT_ARM, BipedPart.RIGHT_LEG);
			case 0:	return EnumSet.allOf(BipedPart.class);
			default:	return IAnimatedBiped.super.getPartsAnimating();
		}
	}
}
