package com.lying.tricksy.entity.ai.whiteboard.object;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class WhiteboardObjBlock extends WhiteboardObjBase<BlockPos, com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock.BlockData, NbtCompound>
{
	public WhiteboardObjBlock()
	{
		super(TFObjType.BLOCK, NbtElement.COMPOUND_TYPE);
	}
	
	public WhiteboardObjBlock(@NotNull BlockPos pos, @NotNull Direction face)
	{
		this();
		value.clear();
		value.add(new BlockData(pos, face));
	}
	
	public WhiteboardObjBlock(@NotNull BlockPos pos)
	{
		this(pos, Direction.UP);
	}
	
	public Text describeValue(WhiteboardObjBlock.BlockData value)
	{
		if(value.face != Direction.UP)
			return Text.translatable("value."+Reference.ModInfo.MOD_ID+".blockpos_long", value.pos.toShortString(), value.face.getName());
		else
			return Text.translatable("value."+Reference.ModInfo.MOD_ID+".blockpos", value.pos.toShortString());
	}
	
	public Direction direction() { return value.isEmpty() ? Direction.UP : value.get(0).blockFace(); }
	
	protected BlockData storeValue(BlockPos val) { return new BlockData(val); }
	
	public BlockData storeValue(BlockPos val, Direction face) { return new BlockData(val, face); }
	
	protected BlockPos getValue(WhiteboardObjBlock.BlockData entry) { return entry.pos; }
	
	protected NbtCompound valueToNbt(WhiteboardObjBlock.BlockData val) { return val.storeToNbt(new NbtCompound()); }
	
	protected BlockData valueFromNbt(NbtCompound nbt)
	{
		NbtCompound compound = (NbtCompound)nbt;
		BlockPos pos = NbtHelper.toBlockPos(compound.getCompound("Pos"));
		Direction face = Direction.byName(compound.getString("Face"));
		return new BlockData(pos, face);
	}
	
	public static class BlockData
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
}