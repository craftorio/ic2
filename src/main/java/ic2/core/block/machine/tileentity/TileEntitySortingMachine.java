package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.block.machine.gui.GuiSortingMachine;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySortingMachine extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
	public static final int defaultTier = 2;
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlot buffer;
	private final ItemStack[][] filters;
	public EnumFacing defaultRoute = EnumFacing.DOWN;

	public TileEntitySortingMachine()
	{
		super(15000, 2, false);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
		this.buffer = new InvSlot(this, "Buffer", InvSlot.Access.IO, 11);
		this.filters = new ItemStack[6][7];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList filtersTag = nbt.getTagList("filters", 10);

		for (int i = 0; i < filtersTag.tagCount(); i++)
		{
			NBTTagCompound filterTag = filtersTag.getCompoundTagAt(i);
			int index = filterTag.getByte("index") & 255;
			ItemStack stack = new ItemStack(filterTag);
			this.filters[index / 7][index % 7] = stack;
		}

		int defaultRouteIdx = nbt.getByte("defaultroute");
		if (defaultRouteIdx >= 0 && defaultRouteIdx < EnumFacing.VALUES.length)
		{
			this.defaultRoute = EnumFacing.VALUES[defaultRouteIdx];
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList filtersTag = new NBTTagList();

		for (int i = 0; i < 42; i++)
		{
			ItemStack stack = this.filters[i / 7][i % 7];
			if (stack != null)
			{
				NBTTagCompound contentTag = new NBTTagCompound();
				contentTag.setByte("index", (byte) i);
				stack.writeToNBT(contentTag);
				filtersTag.appendTag(contentTag);
			}
		}

		nbt.setTag("filters", filtersTag);
		nbt.setByte("defaultroute", (byte) this.defaultRoute.ordinal());
		return nbt;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (IC2.platform.isSimulating())
		{
			this.setUpgradableBlock();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();

		label87:
		for (int index = 0; index < this.buffer.size(); index++)
		{
			if (this.energy.getEnergy() < 20.0)
			{
				return;
			}

			ItemStack stack = this.buffer.get(index);
			if (!StackUtil.isEmpty(stack))
			{
				for (StackUtil.AdjacentInv inv : StackUtil.getAdjacentInventories(this))
				{
					if (inv.dir != this.defaultRoute)
					{
						for (ItemStack filterStack : this.getFilterSlots(inv.dir))
						{
							if (!StackUtil.isEmpty(filterStack))
							{
								int filterSize = StackUtil.getSize(filterStack);
								if (StackUtil.getSize(stack) >= filterSize
									&& StackUtil.checkItemEquality(filterStack, stack)
									&& this.energy.canUseEnergy(filterSize * 20))
								{
									ItemStack transferStack = StackUtil.copyWithSize(stack, filterSize);
									int amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, true);
									if (amount == filterSize)
									{
										amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, false);
										stack = StackUtil.decSize(stack, amount);
										this.buffer.put(index, stack);
										this.energy.useEnergy(amount * 20);
										if (StackUtil.isEmpty(stack))
										{
											continue label87;
										}
									}
									break;
								}
							}
						}
					} else
					{
						boolean inFilter = false;
						ItemStack[][] amount = this.filters;
						int var7 = amount.length;
						int filterStack = 0;

						label68:
						while (true)
						{
							if (filterStack < var7)
							{
								ItemStack[] sideFilters = amount[filterStack];
								ItemStack[] transferStack = sideFilters;
								int amountx = transferStack.length;
								int var12 = 0;

								while (true)
								{
									if (var12 >= amountx)
									{
										filterStack++;
										continue label68;
									}

									ItemStack filter = transferStack[var12];
									if (StackUtil.checkItemEquality(filter, stack))
									{
										inFilter = true;
										break;
									}

									var12++;
								}
							}

							if (!inFilter)
							{
								int amountx = StackUtil.putInInventory(inv.te, inv.dir, StackUtil.copyWithSize(stack, 1), false);
								if (amountx > 0)
								{
									stack = StackUtil.decSize(stack, amountx);
									this.buffer.put(index, stack);
									this.energy.useEnergy(20.0);
									if (StackUtil.isEmpty(stack))
									{
									}
								}
								continue label87;
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		if (event >= 0 && event <= 5)
		{
			this.defaultRoute = EnumFacing.VALUES[event];
		}
	}

	@Override
	public ContainerBase<TileEntitySortingMachine> getGuiContainer(EntityPlayer player)
	{
		return new ContainerSortingMachine(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiSortingMachine(new ContainerSortingMachine(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer);
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
		{
			this.setUpgradableBlock();
		}
	}

	public void setUpgradableBlock()
	{
		this.energy.setSinkTier(this.upgradeSlot.getTier(2));
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	public ItemStack[] getFilterSlots(EnumFacing side)
	{
		return this.filters[side.ordinal()];
	}
}
