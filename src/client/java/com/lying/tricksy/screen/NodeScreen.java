package com.lying.tricksy.screen;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.NodeRenderUtils.NodeRenderFlags;
import com.lying.tricksy.screen.TreeScreen.NodeElement;
import com.lying.tricksy.screen.subscreen.NestedScreen;
import com.lying.tricksy.screen.subscreen.ReferencesScreen;
import com.lying.tricksy.screen.subscreen.SubTypeScreen;
import com.lying.tricksy.screen.subscreen.TypeScreen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;

/**
 * Dedicated screen for editing specific nodes more clearly
 * @author Lying
 */
public class NodeScreen	extends TricksyScreenBase implements INestedScreenProvider<NodeScreen>
{
	public static final Identifier EDITOR_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree_editor.png");
	private static final int MAIN_BAR_Y = 82;
	public final PlayerEntity player;
	
	/** The node being edited */
	public TreeNode<?> currentNode;
	private final Predicate<TreeNode<?>> displayPredicate;
	private Vec2f treeOffset;
	
	private TextFieldWidget nameField;
	private ButtonWidget discreteButton;
	
	private final List<EditablePart> parts = Lists.newArrayList();
	private int targetIndex = -1;
	private int hoveredIndex = -1;
	
	private Map<NodeElement, NestedScreen<NodeScreen>> editorMap = new HashMap<>();
	
	public NodeScreen(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title, @NotNull TreeNode<?> node)
	{
		super(handler, inventory, title);
		this.player = inventory.player;
		this.currentNode = node;
		
		this.displayPredicate = (child) -> child.getID().equals(this.currentNode.getID());
	}
	
	protected void init()
	{
		updateTreeRender();
		int midWidth = this.width / 2;
		initialiseParts();
		generateParts();
		
		this.nameField = new TextFieldWidget(this.textRenderer, midWidth - 52, MAIN_BAR_Y, 104, 12, Text.empty());
		this.nameField.setEditableColor(-1);
		this.nameField.setUneditableColor(-1);
		this.nameField.setDrawsBackground(false);
		this.nameField.setMaxLength(18);
		this.nameField.setFocusUnlocked(true);
		this.nameField.setChangedListener(this::onRenamed);
		this.nameField.setPlaceholder(currentNode.getType().translatedName());
		this.nameField.setText(currentNode.hasCustomName() ? currentNode.getDisplayName().getString() : "");
		this.nameField.setEditable(!currentNode.isRoot());
		this.addSelectableChild(this.nameField);
		
		addDrawableChild(discreteButton = new DiscretionButton(midWidth + 60, MAIN_BAR_Y - 6, (button) -> 
		{
			this.currentNode.setDiscrete(!this.currentNode.isDiscrete(true));
			updateTreeRender();
			generateParts();
		}, this));
		this.discreteButton.active = !currentNode.isRoot() && currentNode.hasChildren();
		
		// Changing supertype
		editorMap.put(NodeElement.TYPE, new TypeScreen(this));
		// Changing subtype
		editorMap.put(NodeElement.SUBTYPE, new SubTypeScreen(this));
		// Assigning input variables
		editorMap.put(NodeElement.VARIABLES, new ReferencesScreen(this));
		
		editorMap.values().forEach((sub) -> sub.init(client, width, height));
	}
	
	@Nullable
	public EditablePart targetPart() { return this.targetIndex >= 0 ? parts.get(targetIndex) : null; }
	
	@Nullable
	public EditablePart hoveredPart() { return this.hoveredIndex >= 0 ? parts.get(hoveredIndex) : null; }
	
	public Optional<NestedScreen<NodeScreen>> getSubScreen() { return this.targetPart() == null || !editorMap.containsKey(this.targetPart().type) ? Optional.empty() : Optional.of(editorMap.get(this.targetPart().type)); }
	
	public void closeSubScreen() { this.targetIndex = -1; }
	
	private void setTargetPart(int index)
	{
		this.targetIndex = index;
		initChild(client, width, height);
	}
	
	public void updateTreeRender()
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
		tickChild();
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		return childCharTyped(chr, modifiers) || super.charTyped(chr, modifiers);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(childKeyPressed(keyCode, scanCode, modifiers))
			return true;
		
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
		else if(keyCode == GLFW.GLFW_KEY_ESCAPE || client.options.inventoryKey.matchesKey(keyCode, scanCode))
		{
			TreeScreen tree = new TreeScreen(getScreenHandler(), this.playerInv, this.title);
			tree.setPosition((int)treeOffset.x, (int)treeOffset.y);
			client.setScreen(tree);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		return childMouseScrolled(mouseX, mouseY, amount) || super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0)
		{
			if(!this.nameField.isMouseOver(x, y))
				this.setFocused(null);
			
			if(this.nameField.isMouseOver(x, y))
			{
				this.setFocused(this.nameField);
				return true;
			}
			
			if(!childrenMouseClicked(x, y, mouseKey))
			{
				if(this.hoveredPart() != null)
				{
					setTargetPart(this.hoveredIndex);
					return true;
				}
				else
					setTargetPart(-1);
			}
			else
				return true;
		}
		return super.mouseClicked(mouseKey, mouseKey, mouseKey);
	}
	
	protected boolean childrenMouseClicked(double x, double y, int mouseKey)
	{
		return childMouseClicked(x, y, mouseKey) || super.childrenMouseClicked(x, y, mouseKey);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		this.hoveredIndex = hoveredPart(mouseX, mouseY);
		if(this.hoveredIndex >= 0 && this.hoveredIndex != this.targetIndex)
			this.hoveredPart().render(context, false);
		if(this.targetPart() != null)
		{
			this.targetPart().render(context, true);
			
			switch(this.targetPart().type)
			{
				case TYPE:
					renderPartTooltip(List.of(currentNode.getType().description()), context, currentNode.screenY + currentNode.height + 20);
					break;
				case SUBTYPE:
					renderPartTooltip(currentNode.getSubType().fullDescription(), context, currentNode.screenY + currentNode.height + 20);
					break;
				case VARIABLES:
					break;
			}
		}
		
		renderChildForeground(context, mouseX, mouseY);
		
		context.drawTexture(NodeRenderUtils.TREE_TEXTURES, (this.width - 200) / 2, 2, 0, 68, 200, 26);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		
		this.nameField.render(context, mouseY, mouseX, 0F);
	}
	
	@SuppressWarnings("deprecation")
	private void renderPartTooltip(List<MutableText> desc, DrawContext context, final int y)
	{
		List<OrderedText> lines = Lists.newArrayList();
		for(MutableText line : desc)
			lines.addAll(textRenderer.wrapLines(line, 170));
		
		int padding = 4;
		
		int tooltipHeight = (textRenderer.fontHeight * lines.size() + lines.size() - 1) + padding;
		int drawY = Math.min(this.height - tooltipHeight - 2, y);
		
		int maxWidth = 0;
		for(OrderedText text : lines)
			maxWidth = Math.max(maxWidth, textRenderer.getWidth(text));
		int tooltipWidth = maxWidth + padding;
		
		context.draw(() -> TooltipBackgroundRenderer.render(context, (this.width - tooltipWidth) / 2, drawY - (padding / 2), tooltipWidth, tooltipHeight, 0));
		
		int textY = drawY;
		for(OrderedText text : lines)
		{
			int width = textRenderer.getWidth(text);
			context.drawText(textRenderer, text, (this.width - width) / 2, textY, -1, false);
			textY += textRenderer.fontHeight + 1;
		}
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		TreeNode<?> root = handler.getTree().root();
		NodeRenderUtils.renderTree(root, context, textRenderer, this.ticksOpen, this.displayPredicate, true);
		renderBackground(context);
		
		EnumSet<NodeRenderFlags> flags = EnumSet.noneOf(NodeRenderFlags.class);
		flags.addAll(NodeRenderFlags.SOLO);
		if(this.nameField.isFocused())
			flags.remove(NodeRenderFlags.TYPE);
		NodeRenderUtils.renderNode(currentNode, context, textRenderer, this.ticksOpen, flags);
		
		context.drawTexture(EDITOR_TEXTURES, (this.width / 2) - 94, MAIN_BAR_Y - 13, 0, 0, 60, 188, 31, 256, 256);
		
		renderChild(context, delta, mouseX, mouseY);
	}
	
	private void onRenamed(String name)
	{
		this.currentNode.setCustomName(name.isEmpty() ? null : Text.literal(name));
	}
	
	/** Generates the parts shared by all nodes, ie. the type and subtype */
	private void initialiseParts()
	{
		int width = currentNode.width;
		parts.add(new EditablePart(NodeElement.TYPE).setBounds(10, 1, width - 10, 14));
		parts.add(new EditablePart(NodeElement.SUBTYPE).setBounds(30, 13, width - 30, 25));
	}
	
	/** Recalculates variable entries in nodes, mainly variables */
	public void generateParts()
	{
		// Trim parts list to just the known essentials (type and subtype)
		List<EditablePart> typeAndSubtype = Lists.newArrayList();
		typeAndSubtype.addAll(parts.subList(0, 2));
		parts.clear();
		parts.addAll(typeAndSubtype);
		
		// Generate parts related to variables
		List<Pair<WhiteboardRef, Optional<INodeIOValue>>> sortedVariables = NodeRenderUtils.getSortedIOs(currentNode);
		if(!sortedVariables.isEmpty())
		{
			int width = currentNode.width;
			for(int i=0; i<sortedVariables.size(); i++)
				parts.add(new EditablePart(sortedVariables.get(i).getLeft()).setBounds(50, 24 + (i * 11), width - 6, 25 + (i + 1) * 11));
		}
		
		// If the target part index referred to a variable, reset it
		if(this.targetIndex > 1)
			this.targetIndex = -1;
	}
	
	public int hoveredPart(int mouseX, int mouseY)
	{
		Vec2f currentPos = new Vec2f(currentNode.screenX, currentNode.screenY);
		for(int i=0; i<parts.size(); i++)
		{
			EditablePart part = parts.get(i);
			part.move(currentPos);
			if(part.contains(mouseX, mouseY))
				return i;
		}
		return -1;
	}
	
	@Nullable
	public WhiteboardRef targetIORef() { return this.targetPart() != null ? this.targetPart().inputRef : null; }
	
	@Nullable
	public Predicate<WhiteboardRef> targetInputPred()
	{
		if(this.targetPart() == null || this.targetPart().type != NodeElement.VARIABLES)
			return Predicates.alwaysTrue();
		INodeIO input = currentNode.getSubType().getIOCondition(this.targetPart().inputRef);
		return input != null ? input.predicate() : Predicates.alwaysTrue();
	}
	
	private static class EditablePart
	{
		/** The type of this part, used for option handling */
		public final NodeElement type;
		
		/** The input reference of this part, used for variable assignment */
		public final WhiteboardRef inputRef;
		
		private Vec2f min;
		private int width, height;
		private Vec2f pos = Vec2f.ZERO;
		
		public EditablePart(WhiteboardRef refIn)
		{
			this.type = NodeElement.VARIABLES;
			this.inputRef = refIn;
		}
		
		public EditablePart(NodeElement typeIn)
		{
			this.type = typeIn;
			this.inputRef = null;
		}
		
		public EditablePart setBounds(int x1, int y1, int x2, int y2)
		{
			this.min = new Vec2f(Math.min(x1, x2), Math.min(y1, y2));
			this.width = x2 - x1;
			this.height = y2 - y1;
			return this;
		}
		
		public boolean contains(int x, int y)
		{
			int x1 = (int)(min.x + pos.x);
			int x2 = x1 + width;
			int y1 = (int)(min.y + pos.y);
			int y2 = y1 + height;
			return x >= x1 && x <= x2 && y >= y1 && y <= y2;
		}
		
		public void render(DrawContext context, boolean selected)
		{
			Vec2f topLeft = min.add(pos);
			if(selected)
				for(int i=0; i<3; i++)
				{
					int col = i%2 == 1 ? 0xFFFFF3CA : 0xFFF0D299;
					context.drawBorder((int)topLeft.x - i, (int)topLeft.y - i, width + i*2, height + i*2, col);
				}
			else
				context.drawBorder((int)topLeft.x, (int)topLeft.y, width, height, 0xFF707070);
		}
		
		public void move(Vec2f posIn) { this.pos = posIn; }
	}
	
	private static class DiscretionButton extends ButtonWidget
	{
		private final NodeScreen parent;
		
		public DiscretionButton(int x, int y, PressAction onPress, NodeScreen parentIn)
		{
			super(x, y, 20, 20, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
			this.parent = parentIn;
		}
		
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			super.renderButton(context, mouseX, mouseY, delta);
			int texY = parent.currentNode.isDiscrete(true) ? 20 : 0;
			context.drawTexture(EDITOR_TEXTURES, this.getX(), this.getY(), 0, 0, texY, 20, 20, 256, 256);
		}
	}
}
