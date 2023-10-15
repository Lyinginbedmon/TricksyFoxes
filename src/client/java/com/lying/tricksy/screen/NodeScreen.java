package com.lying.tricksy.screen;

import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

/**
 * Dedicated screen for editing specific nodes more clearly
 * @author Lying
 */
public class NodeScreen	extends TricksyScreenBase
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public final PlayerEntity player;
	
	/** The node being edited */
	private final TreeNode<?> currentNode;
	private final Vec2f treeOffset;
	private final Predicate<TreeNode<?>> displayPredicate;
	
	private TextFieldWidget nameField;
	private ButtonWidget discreteButton;
	
	public NodeScreen(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title, TreeNode<?> node)
	{
		super(handler, inventory, title);
		player = inventory.player;
		this.currentNode = node;
		this.displayPredicate = (child) -> child.getID().equals(this.currentNode.getID());
		
		TreeNode<?> root = handler.getTree().root();
		NodeRenderUtils.scaleAndPositionNode(root, 0, 0, this.displayPredicate, true);
		this.treeOffset = new Vec2f(node.screenX + (node.width / 2), node.screenY + (node.height / 2)).negate();
	}
	
	protected void init()
	{
		int midWidth = this.width / 2;
		this.nameField = new TextFieldWidget(this.textRenderer, midWidth - 52, 50, 104, 12, Text.translatable("container.repair"));
		this.nameField.setFocusUnlocked(false);
		this.nameField.setEditableColor(-1);
		this.nameField.setUneditableColor(-1);
		this.nameField.setDrawsBackground(true);
		this.nameField.setMaxLength(50);
		this.nameField.setChangedListener(this::onRenamed);
		this.nameField.setText(currentNode.hasCustomName() ? currentNode.getDisplayName().getString() : "");
		this.nameField.setEditable(!currentNode.isRoot());
		this.addSelectableChild(this.nameField);
		this.setInitialFocus(this.nameField);
		
		addDrawableChild(discreteButton = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.hide"), (button) -> 
		{
			this.currentNode.setDiscrete(!this.currentNode.isDiscrete(true));
		}).dimensions(midWidth + 60, 50, 20, 20).build());
		discreteButton.active = !currentNode.isRoot();
		
		/**
		 * TODO Add more editing tools to node screen
		 * Change node type
		 * Change node subtype
		 *  - Categorised list of available subtypes
		 * Assign input variables
		 *  - Categorised list of whiteboard references
		 *  - Option to create a static value
		 */
	}
	
	public void handledScreenTick()
	{
		super.handledScreenTick();
		this.nameField.tick();
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(getFocused() == this.nameField)
		{
			if(keyCode == GLFW.GLFW_KEY_ESCAPE)
			{
				setFocused(null);
				return true;
			}
			else if(this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.isActive())
				return true;
		}
		else if(keyCode == GLFW.GLFW_KEY_ESCAPE || mc.options.inventoryKey.matchesKey(keyCode, scanCode))
		{
			TreeScreen tree = new TreeScreen(getScreenHandler(), this.playerInv, this.title);
			tree.setPosition((int)treeOffset.x, (int)treeOffset.y);
			client.setScreen(tree);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		
		this.nameField.render(context, mouseY, mouseX, 0F);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		int renderX = this.width / 2 + (int)treeOffset.x;
		int renderY = this.height / 2 + (int)treeOffset.y;
		
		renderBackground(context);
		TreeNode<?> root = handler.getTree().root();
		NodeRenderUtils.scaleAndPositionNode(root, renderX, renderY, this.displayPredicate, true);
		NodeRenderUtils.renderTree(root, context, textRenderer, this.ticksOpen, this.displayPredicate, true);
		renderBackground(context);
		NodeRenderUtils.renderNode(currentNode, context, textRenderer, this.ticksOpen, true);
	}
	
	private void onRenamed(String name)
	{
		this.currentNode.setCustomName(Text.literal(name));
	}
}
