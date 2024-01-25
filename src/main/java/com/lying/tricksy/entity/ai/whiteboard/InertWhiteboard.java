package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFWhiteboards.BoardType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class InertWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public InertWhiteboard(BoardType typeIn, World worldIn)
	{
		super(typeIn, worldIn);
	}
	
	public Whiteboard<?> build() { return this; }
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		InertWhiteboard copy = new InertWhiteboard(this.type, this.world);
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
}
