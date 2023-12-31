package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.screen.subscreen.CreateStaticScreen;
import com.lying.tricksy.utility.TricksyUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class BlockPosDialog extends ValueDialog<BlockPos>
{
	public BlockPosDialog(CreateStaticScreen parentIn) { super(parentIn); }

	private final TextFieldWidget[] inputs = new TextFieldWidget[3];
	private Direction face = Direction.UP;
	
	protected void init()
	{
		BlockPos initialPos = this.client.player.getBlockPos();
		Integer[] coords = new Integer[] {initialPos.getX(), initialPos.getY(), initialPos.getZ()};
		for(int i=0; i<3; i++)
		{
			inputs[i] = makeCentredIntInput(this.width / 2 - 40, (this.height) / 2 - 29 + (i * 25), 0, true);
			inputs[i].setText(String.valueOf(coords[i]));
			addSelectableChild(inputs[i]);
		}
		setInitialFocus(inputs[0]);
		
		addDrawableChild(ButtonWidget.builder(TricksyUtils.translateDirection(Direction.UP), (button) -> 
		{
			Direction[] options = Direction.values();
			face = options[(face.ordinal() + 1) % options.length];
			button.setMessage(TricksyUtils.translateDirection(face));
		}).dimensions(this.width / 2 + 25, this.height / 2 - 20, 40, 40).build());
	}
	
	public void incVal(int amount, TextFieldWidget input, Pair<Integer, Integer> limits)
	{
		int val = 0;
		try
		{
			val = Integer.valueOf(input.getText());
		}
		catch(NumberFormatException e) { }
		val = MathHelper.clamp(val + amount, limits.getFirst(), limits.getSecond());
		input.setText(String.valueOf(val));
	}
	
	private static Pair<Integer, Integer> getLimitsFor(int index)
	{
		return index == 1 ? Pair.of(-64, 320) : Pair.of(-30000000, 30000000);
	}
	
	public void tick()
	{
		super.tick();
		for(TextFieldWidget input : inputs)
			input.tick();
	}
	
	public IWhiteboardObject<BlockPos> createValue()
	{
		BlockPos pos = BlockPos.ORIGIN;
		try
		{
			int x = MathHelper.clamp(Integer.valueOf(inputs[0].getText()), getLimitsFor(0).getFirst(), getLimitsFor(0).getSecond());
			int y = MathHelper.clamp(Integer.valueOf(inputs[1].getText()), getLimitsFor(1).getFirst(), getLimitsFor(1).getSecond());
			int z = MathHelper.clamp(Integer.valueOf(inputs[2].getText()), getLimitsFor(2).getFirst(), getLimitsFor(2).getSecond());
			pos = new BlockPos(x, y, z);
		}
		catch(NumberFormatException e) { }
		return new WhiteboardObjBlock(pos, face);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(getFocused() != null && getFocused() instanceof TextFieldWidget)
			for(int i=0; i<3; i++)
			{
				TextFieldWidget input = inputs[i];
				if(!input.isFocused())
					continue;
				incVal((int)(amount * (hasShiftDown() ? 10 : 1)), input, getLimitsFor(i));
				return true;
			}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		for(TextFieldWidget input : inputs)
			if(getFocused() == input && input.keyPressed(keyCode, scanCode, modifiers))
				return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		context.drawNineSlicedTexture(DIALOG_TEXTURES, (this.width / 2) - 95, (this.height / 2) - 60, 185, 115, 10, 200, 26, 0, 0);
		renderTitle(TFObjType.BLOCK.translated(), context, (this.height / 2) - 50);
		super.render(context, mouseX, mouseY, delta);
		for(TextFieldWidget input : inputs)
			input.render(context, mouseX, mouseY, delta);
	}
}
