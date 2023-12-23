package com.lying.tricksy.block;

import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFParticles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class BlockFoxFire extends Block implements Waterloggable
{
	public static final DefaultParticleType PARTICLE = TFParticles.FOXFIRE;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final IntProperty TIME = Properties.AGE_4;
	
	public BlockFoxFire(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(TIME, 3).with(WATERLOGGED, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(TIME, WATERLOGGED);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return VoxelShapes.empty();
	}
	
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) { return true; }
	
	public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.INVISIBLE; }
	
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) { return 1F; }
	
	public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED).booleanValue() ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState(); }
	
	public boolean hasRandomTicks(BlockState state) { return state.get(TIME) < 4; }
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		int time = state.get(TIME).intValue();
		if(time-- <= 0)
			world.breakBlock(pos, false);
		else
			world.setBlockState(pos, state.with(TIME, time));
	}
	
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
    {
        double d = (double)pos.getX() + 0.5 + (random.nextDouble() * 0.25D);
        double e = (double)pos.getY() + 0.5 + (random.nextDouble() * 0.25D);
        double f = (double)pos.getZ() + 0.5 + (random.nextDouble() * 0.25D);
        world.addParticle(PARTICLE, d, e, f, 0.0, 0.0, 0.0);
    }
    
    public BlockState getPlacementState(ItemPlacementContext ctx)
    {
    	if(!ctx.getWorld().getBlockState(ctx.getBlockPos()).getFluidState().isEmpty())
    		return getDefaultState().with(WATERLOGGED, true);
    	return super.getPlacementState(ctx);
    }
    
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
    {
    	BlockState stateAt = world.getBlockState(pos);
    	return stateAt.getFluidState().isEmpty() && (world.isAir(pos) || stateAt.isReplaceable() || stateAt.isOf(TFBlocks.FOX_FIRE));
    }
}
