package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFSpecialVisual
{
	private static final List<TFSpecialVisual> VISUALS = Lists.newArrayList();
	
	public static final TFSpecialVisual ONRYOJI_BALANCE = register(ofName("onryoji_balance"));
	public static final TFSpecialVisual GOAT_JUMP = register(ofName("goat_jump"));
	public static final TFSpecialVisual WOLF_BLESS = register(ofName("wolf_bless"));
	
	public static final Event<RegisterVisual> REGISTER_VISUAL = EventFactory.createArrayBacked(RegisterVisual.class, callbacks -> (registry) -> 
	{
		for(RegisterVisual callback : callbacks)
			callback.registerVisuals(registry);
	});
	
	@FunctionalInterface
	public interface RegisterVisual
	{
		void registerVisuals(SpecialVisualRegistry registry);
	}
	
	private static TFSpecialVisual ofName(String nameIn) { return new TFSpecialVisual(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
	
	private final Identifier registryName;
	
	public TFSpecialVisual(Identifier nameIn)
	{
		registryName = nameIn;
	}
	
	private static TFSpecialVisual register(TFSpecialVisual status)
	{
		VISUALS.add(status);
		return status;
	}
	
	public static void init()
	{
		SpecialVisualRegistry registry = new SpecialVisualRegistry();
		VISUALS.forEach(visual -> registry.registry(visual));
		REGISTER_VISUAL.invoker().registerVisuals(registry);
		
		registry.specialVisuals.forEach((name, type) -> Registry.register(TFRegistries.VISUAL_REGISTRY, name, type));
	}
	
	public Identifier name() { return registryName; }
	
	public String asString() { return name().getPath().toLowerCase(); }
	
	@Nullable
	public static TFSpecialVisual fromString(Identifier nameIn)
	{
		return TFRegistries.VISUAL_REGISTRY.get(nameIn);
	}
	
	public static class SpecialVisualRegistry
	{
		private final Map<Identifier, TFSpecialVisual> specialVisuals = new HashMap<>();
		
		public void registry(TFSpecialVisual visual) { specialVisuals.put(visual.name(), visual); }
	}
}
