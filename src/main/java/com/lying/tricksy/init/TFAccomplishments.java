package com.lying.tricksy.init;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

public class TFAccomplishments
{
	public static final RegistryKey<Registry<Accomplishment>> ACC_KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "accomplishment"));
	public static final Registry<Accomplishment> REGISTRY = FabricRegistryBuilder.createSimple(ACC_KEY).buildAndRegister();
	
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
	public static final Accomplishment OUTLAW = make("outlaw");
	public static final Accomplishment DEATH_DEFIER = make("death_defier");
	public static final Accomplishment SCHOLAR = make("scholar").condition(mob -> LocationPredicate.feature(StructureKeys.STRONGHOLD).test((ServerWorld)mob.getWorld(), mob.getPos().getX(), mob.getPos().getY(), mob.getPos().getZ()));
	public static final Accomplishment JAILBIRD = make("jailbird").condition(mob -> LocationPredicate.feature(StructureKeys.ANCIENT_CITY).test((ServerWorld)mob.getWorld(), mob.getPos().getX(), mob.getPos().getY(), mob.getPos().getZ()));
	public static final Accomplishment ARCHAEOLOGIST = make("archaeologist").condition(mob -> LocationPredicate.feature(StructureKeys.TRAIL_RUINS).test((ServerWorld)mob.getWorld(), mob.getPos().getX(), mob.getPos().getY(), mob.getPos().getZ()));
	public static final Accomplishment INCONCEIVABLE = make("inconceivable");
	public static final Accomplishment CLOUDSEEKER = make("cloudseeker").condition(ON_GROUND).condition(IN_OVERWORLD).condition((mob) -> mob.getY() == World.MAX_Y);
	public static final Accomplishment OUTSIDE_THE_BOX = make("outside_the_box").condition(ON_GROUND).condition(IN_NETHER).condition((mob) -> mob.getY() >= 128);
	public static final Accomplishment FIRETOUCHED = make("firetouched").condition(LOW_HEALTH).condition((mob) -> !mob.isFireImmune() && !mob.isInvulnerableTo(mob.getWorld().getDamageSources().onFire()) && !mob.hasStatusEffect(StatusEffects.FIRE_RESISTANCE));
	public static final Accomplishment WATERBORNE = make("waterborne").condition(LOW_HEALTH).condition((mob) -> !mob.isInvulnerableTo(mob.getWorld().getDamageSources().drown()) && !mob.hasStatusEffect(StatusEffects.FIRE_RESISTANCE));
	public static final Accomplishment FISHERMAN = make("fisherman").condition((mob) -> mob.getMainHandStack().isIn(ItemTags.FISHES) || mob.getOffHandStack().isIn(ItemTags.FISHES));
	
	public static final Accomplishment[] PER_TICK = new Accomplishment[] 
			{
					CLOUDSEEKER,
					SCHOLAR,
					JAILBIRD,
					ARCHAEOLOGIST,
					OUTSIDE_THE_BOX
			};
	
	private static Accomplishment make(String nameIn)
	{
		Accomplishment made = new Accomplishment(new Identifier(Reference.ModInfo.MOD_ID, nameIn));
		ACCOMPLISHMENTS.add(made);
		return made;
	}
	
	@Nullable
	public static Accomplishment get(Identifier nameIn)
	{
		return REGISTRY.get(nameIn);
	}
	
	public static List<Accomplishment> getAll()
	{
		List<Accomplishment> values = Lists.newArrayList();
		REGISTRY.getEntrySet().forEach(entry -> values.add(entry.getValue()));
		return values;
	}
	
	public static void init()
	{
		ACCOMPLISHMENTS.forEach(acc -> Registry.register(REGISTRY, acc.registryName(), acc));
	}
}
