package com.lying.tricksy.entity.ai.node.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface NodeTickHandler<M extends TreeNode<?>>
{
	public static final double INTERACT_RANGE = 4D;
	
	/** Returns a map containing all necessary input variables of this behaviour and predicates defining their needs */
	@NotNull
	public default Map<WhiteboardRef, INodeInput> inputSet(){ return new HashMap<>(); }
	
	@Nullable
	public default INodeInput inputCondition(WhiteboardRef name)
	{
		for(Entry<WhiteboardRef, INodeInput> entry : inputSet().entrySet())
			if(entry.getKey().isSameRef(name))
				return entry.getValue();
		return null;
	}
	
	/** Returns true if the operational needs of this handler have been met */
	public default boolean inputsSufficient(M parent) { return !anyInputMissing(parent); }
	
	/**
	 * Returns true if any variable in {@link inputSet} is unassigned in the given parent node<br>
	 * Note: This does NOT account for whether a whiteboard target value is empty or not.
	 */
	public default boolean anyInputMissing(M parent)
	{
		if(inputSet().isEmpty())
			return false;
		
		for(Entry<WhiteboardRef, INodeInput> entry : inputSet().entrySet())
		{
			INodeInput qualifier = entry.getValue();
			if(qualifier.isOptional())
				continue;
			else
			{
				INodeValue assigned = parent.getInput(entry.getKey());
				if(assigned == null)
					return true;
				
				// Ensure assigned value is appropriate for this input
				switch(assigned.type())
				{
					case STATIC:
						// Static values are presumed to be appropriate at input
						break;
					case WHITEBOARD:
						if(!qualifier.predicate().test(((WhiteboardValue)assigned).assignment()))
							return true;
						break;
				}
			}
		}
		
		return false;
	}
	
	/** Returns the value associated with the given input by the given parent node, or its default value if it is optional */
	@Nullable
	public default IWhiteboardObject<?> getOrDefault(WhiteboardRef input, M parent, LocalWhiteboard<?> local, GlobalWhiteboard global)
	{
		if(!parent.inputAssigned(input))
			return inputCondition(input).isOptional() ? inputCondition(input).defaultValue().get() : null;
		else
			return parent.getInput(input).get(local, global);
	}
	
	/** Performs a single tick of this node */
	@NotNull
	public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, M parent);
	
	/** Performs any logic needed when the node stops */
	public default <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, M parent) { }
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> boolean canInteractWithBlock(T tricksy, BlockPos pos)
	{
		Vec3d position = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
		return tricksy.getEyePos().distanceTo(position) <= INTERACT_RANGE || tricksy.getBlockPos().isWithinDistance(pos, INTERACT_RANGE);
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> boolean canInteractWithEntity(T tricksy, Entity pos)
	{
		return tricksy.distanceTo(pos) < INTERACT_RANGE;
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> ActionResult activateBlock(BlockPos blockPos, ServerWorld world, T tricksy, Identifier builderID)
	{
		BlockState state = world.getBlockState(blockPos);
		ServerFakePlayer player = ServerFakePlayer.makeForMob(tricksy, builderID);
		Vec3d hitPos = new Vec3d(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
		Vec3d hitDir = hitPos.subtract(tricksy.getEyePos()).normalize().negate();
		BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.getFacing(hitDir.x, hitDir.y, hitDir.z), blockPos, false);
		
		ActionResult result = state.onUse(world, player, Hand.MAIN_HAND, hitResult);
		updateFromPlayer(tricksy, player);
		player.discard();
		return result;
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useHeldOnBlock(BlockPos blockPos, ServerWorld world, T tricksy, Identifier builderID)
	{
		ServerFakePlayer player = ServerFakePlayer.makeForMob(tricksy, builderID);
		Vec3d hitPos = new Vec3d(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
		Vec3d hitDir = hitPos.subtract(tricksy.getEyePos()).normalize().negate();
		Direction face = Direction.getFacing(hitDir.x, hitDir.y, hitDir.z);
		BlockHitResult hitResult = new BlockHitResult(hitPos, face, blockPos, false);
		
		ActionResult result = player.getMainHandStack().useOnBlock(new ItemUsageContext(player, Hand.MAIN_HAND, hitResult));
		updateFromPlayer(tricksy, player);
		player.discard();
		return result;
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useHeldOnEntity(LivingEntity living, ServerWorld world, T tricksy, Identifier builderID)
	{
		ServerFakePlayer player = ServerFakePlayer.makeForMob(tricksy, builderID);
		ActionResult result = living.interact(player, Hand.MAIN_HAND);
		updateFromPlayer(tricksy, player);
		player.discard();
		return result;
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> void updateFromPlayer(T tricksy, PlayerEntity player)
	{
		ItemStack stack = tricksy.getMainHandStack().copy();
		for(EquipmentSlot slot : EquipmentSlot.values())
			tricksy.equipStack(slot, player.getEquippedStack(slot));
		tricksy.getLocalWhiteboard().setItemCooldown(stack.getItem(), (int)player.getItemCooldownManager().getCooldownProgress(stack.getItem(), 0));
	}
	
	public static boolean matchesEntityFilter(Entity entity, @Nullable IWhiteboardObject<Entity> filter)
	{
		return filter == null ? true : ((WhiteboardObjEntity)filter).matches(entity);
	}
	
	public static void swingHand(LivingEntity entity, Hand hand)
	{
		entity.swingHand(hand);
		// FIXME Ensure swing animation actually plays
		if(!entity.getWorld().isClient())
		{
			EntityAnimationS2CPacket packet = new EntityAnimationS2CPacket(entity, hand == Hand.MAIN_HAND ? EntityAnimationS2CPacket.SWING_MAIN_HAND : EntityAnimationS2CPacket.SWING_OFF_HAND);
			entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class, entity.getBoundingBox().expand(16D), Predicates.alwaysTrue()).forEach((player) ->  player.networkHandler.sendPacket(packet));
		}
	}
}
