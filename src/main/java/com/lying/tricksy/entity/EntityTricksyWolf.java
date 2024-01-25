package com.lying.tricksy.entity;

import java.util.EnumSet;

import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class EntityTricksyWolf extends AbstractTricksyAnimal implements IAnimatedBiped
{
	/**
	 * 0 - Bless
	 * 1 - Howl
	 */
    public final AnimationManager<EntityTricksyWolf> animations = new AnimationManager<>(2);
    
	public EntityTricksyWolf(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_WOLF, world);
		// TODO Auto-generated constructor stub
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(ANIMATING, -1);
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return WolfEntity.createWolfAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
	}
	
	public PassiveEntity createChild(ServerWorld var1, PassiveEntity var2)
	{
		/**
		 * Tricksy mobs never create tricksy offspring, only their nascent form.
		 * Everyone has to find enlightenment on their own.
		 */
		if(var2.getType() == getType())
		{
			WolfEntity child = EntityType.WOLF.create(var1);
			return child;
		}
		
		return null;
	}
	
	public int getDefaultColor() { return 0x313030; }
	
	public void tick()
	{
		super.tick();
		this.animations.tick(this);
	}
	
	@Override
	protected SoundEvent getAmbientSound()
	{
//		if (this.hasAngerTime()) {
//			return SoundEvents.ENTITY_WOLF_GROWL;
//		}
//		if (this.random.nextInt(3) == 0) {
//			if (this.isTamed() && this.getHealth() < 10.0f) {
//				return SoundEvents.ENTITY_WOLF_WHINE;
//			}
//			return SoundEvents.ENTITY_WOLF_PANT;
//		}
		return SoundEvents.ENTITY_WOLF_AMBIENT;
	}
	
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_WOLF_HURT; }
	
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_WOLF_DEATH; }
	
	protected float getSoundVolume() { return 0.4f; }
	
	public void setBlessing() { this.getDataTracker().set(ANIMATING, 0); }
	
	public void setHowling() { this.getDataTracker().set(ANIMATING, 1); }
	
	public void clearBlessing() { clearAnimation(0); }
	
	public void clearHowling() { clearAnimation(1); }
	
	public void clearAnimation(int index)
	{
		if(index < 0 || this.animations.currentAnim() == index)
			this.getDataTracker().set(ANIMATING, this.animations.stopAll());
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
		switch(this.animations.currentAnim())
		{
			case 0:	return EnumSet.complementOf(EnumSet.of(BipedPart.LEFT_LEG, BipedPart.RIGHT_LEG));
			case 1:	return EnumSet.allOf(BipedPart.class);
			default:	return IAnimatedBiped.super.getPartsAnimating();
		}
	}
}
