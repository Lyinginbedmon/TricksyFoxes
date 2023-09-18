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
	/** A distance value */
	public static final WhiteboardRef VAR_DIS = new WhiteboardRef("distance", TFObjType.INT).displayName(translate("range"));
	/** A number value */
	public static final WhiteboardRef VAR_COUNT = new WhiteboardRef("count", TFObjType.INT).displayName(translate("number"));
	/** A receiver entity */
	public static final WhiteboardRef TARGET_ENT = new WhiteboardRef("target", TFObjType.ENT).displayName(translate("ref_ent"));
	
	public static Text translate(String nameIn) { return Text.translatable("variable."+Reference.ModInfo.MOD_ID+"."+nameIn); }
}
