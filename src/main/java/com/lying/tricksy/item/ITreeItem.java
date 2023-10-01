package com.lying.tricksy.item;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface ITreeItem
{
	public <T extends PathAwareEntity & ITricksyMob<?>> ActionResult useOnTricksy(ItemStack stack, T tricksy, PlayerEntity user);
}
