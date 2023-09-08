package com.lying.tricksy.entity.ai;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

/**
 * Base class for behaviour tree nodes.
 * TODO Implement ticking, storage, and recreation from memory
 * @author Lying
 */
public abstract class TreeNode
{
	/** A unique identifier, stored by behaviour trees to reduce unnecessary ticking in large trees by only ticking the running node */
	@NotNull
	private final UUID nodeID;
	
	/** The result returned when this node was last ticked */
	@NotNull
	protected Result lastResult = Result.READY;
	
	@Nullable
	private TreeNode parent = null;
	private Collection<TreeNode> children = Lists.newArrayList();
	
	protected TreeNode(UUID uuidIn)
	{
		this.nodeID = uuidIn;
	}
	
	/** Returns the unique ID of this node */
	public final UUID getID() { return this.nodeID; }
	
	public final void addChild(TreeNode childIn) { children.add(childIn.setParent(this)); }
	
	public final void removeChild(TreeNode nodeIn) { removeChild(nodeIn.getID()); }
	
	public final void removeChild(UUID uuidIn) { children.removeIf((node) -> node.getID().equals(uuidIn)); }
	
	public Collection<TreeNode> children() { return children; }
	
	/** Returns true if this node has no parent and, hence, is a root node */
	public final boolean isRoot() { return parent != null; }
	
	protected TreeNode setParent(TreeNode node) { parent = node; return this; }
	
	@Nullable
	public TreeNode parent() { return this.parent; }
	
	/** Returns true if the previous tick of this node did not end in a completion result */
	public boolean isRunning() { return !lastResult.isEnd(); }
	
	/** Returns the node with the given ID, if it exists at or below this node in the tree */
	@Nullable
	public final TreeNode getByID(UUID uuidIn)
	{
		if(nodeID.equals(uuidIn))
			return this;
		
		for(TreeNode child : children)
		{
			TreeNode result = child.getByID(uuidIn);
			if(result != null)
				return result;
		}
		
		return null;
	}
	
	public static enum Result
	{
		READY,
		RUNNING,
		SUCCESS,
		FAILURE;
		
		public boolean isEnd() { return this != RUNNING; }
	}
}