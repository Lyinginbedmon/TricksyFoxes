package com.lying.tricksy.screen.subscreen;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.screen.WhiteboardScreen;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CreateRefScreen extends NestedScreen<WhiteboardScreen>
{
	private TextFieldWidget nameField;
	private TFObjType<?> objType = TFObjType.BOOL;
	private final List<ButtonWidget> typeButtons = Lists.newArrayList();
	
	private ButtonWidget createButton;
	
	public CreateRefScreen(WhiteboardScreen parentIn)
	{
		super(parentIn);
	}
	
	protected void init()
	{
		super.init();
		
		int midY = this.height / 2;
		addSelectableChild(nameField = new TextFieldWidget(this.textRenderer, (this.width - 100) / 2, midY - 20, 100, 20, Text.empty()));
		setInitialFocus(nameField);
		
		int buttonY = midY + 15;
		int buttonX = (this.width - TFObjType.CREATABLES.size() * 30) / 2;
		typeButtons.clear();
		for(TFObjType<?> type : TFObjType.CREATABLES)
		{
			ButtonWidget typeButton = ButtonWidget.builder(type.translated(), (button) -> 
			{
				objType = type;
				typeButtons.forEach(entry -> entry.active = true);
				button.active = false;
			}).dimensions(buttonX, buttonY, 28, 20).build();
			typeButton.active = type != objType;
			addDrawableChild(typeButton);
			typeButtons.add(typeButton);
			buttonX += 30;
		};
		
		addDrawableChild(createButton = ButtonWidget.builder(Text.literal("Create"), button -> 
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
		}).dimensions((this.width - 30) / 2, midY + 40, 30, 20).build());
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
		return (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.isActive()) || super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean isNameHovered = this.nameField.isMouseOver(mouseX, mouseY);
		if(this.nameField.isFocused())
		{
			if(!isNameHovered)
				setFocused(null);
		}
		else if(isNameHovered)
			setFocused(this.nameField);
		
		return this.nameField.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		this.nameField.render(context, mouseX, mouseY, delta);
	}
}
