package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2FluidTags;
import ic2.core.ref.Ic2Fluids;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;

import java.util.Iterator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@NotClassic
public class TileEntitySteamRepressurizer extends TileEntityInventory implements IHasGui
{
	private static Fluid detectedSteamFluid;
	protected int currentHeat;
	@GuiSynced
	protected final Ic2FluidTank output;
	@GuiSynced
	protected final Ic2FluidTank input;
	protected static final int CONSUMPTION = 10;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntitySteamRepressurizer(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STEAM_REPRESSURIZER, pos, state);
		this.input = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(Ic2Fluids.STEAM.still(), Ic2Fluids.SUPERHEATED_STEAM.still()));
		this.output = this.fluids.addTankExtract("output", 10000);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.currentHeat = nbt.getInt("heat");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("heat", this.currentHeat);
	}

	public static boolean hasSteam()
	{
		return getSteam() != null;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		Fluid steam = getSteam();
		if (steam != null)
		{
			if (this.input.getFluidAmount() >= 10)
			{
				if (this.currentHeat < this.input.getFluidAmount() / 10)
				{
					this.getHeat();
				}

				int amount = this.getOutput();

				while (this.currentHeat > 0 && this.input.getFluidAmount() >= 10 && this.canOutput(amount))
				{
					this.currentHeat--;
					this.input.drainMbUnchecked(10, false);
					this.output.fillMbUnchecked(Ic2FluidStack.create(steam, amount), false);
				}
			}
		}
	}

	protected void getHeat()
	{
		int aim = this.input.getFluidAmount() / 10;
		if (aim > 0)
		{
			Level world = this.getLevel();
			int targetHeat = aim;

			for (Direction dir : Util.ALL_DIRS)
			{
				if (world.getBlockEntity(this.worldPosition.relative(dir)) instanceof IHeatSource hs)
				{
					int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
					if (request > 0)
					{
						targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
						if (targetHeat <= 0)
						{
							break;
						}
					}
				}
			}

			this.currentHeat += aim - targetHeat;
		}
	}

	protected int getOutput()
	{
		assert !this.input.isEmpty();
		Fluid fluid = this.input.getFluidStack().getFluid();
		if (fluid == Ic2Fluids.STEAM.still())
		{
			return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSteam");
		} else if (fluid == Ic2Fluids.SUPERHEATED_STEAM.still())
		{
			return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSuperSteam");
		} else
		{
			throw new IllegalStateException("Unknown tank contents: " + fluid);
		}
	}

	protected boolean canOutput(int amount)
	{
		return this.output.fillMbUnchecked(Ic2FluidStack.create(getSteam(), amount), true) == amount;
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
	public boolean getGuiState(String name)
	{
		return "valid".equals(name) ? hasSteam() : super.getGuiState(name);
	}

	private static Fluid getSteam()
	{
		Fluid ret = detectedSteamFluid;
		if (ret == null)
		{
			var tag = net.minecraft.core.registries.BuiltInRegistries.FLUID.getTag(Ic2FluidTags.STEAM);
			if (tag.isPresent())
			{
				for (Holder<Fluid> entry : tag.get())
				{
					detectedSteamFluid = ret = entry.value();
					break;
				}
			}
		}

		return ret;
	}
}
