package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public class CommandWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef ACTIVE = makeRef("is_active", TFObjType.BOOL).hidden();
	public static final WhiteboardRef TYPE = makeRef("type", TFObjType.INT).hidden();
	
	public static final WhiteboardRef POS = makeRef("position", TFObjType.BLOCK);
	public static final WhiteboardRef TAR = makeRef("target", TFObjType.ENT);
	
	public CommandWhiteboard()
	{
		super(BoardType.COMMAND, null);
	}
	
	private static WhiteboardRef makeRef(String name, TFObjType<?> type)
	{
		return makeRef(name, type, BoardType.COMMAND).displayName(Text.translatable("variable."+Reference.ModInfo.MOD_ID+".order_"+name));
	}
	
	public Whiteboard<?> build()
	{
		register(ACTIVE, () -> new WhiteboardObj.Bool(false));
		register(TYPE, () -> new WhiteboardObj.Int(0));
		
		register(POS, () -> new WhiteboardObjBlock());
		register(TAR, () -> new WhiteboardObjEntity());
		return this;
	}
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		CommandWhiteboard copy = new CommandWhiteboard();
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	public boolean hasOrder() { return currentType() != null; }
	
	@Nullable
	public Order currentType()
	{
		if(getValue(ACTIVE).as(TFObjType.BOOL).get())
		{
			int cap = Order.values().length;
			return Order.values()[getValue(TYPE).as(TFObjType.INT).get() % cap];
		}
		else
			return null;
	}
	
	/** Creates a whiteboard set to the given command, with no other data */
	public static CommandWhiteboard ofCommand(Order orderIn)
	{
		CommandWhiteboard board = new CommandWhiteboard();
		board.setValue(ACTIVE, new WhiteboardObj.Bool(true));
		board.setValue(TYPE, new WhiteboardObj.Int(orderIn.ordinal()));
		return board;
	}
	
	public static enum Order implements StringIdentifiable
	{
		GOTO((board,obj) -> board.setValue(CommandWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		GUARD((board,obj) -> board.setValue(CommandWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		ATTACK((board,obj) -> board.setValue(CommandWhiteboard.TAR, obj.as(TFObjType.ENT))),
		INTERACT((board,obj) -> board.setValue(CommandWhiteboard.TAR, obj.as(TFObjType.ENT))),
		BREAK((board,obj) -> board.setValue(CommandWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		ACTIVATE((board,obj) -> board.setValue(CommandWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		STOP((board,obj) -> {});
		
		private final BiConsumer<CommandWhiteboard, IWhiteboardObject<?>> commandFunc;
		
		private Order(BiConsumer<CommandWhiteboard, IWhiteboardObject<?>> funcIn)
		{
			commandFunc = funcIn;
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public CommandWhiteboard create(IWhiteboardObject<?> obj)
		{
			CommandWhiteboard command = CommandWhiteboard.ofCommand(this);
			commandFunc.accept(command, obj);
			return command;
		}
		
		public MutableText translate() { return Text.translatable("order."+Reference.ModInfo.MOD_ID+"."+asString()); }
		
		public static Order fromString(String nameIn)
		{
			for(Order order : values())
				if(order.asString().equalsIgnoreCase(nameIn))
					return order;
			return null;
		}
	}
}
