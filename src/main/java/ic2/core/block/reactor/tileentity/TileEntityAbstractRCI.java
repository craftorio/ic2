package ic2.core.block.reactor.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityAbstractRCI extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui
{
	private TileEntityNuclearReactorElectric reactor;
	private final ItemStack target;
	private final double energyPerOperation = 1000.0;
	public final InvSlotConsumableItemStack inputSlot;
	public final InvSlotUpgrade upgradeSlot;

	public TileEntityAbstractRCI(ItemStack target, ItemStack coolant)
	{
		super(48000, 2);
		this.target = target;
		this.inputSlot = new InvSlotConsumableItemStack(this, "input", InvSlot.Access.I, 9, InvSlot.InvSide.ANY, coolant);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getWorld().isRemote)
		{
			this.updateEnergyFacings();
		}

		this.updateReactor();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (!this.inputSlot.isEmpty() && this.energy.getEnergy() >= 1000.0 && this.reactor != null)
		{
			this.setActive(true);
		} else
		{
			this.setActive(false);
		}

		if (this.getActive())
		{
			for (ItemStack comp : this.reactor.reactorSlot)
			{
				if (comp != null && StackUtil.checkItemEquality(comp, this.target))
				{
					ItemReactorCondensator cond = (ItemReactorCondensator) comp.getItem();
					if (cond.getDurabilityForDisplay(comp) > 0.85 && this.inputSlot.consume(1) != null && this.energy.useEnergy(1000.0))
					{
						cond.setCustomDamage(comp, 0);
						needsInvUpdate = true;
					}
				}
			}
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			super.markDirty();
		}
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.updateEnergyFacings();
		this.updateReactor();
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		super.setFacing(facing);
		this.updateEnergyFacings();
		this.updateReactor();
	}

	public void updateEnergyFacings()
	{
		World world = this.getWorld();
		Set<EnumFacing> ret = new HashSet<>();

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			TileEntity te = world.getTileEntity(this.pos.offset(facing));
			if (!(te instanceof TileEntityNuclearReactorElectric) && !(te instanceof TileEntityReactorChamberElectric))
			{
				ret.add(facing);
			}
		}

		this.energy.setDirections(ret, Collections.emptySet());
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming);
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
	public ContainerBase<TileEntityAbstractRCI> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityAbstractRCI>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	private void updateReactor()
	{
		World world = this.getWorld();
		if (!world.isAreaLoaded(this.pos, 2))
		{
			this.reactor = null;
		} else
		{
			TileEntity tileEntity = world.getTileEntity(this.pos.offset(this.getFacing().getOpposite()));
			if (tileEntity instanceof TileEntityNuclearReactorElectric)
			{
				this.reactor = (TileEntityNuclearReactorElectric) tileEntity;
			} else if (tileEntity instanceof TileEntityReactorChamberElectric)
			{
				this.reactor = ((TileEntityReactorChamberElectric) tileEntity).getReactorInstance();
			} else
			{
				this.reactor = null;
			}
		}
	}
}
