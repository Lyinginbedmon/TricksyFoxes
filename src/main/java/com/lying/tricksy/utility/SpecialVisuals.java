package com.lying.tricksy.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.tricksy.init.TFSpecialVisual;
import com.lying.tricksy.network.SyncSpecialVisualsPacket;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class SpecialVisuals extends PersistentState
{
	private Map<UUID, List<ActiveVisual>> activeVisuals = new HashMap<>();
	private World world = null;
	
	public static SpecialVisuals getVisuals(ServerWorld world)
	{
		PersistentStateManager manager = world.getServer().getWorld(World.OVERWORLD).getPersistentStateManager();
		SpecialVisuals visuals = manager.getOrCreate(SpecialVisuals::createFromNbt, SpecialVisuals::new, Reference.ModInfo.MOD_ID + ":special_visuals");
		visuals.setWorld(world);
		visuals.markDirty();
		return visuals;
	}
	
	public void setWorld(World world) { this.world = world; }
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		long currentTime = world.getTime();
		NbtList set = new NbtList();
		for(Entry<UUID, List<ActiveVisual>> entry : activeVisuals.entrySet())
		{
			NbtCompound data = new NbtCompound();
			data.putUuid("UUID", entry.getKey());
			NbtList visuals = new NbtList();
			entry.getValue().forEach(visual -> 
			{
				if(!visual.isActive(currentTime))
					return;
				
				visuals.add(visual.writeToNbt());
			});
			
			if(!visuals.isEmpty())
			{
				data.put("Visuals", visuals);
				set.add(data);
			}
		}
		nbt.put("Data", set);
		return nbt;
	}
	
	public void readFromNbt(NbtCompound nbt)
	{
		activeVisuals.clear();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound data = set.getCompound(i);
			UUID uuid = data.getUuid("UUID");
			List<ActiveVisual> visualsOnEntity = Lists.newArrayList();
			NbtList visualList = data.getList("Visuals", NbtElement.COMPOUND_TYPE);
			for(int j=0; j<visualList.size(); j++)
				visualsOnEntity.add(ActiveVisual.fromNbt(visualList.getCompound(j)));
			
			activeVisuals.put(uuid, visualsOnEntity);
		}
	}
	
	public static SpecialVisuals createFromNbt(NbtCompound nbt)
	{
		SpecialVisuals visuals = new SpecialVisuals();
		visuals.readFromNbt(nbt);
		return visuals;
	}
	
	public boolean hasActiveVisuals(Entity entity)
	{
		return hasActiveVisuals(entity.getUuid());
	}
	
	public boolean hasActiveVisuals(UUID uuid)
	{
		if(activeVisuals.containsKey(uuid))
		{
			long currentTime = world.getTime();
			return activeVisuals.get(uuid).stream().anyMatch(visual -> visual.isActive(currentTime));
		}
		return false;
	}
	
	public boolean hasVisual(UUID uuid, TFSpecialVisual visual)
	{
		return activeVisuals.containsKey(uuid) ? activeVisuals.getOrDefault(uuid, Lists.newArrayList()).stream().anyMatch(active -> active.visual() == visual) : false;
	}
	
	public List<ActiveVisual> getActiveVisuals(UUID uuid)
	{
		if(!activeVisuals.containsKey(uuid))
			return Lists.newArrayList();
		
		long currentTime = world.getTime();
		List<ActiveVisual> visuals = Lists.newArrayList();
		visuals.addAll(activeVisuals.get(uuid));
		visuals.removeIf(visual -> !visual.isActive(currentTime));
		return visuals;
	}
	
	public void addVisual(Entity ent, TFSpecialVisual visual, int duration)
	{
		addVisual(ent, visual, duration, true);
	}
	
	public void addVisual(Entity ent, TFSpecialVisual visual, int duration, boolean override)
	{
		List<ActiveVisual> set = activeVisuals.getOrDefault(ent.getUuid(), Lists.newArrayList());
		if(override)
			set.removeIf(active -> active.visual() == visual);
		set.add(new ActiveVisual(world.getTime(), duration, visual));
		activeVisuals.put(ent.getUuid(), set);
		markDirty();
	}
	
	public void clearVisual(Entity ent, TFSpecialVisual visual)
	{
		if(!hasVisual(ent.getUuid(), visual)) return;
		
		List<ActiveVisual> visuals = activeVisuals.getOrDefault(ent.getUuid(), Lists.newArrayList());
		if(visuals.isEmpty())
			return;
		else if(visuals.removeIf(active -> active.visual() == visual))
			{
				activeVisuals.put(ent.getUuid(), visuals);
				markDirty();
			}
	}
	
	public void clearVisuals(Entity ent)
	{
		if(activeVisuals.containsKey(ent.getUuid()) && !activeVisuals.getOrDefault(ent.getUuid(), Lists.newArrayList()).isEmpty())
		{
			activeVisuals.remove(ent.getUuid());
			markDirty();
		}
	}
	
	public void markDirty()
	{
		super.markDirty();
		if(this.world != null && !this.world.isClient)
			this.world.getPlayers().forEach(player -> SyncSpecialVisualsPacket.send(player, this));
	}
	
	public static class ActiveVisual
	{
		private final long startTime;
		private final long endTime;
		private final TFSpecialVisual visualID;
		
		public ActiveVisual(long start, int duration, TFSpecialVisual visual)
		{
			this(start, start + duration, visual);
		}
		
		public ActiveVisual(long start, long end, TFSpecialVisual visual)
		{
			this.startTime = start;
			this.endTime = end;
			this.visualID = visual;
		}
		
		public boolean isActive(long time) { return time <= endTime; }
		
		public NbtCompound writeToNbt()
		{
			NbtCompound data = new NbtCompound();
			data.putLong("Start", startTime);
			data.putLong("End", endTime);
			data.putString("ID", visualID.name().toString());
			return data;
		}
		
		public static ActiveVisual fromNbt(NbtCompound data)
		{
			return new ActiveVisual(data.getLong("Start"), data.getLong("End"), TFSpecialVisual.fromString(new Identifier(data.getString("ID"))));
		}
		
		public TFSpecialVisual visual() { return this.visualID; }
		
		public int ticksActive(long currentTime) { return (int)(currentTime - startTime); }
		
		public float progress(long currentTime) { return (float)ticksActive(currentTime) / (float)(endTime - startTime); }
	}
}
