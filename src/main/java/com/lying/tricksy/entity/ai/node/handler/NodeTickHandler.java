package com.lying.tricksy.entity.ai.node.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.fakeplayer.ServerFakePlayer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
	
	/** Accept only values of the given type */
	public static Predicate<WhiteboardRef> ofType(TFObjType<?> typeIn) { return (ref) -> ref.type().castableTo(typeIn); }
	/** Accept any value from anywhere */
	public static Predicate<WhiteboardRef> any() { return Predicates.alwaysTrue(); }
	/** Accept any value from the local whiteboard */
	public static Predicate<WhiteboardRef> anyLocal() { return (ref) -> ref.boardType() == BoardType.LOCAL; }
	
	/** Returns a map containing all necessary variables of this behaviour and predicates defining their needs */
	@NotNull
	public default Map<WhiteboardRef, INodeInput> variableSet(){ return new HashMap<>(); }
	
	public default boolean variablesSufficient(M parent) { return !noVariableMissing(parent); }
	
	/**
	 * Returns true if any variable in {@link variableSet} is unassigned in the given parent node<br>
	 * Note: This does NOT account for whether the target value is empty or not.
	 */
	public default boolean noVariableMissing(M parent)
	{
		if(variableSet().isEmpty())
			return false;
		
		for(Entry<WhiteboardRef, INodeInput> entry : variableSet().entrySet())
			if(entry.getValue().isOptional())
				continue;
			else if(!parent.variableAssigned(entry.getKey()) || !entry.getValue().predicate().test(parent.variable(entry.getKey())))
				return true;
		
		return false;
	}
	
	/** Returns the value associated with the given input by the given parent node, or its default value if it is optional */
	@Nullable
	public default IWhiteboardObject<?> getOrDefault(WhiteboardRef input, M parent, Whiteboard.Local<?> local, Whiteboard.Global global)
	{
		if(!parent.variableAssigned(input))
			return variableSet().get(input).isOptional() ? variableSet().get(input).defaultValue().get() : null;
		else
			return Whiteboard.get(parent.variable(input), local, global);
	}
	
	/** Performs a single tick of this node */
	@NotNull
	public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, M parent);
	
	/** Performs any logic needed when the node stops */
	public default <T extends PathAwareEntity & ITricksyMob<?>> void stop(T tricksy, M parent) { }
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> boolean canInteractWithBlock(T tricksy, BlockPos pos)
	{
		return tricksy.getEyePos().distanceTo(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) < INTERACT_RANGE;
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
	
	private static <T extends PathAwareEntity & ITricksyMob<?>> void updateFromPlayer(T tricksy, PlayerEntity player)
	{
		ItemStack stack = tricksy.getMainHandStack().copy();
		tricksy.setStackInHand(Hand.MAIN_HAND, player.getMainHandStack().copy());
		tricksy.setStackInHand(Hand.OFF_HAND, player.getOffHandStack().copy());
		tricksy.getLocalWhiteboard().setItemCooldown(stack.getItem(), (int)player.getItemCooldownManager().getCooldownProgress(stack.getItem(), 0));
	}
	
	public static boolean matchesEntityFilter(Entity stack, IWhiteboardObject<Entity> filter)
	{
		if(filter.size() == 0)
			return true;
		
		for(Entity option : filter.getAll())
			if(stack.getType() == option.getType())
				return true;
		return false;
	}
}
