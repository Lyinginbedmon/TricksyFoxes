package com.lying.tricksy.entity.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A polymorphic data object for use with whiteboards
 * @author Lying
 */
public abstract class WhiteboardObject
{
	private static final Map<ObjectType, ObjectBuilder> REGISTRY = new HashMap<>();
	
	/** Generic empty value, representing null */
	public static final WhiteboardObject EMPTY = new WhiteboardObject(null) 
	{
		public boolean isEmpty() { return true; }
		
		public NbtCompound storeToNbt(NbtCompound data) { return data; }
		
		public void readFromNbt(NbtCompound data) { }
	};
	
	@Nullable
	private final ObjectType type;
	
	protected WhiteboardObject(ObjectType typeIn)
	{
		this.type = typeIn;
	}
	
	public ObjectType type() { return this.type; }
	
	/** Returns true if this object holds no value */
	public boolean isEmpty() { return false; }
	
	/** Attempts to recache this object, usually to refresh an entity reference */
	public <T extends LivingEntity & ITricksyMob> void recacheIfNecessary(T tricksy, World world) { }
	
	public static WhiteboardObject createFromNbt(NbtCompound data)
	{
		ObjectType type = ObjectType.fromString(data.getString("Type"));
		if(type != null)
		{
			WhiteboardObject obj = REGISTRY.get(type).create();
			obj.readFromNbt(data.getCompound("Data"));
			return obj;
		}
		return EMPTY;
	}
	
	public NbtCompound writeToNbt(NbtCompound data)
	{
		if(this.type != null)
			data.putString("Type", this.type.asString());
		data.put("Data", storeToNbt(new NbtCompound()));
		return data;
	}
	
	/** Stores class-specific information about this object in NBT */
	protected abstract NbtCompound storeToNbt(NbtCompound data);
	
	/** Loads class-specific information about this object from NBT */
	protected abstract void readFromNbt(NbtCompound data);
	
	@Nullable
	public Entity asEntity() { return null; }
	@Nullable
	public BlockPos asBlockPos() { return null; }
	public int asInt() { return 0; }
	@Nullable
	public Direction asDirection() { return null; }
	@Nullable
	public ItemStack asItem() { return null; }
	
	protected static void register(ObjectType type, ObjectBuilder builder)
	{
		REGISTRY.put(type, builder);
	}
	
	static
	{
		register(ObjectType.INT, IntegerObject::create);
		register(ObjectType.BLOCK, BlockPosObject::create);
		register(ObjectType.DIR, DirectionObject::create);
		register(ObjectType.ENT, EntityObject::create);
		register(ObjectType.ITEM, ItemObject::create);
	}
	
	private static enum ObjectType implements StringIdentifiable
	{
		INT,
		BLOCK,
		DIR,
		ENT,
		ITEM;
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static ObjectType fromString(String nameIn)
		{
			for(ObjectType type : values())
				if(nameIn.equals(type.asString()))
					return type;
			return null;
		}
	}
	
	public static class IntegerObject extends WhiteboardObject
	{
		private int value = Integer.MIN_VALUE;
		
		public IntegerObject(int intIn)
		{
			super(ObjectType.INT);
			this.value = intIn;
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			data.putInt("Value", this.value);
			return data;
		}
		
		protected void readFromNbt(NbtCompound data)
		{
			this.value = data.getInt("Value");
		}
		
		public int asInt() { return this.value; }
		
		public boolean isEmpty() { return this.value == Integer.MIN_VALUE; }
		
		public static WhiteboardObject create()
		{
			return new IntegerObject(Integer.MIN_VALUE);
		}
	}
	
	public static class BlockPosObject extends WhiteboardObject
	{
		private BlockPos value = BlockPos.ORIGIN;
		
		public BlockPosObject(BlockPos posIn)
		{
			super(ObjectType.BLOCK);
			this.value = posIn;
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			data.put("Value", NbtHelper.fromBlockPos(this.value));
			return data;
		}
		
		protected void readFromNbt(NbtCompound data)
		{
			this.value = NbtHelper.toBlockPos(data.getCompound("Value"));
		}
		
		public BlockPos asBlockPos() { return this.value; }
		
		public static WhiteboardObject create()
		{
			return new BlockPosObject(BlockPos.ORIGIN);
		}
	}
	
	public static class DirectionObject extends WhiteboardObject
	{
		private Direction value = Direction.UP;
		
		public DirectionObject(Direction faceIn)
		{
			super(ObjectType.DIR);
			this.value = faceIn;
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			data.putString("Value", this.value.getName());
			return data;
		}
		
		protected void readFromNbt(NbtCompound data)
		{
			this.value = Direction.byName(data.getString("Value"));
		}
		
		public Direction asDirection() { return this.value; }
		
		public static WhiteboardObject create()
		{
			return new DirectionObject(Direction.UP);
		}
	}
	
	public static class EntityObject extends WhiteboardObject
	{
		private static final Box SEARCH_AREA = Box.of(Vec3d.ZERO, 16, 16, 16);
		
		private Entity value;
		
		private UUID uuid;
		private BlockPos lastKnownPos;
		private boolean isPlayer;
		
		private EntityObject()
		{
			super(ObjectType.ENT);
		}
		
		public EntityObject(@NotNull Entity entIn)
		{
			this();
			this.value = entIn;
			this.uuid = entIn.getUuid();
			this.lastKnownPos = entIn.getBlockPos();
			this.isPlayer = entIn.getType() == EntityType.PLAYER;
		}
		
		public EntityObject(UUID uuid)
		{
			this();
			this.uuid = uuid;
			this.isPlayer = true;
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			data.putUuid("UUID", this.uuid);
			
			if(isPlayer)
				data.putBoolean("IsPlayer", this.isPlayer);
			else
				data.put("LastKnownPos", NbtHelper.fromBlockPos(this.lastKnownPos));
			
			return data;
		}
		
		protected void readFromNbt(NbtCompound data)
		{
			this.uuid = data.getUuid("UUID");
			
			if(data.contains("LastKnownPos"))
				this.lastKnownPos = NbtHelper.toBlockPos(data.getCompound("LastKnownPos"));
			
			if(data.contains("IsPlayer"))
				this.isPlayer = data.getBoolean("IsPlayer");
		}
		
		public <T extends LivingEntity & ITricksyMob> void recacheIfNecessary(T tricksy, World world)
		{
			if(value != null)
			{
				if(!isPlayer)
					this.lastKnownPos = this.value.getBlockPos();
				return;
			}
			
			/*
			 * If this object represents a player, search the playerlist for them.
			 * Otherwise, search around the last known position of the entity for one with a matching UUID
			 */
			if(isPlayer)
			{
				PlayerEntity player = world.getPlayerByUuid(this.uuid);
				if(player != null)
					this.value = player;
			}
			else
				for(Entity ent : world.getEntitiesByClass(Entity.class, SEARCH_AREA.offset(this.lastKnownPos), (entity) -> entity.getUuid().equals(this.uuid)))
				{
					this.value = ent;
					break;
				}
		}
		
		public boolean isEmpty() { return this.value != null || this.value.isAlive() && !this.value.isSpectator(); }
		
		public Entity asEntity() { return this.value; }
		
		public static WhiteboardObject create()
		{
			return new EntityObject();
		}
	}
	
	/**
	 * ItemStack object stored in a whiteboard.<br>
	 * This object deliberately does not store its value in NBT.
	 * @author Lying
	 */
	public static class ItemObject extends WhiteboardObject
	{
		private ItemStack stack = ItemStack.EMPTY;
		
		public ItemObject(ItemStack stackIn)
		{
			super(ObjectType.ITEM);
			this.stack = stackIn.copy();
		}
		
		protected NbtCompound storeToNbt(NbtCompound data) { return data; }
		
		protected void readFromNbt(NbtCompound data) { }
		
		public boolean isEmpty() { return this.stack == null || stack.isEmpty(); }
		
		public ItemStack asItem() { return this.stack; }
		
		public static WhiteboardObject create()
		{
			return new ItemObject(ItemStack.EMPTY);
		}
	}
}
