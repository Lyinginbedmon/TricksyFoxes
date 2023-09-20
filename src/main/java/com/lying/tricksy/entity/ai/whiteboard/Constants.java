package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import com.lying.tricksy.init.TFObjType;

public class Constants extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef NUM_1 = new WhiteboardRef("number_1", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_2 = new WhiteboardRef("number_2", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_4 = new WhiteboardRef("number_4", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_8 = new WhiteboardRef("number_8", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_16 = new WhiteboardRef("number_16", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_32 = new WhiteboardRef("number_32", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef NUM_64 = new WhiteboardRef("number_64", TFObjType.INT, BoardType.CONSTANT);
	public static final WhiteboardRef BOOL_TRUE = new WhiteboardRef("boolean_true", TFObjType.BOOL, BoardType.CONSTANT);
	public static final WhiteboardRef BOOL_FALSE = new WhiteboardRef("boolean_false", TFObjType.BOOL, BoardType.CONSTANT);
	
	public Constants() { super(BoardType.CONSTANT, null); }
	
	public Whiteboard<?> build()
	{
		register(NUM_1, () -> new WhiteboardObj.Int(1));
		register(NUM_2, () -> new WhiteboardObj.Int(2));
		register(NUM_4, () -> new WhiteboardObj.Int(4));
		register(NUM_8, () -> new WhiteboardObj.Int(8));
		register(NUM_16, () -> new WhiteboardObj.Int(16));
		register(NUM_32, () -> new WhiteboardObj.Int(32));
		register(NUM_64, () -> new WhiteboardObj.Int(64));
		register(BOOL_TRUE, () -> new WhiteboardObj.Bool(true));
		register(BOOL_FALSE, () -> new WhiteboardObj.Bool(false));
		return this;
	}
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
}