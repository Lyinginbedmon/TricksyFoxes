package com.lying.tricksy.screen.subscreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.screen.NodeRenderUtils;
import com.lying.tricksy.screen.NodeScreen;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class TypeScreen extends NestedScreen<NodeScreen>
{
	private static final NodeType<?>[] DISPLAYED_TYPES = new NodeType<?>[] {TFNodeTypes.CONTROL_FLOW, TFNodeTypes.LEAF, TFNodeTypes.DECORATOR, TFNodeTypes.CONDITION}; 
	private final Map<NodeType<?>, TypeButton> buttonMap = new HashMap<>();
	
	public TypeScreen(NodeScreen parentIn)
	{
		super(parentIn);
	}
	
	public void tick()
	{
		for(Entry<NodeType<?>, TypeButton> entry : buttonMap.entrySet())
		{
			TypeButton button = entry.getValue();
			button.setActive(parent.currentNode.getType() != entry.getKey());
		}
	}
	
	protected void init()
	{
		int midY = (this.height - DISPLAYED_TYPES.length * (TypeButton.HEIGHT + 10)) / 2;
		for(int i=0; i<DISPLAYED_TYPES.length; i++)
		{
			NodeType<?> type = DISPLAYED_TYPES[i];
			TypeButton part = new TypeButton(type, 30, midY, (button) -> 
			{
				if(parent.currentNode.isRoot())
					return;
				
				UUID uuid = parent.currentNode.getID();
				TreeNode<?> replacement = type.create(uuid);
				parent.currentNode.parent().replaceChild(uuid, replacement);
				parent.currentNode = replacement;
				parent.generateParts();
				parent.updateTreeRender();
			});
			part.setActive(this.parent.currentNode.getType() != type);
			addDrawableChild(part);
			buttonMap.put(type, part);
			midY += TypeButton.HEIGHT + 10;
		}
	}
	
	public static class TypeButton extends ButtonWidget
	{
		private static final MinecraftClient mc = MinecraftClient.getInstance();
		public static final int HEIGHT = 25;
		private final NodeType<?> type;
		
		public TypeButton(NodeType<?> typeIn, int x, int y, PressAction onPress)
		{
			super(x, y, 120, HEIGHT, typeIn.translatedName(), onPress, DEFAULT_NARRATION_SUPPLIER);
			this.type = typeIn;
			setTooltip(Tooltip.of(this.type.description()));
		}
		
		public void setActive(boolean bool)
		{
			this.active = bool;
			setTooltip(bool ? Tooltip.of(this.type.description()) : null);
		}
		
		protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			
			int texY = 92;
			if(!this.active)
				texY += height * 2;
			else if(this.hovered)
				texY += height;
			context.drawTexture(NodeScreen.EDITOR_TEXTURES, getX(), getY(), 0, 0, texY, width, height, 256, 256);
			context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			
			int colour = this.type.color();
	        int r = ((colour & 0xFF0000) >> 16);
	        int g = ((colour & 0xFF00) >> 8);
	        int b = ((colour & 0xFF) >> 0);
	        NodeRenderUtils.drawTintedTexture(NodeScreen.EDITOR_TEXTURES, context, getX(), getY(), 120, 92, width, height, r, g, b);
	        
			int i = this.active ? colour : 0xA0A0A0;
			
			Text name = type.translatedName();
			context.drawText(
					mc.textRenderer, 
					name, 
					this.getX() + (this.getWidth() - mc.textRenderer.getWidth(name)) / 2, 
					this.getY() + (getHeight() - mc.textRenderer.fontHeight) / 2 + 1, 
					 i | MathHelper.ceil((float)(this.alpha * 255.0f)) << 24, 
					false);
		}
	}
}
