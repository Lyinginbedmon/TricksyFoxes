package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj.Bool;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj.Int;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj.Item;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjRegion;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.Region;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TFObjType<T>
{
	private static final Map<Identifier, TFObjType<?>> REGISTRY = new HashMap<>();
	private static int iconIndex = 0;
	
	/** Empty value, usually obtained when the whiteboard grabs a value it doesn't have */
	public static final TFObjType<Object> EMPTY = register(new TFObjType<>("empty", () -> WhiteboardObj.EMPTY)
			.emptyIf((obj) -> true));
	/** Boolean true/false value */
	public static final TFObjType<Boolean> BOOL = register(new TFObjType<>("boolean", () -> new Bool()))
			.castTo(TFObjType.EMPTY, (obj) -> TFObjType.BOOL.blank())
			.castTo(TFObjType.INT, (obj) -> new WhiteboardObj.Int(obj.get() ? 1 : 0));
	/** Numerical value, always between zero and {@link Integer.MAX_VALUE} */
	public static final TFObjType<Integer> INT = register(new TFObjType<>("integer", () -> new Int())
			.castTo(TFObjType.EMPTY, (obj) -> TFObjType.INT.blank())
			.castTo(TFObjType.BOOL, (obj) -> new WhiteboardObj.Bool(obj.get() > 0))
			.emptyIf((obj) -> obj.get() <= 0));
	/** Block position with optional direction for addressing specific sides of containers */
	public static final TFObjType<BlockPos> BLOCK = register(new TFObjType<>("block", () -> new WhiteboardObjBlock()));
	/** Pair of block positions representing opposite corners of a cuboid area */
	public static final TFObjType<Region> REGION = register(new TFObjType<>("region", () -> new WhiteboardObjRegion()))
			.castTo(TFObjType.BLOCK, (obj) -> new WhiteboardObjBlock(obj.get().center()));
	/** Entity value, the only object type that must be recached after loading to restore its value */
	public static final TFObjType<Entity> ENT = register(new TFObjType<>("entity", () -> new WhiteboardObjEntity())
			.castTo(TFObjType.BLOCK, (obj) -> new WhiteboardObjBlock(obj.get().getBlockPos(), Direction.UP))
			.emptyIf((obj) -> obj.get() == null || !obj.get().isAlive() || obj.get().isSpectator()));
	/** ItemStack value */
	public static final TFObjType<ItemStack> ITEM = register(new TFObjType<>("item", () -> new Item())
			.castTo(TFObjType.INT, (obj) -> new WhiteboardObj.Int(obj.get().getCount()))
			.emptyIf((obj) -> obj.get() == null || obj.get().isEmpty()));
	
	private final Identifier name;
	private final Supplier<IWhiteboardObject<T>> supplier;
	private int texIndex;
	
	private Map<TFObjType<?>, Function<IWhiteboardObject<T>, ?>> castingMap = new HashMap<>();
	private Predicate<IWhiteboardObject<T>> isEmpty = (obj) -> obj.get() == null;
	
	public TFObjType(String nameIn, Supplier<IWhiteboardObject<T>> supplierIn)
	{
		this(new Identifier(Reference.ModInfo.MOD_ID, nameIn), supplierIn);
	}
	
	public TFObjType(Identifier nameIn, Supplier<IWhiteboardObject<T>> supplierIn)
	{
		this.name = nameIn;
		this.supplier = supplierIn;
	}
	
	public String toString() { return name.getPath(); }
	
	public Identifier registryName() { return this.name; }
	
	public Text translated() { return Text.translatable("type."+registryName().getNamespace()+"."+registryName().getPath().toString()); }
	
	public int texIndex() { return this.texIndex; }
	
	public static void init() { }
	
	public IWhiteboardObject<T> create(NbtCompound data)
	{
		IWhiteboardObject<T> obj = supplier.get();
		obj.readFromNbt(data);
		return obj;
	}
	
	private TFObjType<T> castTo(TFObjType<?> type, Function<IWhiteboardObject<T>, ?> logic)
	{
		castingMap.put(type, logic);
		return this;
	}
	
	public TFObjType<T> emptyIf(Predicate<IWhiteboardObject<T>> logicIn)
	{
		this.isEmpty = logicIn;
		return this;
	}
	
	public boolean castableTo(TFObjType<?> typeIn) { return typeIn == this || castingMap.containsKey(typeIn); }
	
	/** Applies type-casting logic to convert this object into an object of the given type */
	@SuppressWarnings("unchecked")
	public <N> IWhiteboardObject<N> getAs(TFObjType<N> typeIn,  IWhiteboardObject<T> obj)
	{
		if(typeIn == this)
			return (IWhiteboardObject<N>)obj;
		else if(this == TFObjType.EMPTY)
			return typeIn.blank();
		else if(castingMap.containsKey(typeIn))
			return (IWhiteboardObject<N>)castingMap.get(typeIn).apply(obj);
		return typeIn.blank();
	}
	
	public IWhiteboardObject<T> blank() { return supplier.get(); }
	
	public boolean isEmpty(IWhiteboardObject<T> obj) { return isEmpty.test(obj); }
	
	private static <N> TFObjType<N> register(TFObjType<N> typeIn)
	{
		typeIn.texIndex = iconIndex++;
		REGISTRY.put(typeIn.registryName(), typeIn);
		return typeIn;
	}
	
	public static TFObjType<?> getType(Identifier nameIn)
	{
		for(Entry<Identifier, TFObjType<?>> entry : REGISTRY.entrySet())
			if(entry.getKey().toString().equals(nameIn.toString()))
				return entry.getValue();
		return null;
	}
}