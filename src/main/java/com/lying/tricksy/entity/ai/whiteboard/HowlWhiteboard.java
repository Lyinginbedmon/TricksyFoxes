package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public class HowlWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef SENDER = makeRef("sender", TFObjType.ENT, TFWhiteboards.HOWL);
	public static final WhiteboardRef POSITION = makeRef("position", TFObjType.BLOCK, TFWhiteboards.HOWL);
	
	public HowlWhiteboard()
	{
		super(TFWhiteboards.HOWL, null);
	}
	
	public Whiteboard<?> build()
	{
		// TODO Add getter functions to retrieve values from Howls data
		return this;
	}
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		HowlWhiteboard copy = new HowlWhiteboard();
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	public void setToWolf(LivingEntity wolf)
	{
		setValue(SENDER, new WhiteboardObjEntity(wolf));
		setValue(POSITION, new WhiteboardObjBlock(wolf.getBlockPos(), Direction.UP));
	}
}
