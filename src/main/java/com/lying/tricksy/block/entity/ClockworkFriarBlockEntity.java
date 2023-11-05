package com.lying.tricksy.block.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.block.BlockClockworkFriar;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.ClockworkFriarScreenHandler;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ClockworkFriarBlockEntity extends LockableContainerBlockEntity implements Inventory
{
	public static final int TIME_TO_CRAFT = Reference.Values.TICKS_PER_SECOND * 1;
	
	private Identifier recipeId = null;
	private ItemStack recipeResult = ItemStack.EMPTY;
	
	private int ticksCrafting = 0;
	private DefaultedList<ItemStack> inventory = DefaultedList.<ItemStack>ofSize(9, ItemStack.EMPTY);
	private int craftTime = 0;
	private List<ItemStack> heldStacks = Lists.newArrayList();
	
	public ClockworkFriarBlockEntity(BlockPos pos, BlockState state)
	{
		super(TFBlockEntities.CLOCKWORK_FRIAR, pos, state);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.craftTime = nbt.getInt("CraftTicks");
		this.recipeId = nbt.contains("RecipeID", NbtElement.STRING_TYPE) ? new Identifier(nbt.getString("RecipeID")) : null;
		this.recipeResult = nbt.contains("Result", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(nbt.getCompound("Result")) : ItemStack.EMPTY;
		
		if(nbt.contains("HeldStacks", NbtElement.LIST_TYPE))
		{
			NbtList stacks = nbt.getList("HeldStacks", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<stacks.size(); i++)
			{
				ItemStack stack = ItemStack.fromNbt(stacks.getCompound(i));
				if(!stack.isEmpty())
					heldStacks.add(stack);
			}
		}
		
		Inventories.readNbt(nbt, inventory);
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("CraftTicks", craftTime);
		
		if(recipeId != null)
			nbt.putString("RecipeID", recipeId.toString());
		
		if(!recipeResult.isEmpty())
			nbt.put("Result", recipeResult.writeNbt(new NbtCompound()));
		
		if(!heldStacks.isEmpty())
		{
			NbtList stacks = new NbtList();
			heldStacks.forEach((stack) -> stacks.add(stack.writeNbt(new NbtCompound())));
			nbt.put("HeldStacks", stacks);
		}
		
		Inventories.writeNbt(nbt, inventory);
	}
	
	public void clear() { inventory.clear(); }
	
	public int size() { return 9; }
	
	public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
	
	public ItemStack getStack(int slot) { return inventory.get(slot); }
	
	public ItemStack removeStack(int slot, int amount)
	{
		ItemStack stackInSlot = Inventories.splitStack(inventory, slot, amount);
		if(!stackInSlot.isEmpty())
			markDirty();
		return stackInSlot;
	}
	
	public ItemStack removeStack(int slot) { markDirty(); return Inventories.removeStack(inventory, slot); }
	
	public void setStack(int slot, ItemStack stack) { inventory.set(slot, stack); markDirty(); }
	
	public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(this, player); }
	
	protected Text getContainerName() { return Text.translatable("block."+Reference.ModInfo.MOD_ID+".clockwork_friar"); }
	
	public Direction facing()
	{
		Optional<Direction> facing = world.getBlockState(pos).getOrEmpty(BlockClockworkFriar.FACING);
		return facing.isPresent() ? facing.get() : Direction.NORTH;
	}
	
	public void tryCraft()
	{
		if(canCraft() && !isCrafting())
		{
			craftTime = TIME_TO_CRAFT;
			consumeIngredients();
			setBlockCrafting(true);
			markDirty();
		}
	}
	
	public void doCraft()
	{
		if(world.isClient())
			return;
		
		heldStacks.add(getCraftResult());
		BlockPointerImpl pointer = new BlockPointerImpl((ServerWorld)world, pos);
		Direction direction = world.getBlockState(pos).get(BlockClockworkFriar.FACING);
		
		double d = pointer.getX() + 0.7D * (double)direction.getOffsetX();
		double e = pointer.getY() + 0.7D;
		double f = pointer.getZ() + 0.7D * (double)direction.getOffsetZ();
		Position output = new PositionImpl(d, e, f);
		
		heldStacks.forEach((stack) -> ItemDispenserBehavior.spawnItem(world, stack, 1, direction, output));
		heldStacks.clear();
		
		pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
	}
	
	private void setBlockCrafting(boolean crafting)
	{
		if(world.isClient() || world.getBlockState(pos).get(BlockClockworkFriar.CRAFTING) == crafting)
			return;
		world.setBlockState(pos, world.getBlockState(pos).with(BlockClockworkFriar.CRAFTING, crafting), Block.NOTIFY_ALL);
	}
	
	public static void tickClient(World world, BlockPos pos, BlockState state, ClockworkFriarBlockEntity blockEntity)
	{
		if(blockEntity.isCrafting())
		{
			if(blockEntity.ticksCrafting % 20 == 0)
				world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER, SoundCategory.BLOCKS, 1F, world.random.nextFloat(), true);
			blockEntity.ticksCrafting++;
		}
		else
			blockEntity.ticksCrafting = 0;
	}
	
	public static void tickServer(World world, BlockPos pos, BlockState state, ClockworkFriarBlockEntity blockEntity)
	{
		if(blockEntity.isCrafting())
		{
			if(--blockEntity.craftTime == 0)
			{
				blockEntity.doCraft();
				blockEntity.setBlockCrafting(false);
			}
			blockEntity.markDirty();
		}
		else if(blockEntity.recipeId != null && blockEntity.getCraftResult().isEmpty())
			blockEntity.setRecipe(blockEntity.recipeId);
	}
	
	public boolean isCrafting()
	{
		if(world.isClient())
		{
			BlockState state = world.getBlockState(pos);
			return state.isOf(TFBlocks.CLOCKWORK_FRIAR) ? state.get(BlockClockworkFriar.CRAFTING) : false;
		}
		return craftTime > 0;
	}
	
	public int ticksCrafting() { return ticksCrafting; }
	
	public void dropHeldStacks()
	{
		ItemScatterer.spawn(world, pos, inventory);
		if(heldStacks.isEmpty())
			return;
		
		Inventory inv = new SimpleInventory(heldStacks.size());
		for(int i=0; i<heldStacks.size(); i++)
			inv.setStack(i, heldStacks.get(i));
		ItemScatterer.spawn(world, pos, inv);
		
	}
	
	public boolean canCraft()
	{
		// Ensure that we have a viable recipe
		if(this.recipeId == null || recipeResult.isEmpty() || getRecipe() == null)
			return false;
		
		// Ensure necessary ingredients are accounted for in neighbouring inventories
		for(Entry<Ingredient, Integer> entry : getIngredients().entrySet())
		{
			Ingredient input = entry.getKey();
			int tally = entry.getValue();
			for(Direction face : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST})
			{
				BlockEntity ent = world.getBlockEntity(pos.offset(face));
				if(ent == null || !(ent instanceof Inventory))
					continue;
				
				tally -= tallyIngredientsInInventory((Inventory)ent, face, input, tally);
				if(tally <= 0)
					break;
			};
			
			if(tally > 0)
				return false;
		}
		
		return true;
	}
	
	/** Counts matching items in the given inventory from the given face, up to a given target amount */
	private static int tallyIngredientsInInventory(Inventory inventory, Direction face, Ingredient ingredient, int target)
	{
		if(inventory.isEmpty())
			return 0;
		
		int tally = target;
		if(inventory instanceof SidedInventory)
		{
			SidedInventory inv = (SidedInventory)inventory;
			int[] slots = inv.getAvailableSlots(face.getOpposite());
			if(slots.length == 0)
				return 0;
			else
				for(int slot : slots)
				{
					ItemStack stackInSlot = inv.getStack(slot);
					if(stackInSlot.isEmpty())
						continue;
					else if(ingredient.test(stackInSlot))
					{
						tally -= Math.min(tally, stackInSlot.getCount());
						if(tally == 0)
							break;
					}
				}
		}
		else
			for(int slot=0; slot<inventory.size(); slot++)
			{
				ItemStack stackInSlot = inventory.getStack(slot);
				if(stackInSlot.isEmpty())
					continue;
				else if(ingredient.test(stackInSlot))
				{
					tally -= Math.min(tally, stackInSlot.getCount());
					if(tally == 0)
						break;
				}
			}
		
		return target - tally;
	}
	
	private void consumeIngredients()
	{
		for(Entry<Ingredient, Integer> entry : getIngredients().entrySet())
		{
			int tally = entry.getValue();
			Ingredient input = entry.getKey();
			
			for(Direction face : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST})
			{
				BlockEntity ent = world.getBlockEntity(pos.offset(face));
				if(ent == null || !(ent instanceof Inventory))
					continue;
				
				Inventory inventory = (Inventory)ent;
				if(inventory.isEmpty())
					continue;
				
				if(inventory instanceof SidedInventory)
				{
					SidedInventory inv = (SidedInventory)inventory;
					for(int slot : inv.getAvailableSlots(face.getOpposite()))
					{
						ItemStack stackInSlot = inv.getStack(slot);
						if(stackInSlot.isEmpty())
							continue;
						else if(input.test(stackInSlot))
						{
							ItemStack consumed = stackInSlot.split(tally);
							if(!consumed.getRecipeRemainder().isEmpty())
								heldStacks.add(consumed.getRecipeRemainder().copyWithCount(consumed.getCount()));
							
							tally -= Math.min(tally, consumed.getCount());
							inv.markDirty();
							if(tally == 0)
								break;
						}
					}
				}
				else
					for(int slot=0; slot<inventory.size(); slot++)
					{
						ItemStack stackInSlot = inventory.getStack(slot);
						if(stackInSlot.isEmpty())
							continue;
						else if(input.test(stackInSlot))
						{
							ItemStack consumed = stackInSlot.split(tally);
							if(!consumed.getRecipeRemainder().isEmpty())
								heldStacks.add(consumed.getRecipeRemainder().copyWithCount(consumed.getCount()));
							
							tally -= Math.min(tally, consumed.getCount());
							inventory.markDirty();
							if(tally == 0)
								break;
						}
					}
				
				if(tally <= 0)
					break;
			};
		}
	}
	
	@Nullable
	private Recipe<?> getRecipe()
	{
		if(world.isClient())
			return null;
		
		RecipeManager manager = world.getServer().getRecipeManager();
		Optional<? extends Recipe<?>> recipeFile = manager.get(recipeId);
		return recipeFile.isPresent() ? recipeFile.get() : null;
	}
	
	public void setRecipe(Identifier recipeID)
	{
		if(world == null || world.isClient())
			return;
		
		this.recipeId = recipeID;
		Recipe<?> recipe = getRecipe();
		if(recipe != null)
		{
			ItemStack result = recipe.getOutput(world.getRegistryManager());
			this.recipeResult = result == null ? ItemStack.EMPTY : result;
		}
		else
			this.recipeId = null;
		
		markDirty();
	}
	
	public void markDirty()
	{
		super.markDirty();
		BlockState state = world.getBlockState(pos);
		world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
	}
	
	private Map<Ingredient, Integer> getIngredients()
	{
		Recipe<?> recipe = getRecipe();
		DefaultedList<Ingredient> ingredients = recipe.getIngredients();
		
		Map<Ingredient, Integer> inputCounts = new HashMap<>();
		if(recipe != null)
			for(Ingredient input : ingredients)
				if(!input.isEmpty())
					inputCounts.put(input, inputCounts.getOrDefault(input, 0) + 1);
		
		return inputCounts;
	}
	
	public ItemStack getCraftResult() { return recipeResult.copy(); }
	
	public BlockEntityUpdateS2CPacket toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
	
	@Override
	public NbtCompound toInitialChunkDataNbt()
	{
		return this.createNbt();
	}
	
	public void findAndSetRecipe()
	{
		if(world == null || world.isClient())
			return;
		
		RecipeInputInventory inv = TricksyUtils.ingredientsFromInventory(this);
		Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inv, world);
		optional.ifPresentOrElse((rec) -> {
			ItemStack result;
			if((result = rec.craft(inv, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures()))
			{
				this.recipeResult = result;
				this.recipeId = rec.getId();
			}
			else
			{
				this.recipeResult = ItemStack.EMPTY;
				this.recipeId = null;
			}
		}, () -> {
			this.recipeResult = ItemStack.EMPTY;
			this.recipeId = null;
		});
		markDirty();
	}
	
	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return new ClockworkFriarScreenHandler(syncId, playerInventory, pos, this);
	}
}
