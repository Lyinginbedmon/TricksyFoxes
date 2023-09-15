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
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFObjType;

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
	protected abstract void loadValues(NbtList list);
	
	/**
	 * Stores whiteboard values and references in the given NBT list.<br>
	 * Note that recalculating values are dropped, as they are assumed to be system-provided.
	 */
	public void writeCachableValues(NbtList list)
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
	
	public IWhiteboardObject<?> getValue(WhiteboardRef nameIn)
	{
		if(nameIn.boardType() != this.type || !allReferences().contains(nameIn))
			return WhiteboardObj.EMPTY;
		
		if(!nameIn.uncached() && cache.containsKey(nameIn))
		{
			IWhiteboardObject<?> val = cache.get(nameIn);
			val.recacheIfNecessary(world);
			return val;
		}
		
		return getAndCache(nameIn);
	}
	
	protected abstract IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn);
	
	public IWhiteboardObject<?> cache(WhiteboardRef reference, WhiteboardObj<?> obj) { cache.put(reference, obj); return obj; }
	
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
	
	public static final <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> get(WhiteboardRef nameIn, Local<T> local, Global global)
	{
		return nameIn.boardType() == BoardType.LOCAL ? local.getValue(nameIn) : global.getValue(nameIn);
	}
	
	/** Returns a collection of all references stored in this whiteboard, without their values */
	protected Collection<WhiteboardRef> allReferences() { return values.keySet(); }
	
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
	
	/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
	public static class Local<T extends PathAwareEntity & ITricksyMob<?>> extends Whiteboard
	{
		public static final WhiteboardRef HP = new WhiteboardRef("health", TFObjType.INT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef ARMOUR = new WhiteboardRef("armor", TFObjType.INT, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HANDS_FULL = new WhiteboardRef("hands_full", TFObjType.BOOL, BoardType.LOCAL).noCache();
		public static final WhiteboardRef HOME = new WhiteboardRef("home_pos", TFObjType.BLOCK, BoardType.LOCAL).noCache();
		public static final WhiteboardRef NEAREST_MASTER = new WhiteboardRef("master", TFObjType.ENT, BoardType.LOCAL).noCache();
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
			addValue(HP, (tricksy) -> new WhiteboardObj.Int((int)tricksy.getHealth()));
			addValue(ARMOUR, (tricksy) -> new WhiteboardObj.Int(tricksy.getArmor()));
			addValue(HANDS_FULL, (tricksy) -> new WhiteboardObj.Bool(!tricksy.getMainHandStack().isEmpty() && !tricksy.getOffHandStack().isEmpty()));
			addValue(HOME, (tricksy) -> new WhiteboardObj.Block(tricksy.getPositionTarget()));
			addValue(NEAREST_MASTER, (tricksy) -> 
			{
				PlayerEntity nearestMaster = tricksy.getWorld().getClosestPlayer(tricksy.getX(), tricksy.getY(), tricksy.getZ(), 32D, (player) -> tricksy.isMaster((PlayerEntity)player));
				return nearestMaster == null ? WhiteboardObj.EMPTY : new WhiteboardObj.Ent(nearestMaster);
			});
			addValue(ATTACK_TARGET, (tricksy) -> new WhiteboardObj.Ent(tricksy.getAttacking()));
			addValue(ON_GROUND, (tricksy) -> new WhiteboardObj.Bool(tricksy.isOnGround()));
			return this;
		}
		
		protected void loadValues(NbtList list)
		{
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
				IWhiteboardObject <?>obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
				addValue(ref, (tricksy) -> obj);
			}
		}
		
		public Collection<WhiteboardRef> allReferences(){ return mobValues.keySet(); }
		
		/** Adds a supplier to this whiteboard */
		public void addValue(WhiteboardRef reference, Function<T, IWhiteboardObject<?>> supplier)
		{
			mobValues.put(reference, supplier);
		}
		
		protected IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn)
		{
			IWhiteboardObject<?> value = mobValues.getOrDefault(nameIn, (mob) -> WhiteboardObj.EMPTY).apply(tricksy);
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
		
		public Whiteboard build() { return this; }
		
		protected void loadValues(NbtList list)
		{
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				
				WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
				IWhiteboardObject<?> obj = WhiteboardObj.createFromNbt(data.getCompound("Value"));
				addValue(ref, () -> obj);
			}
		}
		
		public IWhiteboardObject<?> getAndCache(WhiteboardRef nameIn)
		{
			IWhiteboardObject<?> value = values.getOrDefault(nameIn, () -> WhiteboardObj.EMPTY).get();
			cache.put(nameIn, value);
			return value;
		}
		
		/** Adds a supplier to this whiteboard */
		public void addValue(WhiteboardRef reference, Supplier<IWhiteboardObject<?>> supplier)
		{
			values.put(reference, supplier);
		}
	}
}
