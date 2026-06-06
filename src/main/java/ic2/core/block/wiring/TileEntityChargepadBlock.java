package ic2.core.block.wiring;

import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.EntityIC2FX;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityChargepadBlock extends TileEntityElectricBlock
{
	private static final List<AxisAlignedBB> aabbs = Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0));
	private int updateTicker;
	private EntityPlayer player = null;
	public static final byte redstoneModes = 2;

	public TileEntityChargepadBlock(int tier1, int output1, int maxStorage1)
	{
		super(tier1, output1, maxStorage1);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(EnumFacing.DOWN));
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.superReadFromNBT(nbt);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing(), EnumFacing.UP)), EnumSet.of(this.getFacing()));
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		return aabbs;
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if (!this.getWorld().isRemote && entity instanceof EntityPlayer)
		{
			this.updatePlayer((EntityPlayer) entity);
		}
	}

	private void updatePlayer(EntityPlayer entity)
	{
		this.player = entity;
	}

	protected int getTickRate()
	{
		return 2;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (this.player != null && this.energy.getEnergy() >= 1.0)
			{
				if (!this.getActive())
				{
					this.setActive(true);
				}

				this.getItems(this.player);
				this.player = null;
				needsInvUpdate = true;
			} else if (this.getActive())
			{
				this.setActive(false);
				needsInvUpdate = true;
			}

			if (needsInvUpdate)
			{
				this.markDirty();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		World world = this.getWorld();
		Random rnd = world.rand;
		if (rnd.nextInt(8) == 0)
		{
			if (this.getActive())
			{
				ParticleManager effect = FMLClientHandler.instance().getClient().effectRenderer;

				for (int particles = 20; particles > 0; particles--)
				{
					double x = this.pos.getX() + 0.0F + rnd.nextFloat();
					double y = this.pos.getY() + 0.9F + rnd.nextFloat();
					double z = this.pos.getZ() + 0.0F + rnd.nextFloat();
					effect.addEffect(new EntityIC2FX(world, x, y, z, 60, new double[] { 0.0, 0.1, 0.0 }, new float[] { 0.2F, 0.2F, 1.0F }));
				}
			}
		}
	}

	protected abstract void getItems(EntityPlayer var1);

	@Override
	protected boolean shouldEmitRedstone()
	{
		return this.redstoneMode == 0 && this.getActive() || this.redstoneMode == 1 && !this.getActive();
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(facing, EnumFacing.UP)), EnumSet.of(facing));
		this.superSetFacing(facing);
	}

	@Override
	public ContainerBase<TileEntityChargepadBlock> getGuiContainer(EntityPlayer player)
	{
		return new ContainerChargepadBlock(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiChargepadBlock(new ContainerChargepadBlock(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		this.redstoneMode++;
		if (this.redstoneMode >= redstoneModes)
		{
			this.redstoneMode = 0;
		}

		IC2.platform.messagePlayer(player, this.getRedstoneMode());
	}

	@Override
	public String getRedstoneMode()
	{
		return this.redstoneMode <= 1 && this.redstoneMode >= 0 ? Localization.translate("ic2.blockChargepad.gui.mod.redstone" + this.redstoneMode) : "";
	}

	protected void chargeItem(ItemStack stack, int chargeFactor)
	{
		if (stack.getItem() != ItemName.debug_item.getInstance())
		{
			double freeAmount = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.energy.getSourceTier(), true, true);
			double charge = 0.0;
			if (freeAmount >= 0.0)
			{
				if (freeAmount >= chargeFactor * this.getTickRate())
				{
					charge = chargeFactor * this.getTickRate();
				} else
				{
					charge = freeAmount;
				}

				if (this.energy.getEnergy() < charge)
				{
					charge = this.energy.getEnergy();
				}

				this.energy.useEnergy(ElectricItem.manager.charge(stack, charge, this.energy.getSourceTier(), true, false));
			}
		}
	}
}
