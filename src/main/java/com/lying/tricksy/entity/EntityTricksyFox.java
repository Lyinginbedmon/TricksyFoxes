package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.TricksyLookControl;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.item.ITreeItem;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerWhiteboards;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FoxEntity.Type;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityTricksyFox extends AnimalEntity implements ITricksyMob<EntityTricksyFox>, VariantHolder<Type>, Inventory
{
	private static final TrackedData<Integer> TYPE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> SLEEPING = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<Text> LOG = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);
	private static final TrackedData<Integer> BARK = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	
	protected BehaviourTree behaviourTree = new BehaviourTree();
	@SuppressWarnings("unchecked")
	protected LocalWhiteboard<EntityTricksyFox> boardLocal = (LocalWhiteboard<EntityTricksyFox>)(new LocalWhiteboard<EntityTricksyFox>(this)).build();
	
	private int barkTicks = 0;
	
	private PlayerEntity customer = null;
//	private SimpleInventory inventory;
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
		this.lookControl = new TricksyLookControl(this);
//		this.inventory = ITricksyMob.createInventory();
//		this.inventory.addListener(this);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(TYPE, 0);
		this.getDataTracker().startTracking(SLEEPING, false);
		this.getDataTracker().startTracking(OWNER_UUID, Optional.empty());
		this.getDataTracker().startTracking(COLOR, OptionalInt.empty());
		this.getDataTracker().startTracking(TREE_NBT, BehaviourTree.INITIAL_TREE.write(new NbtCompound()));
		this.getDataTracker().startTracking(LOG, Text.empty());
		this.getDataTracker().startTracking(BARK, 0);
	}
	
	protected void initGoals()
	{
//		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
//		this.goalSelector.add(8, new LookAroundGoal(this));
	}
	
	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return FoxEntity.createFoxAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2D);
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		if(data.contains("MasterID", NbtElement.INT_ARRAY_TYPE))
			setSage(data.getUuid("MasterID"));
		setVariant(Type.byName(data.getString("Type")));
//		readInventory(data);
//		updateEquippedItems();
		
		boardLocal.readFromNbt(data.getCompound("Whiteboard"));
		setBehaviourTree(data.getCompound("BehaviourTree"));
		if(data.contains("Home", NbtElement.COMPOUND_TYPE))
			setPositionTarget(NbtHelper.toBlockPos(data.getCompound("Home")), 6);
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		getDataTracker().get(COLOR).ifPresent((val) -> data.putInt("Color", val));
		getDataTracker().get(OWNER_UUID).ifPresent((uuid) -> data.putUuid("MasterID", uuid));
		data.putString("Type", this.getVariant().asString());
//		writeInventory(data);
		
		data.put("Whiteboard", boardLocal.writeToNbt(new NbtCompound()));
		data.put("BehaviourTree", this.behaviourTree.storeInNbt());
		if(hasPositionTarget())
			data.put("Home", NbtHelper.fromBlockPos(getPositionTarget()));
	}
	
	public boolean isPersistent() { return true; }
	
	@SuppressWarnings("resource")
	public ActionResult interactMob(PlayerEntity player, Hand hand)
	{
		boolean isClient = player.getWorld().isClient;
		ItemStack heldStack = player.getStackInHand(hand);
		if(!hasSage() || isSage(player))
		{
			if(heldStack.getItem() instanceof DyeItem)
			{
				if(!isClient)
				{
					float[] comp = ((DyeItem)heldStack.getItem()).getColor().getColorComponents();
					int r = (int)(comp[0] * 255);
					int g = (int)(comp[1] * 255);
					int b = (int)(comp[2] * 255);
					
					// Recompose original decimal value of the dye colour from derived RGB values
					int col = r;
					col = (col << 8) + g;
					col = (col << 8) + b;
					
					getDataTracker().set(COLOR, OptionalInt.of(col));
					
					if(!player.isCreative())
						heldStack.decrement(1);
				}
				return ActionResult.success(isClient);
			}
			else if(heldStack.getItem() == Items.WET_SPONGE)
			{
				if(!isClient)
					getDataTracker().set(COLOR, OptionalInt.empty());
				return ActionResult.success(isClient);
			}
			else if(heldStack.getItem() instanceof ITreeItem)
				return ((ITreeItem)heldStack.getItem()).useOnTricksy(heldStack, this, player);
			else if(!player.isSneaking() && !hasCustomer() && isSage(player))
			{
				if(player instanceof ServerPlayerEntity)
					setCustomer(player);
				
				ITricksyMob.openInventoryScreen(player, this);
				return ActionResult.success(isClient);
			}
		}
		
		return super.interactMob(player, hand);
	}
	
	public void tick()
	{
		super.tick();
		if(!hasCustomer() && !isAiDisabled())
			ITricksyMob.updateBehaviourTree(this);
		
		if(this.barkTicks > 0 && --this.barkTicks == 0)
				getDataTracker().set(BARK, 0);
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
	
	public boolean isSleeping() { return this.getDataTracker().get(SLEEPING).booleanValue(); }
	
	public void setSleeping(boolean var)
	{
		this.getDataTracker().set(SLEEPING, var);
		if(var)
			setPose(EntityPose.SITTING);
		else
			setPose(EntityPose.STANDING);
	}
	
	public EntityDimensions getDimensions(EntityPose pose)
	{
		switch(pose)
		{
			case SITTING:	return EntityDimensions.fixed(super.getDimensions(pose).width, 0.7F);
			default:
				return super.getDimensions(pose);
		}
	}
	
	public void tickMovement()
	{
		if(isSleeping() || isImmobile())
		{
			this.jumping = false;
			this.sidewaysSpeed = 0F;
			this.forwardSpeed = 0F;
		}
		super.tickMovement();
	}
	
	public Optional<UUID> getSage() { return this.getDataTracker().get(OWNER_UUID); }
	
	public void setSage(@Nullable UUID uuidIn) { this.getDataTracker().set(OWNER_UUID, Optional.of(uuidIn)); }
	
	public BehaviourTree getBehaviourTree()
	{
		return getWorld().isClient() ? BehaviourTree.create(getDataTracker().get(TREE_NBT)) : this.behaviourTree;
	}
	
	public LocalWhiteboard<EntityTricksyFox> getLocalWhiteboard() { return this.boardLocal; }
	
	public GlobalWhiteboard getGlobalWhiteboard()
	{
		return hasSage() ? 
					ServerWhiteboards.getServerWhiteboards(getServer()).getWhiteboardFor(getSage().get()) : 
					new GlobalWhiteboard(getEntityWorld());
	}
	
	public void setBehaviourTree(NbtCompound data)
	{
		behaviourTree.root().stop(this);
		getDataTracker().set(TREE_NBT, data);
		behaviourTree = BehaviourTree.create(data);
	}
	
	public void setVariant(Type type) { this.dataTracker.set(TYPE, type.getId()); }
	
	public Type getVariant() { return Type.fromId(this.dataTracker.get(TYPE)); }
	
	public int getColor() { return getDataTracker().get(COLOR).isPresent() ? getDataTracker().get(COLOR).getAsInt() : 12779520; }
	
	@Nullable
	protected SoundEvent getAmbientSound()
	{
		if(this.isSleeping())
			return SoundEvents.ENTITY_FOX_SLEEP;
		if(!this.getWorld().isDay() && this.random.nextFloat() < 0.1f && getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR).isEmpty())
			return SoundEvents.ENTITY_FOX_SCREECH;
		return SoundEvents.ENTITY_FOX_AMBIENT;
	}
	
	@Nullable
	protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_FOX_HURT; }
	
	@Nullable
	protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_FOX_DEATH; }
	
	public boolean hasCustomer() { return this.customer != null; }
	
	public void setCustomer(@Nullable PlayerEntity player) { this.customer = player; }
	
	public void logStatus(Text message)
	{
		this.getDataTracker().set(LOG, message);
	}
	
	public Text latestLog() { return this.getDataTracker().get(LOG); }
	
	public void bark(Bark bark)
	{
		if(bark == null)
			bark = Bark.NONE;
		
		switch(bark)
		{
			case HAPPY:
				this.playSound(SoundEvents.ENTITY_FOX_AMBIENT, 1F, 1F);
				break;
			case CURIOUS:
				this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1F, 1F);
				break;
			case CONFUSED:
				this.playSound(SoundEvents.ENTITY_FOX_SCREECH, 2F, 1F);
				break;
			case ALERT:
				this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 5F, 1F);
				break;
			case NONE:
			default:
				break;
		}
		getDataTracker().set(BARK, bark.ordinal());
		this.barkTicks = Reference.Values.TICKS_PER_SECOND * 3;
	}
	
	public Bark currentBark()
	{
		int index = getDataTracker().get(BARK).intValue();
		return Bark.values()[index % Bark.values().length];
	}
	
	public ItemStack getProjectileType(ItemStack stack) { return ITricksyMob.getRangedProjectile(stack, this); }
	
	public Inventory getMainInventory() { return this; }
	
	public boolean canPlayerUse(PlayerEntity player) { return isSage(player) && player.distanceTo(this) < 4; }
	
	public ItemStack getStack(int slot) { return getEquippedStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot)); }
	
	public void setStack(int slot, ItemStack stack) { equipStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot), stack); }
}
