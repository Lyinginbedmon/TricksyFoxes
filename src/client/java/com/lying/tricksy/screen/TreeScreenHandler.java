package com.lying.tricksy.screen;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class TreeScreenHandler extends ScreenHandler
{
	private BehaviourTree tricksyTree = new BehaviourTree();
	private ITricksyMob<?> tricksy = null;
	private boolean shouldWrite = false;
	
	public TreeScreenHandler(int syncId, ITricksyMob<?> tricksyIn)
	{
		this(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId, tricksyIn);
	}
	
	public TreeScreenHandler(ScreenHandlerType<?> type, int syncId, ITricksyMob<?> tricksyIn)
	{
		super(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId);
		this.tricksy = tricksyIn;
		resetTree();
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return var1.isCreative() || tricksy != null && tricksy.isSage(var1); }
	
	public void setTricksy(ITricksyMob<?> mobIn) { this.tricksy = mobIn; }
	
	public void setTree(BehaviourTree treeIn)
	{
		this.tricksyTree = treeIn;
	}
	
	public BehaviourTree getTree()
	{
		return tricksyTree;
	}
	
	public void setWrite(boolean var) { this.shouldWrite = var; }
	
	public void resetTree()
	{
		if(tricksy != null)
			this.tricksyTree = tricksy.getBehaviourTree().copy();
		else
			this.tricksyTree = new BehaviourTree();
	}
	
	public void onClosed(PlayerEntity player)
	{
		if(!player.getWorld().isClient() && shouldWrite && tricksy != null)
			tricksy.setBehaviourTree(tricksyTree.storeInNbt());
	}

}
