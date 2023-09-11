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
	
	/**
	 * TODO NODE TYPES
	 * Nodes are divided by a NodeType which contains a subset of variants to select from
	 * Variants may have additional options to choose, as well as different input requirements
	 * 
	 * Root	- The initial node, always present, and has no parent (Necessary? Why not just allow a control flow node?)
	 * Control Flow	- Executes child nodes in particular ways
	 * 		Selector	- Executes the first node that does not return failure
	 * 		Sequential	- Executes each node one after the other until end or one returns failure
	 * 		Reactive	- Executes all nodes until any return failure or all return success
	 * Condition	- Monitors values and returns success or failure based on them, may accept object references
	 * Decorator	- Alters the result or modifies the operation of a singular child node
	 * 		Force failure	- Always returns failure
	 * 		Force success	- Always returns success
	 * 		Inverter		- Returns the opposite of the child node (failure = success, success = failure, running unchanged)
	 * 		Repeat			- Runs the child N times or until it fails
	 * 		Retry			- Runs the child N times or until it succeeds
	 * 		Delay			- Runs the child after N ticks
	 * Leaf	- Performs an action and has no child nodes
	 * 		Action	- Performs a base singular action from a predefined set
	 * 		SubTree	- Performs a predefined complex action that would otherwise necessitate multiple nodes, such as melee combat
	 */
	
	/** The result returned when this node was last ticked */
	@NotNull
	protected Result lastResult = Result.FAILURE;
	
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
		RUNNING,
		SUCCESS,
		FAILURE;
		
		public boolean isEnd() { return this != RUNNING; }
	}
}