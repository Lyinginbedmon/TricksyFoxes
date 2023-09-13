package com.lying.tricksy.entity.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.WhiteboardObj.ObjectType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

/**
 * Data storage object used by whiteboards
 * @author Lying
 */
public abstract class Whiteboard
{
	protected final Map<WhiteboardRef, Supplier<WhiteboardObj>> values = new HashMap<>();
	protected final Map<WhiteboardRef, WhiteboardObj> cache = new HashMap<>();
	
	protected final BoardType type;
	protected final World world;
	
	protected Whiteboard(BoardType typeIn, World worldIn)
	{
		this.type = typeIn;
		this.world = worldIn;
		registerSystemValues();
	}
	
	protected abstract void registerSystemValues();
	
	public final NbtCompound writeToNbt(NbtCompound data)
	{
		NbtList list = new NbtList();
		writeCachableValues(list);
		data.put("Values", list);
		return data;
	}
	
	public final void readFromNbt(NbtCompound data)
	{
		values.clear();
		cache.clear();
		registerSystemValues();
		loadValues(data.getList("Values", NbtElement.COMPOUND_TYPE));
	}
	
	protected abstract void loadValues(NbtList list);
	
	/**
	 * Stores whiteboard values and references in the given NBT list.<br>
	 * Note that recalculating values are dropped, as they are assumed to be system-provided.
	 */
	public void writeCachableValues(NbtList list)
	{
		for(Entry<WhiteboardRef, Supplier<WhiteboardObj>> entry : values.entrySet())
		{
			if(entry.getKey().shouldRecalc())
				continue;
			
			NbtCompound data = new NbtCompound();
			data.put("Ref", entry.getKey().writeToNbt(new NbtCompound()));
			data.put("Value", entry.getValue().get().writeToNbt(new NbtCompound()));
			list.add(data);
		}
	}
	
	public WhiteboardObj getValue(WhiteboardRef nameIn)
	{
		if(nameIn.boardType() != this.type || !allReferences().contains(nameIn))
			return WhiteboardObj.EMPTY;
		
		if(!nameIn.shouldRecalc() && cache.containsKey(nameIn))
		{
			WhiteboardObj val = cache.get(nameIn);
			val.recacheIfNecessary(world);
			return val;
		}
		
		return getAndCache(nameIn);
	}
	
	protected abstract WhiteboardObj getAndCache(WhiteboardRef nameIn);
	
	public WhiteboardObj cache(WhiteboardRef reference, WhiteboardObj obj) { cache.put(reference, obj); return obj; }
	
	public static enum BoardType implements StringIdentifiable
	{
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
	
	public static final <T extends PathAwareEntity & ITricksyMob<?>> WhiteboardObj get(WhiteboardRef nameIn, Local<T> local, Global global)
	{
		return nameIn.boardType() == BoardType.LOCAL ? local.getValue(nameIn) : global.getValue(nameIn);
	}
	
	protected Collection<WhiteboardRef> allReferences() { return values.keySet(); }
	
	/** Returns a list of all references in this whiteboard of the given type. */
	public List<WhiteboardRef> availableOfType(ObjectType typeIn)
	{
		List<WhiteboardRef> references = Lists.newArrayList();
		allReferences().forEach((ref) -> 
		{
			if(ref.type() == typeIn)
				references.add(ref);
		});
		return references;
	}
	
	/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
	public static class Local<T extends PathAwareEntity & ITricksyMob<?>> extends Whiteboard
	{
		public static final WhiteboardRef HP = new WhiteboardRef("health", ObjectType.INT, BoardType.LOCAL).setRecalc();
		public static final WhiteboardRef HANDS_FULL = new WhiteboardRef("hands_full", ObjectType.BOOL, BoardType.LOCAL).setRecalc();
		public static final WhiteboardRef NEAREST_MASTER = new WhiteboardRef("master", ObjectType.ENT, BoardType.LOCAL).setRecalc();
		
		private final Map<WhiteboardRef, Function<T, WhiteboardObj>> mobValues = new HashMap<>();
		private final T tricksy;
		
		public Local(T tricksyIn)
		{
			super(BoardType.LOCAL, tricksyIn.getWorld());
			tricksy = tricksyIn;
		}
		
		protected void registerSystemValues()
		{
			addValue(HP, (tricksy) -> new WhiteboardObj.IntegerObject((int)tricksy.getHealth()));
			addValue(HANDS_FULL, (tricksy) -> new WhiteboardObj.BooleanObject(!tricksy.getMainHandStack().isEmpty() && !tricksy.getOffHandStack().isEmpty()));
			addValue(NEAREST_MASTER, (tricksy) -> 
			{
				PlayerEntity nearestMaster = tricksy.getWorld().getClosestPlayer(tricksy.getX(), tricksy.getY(), tricksy.getZ(), 32D, (player) -> tricksy.isMaster((PlayerEntity)player));
				return new WhiteboardObj.EntityObject(nearestMaster);
			});
		}
		
		protected void loadValues(NbtList list)
		{
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				
				WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
				WhiteboardObj obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
				addValue(ref, (tricksy) -> obj);
			}
		}
		
		public Collection<WhiteboardRef> allReferences(){ return mobValues.keySet(); }
		
		/** Adds a supplier to this whiteboard */
		public void addValue(WhiteboardRef reference, Function<T, WhiteboardObj> supplier)
		{
			mobValues.put(reference, supplier);
		}
		
		protected WhiteboardObj getAndCache(WhiteboardRef nameIn)
		{
			WhiteboardObj value = mobValues.getOrDefault(nameIn, (mob) -> WhiteboardObj.EMPTY).apply(tricksy);
			cache.put(nameIn, value);
			return value;
		}
	}
	
	/** A whiteboard containing globally-accessible values set by a tricksy mob's master */
	public static class Global extends Whiteboard
	{
		public Global(World worldIn)
		{
			super(BoardType.GLOBAL, worldIn);
		}
		
		protected void registerSystemValues() { }
		
		protected void loadValues(NbtList list)
		{
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				
				WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
				WhiteboardObj obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
				addValue(ref, () -> obj);
			}
		}
		
		public WhiteboardObj getAndCache(WhiteboardRef nameIn)
		{
			WhiteboardObj value = values.getOrDefault(nameIn, () -> WhiteboardObj.EMPTY).get();
			cache.put(nameIn, value);
			return value;
		}
		
		/** Adds a supplier to this whiteboard */
		public void addValue(WhiteboardRef reference, Supplier<WhiteboardObj> supplier)
		{
			values.put(reference, supplier);
		}
	}
}
