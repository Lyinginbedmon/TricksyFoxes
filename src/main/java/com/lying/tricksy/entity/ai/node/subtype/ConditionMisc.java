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
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConditionMisc extends NodeGroupCondition
{
	public static NodeSubType<ConditionNode> BLOCK_POWERED;
	public static NodeSubType<ConditionNode> ON_FIRE;
	public static NodeSubType<ConditionNode> IS_ENTITY;
	public static NodeSubType<ConditionNode> IS_ITEM;
	public static NodeSubType<ConditionNode> CAN_MINE;
	public static NodeSubType<ConditionNode> IS_CROP;
	public static NodeSubType<ConditionNode> IS_MATURE;
	public static NodeSubType<ConditionNode> IS_LEASHED_TO;
	
	public static NodeSubType<ConditionNode> CLOSER_THAN;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "condition_misc"); }
	
	public Collection<NodeSubType<ConditionNode>> getSubtypes()
	{
		List<NodeSubType<ConditionNode>> set = Lists.newArrayList();
		/** Performs a simple distance check from the mob to the given position and returns SUCCESS if the distance is less than a desired value */
		set.add(CLOSER_THAN = subtype(ISubtypeGroup.variant("closer_than"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)), 
						CommonVariables.VAR_POS_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()), 
						CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(8)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				// Value A - mandatory
				IWhiteboardObject<?> objPosA = getOrDefault(CommonVariables.VAR_POS_A, parent, whiteboards);
				if(objPosA.isEmpty())
					return Result.FAILURE;
				BlockPos posA = objPosA.as(TFObjType.BLOCK).get();
				
				// Value B - optional, defaults to mob's position
				IWhiteboardObject<?> objPosB = getOrDefault(CommonVariables.VAR_POS_B, parent, whiteboards);
				BlockPos posB;
				if(objPosB.isEmpty())
				{
					if(objPosB.size() == 0)
						posB = tricksy.getBlockPos();
					else
						return Result.FAILURE;
				}
				else
					posB = objPosB.as(TFObjType.BLOCK).get();
				
				int dist = getOrDefault(CommonVariables.VAR_DIS, parent, whiteboards).as(TFObjType.INT).get();
				return Math.sqrt(posA.getSquaredDistance(posB)) < dist ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(BLOCK_POWERED = subtype(ISubtypeGroup.variant("block_powered"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				BlockPos position = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK).get();
				return tricksy.getEntityWorld().isReceivingRedstonePower(position) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(ON_FIRE = subtype(ISubtypeGroup.variant("on_fire"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), TFObjType.ENT.blank(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<Entity> var = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				
				Entity ent = var.size() == 0 ? tricksy : var.get();
				if(ent == null || !(ent instanceof LivingEntity))
					return Result.FAILURE;
				
				return ((LivingEntity)ent).isOnFire() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(CAN_MINE = subtype(ISubtypeGroup.variant("can_mine"), new INodeTickHandler<ConditionNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "condition_mine");
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)),
						CommonVariables.VAR_ITEM, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item(), LocalWhiteboard.MAIN_ITEM.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<ItemStack> item = getOrDefault(CommonVariables.VAR_ITEM, parent, whiteboards).as(TFObjType.ITEM);
				
				BlockPos blockPos = pos.get();
				ItemStack stack = item.size() == 0 ? tricksy.getMainHandStack() : item.get();
				World world = tricksy.getWorld();
				BlockState state = world.getBlockState(blockPos);
				if(state.isAir() || state.getHardness(world, blockPos) < 0F)
					return Result.FAILURE;
				
				ServerFakePlayer player = ServerFakePlayer.makeForMob(tricksy, BUILDER_ID);
				player.setStackInHand(Hand.MAIN_HAND, stack.copy());
				boolean result = stack.getItem().canMine(state, world, blockPos, player) && stack.isSuitableFor(state);
				player.discard();
				return result ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(IS_ITEM = subtype(ISubtypeGroup.variant("is_item"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.VAR_ITEM, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, false), new WhiteboardObj.Item(), LocalWhiteboard.MAIN_ITEM.displayName()),
						InventoryHandler.FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<ItemStack> item = getOrDefault(CommonVariables.VAR_ITEM, parent, whiteboards).as(TFObjType.ITEM);
				IWhiteboardObject<ItemStack> filter = getOrDefault(InventoryHandler.FILTER, parent, whiteboards).as(TFObjType.ITEM);
				ItemStack stack = item.size() == 0 ? tricksy.getMainHandStack() : item.get();
				
				return InventoryHandler.matchesItemFilter(stack, filter) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(IS_ENTITY = subtype(ISubtypeGroup.variant("is_type"), new INodeTickHandler<ConditionNode>()
		{
			public static final WhiteboardRef FILTER = new WhiteboardRef("entity_filter", TFObjType.ENT).displayName(CommonVariables.translate("item_filter"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()),
						FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, true), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<Entity> entity = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, whiteboards).as(TFObjType.ENT);
				Entity ent = entity.size() == 0 ? tricksy : entity.get();
				return INodeTickHandler.matchesEntityFilter(ent, filter) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(IS_CROP = subtype(ISubtypeGroup.variant("is_crop"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				return tricksy.getWorld().getBlockState(pos.get()).getBlock() instanceof CropBlock ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(IS_MATURE = subtype(ISubtypeGroup.variant("is_mature_crop"), new INodeTickHandler<ConditionNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK);
				BlockState state = tricksy.getWorld().getBlockState(pos.get());
				return state.getBlock() instanceof CropBlock && ((CropBlock)state.getBlock()).isMature(state) ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(IS_LEASHED_TO = subtype(ISubtypeGroup.variant("is_leashed_to"), new INodeTickHandler<ConditionNode>()
		{
			public static final WhiteboardRef ENT_A = CommonVariables.VAR_A;
			public static final WhiteboardRef ENT_B = CommonVariables.VAR_B;
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						ENT_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false)),
						ENT_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, ConditionNode parent)
			{
				IWhiteboardObject<Entity> leashed = getOrDefault(ENT_A, parent, whiteboards).as(TFObjType.ENT);
				IWhiteboardObject<Entity> holder = parent.inputAssigned(ENT_B) ? getOrDefault(ENT_B, parent, whiteboards).as(TFObjType.ENT) : new WhiteboardObjEntity(tricksy);
				
				if(leashed.isEmpty() || !leashed.get().isAlive() || !(leashed.get() instanceof MobEntity))
					return Result.FAILURE;
				
				MobEntity mob = (MobEntity)leashed.get();
				return mob.getHoldingEntity() == holder.get() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		return set;
	}

}
