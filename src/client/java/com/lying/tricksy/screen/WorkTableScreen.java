package com.lying.tricksy.screen;

import com.lying.tricksy.init.TFBlocks;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WorkTableScreen extends HandledScreen<WorkTableScreenHandler>
{
	public static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
	
	public WorkTableScreen(WorkTableScreenHandler handler, PlayerInventory inventory, Text titleIn)
	{
		super(handler, inventory, TFBlocks.WORK_TABLE.getName());
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int i = this.x;
		int j = (this.height - this.backgroundHeight) / 2;
		context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}
}
