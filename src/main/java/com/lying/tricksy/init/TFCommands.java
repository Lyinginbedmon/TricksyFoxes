package com.lying.tricksy.init;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.TricksyUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TFCommands
{
	private static final Text GENERIC_FAIL = Text.translatable("command."+Reference.ModInfo.MOD_ID+".failed");
	private static final String ACC_SLUG = "command."+Reference.ModInfo.MOD_ID+".accomplishments";
	
	private static final SimpleCommandExceptionType REVOKE_FAILED = new SimpleCommandExceptionType(Text.translatable(ACC_SLUG+".revoke.failed"));
	
	private static final String TARGET_KEY = "target";
	private static final String ACC_KEY = "accomplishment";
	
	public static void init()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> 
		{
			dispatcher.register(literal(Reference.ModInfo.MOD_ID).requires(source -> source.hasPermissionLevel(2))
					.then(literal("enlighten")
						.then(argument("targets", EntityArgumentType.entities())
						.executes(context ->
						{
							int tally = 0;
							for(Entity ent : EntityArgumentType.getEntities(context, "targets"))
								if(tryToEnlighten(ent, context.getSource()))
									tally++;
							return Math.min(15, tally);
						})))
					.then(literal("accomplishments")
						.then(literal("list")
							.executes(context -> listAccomplishments(context.getSource())))
						.then(argument(TARGET_KEY, EntityArgumentType.entity())
						.then(literal("get")
							.executes(context -> tryGetAccomplishments(EntityArgumentType.getEntity(context, TARGET_KEY), context.getSource())))
						.then(literal("grant")
								.then(literal("all")
								.executes(context -> grantAll(EntityArgumentType.getEntity(context, TARGET_KEY), context.getSource())))
							.then(argument(ACC_KEY, RegistryEntryArgumentType.registryEntry(registryAccess, TFRegistries.ACC_KEY))
							.executes(context -> tryAddAccomplishment(EntityArgumentType.getEntity(context, TARGET_KEY), RegistryEntryArgumentType.getRegistryEntry(context, ACC_KEY, TFRegistries.ACC_KEY), context.getSource()))))
						.then(literal("test")
								.then(argument(ACC_KEY, RegistryEntryArgumentType.registryEntry(registryAccess, TFRegistries.ACC_KEY))
								.executes(context -> tryTestAccomplishment(EntityArgumentType.getEntities(context, TARGET_KEY), RegistryEntryArgumentType.getRegistryEntry(context, ACC_KEY, TFRegistries.ACC_KEY), context.getSource()))))
						.then(literal("revoke")
								.then(literal("all")
								.executes(context -> revokeAll(EntityArgumentType.getEntity(context, TARGET_KEY), context.getSource())))
							.then(argument(ACC_KEY, RegistryEntryArgumentType.registryEntry(registryAccess, TFRegistries.ACC_KEY))
							.executes(context -> tryRevokeAccomplishment(EntityArgumentType.getEntity(context, TARGET_KEY), RegistryEntryArgumentType.getRegistryEntry(context, ACC_KEY, TFRegistries.ACC_KEY), context.getSource())))))));
		});
	}
	
	private static boolean tryToEnlighten(Entity ent, ServerCommandSource source) throws CommandSyntaxException
	{
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			if(comp.canBeEnlightened())
				result = comp.enlighten();
		}
		catch(Exception e) { }
		if(!result)
			throw (new SimpleCommandExceptionType(Text.translatable(ACC_SLUG+".enlighten.failed", ent.getDisplayName()))).create();
		Text message = Text.translatable("command."+Reference.ModInfo.MOD_ID+".enlighten.success", ent.getDisplayName());
		source.sendFeedback(() -> message, true);
		return result;
	}
	
	private static int listAccomplishments(ServerCommandSource source)
	{
		List<Accomplishment> set = TFAccomplishments.getAll();
		Collections.sort(set, (acc1,acc2) -> TricksyUtils.stringComparator(acc1.translate().getString(), acc2.translate().getString()));
		source.sendFeedback(() -> Text.translatable(ACC_SLUG+".list", TFAccomplishments.getAll().size(), listToText(set)), true);
		return 1;
	}
	
	private static int tryGetAccomplishments(Entity ent, ServerCommandSource source) throws CommandSyntaxException
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
			throw (new SimpleCommandExceptionType(Text.translatable(ACC_SLUG+".get.failed", ent.getDisplayName()))).create();
		else
			message = Text.translatable(ACC_SLUG+".get.success", ent.getDisplayName(), listToText(set));
		source.sendFeedback(() -> message, true);
		return set.isEmpty() ? 0 : 1;
	}
	
	private static int grantAll(Entity ent, ServerCommandSource source) throws CommandSyntaxException
	{
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.grantAllAccomplishments();
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		Text message = Text.translatable(ACC_SLUG+".revoke.all", ent.getDisplayName());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static int tryAddAccomplishment(Entity ent, RegistryEntry.Reference<Accomplishment> acc, ServerCommandSource source) throws CommandSyntaxException
	{
		Accomplishment accomplishment = acc.value();
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.addAccomplishment(accomplishment, true);
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		if(!result)
			throw (new SimpleCommandExceptionType(Text.translatable(ACC_SLUG+".grant.failed", ent.getDisplayName(), accomplishment.translate()))).create();
		
		Text message = Text.translatable(ACC_SLUG+".grant.success", ent.getDisplayName(), accomplishment.translate());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static int tryTestAccomplishment(Collection<? extends Entity> ent, RegistryEntry.Reference<Accomplishment> acc, ServerCommandSource source) throws CommandSyntaxException
	{
		Accomplishment accomplishment = acc.value();
		boolean result = ent.stream().anyMatch(entity -> entity instanceof MobEntity && TFComponents.TRICKSY_TRACKING.get((MobEntity)entity).hasAchieved(accomplishment));
		if(!result)
			throw (new SimpleCommandExceptionType(Text.translatable(ACC_SLUG+".test.failed", accomplishment.translate()))).create();
		
		Text message = Text.translatable(ACC_SLUG+".test.success", accomplishment.translate());
		source.sendFeedback(() -> message, true);
		return result ? 1 : 0;
	}
	
	private static int tryRevokeAccomplishment(Entity ent, RegistryEntry.Reference<Accomplishment> acc, ServerCommandSource source) throws CommandSyntaxException
	{
		Accomplishment accomplishment = acc.value();
		boolean result = false;
		try
		{
			TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(ent);
			result = comp.revokeAccomplishment(accomplishment);
		}
		catch(Exception e) { source.sendFeedback(() -> GENERIC_FAIL, true); return 0; }
		if(!result)
			throw REVOKE_FAILED.create();
		
		Text message = Text.translatable(ACC_SLUG+".revoke.success", accomplishment.translate(), ent.getDisplayName());
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
