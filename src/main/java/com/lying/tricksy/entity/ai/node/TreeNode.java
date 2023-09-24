package com.lying.tricksy.entity.ai.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
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
	
	/** Map of input variable references to corresponding whiteboard references */
	private Map<WhiteboardRef, Optional<WhiteboardRef>> variableSet = new HashMap<>();
	
	// Client-side values used for visualisation
	public int screenX, screenY;
	public int width, height;
	
	protected TreeNode(NodeType<N> typeIn, UUID uuidIn)
	{
		this.nodeType = typeIn;
		this.nodeID = uuidIn;
		this.subType = typeIn.baseSubType();
		
		setSubType(typeIn.baseSubType());
	}
	
	public final @Nullable TreeNode<?> copy()
	{
		return TreeNode.create(write(new NbtCompound()));
	}
	
	public final void setPosition(int x, int y)
	{
		this.screenX = x;
		this.screenY = y;
	}
	
	public final void setPositionAndWidth(int x, int y, int widthIn, int heightIn)
	{
		setPosition(x, y);
		this.width = widthIn;
		this.height = heightIn;
	}
	
	public final boolean containsPoint(int x, int y)
	{
		return x >= screenX && x <= (screenX + width) && y >= screenY && y <= (screenY + height);
	}
	
	@Nullable
	public TreeNode<?> findNodeAt(int x, int y)
	{
		if(containsPoint(x, y))
			return this;
		
		for(TreeNode<?> child : children())
		{
			TreeNode<?> node = child.findNodeAt(x, y);
			if(node != null)
				return node;
		}
		return null;
	}
	
	public Random getRNG() { return new Random(getID().getLeastSignificantBits()); }
	
	/** Returns the unique ID of this node */
	public final UUID getID() { return this.nodeID; }
	
	public final NodeType<?> getType() { return this.nodeType; }
	
	public final void changeSubType(int dir)
	{
		List<Identifier> subTypes = getType().subTypes();
		int options = subTypes.size();
		int index = 0;
		for(Identifier subType : subTypes)
		{
			if(subType == getSubType().getRegistryName())
				break;
			index++;
		}
		
		index += dir;
		if(index < 0)
			index = options - 1;
		index %= options;
		
		Identifier regName = subTypes.get(index);
		setSubType(regName);
	}
	
	public final TreeNode<N> setSubType(Identifier typeIn)
	{
		this.subType = typeIn;
		this.variableSet.clear();
		nodeType.getSubType(typeIn).variableSet().keySet().forEach((key) -> this.variableSet.put(key, Optional.empty()));
		return this;
	};
	
	public final NodeSubType<?> getSubType() { return this.getType().getSubType(this.subType); }
	
	public final boolean variableAssigned(WhiteboardRef reference)
	{
		for(Entry<WhiteboardRef, Optional<WhiteboardRef>> entry : variableSet.entrySet())
			if(entry.getKey().equals(reference))
				return entry.getValue().isPresent();
		return false;
	}
	
	public final TreeNode<N> assign(WhiteboardRef variable, @Nullable WhiteboardRef value)
	{
		WhiteboardRef mapEntry = null;
		for(WhiteboardRef entry : variableSet.keySet())
			if(entry.equals(variable))
			{
				mapEntry = entry;
				break;
			}
		
		if(mapEntry != null)
			variableSet.put(mapEntry, value == null ? Optional.empty() : Optional.of(value));
		else
			TricksyFoxes.LOGGER.warn("Attempted to assign a variable this node does not have! "+variable.name()+" in "+subType.toString()+" of "+nodeType.getRegistryName().toString());
		return this;
	}
	
	/** Returns the whiteboard reference assigned to the specified input variable, or null if it is unassigned */
	@Nullable
	public final WhiteboardRef variable(WhiteboardRef reference)
	{
		if(variableAssigned(reference))
			for(Entry<WhiteboardRef, Optional<WhiteboardRef>> entry : variableSet.entrySet())
				if(entry.getKey().equals(reference))
					return entry.getValue().get();
		return null;
	}
	
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
	public boolean canAddChild() { return true; }
	
	public final TreeNode<?> addChild(TreeNode<?> childIn)
	{
		children.add(childIn.setParent(this));
		return this;
	}
	
	public final void replaceChild(UUID childID, TreeNode<?> replacement)
	{
		int index = -1;
		for(int i=0; i<this.children.size(); i++)
			if(children.get(i).nodeID.equals(childID))
			{
				index = i;
				break;
			}
		if(index < 0)
			return;
		
		children.set(index, replacement.setParent(this));
	}
	
	public final void removeChild(TreeNode<?> nodeIn) { removeChild(nodeIn.getID()); }
	
	public final boolean removeChild(UUID uuidIn)
	{
		if(children.removeIf((node) -> node.getID().equals(uuidIn)))
			return true;
		else
		{
			for(TreeNode<?> child : children)
				if(child.removeChild(uuidIn))
					return true;
			
			return false;
		}
	}
	
	public List<TreeNode<?>> children() { return children; }
	
	/** Returns true if this node has no parent and, hence, is a root node */
	public final boolean isRoot() { return parent == null; }
	
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
			TreeNode<?> parent = nodeType.create(uuid, data.contains("Data", NbtElement.COMPOUND_TYPE) ? data.getCompound("Data") : new NbtCompound());
			if(parent == null)
			{
				TricksyFoxes.LOGGER.warn("Behaviour tree node failed to initialise! Type: "+nodeType.getRegistryName().toString());
				return null;
			}
			
			parent.setSubType(data.contains("Variant", NbtElement.STRING_TYPE) ? new Identifier(data.getString("Variant")) : NodeType.DUMMY_ID);
			if(data.contains("Variables", NbtElement.LIST_TYPE))
			{
				NbtList variables = data.getList("Variables", NbtElement.COMPOUND_TYPE);
				parent.variableSet.clear();
				for(int i=0; i<variables.size(); i++)
				{
					NbtCompound nbt = variables.getCompound(i);
					WhiteboardRef variable = WhiteboardRef.fromNbt(nbt.getCompound("Variable"));
					WhiteboardRef value = WhiteboardRef.fromNbt(nbt.getCompound("Value"));
					parent.variableSet.put(variable, Optional.of(value));
				}
			}
			
			NbtList children = data.contains("Children", NbtElement.LIST_TYPE) ? data.getList("Children", NbtElement.COMPOUND_TYPE) : new NbtList();
			for(int i=0; i<children.size(); i++)
			{
				TreeNode<?> child = create(children.getCompound(i));
				if(child != null && parent.canAddChild())
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
		
		if(!variableSet.isEmpty())
		{
			NbtList variables = new NbtList();
			variableSet.entrySet().forEach((entry) -> 
			{
				if(entry.getValue().isEmpty())
					return;
				
				NbtCompound nbt = new NbtCompound();
				nbt.put("Variable", entry.getKey().writeToNbt(new NbtCompound()));
				nbt.put("Value", entry.getValue().get().writeToNbt(new NbtCompound()));
				
				variables.add(nbt);
			});
			data.put("Variables", variables);
		}
		
		if(!children().isEmpty())
		{
			NbtList children = new NbtList();
			children().forEach((child) -> children.add(child.write(new NbtCompound())));
			data.put("Children", children);
		}
		
		NbtCompound storage = writeToNbt(new NbtCompound());
		if(!storage.isEmpty())
			data.put("Data", storage);
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