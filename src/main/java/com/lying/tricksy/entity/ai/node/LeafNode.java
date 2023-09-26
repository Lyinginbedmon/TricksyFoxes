package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
	public static final Identifier VARIANT_SET = new Identifier(Reference.ModInfo.MOD_ID, "set_value");
	
	public static final Identifier VARIANT_GOTO = new Identifier(Reference.ModInfo.MOD_ID, "goto");
	public static final Identifier VARIANT_DROP = new Identifier(Reference.ModInfo.MOD_ID, "drop_item");
	public static final Identifier VARIANT_SWAP = new Identifier(Reference.ModInfo.MOD_ID, "swap_items");
	public static final Identifier VARIANT_INSERT_ITEM = new Identifier(Reference.ModInfo.MOD_ID, "insert_item");
	public static final Identifier VARIANT_EXTRACT_ITEM = new Identifier(Reference.ModInfo.MOD_ID, "extract_item");
	public static final Identifier VARIANT_WAIT = new Identifier(Reference.ModInfo.MOD_ID, "wait");
	public static final Identifier VARIANT_SLEEP = new Identifier(Reference.ModInfo.MOD_ID, "sleep");
	
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
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeTickHandler.ofType(TFObjType.BLOCK));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR_POS);
				
				EntityNavigation navigator = tricksy.getNavigation();
				if(!parent.isRunning())
				{
					IWhiteboardObject<?> targetObj = Whiteboard.get(reference, local, global);
					if(targetObj.isEmpty())
						return Result.FAILURE;
					
					BlockPos dest = targetObj.as(TFObjType.BLOCK).get();
					if(navigator.findPathTo(dest, 20) == null)
					{
						System.out.println("Failed to find path to "+dest.toShortString());
						return Result.FAILURE;
					}
					
					navigator.startMovingTo(dest.getX(), dest.getY(), dest.getZ(), 0.5D);
					return Result.RUNNING;
				}
				else
					return navigator.isFollowingPath() ? Result.RUNNING : Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_DROP, new NodeTickHandler<LeafNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty())
					return Result.FAILURE;
				
				tricksy.dropStack(heldStack);
				tricksy.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
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
				if(tricksy.hurtTime > 0 || !tricksy.isOnGround())
					return Result.FAILURE;
				
				if(parent.ticksRunning > 0 && parent.ticksRunning%Reference.Values.TICKS_PER_SECOND == 0)
					tricksy.heal(1F);
				
				return tricksy.getHealth() >= tricksy.getMaxHealth() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_CYCLE, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(VAR_A, NodeTickHandler.anyLocal());
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<?> value = Whiteboard.get(parent.variable(VAR_A), local, global);
				if(!value.isList())
					return Result.FAILURE;
				
				value.cycle();
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_SET, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_copy", TFObjType.BOOL).displayName(CommonVariables.translate("to_copy"));
			public static final WhiteboardRef VAR_B = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(
						VAR_A, NodeTickHandler.any(),
						VAR_B, NodeTickHandler.anyLocal());
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
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, NodeTickHandler.ofType(TFObjType.INT));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR_COUNT);
				IWhiteboardObject<Integer> duration = Whiteboard.get(reference, local, global).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return Result.SUCCESS;
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_INSERT_ITEM, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, (ref) -> ref.type() == TFObjType.BLOCK);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR_POS);
				
				ItemStack heldStack = tricksy.getMainHandStack();
				if(heldStack.isEmpty())
					return Result.FAILURE;
				
				IWhiteboardObject<BlockPos> value = Whiteboard.get(reference, local, global).as(TFObjType.BLOCK);
				BlockPos block = value.get();
				if(tricksy.squaredDistanceTo(block.getX(), block.getY(), block.getZ()) > (4* 4))
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
					return Result.FAILURE;
				
				if(tile instanceof SidedInventory)
				{
					SidedInventory inv = (SidedInventory)tile;
					Direction face = ((WhiteboardObjBlock)value).direction();
					int[] slots = inv.getAvailableSlots(face);
					
					for(int slot : slots)
						if(inv.canInsert(slot, heldStack, face) && inv.isValid(slot, heldStack) && insertStackInto(inv, slot, heldStack))
							return Result.SUCCESS;
				}
				else if(insertStackInto((Inventory)tile, -1, heldStack))
					return Result.SUCCESS;
				
				return Result.FAILURE;
			}
			
			/** Inserts the given itemstack into the given inventory slot, or all viable slots, until the stack is empty */
			private static boolean insertStackInto(Inventory inv, int slot, ItemStack stack)
			{
				// Try insert into any slot in the inventory
				if(slot < 0)
				{
					boolean foundSlot = false;
					for(int i=0; i<inv.size(); i++)
						if(insertStackInto(inv, i, stack))
						{
							foundSlot = true;
							if(stack.getCount() == 0)
								return true;
						}
					return foundSlot;
					
				}
				// Try insert into specified slot in the inventory
				else
				{
					ItemStack stackInSlot = inv.getStack(slot);
					if(stackInSlot.isEmpty())
					{
						inv.setStack(slot, stack.copy());
						stack.decrement(stack.getCount());
						return true;
					}
					else if(canMergeStacks(stackInSlot, stack))
					{
						int amount = Math.min(stackInSlot.getMaxCount() - stackInSlot.getCount(), stack.getCount());
						stackInSlot.increment(amount);
						stack.decrement(amount);
						return true;
					}
				}
				return false;
			}
			
			private static boolean canMergeStacks(ItemStack a, ItemStack b) { return a.getCount() <= a.getMaxCount() && ItemStack.canCombine(a, b); }
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_EXTRACT_ITEM, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, (ref) -> ref.type() == TFObjType.BLOCK);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(CommonVariables.VAR_POS);
				
				IWhiteboardObject<BlockPos> value = Whiteboard.get(reference, local, global).as(TFObjType.BLOCK);
				BlockPos block = value.get();
				if(tricksy.squaredDistanceTo(block.getX(), block.getY(), block.getZ()) > (4* 4))
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				BlockEntity tile = world.getBlockEntity(block);
				if(tile == null || !(tile instanceof Inventory))
					return Result.FAILURE;
				
				ItemStack heldStack = tricksy.getMainHandStack();
				if(!heldStack.isEmpty() && heldStack.getCount() == heldStack.getMaxCount())
					return Result.FAILURE;
				
				if(tile instanceof SidedInventory)
				{
					SidedInventory inv = (SidedInventory)tile;
					Direction face = ((WhiteboardObjBlock)value).direction();
					int[] slots = inv.getAvailableSlots(face);
					
					for(int slot : slots)
						if(inv.canExtract(slot, heldStack, face))
						{
							heldStack = extractStackFrom(inv, slot, heldStack);
							tricksy.setStackInHand(Hand.MAIN_HAND, heldStack);
						}
				}
				else
				{
					heldStack = extractStackFrom((Inventory)tile, -1, heldStack);
					tricksy.setStackInHand(Hand.MAIN_HAND, heldStack);
				}
				
				return heldStack.isEmpty() ? Result.FAILURE : Result.SUCCESS;
			}
			
			/** Extracts the contents of the given slot, or all available slots, merging with the given stack */
			private static ItemStack extractStackFrom(Inventory inv, int slot, ItemStack stack)
			{
				// Try extract from any slot in the inventory
				if(slot < 0)
				{
					for(int i=0; i<inv.size(); i++)
						extractStackFrom(inv, i, stack);
					return stack;
				}
				// Try extract from specified slot in the inventory
				else
				{
					ItemStack stackInSlot = inv.getStack(slot);
					if(stackInSlot.isEmpty())
						return stack;
					else if(stack.isEmpty())
					{
						stack = stackInSlot.copy();
						inv.setStack(slot, ItemStack.EMPTY);
						return stack;
					}
					else if(canMergeStacks(stack, stackInSlot))
					{
						int amount = Math.min(stack.getMaxCount() - stack.getCount(), stackInSlot.getCount());
						stackInSlot.decrement(amount);
						stack.increment(amount);
						return stack;
					}
				}
				return stack;
			}
			
			private static boolean canMergeStacks(ItemStack a, ItemStack b) { return a.getCount() <= a.getMaxCount() && ItemStack.canCombine(a, b); }
		}));
	}
}
