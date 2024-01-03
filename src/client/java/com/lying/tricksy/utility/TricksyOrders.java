package com.lying.tricksy.utility;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.network.GiveOrderPacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public class TricksyOrders
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	
	private static IWhiteboardObject<?> orderTarget = null;
	
	private static List<Order> orderOptions = Lists.newArrayList();
	private static int orderInd = 0;
	
	private static boolean showOrders = false;
	
	public static boolean shouldRenderOrders()
	{
		PlayerEntity player = mc.player;
		if(player == null || player.getActiveItem().isEmpty() || player.getActiveItem().getItem() != TFItems.SAGE_FAN)
			return showOrders = false;
		
		return showOrders && !orderOptions.isEmpty();
	}
	
	@Nullable
	public static Order currentOrder() { return orderOptions.isEmpty() ? null : orderOptions.get(orderInd); }
	
	public static int orderInd() { return orderInd; }
	
	public static IWhiteboardObject<?> currentTarget() { return orderTarget; }
	
	public static List<Order> options() { return orderOptions; }
	
	public static Order getNextLast(int step)
	{
		return orderOptions.get(conformToAvailableOptions(orderInd + step));
	}
	
	public static void setTarget()
	{
		// FIXME Cast ray trace to max range, instead of using crosshair
		switch(mc.crosshairTarget.getType())
		{
			case BLOCK:
				setTarget(new WhiteboardObjBlock(((BlockHitResult)mc.crosshairTarget).getBlockPos()));
				showOrders = true;
				break;
			case ENTITY:
				setTarget(new WhiteboardObjEntity(((EntityHitResult)mc.crosshairTarget).getEntity()));
				showOrders = true;
				break;
			case MISS:
				setTarget(TFObjType.EMPTY.blank());
			default:
				break;
		}
	}
	
	public static void incOrder(int amount)
	{
		if(!shouldRenderOrders())
			return;
		
		orderInd = conformToAvailableOptions(orderInd + amount);
	}
	
	private static int conformToAvailableOptions(int index)
	{
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
		orderInd = 0;
		showOrders = false;
	}
	
	public static void sendOrder(PlayerEntity player)
	{
		if(showOrders && currentOrder() != null)
			GiveOrderPacket.send(player, orderTarget, currentOrder());
		
		clear();
	}
}
