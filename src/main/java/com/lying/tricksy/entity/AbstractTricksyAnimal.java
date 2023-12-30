package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.TricksyLookControl;
import com.lying.tricksy.entity.ai.whiteboard.CommandWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.item.ITreeItem;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerWhiteboards;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public abstract class AbstractTricksyAnimal extends AnimalEntity implements ITricksyMob<AbstractTricksyAnimal>, Inventory
{
	public static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<NbtCompound> LOG_NBT = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<EntityPose> TREE_POSE = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.ENTITY_POSE);
	public static final TrackedData<Integer> BARK = DataTracker.registerData(AbstractTricksyAnimal.class, TrackedDataHandlerRegistry.INTEGER);
	
	public static final String TREE_KEY = "BehaviourTree";
	
	private BehaviourTree behaviourTree = new BehaviourTree(null);
	
	@SuppressWarnings("unchecked")
	protected LocalWhiteboard<AbstractTricksyAnimal> boardLocal = (LocalWhiteboard<AbstractTricksyAnimal>)(new LocalWhiteboard<AbstractTricksyAnimal>(this)).build();
	
	protected int barkTicks = 0;
	
	protected PlayerEntity customer = null;
	
	public AbstractTricksyAnimal(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(entityType, world);
		this.lookControl = new TricksyLookControl(this);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(OWNER_UUID, Optional.empty());
		this.getDataTracker().startTracking(COLOR, OptionalInt.empty());
		this.getDataTracker().startTracking(TREE_NBT, BehaviourTree.INITIAL_TREE.write(new NbtCompound()));
		this.getDataTracker().startTracking(LOG_NBT, new NbtCompound());
		this.getDataTracker().startTracking(TREE_POSE, EntityPose.STANDING);
		this.getDataTracker().startTracking(BARK, Bark.NONE.ordinal());
	}
	
	protected void initGoals() { }
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		if(data.contains("MasterID", NbtElement.INT_ARRAY_TYPE))
			setSage(data.getUuid("MasterID"));
		
		boardLocal.readFromNbt(data.getCompound("Whiteboard"));
		
		if(data.contains(TREE_KEY, NbtElement.COMPOUND_TYPE))
		{
			this.behaviourTree = BehaviourTree.create(data.getCompound(TREE_KEY));
			this.behaviourTree.syncWithForest(getWorld(), getUuid());
			if(!getWorld().isClient())
				this.getDataTracker().set(TREE_NBT, this.behaviourTree.storeTrees(new NbtCompound()));
		}
		
		setTreePose(data.getBoolean("IsSleeping") ? EntityPose.SLEEPING : EntityPose.STANDING);
		if(data.contains("Home", NbtElement.COMPOUND_TYPE))
			setPositionTarget(NbtHelper.toBlockPos(data.getCompound("Home")), 6);
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		getDataTracker().get(COLOR).ifPresent((val) -> data.putInt("Color", val));
		getDataTracker().get(OWNER_UUID).ifPresent((uuid) -> data.putUuid("MasterID", uuid));
		
		data.put("Whiteboard", boardLocal.writeToNbt(new NbtCompound()));
		
		behaviourTree.logInForest(getWorld(), getUuid());
		data.put(TREE_KEY, this.behaviourTree.storeCommand(new NbtCompound()));
		
		data.putBoolean("IsSleeping", isTreeSleeping());
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
			getDataTracker().set(BARK, Bark.NONE.ordinal());
	}
	
	public void tickMovement()
	{
		if(isTreeSleeping() || isImmobile())
		{
			this.jumping = false;
			this.sidewaysSpeed = 0F;
			this.forwardSpeed = 0F;
		}
		super.tickMovement();
	}
	
	/** Returns true if the behaviour tree is enforcing a sitting pose */
	public boolean isTreeSleeping() { return this.getDataTracker().get(TREE_POSE) == EntityPose.SITTING; }
	
	public void setTreePose(EntityPose pose)
	{
		getDataTracker().set(TREE_POSE, pose);
		setPose(pose);
	}
	
	public EntityPose getTreePose() { return this.getDataTracker().get(TREE_POSE); }
	
	public Optional<UUID> getSage() { return this.getDataTracker().get(OWNER_UUID); }
	
	public void setSage(@Nullable UUID uuidIn) { this.getDataTracker().set(OWNER_UUID, Optional.of(uuidIn)); }
	
	public BehaviourTree getBehaviourTree() { return getWorld().isClient() ? BehaviourTree.create(getDataTracker().get(TREE_NBT)) : this.behaviourTree; }
	
	public void setBehaviourTree(NbtCompound data)
	{
		behaviourTree.root().stop(this);
		getDataTracker().set(TREE_NBT, data);
		behaviourTree.setTrees(data);
		behaviourTree.logInForest(getWorld(), getUuid());
	}
	
	public void setLatestLog(NodeStatusLog logIn) { this.getDataTracker().set(LOG_NBT, logIn.writeToNbt(new NbtCompound())); }
	
	public NodeStatusLog getLatestLog() { return NodeStatusLog.fromNbt(this.getDataTracker().get(LOG_NBT)); }
	
	public void giveCommand(CommandWhiteboard command)
	{
		getBehaviourTree().giveCommand(command, this);
	}
	
	public LocalWhiteboard<AbstractTricksyAnimal> getLocalWhiteboard() { return this.boardLocal; }
	
	public GlobalWhiteboard getGlobalWhiteboard()
	{
		return hasSage() ? 
					ServerWhiteboards.getServerWhiteboards(getServer()).getWhiteboardFor(getSage().get()) : 
					new GlobalWhiteboard(getEntityWorld());
	}
	
	public int getColor() { return getDataTracker().get(COLOR).isPresent() ? getDataTracker().get(COLOR).getAsInt() : getDefaultColor(); }
	
	public abstract int getDefaultColor();
	
	public boolean hasCustomer() { return this.customer != null; }
	
	public void setCustomer(@Nullable PlayerEntity player) { this.customer = player; }
	
	public void bark(Bark bark)
	{
		if(bark == null)
			bark = Bark.NONE;
		
		getDataTracker().set(BARK, bark.ordinal());
		this.barkTicks = Reference.Values.TICKS_PER_SECOND * 3;
		playSoundForBark(bark);
	}
	
	public Bark currentBark() { return Bark.values()[getDataTracker().get(BARK) % Bark.values().length]; }
	
	public ItemStack getProjectileType(ItemStack stack) { return ITricksyMob.getRangedProjectile(stack, this); }
	
	public Inventory getMainInventory() { return this; }
	
	public boolean canPlayerUse(PlayerEntity player) { return isSage(player) && player.distanceTo(this) < 4; }
	
	public ItemStack getStack(int slot) { return getEquippedStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot)); }
	
	public void setStack(int slot, ItemStack stack) { equipStack(ITricksyMob.INDEX_TO_SLOT_MAP.get(slot), stack); }
}
