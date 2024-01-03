package com.lying.tricksy.utility;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TricksyUtils
{
	public static int componentsToColor(float[] comp)
	{
		int r = (int)(comp[0] * 255);
		int g = (int)(comp[1] * 255);
		int b = (int)(comp[2] * 255);
		
		// Recompose original decimal value of the dye colour from derived RGB values
		int col = r;
		col = (col << 8) + g;
		col = (col << 8) + b;
		
		return col;
	}
	
	public static int stringComparator(String name1, String name2)
	{
		List<String> names = Lists.newArrayList(name1, name2);
		Collections.sort(names);
		int ind1 = names.indexOf(name1);
		int ind2 = names.indexOf(name2);
		return ind1 > ind2 ? 1 : ind1 < ind2 ? -1 : 0;
	}
	
	public static Text translateDirection(Direction dir)
	{
		return Text.translatable("enum."+Reference.ModInfo.MOD_ID+".direction."+dir.asString());
	}
	
	public static MutableText tooltipToMultilineText(List<MutableText> tooltip)
	{
		if(tooltip.isEmpty())
			return Text.empty();
		MutableText text = tooltip.get(0);
		// TODO Figure out how to turn avoid style overflow in concatenated text
		MutableText lineBreak = Text.literal("\n");
		if(tooltip.size() > 1)
			for(int i=1; i<tooltip.size(); i++)
				text.append(lineBreak).append(tooltip.get(i));
		return text;
	}
	
	public static RecipeInputInventory ingredientsFromInventory(Inventory inventory)
	{
		RecipeInputInventory input = new RecipeInputInventory()
		{
			private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
			
			public int size() { return 9; }
			
			public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
			
			public ItemStack getStack(int var1) { return inventory.get(var1); }
			
			public ItemStack removeStack(int var1, int var2) { return Inventories.splitStack(inventory, var1, var2); }
			
			public ItemStack removeStack(int var1) { return Inventories.removeStack(inventory, var1); }
			
			public void setStack(int var1, ItemStack var2) { inventory.set(var1, var2); }
			
			public void markDirty() { }
			
			public boolean canPlayerUse(PlayerEntity var1) { return true; }
			
			public void clear() { inventory.clear(); }
			
			public void provideRecipeInputs(RecipeMatcher var1)
			{
				for(ItemStack stack : inventory)
					var1.addUnenchantedInput(stack);
			}
			
			public int getWidth() { return 3; }
			
			public int getHeight() { return 3; }
			
			public List<ItemStack> getInputStacks() { return inventory; }
		};
		
		for(int i=0; i<9; i++)
			input.setStack(i, inventory.getStack(i));
		
		return input;
	}
	
	/**
	 * Returns a direction vector reflected by impact with a plane with the given normal vector
	 * @param v Direction of motion at impact
	 * @param n Normal of the face hit
	 * @param f Friction coefficient
	 * @param r Elasticity coefficient
	 * @return
	 */
	public static Vec3d reflect(Vec3d v, Vec3d n, double f, double r)
	{
		v = v.normalize();
		n = n.normalize();
		
		// Direction perpendicular to the impact face
		Vec3d u = n.multiply(v.dotProduct(n));
		// Direction parallel to the impact face
		Vec3d w = v.subtract(u);
		
		return (w.multiply(f)).subtract(u.multiply(r));
	}
	
	public static Vec3d reflect(Vec3d v, Vec3d n) { return reflect(v, n, 1D, 1D); }
	
	public static float lerpAngle(float angleOne, float angleTwo, float magnitude)
	{
		float f = (magnitude - angleTwo) % ((float)Math.PI * 2);
		if (f < (float)(-Math.PI))
			f += (float)Math.PI * 2;
		if (f >= (float)Math.PI)
			f -= (float)Math.PI * 2;
		return angleTwo + angleOne * f;
	}
}
