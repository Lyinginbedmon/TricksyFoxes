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
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.entity.EntityType;
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
	private boolean isMaster = false;
	/** Displayed sub tree */
	private Order showSubTree = null;
	
	/** All references available to the mob, categorised by their whiteboard type */
	private Map<BoardType, Map<WhiteboardRef, IWhiteboardObject<?>>> references = new HashMap<>();
	
	/** Set of references flagged to be deleted when the tree is saved */
	private List<WhiteboardRef> markedForDeletion = Lists.newArrayList();
	/** Set of references flagged to be added when the tree is saved */
	private List<WhiteboardRef> addedReferences = Lists.newArrayList();
	
	/** The specific mob being interacted with */
	private ITricksyMob<?> tricksy = null;
	private PathAwareEntity tricksyMob = null;
	private UUID tricksyID;
	
	/** Current size of the displayed behaviour tree, cached */
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
	
	public boolean canSyncToServer() { return this.tricksyID != null; }
	
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
		Map<WhiteboardRef, IWhiteboardObject<?>> entries = new HashMap<>();
		references.getOrDefault(board, new HashMap<>()).entrySet().forEach(entry -> entries.put(entry.getKey(), entry.getValue()));
		if(board == BoardType.LOCAL)
			addedReferences.forEach(ref -> entries.put(ref, ref.type().blank()));
		return entries;
	}
	
	public void setTricksy(ITricksyMob<?> mobIn) { this.tricksy = mobIn; }
	
	public void setTree(BehaviourTree treeIn) { this.tricksyTree = treeIn; }
	
	public BehaviourTree getTree() { return tricksyTree; }
	
	public TreeNode<?> root() { return getTree().root(showSubTree); }
	
	public boolean showSubTrees() { return this.isMaster; }
	
	public void sync(ITricksyMob<?> tricksyIn, PathAwareEntity mobIn)
	{
		this.tricksy = tricksyIn;
		this.tricksyMob = mobIn;
		resetTree();
	}
	
	@Nullable
	public EntityType<?> getTricksyType() { return this.tricksyMob != null ? this.tricksyMob.getType() : null; }
	
	public void setUUID(UUID idIn) { this.tricksyID = idIn; }
	
	public void setMaster(boolean bool) { this.isMaster = bool; }
	
	public void showSubTree(Order orderIn)
	{
		this.showSubTree = orderIn;
		countNodes();
	}
	
	public void setCap(int capIn)
	{
		this.treeSizeCap = capIn;
	}
	
	public void setAvailableReferences(List<Pair<WhiteboardRef, IWhiteboardObject<?>>> refsIn)
	{
		references.clear();
		BoardType.displayOrder().forEach(type -> references.put(type, new HashMap<>()));
		refsIn.forEach((pair) -> references.get(pair.getLeft().boardType()).put(pair.getLeft(), pair.getRight()));
	}
	
	public void resetTree()
	{
		if(tricksy != null)
			this.tricksyTree = tricksy.getBehaviourTree().copy();
		else
			this.tricksyTree = new BehaviourTree();
		
		markedForDeletion.clear();
		addedReferences.clear();
		countNodes();
	}
	
	public void countNodes()
	{
		this.treeSize = this.root().branchSize();
	}
	
	public UUID tricksyUUID() { return this.tricksyID; }
	
	public void markForDeletion(WhiteboardRef reference)
	{
		/** Only permit removing references from the global or local whiteboards */
		if(reference.boardType() == BoardType.CONSTANT)
			return;
		
		if(!isMarkedForDeletion(reference))
			markedForDeletion.add(reference);
		else
			markedForDeletion.removeIf((marked) -> marked.isSameRef(reference));
	}
	
	public void addBlankReference(WhiteboardRef reference)
	{
		/** Only permit adding new cachable references to the local whiteboard */
		if(reference.boardType() != BoardType.LOCAL || reference.uncached())
			return;
		
		/** If a reference matches one marked for deletion, unmark it */
		if(markedForDeletion.removeIf(marked -> marked.isSameRef(reference)))
			return;
		
		/** If a reference already exists amongst the references as a system value, ignore it */
		for(WhiteboardRef ref : references.getOrDefault(reference.boardType(), new HashMap<>()).keySet())
			if(ref.uncached() && ref.isSameRef(reference))
				return;
		
		addedReferences.removeIf(marked -> marked.isSameRef(reference));
		addedReferences.add(reference);
	}
	
	public boolean isMarkedForDeletion(WhiteboardRef ref)
	{
		return markedForDeletion.stream().anyMatch(marked -> marked.isSameRef(ref));
	}
	
	public boolean isNew(WhiteboardRef ref)
	{
		return addedReferences.stream().anyMatch(marked -> marked.isSameRef(ref));
	}
	public List<WhiteboardRef> getAdditions() { return this.addedReferences; }
	public List<WhiteboardRef> getDeletions() { return this.markedForDeletion; }
	
	public void onClosed(PlayerEntity player)
	{
		super.onClosed(player);
		if(tricksy != null)
			tricksy.setCustomer(null);
	}
}
