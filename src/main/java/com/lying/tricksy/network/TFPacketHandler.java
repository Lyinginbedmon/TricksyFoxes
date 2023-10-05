package com.lying.tricksy.network;

import com.lying.tricksy.reference.Reference;

import net.minecraft.util.Identifier;

public class TFPacketHandler
{
	public static final Identifier ADD_GLOBAL_REF_ID	= make("add_global_ref");
	public static final Identifier CLOSE_TREE_ID		= make("close_tree");
	public static final Identifier DELETE_REF_ID		= make("delete_ref");
	public static final Identifier REF_ADDED_ID			= make("ref_added");
	public static final Identifier SAVE_TREE_ID			= make("save_tree");
	public static final Identifier SYNC_SCRIPTURE_ID	= make("sync_scripture_screen");
	public static final Identifier SYNC_TREE_ID			= make("sync_tree_screen");
	
	private static Identifier make(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }
}
