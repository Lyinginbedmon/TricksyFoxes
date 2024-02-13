package com.lying.tricksy.entity.projectile;

import java.util.Optional;

import com.lying.tricksy.block.BlockFoxFire;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class EntityFoxFire extends ProjectileEntity implements FlyingItemEntity
{
	private static final double VELOCITY = 0.5D;
	private static final TrackedData<Optional<BlockPos>> TARGET_POSITION = DataTracker.registerData(EntityFoxFire.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
	private static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(EntityFoxFire.class, TrackedDataHandlerRegistry.INTEGER);
	
	public EntityFoxFire(EntityType<? extends ProjectileEntity> entityType, World world)
	{
		super(TFEntityTypes.FOX_FIRE, world);
		setNoGravity(true);
	}
	
	public static EntityFoxFire fromMob(Entity shooter)
	{
		EntityFoxFire fire = TFEntityTypes.FOX_FIRE.create(shooter.getWorld());
		fire.setOwner(shooter);
		fire.setPos(shooter.getX(), shooter.getEyeY() + 0.5D, shooter.getZ());
		return fire;
	}
	
	public static void spawnFlameTargeting(LivingEntity mob, BlockPos targetPos)
	{
		EntityFoxFire fire = fromMob(mob);
		fire.setDestination(targetPos);
		
		mob.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f / (mob.getRandom().nextFloat() * 0.4f + 0.8f));
		mob.getWorld().spawnEntity(fire);
	}
	
	protected void initDataTracker()
	{
		getDataTracker().startTracking(TARGET_POSITION, Optional.empty());
		getDataTracker().startTracking(LIFESPAN, Reference.Values.TICKS_PER_SECOND * 5);
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Lifespan", lifespan());
		if(hasDestination())
			nbt.put("Target", NbtHelper.fromBlockPos(destination()));
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		getDataTracker().set(LIFESPAN, nbt.getInt("Lifespan"));
		if(nbt.contains("Target", NbtElement.COMPOUND_TYPE))
			getDataTracker().set(TARGET_POSITION, Optional.of(NbtHelper.toBlockPos(nbt.getCompound("Target"))));
	}
	
	public boolean canHit() { return false; }
	
	public boolean damage(DamageSource source, float amount) { return source.isOf(DamageTypes.OUT_OF_WORLD) ? super.damage(source, amount) : false; }
	
	public int lifespan() { return getDataTracker().get(LIFESPAN).intValue(); }
	
	public boolean hasDestination() { return getDataTracker().get(TARGET_POSITION).isPresent(); }
	
	public BlockPos destination() { return hasDestination() ? getDataTracker().get(TARGET_POSITION).get() : BlockPos.ORIGIN; }
	
	public void setDestination(BlockPos pos)
	{
		getDataTracker().set(TARGET_POSITION, Optional.of(pos));
		
		Vec3d target = pos.toCenterPos();
		Vec3d origin = getPos();
		
		Vec3d offset = target.subtract(origin).normalize();
		setVelocity(offset.multiply(VELOCITY));
	}
	
	public void tick()
	{
		if(getWorld().isClient())
		{
			if(random.nextBoolean())
			{
				Vec3d pos = getPos();
		        double d = pos.getX() + (random.nextDouble() * 0.15D);
		        double e = pos.getY() + (random.nextDouble() * 0.15D);
		        double f = pos.getZ() + (random.nextDouble() * 0.15D);
		        getWorld().addParticle(BlockFoxFire.PARTICLE, d, e, f, 0.0, 0.0, 0.0);
			}
		}
		else
		{
			HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
			if(hitResult != null && hitResult.getType() != HitResult.Type.MISS)
				onCollision(hitResult);
			this.checkBlockCollision();
			
			this.prevX = getX();
			this.prevY = getY();
			this.prevZ = getZ();
			this.setPos(getX() + getVelocity().getX(), getY() + getVelocity().getY(), getZ() + getVelocity().getZ());
			
			if(hasDestination() && getBlockPos().getManhattanDistance(destination()) == 0)
			{
				tryPlaceLight();
				return;
			}
			
			int life = lifespan() - 1;
			BlockState state = getWorld().getBlockState(getBlockPos());
			if(!state.isAir() && canIgnite(state))
			{
				ignite(getBlockPos(), getWorld());
				life -= 5;
			}
			
			getDataTracker().set(LIFESPAN, life);
			if(life < 0)
				tryPlaceLight();
		}
		
		super.tick();
	}
	
	public void tryPlaceLight()
	{
		remove(RemovalReason.DISCARDED);
		
		World world = getEntityWorld();
		BlockPos pos = getBlockPos();
		if(pos.getY() < world.getBottomY() || pos.getY() > 256)
			return;
		
		BlockState stateAt = world.getBlockState(pos);
		if(TFBlocks.FOX_FIRE.getDefaultState().canPlaceAt(world, pos))
			world.setBlockState(pos, TFBlocks.FOX_FIRE.getDefaultState().with(BlockFoxFire.WATERLOGGED, !stateAt.getFluidState().isEmpty()));
		else if(canIgnite(stateAt))
			ignite(pos, world);
	}
	
	public ItemStack getStack() { return ItemStack.EMPTY; }
	
	protected boolean canHit(Entity entity) { return entity == null; }
	
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		super.onBlockHit(blockHitResult);
		Vec3d vel = getVelocity();
		Vec3d ref = TricksyUtils.reflect(vel, Vec3d.of(blockHitResult.getSide().getVector()));
		setVelocity(ref.normalize().multiply(vel.length()));
	}
	
	public static boolean canIgnite(BlockState state)
	{
		return CampfireBlock.canBeLit(state) || CandleBlock.canBeLit(state) || CandleCakeBlock.canBeLit(state);
	}
	
	public static void ignite(BlockPos pos, World world)
	{
		BlockState state = world.getBlockState(pos);
		if(!state.getProperties().contains(Properties.LIT))
			return;
		
		world.setBlockState(pos, world.getBlockState(pos).with(Properties.LIT, true));
		world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
	}
}
