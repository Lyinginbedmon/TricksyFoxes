package com.lying.tricksy.screen;

import com.lying.tricksy.network.RemoveUserPacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public abstract class TricksyScreenBase extends HandledScreen<TricksyTreeScreenHandler>
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	
	protected final PlayerEntity player;
	protected final PlayerInventory playerInv;
	
	protected int ticksOpen = 0;

	public TricksyScreenBase(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.playerInv = inventory;
		this.player = inventory.player;
	}
	
	public boolean shouldPause() { return true; }
	
	public void handledScreenTick()
	{
		this.ticksOpen++;
	}
	
	public void close()
	{
		super.close();
		RemoveUserPacket.send(player, getScreenHandler().tricksyUUID());
	}
	
	public static ButtonWidget makeTexturedWidget(int posX, int posY, int texX, int texY, ButtonWidget.PressAction action)
	{
		return new TexturedButtonWidget(posX, posY, 16, 16, texX, texY, 16, NodeRenderUtils.TREE_TEXTURES, 256, 256, action);
	}
}
