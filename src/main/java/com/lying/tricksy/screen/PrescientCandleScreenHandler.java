package com.lying.tricksy.screen;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;

public class PrescientCandleScreenHandler extends ScreenHandler
{
	@Nullable
	private UUID tricksyID = null;
	
	@Nullable
	private Optional<LivingEntity> tricksy = Optional.empty();
	
	public PrescientCandleScreenHandler(int syncId, UUID idIn)
	{
		super(TFScreenHandlerTypes.PRESCIENT_CANDLE_SCREEN_HANDLER, syncId);
		if(idIn != null)
			this.tricksyID = idIn;
	}
	
	public ItemStack quickMove(PlayerEntity var1, int var2) { return null; }
	
	public boolean canUse(PlayerEntity var1) { return true; }
	
	public void setUUID(UUID idIn)
	{
		this.tricksyID = idIn;
		this.tricksy = null;
	}
	
	@Nullable
	public UUID getUUID() { return this.tricksyID; }
	
	public Optional<LivingEntity> getTricksyMob(World world, PlayerEntity player)
	{
		if(this.tricksy != null)
			return this.tricksy;
		
		return this.tricksy = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(64), mob -> mob instanceof ITricksyMob && mob.getUuid().equals(this.tricksyID)).stream().findFirst();
	}
}
