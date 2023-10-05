package com.lying.tricksy.screen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.lying.tricksy.item.ItemScripture;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ScriptureScreenHandler extends ScreenHandler
{
	private TreeNode<?> root;
	
	public ScriptureScreenHandler(int syncId, ItemStack stack)
	{
		super(TFScreenHandlerTypes.SCRIPTURE_SCREEN_HANDLER, syncId);
		if(stack != null)
			root = ItemScripture.getTree(stack).root();
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return true; }
	
	@Nullable
	public TreeNode<?> getRoot() { return root; }
	
	public void setScripture(@NotNull ItemStack stack)
	{
		this.root = ItemScripture.getTree(stack).root();
	}
}
