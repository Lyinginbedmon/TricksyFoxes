package com.lying.tricksy.utility;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

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
}
