package com.lying.tricksy.screen.subscreen;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.NodeScreen;
import com.lying.tricksy.screen.subscreen.dialog.BlockPosDialog;
import com.lying.tricksy.screen.subscreen.dialog.BooleanDialog;
import com.lying.tricksy.screen.subscreen.dialog.IntegerDialog;
import com.lying.tricksy.screen.subscreen.dialog.ValueDialog;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CreateStaticScreen extends NodeSubScreen
{
	private static final Map<TFObjType<?>, Supplier<ValueDialog<?>>> DIALOGS = Map.of(
			TFObjType.BOOL, () -> new BooleanDialog(),
			TFObjType.INT, () -> new IntegerDialog(),
			TFObjType.BLOCK, () -> new BlockPosDialog());
	
	/** The parent ReferencesScreen */
	private final ReferencesScreen refParent;
	/** The base TFObjType of the target input */
	private final TFObjType<?> objType;
	/** The full WhiteboardRef predicate of the target input */
	private final Predicate<WhiteboardRef> objPredicate;
	
	private ObjTypeList typeList;
	private ButtonWidget saveButton;
	
	public TFObjType<?> currentType;
	private ValueDialog<?> currentDialog;
	
	public CreateStaticScreen(NodeScreen parentIn, ReferencesScreen refParentIn)
	{
		super(parentIn);
		this.refParent = refParentIn;
		objType = parentIn.targetInputRef().type();
		objPredicate = parentIn.targetInputPred();
	}
	
	protected void init()
	{
		clearChildren();
		addDrawableChild(typeList = new ObjTypeList(150, this.height, 0, this.height));
		typeList.setEntries(objPredicate, this);
		typeList.setLeftPos(0);
		
		addDrawableChild(saveButton = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.save"), (button) -> 
		{
			this.parent.currentNode.assignObj(parent.targetInputRef(), getCurrentValue()); 
			this.refParent.closeStatic();
		}).dimensions(this.width / 2 - 20, (this.height / 2) + 60, 40, 20).build());
		
		openDialog(objType);
	}
	
	public void openDialog(TFObjType<?> type)
	{
		this.currentType = type;
		this.currentDialog = DIALOGS.getOrDefault(type, () -> null).get();
		if(this.currentDialog != null)
			this.currentDialog.init(client, width, height);
	}
	
	public Optional<ValueDialog<?>> dialogOpen() { return this.currentDialog == null ? Optional.empty() : Optional.of(this.currentDialog); }
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		if(typeList.children().size() > 1)
			typeList.render(context, mouseX, mouseY, delta);
		saveButton.render(context, mouseX, mouseY, delta);
		
		dialogOpen().ifPresent(dialog -> dialog.render(context, mouseX, mouseY, delta));
	}
	
	private IWhiteboardObject<?> getCurrentValue()
	{
		if(dialogOpen().isPresent())
			return dialogOpen().get().createValue();
		
		return objType.blank();
	}
	
	public void tick()
	{
		super.tick();
		dialogOpen().ifPresent(dialog -> dialog.tick());
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		if(dialogOpen().isPresent())
			return dialogOpen().get().charTyped(chr, modifiers);
		
		return super.charTyped(chr, modifiers);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode == GLFW.GLFW_KEY_ESCAPE)
		{
			refParent.closeStatic();
			return true;
		}
		
		if(dialogOpen().isPresent() && dialogOpen().get().keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(dialogOpen().isPresent() && dialogOpen().get().mouseClicked(x, y, mouseKey))
			return true;
		return super.mouseClicked(x, y, mouseKey);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(dialogOpen().isPresent() && dialogOpen().get().mouseScrolled(mouseX, mouseY, amount))
			return true;
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
}
