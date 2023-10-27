package com.lying.tricksy.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Pair;

public class TricksyTreeScreenHandler extends ScreenHandler implements ITricksySyncable
{
	/** The mob's current behaviour tree */
	private BehaviourTree tricksyTree = new BehaviourTree();
	/** All references available to the mob, categorised by their whiteboard type */
	private Map<BoardType, Map<WhiteboardRef, IWhiteboardObject<?>>> references = new HashMap<>();
	
	private List<WhiteboardRef> markedForDeletion = Lists.newArrayList();
	
	/** The specific mob being interacted with */
	private ITricksyMob<?> tricksy = null;
	private PathAwareEntity tricksyMob = null;
	private UUID tricksyID;
	
	/** Current size of the behaviour tree, cached */
	private int treeSize;
	/** Server-set limit of how large the behaviour tree can get */
	private int treeSizeCap;
	
	public <T extends PathAwareEntity & ITricksyMob<?>> TricksyTreeScreenHandler(int syncId, PlayerInventory playerInventory, T tricksyIn)
	{
		this(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId, playerInventory, tricksyIn, 25);
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> TricksyTreeScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, @NotNull T tricksyIn, int sizeCap)
	{
		super(TFScreenHandlerTypes.TREE_SCREEN_HANDLER, syncId);
		if(tricksyIn != null)
		{
			tricksy = tricksyIn;
			tricksyMob = tricksyIn;
			sync(tricksyIn, tricksyIn);
			setUUID(tricksyIn.getUuid());
		}
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return var1.isCreative() || tricksy != null && tricksy.isSage(var1) && tricksyMob.distanceTo(var1) < 6D; }
	
	public boolean canAddNode() { return this.treeSize < this.treeSizeCap; }
	
	public Map<WhiteboardRef, IWhiteboardObject<?>> getMatches(Predicate<WhiteboardRef> predicate, @Nullable BoardType board)
	{
		Map<WhiteboardRef, IWhiteboardObject<?>> options = new HashMap<>();
		if(predicate == null)
			predicate = Predicates.alwaysTrue();
		
		if(board != null)
			for(Entry<WhiteboardRef, IWhiteboardObject<?>> entry : getEntriesOnBoard(board).entrySet())
			{
				if(predicate.test(entry.getKey()))
					options.put(entry.getKey(), entry.getValue());
			}
		else
			for(BoardType bd : BoardType.values())
				for(Entry<WhiteboardRef, IWhiteboardObject<?>> entry : getEntriesOnBoard(bd).entrySet())
				{
					if(predicate.test(entry.getKey()))
						options.put(entry.getKey(), entry.getValue());
				}
		return options;
	}
	
	public Map<WhiteboardRef,IWhiteboardObject<?>> getEntriesOnBoard(BoardType board)
	{
		return references.getOrDefault(board, new HashMap<>()); 
	}
	
	public void setTricksy(ITricksyMob<?> mobIn) { this.tricksy = mobIn; }
	
	public void setTree(BehaviourTree treeIn) { this.tricksyTree = treeIn; }
	
	public BehaviourTree getTree() { return tricksyTree; }
	
	public void sync(ITricksyMob<?> tricksyIn, PathAwareEntity mobIn)
	{
		this.tricksy = tricksyIn;
		this.tricksyMob = mobIn;
		resetTree();
	}
	
	public void setUUID(UUID idIn) { this.tricksyID = idIn; }
	
	public void setCap(int capIn)
	{
		this.treeSizeCap = capIn;
	}
	
	public void setAvailableReferences(List<Pair<WhiteboardRef, IWhiteboardObject<?>>> refsIn)
	{
		references.clear();
		references.put(BoardType.CONSTANT, new HashMap<>());
		references.put(BoardType.GLOBAL, new HashMap<>());
		references.put(BoardType.LOCAL, new HashMap<>());
		refsIn.forEach((pair) -> references.get(pair.getLeft().boardType()).put(pair.getLeft(), pair.getRight()));
	}
	
	public void resetTree()
	{
		if(tricksy != null)
			this.tricksyTree = tricksy.getBehaviourTree().copy();
		else
			this.tricksyTree = new BehaviourTree();
		markedForDeletion.clear();
		countNodes();
	}
	
	public void countNodes()
	{
		this.treeSize = this.tricksyTree.size();
	}
	
	public UUID tricksyUUID() { return this.tricksyID; }
	
	public void markForDeletion(WhiteboardRef reference)
	{
		if(!isMarkedForDeletion(reference))
			markedForDeletion.add(reference);
		else
			markedForDeletion.removeIf((marked) -> marked.isSameRef(reference));
	}
	
	public boolean isMarkedForDeletion(WhiteboardRef ref)
	{
		for(WhiteboardRef marked : markedForDeletion)
			if(marked.isSameRef(ref))
				return true;
		return false;
	}
	
	public Iterable<WhiteboardRef> markedForDeletion() { return this.markedForDeletion; }
	
	public void onClosed(PlayerEntity player)
	{
		super.onClosed(player);
		if(tricksy != null)
			tricksy.setCustomer(null);
	}
}
