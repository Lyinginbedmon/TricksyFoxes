package com.lying.tricksy.utility;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.HowlWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.InertWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class Howls extends PersistentState
{
	public static final int HOWL_DURATION = Reference.Values.TICKS_PER_MINUTE;
	public static final InertWhiteboard EMPTY_BOARD = new InertWhiteboard(TFWhiteboards.HOWL, null);
	
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
	
	public Howl getCurrentHowl(BlockPos pos)
	{
		if(activeHowls.isEmpty())
			return new Howl();
		
		long time = world.getTime();
		Entry youngest = null;
		for(Entry howl : activeHowls)
		{
			long age = howl.age(time);
			if(howl.isActive(time)  && howl.board().position.as(TFObjType.BLOCK).get().isWithinDistance(pos, ((age / 20) * Reference.Values.SPEED_OF_SOUND)))
				if(youngest == null || age < youngest.age(time))
					youngest = howl;
		}
		
		return youngest == null ? new Howl() : youngest.board();
	}
	
	private static class Entry
	{
		private long startTick = -1;
		private Howl howl = null;
		
		public Entry(LivingEntity wolf, long start)
		{
			startTick = start;
			howl = new Howl(wolf);
		}
		
		private Entry() { }
		
		public long age(long time) { return time - startTick; }
		
		public boolean isActive(long time) { return age(time) <= HOWL_DURATION; }
		
		public Howl board() { return this.howl; }
		
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
			entry.howl.readFromNbt(compound.getCompound("Data"));
			
			return entry;
		}
	}
	
	public static class Howl
	{
		public IWhiteboardObject<BlockPos> position = new WhiteboardObjBlock();
		public IWhiteboardObject<Entity> sender = new WhiteboardObjEntity();
		
		public Howl(LivingEntity wolf)
		{
			sender = new WhiteboardObjEntity(wolf);
			position = new WhiteboardObjBlock(wolf.getBlockPos());
		}
		
		public Howl() { }
		
		public NbtCompound writeToNbt(NbtCompound compound)
		{
			compound.put("Sender", sender.writeToNbt(new NbtCompound()));
			compound.put("Position", position.writeToNbt(new NbtCompound()));
			return compound;
		}
		
		public void readFromNbt(NbtCompound compound)
		{
			position = IWhiteboardObject.createFromNbt(compound.getCompound("Position")).as(TFObjType.BLOCK);
			sender = IWhiteboardObject.createFromNbt(compound.getCompound("Sender")).as(TFObjType.ENT);
		}
	}
	
	static
	{
		EMPTY_BOARD.addValue(HowlWhiteboard.POSITION, () -> new WhiteboardObjBlock());
		EMPTY_BOARD.addValue(HowlWhiteboard.SENDER, () -> new WhiteboardObjEntity());
	}
}
