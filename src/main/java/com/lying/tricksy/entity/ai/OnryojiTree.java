package com.lying.tricksy.entity.ai;

import java.util.Map;

import com.lying.tricksy.api.entity.ai.INodeIOValue.StaticValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerUntyped;
import com.lying.tricksy.entity.ai.node.subtype.ConditionMisc;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafArithmetic;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafSpecial;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class OnryojiTree
{
	public static class OnryojiWhiteboard extends LocalWhiteboard<EntityOnryoji>
	{
		public static final WhiteboardRef NEAREST_PLAYER = new WhiteboardRef("near_player", TFObjType.ENT, TFWhiteboards.LOCAL).noCache();
		public static final WhiteboardRef ALTITUDE = new WhiteboardRef("altitude", TFObjType.INT, TFWhiteboards.LOCAL).noCache();
		
		public static final WhiteboardRef MOVE_POS = new WhiteboardRef("move_pos", TFObjType.BLOCK, TFWhiteboards.LOCAL);
		public static final WhiteboardRef MOVE_DIS = new WhiteboardRef("move_dis", TFObjType.INT, TFWhiteboards.LOCAL);
		
		public OnryojiWhiteboard(EntityOnryoji tricksyIn)
		{
			super(tricksyIn);
		}
		
		public Whiteboard<?> build()
		{
			super.build();
			
			register(NEAREST_PLAYER, (mob) -> new WhiteboardObjEntity(mob.getWorld().getClosestPlayer(mob.getX(), mob.getY(), mob.getZ(), 32D, true)));
			register(ALTITUDE, (mob) ->
			{
				if(mob.isOnGround())
					return new WhiteboardObj.Int(0);
				
				BlockPos pos = mob.getBlockPos();
				if(pos.getY() > 256)
					pos = new BlockPos(pos.getX(), 256, pos.getZ());
				
				int altitude = 0;
				while(pos.getY() > -64 && mob.getWorld().getBlockState(pos.down()).isAir())
				{
					altitude++;
					pos = pos.down();
				}
				
				return new WhiteboardObj.Int(altitude);
			});
			
			register(MOVE_POS, (mob) -> new WhiteboardObjBlock(mob.getBlockPos()));
			register(MOVE_DIS, (mob) -> new WhiteboardObj.Int(0));
			
			return this;
		}
	}
	
	public static TreeNode<?> get()
	{
		return ControlFlowMisc.REACTIVE.create()
				.child(attackControl())
//				.child(DecoratorMisc.FORCE_SUCCESS.create()
//					.child(motionControl()))
				.child(DecoratorMisc.FORCE_SUCCESS.create()
					.child(LeafMisc.LOOK_AT.create(Map.of(CommonVariables.TARGET_ENT, new WhiteboardValue(OnryojiWhiteboard.NEAREST_PLAYER)))))
		;
	}
	
	private static TreeNode<?> attackControl()
	{
		return ControlFlowMisc.SEQUENCE.create().named(Text.literal("Attack control"))
				.child(LeafMisc.WAIT.create())
				.child(ControlFlowMisc.SELECTOR.create()
					.child(LeafSpecial.ONRYOJI_OFUDA.create())
//					.child(LeafSpecial.ONRYOJI_BALANCE.create())
					.child(LeafSpecial.ONRYOJI_FOXFIRE.create())
					.child(LeafSpecial.ONRYOJI_SECLUSION.create())
//					.child(LeafSpecial.ONRYOJI_COMMANDERS.create())
					);
	}
	
	private static TreeNode<?> motionControl()
	{
		return ControlFlowMisc.SELECTOR.create().named(Text.literal("Motion control"))
				.child(keepAway())
				.child(manageAltitude())
				.child(LeafMisc.WAIT.create());
	}
	
	/** If the nearest player is closer than 5 blocks, move to a random position that is further away */
	private static TreeNode<?> keepAway()
	{
		return ControlFlowMisc.SEQUENCE.create().named(Text.literal("Keep away"))
				.child(ConditionMisc.CLOSER_THAN.create(Map.of(
						CommonVariables.VAR_POS_A, new WhiteboardValue(OnryojiWhiteboard.NEAREST_PLAYER),
						CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(5)))).named(Text.literal("Player within 5 blocks")))
				.child(DecoratorMisc.RETRY.create(Map.of(CommonVariables.VAR_NUM, new StaticValue(new WhiteboardObj.Int(10))))
					.child(ControlFlowMisc.SEQUENCE.create()
						.child(LeafArithmetic.RANDOM_POS.create(Map.of(
							CommonVariables.X, new StaticValue(new WhiteboardObj.Int(4)),
							CommonVariables.Y, new StaticValue(new WhiteboardObj.Int(4)),
							CommonVariables.Z, new StaticValue(new WhiteboardObj.Int(4)),
							GetterHandlerUntyped.makeOutput(TFObjType.BLOCK), new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))).named(Text.literal("Get random position")).silent())
						.child(LeafArithmetic.ADD.create(Map.of(
							CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS),
							CommonVariables.VAR_B, new WhiteboardValue(LocalWhiteboard.SELF),
							CommonVariables.SUBTRACT, new StaticValue(new WhiteboardObj.Bool(false)),
							GetterHandlerUntyped.makeOutput(TFObjType.INT, TFObjType.BLOCK), new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))).silent())
						.child(DecoratorMisc.INVERTER.create()
							.child(ConditionMisc.CLOSER_THAN.create(Map.of(
								CommonVariables.VAR_POS_A, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS),
								CommonVariables.VAR_POS_B, new WhiteboardValue(OnryojiWhiteboard.NEAREST_PLAYER),
								CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(5))))))
						.child(ConditionMisc.CAN_PATH_TO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))))
				.child(LeafMisc.GOTO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))
				.child(LeafMisc.WAIT.create(Map.of(CommonVariables.VAR_NUM, new StaticValue(new WhiteboardObj.Int(3)))));
	}
	
	private static TreeNode<?> manageAltitude()
	{
		return ControlFlowMisc.SELECTOR.create().named(Text.literal("Altitude management"))
				.child(ControlFlowMisc.SEQUENCE.create().named(Text.literal("Descent")).discrete()
					.child(ControlFlowMisc.REACTIVE.create().named(Text.literal("Higher than 5 blocks"))
						.child(ConditionWhiteboard.GREATER_THAN.create(Map.of(
							CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.ALTITUDE), 
							CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Int(5)))))
						.child(LeafArithmetic.ADD.create(Map.of(
							CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.ALTITUDE),
							CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Int(4)),
							CommonVariables.SUBTRACT, new StaticValue(new WhiteboardObj.Bool(true)),
							GetterHandlerUntyped.makeOutput(TFObjType.INT, TFObjType.BLOCK), new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS))).silent())
						.child(ConditionWhiteboard.GREATER_THAN.create(Map.of(
								CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS),
								CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Int(2))))))
					.child(LeafArithmetic.OFFSET.create(Map.of(
						CommonVariables.VAR_A, new WhiteboardValue(LocalWhiteboard.SELF),
						CommonVariables.VAR_B, new WhiteboardValue(ConstantsWhiteboard.DIRECTIONS.get(Direction.DOWN)),
						CommonVariables.VAR_NUM, new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS),
						GetterHandlerUntyped.makeOutput(TFObjType.BLOCK), new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))).silent())
					.child(ConditionMisc.CAN_PATH_TO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))
					.child(LeafMisc.GOTO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))
					.child(LeafMisc.WAIT.create()))
				.child(ControlFlowMisc.SEQUENCE.create().named(Text.literal("Ascent")).discrete()
					.child(ControlFlowMisc.REACTIVE.create().named(Text.literal("Lower than 3 blocks"))
						.child(ConditionWhiteboard.LESS_THAN.create(Map.of(
							CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.ALTITUDE), 
							CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Int(3)))))
						.child(LeafArithmetic.ADD.create(Map.of(
							CommonVariables.VAR_A, new StaticValue(new WhiteboardObj.Int(4)),
							CommonVariables.VAR_B, new WhiteboardValue(OnryojiWhiteboard.ALTITUDE),
							CommonVariables.SUBTRACT, new StaticValue(new WhiteboardObj.Bool(true)),
							new WhiteboardRef("target_reference", TFObjType.INT), new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS))).silent())
						.child(ConditionWhiteboard.GREATER_THAN.create(Map.of(
							CommonVariables.VAR_A, new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS),
							CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Int(2))))))
					.child(LeafArithmetic.OFFSET.create(Map.of(
						CommonVariables.VAR_A, new WhiteboardValue(LocalWhiteboard.SELF),
						CommonVariables.VAR_B, new WhiteboardValue(ConstantsWhiteboard.DIRECTIONS.get(Direction.UP)),
						CommonVariables.VAR_NUM, new WhiteboardValue(OnryojiWhiteboard.MOVE_DIS),
						new WhiteboardRef("target_reference", TFObjType.BLOCK), new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))).silent())
					.child(ConditionMisc.CAN_PATH_TO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))
					.child(LeafMisc.GOTO.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(OnryojiWhiteboard.MOVE_POS))))
					.child(LeafMisc.WAIT.create()));
	}
}