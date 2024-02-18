package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConditionInventory extends NodeGroupCondition
{
	public static NodeSubType<ConditionNode> INV_HAS;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "condition_inventory"); }
	
	public Collection<NodeSubType<ConditionNode>> getSubtypes()
	{
		List<NodeSubType<ConditionNode>> set = Lists.newArrayList();
		set.add(INV_HAS = subtype(ISubtypeGroup.variant("inv_has"), new INodeTickHandler<ConditionNode>()
		{
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						TILE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK),
						FACE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.DOWN), ConstantsWhiteboard.DIRECTIONS.get(Direction.DOWN).displayName()),
						FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull boolean validityCheck(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, whiteboards).as(TFObjType.BLOCK);
				BlockPos block = value.get();
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
				{
					parent.logStatus(TFNodeStatus.INPUT_ERROR);
					return false;
				}
				return true;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onCast(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, whiteboards).as(TFObjType.ITEM);
				
				BlockPos block = value.get();
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				Inventory inv = (Inventory)tile;
				int exitSlot = -1;
				if(tile instanceof SidedInventory)
				{
					SidedInventory sidedInv = (SidedInventory)inv;
					Direction side = ((WhiteboardObjBlock)face).direction();
					int[] slots = ((SidedInventory)tile).getAvailableSlots(side);
					for(int slot : slots)
					{
						if(sidedInv.getStack(slot).isEmpty())
							continue;
						else if(InventoryHandler.matchesItemFilter(sidedInv.getStack(slot), filter))
						{
							exitSlot = slot;
							break;
						}
					}
				}
				else
					for(int slot = 0; slot < inv.size(); slot++)
					{
						if(inv.getStack(slot).isEmpty())
							continue;
						else if(InventoryHandler.matchesItemFilter(inv.getStack(slot), filter))
						{
							exitSlot = slot;
							break;
						}
					}
				
				if(exitSlot >= 0)
					return Result.SUCCESS;
				else
					return Result.FAILURE;
			}
		}));
		return set;
	}
}
