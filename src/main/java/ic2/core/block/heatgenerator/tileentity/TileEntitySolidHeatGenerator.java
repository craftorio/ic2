package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.type.MiscResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySolidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui, IGuiValueProvider
{
	public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);

	public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);

	public int ticksSinceLastActiveUpdate = IC2.random.nextInt(256);

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (needsFuel())
			needsInvUpdate = gainFuel();
		boolean newActive = gainHeat();
		if (needsInvUpdate)
			markDirty();
		if (!delayActiveUpdate())
		{
			setActive(newActive);
		} else
		{
			if (this.ticksSinceLastActiveUpdate % 256 == 0)
			{
				setActive((this.activityMeter > 0));
				this.activityMeter = 0;
			}
			if (newActive)
			{
				this.activityMeter++;
			} else
			{
				this.activityMeter--;
			}
			this.ticksSinceLastActiveUpdate++;
		}
	}

	public boolean gainHeat()
	{
		if (isConverting())
		{
			this.heatBuffer += getMaxHeatEmittedPerTick();
			this.fuel--;
			if (this.fuel == 0 &&
				(int) (Math.random() * 2.0D) == 1)
				this.outputSlot.add(ItemName.misc_resource.getItemStack((Enum) MiscResourceType.ashes));
			return true;
		}
		return false;
	}

	public boolean needsFuel()
	{
		return (this.fuel <= 0 && getHeatBuffer() == 0);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fuel = nbt.getInteger("fuel");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("fuel", this.fuel);
		return nbt;
	}

	public boolean delayActiveUpdate()
	{
		return false;
	}

	public boolean gainFuel()
	{
		if (this.outputSlot.canAdd(ItemName.misc_resource.getItemStack((Enum) MiscResourceType.ashes)))
		{
			int fuelValue = this.fuelSlot.consumeFuel() / 4;
			if (fuelValue == 0)
				return false;
			this.fuel += fuelValue;
			this.itemFuelTime = fuelValue;
			return true;
		}
		return false;
	}

	public boolean isConverting()
	{
		return (this.fuel > 0);
	}

	protected int fillHeatBuffer(int maxAmount)
	{
		if (this.heatBuffer - maxAmount >= 0)
		{
			this.heatBuffer -= maxAmount;
			return maxAmount;
		}
		maxAmount = this.heatBuffer;
		this.heatBuffer = 0;
		return maxAmount;
	}

	public int getMaxHeatEmittedPerTick()
	{
		return emittedHU;
	}

	public ContainerBase<TileEntitySolidHeatGenerator> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}

	public double getGuiValue(String name)
	{
		if ("fuel".equals(name))
			return (this.fuel == 0) ? 0.0D : ((double) this.fuel / this.itemFuelTime);
		throw new IllegalArgumentException("Unexpected value requested: " + name);
	}

	private int heatBuffer = 0;

	public int activityMeter = 0;

	@GuiSynced
	public int fuel = 0;

	@GuiSynced
	public int itemFuelTime = 0;

	public static final int emittedHU = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/solid"));
}
