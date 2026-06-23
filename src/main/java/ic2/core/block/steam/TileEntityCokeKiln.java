package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ParticleUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.RandomSource;

public class TileEntityCokeKiln extends TileEntityBase implements IHasGui, IGuiValueProvider
{
	protected static final int TICK_RATE = 20;
	protected int updateTicker;
	protected boolean isFormed = false;
	protected final InvSlotOutput outputSlot;
	protected static final List<CokeRecipe> recipes = new ArrayList<>();
	protected int progress = 0;
	protected int operationLength = 0;
	protected CokeRecipe currentRecipe = null;
	@GuiSynced
	protected float guiProgress;

	public TileEntityCokeKiln(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.COKE_KILN, pos, state);
		this.updateTicker = IC2.random.nextInt(TICK_RATE);
		this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.ANY);
	}

	static
	{
		// Coal → Coal Coke + 500mB Creosote
		recipes.add(new CokeRecipe(
			Items.COAL,
			new ItemStack(Ic2Items.COKE, 1),
			Ic2FluidStack.create(Ic2Fluids.CREOSOTE.still(), 500),
			1800,
			false
		));
		// LogWood → Charcoal + 250mB Creosote
		recipes.add(new CokeRecipe(
			null,
			new ItemStack(Items.CHARCOAL, 1),
			Ic2FluidStack.create(Ic2Fluids.CREOSOTE.still(), 250),
			1800,
			true
		));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getInt("progress");
		this.operationLength = nbt.getInt("operationLength");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("progress", this.progress);
		nbt.putInt("operationLength", this.operationLength);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % TICK_RATE == 0)
		{
			this.isFormed = this.hasValidStructure();
			if (!this.isFormed)
			{
				this.progress = 0;
				this.guiProgress = 0.0F;
				this.shutdown(false);
			}
			else
			{
				boolean needsInventoryUpdate = false;
				if (this.canWork())
				{
					this.activate(false);
					if (this.progress == 0)
					{
						needsInventoryUpdate = true;
					}

					int progressNeeded = this.currentRecipe.operationDuration;
					if (this.progress < progressNeeded)
					{
						this.progress += TICK_RATE;
					}

					if (this.progress >= progressNeeded)
					{
						this.finishWork();
						needsInventoryUpdate = true;
					}
				}
				else
				{
					this.shutdown(false);
				}

				if (this.progress != 0 && this.operationLength != 0)
				{
					this.guiProgress = (float) this.progress / this.operationLength;
				}
				else
				{
					this.guiProgress = 0.0F;
				}

				if (needsInventoryUpdate)
				{
					this.setChanged();
				}
			}
		}
	}

	protected boolean canWork()
	{
		// Find the hatch (above, opposite of facing)
		BlockPos hatchPos = new BlockPos(
			this.worldPosition.getX() - this.getFacing().getStepX(),
			this.worldPosition.getY() + 1,
			this.worldPosition.getZ() - this.getFacing().getStepZ()
		);
		BlockEntity hatch = this.level.getBlockEntity(hatchPos);
		if (!(hatch instanceof TileEntityCokeKilnHatch))
		{
			return false;
		}

		ItemStack input = ((TileEntityCokeKilnHatch) hatch).inventory.get();
		if (input.isEmpty())
		{
			return false;
		}

		// Validate or find recipe
		if (this.currentRecipe != null)
		{
			if (!this.currentRecipe.matches(input))
			{
				this.reset();
			}
		}

		if (this.currentRecipe != null)
		{
			// Check solid output
			if (this.currentRecipe.outputItem != null && !this.outputSlot.canAdd(this.currentRecipe.outputItem.copy()))
			{
				return false;
			}

			// Check fluid output
			if (this.currentRecipe.outputFluid != null)
			{
				BlockPos gratePos = new BlockPos(
					this.worldPosition.getX() - this.getFacing().getStepX(),
					this.worldPosition.getY() - 1,
					this.worldPosition.getZ() - this.getFacing().getStepZ()
				);
				BlockEntity grate = this.level.getBlockEntity(gratePos);
				if (!(grate instanceof TileEntityCokeKilnGrate))
				{
					return false;
				}

				Ic2FluidStack fs = this.currentRecipe.outputFluid.copy();
				int filled = ((TileEntityCokeKilnGrate) grate).fluidTank.fillMb(fs, false);
				if (filled < fs.getAmountMb())
				{
					return false;
				}
			}

			return true;
		}
		else
		{
			CokeRecipe found = findRecipe(input);
			if (found == null)
			{
				return false;
			}

			this.updateRecipe(found);

			if (found.outputItem != null && !this.outputSlot.canAdd(found.outputItem.copy()))
			{
				return false;
			}

			if (found.outputFluid != null)
			{
				BlockPos gratePos = new BlockPos(
					this.worldPosition.getX() - this.getFacing().getStepX(),
					this.worldPosition.getY() - 1,
					this.worldPosition.getZ() - this.getFacing().getStepZ()
				);
				BlockEntity grate = this.level.getBlockEntity(gratePos);
				if (!(grate instanceof TileEntityCokeKilnGrate))
				{
					return false;
				}

				Ic2FluidStack fs = found.outputFluid.copy();
				int filled = ((TileEntityCokeKilnGrate) grate).fluidTank.fillMb(fs, false);
				if (filled < fs.getAmountMb())
				{
					return false;
				}
			}

			return true;
		}
	}

	protected CokeRecipe findRecipe(ItemStack input)
	{
		for (CokeRecipe recipe : recipes)
		{
			if (recipe.matches(input))
			{
				return recipe;
			}
		}
		return null;
	}

	protected void finishWork()
	{
		BlockPos hatchPos = new BlockPos(
			this.worldPosition.getX() - this.getFacing().getStepX(),
			this.worldPosition.getY() + 1,
			this.worldPosition.getZ() - this.getFacing().getStepZ()
		);
		BlockEntity hatch = this.level.getBlockEntity(hatchPos);
		if (hatch instanceof TileEntityCokeKilnHatch)
		{
			InvSlot inventory = ((TileEntityCokeKilnHatch) hatch).inventory;
			if (!inventory.get().isEmpty())
			{
				// Consume one input item
				inventory.get().shrink(1);

				// Produce solid output
				if (this.currentRecipe.outputItem != null)
				{
					this.outputSlot.add(this.currentRecipe.outputItem.copy());
				}

				// Produce fluid output
				if (this.currentRecipe.outputFluid != null)
				{
					BlockPos gratePos = new BlockPos(
						this.worldPosition.getX() - this.getFacing().getStepX(),
						this.worldPosition.getY() - 1,
						this.worldPosition.getZ() - this.getFacing().getStepZ()
					);
					BlockEntity grate = this.level.getBlockEntity(gratePos);
					if (grate instanceof TileEntityCokeKilnGrate)
					{
						((TileEntityCokeKilnGrate) grate).fluidTank.fillMb(this.currentRecipe.outputFluid.copy(), false);
					}
				}

				this.progress = 0;
			}
		}
	}

	protected void updateRecipe(CokeRecipe recipe)
	{
		this.operationLength = recipe.operationDuration;
		this.currentRecipe = recipe;
	}

	protected void reset()
	{
		this.progress = 0;
		this.operationLength = 0;
		this.currentRecipe = null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
     RandomSource rng = RandomSource.create();
		super.updateEntityClient();
		if (this.getActive())
		{
			Level world = this.getLevel();
			ParticleUtil.showFurnaceFlames(world, this.worldPosition, this.getFacing());
			if (rng.nextDouble() < 0.1)
			{
				world.playLocalSound(
					this.worldPosition.getX() + 0.5,
					this.worldPosition.getY() + 0.5,
					this.worldPosition.getZ() + 0.5,
					SoundEvents.FURNACE_FIRE_CRACKLE,
					SoundSource.BLOCKS,
					1.0F,
					1.0F,
					false
				);
			}
		}
	}

	public boolean hasValidStructure()
	{
		Level world = this.getLevel();
		BlockPos pos = this.worldPosition;
		Direction facing = this.getFacing();
		int fDX = -facing.getStepX();
		int fDZ = -facing.getStepZ();

		// Bottom layer (y-1): 3x3, centre = Grate, rest = RefractoryBricks
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				BlockPos cPos = new BlockPos(pos.getX() + x + fDX, pos.getY() - 1, pos.getZ() + z + fDZ);
				if (x == 0 && z == 0)
				{
					if (!(world.getBlockEntity(cPos) instanceof TileEntityCokeKilnGrate))
					{
						return false;
					}
				}
				else
				{
					if (world.getBlockState(cPos).getBlock() != Ic2Blocks.REFRACTORY_BRICKS)
					{
						return false;
					}
				}
			}
		}

		// Middle layer (y=0): 3x3, centre = AIR, controller on outer edge
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				BlockPos cPos = new BlockPos(pos.getX() + x + fDX, pos.getY(), pos.getZ() + z + fDZ);
				if (x == 0 && z == 0)
				{
					if (!world.getBlockState(cPos).isAir())
					{
						return false;
					}
				}
				else if (cPos.equals(pos))
				{
					if (world.getBlockEntity(cPos) != this)
					{
						return false;
					}
				}
				else
				{
					if (world.getBlockState(cPos).getBlock() != Ic2Blocks.REFRACTORY_BRICKS)
					{
						return false;
					}
				}
			}
		}

		// Top layer (y+1): 3x3, centre = Hatch, rest = RefractoryBricks
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				BlockPos cPos = new BlockPos(pos.getX() + x + fDX, pos.getY() + 1, pos.getZ() + z + fDZ);
				if (x == 0 && z == 0)
				{
					if (!(world.getBlockEntity(cPos) instanceof TileEntityCokeKilnHatch))
					{
						return false;
					}
				}
				else
				{
					if (world.getBlockState(cPos).getBlock() != Ic2Blocks.REFRACTORY_BRICKS)
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, ic2.core.network.GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
		{
			return this.guiProgress;
		}
		throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return SoundEvents.FURNACE_FIRE_CRACKLE;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, java.util.List<String> info, TooltipFlag advanced)
	{
		info.add("");
		info.add("MultiBlock Structure:");
		info.add("");
		info.add(" Bottom Layer - 3x3 of Refractory Blocks with a Coke Kiln Grate in the centre");
		info.add("");
		info.add(" Middle Layer - 3x3 of Refractory Blocks with a hollow centre and this block in the middle of one of the sides");
		info.add("");
		info.add(" Top Layer - 3x3 of Refractory Blocks with a Coke Kiln Hatch in the centre");
		info.add("");
	}

	public static class CokeRecipe
	{
		public final net.minecraft.world.item.Item inputItem; // null for tag-based matching (logWood)
		public final ItemStack outputItem;
		public final Ic2FluidStack outputFluid;
		public final int operationDuration;
		public final boolean matchLogs;

		public CokeRecipe(net.minecraft.world.item.Item inputItem, ItemStack outputItem, Ic2FluidStack outputFluid, int operationDuration, boolean matchLogs)
		{
			this.inputItem = inputItem;
			this.outputItem = outputItem;
			this.outputFluid = outputFluid;
			this.operationDuration = operationDuration;
			this.matchLogs = matchLogs;
		}

		public boolean matches(ItemStack stack)
		{
			if (stack.isEmpty())
			{
				return false;
			}
			if (this.matchLogs)
			{
				return stack.is(ItemTags.LOGS);
			}
			if (this.inputItem != null)
			{
				return stack.getItem() == this.inputItem;
			}
			return false;
		}
	}
}
