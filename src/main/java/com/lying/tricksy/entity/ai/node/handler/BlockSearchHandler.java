package com.lying.tricksy.entity.ai.node.handler;

import java.util.Map;
import java.util.Optional;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.subtype.LeafSearch;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.Region;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSearchHandler extends GetterHandler<BlockPos>
{
	private final Optional<BlockSearchHandler.BlockSearchFunc> func;
	
	public BlockSearchHandler(BlockSearchHandler.BlockSearchFunc function)
	{
		super(TFObjType.BLOCK);
		this.func = function == null ? Optional.empty() : Optional.of(function);
	}
	
	public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
	{
		set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
		set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
	{
		IWhiteboardObject<?> searchPos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
		IWhiteboardObject<Integer> searchRange = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
		Region searchArea = GetterHandler.getSearchArea(searchPos, searchRange, tricksy);
		if(searchArea == null)
			return null;
		
		WhiteboardObjBlock result = new WhiteboardObjBlock();
		if(!func.isPresent())
			return result;
		
		World world = tricksy.getWorld();
		BlockSearchFunc function = func.get();
		LeafSearch.sortByDistanceTo(searchArea.center(), searchArea.getBlocks(world, (blockpos, blockstate) -> function.test(world, blockpos, blockstate))).forEach((block) -> result.add(block));
		return result;
	}
	
	@FunctionalInterface
	public static interface BlockSearchFunc
	{
		public boolean test(World world, BlockPos pos, BlockState state);
	}
}