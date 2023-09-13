package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard.Global;
import com.lying.tricksy.entity.ai.Whiteboard.Local;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Defines a single behaviour tree node supertype */
public class NodeType<M extends TreeNode<?>>
{
	public static final Identifier DUMMY_ID = new Identifier(Reference.ModInfo.MOD_ID, "dummy");
	private final NodeSubType<M> dummy = new NodeSubType<M>(DUMMY_ID, new NodeTickHandler<M>()
	{
		public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, M parent)
		{
			return Result.FAILURE;
		}
	});
	private Identifier registryName = null;
	
	private Map<Identifier, NodeSubType<M>> subTypes = new HashMap<>();
	private Identifier baseSubType;
	
	private final BiFunction<UUID,NbtCompound, M> factory;
	
	public NodeType(BiFunction<UUID,NbtCompound, M> factoryIn, Consumer<Collection<NodeSubType<M>>> subTypeBuilder)
	{
		factory = factoryIn;
		
		List<NodeSubType<M>> subTypeList = Lists.newArrayList();
		subTypeBuilder.accept(subTypeList);
		for(NodeSubType<M> subType : subTypeList)
			subTypes.put(subType.getRegistryName(), subType);
		
		baseSubType = subTypes.isEmpty() ? DUMMY_ID : getAvailableSubTypes().get(0);
	}
	
	public final void setRegistryName(Identifier idIn)
	{
		if(this.registryName != null)
			TricksyFoxes.LOGGER.error("Attempted to alter registry name of node type "+this.registryName.toString()+"!");
		this.registryName = idIn;
	}
	
	public final Identifier getRegistryName() { return this.registryName; }
	
	public Text translatedName() { return Text.translatable("node."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public final M create(UUID uuidIn, NbtCompound data) { return factory.apply(uuidIn, data); }
	
	public final Identifier baseSubType() { return baseSubType; }
	
	public NodeType<M> setBaseSubType(Identifier nameIn) { this.baseSubType = nameIn; return this; }
	
	protected void addSubType(String nameIn, NodeSubType<M> typeIn)
	{
		subTypes.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), typeIn);
	}
	
	public final NodeSubType<M> getSubType(Identifier typeIn)
	{
		return subTypes.getOrDefault(typeIn, dummy);
	}
	
	public final Collection<NodeSubType<M>> subTypes() { return subTypes.values(); }
	
	public final List<Identifier> getAvailableSubTypes()
	{
		List<Identifier> types = Lists.newArrayList();
		types.addAll(subTypes.keySet());
		return types;
	}
}
