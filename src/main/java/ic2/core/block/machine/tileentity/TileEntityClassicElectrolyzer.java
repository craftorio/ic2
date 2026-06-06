package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.item.type.CellType;
import ic2.core.network.GuiSynced;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@TeBlock.Delegated(current = TileEntityElectrolyzer.class, old = TileEntityClassicElectrolyzer.class)
public class TileEntityClassicElectrolyzer extends TileEntityInventory implements IHasGui
{
	public TileEntityElectricBlock mfe = null;
	public int ticker = IC2.random.nextInt(16);
	public final InvSlotConsumableItemStack waterSlot = new InvSlotConsumableItemStack(
		this, "water", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, ItemName.cell.getItemStack(CellType.water)
	);
	public final InvSlotConsumableItemStack hydrogenSlot = new InvSlotConsumableItemStack(
		this, "hydrogen", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, ItemName.cell.getItemStack(CellType.electrolyzed_water)
	);
	protected AudioSource audio;
	@GuiSynced
	protected final Energy energy = this.addComponent(new Energy(this, 20000.0, Util.noFacings, Util.noFacings, 1));

	public TileEntityClassicElectrolyzer()
	{
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.getWorld().isRemote)
		{
			this.audio = IC2.audioManager.createSource(this, "Machines/ElectrolyzerLoop.ogg");
		}
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (this.audio != null)
		{
			IC2.audioManager.removeSources(this);
			this.audio = null;
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		boolean turnActive = false;
		if (++this.ticker % 16 == 0)
		{
			this.mfe = this.lookForMFE();
		}

		if (this.mfe != null)
		{
			if (this.shouldDrain() && this.canDrain())
			{
				needsInvUpdate |= this.drain();
				turnActive = true;
			}

			if (this.shouldPower() && (this.canPower() || this.energy.getEnergy() > 0.0))
			{
				needsInvUpdate |= this.power();
				turnActive = true;
			}

			this.setActive(turnActive);
			if (needsInvUpdate)
			{
				this.markDirty();
			}
		}
	}

	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.ticker++ % 32 == 0 && this.audio != null)
		{
			this.audio.stop();
			if (this.getActive())
			{
				this.audio.play();
			}
		}
	}

	public boolean shouldDrain()
	{
		return this.mfe != null && this.mfe.energy.getFillRatio() >= 0.7;
	}

	public boolean shouldPower()
	{
		return this.mfe != null && this.mfe.energy.getFillRatio() <= 0.3;
	}

	public boolean canDrain()
	{
		return this.waterSlot.consume(1, true, false) != null
			&& (
			this.hydrogenSlot.isEmpty()
				|| StackUtil.getSize(this.hydrogenSlot.get()) < Math.min(this.hydrogenSlot.getStackSizeLimit(), this.hydrogenSlot.get().getMaxStackSize())
		);
	}

	public boolean canPower()
	{
		return this.hydrogenSlot.consume(1, true, false) != null
			&& (
			this.waterSlot.isEmpty()
				|| StackUtil.getSize(this.waterSlot.get()) < Math.min(this.waterSlot.getStackSizeLimit(), this.waterSlot.get().getMaxStackSize())
		);
	}

	public boolean drain()
	{
		double amount = this.processRate();
		if (!this.mfe.energy.useEnergy(amount))
		{
			return false;
		}

		this.energy.addEnergy(amount);
		if (this.energy.useEnergy(20000.0))
		{
			this.waterSlot.consume(1);
			if (this.hydrogenSlot.isEmpty())
			{
				this.hydrogenSlot.put(ItemName.cell.getItemStack(CellType.electrolyzed_water));
			} else
			{
				this.hydrogenSlot.put(StackUtil.incSize(this.hydrogenSlot.get()));
			}

			return true;
		} else
		{
			return false;
		}
	}

	public boolean power()
	{
		if (this.energy.getEnergy() > 0.0)
		{
			double out = Math.min(this.energy.getEnergy(), this.processRate());
			this.energy.useEnergy(out);
			this.mfe.energy.addEnergy(out);
			return false;
		}

		this.energy.forceAddEnergy(12000 + 2000 * this.mfe.energy.getSinkTier());
		this.hydrogenSlot.consume(1);
		if (this.waterSlot.isEmpty())
		{
			this.waterSlot.put(ItemName.cell.getItemStack(CellType.water));
		} else
		{
			this.waterSlot.put(StackUtil.incSize(this.waterSlot.get()));
		}

		return true;
	}

	public int processRate()
	{
		switch (this.mfe.energy.getSinkTier())
		{
			case 2:
				return 8;
			case 3:
				return 32;
			case 4:
				return 128;
			default:
				return 2;
		}
	}

	public TileEntityElectricBlock lookForMFE()
	{
		World world = this.getWorld();

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity te = world.getTileEntity(this.pos.offset(dir));
			if (te instanceof TileEntityElectricBlock)
			{
				return (TileEntityElectricBlock) te;
			}
		}

		return null;
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityClassicElectrolyzer>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}
}
