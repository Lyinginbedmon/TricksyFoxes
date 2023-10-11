package com.lying.tricksy.screen;

import com.lying.tricksy.network.OpenTreeScreenPacket;
import com.lying.tricksy.network.RemoveUserPacket;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TricksyInventoryScreen extends AbstractInventoryScreen<TricksyInventoryScreenHandler>
{
	public static final Identifier BACKGROUND_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/inventory.png");
	
	// Button to view behaviour tree
	public ButtonWidget tree;
	
	protected final PlayerEntity player;
	protected final PlayerInventory playerInv;
	
	public TricksyInventoryScreen(TricksyInventoryScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.player = inventory.player;
		this.playerInv = inventory;
		this.titleX = 129;
		this.backgroundHeight = 174;
		this.playerInventoryTitleY = this.backgroundHeight - 93;
	}
	
	public void init()
	{
		super.init();
		
		addDrawableChild(tree = TricksyScreenBase.makeTexturedWidget(this.x + 77, this.y + 8, 48, 184, (button) -> 
		{
			OpenTreeScreenPacket.send(player, getScreenHandler().tricksyUUID());
		}));
	}
	
	public boolean shouldPause() { return false; }
	
	public void close()
	{
		super.close();
		RemoveUserPacket.send(player, getScreenHandler().tricksyUUID());
	}
	
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
    
    protected void drawForeground(DrawContext context, int mouseX, int mouseY)
    {
    	context.drawText(this.textRenderer, this.title, this.titleX - this.textRenderer.getWidth(this.title) / 2, this.titleY, 0x404040, false);
    	context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
    }
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		if(getScreenHandler().getTricksyMob() != null)
			InventoryScreen.drawEntity(context, this.x + 132, this.y + 75, 30, (float)(this.x + 132) - mouseX, (float)(this.y + 75 - 50) - mouseY, getScreenHandler().getTricksyMob());
	}
}
