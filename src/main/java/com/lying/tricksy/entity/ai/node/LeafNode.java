package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.Constants;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * TODO Add more actions
 * NODE TYPES
 * Leaf	- Performs an action and has no child nodes
 * 		Action	- Performs a base singular action from a predefined set
 * 		SubTree	- Performs a predefined complex action that would otherwise necessitate multiple nodes, such as melee combat
 */
public class LeafNode extends TreeNode<LeafNode>
{
	public static final Identifier VARIANT_CYCLE = new Identifier(Reference.ModInfo.MOD_ID, "cycle_value");
	public static final Identifier VARIANT_SORT_NEAREST = new Identifier(Reference.ModInfo.MOD_ID, "sort_nearest");
	public static final Identifier VARIANT_SET = new Identifier(Reference.ModInfo.MOD_ID, "set_value");
	
	public static final Identifier VARIANT_GOTO = new Identifier(Reference.ModInfo.MOD_ID, "goto");
	public static final Identifier VARIANT_DROP = new Identifier(Reference.ModInfo.MOD_ID, "drop_item");
	public static final Identifier VARIANT_SWAP = new Identifier(Reference.ModInfo.MOD_ID, "swap_items");
	public static final Identifier VARIANT_INSERT_ITEM = new Identifier(Reference.ModInfo.MOD_ID, "insert_item");
	public static final Identifier VARIANT_EXTRACT_ITEM = new Identifier(Reference.ModInfo.MOD_ID, "extract_item");
	public static final Identifier VARIANT_WAIT = new Identifier(Reference.ModInfo.MOD_ID, "wait");
	public static final Identifier VARIANT_SLEEP = new Identifier(Reference.ModInfo.MOD_ID, "sleep");
	public static final Identifier VARIANT_SET_HOME = new Identifier(Reference.ModInfo.MOD_ID, "set_home");
	public static final Identifier VARIANT_SET_ATTACK = new Identifier(Reference.ModInfo.MOD_ID, "set_attack");
	
	protected int ticks = 20;
	
	public LeafNode(UUID uuidIn)
	{
		super(TFNodeTypes.LEAF, uuidIn);
	}
	
	public static LeafNode fromData(UUID uuid, NbtCompound data)
	{
		return new LeafNode(uuid);
	}
	
	public final boolean canAddChild() { return false; }
	
	public static void populateSubTypes(Collection<NodeSubType<LeafNode>> set)
	{
		set.add(new NodeSubType<LeafNode>(VARIANT_GOTO, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
					if(targetObj.isEmpty())
					{
						tricksy.logStatus(Text.literal("No destination to go to"));
						return Result.FAILURE;
					}
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(dest.getSquaredDistance(tricksy.getBlockPos()) <= 1D)
						return Result.SUCCESS;
					
					navigator.startMovingTo(dest.getX(), dest.getY(), dest.getZ(), 0.5D);
					tricksy.logStatus(Text.literal(navigator.isFollowingPath() ? "Moving to destination" : "No path found"));
					return navigator.isFollowingPath() ? Result.RUNNING : Result.FAILURE;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		}));
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
		set.add(new NodeSubType<LeafNode>(VARIANT_SLEEP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				if(!parent.isRunning())
				{
					if(canSleep(tricksy))
					{
						tricksy.logStatus(Text.literal("zzZZzzzZZ"));
						tricksy.setSleeping(true);
						return Result.RUNNING;
					}
					else
					{
						tricksy.logStatus(Text.literal("I can't sleep now"));
						return Result.FAILURE;
					}
				}
				else
				{
					Result end = Result.RUNNING;
					
					if(!canSleep(tricksy))
						end = Result.FAILURE;
					else if(parent.ticksRunning%Reference.Values.TICKS_PER_SECOND == 0 && tricksy.getHealth() < tricksy.getMaxHealth())
						tricksy.heal(1F);
					
					tricksy.setSleeping(!end.isEnd());
					return end;
				}
			}
			
			private <T extends PathAwareEntity & ITricksyMob<?>> boolean canSleep(T tricksy) { return tricksy.isOnGround() && tricksy.hurtTime <= 0; }
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_CYCLE, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(VAR_A, INodeInput.makeInput(NodeTickHandler.anyLocal()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(VAR_A, parent, local, global);
				if(!value.isList())
					return Result.FAILURE;
				
				value.cycle();
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SORT_NEAREST, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BLOCK).displayName(CommonVariables.translate("to_cycle"));
			private static BlockPos position;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						VAR_A, INodeInput.makeInput((ref) -> ref.type().castableTo(TFObjType.BLOCK) && ref.boardType() == BoardType.LOCAL),
						CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), Whiteboard.Local.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(VAR_A);
				IWhiteboardObject<BlockPos> value = getOrDefault(VAR_A, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				
				position = null;
				if(pos.size() == 0)
					position = tricksy.getBlockPos();
				else
					position = pos.as(TFObjType.BLOCK).get();
				
				if(value.isEmpty() || !value.isList())
					return Result.FAILURE;
				
				List<BlockPos> points = value.getAll();
				points.sort(new Comparator<BlockPos>() 
				{
					public int compare(BlockPos o1, BlockPos o2)
					{
						double dist1 = o1.getSquaredDistance(position);
						double dist2 = o2.getSquaredDistance(position);
						return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
					}
				});
				
				tricksy.logStatus(Text.literal("Closest position was "+points.get(0).toShortString()));
				WhiteboardObjBlock sorted = new WhiteboardObjBlock();
				points.forEach((point) -> sorted.add(point));
				local.setValue(reference, sorted);
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_copy", TFObjType.BOOL).displayName(CommonVariables.translate("to_copy"));
			public static final WhiteboardRef VAR_B = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						VAR_A, INodeInput.makeInput(NodeTickHandler.any()),
						VAR_B, INodeInput.makeInput(NodeTickHandler.anyLocal()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef from = parent.variable(VAR_A);
				WhiteboardRef to = parent.variable(VAR_B);
				
				/** Destination must be a cachable value in a local whiteboard of the same or castable data type */
				if(to.uncached() || to.boardType() != BoardType.LOCAL || !from.type().castableTo(to.type()))
					return Result.FAILURE;
				
				local.setValue(to, Whiteboard.get(from, local, global).as(to.type()));
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_WAIT, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return Result.SUCCESS;
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_INSERT_ITEM, new InventoryHandler()
		{
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
				if(!NodeTickHandler.canInteractWith(tricksy, block))
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
				return insertStack.isEmpty() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_EXTRACT_ITEM, new InventoryHandler()
		{
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
				if(!NodeTickHandler.canInteractWith(tricksy, block))
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
					return Result.SUCCESS;
				}
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_ATTACK, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(value.size() == 0)
				{
					tricksy.setTarget(null);
					return Result.SUCCESS;
				}
				
				Entity ent = value.get();
				if(ent instanceof LivingEntity)
				{
					tricksy.setTarget((LivingEntity)ent);
					return Result.SUCCESS;
				}
				else
					return Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_HOME, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> value = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(value.size() == 0)
					tricksy.clearPositionTarget();
				else
					tricksy.setPositionTarget(value.get(), 6);
				return Result.SUCCESS;
			}
		}));
	}
}
