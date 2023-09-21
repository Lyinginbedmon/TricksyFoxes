package com.lying.tricksy.screen;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.lying.tricksy.network.SaveTreePacket;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class TreeScreenHandler extends ScreenHandler
{
	private BehaviourTree tricksyTree = new BehaviourTree();
	private List<WhiteboardRef> references = Lists.newArrayList();
	private ITricksyMob<?> tricksy = null;
	private UUID tricksyID;
	private boolean shouldWrite = false;
	
	public <T extends PathAwareEntity & ITricksyMob<?>> TreeScreenHandler(int syncId, T tricksyIn)
	{
		this(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId, tricksyIn);
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> TreeScreenHandler(ScreenHandlerType<?> type, int syncId, @NotNull T tricksyIn)
	{
		super(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId);
		if(tricksyIn != null)
			sync(tricksyIn, tricksyIn.getUuid());
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return var1.isCreative() || tricksy != null && tricksy.isSage(var1); }
	
	public List<WhiteboardRef> getMatches(Predicate<WhiteboardRef> predicate)
	{
		List<WhiteboardRef> options = Lists.newArrayList();
		references.forEach((ref) -> { if(predicate.test(ref)) options.add(ref); });
		return options;
	}
	
	public void setTricksy(ITricksyMob<?> mobIn) { this.tricksy = mobIn; }
	
	public void setTree(BehaviourTree treeIn)
	{
		this.tricksyTree = treeIn;
	}
	
	public BehaviourTree getTree() { return tricksyTree; }
	
	public void setWrite(boolean var) { this.shouldWrite = var; }
	
	public void sync(@NotNull ITricksyMob<?> tricksyIn, UUID mobID)
	{
		this.tricksy = tricksyIn;
		this.tricksyID = mobID;
		
		references.clear();
		references.addAll(Whiteboard.CONSTANTS.allReferences());
		if(tricksy != null)
		{
			references.addAll(tricksyIn.getLocalWhiteboard().allReferences());
			// TODO Add references from global whiteboard
		}
		
		resetTree();
	}
	
	public void resetTree()
	{
		if(tricksy != null)
			this.tricksyTree = tricksy.getBehaviourTree().copy();
		else
			this.tricksyTree = new BehaviourTree();
	}
	
	public void onClosed(PlayerEntity player)
	{
		if(player.getWorld().isClient() && shouldWrite && tricksy != null)
			SaveTreePacket.send(player, tricksyID, tricksyTree);
	}

}
