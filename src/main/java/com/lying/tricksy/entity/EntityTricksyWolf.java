package com.lying.tricksy.entity;

import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class EntityTricksyWolf extends AbstractTricksyAnimal
{
	public EntityTricksyWolf(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_WOLF, world);
		// TODO Auto-generated constructor stub
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

}
