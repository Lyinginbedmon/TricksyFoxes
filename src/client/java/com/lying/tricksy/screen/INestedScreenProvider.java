package com.lying.tricksy.screen;

import java.util.Optional;

import com.lying.tricksy.screen.subscreen.NestedScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

/** Defines a screen that contains one or more {@link NestedScreen} components */
public interface INestedScreenProvider<T extends Screen & INestedScreenProvider<T>>
{
	public Optional<NestedScreen<T>> getSubScreen();
	
	public void closeSubScreen();
	
	public default boolean childCharTyped(char chr, int modifiers)
	{
		if(getSubScreen().isPresent())
			return getSubScreen().get().charTyped(chr, modifiers);
		return false;
	}
	
	public default void initChild(MinecraftClient client, int width, int height) { getSubScreen().ifPresent(child -> child.init(client, width, height)); }
	
	public default void tickChild() { getSubScreen().ifPresent(child -> child.tick()); }
	
	public default void renderChild(DrawContext context, float delta, int mouseX, int mouseY) { getSubScreen().ifPresent(child -> child.render(context, mouseX, mouseY, delta)); }
	
	public default void renderChildForeground(DrawContext context, int mouseX, int mouseY) { getSubScreen().ifPresent(child -> child.doForegroundRendering(context, mouseX, mouseY)); }
	
	public default boolean childKeyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(getSubScreen().isPresent())
			return getSubScreen().get().keyPressed(keyCode, scanCode, modifiers);
		return false;
	}
	
	public default boolean childMouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(getSubScreen().isPresent() && getSubScreen().get().mouseScrolled(mouseX, mouseY, amount))
			return true;
		return false;
	}
	
	public default boolean childMouseClicked(double x, double y, int mouseKey)
	{
		if(getSubScreen().isPresent() && getSubScreen().get().mouseClicked(x, y, mouseKey))
			return true;
		return false;
	}
}
