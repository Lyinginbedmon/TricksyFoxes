package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.network.SyncTreeScreenPacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.TreeScreenHandler;

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
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
	private static final TrackedData<OptionalInt> COLOR = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	protected BehaviourTree behaviourTree = new BehaviourTree();
	@SuppressWarnings("unchecked")
	protected Whiteboard.Local<EntityTricksyFox> boardLocal = (Local<EntityTricksyFox>)(new Local<EntityTricksyFox>(this)).build();
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(TYPE, 0);
		this.getDataTracker().startTracking(OWNER_UUID, Optional.empty());
		this.getDataTracker().startTracking(COLOR, OptionalInt.empty());
	}
	
	protected void initGoals()
	{
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("Color", NbtElement.INT_TYPE))
			getDataTracker().set(COLOR, OptionalInt.of(data.getInt("Color")));
		if(data.contains("MasterID", NbtElement.INT_ARRAY_TYPE))
			setMaster(data.getUuid("MasterID"));
		setVariant(Type.byName(data.getString("Type")));
		
		boardLocal.readFromNbt(data.getCompound("Whiteboard"));
//		setBehaviourTree(data.getCompound("BehaviourTree"));
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
		if(heldStack.getItem() == TFItems.SAGE_HAT)
		{
			if(!hasSage() || isSage(player))
			{
				setMaster(ItemSageHat.getMasterID(heldStack, player));
				player.sendMessage(Text.translatable("entity."+Reference.ModInfo.MOD_ID+".tricksy_fox.master_set", getDisplayName()), true);
				return ActionResult.success(isClient);
			}
			else
				player.sendMessage(Text.translatable("entity."+Reference.ModInfo.MOD_ID+".tricksy_fox.master_set.fail", getDisplayName()), true);
		}
		else if(isSage(player))
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
			else if(!player.isSneaking())
			{
				// FIXME Ensure sync of fox between client and server
				player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TreeScreenHandler(id, this), getDisplayName())).ifPresent(syncId -> SyncTreeScreenPacket.send(player, this, syncId));
				return ActionResult.success(isClient);
			}
		}
		
		return super.interactMob(player, hand);
	}
	
	public void tick()
	{
		super.tick();
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
	
	public Optional<UUID> getMaster() { return this.getDataTracker().get(OWNER_UUID); }
	
	public void setMaster(@Nullable UUID uuidIn) { this.getDataTracker().set(OWNER_UUID, Optional.of(uuidIn)); }
	
	public BehaviourTree getBehaviourTree()
	{
		return getWorld().isClient() ? BehaviourTree.create(getDataTracker().get(TREE_NBT)) : this.behaviourTree;
	}
	
	public Local<EntityTricksyFox> getLocalWhiteboard() { return this.boardLocal; }
	
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
