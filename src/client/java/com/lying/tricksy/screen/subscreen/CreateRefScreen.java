package com.lying.tricksy.screen.subscreen;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.NodeRenderUtils;
import com.lying.tricksy.screen.WhiteboardScreen;
import com.lying.tricksy.screen.subscreen.dialog.ValueDialog;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CreateRefScreen extends NestedScreen<WhiteboardScreen>
{
	private static final int buttonSpacing = 5;
	private static final int buttonWidth = TFObjType.CREATABLES.length * 20 + (TFObjType.CREATABLES.length - 1) * buttonSpacing;
	
	private TextFieldWidget nameField;
	private TFObjType<?> objType = TFObjType.BOOL;
	private final List<TypeButton> typeButtons = Lists.newArrayList();
	
	private ButtonWidget createButton;
	
	public CreateRefScreen(WhiteboardScreen parentIn)
	{
		super(parentIn);
	}
	
	protected void init()
	{
		super.init();
		this.clearChildren();
		
		int midY = this.height / 2;
		addSelectableChild(nameField = new TextFieldWidget(this.textRenderer, (this.width - 102) / 2 + 5, midY - 15, 100, 20, Text.empty())
				{
					public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
					{
						context.drawTexture(ValueDialog.DIALOG_TEXTURES, getX() - 5, getY() - 6, 0, 48, width + 2, height + 2);
						super.renderButton(context, mouseX, mouseY, delta);
					}
				});
		setInitialFocus(nameField);
		this.nameField.setMaxLength(15);
		this.nameField.setDrawsBackground(false);
		
		int buttonY = midY + 10;
		int buttonX = (this.width - buttonWidth) / 2;
		typeButtons.clear();
		for(TFObjType<?> type : TFObjType.CREATABLES)
		{
			TypeButton typeButton = new TypeButton(buttonX, buttonY, type, (button) -> 
			{
				objType = type;
				typeButtons.forEach(entry -> entry.active = true);
				button.active = false;
			});
			typeButton.active = type != objType;
			addDrawableChild(typeButton);
			typeButtons.add(typeButton);
			buttonX += 20 + buttonSpacing;
		};
		
		addDrawableChild(createButton = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".whiteboard_screen.finalise"), button -> 
		{
			WhiteboardRef reference = makeRef();
			if(reference != null)
			{
				this.parent.getScreenHandler().addBlankReference(makeRef());
				this.parent.updateList();
			}
			else
				TricksyFoxes.LOGGER.warn("Attempted to add an invalid blank reference to the whiteboard");
			this.parent.closeSubScreen();
		}).dimensions((this.width - 30) / 2, midY + 45, 30, 20).build());
	}
	
	@Nullable
	public WhiteboardRef makeRef()
	{
		String name = SharedConstants.stripInvalidChars(nameField.getText());
		if(name.length() == 0 || name.length() > 50)
			return null;
		
		return new WhiteboardRef(nameField.getText(), this.objType, BoardType.LOCAL);
	}
	
	public void tick()
	{
		super.tick();
		this.nameField.tick();
		this.createButton.active = makeRef() != null;
	}
	
	public boolean charTyped(char chr, int modifiers)
	{
		return this.nameField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(this.nameField.isFocused() && keyCode == GLFW.GLFW_KEY_ESCAPE)
		{
			setFocused(null);
			return true;
		}
		return (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.isActive()) || super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean isNameHovered = this.nameField.isMouseOver(mouseX, mouseY);
		if(this.nameField.isFocused() && !isNameHovered)
			setFocused(null);
		else if(isNameHovered)
			setFocused(this.nameField);
		
		return this.nameField.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		int backingWidth = 185;
		int backingHeight = 97;
		context.drawNineSlicedTexture(ValueDialog.DIALOG_TEXTURES, (this.width - backingWidth) / 2, (this.height - backingHeight) / 2, backingWidth, backingHeight, 10, 200, 26, 0, 0);
		super.render(context, mouseX, mouseY, delta);
		
		Text title = Text.translatable("gui."+Reference.ModInfo.MOD_ID+".whiteboard_screen.create_reference");
		context.drawText(client.textRenderer, title, (this.width - client.textRenderer.getWidth(title)) / 2, (this.height / 2) - 35, 0x505050, false);
		this.nameField.render(context, mouseX, mouseY, delta);
		
		for(TypeButton button : this.typeButtons)
			if(button.isHovered())
			{
				context.drawTooltip(this.textRenderer, button.type.translated(), mouseX, mouseY);
				break;
			}
	}
	
	private static class TypeButton extends ButtonWidget
	{
		private final TFObjType<?> type;
		
		public TypeButton(int x, int y, TFObjType<?> typeIn, PressAction onPress)
		{
			super(x, y, 20, 20, Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			this.type = typeIn;
		}
		
		protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			super.renderButton(context, mouseX, mouseY, delta);
			int iconX = this.getX() + (this.getWidth() - 16) / 2;
			int iconY = this.getY() + (this.getHeight() - 16) / 2;
			NodeRenderUtils.renderRefType(type, context, iconX, iconY, 17, 17);
		}
	}
}
