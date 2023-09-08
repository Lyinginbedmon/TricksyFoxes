package com.lying.tricksy.entity.ai;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.LivingEntity;

/**
 * AI framework used by <@link ITricksyMob> in concert with one or more <@link Whiteboard>
 * @author Lying
 */
public class BehaviourTree
{
	public <T extends LivingEntity & ITricksyMob> void update(T tricksy, Whiteboard local, Whiteboard global)
	{
		// TODO Implement tree update procedure
	}
	
	/**
	 * TODO NODE TYPES
	 * 
	 * Root	- Always present, no parents, only children, variant of Selector.Sequential
	 * Leaf	- End point, no children, contains a single predefined action
	 * 		SubTree		- Predefined node encapsulating an additional behaviour tree, performs a larger generic behaviour
	 * Selector	- Contains a set of children
	 * 		Sequential	- Runs first child that returns running until it fails or succeeds, checked in order
	 * 		Random		- Runs first child that returns running until it fails or succeeds, checked in random order
	 * Decorator	- Performs a logic operation, only has one child
	 * 		Force failure	- Always returns failure
	 * 		Force success	- Always returns success
	 * 		Inverter		- Returns the opposite of the child node
	 * 		Repeat			- Runs the child N times until it fails
	 * 		Retry			- Runs the child N times until it succeeds
	 * 		Delay			- Runs the child after N ticks
	 * Sequence
	 * 		Sequential	- Performs children in order until total success or individual failure
	 * 		Reactive	- Performs all children at once until one fails
	 * 		Star		- Performs children repeatedly until each succeeds
	 * Parallel	- Performs all children at once until all succeed or fail
	 */
}
