package com.lying.tricksy.entity.ai.whiteboard;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WhiteboardObjEntity extends WhiteboardObjBase<Entity, com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity.EntityData, NbtCompound>
{
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
	
	protected EntityData storeValue(Entity val) { return val == null ? new EntityData() : new EntityData(val); }
	
	protected Entity getValue(WhiteboardObjEntity.EntityData entry) { return entry.blank ? null : entry.value; }
	
	protected NbtCompound valueToNbt(WhiteboardObjEntity.EntityData val) { return val.storeToNbt(new NbtCompound()); }
	
	protected EntityData valueFromNbt(NbtCompound nbt)
	{
		if(!nbt.contains("UUID", NbtElement.INT_ARRAY_TYPE))
			return new EntityData();
		
		WhiteboardObjEntity.EntityData data = new EntityData(nbt.getUuid("UUID"), nbt.getBoolean("IsPlayer"));
		data.lastKnownPos = NbtHelper.toBlockPos(nbt.getCompound("LastKnownPos"));
		if(nbt.contains("EntityName", NbtElement.STRING_TYPE))
		{
			String string = nbt.getString("EntityName");
			try
			{
				data.valueName = Text.Serializer.fromJson(string);
			}
			catch(Exception e)
			{
				TricksyFoxes.LOGGER.warn("Failed to parse entity name {}", (Object)string, (Object)e);
			}
		}
		return data;
	}
	
	public static class EntityData
	{
		private Entity value = null;
		private Text valueName = null;
		private BlockPos lastKnownPos;
		
		private final UUID uuid;
		private final boolean isPlayer;
		private final boolean blank;
		
		public EntityData()
		{
			blank = true;
			uuid = UUID.randomUUID();
			isPlayer = false;
		}
		
		public EntityData(UUID uuidIn, boolean isPlayerIn)
		{
			this.blank = false;
			this.uuid = uuidIn;
			this.isPlayer = isPlayerIn;
		}
		
		public EntityData(@NotNull Entity entIn)
		{
			this(entIn.getUuid(), entIn.getType() == EntityType.PLAYER);
			this.value = entIn;
			storeName(value.getName());
			this.lastKnownPos = entIn.getBlockPos();
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			if(blank)
				return data;
			
			data.putUuid("UUID", this.uuid);
			data.putBoolean("IsPlayer", this.isPlayer);
			data.put("LastKnownPos", NbtHelper.fromBlockPos(this.lastKnownPos));
			if(valueName != null)
				data.putString("EntityName", Text.Serializer.toJson(valueName));
			
			return data;
		}
		
		public void recache(World world)
		{
			if(blank)
				return;
			
			/*
			 * If this object represents a player, search the playerlist for them.
			 * Otherwise, search around the last known position of the entity for one with a matching UUID
			 */
			if(isPlayer)
				value = world.getPlayerByUuid(this.uuid);
			else
			{
				Box searchArea = Box.from(new Vec3d(0.5D, 0.5D, 0.5D).add(this.lastKnownPos.getX(), this.lastKnownPos.getY(), this.lastKnownPos.getZ())).expand(16D);
				if(searchArea.minY < world.getBottomY())
					searchArea = searchArea.withMinY(world.getBottomY());
				
				for(Entity ent : world.getEntitiesByClass(Entity.class, searchArea, (entity) -> entity.getUuid().equals(this.uuid)))
				{
					value = ent;
					break;
				}
			}
			
			if(value != null)
			{
				storeName(value.getName());
				if(!isPlayer)
					this.lastKnownPos = value.getBlockPos();
			}
		}
		
		private void storeName(Text nameIn)
		{
			this.valueName = nameIn.copyContentOnly();
		}
	}
}