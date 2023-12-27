package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.StringIdentifiable;

public class CommandWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef ACTIVE = makeRef("is_active", TFObjType.BOOL, BoardType.COMMAND);
	public static final WhiteboardRef TYPE = makeRef("type", TFObjType.INT, BoardType.COMMAND);
	
	public static final WhiteboardRef POS = makeRef("position", TFObjType.BLOCK, BoardType.COMMAND);
	
	private boolean isDirty = false;
	
	public CommandWhiteboard()
	{
		super(BoardType.COMMAND, null);
	}
	
	public Whiteboard<?> build()
	{
		register(ACTIVE, () -> new WhiteboardObj.Bool(false));
		register(TYPE, () -> new WhiteboardObj.Int(0));
		
		register(POS, () -> new WhiteboardObjBlock());
		return this;
	}
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		CommandWhiteboard copy = new CommandWhiteboard();
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	public boolean hasOrder() { return currentType() != null; }
	
	@Nullable
	public Order currentType()
	{
		if(getValue(ACTIVE).as(TFObjType.BOOL).get())
		{
			int cap = Order.values().length;
			return Order.values()[getValue(TYPE).as(TFObjType.INT).get() % cap];
		}
		else
			return null;
	}
	
	/** Creates a whiteboard set to the given command, with no other data */
	public static CommandWhiteboard ofCommand(Order orderIn)
	{
		CommandWhiteboard board = new CommandWhiteboard();
		board.setValue(ACTIVE, new WhiteboardObj.Bool(true));
		board.setValue(TYPE, new WhiteboardObj.Int(orderIn.ordinal()));
		board.markDirty(true);
		return board;
	}
	
	public boolean isDirty() { return this.isDirty; }
	
	public void markDirty(boolean par1Bool) { this.isDirty = par1Bool; }
	
	public static enum Order implements StringIdentifiable
	{
		GOTO;
		
		public String asString() { return name().toLowerCase(); }
		
		public static Order fromString(String nameIn)
		{
			for(Order order : values())
				if(order.asString().equalsIgnoreCase(nameIn))
					return order;
			return null;
		}
	}
}
