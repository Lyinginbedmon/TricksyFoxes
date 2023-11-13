package com.lying.tricksy.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
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
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_cycle").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
	}
	
	public static IWhiteboardObject<?> getVariable(ItemStack stack)
	{
		return IWhiteboardObject.createFromNbt(stack.getNbt().getCompound("Variable"));
	}
	
	public static void setVariable(IWhiteboardObject<?> obj, ItemStack stack)
	{
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtCompound variable = obj.writeToNbt(new NbtCompound());
		nbt.put("Variable", variable);
		stack.setNbt(nbt);
	}
	
	@Nullable
	public static WhiteboardRef createReference(ItemStack stack, BoardType board)
	{
		Text displayName = null;
		try
		{
			displayName = Text.Serializer.fromJson(stack.getSubNbt(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
			if(displayName == null)
				return null;
		}
		catch(Exception e) { }
		WhiteboardRef name = new WhiteboardRef(WhiteboardRef.conformTextToName(displayName), getVariable(stack).type(), board);
		name.displayName(displayName);
		return name;
	}
	
	public static void addVariable(IWhiteboardObject<?> obj, ItemStack stack)
	{
		IWhiteboardObject<?> variable = getVariable(stack);
		variable.tryAdd(obj);
		setVariable(variable, stack);
	}
	
	public static boolean isFinalised(ItemStack stack) { return stack.hasCustomName() && getVariable(stack).size() > 0; }
	
	public static abstract class Typed<T> extends Item implements ISealableItem, ITreeItem
	{
		private final TFObjType<T> type;
		
		protected Typed(TFObjType<T> typeIn, Settings settings)
		{
			super(settings.maxCount(16));
			this.type = typeIn;
		}
		
		public String getTranslationKey(ItemStack stack) { return TFItems.NOTE.getTranslationKey(); }
		
		public boolean canBeSealed(ItemStack stack) { return getVariable(stack).size() > 0; }
		
		public TFObjType<?> getType() { return this.type; }
		
		public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
		{
			ItemStack stack = user.getStackInHand(hand);
			if(user.isSneaking() && !ISealableItem.isSealed(stack))
				return TypedActionResult.success(new ItemStack(TFItems.NOTE, stack.getCount()));
			
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
		{
			IWhiteboardObject<?> variable = getVariable(stack);
			if(variable == null)
			{
				tooltip.add(Text.literal("Malformed NBT!"));
				return;
			}
			tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.type", variable.type().translated()));
			if(variable.size() > 0)
				tooltip.addAll(variable.describe());
		}
		
		public <N extends PathAwareEntity & ITricksyMob<?>> ActionResult useOnTricksy(ItemStack stack, N tricksy, PlayerEntity user)
		{
			if(!isFinalised(stack) || !tricksy.isSage(user) || user.isSneaking())
				return ActionResult.PASS;
			
			boolean isClient = user.getWorld().isClient();
			if(!isClient)
			{
				IWhiteboardObject<?> value = getVariable(stack);
				WhiteboardRef name = createReference(stack, BoardType.LOCAL);
				LocalWhiteboard<?> whiteboard = tricksy.getLocalWhiteboard();
				whiteboard.addValue(name, (mob) -> value);
				
				if(!user.isCreative() && !ISealableItem.isSealed(stack))
					stack.decrement(1);
				
				user.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.give_value", tricksy.getDisplayName()), true);
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.MASTER, 1F, 0.75F + (tricksy.getRandom().nextFloat() * 0.5F));
			}
			
			return ActionResult.success(isClient);
		}
	}
	
	public static class Crafting<T> extends Typed<T>
	{
		public Crafting(TFObjType<T> typeIn, Settings settings)
		{
			super(typeIn, settings);
		}
		
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
		{
			super.appendTooltip(stack, world, tooltip, context);
			if(!ISealableItem.isSealed(stack))
			{
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_craft").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.empty());
				if(TFItems.NOTES_CYCLE.contains(stack.getItem()))
					tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_cycle").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_clear").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
			}
		}
	}
	
	public static class Block extends Typed<BlockPos>
	{
		public Block(Settings settings)
		{
			super(TFObjType.BLOCK, settings);
		}
		
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
		{
			super.appendTooltip(stack, world, tooltip, context);
			if(!ISealableItem.isSealed(stack))
			{
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.empty());
				if(TFItems.NOTES_CYCLE.contains(stack.getItem()))
					tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_cycle").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_clear").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
			}
		}
		
		public ActionResult useOnBlock(ItemUsageContext context)
		{
			PlayerEntity user = context.getPlayer();
			ItemStack stack = context.getStack();
			if(!ISealableItem.isSealed(stack) && user.isSneaking())
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
		
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
		{
			super.appendTooltip(stack, world, tooltip, context);
			if(!ISealableItem.isSealed(stack))
			{
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.empty());
				if(TFItems.NOTES_CYCLE.contains(stack.getItem()))
					tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_cycle").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
				tooltip.add(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.tooltip_clear").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
			}
		}
		
		public static void addEntityToStack(ItemStack stack, LivingEntity entity)
		{
			WhiteboardObjEntity value = (WhiteboardObjEntity)getVariable(stack).as(TFObjType.ENT);
			value.add(entity);
			setVariable(value, stack);
		}
	}
}
