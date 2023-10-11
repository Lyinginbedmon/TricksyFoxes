package com.lying.tricksy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFParticles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixinClient
{
	@Shadow
	public abstract World getWorld();
	
	@Shadow
	public abstract Vec3d getVelocity();
	
	@Shadow
	public abstract BlockPos getBlockPos();
	
	@Shadow
	public abstract BlockPos getLandingPos();
	
	@Inject(method = "spawnSprintingParticles()V", at = @At("RETURN"), cancellable = true)
	private void tricksy$spawnHatParticles(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent.getType() != EntityType.PLAYER)
			return;
		
		PlayerEntity player = (PlayerEntity)ent;
		if(player.getEquippedStack(EquipmentSlot.HEAD).getItem() != TFItems.SAGE_HAT)
			return;
		
		Random random = player.getRandom();
		if(random.nextInt(3) > 0)
			return;
		
        Vec3d vel = this.getVelocity().normalize().multiply(0.3D);
        BlockPos landPos = this.getLandingPos();
        BlockPos entPos = this.getBlockPos();
        
        double posX = player.getX() + (random.nextDouble() - 0.5) * (double)player.getDimensions(player.getPose()).width;
        if(entPos.getX() != landPos.getX())
            posX = MathHelper.clamp(posX, (double)landPos.getX(), (double)landPos.getX() + 1.0);
        
        double posZ = player.getZ() + (random.nextDouble() - 0.5) * (double)player.getDimensions(player.getPose()).width;
        if(entPos.getZ() != landPos.getZ())
            posZ = MathHelper.clamp(posZ, (double)landPos.getZ(), (double)landPos.getZ() + 1.0);
        
        this.getWorld().addParticle(TFParticles.PAPER, posX - vel.x, player.getY() + player.getEyeHeight(player.getPose()) + 0.2D, posZ - vel.z, 0, 0, 0);
	}
}
