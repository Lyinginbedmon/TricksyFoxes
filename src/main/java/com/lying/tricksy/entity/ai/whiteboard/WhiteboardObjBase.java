package com.lying.tricksy.entity.ai.whiteboard;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A whiteboard object which stores its values in a form different from how they are retreived */
public abstract class WhiteboardObjBase<T, N> implements IWhiteboardObject<T>
{
	/** Generic empty value, representing null in most contexts */
	public static final WhiteboardObj<Object> EMPTY = new WhiteboardObj<Object>(TFObjType.EMPTY, NbtElement.STRING_TYPE, null)
	{
		protected NbtElement valueToNbt(Object val){ return NbtString.of(""); }
		protected Object valueFromNbt(NbtElement nbt) { return null; }
	};
	
	private final TFObjType<T> objType;
	protected List<N> value = Lists.newArrayList();
	
	private final byte nbtType;
	
	protected WhiteboardObjBase(TFObjType<T> typeIn, byte storageType, T initialValue)
	{
		this.objType = typeIn;
		this.nbtType = storageType;
		this.value.add(storeValue(initialValue));
	}
	
	public final TFObjType<T> type() { return this.objType; }
	
	protected abstract N storeValue(T val);
	
	protected abstract T getValue(N entry);
	
	protected abstract NbtElement valueToNbt(N val);
	
	protected abstract N valueFromNbt(NbtElement nbt);
	
	/** Returns the top-most value of this object */
	public T get() { return value.isEmpty() ? null : getValue(value.get(0)); }
	
	public final void recacheIfNecessary(World world)
	{
		value.forEach((val) -> recache(val, world));
	}
	
	protected void recache(N value, World world) { }
	
	public final void set(T val)
	{
		this.value.clear();
		this.value.add(storeValue(val));
	}
	
	public final void add(T val)
	{
		this.value.add(storeValue(val));
	}
	
	protected final void addToStorage(N entry) { this.value.add(entry); }
	
	/** Moves the top value of this list to the bottom */
	public void cycle()
	{
		if(!isList())
			return;
		
		N first = value.remove(0);
		value.add(first);
	}
	
	/** Returns true if this object contains more than one value */
	public boolean isList() { return this.value.size() > 1; }
	
	public static IWhiteboardObject<?> createFromNbt(NbtCompound data)
	{
		TFObjType<?> type = TFObjType.getType(new Identifier(data.getString("Type")));
		if(type != null)
			return type.create(data);
		return EMPTY;
	}
	
	public final NbtCompound writeToNbt(NbtCompound data)
	{
		data.putString("Type", this.objType.registryName().toString());
		
		if(!value.isEmpty())
		{
			NbtList values = new NbtList();
			for(N val : value)
				values.add(valueToNbt(val));
			data.put("Data", values);
		}
		
		return data;
	}
	
	public final void readFromNbt(NbtCompound nbt)
	{
		value.clear();
		if(nbt.contains("Data", NbtElement.LIST_TYPE))
		{
			NbtList values = nbt.getList("Data", nbtType);
			for(int i=0; i<values.size(); i++)
				addToStorage(valueFromNbt(values.get(i)));
		}
	}
	
	public static class Ent extends WhiteboardObjBase<Entity, com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBase.Ent.EntData>
	{
		private static final Box SEARCH_AREA = Box.of(Vec3d.ZERO, 16, 16, 16);
		
		public Ent(@NotNull Entity entIn)
		{
			super(TFObjType.ENT, NbtElement.COMPOUND_TYPE, entIn);
		}
		
		public Ent(UUID uuid)
		{
			this((Entity)null);
			
		}
		
		public void recache(EntData val, World world)
		{
			val.recache(world);
		}
		
		private static class EntData
		{
			private Entity value;
			
			private final UUID uuid;
			private BlockPos lastKnownPos;
			private final boolean isPlayer;
			
			public EntData(@NotNull Entity entIn)
			{
				this(entIn.getUuid(), entIn.getType() == EntityType.PLAYER);
				this.lastKnownPos = entIn.getBlockPos();
			}
			
			public EntData(UUID uuidIn, boolean isPlayerIn)
			{
				this.uuid = uuidIn;
				this.isPlayer = isPlayerIn;
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
			
			public void recache(World world)
			{
				if(value != null)
				{
					if(!isPlayer)
						this.lastKnownPos = value.getBlockPos();
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
						value = player;
				}
				else
					for(Entity ent : world.getEntitiesByClass(Entity.class, SEARCH_AREA.offset(this.lastKnownPos), (entity) -> entity.getUuid().equals(this.uuid)))
					{
						value = ent;
						break;
					}
			}
		}
		
		protected EntData storeValue(Entity val) { return new EntData(val); }
		
		protected Entity getValue(EntData entry) { return entry.value; }
		
		protected NbtElement valueToNbt(EntData val) { return val.storeToNbt(new NbtCompound()); }
		
		protected EntData valueFromNbt(NbtElement nbt)
		{
			NbtCompound compound = (NbtCompound)nbt;
			
			UUID uuid = compound.getUuid("UUID");
			boolean player = compound.getBoolean("IsPlayer");
			EntData data = new EntData(uuid, player);
			if(compound.contains("LastKnownPos"))
				data.lastKnownPos = NbtHelper.toBlockPos(compound.getCompound("LastKnownPos"));
			return data;
		}
	}
	
	public static class Block extends WhiteboardObjBase<BlockPos, com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBase.Block.BlockData>
	{
		public Block(@NotNull BlockPos pos)
		{
			super(TFObjType.BLOCK, NbtElement.COMPOUND_TYPE, pos);
		}
		
		private static class BlockData
		{
			private BlockPos pos;
			private Direction face;
			
			public BlockData(@NotNull BlockPos pos)
			{
				this(pos, Direction.UP);
			}
			
			public BlockData(BlockPos posIn, Direction faceIn)
			{
				this.pos = posIn;
				this.face = faceIn;
			}
			
			protected NbtCompound storeToNbt(NbtCompound data)
			{
				data.putString("Face", face.asString());
				data.put("Pos", NbtHelper.fromBlockPos(pos));
				return data;
			}
			
			public Direction blockFace() { return this.face; }
		}
		
		public Direction direction() { return value.isEmpty() ? Direction.UP : value.get(0).blockFace(); }
		
		protected BlockData storeValue(BlockPos val) { return new BlockData(val); }
		
		protected BlockPos getValue(BlockData entry) { return entry.pos; }
		
		protected NbtElement valueToNbt(BlockData val) { return val.storeToNbt(new NbtCompound()); }
		
		protected BlockData valueFromNbt(NbtElement nbt)
		{
			NbtCompound compound = (NbtCompound)nbt;
			BlockPos pos = NbtHelper.toBlockPos(compound.getCompound("Pos"));
			Direction face = Direction.byName(compound.getString("Face"));
			BlockData data = new BlockData(pos, face);
			return data;
		}
	}
}
