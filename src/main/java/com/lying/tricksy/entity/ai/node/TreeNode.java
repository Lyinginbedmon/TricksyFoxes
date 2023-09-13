package com.lying.tricksy.entity.ai.node;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard.Global;
import com.lying.tricksy.entity.ai.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

/**
 * Base class for behaviour tree nodes.
 * @author Lying
 */
public abstract class TreeNode<N extends TreeNode<?>>
{
	/** A unique identifier, stored by behaviour trees to reduce unnecessary ticking in large trees by only ticking the running node */
	@NotNull
	private final UUID nodeID;
	
	/** The primary node type */
	private final NodeType<N> nodeType;
	
	/** Subtype identifier */
	protected Identifier subType;
	
	/** The result returned when this node was last ticked */
	@NotNull
	protected Result lastResult = Result.FAILURE;
	protected int ticksRunning = 0;
	
	@Nullable
	private TreeNode<?> parent = null;
	private List<TreeNode<?>> children = Lists.newArrayList();
	
	protected TreeNode(NodeType<N> typeIn, UUID uuidIn)
	{
		this.nodeType = typeIn;
		this.nodeID = uuidIn;
		this.subType = typeIn.baseSubType();
	}
	
	/** Returns the unique ID of this node */
	public final UUID getID() { return this.nodeID; }
	
	public final NodeType<?> getType() { return this.nodeType; }
	
	public final TreeNode<N> setSubType(Identifier typeIn) { this.subType = typeIn; return this; };
	
	@SuppressWarnings("unchecked")
	public final <T extends PathAwareEntity & ITricksyMob<?>> Result tick(T tricksy, Local<T> local, Global global)
	{
		if(!isRunnable())
			return this.lastResult = Result.FAILURE;
		
		if(this.lastResult.isEnd())
			this.ticksRunning = 0;
		else
			this.ticksRunning++;
		
		@Nullable
		Result result = null;
		try
		{
			NodeSubType<N> subType = this.nodeType.getSubType(this.subType);
			result = subType.call(tricksy, local, global, (N)this);
		}
		catch(Exception e) { }
		return this.lastResult = (result == null ? Result.FAILURE : result);
	}
	
	/** Returns true if this node is in a runnable condition */
	public boolean isRunnable() { return true; }
	
	/** Returns true if this node can accept the given child node */
	public boolean canAddChild(TreeNode<?> child) { return true; }
	
	public final TreeNode<?> addChild(TreeNode<?> childIn)
	{
		children.add(childIn.setParent(this));
		return this;
	}
	
	public final void removeChild(TreeNode<?> nodeIn) { removeChild(nodeIn.getID()); }
	
	public final void removeChild(UUID uuidIn) { children.removeIf((node) -> node.getID().equals(uuidIn)); }
	
	public List<TreeNode<?>> children() { return children; }
	
	/** Returns true if this node has no parent and, hence, is a root node */
	public final boolean isRoot() { return parent != null; }
	
	protected TreeNode<?> setParent(TreeNode<?> node) { parent = node; return this; }
	
	@Nullable
	public TreeNode<?> parent() { return this.parent; }
	
	/** Returns true if the previous tick of this node did not end in a completion result */
	public boolean isRunning() { return !lastResult.isEnd(); }
	
	@Nullable
	public static TreeNode<?> create(NbtCompound data)
	{
		if(data.contains("Type", NbtElement.STRING_TYPE))
		{
			Identifier type = new Identifier(data.getString("Type"));
			NodeType<?> nodeType = TFNodeTypes.getTypeById(type);
			if(nodeType == null)
			{
				TricksyFoxes.LOGGER.warn("Behaviour tree node not recognise! Type received: "+type.toString());
				return null;
			}
			
			UUID uuid = data.contains("UUID", NbtElement.INT_ARRAY_TYPE) ? data.getUuid("UUID") : UUID.randomUUID();
			TreeNode<?> parent = nodeType.create(uuid, data.getCompound("Data"));
			if(parent == null)
			{
				TricksyFoxes.LOGGER.warn("Behaviour tree node failed to initialise! Type: "+nodeType.getRegistryName().toString());
				return null;
			}
			
			parent.setSubType(data.contains("Variant", NbtElement.STRING_TYPE) ? new Identifier(data.getString("Variant")) : NodeType.DUMMY_ID);
			
			NbtList children = data.contains("Children", NbtElement.LIST_TYPE) ? data.getList("Children", NbtElement.COMPOUND_TYPE) : new NbtList();
			for(int i=0; i<children.size(); i++)
			{
				TreeNode<?> child = create(children.getCompound(i));
				if(child != null && parent.canAddChild(child))
					parent.addChild(child);
			}
			
			return parent;
		}
		return null;
	}
	
	public final NbtCompound write(NbtCompound data)
	{
		data.putString("Type", this.nodeType.getRegistryName().toString());
		data.putUuid("UUID", this.nodeID);
		data.putString("Variant", this.subType.toString());
		if(!children().isEmpty())
		{
			NbtList children = new NbtList();
			children().forEach((child) -> children.add(child.write(new NbtCompound())));
			data.put("Children", children);
		}
		data.put("Data", writeToNbt(new NbtCompound()));
		return data;
	}
	
	protected NbtCompound writeToNbt(NbtCompound data) { return data; }
	
	/** Returns the node with the given ID, if it exists at or below this node in the tree */
	@Nullable
	public final TreeNode<?> getByID(UUID uuidIn)
	{
		if(nodeID.equals(uuidIn))
			return this;
		
		for(TreeNode<?> child : children)
		{
			TreeNode<?> result = child.getByID(uuidIn);
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