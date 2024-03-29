package com.lying.tricksy.entity.ai.node.handler;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.subtype.LeafSearch;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType.CooldownBehaviour;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.Region;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MatchBlockSearchHandler extends GetterHandlerTyped<BlockPos>
{
	private static final WhiteboardRef MATCH = new WhiteboardRef("ref", TFObjType.BLOCK).displayName(CommonVariables.translate("item_filter"));
	
	public MatchBlockSearchHandler()
	{
		super(TFObjType.BLOCK);
	}
	
	public CooldownBehaviour cooldownBehaviour() { return CooldownBehaviour.ALWAYS; }
	
	public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
	{
		set.put(CommonVariables.VAR_POS, GetterHandlerTyped.POS_OR_REGION);
		set.put(CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)INodeTickHandler.INTERACT_RANGE)));
		set.put(MATCH, NodeInput.makeInput(ref -> ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.ITEM, new WhiteboardObj.Item(new ItemStack(Blocks.STONE))));
	}
	
	@Override
	@Nullable
	public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
	{
		IWhiteboardObject<?> searchPos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards);
		IWhiteboardObject<Integer> searchRange = getOrDefault(CommonVariables.VAR_DIS, parent, whiteboards).as(TFObjType.INT);
		Region searchArea = GetterHandlerTyped.getSearchArea(searchPos, searchRange, tricksy);
		
		WhiteboardObjBlock result = new WhiteboardObjBlock();
		IWhiteboardObject<?> filter = getOrDefault(MATCH, parent, whiteboards);
		if(filter == null || filter.size() == 0)
		{
			parent.logStatus(TFNodeStatus.INPUT_ERROR, Text.literal("No filter supplied"));
			return null;
		}
		
		World world = tricksy.getWorld();
		LeafSearch.sortByDistanceTo(searchArea.center(), searchArea.getBlocks(world, (blockpos, blockstate) -> checkFilter(world, blockstate, filter))).forEach((block) -> result.add(block));
		return result;
	}
	
	public boolean checkFilter(World world, BlockState state, IWhiteboardObject<?> filter)
	{
		Block block = state.getBlock();
		if(filter.type() == TFObjType.BLOCK)
			return filter.getAll().stream().anyMatch(pos -> world.getBlockState((BlockPos)pos).getBlock() == block);
		else if(filter.type() == TFObjType.ITEM)
			return filter.getAll().stream().anyMatch(stack -> 
			{
				Item item = ((ItemStack)stack).getItem();
				return item instanceof BlockItem && ((BlockItem)item).getBlock() == block;
			});
		
		return false;
	}
}
