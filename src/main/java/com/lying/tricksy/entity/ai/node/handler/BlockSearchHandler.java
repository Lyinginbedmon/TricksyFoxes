package com.lying.tricksy.entity.ai.node.handler;

import java.util.List;
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
	
	public BlockSearchHandler()
	{
		this(null);
	}
	
	public BlockSearchHandler(BlockSearchHandler.BlockSearchFunc function)
	{
		super(TFObjType.BLOCK);
		this.func = function == null ? Optional.empty() : Optional.of(function);
	}
	
	public void addVariables(Map<WhiteboardRef, INodeInput> set)
	{
		set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
		set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
	{
		IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
		IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
		
		Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
		World world = tricksy.getWorld();
		List<BlockPos> blocks = LeafSearch.sortByDistanceTo(searchArea.center(), searchArea.getBlocks(world, (blockpos, blockstate) -> func.isPresent() ? func.get().test(world, blockpos, blockstate) : test(world, blockpos, blockstate)));
		
		WhiteboardObjBlock result = new WhiteboardObjBlock();
		blocks.forEach((block) -> result.add(block));
		return result;
	}
	
	public boolean test(World world, BlockPos pos, BlockState state)
	{
		return false;
	}
	
	@FunctionalInterface
	public static interface BlockSearchFunc
	{
		public boolean test(World world, BlockPos pos, BlockState state);
	}
}