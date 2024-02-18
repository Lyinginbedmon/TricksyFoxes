package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Defines a single behaviour tree node supertype */
public class NodeType<M extends TreeNode<?>>
{
	public static final Identifier DUMMY_ID = new Identifier(Reference.ModInfo.MOD_ID, "dummy");
	private final NodeSubType<M> dummy = new NodeSubType<M>(DUMMY_ID, this, new INodeTickHandler<M>()
	{
		public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, M parent)
		{
			return Result.FAILURE;
		}
	});
	public Comparator<Identifier> subTypeSort = (o1,o2) -> TricksyUtils.stringComparator(getSubType(o1).translatedName().getString(), getSubType(o2).translatedName().getString());
	private Identifier registryName = null;
	private final int displayColor;
	private final Identifier flowerTexture;
	private final Supplier<Collection<ISubtypeGroup<M>>> groupsBuilder;
	
	private List<ISubtypeGroup<M>> subTypeGroups = Lists.newArrayList();
	private Identifier baseSubType = DUMMY_ID;
	
	private final BiFunction<UUID,NbtCompound, M> factory;
	
	public NodeType(int colorIn, BiFunction<UUID,NbtCompound, M> factoryIn, Supplier<Collection<ISubtypeGroup<M>>> subTypeBuilder)
	{
		this(colorIn, null, factoryIn, subTypeBuilder);
	}
	
	public NodeType(int colorIn, Identifier tex, BiFunction<UUID,NbtCompound, M> factoryIn, Supplier<Collection<ISubtypeGroup<M>>> subTypeBuilder)
	{
		displayColor = colorIn;
		flowerTexture = tex;
		factory = factoryIn;
		
		groupsBuilder = subTypeBuilder;
	}
	
	public final void setRegistryName(Identifier idIn)
	{
		if(this.registryName != null)
			TricksyFoxes.LOGGER.error("Attempted to alter registry name of node type "+this.registryName.toString()+"!");
		this.registryName = idIn;
	}
	
	public final Identifier getRegistryName() { return this.registryName; }
	
	public int color() { return this.displayColor; }
	
	@Nullable
	public Identifier flowerTexture() { return this.flowerTexture; }
	
	public Text translatedName() { return Text.translatable("node."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public MutableText description() { return Text.translatable("node."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public final M create(UUID uuidIn) { return create(uuidIn, new NbtCompound()); }
	
	public final M create(UUID uuidIn, NbtCompound data) { return factory.apply(uuidIn, data); }
	
	public final TreeNode<?> create(UUID uuidIn, Identifier subType) { return create(uuidIn).setSubType(subType); }
	
	public final TreeNode<?> create(Identifier subType){ return create(UUID.randomUUID(), subType); }
	
	public final TreeNode<?> create(Identifier subType, Map<WhiteboardRef, INodeIOValue> ios)
	{
		TreeNode<?> node = create(subType);
		ios.forEach((ref,io) -> node.assignIO(ref, io));
		return node;
	}
	
	public final Identifier baseSubType() { return baseSubType; }
	
	public NodeType<M> setBaseSubType(Identifier nameIn) { this.baseSubType = nameIn; return this; }
	
	@NotNull
	public final NodeSubType<M> getSubType(Identifier typeIn)
	{
		for(ISubtypeGroup<M> group : subTypeGroups)
			for(NodeSubType<M> subType : group.getSubtypes())
				if(subType.getRegistryName().equals(typeIn))
					return subType;
		return dummy;
	}
	
	public void populateGroups()
	{
		subTypeGroups.clear();
		subTypeGroups.addAll(groupsBuilder.get());
	}
	
	public final List<ISubtypeGroup<M>> groups() { return this.subTypeGroups; }
	
	public final Identifier getGroupOf(Identifier subtype)
	{
		for(ISubtypeGroup<M> group : groups())
			for(NodeSubType<M> sub : group.getSubtypes())
				if(sub.getRegistryName().equals(subtype))
					return group.getRegistryName();
		return null;
	}
	
	public final List<Identifier> subTypes()
	{
		List<Identifier> subtypes = Lists.newArrayList();
		for(ISubtypeGroup<M> group : subTypeGroups)
			for(NodeSubType<M> subType : group.getSubtypes())
				subtypes.add(subType.getRegistryName());
		subtypes.sort(subTypeSort);
		return subtypes;
	}
}
