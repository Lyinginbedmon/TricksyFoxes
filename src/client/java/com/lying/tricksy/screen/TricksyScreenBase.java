package com.lying.tricksy.screen;

import com.lying.tricksy.network.CloseTreePacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public abstract class TricksyScreenBase extends HandledScreen<TreeScreenHandler>
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	
	protected final PlayerEntity player;
	protected final PlayerInventory playerInv;

	public TricksyScreenBase(TreeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.playerInv = inventory;
		this.player = inventory.player;
	}
	
	public boolean shouldPause() { return true; }
	
	public void close()
	{
		super.close();
		CloseTreePacket.send(player, getScreenHandler().tricksyUUID());
	}
}