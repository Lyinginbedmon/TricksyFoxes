package com.lying.tricksy.entity.ai.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.StaticValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

/**
 * Base class for behaviour tree nodes.
 * @author Lying
 */
public abstract class TreeNode<N extends TreeNode<?>>
{
	public static final String TYPE_KEY = "Type";
	public static final String SUBTYPE_KEY = "Variant";
	public static final String IO_KEY = "IO";
	
	/** A unique identifier, stored by behaviour trees to reduce unnecessary ticking in large trees by only ticking the running node */
	@NotNull
	private final UUID nodeID;
	
	private Text customName = null;
	private boolean hideChildren = false;
	// TODO Implement isSilent, to prevent a node from emitting any sound events
	private boolean isSilent = false;
	
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
	
	/** Map of input variable references to corresponding value getters */
	private Map<WhiteboardRef, Optional<INodeIOValue>> assignedIO = new HashMap<>();
	
	/** Temporary storage for tick handler use */
	public NbtCompound nodeRAM = new NbtCompound();
	
	// Client-side values used for visualisation
	public int screenX, screenY;
	public int width, height;
	
	private NodeStatusLog currentLog = new NodeStatusLog();
	private boolean loggedThisTick = false;
	
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
	
	public final int branchSize()
	{
		return recursiveBranchSize(this);
	}
	
	private final int recursiveBranchSize(TreeNode<?> node)
	{
		int tally = 1;
		for(TreeNode<?> child : node.children())
			tally += recursiveBranchSize(child);
		return tally;
	}
	
	public Random getRNG() { return new Random(getID().getLeastSignificantBits()); }
	
	/** Returns the unique ID of this node */
	public final UUID getID() { return this.nodeID; }
	
	public final Text getDisplayName()
	{
		return !hasCustomName() ? getType().translatedName() : this.customName;
	}
	
	public final boolean hasCustomName() { return this.customName != null && this.customName.getString().length() > 0; }
	
	public final TreeNode<N> named(Text nameIn) { this.customName = nameIn; return this; }
	
	public final TreeNode<N> discrete(boolean val) { this.hideChildren = val; return this; }
	
	public final TreeNode<N> discrete() { return discrete(true); }
	
	/** Returns true if discretion is permitted and this node should not display its children */
	public final boolean isDiscrete(boolean permitted) { return permitted && (this.hideChildren && hasChildren() && !isRoot()); }
	
	public final TreeNode<N> silent() { this.isSilent = true; return this; }
	
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
		this.assignedIO.clear();
		getSubType().ioSet().keySet().forEach((key) -> this.assignedIO.put(key, Optional.empty()));
		return this;
	};
	
	public final NodeSubType<N> getSubType() { return nodeType.getSubType(this.subType); }
	
	/** Returns true if any value is assigned to the given input */
	public final boolean isIOAssigned(WhiteboardRef reference)
	{
		return assignedIO.entrySet().stream().anyMatch(entry -> entry.getKey().isSameRef(reference) && entry.getValue().isPresent());
	}
	
	public final TreeNode<N> ioRef(WhiteboardRef variable, WhiteboardRef value)
	{
		return assignIO(variable, new WhiteboardValue(value));
	}
	
	public final TreeNode<N> ioStatic(WhiteboardRef variable, IWhiteboardObject<?> value)
	{
		return assignIO(variable, new StaticValue(value));
	}
	
	public final TreeNode<N> assignIO(WhiteboardRef variable, @Nullable INodeIOValue value)
	{
		if(WhiteboardRef.findInMap(getSubType().ioSet(), variable) == null)
		{
			TricksyFoxes.LOGGER.warn("Attempted to assign a variable this node does not have! "+variable.name()+" in "+subType.toString()+" of "+nodeType.getRegistryName().toString());
			return this;
		}
		
		assignedIO.entrySet().removeIf(input -> input.getKey().isSameRef(variable));
		assignedIO.put(variable, value == null ? Optional.empty() : Optional.of(value));
		return this;
	}
	
	/** Returns the node value assigned to the specified input variable, or null if it is unassigned */
	@Nullable
	public final INodeIOValue getIO(WhiteboardRef reference)
	{
		if(isIOAssigned(reference))
			return assignedIO.entrySet().stream().filter(entry -> entry.getKey().isSameRef(reference)).findFirst().get().getValue().get();
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends PathAwareEntity & ITricksyMob<?>> Result tick(T tricksy, WhiteboardManager<T> whiteboards)
	{
		if(!isRunnable())
			return this.lastResult = Result.FAILURE;
		
		this.loggedThisTick = false;
		
		NodeSubType<N> subType = nodeType.getSubType(this.subType);
		if(whiteboards.local().isNodeCoolingDown(subType))
		{
			logStatus(TFNodeStatus.ON_COOLDOWN);
			return Result.FAILURE;
		}
		
		if(this.lastResult.isEnd())
			this.ticksRunning = 0;
		else
			this.ticksRunning++;
		
		@Nullable
		Result result = Result.FAILURE;
		if(subType.usesFlags().isEmpty() || subType.usesFlags().stream().allMatch(flag -> whiteboards.local().canUseFlag(flag)))
			try
			{
				result = subType.call(tricksy, whiteboards, (N)this);
				if(result.isEnd())
				{
					subType.onEnd(tricksy, (N)this);
					
					int cooldown = subType.getCooldown(tricksy);
					if(cooldown > 0)
						whiteboards.local().setNodeCooldown(subType, cooldown);
					
					this.nodeRAM = new NbtCompound();
				}
				
				if(!subType.usesFlags().isEmpty())
					whiteboards.local().flagAction(subType.usesFlags());
			}
			catch(Exception e) { logStatus(TFNodeStatus.FAILURE, Text.literal(e.getLocalizedMessage())); }
		else
			logStatus(TFNodeStatus.FLAGS_OCCUPIED);
		
		if(!this.loggedThisTick)
			switch(result)
			{
				case SUCCESS:
					logStatus(TFNodeStatus.SUCCESS);
					break;
				case FAILURE:
					logStatus(TFNodeStatus.FAILURE);
					break;
				case RUNNING:
					logStatus(TFNodeStatus.RUNNING);
					break;
			}
		return this.lastResult = result;
	}
	
	public void playSound(Entity tricksy, SoundEvent sound, float volume, float pitch)
	{
		if(isSilent) return;
		tricksy.playSound(sound, volume, pitch);
	}
	
	public void playSound(Entity tricksy, BlockPos position, SoundEvent sound, SoundCategory category, float volume, float pitch)
	{
		if(isSilent) return;
		tricksy.getWorld().playSound(null, position, sound, category, volume, pitch);
	}
	
	public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch)
	{
		if(isSilent) return;
		entity.getWorld().playSoundFromEntity(null, entity, sound, category, volume, pitch);
	}
	
	public NodeStatusLog getLog()
	{
		return isRoot() ? this.currentLog : this.parent().getLog();
	}
	
	public void logStatus(TFNodeStatus status)
	{
		if(!this.loggedThisTick)
			getLog().logStatus(getID(), status);
		
		this.loggedThisTick = true;
	}
	
	public void logStatus(TFNodeStatus status, Text message)
	{
		if(!hasLogged())
			getLog().logStatus(getID(), status, message);
		this.loggedThisTick = true;
	}
	
	public boolean hasLogged() { return this.loggedThisTick; }
	
	public void tickLog() { this.currentLog.tick(); }
	
	public void clearLog() { this.currentLog.clear(); }
	
	/** Returns true if this node is in a runnable condition */
	public boolean isRunnable() { return true; }
	
	public int ticksRunning() { return this.ticksRunning; }
	
	/** Returns true if this node can accept the given child node */
	public boolean canAddChild() { return true; }
	
	public final TreeNode<?> child(TreeNode<?> childIn) { return addChild(childIn, false); }
	
	public final TreeNode<?> addChild(TreeNode<?> childIn, boolean toStart)
	{
		if(!toStart)
			children.add(childIn.setParent(this));
		else
			children.add(0, childIn.setParent(this));
		return this;
	}
	
	public final void replaceChild(UUID childID, TreeNode<?> replacement)
	{
		if(!hasChildren())
			return;
		
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
		if(!hasChildren())
			return false;
		
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
	
	public final boolean hasChildren() { return !children().isEmpty(); }
	
	public List<TreeNode<?>> children() { return children; }
	
	/** Returns true if this node has no parent and, hence, is a root node */
	public final boolean isRoot() { return parent == null; }
	
	protected TreeNode<?> setParent(TreeNode<?> node) { parent = node; return this; }
	
	@Nullable
	public TreeNode<?> parent() { return this.parent; }
	
	/** Returns true if the previous tick of this node did not end in a completion result */
	public boolean isRunning() { return !lastResult.isEnd(); }
	
	/** Performs an immediate end to this node's activity */
	@SuppressWarnings("unchecked")
	public <T extends PathAwareEntity & ITricksyMob<?>> void stop(T tricksy)
	{
		if(!isRunning())
			return;
		
		try
		{
			NodeSubType<N> subType = nodeType.getSubType(this.subType);
			subType.onEnd(tricksy, (N)this);
		}
		catch(Exception e) { }
		
		lastResult = Result.FAILURE;
		children.forEach((child) -> child.stop(tricksy));
	}
	
	@Nullable
	public static TreeNode<?> create(NbtCompound data)
	{
		if(data.contains("Type", NbtElement.STRING_TYPE))
		{
			Identifier type = new Identifier(data.getString(TYPE_KEY));
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
			
			parent.setSubType(data.contains(SUBTYPE_KEY, NbtElement.STRING_TYPE) ? new Identifier(data.getString(SUBTYPE_KEY)) : NodeType.DUMMY_ID);
			if(data.contains("Variables", NbtElement.LIST_TYPE))
				loadIOFromList(parent, data.getList("Variables", NbtElement.COMPOUND_TYPE));
			else if(data.contains(IO_KEY, NbtElement.LIST_TYPE))
				loadIOFromList(parent, data.getList(IO_KEY, NbtElement.COMPOUND_TYPE));
			
			if(data.contains("CustomName", NbtElement.STRING_TYPE))
			{
				String string = data.getString("CustomName");
				try
				{
					parent.customName = Text.Serializer.fromJson(string);
				}
				catch(Exception e)
				{
					TricksyFoxes.LOGGER.warn("Failed to parse tree node custom name {}", (Object)string, (Object)e);
				}
			}
			
			NbtList children = data.contains("Children", NbtElement.LIST_TYPE) ? data.getList("Children", NbtElement.COMPOUND_TYPE) : new NbtList();
			for(int i=0; i<children.size(); i++)
			{
				TreeNode<?> child = create(children.getCompound(i));
				if(child != null && parent.canAddChild())
					parent.child(child);
			}
			if(children.size() > 0)
				parent.hideChildren = data.getBoolean("Discrete");
			
			if(data.contains("Silent"))
				parent.isSilent = data.getBoolean("Silent");
			
			return parent;
		}
		return null;
	}
	
	private static void loadIOFromList(TreeNode<?> node, NbtList list)
	{
		for(int i=0; i<list.size(); i++)
		{
			NbtCompound nbt = list.getCompound(i);
			WhiteboardRef variable = loadIO(nbt);
			if(variable == null)
				continue;
			INodeIOValue value = INodeIOValue.readFromNbt(nbt.getCompound("Value"));
			node.assignIO(variable, value);
		}
	}
	
	@Nullable
	private static WhiteboardRef loadIO(NbtCompound nbt)
	{
		if(nbt.contains("Variable", NbtElement.COMPOUND_TYPE))
			return WhiteboardRef.fromNbt(nbt.getCompound("Variable"));
		else if(nbt.contains("IO", NbtElement.COMPOUND_TYPE))
			return WhiteboardRef.fromNbt(nbt.getCompound("IO"));
		return null;
	}
	
	public final NbtCompound write(NbtCompound data)
	{
		data.putString(TYPE_KEY, this.nodeType.getRegistryName().toString());
		data.putString(SUBTYPE_KEY, this.subType.toString());
		data.putUuid("UUID", this.nodeID);
		
		if(!assignedIO.isEmpty())
		{
			NbtList variables = new NbtList();
			assignedIO.entrySet().forEach((entry) -> 
			{
				if(!entry.getValue().isPresent() || entry.getKey() == null)
					return;
				
				NbtCompound nbt = new NbtCompound();
				nbt.put("IO", entry.getKey().writeToNbt(new NbtCompound()));
				nbt.put("Value", entry.getValue().get().writeToNbt(new NbtCompound()));
				
				variables.add(nbt);
			});
			data.put(IO_KEY, variables);
		}
		
		if(!children().isEmpty())
		{
			NbtList children = new NbtList();
			children().forEach((child) -> children.add(child.write(new NbtCompound())));
			data.put("Children", children);
			
			data.putBoolean("Discrete", this.hideChildren);
		}
		
		if(this.isSilent)
			data.putBoolean("Silent", true);
		
		if(hasCustomName())
			data.putString("CustomName", Text.Serializer.toJson(this.customName));
		
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
	
	public static enum Result implements StringIdentifiable
	{
		RUNNING,
		SUCCESS,
		FAILURE;
		
		public boolean isEnd() { return this != RUNNING; }
		
		public String asString() { return name().toLowerCase(); }
		
		public Identifier texture() { return new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/node_results/result_"+asString()+".png"); }
		
		@Nullable
		public static Result fromString(String nameIn)
		{
			for(Result result : Result.values())
				if(result.asString().equalsIgnoreCase(nameIn))
					return result;
			return null;
		}
	}
}