package com.lying.tricksy.entity.ai.whiteboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.init.TFWhiteboards.BoardType;
import com.lying.tricksy.utility.TricksyUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;

/**
 * A complex reference object that denotes a value in a whiteboard of a specific type.<br>
 * Used instead of simple string identifiers to provide some level of type guarantee.
 * @author Lying
 *
 */
public class WhiteboardRef
{
	public static final String NAME_KEY = "Name";
	public static final String BOARD_KEY = "Board";
	public static final String TYPE_KEY = "Type";
	
	public static final Codec<WhiteboardRef> CODEC	= RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf(NAME_KEY).forGetter(WhiteboardRef::name),
			TFObjType.CODEC.fieldOf(TYPE_KEY).forGetter(WhiteboardRef::type),
			BoardType.CODEC.fieldOf(BOARD_KEY).forGetter(WhiteboardRef::boardType),
			Codec.BOOL.optionalFieldOf("Live").forGetter(r -> r.noCache ? Optional.of(r.noCache) : Optional.empty()),
			Codec.BOOL.optionalFieldOf("Filter").forGetter(r -> r.isFilter ? Optional.of(r.isFilter) : Optional.empty()),
			Codec.BOOL.optionalFieldOf("Hidden").forGetter(r -> r.isHidden ? Optional.of(r.isHidden) : Optional.empty()),
			Codec.STRING.optionalFieldOf("DisplayName").forGetter(r -> r.displayName != null ? Optional.of(Text.Serializer.toJson(r.displayName)) : Optional.empty())
			).apply(instance, (name, type, board, uncached, filtered, hidden, displayName) -> 
			{
				WhiteboardRef reference = new WhiteboardRef(name, type, board);
				uncached.ifPresent(b -> reference.noCache = b);
				filtered.ifPresent(b -> reference.isFilter = b);
				hidden.ifPresent(b -> reference.isHidden = b);
				if(displayName.isPresent())
				{
					String string = displayName.get();
					try
					{
						reference.displayName = Text.Serializer.fromJson(string);
					}
					catch(Exception e)
					{
						TricksyFoxes.LOGGER.warn("Failed to parse whiteboard reference custom name {}", (Object)string, (Object)e);
					}
				}
				
				return reference;
			}));
	
	public static final Comparator<WhiteboardRef> REF_SORT = new Comparator<>()
	{
		public int compare(WhiteboardRef o1, WhiteboardRef o2)
		{
			int index1 = o1.type().index();
			int index2 = o2.type().index();
			if(index1 != index2)
				return index1 > index2 ? 1 : index1 < index2 ? -1 : 0;
			
			if(o1.uncached() != o2.uncached())
				return o1.uncached() && !o2.uncached() ? -1 : !o1.uncached() && o2.uncached() ? 1 : 0;
			
			String a = o1.displayName().getString();
			String b = o2.displayName().getString();
			
			// Attempt numerical sort
			int numA = Integer.MIN_VALUE;
			int numB = Integer.MIN_VALUE;
			try
			{
				numA = Integer.valueOf(a);
				numB = Integer.valueOf(b);
				return numA > numB ? 1 : numA < numB ? -1 : 0;
			}
			catch(NumberFormatException e) { }
			if(numA > Integer.MIN_VALUE && numB == Integer.MIN_VALUE)
				return -1;
			else if(numB > Integer.MIN_VALUE && numA == Integer.MIN_VALUE)
				return 1;
			
			return TricksyUtils.stringComparator(a, b);
		}
	};
	
	private final String name;
	private final BoardType onBoard;
	private final TFObjType<?> varType;
	
	/** True if this reference should never be cached */
	private boolean noCache = false;
	
	/** True if this reference only holds information used for filtering */
	private boolean isFilter = false;
	
	/** True if this reference should not be accessible by players */
	private boolean isHidden = false;
	
	private Text displayName = null;
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn) { this(nameIn, typeIn, TFWhiteboards.CONSTANT); }
	
	public WhiteboardRef(String nameIn, TFObjType<?> typeIn, BoardType global)
	{
		this.name = conformName(nameIn);
		this.displayName = Text.literal(nameIn);
		this.onBoard = global;
		this.varType = typeIn;
	}
	
	public String toString() { return "WhiteboardRef["+name+", "+varType.toString()+", "+onBoard+"]"; }
	
	public Text displayName() { return displayName == null ? Text.literal(name) : displayName; }
	
	public WhiteboardRef displayName(Text nameIn) { this.displayName = nameIn; return this; }
	
	@Override
	public boolean equals(Object refB) { return refB instanceof WhiteboardRef ? isSameRef((WhiteboardRef)refB) : false; }
	
	public static List<WhiteboardRef> sortByDisplayName(List<WhiteboardRef> set)
	{
		Map<String, WhiteboardRef> valueMap = new HashMap<>();
		set.forEach((val) -> valueMap.put(val.displayName.getString(), val));
		List<String> nameSet = Lists.newArrayList();
		nameSet.addAll(valueMap.keySet());
		Collections.sort(nameSet);
		
		List<WhiteboardRef> sorted = Lists.newArrayList();
		nameSet.forEach((name) -> sorted.add(valueMap.get(name)));
		
		return sorted;
	}
	
	/** Returns true if the given reference shares the same name, type, and whiteboard as this one */
	public boolean isSameRef(@Nullable WhiteboardRef refB) { return refB != null && refB.name.equals(this.name) && refB.varType == this.varType && refB.onBoard == this.onBoard; }
	
	public String name() { return this.name; }
	
	public BoardType boardType() { return this.onBoard; }
	
	public TFObjType<?> type() { return this.varType; }
	
	/** True if this value should not be stored in cache memory */
	public boolean uncached() { return this.noCache; }
	
	/**
	 * Sets this reference to ignore the whiteboard cache and recalculate each time it is needed.<br>
	 * This is most useful for values that change, like mob health or enemy position
	 */
	public WhiteboardRef noCache() { this.noCache = true; return this; }
	
	public WhiteboardRef filter() { this.isFilter = true; return this; }
	
	public boolean isFilter() { return this.isFilter; }
	
	public WhiteboardRef hidden() { this.isHidden = true; return this; }
	
	public boolean isHidden() { return this.isHidden; }
	
	public NbtCompound toNbt()
	{
		return (NbtCompound)CODEC.encodeStart(NbtOps.INSTANCE, this).result().get();
	}
	
	public static WhiteboardRef fromNbt(NbtCompound data)
	{
		return CODEC.parse(NbtOps.INSTANCE, data).resultOrPartial(TricksyFoxes.LOGGER::error).orElseThrow();
	}
	
	@Nullable
	public static <T extends Object> T findInMap(Map<WhiteboardRef, T> mapIn, WhiteboardRef ref)
	{
		return findInMap(mapIn, ref, null);
	}
	
	public static <T extends Object> T findInMap(Map<WhiteboardRef, T> mapIn, WhiteboardRef ref, T defaultValue)
	{
		for(Entry<WhiteboardRef, T> entry : mapIn.entrySet())
			if(entry.getKey().isSameRef(ref))
				return entry.getValue();
		return defaultValue;
	}
	
	/** Removes spaces and capitalisation to minimise errors */
	public static String conformName(String text)
	{
		return text.toLowerCase().trim().replaceAll("[^a-zA-Z0-9 _]", "").replace(' ', '_');
	}
	
	public static String conformTextToName(Text text)
	{
		return conformName(text.getString());
	}
}
