package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/** A whiteboard containing globally-accessible values set by a tricksy mob's sage */
public class GlobalWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef SPAWN = makeSystemRef("spawn_pos", TFObjType.BLOCK, BoardType.GLOBAL);
	
	public GlobalWhiteboard(World worldIn)
	{
		super(BoardType.GLOBAL, worldIn);
	}
	
	public Whiteboard<?> build()
	{
		register(SPAWN, () -> new WhiteboardObjBlock(world.getSpawnPos(), Direction.UP));
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