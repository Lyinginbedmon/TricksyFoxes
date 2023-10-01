package com.lying.tricksy.item;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Rarity;

public class ItemSageHat extends DyeableArmorItem implements ITreeItem
{
	public ItemSageHat(Settings settings)
	{
		super(ArmorMaterials.LEATHER, Type.HELMET, settings.maxCount(1).fireproof().rarity(Rarity.EPIC));
	}
	
	/** Returns the UUID stored in this hat, or null if there isn't one */
	@Nullable
	public static UUID getMasterID(ItemStack stack)
	{
		NbtCompound stackData = stack.getOrCreateNbt();
		if(stackData.contains("MasterID"))
			return stackData.getUuid("MasterID");
		else
			return null;
	}
	
	/** Returns the UUID stored in this hat, or sets it to that of the living entity if there isn't one */
	@Nullable
	public static UUID getSageID(ItemStack stack, LivingEntity living)
	{
		NbtCompound stackData = stack.getOrCreateNbt();
		if(stackData.contains("MasterID"))
			return stackData.getUuid("MasterID");
		else if(living != null)
		{
			UUID id = living.getUuid();
			stackData.putUuid("MasterID", id);
			stack.setNbt(stackData);
			return id;
		}
		
		return null;
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useOnTricksy(ItemStack stack, T tricksy, PlayerEntity user)
	{
		if(!tricksy.hasSage() || tricksy.isSage(user))
		{
			tricksy.setSage(ItemSageHat.getSageID(stack, user));
			user.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".sage_hat.master_set", tricksy.getDisplayName()), true);
			return ActionResult.success(tricksy.getWorld().isClient());
		}
		else
		{
			user.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".sage_hat.master_set.fail", tricksy.getDisplayName()), true);
			return ActionResult.FAIL;
		}
	}
}
