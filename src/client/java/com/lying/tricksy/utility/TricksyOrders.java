package com.lying.tricksy.utility;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.item.IOrderGivingItem;
import com.lying.tricksy.network.GiveOrderPacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TricksyOrders
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final double TARGET_RANGE = 32D;
	
	private static IWhiteboardObject<?> orderTarget = null;
	
	private static List<Order> orderOptions = Lists.newArrayList();
	private static int orderIndex = 0;
	
	private static boolean showOrders = false;
	private static int itemColor = -1;
	
	public static boolean shouldRenderOrders()
	{
		PlayerEntity player = mc.player;
		if(player == null || player.getActiveItem().isEmpty() || !(player.getActiveItem().getItem() instanceof IOrderGivingItem))
		{
			clear();
			return false;
		}
		
		return showOrders && !orderOptions.isEmpty();
	}
	
	@Nullable
	public static Order currentOrder() { return orderOptions.isEmpty() ? null : orderOptions.get(orderIndex); }
	
	public static int orderIndex() { return orderIndex; }
	
	public static IWhiteboardObject<?> currentTarget() { return orderTarget; }
	
	public static List<Order> options() { return orderOptions; }
	
	public static Order getNextLast(int step)
	{
		return orderOptions.get(conformToAvailableOptions(orderIndex + step));
	}
	
	@Nullable
	private static HitResult getTarget()
	{
		Entity ent = mc.getCameraEntity();
		if(ent == null)
			return null;
		Vec3d lookPos = ent.getCameraPosVec(0F);
		Vec3d lookVec = ent.getRotationVec(1F);
		Vec3d lookEnd = lookPos.add(lookVec.x * TARGET_RANGE,  lookVec.y * TARGET_RANGE,  lookVec.z * TARGET_RANGE);
		Box box = ent.getBoundingBox().stretch(lookVec.multiply(TARGET_RANGE)).expand(1, 1, 1);
		EntityHitResult entityHitResult = ProjectileUtil.raycast((Entity)ent, (Vec3d)lookPos, (Vec3d)lookEnd, (Box)box, entity -> !entity.isSpectator() && entity.canHit(), TARGET_RANGE * TARGET_RANGE);
		return entityHitResult == null ? mc.getCameraEntity().raycast(TARGET_RANGE, 0F, false) : entityHitResult;
	}
	
	public static void setTarget(int itemColorIn)
	{
		setTarget(TFObjType.EMPTY.blank());
		HitResult target = getTarget();
		if(target != null)
			switch(target.getType())
			{
				case BLOCK:
					setTarget(new WhiteboardObjBlock(((BlockHitResult)target).getBlockPos()));
					break;
				case ENTITY:
					setTarget(new WhiteboardObjEntity(((EntityHitResult)target).getEntity()));
					break;
				case MISS:
				default:
					break;
			}
		
		showOrders = true;
		itemColor = itemColorIn;
	}
	
	public static void incOrder(int amount)
	{
		if(!shouldRenderOrders() || orderOptions.size() < 2)
			return;
		
		orderIndex = conformToAvailableOptions(orderIndex + amount);
	}
	
	private static int conformToAvailableOptions(int index)
	{
		if(orderOptions.isEmpty())
			return 0;
		
		while(index < 0)
			index += orderOptions.size();
		
		while(index >= orderOptions.size())
			index %= orderOptions.size();
		
		return index;
	}
	
	public static void setTarget(IWhiteboardObject<?> obj)
	{
		if(mc.world == null)
		{
			clear();
			return;
		}
		
		orderTarget = obj;
		orderOptions = Order.getOrdersFor(obj.type());
	}
	
	public static void clear()
	{
		orderTarget = null;
		orderIndex = 0;
		showOrders = false;
		itemColor = -1;
	}
	
	public static void sendOrder()
	{
		if(showOrders && currentOrder() != null && currentTarget() != null)
		{
			Order order = currentOrder();
			IWhiteboardObject<?> val = currentTarget();
			mc.player.sendMessage(order.translate(val), true);
			GiveOrderPacket.send(mc.player, val, order, itemColor);
		}
		
		clear();
	}
}
