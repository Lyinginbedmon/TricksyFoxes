package com.lying.tricksy.init;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TFAccomplishments
{
	private static final List<Accomplishment> ACCOMPLISHMENTS = Lists.newArrayList();
	
	private static final Predicate<MobEntity> IN_NETHER = (mob) -> mob.getWorld().getDimensionEntry() == World.NETHER;
	private static final Predicate<MobEntity> IN_OVERWORLD = (mob) -> mob.getWorld().getDimensionEntry() == World.OVERWORLD;
	private static final Predicate<MobEntity> ON_GROUND = (mob) -> mob.isOnGround();
	private static final Predicate<MobEntity> LOW_HEALTH = (mob) -> mob.getHealth() <= 2F;
	
	public static final Accomplishment VISIT_NETHER = make("visit_nether").condition(IN_NETHER);
	public static final Accomplishment VISIT_OVERWORLD = make("visit_overworld").condition(IN_OVERWORLD);
	public static final Accomplishment VISIT_END = make("visit_end").condition((mob) -> mob.getWorld().getDimensionEntry() == World.END);
	public static final Accomplishment DIMENSIONAL_TRAVEL = make("dimensional_travel");
	public static final Accomplishment SQUIRE = make("squire");
	public static final Accomplishment CLOUDSEEKER = make("cloudseeker").condition(ON_GROUND).condition(IN_OVERWORLD).condition((mob) -> mob.getY() == World.MAX_Y);
	public static final Accomplishment OUTSIDE_THE_BOX = make("outside_the_box").condition(ON_GROUND).condition(IN_NETHER).condition((mob) -> mob.getY() >= 128);
	public static final Accomplishment FIRETOUCHED = make("firetouched").condition(LOW_HEALTH).condition((mob) -> !mob.isFireImmune() && !mob.isInvulnerableTo(mob.getWorld().getDamageSources().onFire()) && !mob.hasStatusEffect(StatusEffects.FIRE_RESISTANCE));
	public static final Accomplishment WATERBORNE = make("waterborne").condition(LOW_HEALTH).condition((mob) -> !mob.isInvulnerableTo(mob.getWorld().getDamageSources().drown()) && !mob.hasStatusEffect(StatusEffects.FIRE_RESISTANCE));
	public static final Accomplishment FISHERMAN = make("fisherman").condition((mob) -> mob.getMainHandStack().isIn(ItemTags.FISHES) || mob.getOffHandStack().isIn(ItemTags.FISHES));
	
	private static Accomplishment make(String nameIn)
	{
		Accomplishment made = new Accomplishment(new Identifier(Reference.ModInfo.MOD_ID, nameIn));
		ACCOMPLISHMENTS.add(made);
		return made;
	}
	
	public static Accomplishment get(Identifier nameIn)
	{
		for(Accomplishment acc : ACCOMPLISHMENTS)
			if(acc.registryName().equals(nameIn))
				return acc;
		return null;
	}
}
