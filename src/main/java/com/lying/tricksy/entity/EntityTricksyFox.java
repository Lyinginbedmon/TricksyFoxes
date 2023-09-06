package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.Whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FoxEntity.Type;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityTricksyFox extends AnimalEntity implements ITricksyMob, VariantHolder<Type>
{
	private static final TrackedData<Integer> TYPE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<NbtCompound> WHITEBOARD_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	protected BehaviourTree behaviourTree = new BehaviourTree();
	protected LocalWhiteboard whiteboardLocal = new LocalWhiteboard();
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(TYPE, 0);
		this.getDataTracker().startTracking(OWNER_UUID, Optional.empty());
	}
	
	protected void initGoals()
	{
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("MasterID"))
			setMaster(data.getUuid("MasterID"));
		setVariant(Type.byName(data.getString("Type")));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		if(hasMaster())
			data.putUuid("MasterID", getMaster().get());
		data.putString("Type", this.getVariant().asString());
	}
	
	@SuppressWarnings("resource")
	public ActionResult interactMob(PlayerEntity player, Hand hand)
	{
		boolean isClient = getEntityWorld().isClient;
		ItemStack heldStack = player.getStackInHand(hand);
		if(heldStack.getItem() == TFItems.SAGE_HAT && !hasMaster())
		{
			setMaster(ItemSageHat.getMasterID(heldStack, player));
			return ActionResult.success(isClient);
		}
		
		return super.interactMob(player, hand);
	}
	
	@Nullable
	public PassiveEntity createChild(ServerWorld arg0, PassiveEntity arg1)
	{
		/**
		 * Tricksy mobs never create tricksy offspring, only their nascent form.
		 * Everyone has to find enlightenment on their own.
		 */
		if(arg1.getType() == getType())
		{
			FoxEntity child = EntityType.FOX.create(arg0);
			child.setVariant(this.random.nextBoolean() ? this.getVariant() : ((FoxEntity)arg1).getVariant());
			return child;
		}
		
		return null;
	}
	
	public Optional<UUID> getMaster() { return this.getDataTracker().get(OWNER_UUID); }
	
	public void setMaster(@Nullable UUID uuidIn) { this.getDataTracker().set(OWNER_UUID, Optional.of(uuidIn)); }
	
	public BehaviourTree getBehaviourTree() { return this.behaviourTree; }
	
	public LocalWhiteboard getLocalWhiteboard() { return this.whiteboardLocal; }
	
	public void setBehaviourTree(NbtCompound data)
	{
		// TODO Implement behaviour tree generation from NBT data
		getDataTracker().set(TREE_NBT, data);
	}
	
	public void setVariant(Type type) { this.dataTracker.set(TYPE, type.getId()); }
	
	public Type getVariant() { return Type.fromId(this.dataTracker.get(TYPE)); }
	
	@Nullable
	protected SoundEvent getAmbientSound()
	{
//		List<Entity> list;
//		if (this.isSleeping()) {
//			return SoundEvents.ENTITY_FOX_SLEEP;
//		}
//		if (!this.getWorld().isDay() && this.random.nextFloat() < 0.1f && (list = this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR)).isEmpty()) {
//			return SoundEvents.ENTITY_FOX_SCREECH;
//		}
		return SoundEvents.ENTITY_FOX_AMBIENT;
	}
	
	@Nullable
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_FOX_HURT; }
	
	@Nullable
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_FOX_DEATH; }
}
