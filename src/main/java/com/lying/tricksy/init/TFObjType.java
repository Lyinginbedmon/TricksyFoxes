package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj.Bool;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj.Int;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj.Item;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj.Pos;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBase;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TFObjType<T>
{
	private static final Map<Identifier, TFObjType<?>> REGISTRY = new HashMap<>();
	
	/** Empty value, usually obtained when the whiteboard grabs a value it doesn't have */
	public static final TFObjType<Object> EMPTY = register(new TFObjType<>("empty", () -> WhiteboardObj.EMPTY)
			.emptyLogic((obj) -> true));
	/** Boolean true/false value */
	public static final TFObjType<Boolean> BOOL = register(new TFObjType<>("boolean", () -> new Bool(false)));
	/** Numerical value, always between zero and {@link Integer.MAX_VALUE} */
	public static final TFObjType<Integer> INT = register(new TFObjType<>("integer", () -> new Int(0))
			.emptyLogic((obj) -> obj.get() <= 0));
	/** Block position */
	public static final TFObjType<BlockPos> BLOCK = register(new TFObjType<>("block", () -> new Pos(BlockPos.ORIGIN)));
	/** Entity value, the only object type that must be recached after loading to restore its value */
	public static final TFObjType<Entity> ENT = register(new TFObjType<>("entity", () -> new WhiteboardObjBase.Ent((Entity)null))
			.addCast(TFObjType.BLOCK, (obj) -> new WhiteboardObj.Pos(obj.get().getBlockPos()))
			.emptyLogic((obj) -> obj.get() == null || !obj.get().isAlive() || obj.get().isSpectator()));
	/** ItemStack value */
	public static final TFObjType<ItemStack> ITEM = register(new TFObjType<>("item", () -> new Item(ItemStack.EMPTY))
			.addCast(TFObjType.INT, (obj) -> new WhiteboardObj.Int(obj.get().getCount()))
			.emptyLogic((obj) -> obj.get() == null || obj.get().isEmpty()));
	
	private final Supplier<IWhiteboardObject<T>> supplier;
	private final Identifier name;
	
	private Map<TFObjType<?>, Function<IWhiteboardObject<T>, ?>> castingMap = new HashMap<>();
	private Function<IWhiteboardObject<T>, Boolean> emptyCheck = (obj) -> obj.get() == null;
	
	public TFObjType(String nameIn, Supplier<IWhiteboardObject<T>> supplierIn)
	{
		this(new Identifier(Reference.ModInfo.MOD_ID, nameIn), supplierIn);
	}
	
	public TFObjType(Identifier nameIn, Supplier<IWhiteboardObject<T>> supplierIn)
	{
		this.name = nameIn;
		this.supplier = supplierIn;
	}
	
	public Identifier registryName() { return this.name; }
	
	public static void init() { }
	
	public IWhiteboardObject<T> create(NbtCompound data)
	{
		IWhiteboardObject<T> obj = supplier.get();
		obj.readFromNbt(data);
		return obj;
	}
	
	private TFObjType<T> addCast(TFObjType<?> type, Function<IWhiteboardObject<T>, ?> logic) { castingMap.put(type, logic); return this; }
	
	public TFObjType<T> emptyLogic(Function<IWhiteboardObject<T>, Boolean> logicIn)
	{
		this.emptyCheck = logicIn;
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
	
	public IWhiteboardObject<T> blank() { return create(new NbtCompound()); }
	
	public boolean isEmpty(IWhiteboardObject<T> obj) { return emptyCheck.apply(obj); }
	
	private static <N> TFObjType<N> register(TFObjType<N> typeIn)
	{
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