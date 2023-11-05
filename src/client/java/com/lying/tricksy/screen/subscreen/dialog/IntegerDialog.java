package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.screen.subscreen.CreateStaticScreen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class IntegerDialog extends ValueDialog<Integer>
{
	public IntegerDialog(CreateStaticScreen parentIn) { super(parentIn); }
	
	public static final int[] BUTTON_VALS = new int[]{-20, -10, -1, 1, 10, 20};
	private TextFieldWidget input;
	
	protected void init()
	{
		addSelectableChild(input = makeCentredIntInput(this.width / 2, this.height / 2 - 4, 4, false));
		setInitialFocus(input);
		
		int y = (this.height / 2) + 30;
		int x = (this.width - BUTTON_VALS.length * 30) / 2;
		for(int val : BUTTON_VALS)
		{
			addDrawableChild(ButtonWidget.builder(Text.literal((val >= 0 ? "+" : "")+String.valueOf(val)), (button) -> incVal(val * (hasShiftDown() ? 10 : 1))).dimensions(x, y, 25, 20).build());
			x += 30;
		}
	}
	
	public void incVal(int amount)
	{
		int val = Integer.valueOf(input.getText());
		val = MathHelper.clamp(val + amount, 0, 30000000);
		input.setText(String.valueOf(val));
	}
	
	public void tick()
	{
		super.tick();
		input.tick();
	}
	
	public IWhiteboardObject<Integer> createValue()
	{
		int val = 0;
		try
		{
			val = Math.max(0, Integer.valueOf(input.getText()));
		}
		catch(NumberFormatException e) { }
		return new WhiteboardObj.Int(val);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(this.input.isFocused())
		{
			incVal((int)(amount * (hasShiftDown() ? 10 : 1)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(this.input.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.drawNineSlicedTexture(DIALOG_TEXTURES, (this.width / 2) - 95, (this.height / 2) - 35, 185, 97, 10, 200, 26, 0, 0);
		renderTitle(TFObjType.INT.translated(), context, (this.height / 2) - 25);
		super.render(context, mouseX, mouseY, delta);
		input.render(context, mouseX, mouseY, delta);
	}
}
