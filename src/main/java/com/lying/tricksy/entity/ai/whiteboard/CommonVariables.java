package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.text.Text;

public class CommonVariables
{
	/** Any generic single value */
	public static final WhiteboardRef VAR = new WhiteboardRef("value", TFObjType.BOOL).displayName(translate("generic_value"));
	/** A generic value in a pair or sequence */
	public static final WhiteboardRef VAR_A = new WhiteboardRef("value_a", TFObjType.BOOL).displayName(translate("value_a"));
	/** The second generic value in a pair or sequence */
	public static final WhiteboardRef VAR_B = new WhiteboardRef("value_b", TFObjType.BOOL).displayName(translate("value_b"));
	/** A location or destination */
	public static final WhiteboardRef VAR_POS = new WhiteboardRef("position", TFObjType.BLOCK).displayName(translate("position"));
	public static final WhiteboardRef VAR_POS_A = new WhiteboardRef("position_a", TFObjType.BLOCK).displayName(translate("position_a"));
	public static final WhiteboardRef VAR_POS_B = new WhiteboardRef("position_b", TFObjType.BLOCK).displayName(translate("position_b"));
	public static final WhiteboardRef VAR_SIDE = new WhiteboardRef("side", TFObjType.BLOCK).displayName(translate("side"));
	/** A distance value */
	public static final WhiteboardRef VAR_DIS = new WhiteboardRef("distance", TFObjType.INT).displayName(translate("range"));
	/** A number value */
	public static final WhiteboardRef VAR_NUM = new WhiteboardRef("count", TFObjType.INT).displayName(translate("number"));
	/** A receiver entity */
	public static final WhiteboardRef TARGET_ENT = new WhiteboardRef("target", TFObjType.ENT).displayName(translate("ref_ent"));
	public static final WhiteboardRef VAR_ITEM = new WhiteboardRef("item", TFObjType.ITEM).displayName(translate("item"));
	public static final WhiteboardRef X = new WhiteboardRef("x_coord", TFObjType.INT).displayName(Text.literal("X"));
	public static final WhiteboardRef Y = new WhiteboardRef("y_coord", TFObjType.INT).displayName(Text.literal("Y"));
	public static final WhiteboardRef Z = new WhiteboardRef("z_coord", TFObjType.INT).displayName(Text.literal("Z"));
	public static final WhiteboardRef SUBTRACT = new WhiteboardRef("subtract", TFObjType.BOOL).displayName(translate("subtract"));
	public static final WhiteboardRef INVERT = new WhiteboardRef("invert", TFObjType.BOOL).displayName(translate("invert"));
	
	public static Text translate(String nameIn) { return Text.translatable("variable."+Reference.ModInfo.MOD_ID+"."+nameIn); }
}
