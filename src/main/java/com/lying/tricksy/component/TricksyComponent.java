package com.lying.tricksy.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFEntityTypes;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Capability-esque class that manages tracking of and conversion resulting from Tricksy enlightenment
 * @author Lying
 */
public final class TricksyComponent implements ServerTickingComponent, AutoSyncedComponent
{
	/** Map of entity types to functions that convert mobs of that type to their enlightened equivalents */
	private static final Map<EntityType<? extends MobEntity>, Function<MobEntity, MobEntity>> ENLIGHTEN_MAP = new HashMap<>();
	
	/** The mob this component is monitoring */
	private final MobEntity theMob;
	
	/** True only if the monitored mob can actually become enlightened and isn't already */
	private final boolean canEnlighten;
	
	/** True if the mob has been given a periapt */
	private boolean hasPeriapt = false;
	
	private List<Identifier> dimensionsVisited = Lists.newArrayList();
	private Identifier lastDimension = null;
	
	public TricksyComponent(MobEntity entityIn)
	{
		theMob = entityIn;
		canEnlighten = !(entityIn instanceof ITricksyMob) && ENLIGHTEN_MAP.containsKey(entityIn.getType());
	}
	
	public void readFromNbt(NbtCompound tag)
	{
		this.hasPeriapt = tag.getBoolean("Periapt");
		
		if(tag.contains("LastDimension", NbtElement.STRING_TYPE))
			this.lastDimension = new Identifier(tag.getString("LastDimension"));
		
		if(tag.contains("Dimensions", NbtElement.LIST_TYPE))
		{
			NbtList list = tag.getList("Dimensions", NbtElement.STRING_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				Identifier id = new Identifier(list.getString(i));
				if(!hasVisited(id))
				{
					this.dimensionsVisited.add(id);
					this.lastDimension = id;
				}
			}
		}
	}
	
	public void writeToNbt(NbtCompound tag)
	{
		tag.putBoolean("Periapt", hasPeriapt);
		
		if(this.lastDimension != null)
			tag.putString("LastDimension", this.lastDimension.toString());
		
		if(!this.dimensionsVisited.isEmpty())
		{
			NbtList list = new NbtList();
			for(Identifier type : this.dimensionsVisited)
				list.add(NbtString.of(type.toString()));
			tag.put("Dimensions", list);
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
			this.dimensionsVisited.clear();
		markDirty();
	}
	
	public void markDirty() { TFComponents.TRICKSY_TRACKING.sync(theMob); }
	
	public void serverTick()
	{
		if(!this.canEnlighten)
			return;
		
		if(hasPeriapt() && this.dimensionsVisited.size() > 1)
		{
			theMob.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION));
			if(!theMob.hasPortalCooldown() && theMob.getWorld().getDimensionKey().getValue().equals(this.lastDimension))
				enlighten();
		}
	}
	
	private boolean enlighten()
	{
		MobEntity tricksy = ENLIGHTEN_MAP.get(theMob.getType()).apply(theMob);
		theMob.getActiveStatusEffects().forEach((effect,instance) -> tricksy.addStatusEffect(instance));
		if(theMob.hasCustomName())
			tricksy.setCustomName(theMob.getCustomName());
		tricksy.copyPositionAndRotation(theMob);
		tricksy.setPose(EntityPose.STANDING);
		
		World world = theMob.getWorld();
		if(Dismounting.canPlaceEntityAt(world, tricksy, tricksy.getBoundingBox(EntityPose.STANDING)))
		{
			// TODO Add dramatic flair
			if(!world.isClient())
			{
				world.spawnEntity(tricksy);
				theMob.discard();
			}
			
			return true;
		}
		else
			return false;
	}
	
	public boolean hasVisited(Identifier type)
	{
		for(Identifier dim : this.dimensionsVisited)
			if(dim.toString().equals(type.toString()))
				return true;
		return false;
	}
	
	public void addVisited(Identifier type)
	{
		if(!hasPeriapt() || hasVisited(type))
			return;
		
		this.dimensionsVisited.add(type);
		this.lastDimension = type;
		markDirty();
	}
	
	static
	{
		ENLIGHTEN_MAP.put(EntityType.FOX, (mob) -> 
		{
			FoxEntity fox = (FoxEntity)mob;
			EntityTricksyFox tricksy = TFEntityTypes.TRICKSY_FOX.create(fox.getEntityWorld());
			tricksy.setVariant(fox.getVariant());
			tricksy.equipStack(EquipmentSlot.MAINHAND, fox.getEquippedStack(EquipmentSlot.MAINHAND));
			return tricksy;
		});
	}
}
