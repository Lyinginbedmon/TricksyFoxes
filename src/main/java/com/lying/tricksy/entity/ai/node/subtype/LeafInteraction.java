package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

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

public class LeafInteraction implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_ACTIVATE = ISubtypeGroup.variant("activate");
	public static final Identifier VARIANT_USE_ITEM_ON = ISubtypeGroup.variant("use_item_on");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		add(set, VARIANT_ACTIVATE, new NodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_activate");
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> blockPos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				if(!NodeTickHandler.canInteractWithBlock(tricksy, blockPos.get()) || blockPos.isEmpty())
					return Result.FAILURE;
				
				if(local.isCoolingDown(tricksy.getMainHandStack().getItem()))
					return Result.RUNNING;
				
				tricksy.logStatus(Text.literal("Activating ").append(tricksy.getEntityWorld().getBlockState(blockPos.get()).getBlock().getName()));
				ActionResult result = NodeTickHandler.activateBlock(blockPos.get(), (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				tricksy.getLookControl().lookAt(blockPos.get().getX() + 0.5D, blockPos.get().getY() + 0.5D, blockPos.get().getZ() + 0.5D);
				tricksy.swingHand(Hand.MAIN_HAND);
				return result != ActionResult.FAIL ? Result.SUCCESS : Result.FAILURE;
			}
		});
		add(set, VARIANT_USE_ITEM_ON, new NodeTickHandler<LeafNode>()
		{
			private static final Identifier BUILDER_ID = new Identifier(Reference.ModInfo.MOD_ID, "leaf_use_item");
			public static final WhiteboardRef TARGET = new WhiteboardRef("target", TFObjType.BLOCK).displayName(Text.literal("Target"));
			
			// Items that directly affect the player using them, hence cannot be used normally by a mob
			private static final Set<Item> UNUSABLES = Set.of(Items.ENDER_PEARL, Items.LEAD);
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(TARGET, INodeInput.makeInput((ref) -> ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.ENT));
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
					if(!NodeTickHandler.canInteractWithBlock(tricksy, blockPos))
						return Result.FAILURE;
					tricksy.logStatus(Text.literal("Using ").append(tricksy.getMainHandStack().getName()).append(" on ").append(tricksy.getEntityWorld().getBlockState(blockPos).getBlock().getName()));
					tricksy.getLookControl().lookAt(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
					result = NodeTickHandler.useHeldOnBlock(blockPos, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				}
				else if(value.type() == TFObjType.ENT)
				{
					Entity entity = value.as(TFObjType.ENT).get();
					if(value.isEmpty() || !(entity instanceof LivingEntity) || !NodeTickHandler.canInteractWithEntity(tricksy, entity))
						return Result.FAILURE;
					tricksy.logStatus(Text.literal("Using ").append(tricksy.getMainHandStack().getName()).append(" on ").append(entity.getName()));
					tricksy.getLookControl().lookAt(entity);
					result = NodeTickHandler.useHeldOnEntity((LivingEntity)entity, (ServerWorld)tricksy.getWorld(), tricksy, BUILDER_ID);
				}
				tricksy.swingHand(Hand.MAIN_HAND);
				return result != ActionResult.FAIL ? Result.SUCCESS : Result.FAILURE;
			}
		});
	}

}
