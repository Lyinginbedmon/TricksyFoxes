package com.lying.tricksy.item;

import java.util.List;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommandWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.CommandWhiteboard.Order;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemSageFan extends Item
{
	public ItemSageFan(Settings settings)
	{
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(!world.isClient())
		{
			List<PathAwareEntity> tricksys = world.getEntitiesByClass(PathAwareEntity.class, user.getBoundingBox().expand(16D), (mob) -> mob.isAlive() && mob instanceof ITricksyMob<?>);
			if(tricksys.isEmpty())
				return TypedActionResult.fail(itemStack);
			
			tricksys.sort((mob1,mob2) -> 
			{
				double dist1 = mob1.distanceTo(user);
				double dist2 = mob2.distanceTo(user);
				return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
			});
			
			ITricksyMob<?> tricksy = (ITricksyMob<?>)tricksys.get(0);
			if(tricksy.isSage(user))
				tricksy.getBehaviourTree().giveCommand(CommandWhiteboard.ofCommand(Order.GOTO));
		}
		return TypedActionResult.success(itemStack, world.isClient());
	}
}
