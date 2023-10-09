package com.lying.tricksy.entity.ai.whiteboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

/**
 * Data storage object used by behaviour trees
 * @author Lying
 */
public abstract class Whiteboard<T>
{
	public static final Whiteboard<?> CONSTANTS = new ConstantsWhiteboard().build();
	
	private final Map<WhiteboardRef, T> values = new HashMap<>();
	protected final Map<WhiteboardRef, IWhiteboardObject<?>> cache = new HashMap<>();
	
	protected final BoardType type;
	protected World world;
	
	protected Whiteboard(BoardType typeIn, World worldIn)
	{
		this.type = typeIn;
		this.world = worldIn;
	}
	
	public void setWorld(World worldIn) { this.world = worldIn; }
	
	/** Populates the whiteboard with its system values, post-construction */
	public abstract Whiteboard<?> build();
	
	protected void register(WhiteboardRef reference, T supplier) { values.put(reference, supplier); }
	
	public final NbtCompound writeToNbt(NbtCompound data)
	{
		NbtList list = new NbtList();
		writeCachableValues(list);
		if(!list.isEmpty())
			data.put("Values", list);
		return data;
	}
	
	public final void readFromNbt(NbtCompound data)
	{
		values.clear();
		cache.clear();
		build();
		if(data.contains("Values", NbtElement.LIST_TYPE))
			loadValues(data.getList("Values", NbtElement.COMPOUND_TYPE));
	}
	
	/** Loads type-specific cachable values from NBT data */
	protected void loadValues(NbtList list)
	{
		for(int i=0; i<list.size(); i++)
		{
			NbtCompound data = list.getCompound(i);
			WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
			if(ref.boardType() != this.type)
			{
				TricksyFoxes.LOGGER.warn("Attempted to load reference value in wrong whiteboard: "+ref.name());
				continue;
			}
			IWhiteboardObject<?> obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
			values.put(ref, objectToSupplier(obj));
		}
	}
	
	/**
	 * Stores whiteboard values and references in the given NBT list.<br>
	 * Note that recalculating values are dropped, as they are assumed to be system-provided.
	 */
	protected void writeCachableValues(NbtList list)
	{
		for(Entry<WhiteboardRef, T> entry : values.entrySet())
		{
			if(entry.getKey().uncached())
				continue;
			
			NbtCompound data = new NbtCompound();
			data.put("Ref", entry.getKey().writeToNbt(new NbtCompound()));
			data.put("Value", supplierToValue(entry.getValue()).writeToNbt(new NbtCompound()));
			list.add(data);
		}
	}
	
	protected void addSupplier(WhiteboardRef reference, T supplier) { values.put(reference, supplier); }
	
	protected abstract T objectToSupplier(IWhiteboardObject<?> object);
	
	public void delete(WhiteboardRef reference)
	{
		Map<WhiteboardRef, T> valuesNext = new HashMap<>();
		values.forEach((ref,val) -> 
		{
			if(!ref.isSameRef(reference))
				valuesNext.put(ref, val);
		});
		
		values.clear();
		valuesNext.forEach((ref,val) -> values.put(ref, val));
		uncache(reference);
	}
	
	public void addValue(WhiteboardRef reference, T object)
	{
		if(reference.boardType() != this.type)
		{
			TricksyFoxes.LOGGER.warn("Attempted to add reference value in wrong whiteboard: "+reference.name());
			return;
		}
		else if(reference.type() != supplierToValue(object).type())
		{
			TricksyFoxes.LOGGER.warn("Attempted to add reference value with non-matching object: "+reference.name());
			return;
		}
		
		boolean exists = false;
		for(WhiteboardRef entry : values.keySet())
			if(entry.isSameRef(reference))
			{
				exists = true;
				break;
			}
		if(exists)
		{
			TricksyFoxes.LOGGER.warn("Overwrote existing value in "+this.type.asString()+" whiteboard: "+reference.name());
			delete(reference);
		}
		
		values.put(reference, object);
	}
	
	public IWhiteboardObject<?> getValue(WhiteboardRef nameIn)
	{
		if(nameIn.boardType() != this.type)
		{
			TricksyFoxes.LOGGER.warn("Attempted to retrieve value "+nameIn.name()+" from "+type.asString()+" but reference is for "+nameIn.boardType().asString());
			return WhiteboardObj.EMPTY;
		}
		else if(!hasReference(nameIn))
		{
			TricksyFoxes.LOGGER.warn("Attempted to retrieve value "+nameIn.name()+" from "+type.asString()+" but it does not exist there");
			return WhiteboardObj.EMPTY;
		}
		else if(!nameIn.uncached() && cached(nameIn))
		{
			IWhiteboardObject<?> val = fromCache(nameIn);
			if(world != null)
				val.refreshIfNecessary(world);
			return val;
		}
		
		return getAndCache(nameIn, world);
	}
	
	protected abstract IWhiteboardObject<?> supplierToValue(T supplier);
	
	public void setValue(WhiteboardRef reference, IWhiteboardObject<?> obj)
	{
		Map<WhiteboardRef, T> valuesNext = new HashMap<>();
		valuesNext.put(reference, objectToSupplier(obj));
		for(Entry<WhiteboardRef, T> value : values.entrySet())
			if(!value.getKey().isSameRef(reference))
				valuesNext.put(value.getKey(), value.getValue());
		
		uncache(reference);
		
		values.clear();
		valuesNext.forEach((ref,val) -> values.put(ref, val));
	}
	
	protected IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn, @Nullable World world)
	{
		IWhiteboardObject<?> value = supplierToValue(getSupplier(nameIn));
		if(world != null)
			value.refreshIfNecessary(world);
		if(!nameIn.uncached())
			cache.put(nameIn, value);
		return value;
	}
	
	@Nullable
	private T getSupplier(WhiteboardRef nameIn)
	{
		for(Entry<WhiteboardRef, T> entry : values.entrySet())
			if(entry.getKey().isSameRef(nameIn))
				return entry.getValue();
		return null;
	}
	
	protected boolean cached(WhiteboardRef nameIn)
	{
		for(WhiteboardRef ref : cache.keySet())
			if(ref.isSameRef(nameIn))
				return true;
		return false;
	}
	
	public IWhiteboardObject<?> fromCache(WhiteboardRef nameIn)
	{
		for(Entry<WhiteboardRef, IWhiteboardObject<?>> entry : cache.entrySet())
			if(entry.getKey().isSameRef(nameIn))
				return entry.getValue();
		return null;
	}
	
	protected IWhiteboardObject<?> cache(WhiteboardRef reference, WhiteboardObj<?,?> obj) { cache.put(reference, obj); return obj; }
	
	protected final void uncache(WhiteboardRef reference)
	{
		Map<WhiteboardRef, IWhiteboardObject<?>> cacheNext = new HashMap<>();
		for(Entry<WhiteboardRef, IWhiteboardObject<?>> value : cache.entrySet())
			if(!value.getKey().isSameRef(reference))
				cacheNext.put(value.getKey(), value.getValue());
		
		cache.clear();
		cacheNext.forEach((ref,val) -> cache.put(ref, val));
	}
	
	public static enum BoardType implements StringIdentifiable
	{
		CONSTANT,
		LOCAL,
		GLOBAL;
		
		public String asString() { return name().toLowerCase(); }
		
		public Text translate() { return Text.translatable("board."+Reference.ModInfo.MOD_ID+"."+asString()); }
		
		@Nullable
		public static BoardType fromString(String nameIn)
		{
			for(BoardType type : values())
				if(nameIn.equals(type.asString()))
					return type;
			return null;
		}
	}
	
	public static final <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> get(WhiteboardRef nameIn, LocalWhiteboard<T> local, GlobalWhiteboard global)
	{
		switch(nameIn.boardType())
		{
			case GLOBAL:
				return global.getValue(nameIn);
			case LOCAL:
				return local.getValue(nameIn);
			case CONSTANT:
				return CONSTANTS.getValue(nameIn);
		}
		return TFObjType.EMPTY.blank();
	}
	
	/** Returns a collection of all references stored in this whiteboard, without their values */
	public Collection<WhiteboardRef> allReferences() { return values.keySet(); }
	
	protected boolean hasReference(WhiteboardRef reference)
	{
		for(WhiteboardRef ref : values.keySet())
			if(ref.isSameRef(reference))
				return true;
		return false;
	}
	
	/** Returns a list of all references in this whiteboard of the given type. */
	public List<WhiteboardRef> availableOfType(TFObjType<?> typeIn)
	{
		List<WhiteboardRef> references = Lists.newArrayList();
		allReferences().forEach((ref) -> 
		{
			if(ref.type() == typeIn)
				references.add(ref);
		});
		return references;
	}
	
	/** Returns an uncached whiteboard reference of the given type and board with a corresponding translated name */
	protected static WhiteboardRef makeSystemRef(String name, TFObjType<?> type, BoardType board)
	{
		name = name.replace(' ', '_').toLowerCase();
		return new WhiteboardRef(name, type, board).noCache().displayName(Text.translatable("whiteboard."+Reference.ModInfo.MOD_ID+"."+name));
	}
}
