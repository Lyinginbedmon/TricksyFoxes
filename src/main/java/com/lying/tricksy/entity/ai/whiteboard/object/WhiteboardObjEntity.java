package com.lying.tricksy.entity.ai.whiteboard.object;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WhiteboardObjEntity extends WhiteboardObjBase<Entity, com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity.EntityData, NbtCompound>
{
	private boolean isFilterList = false;
	
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
	
	public List<Text> describe()
	{
		List<Text> description = Lists.newArrayList();
		
		String header = "value."+Reference.ModInfo.MOD_ID+".entity";
		if(isFilterList)
			header += "_filter";
		if(isList())
			header += "_list";
		
		description.add(Text.translatable(header, value.size()));
		
		value.forEach((val) -> {
			Text entry = describeValue(val);
			description.add(isList() ? Text.literal(" * ").append(entry) : entry);
		});
		return description;
	}
	
	public MutableText describeValue(WhiteboardObjEntity.EntityData value)
	{
		if(value.valueName != null)
			return value.valueName;
		else if(value.isFilter())
			return (MutableText)value.type.getName();
		return Text.translatable("value."+Reference.ModInfo.MOD_ID+".entity");
	}
	
	public boolean isFilterList() { return this.isFilterList; }
	
	public WhiteboardObjEntity setFilter(boolean bool) { this.isFilterList = bool; return this; }
	
	protected NbtCompound write(NbtCompound compound)
	{
		compound.putBoolean("Filter", isFilterList);
		return compound;
	}
	
	protected void read(NbtCompound compound)
	{
		this.isFilterList = compound.getBoolean("Filter");
	}
	
	public void add(Entity val)
	{
		if(val == null || value.size() > 0 && matches(val))
			return;
		
		super.add(val);
	}
	
	protected EntityData storeValue(Entity val)
	{
		return val == null ? new EntityData() : new EntityData(val);
	}
	
	protected Entity getValue(WhiteboardObjEntity.EntityData entry) { return entry.isBlank() ? null : entry.value; }
	
	protected NbtCompound valueToNbt(WhiteboardObjEntity.EntityData val) { return val.storeToNbt(new NbtCompound()); }
	
	protected EntityData valueFromNbt(NbtCompound nbt)
	{
		Optional<EntityType<?>> type = EntityType.fromNbt(nbt);
		if(!type.isPresent())
			return new EntityData();
		
		UUID id = null;
		if(nbt.contains("UUID", NbtElement.INT_ARRAY_TYPE))
			id = nbt.getUuid("UUID");
		
		WhiteboardObjEntity.EntityData data = new EntityData(id, type.get());
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
	
	public boolean isBlank() { return value.get(0).isBlank(); }
	
	/** Creates an entity value that cannot be cached, for use as a filter */
	public static WhiteboardObjEntity ofTypes(EntityType<?>... typeIn)
	{
		WhiteboardObjEntity filter = new WhiteboardObjEntity();
		filter.value.clear();
		for(EntityType<?> type : typeIn)
			filter.value.add(new EntityData((UUID)null, type));
		filter.setFilter(true);
		return filter;
	}
	
	/** Returns true if this object matches the given entity (either as a filter or specific value) */
	public boolean matches(Entity entity)
	{
		return value.isEmpty() ? true : this.value.stream().anyMatch(data -> data.matches(entity, isFilterList));
	}
	
	public static class EntityData
	{
		private Entity value = null;
		private MutableText valueName = null;
		private BlockPos lastKnownPos = BlockPos.ORIGIN;
		
		@Nullable
		private final UUID uuid;
		
		@Nullable
		private final EntityType<?> type;
		
		public EntityData()
		{
			uuid = UUID.randomUUID();
			type = null;
		}
		
		public EntityData(UUID uuidIn, EntityType<?> typeIn)
		{
			this.uuid = uuidIn;
			this.type = typeIn;
		}
		
		public EntityData(@NotNull Entity entIn)
		{
			this(entIn.getUuid(), entIn.getType());
			this.value = entIn;
			storeName(value.getName());
			this.lastKnownPos = entIn.getBlockPos();
		}
		
		protected NbtCompound storeToNbt(NbtCompound data)
		{
			if(isBlank())
				return data;
			
			if(!isFilter())
				data.putUuid("UUID", this.uuid);
			
			String typeName = getSavedEntityId();
			data.putString(Entity.ID_KEY, typeName == null ? "" : typeName);
			data.put("LastKnownPos", NbtHelper.fromBlockPos(this.lastKnownPos));
			if(valueName != null)
				data.putString("EntityName", Text.Serializer.toJson(valueName));
			
			return data;
		}
		
		public boolean isPlayer() { return this.type == EntityType.PLAYER; }
		
		/** An object without an entity type cannot be recached or used as a filter */
		public boolean isBlank() { return this.type == null; }
		
		/** Filters intentionally lack a UUID */
		public boolean isFilter() { return !isBlank() && this.uuid == null; }
		
		public boolean matches(Entity ent, boolean asFilter)
		{
			return asFilter && ent.getType() == this.type || !asFilter && ent.getUuid().equals(this.uuid);
		}
		
		public void recache(World world)
		{
			if(isBlank() || isFilter())
				return;
			
			/*
			 * If this object represents a player, search the playerlist for them.
			 * Otherwise, search around the last known position of the entity for one with a matching UUID
			 */
			if(isPlayer())
				value = world.getPlayerByUuid(this.uuid);
			else
			{
				Box searchArea = Box.from(new Vec3d(0.5D, 0.5D, 0.5D).add(this.lastKnownPos.getX(), this.lastKnownPos.getY(), this.lastKnownPos.getZ())).expand(16D);
				if(searchArea.minY < world.getBottomY())
					searchArea = searchArea.withMinY(world.getBottomY());
				
				Class<? extends Entity> typeClass = Entity.class;
				try
				{
					Entity ent = this.type.create(world);
					typeClass = ent.getClass();
					ent.discard();
				}
				catch(Exception e) { }
				
				for(Entity ent : world.getEntitiesByClass(typeClass, searchArea, (entity) -> entity.getUuid().equals(this.uuid)))
				{
					value = ent;
					break;
				}
			}
			
			if(value != null)
			{
				if(value.isSpectator() || !value.isAlive())
					value = null;
				
				if(value != null)
				{
					storeName(value.getName());
					if(!isPlayer())
						this.lastKnownPos = value.getBlockPos();
				}
			}
		}
		
		private void storeName(Text nameIn)
		{
			this.valueName = nameIn.copyContentOnly();
		}
		
	    @Nullable
	    protected String getSavedEntityId()
	    {
	        EntityType<?> entityType = this.type;
	        Identifier identifier = EntityType.getId(entityType);
	        return identifier == null ? null : identifier.toString();
	    }
	}
}