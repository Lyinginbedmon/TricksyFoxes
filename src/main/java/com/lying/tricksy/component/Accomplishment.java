package com.lying.tricksy.component;

import java.util.function.Predicate;

import com.lying.tricksy.init.TFAccomplishments;
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
	
	private Predicate<MobEntity> conditions;
	
	public Accomplishment(Identifier nameIn)
	{
		this.name = nameIn;
	}
	
	public final Identifier registryName() { return name; }
	
	public MutableText translate()
	{
		String slug = "accomplishment."+Reference.ModInfo.MOD_ID+"."+name.getPath();
		final MutableText desc = Text.translatable(slug+".desc");
		MutableText name = Text.translatable(slug).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, !this.name.equals(TFAccomplishments.INCONCEIVABLE.registryName()) ? desc : desc.formatted(Formatting.OBFUSCATED))));
		return Text.literal("[").append(name).append("]");
	}
	
	public final Accomplishment condition(Predicate<MobEntity> conditionIn)
	{
		if(conditions == null)
			conditions = conditionIn;
		else
			conditions = conditions.and(conditionIn);
		return this;
	}
	
	public final boolean achieved(MobEntity entity) { return conditions == null ? true : conditions.test(entity); }
}
