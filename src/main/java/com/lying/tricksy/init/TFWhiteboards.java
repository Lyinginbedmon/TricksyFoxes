package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TFWhiteboards
{
	private static final Map<Identifier, BoardType> BOARD_TYPES = new HashMap<>();
	
	public static final BoardType CONSTANT = register(new BoardType("constant", 0, true));
	public static final BoardType LOCAL = register(new BoardType("local", 3, false));
	public static final BoardType GLOBAL = register(new BoardType("global", 2, true));
	public static final BoardType ORDER = register(new BoardType("order", 1, true));
	public static final BoardType HOWL = register(new BoardType("howl", 4, true));
	
	private static BoardType register(BoardType typeIn)
	{
		BOARD_TYPES.put(typeIn.registryName(), typeIn);
		return typeIn;
	}
	
	public static void init()
	{
		BOARD_TYPES.forEach((name, type) -> Registry.register(TFRegistries.BOARD_REGISTRY, name, type));
	}
	
	@Nullable
	public static BoardType fromString(String nameIn)
	{
		for(BoardType type : TFRegistries.BOARD_REGISTRY)
			if(nameIn.equals(type.asString()))
				return type;
		return null;
	}
	
	public static List<BoardType> displayOrder()
	{
		List<BoardType> list = Lists.newArrayList();
		TFRegistries.BOARD_REGISTRY.forEach(type -> list.add(type));
		list.sort((a,b) -> a.displayIndex < b.displayIndex ? -1 : a.displayIndex > b.displayIndex ? 1 : 0);
		return list;
	}
	
	public static class BoardType
	{
		private final Identifier registryName;
		private final String name;
		private final boolean readOnly;
		private final int displayIndex;
		
		public BoardType(String nameIn, int indexIn, boolean isReadOnly)
		{
			name = nameIn;
			registryName = new Identifier(Reference.ModInfo.MOD_ID, nameIn.toLowerCase());
			displayIndex = indexIn;
			readOnly = isReadOnly;
		}
		
		public Identifier registryName() { return registryName; }
		
		public String asString() { return name.toLowerCase(); }
		
		public Text translate() { return Text.translatable("board."+Reference.ModInfo.MOD_ID+"."+asString()); }
		
		public boolean isReadOnly() { return this.readOnly; }
	}
}
