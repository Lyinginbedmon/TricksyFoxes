package com.lying.tricksy.init;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TFCommands
{
	private static final Text GENERIC_FAIL = Text.translatable("command."+Reference.ModInfo.MOD_ID+".failed");
	private static final String ACC_SLUG = "command."+Reference.ModInfo.MOD_ID+".accomplishments";
	
	public static void init()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
		{
			dispatcher.register(literal(Reference.ModInfo.MOD_ID).requires(source -> source.hasPermissionLevel(4))
					.then(literal("enlighten")
						.then(argument("targets", EntityArgumentType.entities())
						.executes(context ->
						{
							int tally = 0;
							for(Entity ent : EntityArgumentType.getEntities(context, "targets"))
								if(tryToEnlighten(ent, context.getSource()))
									tally++;
							return tally > 0 ? 1 : 0;
						})))
					.then(literal("accomplishments")
						.then(literal("list")
							.executes(context -> listAccomplishments(context.getSource())))
						.then(literal("get")
							.then(argument("target", EntityArgumentType.entity())
							.executes(context -> tryGetAccomplishments(EntityArgumentType.getEntity(context, "target"), context.getSource()))))
						.then(literal("grant")
							.then(argument("target", EntityArgumentType.entity())
							.then(argument("accomplishment", RegistryEntryArgumentType.registryEntry(registryAccess, TFAccomplishments.ACC_KEY))
							.executes(context -> tryAddAccomplishment(EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getRegistryEntry(context, "accomplishment", TFAccomplishments.ACC_KEY), context.getSource())))))
						.then(literal("revoke")
							.then(argument("target", EntityArgumentType.entity())
								.then(literal("all")
								.executes(context -> revokeAll(EntityArgumentType.getEntity(context, "target"), context.getSource())))
							.then(argument("accomplishment", RegistryEntryArgumentType.registryEntry(registryAccess, TFAccomplishments.ACC_KEY))
							.executes(context -> tryRevokeAccomplishment(EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getRegistryEntry(context, "accomplishment", TFAccomplishments.ACC_KEY), context.getSource())))))));
		});
	}
	
	private static boolean tryToEnlighten(Entity ent, ServerCommandSource source)
	{
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			if(comp.canBeEnlightened())
				result = comp.enlighten();
		}
		catch(Exception e) { }
		Text message = Text.translatable("command."+Reference.ModInfo.MOD_ID+".enlighten."+(result ? "success" : "failed"), ent.getDisplayName());
		source.sendFeedback(() -> message, true);
		return result;
	}
	
	private static int listAccomplishments(ServerCommandSource source)
	{
		source.sendFeedback(() -> Text.translatable(ACC_SLUG+".list", TFAccomplishments.getAll().size(), listToText(TFAccomplishments.getAll())), true);
		return 1;
	}
	
	private static int tryGetAccomplishments(Entity ent, ServerCommandSource source)
	{
		List<Accomplishment> set = Lists.newArrayList();
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			set = comp.getAccomplishments();
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		Text message;
		if(set.isEmpty())
			message = Text.translatable(ACC_SLUG+".get.failed", ent.getDisplayName());
		else
			message = Text.translatable(ACC_SLUG+".get.success", ent.getDisplayName(), listToText(set));
		source.sendFeedback(() -> message, true);
		return set.isEmpty() ? 0 : 1;
	}
	
	private static int tryAddAccomplishment(Entity ent, RegistryEntry.Reference<Accomplishment> acc, ServerCommandSource source)
	{
		Accomplishment accomplishment = acc.value();
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.addAccomplishment(accomplishment, true);
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		Text message = Text.translatable(ACC_SLUG+".grant."+(result ? "success" : "failed"), ent.getDisplayName(), accomplishment.translate());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static int tryRevokeAccomplishment(Entity ent, RegistryEntry.Reference<Accomplishment> acc, ServerCommandSource source)
	{
		Accomplishment accomplishment = acc.value();
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.revokeAccomplishment(accomplishment);
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		Text message = Text.translatable(ACC_SLUG+".revoke."+(result ? "success" : "failed"), accomplishment.translate(), ent.getDisplayName());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static int revokeAll(Entity ent, ServerCommandSource source)
	{
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.revokeAllAccomplishments();
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		Text message = Text.translatable(ACC_SLUG+".revoke.all", ent.getDisplayName());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static MutableText listToText(List<Accomplishment> set)
	{
		if(set.isEmpty())
			return Text.empty();
		
		MutableText names = set.get(0).translate();
		if(set.size() > 1)
			for(int i=1; i<set.size(); i++)
				names.append(Text.literal(", ")).append(set.get(i).translate());
		return names;
	}
}
