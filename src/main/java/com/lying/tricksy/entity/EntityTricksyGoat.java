package com.lying.tricksy.entity;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
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
import net.minecraft.world.World;

public class EntityTricksyGoat extends AbstractTricksyAnimal
{
    private static final TrackedData<Boolean> SCREAMING = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LEFT_HORN = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_HORN = DataTracker.registerData(EntityTricksyGoat.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public EntityTricksyGoat(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_GOAT, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
        this.getDataTracker().startTracking(SCREAMING, false);
        this.getDataTracker().startTracking(LEFT_HORN, true);
        this.getDataTracker().startTracking(RIGHT_HORN, true);
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return GoatEntity.createGoatAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
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
	
	public boolean isTreeSleeping() { return false; }
	
	public void setTreeSleeping(boolean var) { }
	
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
//				this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1F, 1F);
				break;
			case CONFUSED:
//				this.playSound(SoundEvents.ENTITY_FOX_SCREECH, 2F, 1F);
				break;
			case ALERT:
				this.playSound(isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_PREPARE_RAM : SoundEvents.ENTITY_GOAT_PREPARE_RAM, 5F, 1F);
				break;
			case NONE:
			default:
				break;
		}
	}
}
