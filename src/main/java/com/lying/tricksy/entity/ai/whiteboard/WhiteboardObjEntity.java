package com.lying.tricksy.entity.ai.whiteboard;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WhiteboardObjEntity extends WhiteboardObjBase<Entity, com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity.EntityData>
{
	private static final Box SEARCH_AREA = Box.of(Vec3d.ZERO, 16, 16, 16);
	
	public WhiteboardObjEntity()
	{
		super(TFObjType.ENT, NbtElement.COMPOUND_TYPE);
	}
	
	public WhiteboardObjEntity(Entity entityIn)
	{
		this();
		value.clear();
		value.add(storeValue(entityIn));
	}
	
	public void recache(WhiteboardObjEntity.EntityData val, World world)
	{
		val.recache(world);
	}
	
	public Text describeValue(WhiteboardObjEntity.EntityData value)
	{
		return value.valueName != null ? value.valueName : Text.translatable("value."+Reference.ModInfo.MOD_ID+".entity");
	}
	
	protected EntityData storeValue(Entity val) { return new EntityData(val); }
	
	protected Entity getValue(WhiteboardObjEntity.EntityData entry) { return entry.value; }
	
	protected NbtElement valueToNbt(WhiteboardObjEntity.EntityData val) { return val.storeToNbt(new NbtCompound()); }
	
	protected EntityData valueFromNbt(NbtElement nbt)
	{
		NbtCompound compound = (NbtCompound)nbt;
		WhiteboardObjEntity.EntityData data = new EntityData(compound.getUuid("UUID"), compound.getBoolean("IsPlayer"));
		data.lastKnownPos = NbtHelper.toBlockPos(compound.getCompound("LastKnownPos"));
		if(compound.contains("EntityName", NbtElement.STRING_TYPE))
		{
			String string = compound.getString("EntityName");
			try
			{
				data.valueName = Text.Serializer.fromJson(string);
			}
			catch(Exception e)
			{
				TricksyFoxes.LOGGER.warn("Failed to parse entity custom name {}", (Object)string, (Object)e);
			}
		}
		return data;
	}
	
	public static class EntityData
	{
		private Entity value = null;
		private Text valueName = null;
		
		private final UUID uuid;
		private final boolean isPlayer;
		private BlockPos lastKnownPos;
		
		public EntityData(@NotNull Entity entIn)
		{
			this(entIn.getUuid(), entIn.getType() == EntityType.PLAYER);
			this.value = entIn;
			this.valueName = entIn.getDisplayName();
			this.lastKnownPos = entIn.getBlockPos();
		}
		
		public EntityData(UUID uuidIn, boolean isPlayerIn)
		{
			this.uuid = uuidIn;
			this.isPlayer = isPlayerIn;
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			data.putUuid("UUID", this.uuid);
			data.putBoolean("IsPlayer", this.isPlayer);
			data.put("LastKnownPos", NbtHelper.fromBlockPos(this.lastKnownPos));
			if(valueName != null)
				data.putString("EntityName", Text.Serializer.toJson(valueName));
			
			return data;
		}
		
		public void recache(World world)
		{
			if(value != null)
			{
				this.valueName = value.getDisplayName();
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
			
			if(value != null)
				this.valueName = value.getDisplayName();
		}
	}
}