package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlockPosDialog extends ValueDialog<BlockPos>
{
	private TextFieldWidget[] inputs = new TextFieldWidget[3];
	
	protected void init()
	{
		int y = (this.height / 2) - 25;
		for(int i=0; i<3; i++)
		{
			inputs[i] = new TextFieldWidget(this.textRenderer, this.width / 2 - 52, y + i * 25, 104, 20, Text.translatable("container.repair"));
			inputs[i].setText(String.valueOf(0));
			inputs[i].setFocusUnlocked(false);
			inputs[i].setEditableColor(-1);
			inputs[i].setUneditableColor(-1);
			inputs[i].setDrawsBackground(true);
			inputs[i].setMaxLength(18);
			inputs[i].setFocusUnlocked(true);
			inputs[i].setEditable(true);
			
			addDrawableChild(inputs[i]);
			addSelectableChild(inputs[i]);
		}
		
		setInitialFocus(inputs[0]);
	}
	
	public void tick()
	{
		for(TextFieldWidget input : inputs)
			input.tick();
	}
	
	public IWhiteboardObject<BlockPos> createValue()
	{
		BlockPos pos = BlockPos.ORIGIN;
		try
		{
			int x = Integer.valueOf(inputs[0].getText());
			int y = Integer.valueOf(inputs[1].getText());
			int z = Integer.valueOf(inputs[2].getText());
			pos = new BlockPos(x, y, z);
		}
		catch(NumberFormatException e) { }
		return new WhiteboardObjBlock(pos);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		for(TextFieldWidget input : inputs)
			if(input.isFocused() && input.keyPressed(keyCode, scanCode, modifiers))
				return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
