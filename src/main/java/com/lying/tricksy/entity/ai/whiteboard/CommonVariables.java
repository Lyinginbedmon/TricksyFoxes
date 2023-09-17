package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.init.TFObjType;

public class CommonVariables
{
	/** Any generic single value */
	public static final WhiteboardRef VAR = new WhiteboardRef("value", TFObjType.BOOL);
	/** A generic value in a pair or sequence */
	public static final WhiteboardRef VAR_A = new WhiteboardRef("value_a", TFObjType.BOOL);
	/** The second generic value in a pair or sequence */
	public static final WhiteboardRef VAR_B = new WhiteboardRef("value_b", TFObjType.BOOL);
	/** A location or destination */
	public static final WhiteboardRef VAR_POS = new WhiteboardRef("position", TFObjType.BLOCK);
	/** A distance value */
	public static final WhiteboardRef VAR_DIS = new WhiteboardRef("distance", TFObjType.INT);
}
