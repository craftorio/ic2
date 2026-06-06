package ic2.core.block.storage.tank;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.SoundType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityTank extends TileEntityInventory implements IHasGui
{
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final FluidTank contents;

	public TileEntityTank(int bucketMultiplier)
	{
		this.contents = this.fluidsComponent.addTank("contents", 1000 * bucketMultiplier);
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		return Collections.emptyList();
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.world.isRemote)
		{
			NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
			this.contents.readFromNBT(tag);
		}
	}

	@Override
	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		NBTTagCompound tag = StackUtil.getOrCreateNbtData(drop);
		if (this.contents.getFluidAmount() > 0)
		{
			this.contents.writeToNBT(tag);
		}

		return drop;
	}

	@Override
	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!this.world.isRemote && LiquidUtil.isFluidContainer(stack))
		{
			boolean changed = FluidUtil.interactWithFluidHandler(
				player, hand, this.fluidsComponent.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
			);
			if (changed)
			{
				this.markDirty();
			}

			return changed;
		} else
		{
			return super.onActivated(player, hand, side, hitX, hitY, hitZ);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, List<String> info, ITooltipFlag advanced)
	{
		info.add("Capacity: " + this.contents.getCapacity() + " mB");
		NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
		if (!tag.hasKey("Empty"))
		{
			FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag);
			if (fluidStack == null)
			{
				info.add("Empty");
			} else
			{
				info.add(fluidStack.getLocalizedName());
				info.add("Amount: " + fluidStack.amount + " mB");
				info.add("Type: " + (fluidStack.getFluid().isGaseous() ? "Gas" : "Liquid"));
			}
		} else
		{
			info.add("Empty");
		}
	}

	@Override
	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.METAL;
	}

	@Override
	public ContainerBase<? extends TileEntityTank> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityTank>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}
}
