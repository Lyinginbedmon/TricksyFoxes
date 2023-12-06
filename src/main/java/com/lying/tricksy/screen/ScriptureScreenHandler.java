package com.lying.tricksy.screen;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.lying.tricksy.item.ItemScripture;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ScriptureScreenHandler extends ScreenHandler
{
	private TreeNode<?> root;
	private boolean shouldOverrule = false;
	
	public ScriptureScreenHandler(int syncId, ItemStack stack)
	{
		super(TFScreenHandlerTypes.SCRIPTURE_SCREEN_HANDLER, syncId);
		if(stack != null)
		{
			root = ItemScripture.getTree(stack).root();
			shouldOverrule = ItemScripture.shouldOverruleInvalid(stack);
		}
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return true; }
	
	public TreeNode<?> getRoot() { return this.root == null ? TFNodeTypes.LEAF.create(UUID.randomUUID()).setSubType(null) : this.root; }
	
	public void setScripture(@NotNull ItemStack stack)
	{
		this.root = ItemScripture.getTree(stack).root();
	}
	
	public boolean shouldOverrule() { return this.shouldOverrule; }
	
	public void toggleOverrule() { this.shouldOverrule = !this.shouldOverrule; }
}
