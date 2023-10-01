package com.lying.tricksy.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class ItemScripture extends Item implements ISealableItem, ITreeItem
{
	public ItemScripture(Settings settings)
	{
		super(settings);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		if(hasTree(stack))
			tooltip.add(Text.translatable(getTranslationKey()+".tooltip", getTree(stack).size()));
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useOnTricksy(ItemStack stack, T tricksy, PlayerEntity user)
	{
		if(user.isSneaking() && tricksy.isSage(user))
		{
			if(hasTree(stack))
			{
				user.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".scripture.paste", tricksy.getDisplayName()), true);
				tricksy.setBehaviourTree(stack.getOrCreateSubNbt("Tree"));
				return ActionResult.success(user.getWorld().isClient());
			}
			else
				return ActionResult.FAIL;
		}
		else if(!ISealableItem.isSealed(stack))
		{
			user.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".scripture.copy", stack.getName(), tricksy.getDisplayName()), true);
			setTree(tricksy.getBehaviourTree(), stack);
			return ActionResult.success(user.getWorld().isClient());
		}
		
		return ActionResult.PASS;
	}
	
	public static boolean hasTree(ItemStack stack) { return getTree(stack) != null; }
	
	@Nullable
	public static BehaviourTree getTree(ItemStack stack)
	{
		return stack.getOrCreateNbt().contains("Tree", NbtElement.COMPOUND_TYPE) ?  BehaviourTree.create(stack.getOrCreateNbt().getCompound("Tree")) : null;
	}
	
	public static void setTree(BehaviourTree obj, ItemStack stack)
	{
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.put("Tree", obj.storeInNbt());
		stack.setNbt(nbt);
	}
}
