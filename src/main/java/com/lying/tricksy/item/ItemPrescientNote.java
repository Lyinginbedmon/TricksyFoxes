package com.lying.tricksy.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBase;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPrescientNote extends Item
{
	public ItemPrescientNote(Settings settings)
	{
		super(settings.maxCount(16));
	}
	
	public static IWhiteboardObject<?> getVariable(ItemStack stack)
	{
		return WhiteboardObjBase.createFromNbt(stack.getOrCreateSubNbt("Variable"));
	}
	
	public static void setVariable(IWhiteboardObject<?> obj, ItemStack stack)
	{
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.put("Variable", obj.writeToNbt(new NbtCompound()));
		stack.setNbt(nbt);
	}
	
	public static void addVariable(IWhiteboardObject<?> obj, ItemStack stack)
	{
		IWhiteboardObject<?> variable = getVariable(stack);
		variable.tryAdd(obj);
		setVariable(variable, stack);
	}
	
	public static abstract class Typed<T> extends Item
	{
		private final TFObjType<T> type;
		
		protected Typed(TFObjType<T> typeIn, Settings settings)
		{
			super(settings.maxCount(16));
			this.type = typeIn;
		}
		
		public String getTranslationKey(ItemStack stack) { return TFItems.NOTE.getTranslationKey(); }
		
		public TFObjType<?> getType() { return this.type; }
		
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
		{
			ItemStack stack = user.getStackInHand(hand);
			if(user.isSneaking())
				return TypedActionResult.success(new ItemStack(TFItems.NOTE, stack.getCount()));
			
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
		{
			IWhiteboardObject<?> variable = getVariable(stack);
			tooltip.add(Text.translatable(getTranslationKey(stack)+".type", variable.type().translated()));
			if(variable.size() > 0)
				tooltip.addAll(variable.describe());
		}
	}
	
	public static class Block extends Typed<BlockPos>
	{
		public Block(Settings settings)
		{
			super(TFObjType.BLOCK, settings);
		}
		
		public ActionResult useOnBlock(ItemUsageContext context)
		{
			PlayerEntity user = context.getPlayer();
			ItemStack stack = context.getStack();
			if(user.isSneaking())
			{
				boolean isClient = user.getWorld().isClient();
				if(!isClient)
				{
					WhiteboardObjBlock value = (WhiteboardObjBlock)getVariable(stack);
					value.add(new WhiteboardObjBlock(context.getBlockPos(), context.getSide()));
					setVariable(value, stack);
				}
				
				return ActionResult.success(isClient);
			}
			
			return ActionResult.PASS;
		}
	}
	
	public static class Ent extends Typed<Entity>
	{
		public Ent(Settings settings)
		{
			super(TFObjType.ENT, settings);
		}
		
		public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand)
		{
			if(user.isSneaking())
			{
				boolean isClient = user.getWorld().isClient();
				if(!isClient)
				{
					WhiteboardObjEntity value = (WhiteboardObjEntity)getVariable(stack);
					value.add(new WhiteboardObjEntity(entity));
					setVariable(value, stack);
				}
				
				return ActionResult.success(isClient);
			}
			
			return ActionResult.PASS;
		}
	}
	
	public static class Int extends Typed<Integer>
	{
		public Int(Settings settings)
		{
			super(TFObjType.INT, settings);
		}
	}
	
	public static class Bool extends Typed<Boolean>
	{
		public Bool(Settings settings)
		{
			super(TFObjType.BOOL, settings);
		}
	}
	
	public static class Items extends Typed<ItemStack>
	{
		public Items(Settings settings)
		{
			super(TFObjType.ITEM, settings);
		}
	}
}
