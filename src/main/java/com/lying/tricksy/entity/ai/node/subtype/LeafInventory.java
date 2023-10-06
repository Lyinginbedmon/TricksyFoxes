package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.Constants;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LeafInventory implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_DROP = ISubtypeGroup.variant("drop_item");
	public static final Identifier VARIANT_SWAP = ISubtypeGroup.variant("swap_items");
	public static final Identifier VARIANT_PICK_UP = ISubtypeGroup.variant("pick_up");
	public static final Identifier VARIANT_EQUIP = ISubtypeGroup.variant("equip_item");
	public static final Identifier VARIANT_UNEQUIP = ISubtypeGroup.variant("unequip_item");
	public static final Identifier VARIANT_INSERT_ITEM = ISubtypeGroup.variant("insert_item");
	public static final Identifier VARIANT_EXTRACT_ITEM = ISubtypeGroup.variant("extract_item");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		set.add(new NodeSubType<LeafNode>(VARIANT_DROP, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty())
					return Result.FAILURE;
				
				int amount = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT).get();
				tricksy.dropStack(heldStack.split(amount));
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SWAP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				ItemStack mainStack = tricksy.getMainHandStack().copy();
				ItemStack offStack = tricksy.getOffHandStack().copy();
				
				tricksy.setStackInHand(Hand.MAIN_HAND, offStack);
				tricksy.setStackInHand(Hand.OFF_HAND, mainStack);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_INSERT_ITEM, new InventoryHandler()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_insert");
			
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef LIMIT = CommonVariables.VAR_COUNT;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						TILE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK),
						FACE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.UP), Constants.DIRECTIONS.get(Direction.UP).displayName()),
						LIMIT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int()),
						FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<?> filter = getOrDefault(FILTER, parent, local, global);
				IWhiteboardObject<?> count = getOrDefault(LIMIT, parent, local, global);
				
				BlockPos block = value.get();
				if(!NodeTickHandler.canInteractWithBlock(tricksy, block))
					return Result.FAILURE;
				
				ItemStack heldStack = tricksy.getMainHandStack();
				// Fail if we have nothing to insert OR we don't have enough items to insert
				if(heldStack.isEmpty() || count.size() > 0 && count.as(TFObjType.INT).get() > heldStack.getCount())
				{
					tricksy.logStatus(Text.literal("I don't have anything to insert"));
					return Result.FAILURE;
				}
				
				// Fail if our held stack doesn't meet a provided item filter
				if(filter.size() > 0 && !InventoryHandler.matchesFilter(heldStack, filter.as(TFObjType.ITEM)))
				{
					tricksy.logStatus(Text.literal("I don't have the right item to insert"));
					return Result.FAILURE;
				}
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				ItemStack insertStack = heldStack.split(count.size() == 0 ? heldStack.getCount() : count.as(TFObjType.INT).get());
				
				insertStack = InventoryHandler.insertStackIntoTile(insertStack, tile, ((WhiteboardObjBlock)face).direction());
				// Return any remaining items in insertStack to the heldStack
				heldStack.increment(insertStack.getCount());
				tricksy.logStatus(Text.literal(insertStack.isEmpty() ? "Item inserted successfully" : "I couldn't insert the item"));
				if(insertStack.isEmpty())
					tile.markDirty();
				
				if(tricksy.getRandom().nextInt(20) == 0)
					NodeTickHandler.interactWith(block, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				
				return insertStack.isEmpty() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_EXTRACT_ITEM, new InventoryHandler()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_extract");
			
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						TILE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK),
						FACE, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.DOWN), Constants.DIRECTIONS.get(Direction.DOWN).displayName()),
						FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ITEM);
				
				BlockPos block = value.get();
				if(!NodeTickHandler.canInteractWithBlock(tricksy, block))
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
				{
					tricksy.logStatus(Text.literal("That's not an inventory"));
					return Result.FAILURE;
				}
				
				// Fail if we can't put the extracted item into our main hand
				ItemStack heldStack = tricksy.getMainHandStack();
				if(!heldStack.isEmpty())
					if(heldStack.getCount() == heldStack.getMaxCount() || !InventoryHandler.matchesFilter(heldStack, filter))
					{
						tricksy.logStatus(Text.literal("I can't extract that currently"));
						return Result.FAILURE;
					}
				
				Inventory inv = (Inventory)tile;
				
				// Fail if the inventory doesn't contain any items that match the filter
				if(filter.size() > 0 && !inv.containsAny((stack) -> InventoryHandler.matchesFilter(stack, filter)))
				{
					tricksy.logStatus(Text.literal("There isn't any of that there"));
					return Result.FAILURE;
				}
				
				// The extracted item
				ItemStack extracted = ItemStack.EMPTY;
				if(tile instanceof SidedInventory)
				{
					SidedInventory sidedInv = (SidedInventory)inv;
					Direction side = ((WhiteboardObjBlock)face).direction();
					int[] slots = sidedInv.getAvailableSlots(side);
					for(int slot : slots)
					{
						if(InventoryHandler.matchesFilter(sidedInv.getStack(slot), filter))
							extracted = InventoryHandler.extractItemFrom(inv, slot, heldStack);
						
						if(!extracted.isEmpty())
							break;
					}
				}
				else
					extracted = InventoryHandler.extractItemFrom(inv, heldStack);
				
				if(extracted.isEmpty())
				{
					tricksy.logStatus(Text.literal("I didn't manage to extract anything"));
					return Result.FAILURE;
				}
				else
				{
					tile.markDirty();
					tricksy.setStackInHand(Hand.MAIN_HAND, InventoryHandler.mergeStacks(heldStack, extracted));
					tricksy.logStatus(Text.literal("I'm now holding ").append(tricksy.getMainHandStack().getName()).append(Text.literal(" x"+tricksy.getMainHandStack().getCount())));
					
					if(tricksy.getRandom().nextInt(20) == 0)
						NodeTickHandler.interactWith(block, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
					return Result.SUCCESS;
				}
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_PICK_UP, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(value.isEmpty())
					return Result.FAILURE;
				else if(value.get().getType() != EntityType.ITEM || !NodeTickHandler.canInteractWithEntity(tricksy, value.get()))
					return Result.FAILURE;
				
				ItemEntity itemEnt = (ItemEntity)value.get();
				if(itemEnt.cannotPickup())
					return Result.FAILURE;
				
				ItemStack itemStack = itemEnt.getStack();
				ItemStack heldStack = tricksy.getMainHandStack();
				if(!heldStack.isEmpty() && !InventoryHandler.canMergeStacks(heldStack, itemStack))
					return Result.FAILURE;
				
				if(heldStack.isEmpty())
				{
					heldStack = itemStack.copy();
					itemEnt.discard();
					tricksy.setStackInHand(Hand.MAIN_HAND, heldStack);
				}
				else
				{
					tricksy.setStackInHand(Hand.MAIN_HAND, InventoryHandler.mergeStacks(heldStack, itemStack));
					itemEnt.setStack(itemStack);
				}
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_EQUIP, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ITEM);
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty() || !InventoryHandler.matchesFilter(heldStack, filter))
					return Result.FAILURE;
				
				if(!(heldStack.getItem() instanceof ArmorItem))
					return Result.FAILURE;
				
				ArmorItem armor = (ArmorItem)heldStack.getItem();
				EquipmentSlot slot = armor.getSlotType();
				if(!tricksy.getEquippedStack(slot).isEmpty())
				{
					tricksy.logStatus(Text.literal("I'm already wearing a "+slot.getName()));
					return Result.FAILURE;
				}
				
				tricksy.logStatus(Text.literal("Equipped "+heldStack.getName().getString()));
				tricksy.equipStack(slot, heldStack.split(1));
				tricksy.setStackInHand(Hand.MAIN_HAND, heldStack.getCount() > 0 ? heldStack : ItemStack.EMPTY);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_UNEQUIP, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(0)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Integer> slotID = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				
				int slot = MathHelper.clamp(slotID.get(), 0, 3);
				
				EquipmentSlot equip = EquipmentSlot.fromTypeIndex(Type.ARMOR, slot);
				ItemStack equipped = tricksy.getEquippedStack(equip);
				if(equipped.isEmpty())
					return Result.FAILURE;
				
				tricksy.logStatus(Text.literal("Dropping my "+equip.getName()));
				tricksy.dropStack(equipped);
				tricksy.equipStack(equip, ItemStack.EMPTY);
				return Result.SUCCESS;
			}
		}));
	}
}
