package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.reference.Reference;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFSpecialVisual
{
	private static final Map<Identifier, TFSpecialVisual> STATUSES = new HashMap<>();
	
	public static final TFSpecialVisual ONRYOJI_BALANCE = register(ofName("onryoji_balance"));
	public static final TFSpecialVisual WOLF_BLESS = register(ofName("wolf_bless"));
	
	public static TFSpecialVisual ofName(String nameIn) { return new TFSpecialVisual(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
	
	private final Identifier registryName;
	
	public TFSpecialVisual(Identifier nameIn)
	{
		registryName = nameIn;
	}
	
	public static TFSpecialVisual register(TFSpecialVisual status)
	{
		STATUSES.put(status.name(), status);
		return status;
	}
	
	public static void init()
	{
		STATUSES.forEach((name, type) -> Registry.register(TFRegistries.VISUAL_REGISTRY, name, type));
	}
	
	public Identifier name() { return registryName; }
	
	public String asString() { return name().getPath().toLowerCase(); }
	
	@Nullable
	public static TFSpecialVisual fromString(Identifier nameIn)
	{
		return TFRegistries.VISUAL_REGISTRY.get(nameIn);
	}
}
