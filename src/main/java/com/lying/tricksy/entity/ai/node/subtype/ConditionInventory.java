package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ConditionInventory implements ISubtypeGroup<ConditionNode>
{
	public static final Identifier VARIANT_INV_HAS = ISubtypeGroup.variant("inv_has");
	
	public void addActions(Collection<NodeSubType<ConditionNode>> set)
	{
		add(set, VARIANT_INV_HAS, new NodeTickHandler<ConditionNode>()
		{
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						TILE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK),
						FACE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.DOWN), ConstantsWhiteboard.DIRECTIONS.get(Direction.DOWN).displayName()),
						FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ITEM);
				
				BlockPos block = value.get();
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
				{
					tricksy.logStatus(Text.literal("That's not an inventory"));
					return Result.FAILURE;
				}
				
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
				{
					tricksy.logStatus(Text.literal("Found something in slot "+exitSlot));
					return Result.SUCCESS;
				}
				else
					return Result.FAILURE;
			}
		});
	}
}
