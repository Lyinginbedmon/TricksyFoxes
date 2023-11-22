package com.lying.tricksy.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class CandlePowers extends PersistentState
{
	private Map<UUID, Integer> powers = new HashMap<>();
	
	public static CandlePowers getCandlePowers(MinecraftServer server)
	{
		ServerWorld world = server.getWorld(World.OVERWORLD);
		PersistentStateManager manager = world.getPersistentStateManager();
		CandlePowers whiteboards = manager.getOrCreate(CandlePowers::createFromNbt, CandlePowers::new, Reference.ModInfo.MOD_ID + ":candle_powers");
		whiteboards.markDirty();
		return whiteboards;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		NbtList set = new NbtList();
		powers.forEach((tricksy,power) -> 
		{
			NbtCompound compound = new NbtCompound();
			compound.putUuid("ID", tricksy);
			compound.putInt("Power", power);
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static CandlePowers createFromNbt(NbtCompound nbt)
	{
		CandlePowers boards = new CandlePowers();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			boards.powers.put(compound.getUuid("ID"), compound.getInt("Power"));
		}
		return boards;
	}
	
	public int getPowerFor(UUID tricksyID)
	{
		return powers.getOrDefault(tricksyID, 0);
	}
	
	public void setPowerFor(UUID tricksyID, int power)
	{
		powers.put(tricksyID, MathHelper.clamp(power, 0, 15));
		markDirty();
	}
}
