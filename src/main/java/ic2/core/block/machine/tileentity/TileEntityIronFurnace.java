package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ParticleUtil;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityIronFurnace extends TileEntityBase implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener
{
	public final InvSlotProcessableSmelting inputSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotConsumableFuel fuelSlot;
	@GuiSynced
	public int fuel = 0;
	@GuiSynced
	public int totalFuel = 0;
	@GuiSynced
	public short progress = 0;
	protected double xp = 0.0;
	public static final short operationLength = 160;

	public TileEntityIronFurnace(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.IRON_FURNACE, pos, state);
		this.inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
		this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, true);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.fuel = nbt.getInt("fuel");
		this.totalFuel = nbt.getInt("totalFuel");
		this.progress = nbt.getShort("progress");
		this.xp = nbt.getDouble("xp");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("fuel", this.fuel);
		nbt.putInt("totalFuel", this.totalFuel);
		nbt.putShort("progress", this.progress);
		nbt.putDouble("xp", this.xp);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.fuel <= 0 && this.canOperate())
		{
			this.fuel = this.totalFuel = this.fuelSlot.consumeFuel();
			if (this.fuel > 0)
			{
				needsInvUpdate = true;
			}
		}

		if (this.fuel > 0 && this.canOperate())
		{
			this.progress++;
			if (this.progress >= 160)
			{
				this.progress = 0;
				this.operate();
				needsInvUpdate = true;
			}
		} else
		{
			this.progress = 0;
		}

		if (this.fuel > 0)
		{
			this.fuel--;
			this.activate(false);
		} else
		{
			this.shutdown(false);
		}

		if (needsInvUpdate)
		{
			this.setChanged();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.getActive())
		{
			Level world = this.getLevel();
			ParticleUtil.showFurnaceFlames(world, this.worldPosition, this.getFacing());
			if (world.random.nextDouble() < 0.1)
			{
				world.playLocalSound(
					this.worldPosition.getX() + 0.5,
					this.worldPosition.getY(),
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

	public static double spawnXP(Player player, double xp)
	{
		Level world = player.getCommandSenderWorld();
		long balls = (long) Math.floor(xp);

		while (balls > 0L)
		{
			int amount;
			if (balls < 2477L)
			{
				amount = ExperienceOrb.getExperienceValue((int) balls);
			} else
			{
				amount = 2477;
			}

			balls -= amount;
			world.addFreshEntity(new ExperienceOrb(world, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, amount));
		}

		return xp - Math.floor(xp);
	}

	private void operate()
	{
		MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
		ItemStack output = result.getOutput();
		this.outputSlot.add(output);
		this.inputSlot.consume(result);
		this.xp = this.xp + result.getRecipe().getMetaData().getFloat("experience");
	}

	private boolean canOperate()
	{
		MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
		return result == null ? false : this.outputSlot.canAdd(result.getOutput());
	}

	public double getProgress()
	{
		return this.progress / 160.0;
	}

	public double getFuelRatio()
	{
		return this.fuel <= 0 ? 0.0 : (double) this.fuel / this.totalFuel;
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
		if (name.equals("fuel"))
		{
			return this.fuel == 0 ? 0.0 : (double) this.fuel / this.totalFuel;
		} else if (name.equals("progress"))
		{
			return this.progress == 0 ? 0.0 : this.progress / 160.0;
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0)
		{
			assert !this.getLevel().isClientSide;
			this.xp = spawnXP(player, this.xp);
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_IRON_OPERATE;
	}
}
