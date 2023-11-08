package com.lying.tricksy.component;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFAccomplishments;
import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFEnlightenmentPaths;
import com.lying.tricksy.init.TFSoundEvents;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Capability-esque class that manages tracking of and conversion resulting from Tricksy enlightenment
 * @author Lying
 */
public final class TricksyComponent implements ServerTickingComponent, AutoSyncedComponent
{
	/** The mob this component is monitoring */
	private final MobEntity theMob;
	
	/** True only if the monitored mob can actually become enlightened and isn't already */
	private final boolean canEnlighten;
	
	/** True if the mob has been given a periapt */
	private boolean hasPeriapt = false;
	
	private int enlightening = -1;
	
	/** List of unique accomplishments */
	private List<Accomplishment> accomplishments = Lists.newArrayList();
	private Identifier lastDimension = null;
	
	private boolean isBurning, isDrowning;
	
	public TricksyComponent(MobEntity entityIn)
	{
		theMob = entityIn;
		canEnlighten = !(entityIn instanceof ITricksyMob) && TFEnlightenmentPaths.INSTANCE.isEnlightenable(entityIn);
	}
	
	public void readFromNbt(NbtCompound tag)
	{
		this.hasPeriapt = tag.getBoolean("Periapt");
		this.enlightening = tag.getInt("Enlightening");
		if(tag.contains("LastDimension", NbtElement.STRING_TYPE))
			this.lastDimension = new Identifier(tag.getString("LastDimension"));
		
		if(tag.contains("Accomplishments", NbtElement.LIST_TYPE))
		{
			NbtList list = tag.getList("Accomplishments", NbtElement.STRING_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				Identifier id = new Identifier(list.getString(i));
				Accomplishment acc = TFAccomplishments.get(id);
				if(!hasAchieved(id) && acc != null)
					this.accomplishments.add(acc);
			}
		}
	}
	
	public void writeToNbt(NbtCompound tag)
	{
		tag.putBoolean("Periapt", hasPeriapt);
		tag.putInt("Enlightening", enlightening);
		if(this.lastDimension != null)
			tag.putString("LastDimension", this.lastDimension.toString());
		
		if(!this.accomplishments.isEmpty())
		{
			NbtList list = new NbtList();
			for(Accomplishment type : this.accomplishments)
				list.add(NbtString.of(type.registryName().toString()));
			tag.put("Accomplishments", list);
		}
	}
	
	public void cloneFrom(TricksyComponent old)
	{
		NbtCompound oldData = new NbtCompound();
		old.writeToNbt(oldData);
		readFromNbt(oldData);
		markDirty();
	}
	
	public boolean canBeEnlightened() { return this.canEnlighten; }
	
	public boolean hasPeriapt() { return this.hasPeriapt; }
	
	public void setPeriapt(boolean par1)
	{
		this.hasPeriapt = par1;
		if(!par1)
			this.accomplishments.clear();
		markDirty();
	}
	
	public void markDirty() { TFComponents.TRICKSY_TRACKING.sync(theMob); }
	
	public void serverTick()
	{
		Identifier dimension = theMob.getWorld().getDimensionKey().getValue();
		if(!theMob.hasPortalCooldown() && !dimension.equals(this.lastDimension))
			this.lastDimension = dimension;
		
		if(!this.canEnlighten || !hasPeriapt())
			return;
		
		// Accomplishments checked every tick
		for(Accomplishment acc : TFAccomplishments.PER_TICK)
			if(acc.achieved(theMob))
				addAccomplishment(acc);
		
		// Accomplishments from taking damage
		// XXX More flexible approach to accomplishments based on entity state change
		if(theMob.isOnFire())
			this.isBurning = true;
		else if(isBurning)
		{
			if(TFAccomplishments.FIRETOUCHED.achieved(theMob))
				addAccomplishment(TFAccomplishments.FIRETOUCHED);
			isBurning = false;
		}
		
		if(theMob.getAir() <= 0)
			this.isDrowning = true;
		else if(isDrowning)
		{
			if(TFAccomplishments.WATERBORNE.achieved(theMob))
				addAccomplishment(TFAccomplishments.WATERBORNE);
			isDrowning = false;
		}
		
		if(isEnlightening())
		{
			if(enlightening > 0)
				--enlightening;
			else if(enlightening == 0)
				enlighten();
		}
		else if(TFEnlightenmentPaths.INSTANCE.getPath(theMob.getType()).conditionsMet(accomplishments))
		{
			theMob.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 0));
			if(!theMob.hasPortalCooldown())
				enlightening = 300;
		}
	}
	
	public boolean isEnlightening() { return enlightening >= 0; }
	
	/** Immediately enlightens this mob, regardless of prerequisites */
	public boolean enlighten()
	{
		if(!canBeEnlightened())
			return false;
		
		PathAwareEntity tricksy = TFEnlightenmentPaths.INSTANCE.getPath(theMob.getType()).giveEnlightenment(theMob);
		theMob.getActiveStatusEffects().forEach((effect,instance) -> tricksy.addStatusEffect(instance));
		if(theMob.hasCustomName())
			tricksy.setCustomName(theMob.getCustomName());
		tricksy.copyPositionAndRotation(theMob);
		tricksy.setPose(EntityPose.STANDING);
		
		// FIXME Implement proper bounding box check before enlightening
		World world = theMob.getWorld();
		if(!world.isClient())
		{
			// TODO Add particles to enlightenment event
			world.playSound(null, theMob.getBlockPos(), TFSoundEvents.TRICKSY_ENLIGHTENED, SoundCategory.NEUTRAL, 1F, 0.75F + theMob.getRandom().nextFloat());
			world.spawnEntity(tricksy);
			theMob.discard();
		}
		return true;
	}
	
	public boolean hasAchieved(Accomplishment acc)
	{
		return hasAchieved(acc.registryName());
	}
	
	public boolean hasAchieved(Identifier type)
	{
		for(Accomplishment dim : this.accomplishments)
			if(dim.registryName().equals(type))
				return true;
		return false;
	}
	
	public List<Accomplishment> getAccomplishments() { return this.accomplishments; }
	
	public boolean addAccomplishment(Accomplishment type) { return addAccomplishment(type, hasPeriapt()); }
	
	public boolean addAccomplishment(Accomplishment type, boolean accruable)
	{
		if(!accruable || hasAchieved(type))
			return false;
		
		this.accomplishments.add(type);
		if(this.theMob != null && this.theMob.isAlive())
		{
			World world = theMob.getWorld();
			Random random = theMob.getRandom();
			world.playSound(null, theMob.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.NEUTRAL, 1F, 0.75F + random.nextFloat());
			for (int i = 0; i < 5; ++i)
			{
				double d = random.nextGaussian() * 0.02;
				double e = random.nextGaussian() * 0.02;
				double f = random.nextGaussian() * 0.02;
				world.addParticle(ParticleTypes.HAPPY_VILLAGER, theMob.getParticleX(1.0), theMob.getRandomBodyY() + 1.0, theMob.getParticleZ(1.0), d, e, f);
			}
		}
		markDirty();
		return true;
	}
	
	public boolean revokeAllAccomplishments() { this.accomplishments.clear(); markDirty(); return true; }
	
	public boolean revokeAccomplishment(Accomplishment type)
	{
		if(!hasAchieved(type))
			return false;
		this.accomplishments.remove(type);
		markDirty();
		return true;
	}
}
