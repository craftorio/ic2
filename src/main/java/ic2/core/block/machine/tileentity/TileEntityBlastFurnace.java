package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityBlastFurnace extends TileEntityInventory implements IUpgradableBlock, IHasGui, IGuiValueProvider
{
	public int heat = 0;
	public static int maxHeat = 50000;
	@GuiSynced
	public float guiHeat;
	protected final Redstone redstone;
	protected final Fluids fluids;
	protected int progress = 0;
	protected int progressNeeded = 300;
	@GuiSynced
	protected float guiProgress;
	public final InvSlotProcessableGeneric inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.blast_furnace);
	public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 2)
	{
		@Override
		public void onPickupFromSlot(Player player, ItemStack stack)
		{
			if (player != null && stack.getItem() == Ic2Items.STEEL_INGOT)
			{
				IC2.achievements.issueAchievement(player, "acquireRefinedIron");
			}
		}
	};
	public final InvSlotConsumableLiquidByList tankInputSlot = new InvSlotConsumableLiquidByList(this, "cellInput", 1, Ic2Fluids.AIR.still);
	public final InvSlotOutput tankOutputSlot = new InvSlotOutput(this, "cellOutput", 1);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
	@GuiSynced
	public final Ic2FluidTank fluidTank;

	public TileEntityBlastFurnace(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BLAST_FURNACE, pos, state);
		this.redstone = this.addComponent(new Redstone(this));
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(Ic2Fluids.AIR.still));
	}

	@Override
	public void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		this.heatup();
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.getOutput();
		if (result != null && this.isHot())
		{
			this.setActive(true);
			if (result.getRecipe().getMetaData().getInt("fluid") <= this.fluidTank.getFluidAmount())
			{
				this.progress++;
				this.fluidTank.drainMbUnchecked(result.getRecipe().getMetaData().getInt("fluid"), false);
			}

			this.progressNeeded = result.getRecipe().getMetaData().getInt("duration");
			if (this.progress >= result.getRecipe().getMetaData().getInt("duration"))
			{
				this.operateOnce(result, result.getOutput());
				needsInvUpdate = true;
				this.progress = 0;
			}
		} else
		{
			if (result == null)
			{
				this.progress = 0;
			}

			this.setActive(false);
		}

		if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
		{
			this.gainFluid();
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.progressNeeded;
		this.guiHeat = (float) this.heat / maxHeat;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult)
	{
		this.inputSlot.consume(result);
		this.outputSlot.add(processResult);
	}

	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput()
	{
		if (this.inputSlot.isEmpty())
		{
			return null;
		} else
		{
			MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
			if (output != null && output.getRecipe().getMetaData() != null)
			{
				return this.outputSlot.canAdd(output.getOutput()) ? output : null;
			} else
			{
				return null;
			}
		}
	}

	public boolean gainFluid()
	{
		return this.tankInputSlot.processIntoTank(this.fluidTank, this.tankOutputSlot);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.heat = nbt.getInt("heat");
		this.progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("heat", this.heat);
		nbt.putInt("progress", this.progress);
	}

	private void heatup()
	{
		int coolingPerTick = 1;
		int heatRequested = 0;
		int gainhU = 0;
		if ((!this.inputSlot.isEmpty() || this.progress >= 1) && this.heat <= maxHeat)
		{
			heatRequested = maxHeat - this.heat + 100;
		} else if (this.redstone.hasRedstoneInput() && this.heat <= maxHeat)
		{
			heatRequested = maxHeat - this.heat + 100;
		}

		if (heatRequested > 0)
		{
			Direction dir = this.getFacing();
			BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof IHeatSource)
			{
				gainhU = ((IHeatSource) te).drawHeat(dir.getOpposite(), heatRequested, false);
				this.heat += gainhU;
			}

			if (gainhU == 0)
			{
				this.heat = this.heat - Math.min(this.heat, 1);
			}
		} else
		{
			this.heat = this.heat - Math.min(this.heat, 1);
		}
	}

	public boolean isHot()
	{
		return this.heat >= maxHeat;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
		{
			return this.guiProgress;
		} else if (name.equals("heat"))
		{
			return this.guiHeat;
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming
		);
	}
}
