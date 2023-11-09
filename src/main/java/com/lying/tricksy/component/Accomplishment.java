package com.lying.tricksy.component;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class Accomplishment
{
	private final Identifier name;
	private boolean checkEachTick = false;
	private boolean isObfuscated = false;
	
	private boolean hasPrecondition = false;
	private Predicate<MobEntity> preconditions = Predicates.alwaysFalse();
	private Predicate<MobEntity> conditions;
	
	public Accomplishment(Identifier nameIn)
	{
		this.name = nameIn;
	}
	
	public final Identifier registryName() { return name; }
	
	public final Accomplishment tick() { this.checkEachTick = true; return this; }
	
	public final boolean ticking() { return this.checkEachTick; }
	
	public final Accomplishment obfuscate() { this.isObfuscated = true; return this; }
	
	public MutableText translate()
	{
		String slug = "accomplishment."+Reference.ModInfo.MOD_ID+"."+name.getPath();
		final MutableText reg = Text.literal(name.toString()).formatted(Formatting.DARK_GRAY);
		final MutableText desc = (isObfuscated ? Text.translatable(slug+".desc").formatted(Formatting.OBFUSCATED) : Text.translatable(slug+".desc")).append("\n").append(reg);
		MutableText name = Text.translatable(slug).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
		return Text.literal("[").append(name).append("]");
	}
	
	/** Sets or expands the predicate that must be met to achieve this accomplishment */
	public final Accomplishment condition(Predicate<MobEntity> conditionIn)
	{
		if(conditions == null)
			conditions = conditionIn;
		else
			conditions = conditions.and(conditionIn);
		return this;
	}
	
	/** Defines a state that must be met, and exited, to achieve this accomplishment */
	public final Accomplishment precondition(Predicate<MobEntity> conditionIn)
	{
		if(conditionIn != null)
			this.hasPrecondition = true;
		this.preconditions = conditionIn;
		return this;
	}
	
	public final boolean hasPrecondition() { return this.hasPrecondition; }
	
	/** Returns true if any preconditions have no longer been met and any and all conditions have been met */
	public final boolean achieved(MobEntity entity) { return conditions == null ? true : !preconditionsMet(entity) && conditions.test(entity); }
	
	public final boolean preconditionsMet(MobEntity entity) { return preconditions.test(entity); }
}
