package com.lying.tricksy.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class BehaviourForest extends PersistentState
{
	private Map<UUID, NbtCompound> trees = new HashMap<>();
	
	public static BehaviourForest getForest(MinecraftServer server)
	{
		ServerWorld world = server.getWorld(World.OVERWORLD);
		PersistentStateManager manager = world.getPersistentStateManager();
		BehaviourForest forest = manager.getOrCreate(BehaviourForest::createFromNbt, BehaviourForest::new, Reference.ModInfo.MOD_ID + ":behaviour_forest");
		forest.markDirty();
		return forest;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		NbtList set = new NbtList();
		trees.forEach((tricksy, tree) -> 
		{
			NbtCompound compound = new NbtCompound();
			compound.putUuid("ID", tricksy);
			compound.put("Tree", tree);
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static BehaviourForest createFromNbt(NbtCompound nbt)
	{
		TricksyFoxes.LOGGER.info(" # Loading Behaviour Forest #");
		BehaviourForest forest = new BehaviourForest();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			if(compound.contains("Tree", NbtCompound.COMPOUND_TYPE))
				forest.trees.put(compound.getUuid("ID"), compound.getCompound("Tree"));
		}
		return forest;
	}
	
	public void remove(UUID tricksyID)
	{
		if(trees.remove(tricksyID) != null)
			markDirty();
	}
	
	public NbtCompound getTreeFor(UUID tricksyID)
	{
		return trees.getOrDefault(tricksyID, new NbtCompound());
	}
	
	public boolean hasTreeFor(UUID tricksyID) { return !getTreeFor(tricksyID).isEmpty(); }
	
	public void setTreeFor(UUID tricksyID, BehaviourTree tree)
	{
		NbtCompound data = tree.storeTrees();
		if(!data.isEmpty())
		{
			trees.put(tricksyID, tree.storeTrees());
			markDirty();
		}
	}
}
