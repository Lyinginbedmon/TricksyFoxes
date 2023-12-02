package com.lying.tricksy.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.network.SyncScriptureScreenPacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.ScriptureScreenHandler;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
		{
			if(!stack.getNbt().contains("Size", NbtElement.INT_TYPE))
			{
				NbtCompound nbt = stack.getNbt();
				nbt.putInt("Size", getTree(stack).size());
				stack.setNbt(nbt);
			}
			tooltip.add(Text.translatable(getTranslationKey()+".tooltip", stack.getNbt().getInt("Size")));
		}
		if(!ISealableItem.isSealed(stack))
			tooltip.add(Text.translatable(getTranslationKey()+".tooltip_copy").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
		if(hasTree(stack))
			tooltip.add(Text.translatable(getTranslationKey()+".tooltip_paste").setStyle(Style.EMPTY.withItalic(true).withFormatting(Formatting.GRAY)));
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		boolean hasTree = hasTree(itemStack);
		if(!world.isClient() && hasTree)
			user.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new ScriptureScreenHandler(id, itemStack), getTreeName(itemStack))).ifPresent(syncId -> SyncScriptureScreenPacket.send(user, itemStack, syncId));
		return hasTree ? TypedActionResult.success(itemStack, world.isClient()) : TypedActionResult.fail(itemStack);
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useOnTricksy(ItemStack stack, T tricksy, PlayerEntity user)
	{
		if(user.isSneaking())
		{
			if(tricksy.isSage(user) && hasTree(stack))
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
	
	public static Text getTreeName(ItemStack stack)
	{
		Text displayName = stack.getName();
		if(!ISealableItem.isSealed(stack))
			return displayName;
		
		try
		{
			displayName = Text.Serializer.fromJson(stack.getSubNbt(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
			if(displayName == null)
				return null;
		}
		catch(Exception e) { }
		return displayName;
	}
	
	public static boolean hasTree(ItemStack stack)
	{
		if(stack.getNbt().contains("HasCopied", NbtElement.BYTE_TYPE))
			return stack.getNbt().getBoolean("HasCopied");
		
		if(!stack.getNbt().contains("Tree", NbtElement.COMPOUND_TYPE))
			return false;
		
		BehaviourTree tree = getTree(stack);
		if(tree == null || tree.root() == null)
			return false;
		
		return true;
	}
	
	@Nullable
	public static BehaviourTree getTree(ItemStack stack)
	{
		return stack.getOrCreateNbt().contains("Tree", NbtElement.COMPOUND_TYPE) ?  BehaviourTree.create(stack.getOrCreateNbt().getCompound("Tree")) : null;
	}
	
	public static void setTree(BehaviourTree obj, ItemStack stack)
	{
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt("Size", obj.size());
		nbt.put("Tree", obj.storeInNbt());
		nbt.putBoolean("HasCopied", obj != null && obj.root() != null);
		stack.setNbt(nbt);
	}
}
