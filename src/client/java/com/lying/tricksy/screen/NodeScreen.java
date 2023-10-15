package com.lying.tricksy.screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.TreeScreen.HoveredElement;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;

/**
 * Dedicated screen for editing specific nodes more clearly
 * @author Lying
 */
public class NodeScreen	extends TricksyScreenBase
{
	public final PlayerEntity player;
	
	/** The node being edited */
	private TreeNode<?> currentNode;
	private final Predicate<TreeNode<?>> displayPredicate;
	private Vec2f treeOffset;
	
	private TextFieldWidget nameField;
	private ButtonWidget discreteButton;
	
	private final List<EditablePart> parts = Lists.newArrayList();
	private EditablePart hoveredPart = null;
	private EditablePart targetPart = null;
	
	public NodeScreen(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title, @NotNull TreeNode<?> node)
	{
		super(handler, inventory, title);
		player = inventory.player;
		this.currentNode = node;
		
		this.displayPredicate = (child) -> child.getID().equals(this.currentNode.getID());
	}
	
	protected void init()
	{
		updateTreeRender();
		int midWidth = this.width / 2;
		generateParts();
		
		this.nameField = new TextFieldWidget(this.textRenderer, midWidth - 52, currentNode.screenY - 50, 104, 12, Text.translatable("container.repair"));
		this.nameField.setFocusUnlocked(false);
		this.nameField.setEditableColor(-1);
		this.nameField.setUneditableColor(-1);
		this.nameField.setDrawsBackground(true);
		this.nameField.setMaxLength(50);
		this.nameField.setChangedListener(this::onRenamed);
		this.nameField.setText(currentNode.hasCustomName() ? currentNode.getDisplayName().getString() : "");
		this.nameField.setEditable(!currentNode.isRoot());
		this.addSelectableChild(this.nameField);
		
		addDrawableChild(discreteButton = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.hide"), (button) -> 
		{
			this.currentNode.setDiscrete(!this.currentNode.isDiscrete(true));
			updateTreeRender();
		}).dimensions(midWidth + 60, nameField.getY() - 4, 20, 20).build());
		
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
	
	private void updateTreeRender()
	{
		NodeRenderUtils.scaleAndPositionNode(handler.getTree().root(), 0, 0, this.displayPredicate, true);
		this.treeOffset = new Vec2f(currentNode.screenX + (currentNode.width / 2), currentNode.screenY + (currentNode.height / 2)).negate();
		
		int midWidth = this.width / 2;
		int treeX = midWidth + (int)treeOffset.x;
		int treeY = this.height / 2 + (int)treeOffset.y;
		TreeNode<?> root = handler.getTree().root();
		NodeRenderUtils.scaleAndPositionNode(root, treeX, treeY, this.displayPredicate, true);
	}
	
	public void handledScreenTick()
	{
		super.handledScreenTick();
		this.nameField.tick();
		discreteButton.active = !currentNode.isRoot() && currentNode.hasChildren();
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
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0)
		{
			if(!this.nameField.isHovered())
				this.setFocused(null);
			
			if(currentNode.containsPoint((int)x, (int)y))
			{
				this.targetPart = this.hoveredPart;
				return true;
			}
			else
				this.targetPart = null;
			
			if(this.discreteButton.isHovered())
				return this.discreteButton.mouseClicked(x, y, mouseKey);
			else if(this.nameField.isHovered())
				return this.nameField.mouseClicked(x, y, mouseKey);
		}
		return super.mouseClicked(mouseKey, mouseKey, mouseKey);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		
		this.nameField.render(context, mouseY, mouseX, 0F);
		
		this.hoveredPart = hoveredPart(mouseX, mouseY);
		if(this.hoveredPart != null && this.hoveredPart != this.targetPart)
			this.hoveredPart.render(context, -12303292);
		if(this.targetPart != null)
			this.targetPart.render(context, -1);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		TreeNode<?> root = handler.getTree().root();
		NodeRenderUtils.renderTree(root, context, textRenderer, this.ticksOpen, this.displayPredicate, true);
		renderBackground(context);
		NodeRenderUtils.renderNode(currentNode, context, textRenderer, this.ticksOpen, true);
	}
	
	private void onRenamed(String name)
	{
		this.currentNode.setCustomName(Text.literal(name));
	}
	
	/** Generates all available component parts of the current node */
	private void generateParts()
	{
		parts.clear();
		
		int width = currentNode.width;
		parts.add(new EditablePart(HoveredElement.TYPE).setBounds(10, 1, width - 10, 14));
		parts.add(new EditablePart(HoveredElement.SUBTYPE).setBounds(30, 13, width - 30, 25));
		
		List<Pair<WhiteboardRef, Optional<WhiteboardRef>>> sortedVariables = NodeRenderUtils.getSortedVariables(currentNode);
		if(sortedVariables.isEmpty())
			return;
		for(int i=0; i<sortedVariables.size(); i++)
			parts.add(new EditablePart(sortedVariables.get(i).getLeft()).setBounds(50, 24 + (i * 11), width - 6, 25 + (i + 1) * 11));
		
		if(this.targetPart != null)
			switch(targetPart.type)
			{
				case TYPE:
					targetPart = parts.get(0);
					break;
				case SUBTYPE:
					targetPart = parts.get(1);
					break;
				default:
					targetPart = null;
			}
	}
	
	@Nullable
	public EditablePart hoveredPart(int mouseX, int mouseY)
	{
		Vector2f currentPos = new Vector2f(currentNode.screenX, currentNode.screenY);
		for(EditablePart part : parts)
		{
			part.move(currentPos);
			if(part.contains(mouseX, mouseY))
				return part;
		}
		return null;
	}
	
	private static class EditablePart
	{
		/** The type of this part, used for option handling */
		public final HoveredElement type;
		
		/** The input reference of this part, used for variable assignment */
		public final WhiteboardRef inputRef;
		
		private Vector2f min;
		private int width, height;
		private Vector2f pos = new Vector2f(0F, 0F);
		
		public EditablePart(WhiteboardRef refIn)
		{
			this.type = HoveredElement.VARIABLES;
			this.inputRef = refIn;
		}
		
		public EditablePart(HoveredElement typeIn)
		{
			this.type = typeIn;
			this.inputRef = null;
		}
		
		public EditablePart setBounds(int x1, int y1, int x2, int y2)
		{
			this.min = new Vector2f(Math.min(x1, x2), Math.min(y1, y2));
			this.width = x2 - x1;
			this.height = y2 - y1;
			return this;
		}
		
		public boolean contains(int x, int y)
		{
			int x1 = (int)(min.getX() + pos.getX());
			int x2 = x1 + width;
			int y1 = (int)(min.getY() + pos.getY());
			int y2 = y1 + height;
			return x >= x1 && x <= x2 && y >= y1 && y <= y2;
		}
		
		public void render(DrawContext context, int color)
		{
			context.drawBorder((int)(min.getX() + pos.getX()), (int)(min.getY() + pos.getY()), width, height, color);
		}
		
		public void move(Vector2f posIn) { this.pos = posIn; }
	}
}
