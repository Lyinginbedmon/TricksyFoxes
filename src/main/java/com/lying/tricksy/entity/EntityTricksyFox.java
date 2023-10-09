package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.TricksyLookAroundGoal;
import com.lying.tricksy.entity.ai.TricksyLookControl;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.item.ITreeItem;
import com.lying.tricksy.network.SyncTreeScreenPacket;
import com.lying.tricksy.screen.TreeScreenHandler;
import com.lying.tricksy.utility.ServerWhiteboards;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
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
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityTricksyFox extends AnimalEntity implements ITricksyMob<EntityTricksyFox>, VariantHolder<Type>
{
	private static final TrackedData<Integer> TYPE = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> SLEEPING = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	private static final TrackedData<Integer> USERS = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Text> LOG = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);
	
	protected BehaviourTree behaviourTree = new BehaviourTree();
	@SuppressWarnings("unchecked")
	protected LocalWhiteboard<EntityTricksyFox> boardLocal = (LocalWhiteboard<EntityTricksyFox>)(new LocalWhiteboard<EntityTricksyFox>(this)).build();
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
		this.lookControl = new TricksyLookControl(this);
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
		this.getDataTracker().startTracking(USERS, 0);
	}
	
	protected void initGoals()
	{
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(8, new TricksyLookAroundGoal(this));
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
		
		boardLocal.readFromNbt(data.getCompound("Whiteboard"));
		setBehaviourTree(data.getCompound("BehaviourTree"));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		getDataTracker().get(COLOR).ifPresent((val) -> data.putInt("Color", val));
		getDataTracker().get(OWNER_UUID).ifPresent((uuid) -> data.putUuid("MasterID", uuid));
		data.putString("Type", this.getVariant().asString());
		
		data.put("Whiteboard", boardLocal.writeToNbt(new NbtCompound()));
		data.put("BehaviourTree", this.behaviourTree.storeInNbt());
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
			else if(!player.isSneaking() && activeUsers() == 0)
			{
				if(!player.getWorld().isClient())
					addUser();
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TreeScreenHandler(id, this), getDisplayName())).ifPresent(syncId -> SyncTreeScreenPacket.send(player, this, syncId));
				return ActionResult.success(isClient);
			}
		}
		
		return super.interactMob(player, hand);
	}
	
	public void tick()
	{
		super.tick();
		if(activeUsers() <= 0)
			ITricksyMob.updateBehaviourTree(this);
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
	
	public int activeUsers() { return this.getDataTracker().get(USERS).intValue(); }
	
	public void addUser() { this.getDataTracker().set(USERS, activeUsers() + 1); }
	
	public void removeUser() { this.getDataTracker().set(USERS, Math.max(0, activeUsers() - 1)); }
	
	public void logStatus(Text message)
	{
		this.getDataTracker().set(LOG, message);
//		System.out.println("Logged: "+message.getString());	// FIXME Remove this before publishing
	}
	
	public Text latestLog() { return this.getDataTracker().get(LOG); }
	
	public void bark(Bark bark)
	{
		playAmbientSound();
	}
	
	public Bark currentBark() { return Bark.NONE; }
}
