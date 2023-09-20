package com.lying.tricksy.entity.ai.whiteboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Data storage object used by behaviour trees
 * @author Lying
 * 
 * FIXME Type argument to determine storage format and streamline
 */
public abstract class Whiteboard
{
	public static final Whiteboard CONSTANTS = new Constants().build();
	
	protected final Map<WhiteboardRef, Supplier<IWhiteboardObject<?>>> values = new HashMap<>();
	protected final Map<WhiteboardRef, IWhiteboardObject<?>> cache = new HashMap<>();
	
	protected final BoardType type;
	protected final World world;
	
	protected Whiteboard(BoardType typeIn, World worldIn)
	{
		this.type = typeIn;
		this.world = worldIn;
	}
	
	/** Populates the whiteboard with its system values, post-construction */
	public abstract Whiteboard build();
	
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
			values.put(ref, () -> obj);
		}
	}
	
	/**
	 * Stores whiteboard values and references in the given NBT list.<br>
	 * Note that recalculating values are dropped, as they are assumed to be system-provided.
	 */
	protected void writeCachableValues(NbtList list)
	{
		for(Entry<WhiteboardRef, Supplier<IWhiteboardObject<?>>> entry : values.entrySet())
		{
			if(entry.getKey().uncached())
				continue;
			
			NbtCompound data = new NbtCompound();
			data.put("Ref", entry.getKey().writeToNbt(new NbtCompound()));
			data.put("Value", entry.getValue().get().writeToNbt(new NbtCompound()));
			list.add(data);
		}
	}
	
	public void addValue(WhiteboardRef reference, WhiteboardObj<?> object)
	{
		if(reference.boardType() != this.type)
		{
			TricksyFoxes.LOGGER.warn("Attempted to add reference value in wrong whiteboard: "+reference.name());
			return;
		}
		else if(reference.type() != object.type())
		{
			TricksyFoxes.LOGGER.warn("Attempted to add reference value with non-matching object: "+reference.name());
			return;
		}
		values.put(reference, () -> object);
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
	
	protected IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn, @Nullable World world)
	{
		IWhiteboardObject<?> value = getSupplier(nameIn).get();
		if(world != null)
			value.refreshIfNecessary(world);
		if(!nameIn.uncached())
			cache.put(nameIn, value);
		return value;
	}
	
	private Supplier<IWhiteboardObject<?>> getSupplier(WhiteboardRef nameIn)
	{
		for(Entry<WhiteboardRef, Supplier<IWhiteboardObject<?>>> entry : values.entrySet())
		{
			if(entry.getKey().isSameRef(nameIn))
				return entry.getValue();
		}
		return () -> WhiteboardObj.EMPTY;
	}
	
	protected boolean cached(WhiteboardRef nameIn)
	{
		for(WhiteboardRef ref : cache.keySet())
			if(ref.equals(nameIn))
				return true;
		return false;
	}
	
	public IWhiteboardObject<?> fromCache(WhiteboardRef nameIn)
	{
		for(Entry<WhiteboardRef, IWhiteboardObject<?>> entry : cache.entrySet())
			if(entry.getKey().equals(nameIn))
				return entry.getValue();
		return null;
	}
	
	protected IWhiteboardObject<?> cache(WhiteboardRef reference, WhiteboardObj<?> obj) { cache.put(reference, obj); return obj; }
	
	public static enum BoardType implements StringIdentifiable
	{
		CONSTANT,
		LOCAL,
		GLOBAL;
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static BoardType fromString(String nameIn)
		{
			for(BoardType type : values())
				if(nameIn.equals(type.asString()))
					return type;
			return null;
		}
	}
	
	public static final <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> get(WhiteboardRef nameIn, Local<T> local, Global global)
	{
		switch(nameIn.boardType())
		{
			case GLOBAL:
				return global.getValue(nameIn);
			case LOCAL:
				return local.getValue(nameIn);
			default:
			case CONSTANT:
				return CONSTANTS.getValue(nameIn);
		}
	}
	
	/** Returns a collection of all references stored in this whiteboard, without their values */
	protected Collection<WhiteboardRef> allReferences() { return values.keySet(); }
	
	protected boolean hasReference(WhiteboardRef reference)
	{
		for(WhiteboardRef ref : allReferences())
			if(ref.equals(reference))
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
	
	/** A whiteboard containing globally-accessible values set by a tricksy mob's master */
	public static class Global extends Whiteboard
	{
		public static final WhiteboardRef SPAWN = new WhiteboardRef("spawn_pos", TFObjType.BLOCK, BoardType.GLOBAL).noCache();
		
		public Global(World worldIn)
		{
			super(BoardType.GLOBAL, worldIn);
		}
		
		public Whiteboard build()
		{
			values.put(SPAWN, () -> new WhiteboardObjBlock(world.getSpawnPos(), Direction.UP));
			return this;
		}
	}
	
	/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
	public static class Local<T extends PathAwareEntity & ITricksyMob<?>> extends Whiteboard
	{
		public static final WhiteboardRef SELF = new WhiteboardRef("self", TFObjType.ENT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HP = new WhiteboardRef("health", TFObjType.INT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef ARMOUR = new WhiteboardRef("armor", TFObjType.INT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HANDS_FULL = new WhiteboardRef("hands_full", TFObjType.BOOL, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HOME = new WhiteboardRef("home_pos", TFObjType.BLOCK, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HAS_SAGE = new WhiteboardRef("has_sage", TFObjType.BOOL, BoardType.LOCAL).noCache();
		public static final WhiteboardRef NEAREST_SAGE = new WhiteboardRef("nearest_sage", TFObjType.ENT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef ATTACK_TARGET = new WhiteboardRef("attack_target", TFObjType.ENT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef ON_GROUND = new WhiteboardRef("on_ground", TFObjType.BOOL, BoardType.LOCAL).noCache();
		
		private final Map<WhiteboardRef, Function<T, IWhiteboardObject<?>>> mobValues = new HashMap<>();
		private final T tricksy;
		
		public Local(T tricksyIn)
		{
			super(BoardType.LOCAL, tricksyIn.getWorld());
			tricksy = tricksyIn;
		}
		
		public Whiteboard build()
		{
			mobValues.put(SELF, (tricksy) -> new WhiteboardObjEntity(tricksy));
			mobValues.put(HP, (tricksy) -> new WhiteboardObj.Int((int)tricksy.getHealth()));
			mobValues.put(ARMOUR, (tricksy) -> new WhiteboardObj.Int(tricksy.getArmor()));
			mobValues.put(HANDS_FULL, (tricksy) -> new WhiteboardObj.Bool(!tricksy.getMainHandStack().isEmpty() && !tricksy.getOffHandStack().isEmpty()));
			mobValues.put(HOME, (tricksy) -> new WhiteboardObjBlock(tricksy.getPositionTarget(), Direction.UP));
			mobValues.put(HAS_SAGE, (tricksy) -> new WhiteboardObj.Bool(tricksy.hasSage()));
			mobValues.put(NEAREST_SAGE, (tricksy) -> 
			{
				PlayerEntity nearestSage = tricksy.getEntityWorld().getClosestPlayer(tricksy.getX(), tricksy.getY(), tricksy.getZ(), 32D, (player) -> tricksy.isSage((PlayerEntity)player));
				return nearestSage == null ? WhiteboardObj.EMPTY : new WhiteboardObjEntity(nearestSage);
			});
			mobValues.put(ATTACK_TARGET, (tricksy) -> new WhiteboardObjEntity(tricksy.getAttacking()));
			mobValues.put(ON_GROUND, (tricksy) -> new WhiteboardObj.Bool(tricksy.isOnGround()));
			return this;
		}
		
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
				IWhiteboardObject <?>obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
				mobValues.put(ref, (tricksy) -> obj);
			}
		}
		
		protected void writeCachableValues(NbtList list)
		{
			for(Entry<WhiteboardRef, Function<T, IWhiteboardObject<?>>> entry : mobValues.entrySet())
			{
				if(entry.getKey().uncached())
					continue;
				
				NbtCompound data = new NbtCompound();
				data.put("Ref", entry.getKey().writeToNbt(new NbtCompound()));
				data.put("Value", getValue(entry.getKey()).writeToNbt(new NbtCompound()));
				list.add(data);
			}
		}
		
		public void setValue(WhiteboardRef reference, IWhiteboardObject<?> value)
		{
			if(mobValues.containsKey(reference) && !reference.uncached() && reference.boardType() == this.type)
				mobValues.put(reference, (T) -> value);
		}
		
		public Collection<WhiteboardRef> allReferences(){ return mobValues.keySet(); }
		
		@Override
		protected IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn, @Nullable World world)
		{
			IWhiteboardObject<?> value = getSupplier(nameIn).apply(tricksy);
			if(world != null)
				value.refreshIfNecessary(world);
			if(!nameIn.uncached())
				cache.put(nameIn, value);
			return value;
		}
		
		private Function<T, IWhiteboardObject<?>> getSupplier(WhiteboardRef nameIn)
		{
			for(Entry<WhiteboardRef, Function<T, IWhiteboardObject<?>>> entry : mobValues.entrySet())
			{
				
				if(entry.getKey().isSameRef(nameIn))
					return entry.getValue();
			}
			return (T) -> WhiteboardObj.EMPTY;
		}
	}
}
