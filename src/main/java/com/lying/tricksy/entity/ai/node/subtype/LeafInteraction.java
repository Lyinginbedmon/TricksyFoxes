package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class LeafInteraction implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_ACTIVATE = ISubtypeGroup.variant("activate");
	public static final Identifier VARIANT_USE_ITEM_ON = ISubtypeGroup.variant("use_item_on");
	public static final Identifier VARIANT_BREAK_BLOCK = ISubtypeGroup.variant("break_block");
	public static final Identifier VARIANT_USE_ITEM = ISubtypeGroup.variant("use_item");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_interaction"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_USE_ITEM, useItem());
		add(set, VARIANT_ACTIVATE, activateBlock());
		add(set, VARIANT_USE_ITEM_ON, useItemOn());
		add(set, VARIANT_BREAK_BLOCK, breakBlock());
		return set;
	}
	
	private static INodeTickHandler<LeafNode> useItem()
	{
		return new INodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(false), Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Boolean> stopUsing = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BOOL);
				if(stopUsing.get())
				{
					if(tricksy.getItemUseTime() > 0)
					{
						tricksy.clearActiveItem();
						return Result.SUCCESS;
					}
					else
						return Result.FAILURE;
				}
				else
				{
					if(tricksy.getItemUseTime() == 0)
					{
						tricksy.setCurrentHand(Hand.MAIN_HAND);
						return Result.SUCCESS;
					}
					else
						return Result.FAILURE;
				}
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> useItemOn()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_use_item");
			public static final WhiteboardRef TARGET = new WhiteboardRef("target", TFObjType.BLOCK).displayName(Text.literal("Target"));
			
			// Items that directly affect the player using them, hence cannot be used normally by a mob
			private static final Set<Item> UNUSABLES = Set.of(Items.ENDER_PEARL, Items.LEAD, Items.FISHING_ROD);
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(TARGET, NodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.ENT));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(TARGET, parent, local, global);
				if(value == null || value.isEmpty() || UNUSABLES.contains(tricksy.getMainHandStack().getItem()))
					return Result.FAILURE;
				
				if(local.isCoolingDown(tricksy.getMainHandStack().getItem()))
					return Result.RUNNING;
				
				ActionResult result = ActionResult.FAIL;
				if(value.type() == TFObjType.BLOCK)
				{
					BlockPos blockPos = value.as(TFObjType.BLOCK).get();
					if(!INodeTickHandler.canInteractWithBlock(tricksy, blockPos))
						return Result.FAILURE;
					tricksy.getLookControl().lookAt(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
					result = INodeTickHandler.useHeldOnBlock(blockPos, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				}
				else if(value.type() == TFObjType.ENT)
				{
					Entity entity = value.as(TFObjType.ENT).get();
					if(value.isEmpty() || !(entity instanceof LivingEntity) || !INodeTickHandler.canInteractWithEntity(tricksy, entity))
						return Result.FAILURE;
					tricksy.getLookControl().lookAt(entity);
					result = INodeTickHandler.useHeldOnEntity((LivingEntity)entity, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				}
				INodeTickHandler.swingHand(tricksy, Hand.MAIN_HAND);
				return result != ActionResult.FAIL ? Result.SUCCESS : Result.FAILURE;
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> activateBlock()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_activate");
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> blockPos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(!INodeTickHandler.canInteractWithBlock(tricksy, blockPos.get()) || blockPos.isEmpty())
					return Result.FAILURE;
				
				if(local.isCoolingDown(tricksy.getMainHandStack().getItem()))
					return Result.RUNNING;
				
				ActionResult result = INodeTickHandler.activateBlock(blockPos.get(), (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				tricksy.getLookControl().lookAt(blockPos.get().getX() + 0.5D, blockPos.get().getY() + 0.5D, blockPos.get().getZ() + 0.5D);
				INodeTickHandler.swingHand(tricksy, Hand.MAIN_HAND);
				return result != ActionResult.FAIL ? Result.SUCCESS : Result.FAILURE;
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> breakBlock()
	{
		return new INodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_break");
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> blockPos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(blockPos.isEmpty())
					return Result.FAILURE;
				
				World world = tricksy.getWorld();
				if(!world.getGameRules().get(GameRules.DO_MOB_GRIEFING).get())
					return Result.FAILURE;
				
				BlockPos pos = blockPos.get();
				if(!INodeTickHandler.canInteractWithBlock(tricksy, pos))
					return Result.FAILURE;
				
				BlockState state = world.getBlockState(pos);
				if(state.isAir() || state.getHardness(world, pos) < 0F || state.getBlock() instanceof FluidBlock)
					return Result.FAILURE;
				
				ServerFakePlayer player = ServerFakePlayer.makeForMob(tricksy, BUILDER_ID);
				if(!player.getMainHandStack().getItem().canMine(state, world, pos, player))
					return Result.FAILURE;
				
				tricksy.getLookControl().lookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
				INodeTickHandler.swingHand(tricksy, Hand.MAIN_HAND);
				if(state.hasBlockBreakParticles())
					world.addBlockBreakParticles(pos, state);
				
				if(!parent.isRunning())
				{
					parent.ticks = 0F;
					return Result.RUNNING;
				}
				else if((parent.ticks += state.calcBlockBreakingDelta(player, world, pos)) >= 1F)
				{
					// FIXME Ensure tool is appropriately damaged
					player.interactionManager.tryBreakBlock(pos);
					INodeTickHandler.updateFromPlayer(tricksy, player);
					return Result.SUCCESS;
				}
				player.discard();
				
				return Result.RUNNING;
			}
		};
	}
}
