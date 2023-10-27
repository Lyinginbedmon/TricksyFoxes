package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BooleanDialog extends ValueDialog<Boolean>
{
	private boolean value = false;
	
	protected void init()
	{
		clearChildren();
		addDrawableChild(ButtonWidget.builder(Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false"), (button) -> 
		{
			value = !value;
			button.setMessage(Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean."+(value ? "true" : "false")));
		}).dimensions(this.width / 2 - 15, this.height / 2 - 15, 30, 30).build());
	}
	
	public IWhiteboardObject<Boolean> createValue()
	{
		return new WhiteboardObj.Bool(value);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.drawNineSlicedTexture(DIALOG_TEXTURES, (this.width) / 2 - 40, (this.height / 2) - 40, 80, 70, 10, 200, 26, 0, 0);
		renderTitle(TFObjType.BOOL.translated(), context, (this.height / 2) - 30);
		super.render(context, mouseX, mouseY, delta);
	}
}
