package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.reference.Reference;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** The various potential statuses to be reported to a {@link NodeStatusLog} */
public class TFNodeStatus
{
	private static final Map<Identifier, TFNodeStatus> STATUSES = new HashMap<>();
	
	public static final TFNodeStatus FAILURE = register(ofName("failure"));
	public static final TFNodeStatus SUCCESS = register(ofName("success"));
	public static final TFNodeStatus RUNNING = register(ofName("running"));
	
	public static final TFNodeStatus INVALID_USER = register(ofName("invalid_user"));
	public static final TFNodeStatus NO_CHILDREN = register(ofName("no_children"));
	public static final TFNodeStatus MISSING_IO = register(ofName("missing_io"));
	public static final TFNodeStatus BAD_IO = register(ofName("bad_io"));
	
	public static final TFNodeStatus INPUT_ERROR = register(ofName("input_error"));
	public static final TFNodeStatus OUTPUT_ERROR = register(ofName("output_error"));
	public static final TFNodeStatus BAD_RESULT = register(ofName("bad_result"));
	
	public static final TFNodeStatus ON_COOLDOWN = register(ofName("cooldown"));
	public static final TFNodeStatus FLAGS_OCCUPIED = register(ofName("flags_occupied"));
	
	public static TFNodeStatus ofName(String nameIn) { return new TFNodeStatus(new Identifier(Reference.ModInfo.MOD_ID, nameIn)); }
	
	private final Identifier registryName;
	
	public TFNodeStatus(Identifier nameIn)
	{
		registryName = nameIn;
	}
	
	public static TFNodeStatus register(TFNodeStatus status)
	{
		STATUSES.put(status.name(), status);
		return status;
	}
	
	public static void init()
	{
		STATUSES.forEach((name, type) -> Registry.register(TFRegistries.STATUS_REGISTRY, name, type));
	}
	
	public Identifier name() { return registryName; }
	
	public String asString() { return name().getPath().toLowerCase(); }
	
	public final boolean isResult() { return this == SUCCESS || this == FAILURE || this == RUNNING; }
	
	public Identifier texture() { return new Identifier(name().getNamespace(), "textures/gui/node_results/"+asString()+".png"); }
	
	@Nullable
	public static TFNodeStatus fromString(Identifier nameIn)
	{
		return TFRegistries.STATUS_REGISTRY.get(nameIn);
	}
}
