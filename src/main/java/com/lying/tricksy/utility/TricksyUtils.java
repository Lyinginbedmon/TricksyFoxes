package com.lying.tricksy.utility;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public class TricksyUtils
{
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
}
