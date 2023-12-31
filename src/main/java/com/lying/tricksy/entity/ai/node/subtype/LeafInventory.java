package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
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
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
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
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_inventory"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_DROP, new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty())
					return Result.FAILURE;
				
				IWhiteboardObject<Integer> amount = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				tricksy.dropStack(heldStack.split(amount.size() == 0 ? heldStack.getCount() : amount.get()));
				return Result.SUCCESS;
			}
		});
		add(set, VARIANT_SWAP, new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				ItemStack mainStack = tricksy.getMainHandStack().copy();
				ItemStack offStack = tricksy.getOffHandStack().copy();
				
				tricksy.setStackInHand(Hand.MAIN_HAND, offStack);
				tricksy.setStackInHand(Hand.OFF_HAND, mainStack);
				return Result.SUCCESS;
			}
		});
		add(set, VARIANT_INSERT_ITEM, insert());
		add(set, VARIANT_EXTRACT_ITEM, extract());
		add(set, VARIANT_PICK_UP, pickUp());
		add(set, VARIANT_EQUIP, equip());
		add(set, VARIANT_UNEQUIP, unequip());
		return set;
	}
	
	private static INodeTickHandler<LeafNode> insert()
	{
		return new InventoryHandler()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_insert");
			
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef LIMIT = CommonVariables.VAR_NUM;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			public static final WhiteboardRef SLOT = new WhiteboardRef("slot", TFObjType.INT);
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						TILE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK && !ref.isFilter()),
						FACE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.UP), ConstantsWhiteboard.DIRECTIONS.get(Direction.UP).displayName()),
						SLOT, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int()),
						LIMIT, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int()),
						FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<Integer> slot = getOrDefault(SLOT, parent, whiteboards).as(TFObjType.INT);
				IWhiteboardObject<?> filter = getOrDefault(FILTER, parent, whiteboards);
				IWhiteboardObject<?> count = getOrDefault(LIMIT, parent, whiteboards);
				
				BlockPos block = value.get();
				if(!INodeTickHandler.canInteractWithBlock(tricksy, block))
					return Result.FAILURE;
				
				ItemStack heldStack = tricksy.getMainHandStack();
				// Fail if we have nothing to insert OR we don't have enough items to insert
				if(heldStack.isEmpty() || count.size() > 0 && count.as(TFObjType.INT).get() > heldStack.getCount())
					return Result.FAILURE;
				
				// Fail if our held stack doesn't meet a provided item filter
				if(filter.size() > 0 && !InventoryHandler.matchesItemFilter(heldStack, filter.as(TFObjType.ITEM)))
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				ItemStack insertStack = heldStack.split(count.size() == 0 ? heldStack.getCount() : count.as(TFObjType.INT).get());
				
				insertStack = InventoryHandler.insertStackIntoTile(insertStack, tile, ((WhiteboardObjBlock)face).direction(), slot.size() == 0 ? -1 : slot.get());
				// Return any remaining items in insertStack to the heldStack
				heldStack.increment(insertStack.getCount());
				if(insertStack.isEmpty())
					tile.markDirty();
				
				if(tricksy.getRandom().nextInt(20) == 0)
					INodeTickHandler.activateBlock(block, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				tricksy.getLookControl().lookAt(block.getX() + 0.5D, block.getY() + 0.5D, block.getZ() + 0.5D);
				
				return insertStack.isEmpty() ? Result.SUCCESS : Result.FAILURE;
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> extract()
	{
		return new InventoryHandler()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_extract");
			
			public static final WhiteboardRef TILE = CommonVariables.VAR_POS;
			public static final WhiteboardRef FACE = InventoryHandler.FACE;
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						TILE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK && !ref.isFilter()),
						FACE, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK, new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.DOWN), ConstantsWhiteboard.DIRECTIONS.get(Direction.DOWN).displayName()),
						FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(TILE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> face = getOrDefault(FACE, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, whiteboards).as(TFObjType.ITEM);
				
				BlockPos block = value.get();
				if(!INodeTickHandler.canInteractWithBlock(tricksy, block))
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
					return Result.FAILURE;
				
				// Fail if we can't put the extracted item into our main hand
				ItemStack heldStack = tricksy.getMainHandStack();
				if(!heldStack.isEmpty() && (heldStack.getCount() == heldStack.getMaxCount() || !InventoryHandler.matchesItemFilter(heldStack, filter)))
					return Result.FAILURE;
				
				Inventory inv = (Inventory)tile;
				
				// Fail if the inventory doesn't contain any items that match the filter
				if(filter.size() > 0 && !inv.containsAny((stack) -> InventoryHandler.matchesItemFilter(stack, filter)))
					return Result.FAILURE;
				
				tricksy.getLookControl().lookAt(block.getX() + 0.5D, block.getY() + 0.5D, block.getZ() + 0.5D);
				
				// The extracted item
				ItemStack extracted = ItemStack.EMPTY;
				if(tile instanceof SidedInventory)
				{
					SidedInventory sidedInv = (SidedInventory)inv;
					Direction side = ((WhiteboardObjBlock)face).direction();
					int[] slots = sidedInv.getAvailableSlots(side);
					for(int slot : slots)
					{
						if(InventoryHandler.matchesItemFilter(sidedInv.getStack(slot), filter))
							extracted = InventoryHandler.extractItemFrom(inv, slot, heldStack);
						
						if(!extracted.isEmpty())
							break;
					}
				}
				else
					extracted = InventoryHandler.extractItemFrom(inv, heldStack);
				
				if(extracted.isEmpty())
					return Result.FAILURE;
				else
				{
					tile.markDirty();
					tricksy.setStackInHand(Hand.MAIN_HAND, InventoryHandler.mergeStacks(heldStack, extracted));
					
					if(tricksy.getRandom().nextInt(20) == 0)
						INodeTickHandler.activateBlock(block, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
					return Result.SUCCESS;
				}
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> pickUp()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				if(value.isEmpty())
					return Result.FAILURE;
				else if(value.get().getType() != EntityType.ITEM || !INodeTickHandler.canInteractWithEntity(tricksy, value.get()))
					return Result.FAILURE;
				
				ItemEntity itemEnt = (ItemEntity)value.get();
				if(itemEnt.cannotPickup())
					return Result.FAILURE;
				
				tricksy.getLookControl().lookAt(itemEnt);
				
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
		};
	}
	
	private static INodeTickHandler<LeafNode> equip()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef FILTER = InventoryHandler.FILTER;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<ItemStack> filter = getOrDefault(FILTER, parent, whiteboards).as(TFObjType.ITEM);
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty() || !InventoryHandler.matchesItemFilter(heldStack, filter))
					return Result.FAILURE;
				
				if(!(heldStack.getItem() instanceof ArmorItem))
					return Result.FAILURE;
				
				ArmorItem armor = (ArmorItem)heldStack.getItem();
				EquipmentSlot slot = armor.getSlotType();
				if(!tricksy.getEquippedStack(slot).isEmpty())
					return Result.FAILURE;
				
				tricksy.equipStack(slot, heldStack.split(1));
				tricksy.setStackInHand(Hand.MAIN_HAND, heldStack.getCount() > 0 ? heldStack : ItemStack.EMPTY);
				return Result.SUCCESS;
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> unequip()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Integer> slotNum = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				EquipmentSlot equip = EquipmentSlot.HEAD;
				if(slotNum.size() == 0)
				{
					// Drop first equipped armour piece that isn't empty or Curse of Binding
					for(EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
					{
						ItemStack equipped = tricksy.getEquippedStack(slot);
						if(!equipped.isEmpty() && !EnchantmentHelper.hasBindingCurse(equipped))
						{
							equip = slot;
							break;
						}
					}
				}
				else
				{
					int slotIndex = MathHelper.clamp(slotNum.get(), 0, 3);
					equip = EquipmentSlot.fromTypeIndex(Type.ARMOR, slotIndex);
				}
				
				ItemStack equipped = tricksy.getEquippedStack(equip);
				if(equipped.isEmpty() || EnchantmentHelper.hasBindingCurse(equipped))
					return Result.FAILURE;
				
				tricksy.dropStack(equipped);
				tricksy.equipStack(equip, ItemStack.EMPTY);
				return Result.SUCCESS;
			}
		};
	}
}
