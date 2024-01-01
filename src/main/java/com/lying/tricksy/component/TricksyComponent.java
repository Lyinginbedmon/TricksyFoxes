package com.lying.tricksy.component;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.init.TFAccomplishments;
import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFEnlightenmentPaths;
import com.lying.tricksy.init.TFRegistries;
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
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

/**
 * Capability-esque class that manages tracking of and conversion resulting from Tricksy enlightenment
 * @author Lying
 */
public final class TricksyComponent implements ServerTickingComponent, AutoSyncedComponent
{
	/** The mob this component is monitoring */
	private final MobEntity theMob;
	
	/** The current stage of the mob's journey of enlightenment */
	private Rank ranking = Rank.INCAPABLE;
	
	/** Registry name of the path to enlightenment taken by this mob */
	private Identifier pathID = null;
	
	/** True if the mob has been given a periapt */
	private boolean hasPeriapt = false;
	
	private int enlightening = -1;
	
	/** List of unique accomplishments */
	private List<Accomplishment> accomplishments = Lists.newArrayList();
	
	/** List of accomplishments with preconditions currently being monitored */
	private List<Accomplishment> stateAccomplishments = Lists.newArrayList();
	private Identifier lastDimension = null;
	private BlockPos enteredNetherPos;
	
	public TricksyComponent(MobEntity entityIn)
	{
		theMob = entityIn;
		this.ranking = 
				TFEnlightenmentPaths.INSTANCE.isEnlightened(theMob) ? Rank.APPRENTICE : 
				TFEnlightenmentPaths.INSTANCE.isEnlightenable(theMob) ? Rank.NASCENT : Rank.INCAPABLE;
	}
	
	public void readFromNbt(NbtCompound tag)
	{
		if(tag.contains("Rank", NbtElement.STRING_TYPE))
			setRanking(Rank.fromString(tag.getString("Rank")));
		if(tag.contains("Path", NbtElement.STRING_TYPE))
			this.pathID = new Identifier(tag.getString("Path"));
		this.hasPeriapt = tag.getBoolean("Periapt");
		this.enlightening = tag.contains("Enlightening", NbtElement.INT_TYPE) ? tag.getInt("Enlightening") : -1;
		if(tag.contains("LastDimension", NbtElement.STRING_TYPE))
			this.lastDimension = new Identifier(tag.getString("LastDimension"));
		if(tag.contains("EnteredNetherPos", NbtElement.COMPOUND_TYPE))
			this.enteredNetherPos = NbtHelper.toBlockPos(tag.getCompound("EnteredNetherPos"));
		
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
		tag.putString("Rank", ranking().asString());
		if(pathID != null)
			tag.putString("Path", pathID.toString());
		tag.putBoolean("Periapt", hasPeriapt);
		if(enlightening >= 0)
			tag.putInt("Enlightening", enlightening);
		if(this.lastDimension != null)
			tag.putString("LastDimension", this.lastDimension.toString());
		if(this.enteredNetherPos != null)
			tag.put("EnteredNetherPos", NbtHelper.fromBlockPos(enteredNetherPos));
		
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
	
	/** Returns true if this mob has further to go on its journey of enlightenment */
	public boolean canBeEnlightened() { return ranking() != Rank.INCAPABLE && ranking() != Rank.MASTER; }
	
	/** Returns true if this is an enlightened mob */
	public boolean isEnlightened()
	{
		return ranking().enlightened;
	}
	
	public boolean isMaster() { return ranking() == Rank.MASTER; }
	
	public static boolean isMobMaster(MobEntity mob) { return TFComponents.TRICKSY_TRACKING.get(mob).isMaster(); }
	
	public Rank ranking() { return this.ranking; }
	
	public void setRanking(Rank rankIn)
	{
		if(rankIn != null)
		{
			this.ranking = rankIn;
			markDirty();
		}
	}
	
	public boolean hasPeriapt() { return this.hasPeriapt; }
	
	public void setPeriapt(boolean par1)
	{
		this.hasPeriapt = par1;
		if(!par1)
			this.accomplishments.clear();
		markDirty();
	}
	
	public void markDirty() { TFComponents.TRICKSY_TRACKING.sync(theMob); }
	
	public boolean shouldTrackAccomplishments()
	{
		switch(ranking())
		{
			case NASCENT:
				return hasPeriapt();
			case APPRENTICE:
			case MASTER:
			default:
				return false;
		}
	}
	
	public void serverTick()
	{
		Identifier dimension = theMob.getWorld().getDimensionKey().getValue();
		if(!theMob.hasPortalCooldown() && !dimension.equals(this.lastDimension))
			this.lastDimension = dimension;
		
		if(shouldTrackAccomplishments())
		{
			// Accomplishments checked every tick
			TFAccomplishments.ticking().forEach(acc -> 
			{
				if(acc.achieved(theMob))
					addAccomplishment(acc);
			});
			
			// Accomplishments that look for a state change between ticks
			stateAccomplishments.forEach(acc -> 
			{
				if(acc.achieved(theMob))
					addAccomplishment(acc);
			});
			stateAccomplishments.clear();
			TFAccomplishments.stateChangeListeners().forEach(acc -> 
			{
				if(!hasAchieved(acc) && acc.preconditionsMet(theMob))
					stateAccomplishments.add(acc);
			});
		}
		
		switch(ranking())
		{
			case NASCENT:
				if(isEnlightening())
				{
					if(enlightening > 0)
						--enlightening;
					else if(enlightening == 0)
						enlighten();
				}
				else if(getPath().conditionsMet(accomplishments))
				{
					theMob.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 0));
					if(!theMob.hasPortalCooldown())
						enlightening = 300;
				}
				break;
			case APPRENTICE:
				if(getPath() != null && getPath().hasReachedMastery(accomplishments))
					setRanking(Rank.MASTER);
				break;
			case MASTER:
			default:
				return;
		}
	}
	
	public EnlightenmentPath<?, ?> getPath()
	{
		return pathID != null ? TFEnlightenmentPaths.INSTANCE.getPath(pathID) : TFEnlightenmentPaths.INSTANCE.getPath(theMob.getType());
	}
	
	public void changeFromNether(BlockPos position, RegistryKey<DimensionType> dim)
	{
		if(dim == DimensionTypes.THE_NETHER)
		{
			this.enteredNetherPos = position;
			return;
		}
		else if(dim == DimensionTypes.OVERWORLD)
		{
			double dist = this.enteredNetherPos == null ? 0D : this.enteredNetherPos.getSquaredDistance(position);
			if(!hasAchieved(TFAccomplishments.JOURNEYMAN) && dist >= (1600 * 1600) && TFAccomplishments.JOURNEYMAN.achieved(theMob))
				addAccomplishment(TFAccomplishments.JOURNEYMAN);
		}
		this.enteredNetherPos = null;
	}
	
	public boolean isEnlightening() { return enlightening >= 0; }
	
	/** Immediately increments this mob's ranking, regardless of prerequisites */
	public boolean enlighten()
	{
		World world = theMob.getWorld();
		boolean success = false;
		switch(ranking())
		{
			case APPRENTICE:
				setRanking(Rank.MASTER);
				if(!world.isClient())
					doEnlightenmentFanfare(world, theMob.getBlockPos(), theMob.getRandom());
				success = true;
				break;
			case NASCENT:
				EnlightenmentPath<?,?> pathTaken = getPath();
				if(pathTaken.resultType() == theMob.getType())
				{
					setPathTaken(pathTaken.registryName());
					success = true;
					break;
				}
				
				PathAwareEntity tricksy = pathTaken.giveEnlightenment(theMob);
				theMob.getActiveStatusEffects().forEach((effect,instance) -> tricksy.addStatusEffect(instance));
				if(theMob.hasCustomName())
					tricksy.setCustomName(theMob.getCustomName());
				tricksy.copyPositionAndRotation(theMob);
				tricksy.setPose(EntityPose.STANDING);
				TFComponents.TRICKSY_TRACKING.get(tricksy).setRanking(Rank.APPRENTICE);
				TFComponents.TRICKSY_TRACKING.get(tricksy).setPathTaken(pathTaken.registryName());
				
				// FIXME Implement proper bounding box check before enlightening
				if(!world.isClient())
				{
					world.spawnEntity(tricksy);
					theMob.discard();
				}
				success = true;
				break;
			case MASTER:
			default:
				return false;
		}
		if(success && !world.isClient())
			doEnlightenmentFanfare(world, theMob.getBlockPos(), theMob.getRandom());
		return success;
	}
	
	private static void doEnlightenmentFanfare(World world, BlockPos pos, Random rand)
	{
		// TODO Add particles to enlightenment event
		world.playSound(null, pos, TFSoundEvents.TRICKSY_ENLIGHTENED, SoundCategory.NEUTRAL, 1F, 0.75F + rand.nextFloat());
	}
	
	public void setPathTaken(Identifier id)
	{
		this.pathID = id;
		markDirty();
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
	
	public boolean grantAllAccomplishments()
	{
		TFRegistries.ACC_REGISTRY.forEach(acc -> addAccomplishment(acc, true));
		markDirty();
		return true;
	}
	
	public boolean revokeAccomplishment(Accomplishment type)
	{
		if(!hasAchieved(type))
			return false;
		this.accomplishments.remove(type);
		markDirty();
		return true;
	}
	
	public static enum Rank implements StringIdentifiable
	{
		/** Mobs who cannot be enlightened by any means */
		INCAPABLE(false),
		/** Mobs which have an enlightenment path available but have not achieved it yet */
		NASCENT(false),
		/** Mobs that have achieved enlightenment but may have further to go still */
		APPRENTICE(true),
		/** Mobs who have achieved the highest state of enlightenment available to them */
		MASTER(true);
		
		private boolean enlightened;
		
		private Rank(boolean isEnlightened)
		{
			this.enlightened = isEnlightened;
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public static Rank fromString(String nameIn)
		{
			for(Rank rank : values())
				if(rank.asString().equalsIgnoreCase(nameIn))
					return rank;
			return Rank.INCAPABLE;
		}
	}
}
