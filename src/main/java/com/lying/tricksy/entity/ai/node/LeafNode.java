package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * TODO NODE TYPES
 * Leaf	- Performs an action and has no child nodes
 * 		Action	- Performs a base singular action from a predefined set
 * 		SubTree	- Performs a predefined complex action that would otherwise necessitate multiple nodes, such as melee combat
 */
public class LeafNode extends TreeNode<LeafNode>
{
	public LeafNode(UUID uuidIn)
	{
		super(TFNodeTypes.LEAF, uuidIn);
	}
	
	protected <T extends PathAwareEntity & ITricksyMob> @NotNull Result doTick(T tricksy, Whiteboard local, Whiteboard global)
	{
		EntityNavigation navigator = tricksy.getNavigation();
		if(!isRunning())
		{
			PlayerEntity player = tricksy.getWorld().getClosestPlayer(tricksy, 16D);
			if(player == null)
				return Result.FAILURE;
			
			if(navigator.findPathTo(player, 20) == null)
				return Result.FAILURE;
			
			navigator.startMovingTo(player, 0.5D);
			return Result.RUNNING;
		}
		else
			return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
	}
	
	public static LeafNode fromData(UUID uuid, NbtCompound data)
	{
		return new LeafNode(uuid);
	}
	
	public static void populateSubTypes(Collection<NodeSubType<LeafNode>> set)
	{
		
	}
}
