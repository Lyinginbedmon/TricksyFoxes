package com.lying.tricksy.screen.subscreen;

import org.lwjgl.glfw.GLFW;

import com.lying.tricksy.screen.INestedScreenProvider;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class NestedScreen<T extends Screen & INestedScreenProvider<T>> extends Screen
{
	protected final T parent;
	
	public NestedScreen(T parentIn)
	{
		super(Text.empty());
		parent = parentIn;
	}
	
	/** Called by the parent screen in {@link Screen:drawForeground} */
	public void doForegroundRendering(DrawContext context, int mouseX, int mouseY) { }
	
	public boolean shouldCloseOnInvKey() { return true; }
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode == GLFW.GLFW_KEY_ESCAPE || this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && shouldCloseOnInvKey())
		{
			this.parent.closeSubScreen();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
