package com.lying.tricksy.api.entity.ai;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public interface INodeIOValue 
{
	public Type type();
	
	/** Retrieves the corresponding value in the appropriate whiteboard */
	public IWhiteboardObject<?> get(WhiteboardManager<?> whiteboards);
	
	public String asString();
	
	public default NbtCompound writeToNbt(NbtCompound compound)
	{
		compound.putString("Type", type().asString());
		compound.put("Data", write(new NbtCompound()));
		return compound;
	}
	
	@Nullable
	public static INodeIOValue readFromNbt(NbtCompound compound)
	{
		/** Only earlier versions save variables as whiteboard references */
		if(compound.contains(WhiteboardRef.BOARD_KEY, NbtElement.STRING_TYPE))
			return WhiteboardValue.fromNbt(compound);
		
		Type type = Type.fromString(compound.getString("Type"));
		if(type != null)
		{
			NbtCompound nbt = compound.getCompound("Data");
			switch(type)
			{
				case WHITEBOARD:
					return WhiteboardValue.fromNbt(nbt);
				case STATIC:
					return StaticValue.fromNbt(nbt);
				default:
					return null;
			}
		}
		return null;
	}
	
	public NbtCompound write(NbtCompound compound);
	
	public Text displayName();
	
	public static class WhiteboardValue implements INodeIOValue
	{
		private final WhiteboardRef reference;
		
		public WhiteboardValue(WhiteboardRef refIn)
		{
			this.reference = refIn;
		}
		
		public Type type() { return Type.WHITEBOARD; }
		
		public String asString() { return type().name() + "[" + reference.toString() + "]"; }
		
		public Text displayName() { return reference.displayName(); }
		
		public IWhiteboardObject<?> get(WhiteboardManager<?> whiteboards)
		{
			return Whiteboard.get(reference, whiteboards);
		}
		
		public NbtCompound write(NbtCompound compound)
		{
			return this.reference.writeToNbt(compound);
		}
		
		public static WhiteboardValue fromNbt(NbtCompound compound)
		{
			return new WhiteboardValue(WhiteboardRef.fromNbt(compound));
		}
		
		public WhiteboardRef assignment() { return this.reference; }
	}
	
	public static class StaticValue implements INodeIOValue
	{
		private final IWhiteboardObject<?> value;
		
		public StaticValue(IWhiteboardObject<?> objIn)
		{
			this.value = objIn;
		}
		
		public Type type() { return Type.STATIC; }
		
		public String asString() { return type().name() + "[" + value.type().registryName().toString() + "]"; }
		
		public IWhiteboardObject<?> get(WhiteboardManager<?> whiteboards) { return value.copy(); }
		
		public NbtCompound write(NbtCompound compound)
		{
			return value.writeToNbt(compound);
		}
		
		public static StaticValue fromNbt(NbtCompound compound)
		{
			return new StaticValue(IWhiteboardObject.createFromNbt(compound));
		}
		
		public Text displayName() { return value.size() > 0 ? value.describe().get(0) : Text.literal("Static value"); }
	}
	
	public static enum Type implements StringIdentifiable
	{
		WHITEBOARD,
		STATIC;
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static Type fromString(String nameIn)
		{
			for(Type type : values())
				if(type.asString().equalsIgnoreCase(nameIn))
					return type;
			return null;
		}
	}
}
