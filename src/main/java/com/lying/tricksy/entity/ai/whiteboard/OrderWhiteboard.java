package com.lying.tricksy.entity.ai.whiteboard;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class OrderWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef ACTIVE = makeRef("is_active", TFObjType.BOOL).hidden();
	public static final WhiteboardRef TYPE = makeRef("type", TFObjType.INT).hidden();
	
	public static final WhiteboardRef POS = makeRef("position", TFObjType.BLOCK);
	public static final WhiteboardRef TAR = makeRef("target", TFObjType.ENT);
	
	public OrderWhiteboard()
	{
		super(BoardType.ORDER, null);
	}
	
	private static WhiteboardRef makeRef(String name, TFObjType<?> type)
	{
		return makeRef(name, type, BoardType.ORDER).displayName(Text.translatable("variable."+Reference.ModInfo.MOD_ID+".order_"+name));
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
		OrderWhiteboard copy = new OrderWhiteboard();
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
	public static OrderWhiteboard ofOrder(Order orderIn)
	{
		OrderWhiteboard board = new OrderWhiteboard();
		board.setValue(ACTIVE, new WhiteboardObj.Bool(true));
		board.setValue(TYPE, new WhiteboardObj.Int(orderIn.ordinal()));
		return board;
	}
	
	private static void optionalEntityPos(OrderWhiteboard board, IWhiteboardObject<?> obj)
	{
		board.setValue(OrderWhiteboard.POS, obj.as(TFObjType.BLOCK));
		if(obj.type().castableTo(TFObjType.ENT))
			board.setValue(OrderWhiteboard.TAR, obj.as(TFObjType.ENT));
	}
	
	public static enum Order implements StringIdentifiable
	{
		GOTO(0, 0xeb7418, (type) -> type.castableTo(TFObjType.BLOCK), OrderWhiteboard::optionalEntityPos),
		GUARD(3, 0xeb7418, (type) -> type.castableTo(TFObjType.BLOCK), OrderWhiteboard::optionalEntityPos),
		ATTACK(1, 0xd72b2b, TFObjType.ENT, (board,obj) -> board.setValue(OrderWhiteboard.TAR, obj.as(TFObjType.ENT))),
		INTERACT(2, 0xd72b2b, TFObjType.ENT, (board,obj) -> board.setValue(OrderWhiteboard.TAR, obj.as(TFObjType.ENT))),
		BREAK(5, 0x04a31e, TFObjType.BLOCK, (board,obj) -> board.setValue(OrderWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		ACTIVATE(4, 0x04a31e, TFObjType.BLOCK, (board,obj) -> board.setValue(OrderWhiteboard.POS, obj.as(TFObjType.BLOCK))),
		STOP(Integer.MAX_VALUE, 0x7353e8, Predicates.alwaysTrue(), (board,obj) -> {});
		
		private final int index, borderColor;
		private final Predicate<TFObjType<?>> idealType;
		private final BiConsumer<OrderWhiteboard, IWhiteboardObject<?>> commandFunc;
		
		private Order(int indexIn, int colorIn, Predicate<TFObjType<?>> typeIn, BiConsumer<OrderWhiteboard, IWhiteboardObject<?>> funcIn)
		{
			index = indexIn;
			borderColor = colorIn;
			commandFunc = funcIn;
			idealType = typeIn;
		}
		
		private Order(int indexIn, int colorIn, TFObjType<?> typeIn, BiConsumer<OrderWhiteboard, IWhiteboardObject<?>> funcIn)
		{
			this(indexIn, colorIn, (type) -> type == typeIn, funcIn);
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public int color() { return this.borderColor; }
		
		public Identifier texture() { return new Identifier(Reference.ModInfo.MOD_ID,"textures/gui/orders/"+asString()+".png"); }
		
		public MutableText translate() { return Text.translatable("order."+Reference.ModInfo.MOD_ID+"."+asString()); }
		
		public MutableText translate(MutableText target) { return Text.translatable("order."+Reference.ModInfo.MOD_ID+"."+asString()+".desc", target); }
		
		public MutableText translate(IWhiteboardObject<?> target) { return translate(target == null || target.size() == 0 ? Text.empty() : target.describe(0)); }
		
		public boolean validFor(TFObjType<?> typeIn) { return idealType == null || idealType.apply(typeIn); }
		
		/** Returns a sorted list of all valid orders for the given object type */
		public static List<Order> getOrdersFor(TFObjType<?> typeIn)
		{
			List<Order> orders = Lists.newArrayList();
			for(Order order : values())
				if(order.validFor(typeIn))
					orders.add(order);
			orders.sort((a,b) -> a.index < b.index ? -1 : a.index > b.index ? 1 : 0);
			return orders;
		}
		
		public OrderWhiteboard create(@NotNull IWhiteboardObject<?> obj)
		{
			OrderWhiteboard command = OrderWhiteboard.ofOrder(this);
			commandFunc.accept(command, obj);
			return command;
		}
		
		public static Order fromString(String nameIn)
		{
			for(Order order : values())
				if(order.asString().equalsIgnoreCase(nameIn))
					return order;
			return null;
		}
	}
}
