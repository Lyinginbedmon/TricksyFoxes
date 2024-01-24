package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/** A whiteboard containing globally-accessible values set by a tricksy mob's sage */
public class GlobalWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef SPAWN = makeSystemRef("spawn_pos", TFObjType.BLOCK);
	public static final WhiteboardRef TIME = makeSystemRef("current_time", TFObjType.INT);
	public static final WhiteboardRef TICK = makeSystemRef("game_time", TFObjType.INT);
	public static final WhiteboardRef DAY = makeSystemRef("current_day", TFObjType.INT);
	public static final WhiteboardRef MOON = makeSystemRef("lunar_cycle", TFObjType.INT);
	
	protected static WhiteboardRef makeSystemRef(String name, TFObjType<?> type) { return makeSystemRef(name, type, BoardType.GLOBAL); }
	
	public GlobalWhiteboard(World worldIn)
	{
		super(BoardType.GLOBAL, worldIn);
	}
	
	public Whiteboard<?> build()
	{
		register(SPAWN, () -> new WhiteboardObjBlock(world.getSpawnPos(), Direction.UP));
		register(TIME, () -> new WhiteboardObj.Int((int)(world.getTimeOfDay() % 24000L) / Reference.Values.TICKS_PER_SECOND));
		register(TICK, () -> new WhiteboardObj.Int((int)world.getTime()));
		register(DAY, () -> new WhiteboardObj.Int((int)(world.getTimeOfDay() / 24000L) + 1));
		register(MOON, () -> new WhiteboardObj.Int((int)(world.getTimeOfDay() / 24000L) % 8 + 1));
		return this;
	}
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		GlobalWhiteboard copy = new GlobalWhiteboard(this.world);
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
}