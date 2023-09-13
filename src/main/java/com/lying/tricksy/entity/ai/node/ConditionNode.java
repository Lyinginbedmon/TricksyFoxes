package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class ConditionNode extends TreeNode<ConditionNode>
{
	private double searchRange = 8D;
	
	public ConditionNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONDITION, uuidIn);
	}
	
	protected <T extends PathAwareEntity & ITricksyMob> @NotNull Result doTick(T tricksy, Whiteboard local, Whiteboard global)
	{
		World world = tricksy.getWorld();
		PlayerEntity player = world.getClosestPlayer(tricksy, searchRange);
		return player == null ? Result.FAILURE : Result.SUCCESS;
	}
	
	protected NbtCompound writeToNbt(NbtCompound data)
	{
		data.putDouble("Search", searchRange);
		return data;
	}
	
	public static ConditionNode fromData(UUID uuid, NbtCompound data)
	{
		ConditionNode node = new ConditionNode(uuid);
		node.searchRange = data.getDouble("Search");
		return node;
	}
	
	public static ConditionNode isPlayerNearby(UUID uuidIn, double range)
	{
		ConditionNode node = new ConditionNode(UUID.randomUUID());
		node.searchRange = range;
		return node;
	}
	
	public static void populateSubTypes(Collection<NodeSubType<ConditionNode>> set)
	{
		
	}
}
