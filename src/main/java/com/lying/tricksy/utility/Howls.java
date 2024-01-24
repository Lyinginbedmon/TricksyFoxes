package com.lying.tricksy.utility;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.HowlWhiteboard;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

/**
 * TODO Add HowlWhiteboard to available whiteboards for Tricksy Wolves
 */
public class Howls extends PersistentState
{
	public static final int HOWL_DURATION = Reference.Values.TICKS_PER_MINUTE;
	
	private List<Entry> activeHowls = Lists.newArrayList();
	private ServerWorld world = null;
	
	public static Howls getHowls(ServerWorld world)
	{
		PersistentStateManager manager = world.getPersistentStateManager();
		Howls howls = manager.getOrCreate(Howls::createFromNbt, Howls::new, Reference.ModInfo.MOD_ID + ":howls");
		howls.world = world;
		howls.markDirty();
		return howls;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		NbtList set = new NbtList();
		activeHowls.forEach(entry -> set.add(entry.writeToNbt(new NbtCompound())));
		nbt.put("Data", set);
		return nbt;
	}
	
	public static Howls createFromNbt(NbtCompound nbt)
	{
		Howls howls = new Howls();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
			howls.activeHowls.add(Entry.readFromNbt(set.getCompound(i)));
		return howls;
	}
	
	public void startHowl(LivingEntity wolf)
	{
		activeHowls.add(new Entry(wolf, world.getTime()));
	}
	
	// TODO Account for distance to exclude howls too far away to be heard
	@Nullable
	public HowlWhiteboard getCurrentHowl(BlockPos pos)
	{
		long time = world.getTime();
		Entry youngest = null;
		Iterator<Entry> iterator = activeHowls.iterator();
		while(iterator.hasNext())
		{
			Entry howl = iterator.next();
			if(!howl.isActive(time))
				iterator.remove();
			else if(youngest == null || howl.age(time) < youngest.age(time))
				youngest = howl;
		}
		
		return youngest.board();
	}
	
	private static class Entry
	{
		private long startTick = -1;
		private HowlWhiteboard howl = new HowlWhiteboard();
		
		public Entry(LivingEntity wolf, long start)
		{
			startTick = start;
			howl.setToWolf(wolf);
		}
		
		private Entry() { }
		
		public long age(long time) { return time - startTick; }
		
		public boolean isActive(long time) { return age(time) <= HOWL_DURATION; }
		
		public HowlWhiteboard board() { return this.howl; }
		
		public NbtCompound writeToNbt(NbtCompound compound)
		{
			compound.putLong("Start", startTick);
			compound.put("Data", howl.writeToNbt(new NbtCompound()));
			return compound;
		}
		
		public static Entry readFromNbt(NbtCompound compound)
		{
			Entry entry = new Entry();
			entry.startTick = compound.getLong("Start");
			entry.howl = new HowlWhiteboard();
			entry.howl.readFromNbt(compound.getCompound("Data"));
			
			return entry;
		}
	}
}
