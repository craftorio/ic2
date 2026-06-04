package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.IKineticRotor;
import ic2.api.tile.IRotorProvider;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.WorldData;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableKineticRotor;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWindKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui
{
	private int updateTicker = IC2.random.nextInt(getTickRate());

	public final InvSlotConsumableClass rotorSlot = new InvSlotConsumableKineticRotor(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WIND, "rotorSlot");

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % getTickRate() != 0)
			return;
		boolean needsInvUpdate = false;
		boolean isActive = getActive();
		if (((hasRotor() && rotorHasSpace())) != isActive)
		{
			setActive(isActive = !isActive);
			needsInvUpdate = true;
		}
		if (isActive)
		{
			this.crossSection = Util.square(getRotorDiameter() / 2 * 2 * 2 + 1);
			this.obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
			if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (getRotorDiameter() + 1) / 2)
				this.obstructedCrossSection = 0;
			if (this.obstructedCrossSection < 0)
			{
				this.windStrength = 0.0D;
				setRotationSpeed(0.0F);
			} else
			{
				this.windStrength = calcWindStrength();
				float speed = (float) Util.limit((this.windStrength - getMinWindStrength()) / getMaxWindStrength(), 0.0D, 2.0D);
				setRotationSpeed(speed);
				if (this.windStrength >= getMinWindStrength())
				{
					if (this.windStrength <= getMaxWindStrength())
					{
						this.rotorSlot.damage(1, false);
					} else
					{
						this.rotorSlot.damage(4, false);
					}
					needsInvUpdate = true;
				}
			}
		} else
		{
			setRotationSpeed(0.0F);
		}
		if (needsInvUpdate)
			markDirty();
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("rotationSpeed");
		ret.add("rotorSlot");
		return ret;
	}

	public ContainerBase<TileEntityWindKineticGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerWindKineticGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiWindKineticGenerator(new ContainerWindKineticGenerator(player, this));
	}

	public boolean facingMatchesDirection(EnumFacing direction)
	{
		return (direction == getFacing());
	}

	public String getRotorHealth()
	{
		if (!this.rotorSlot.isEmpty())
			return Localization.translate("ic2.WindKineticGenerator.gui.rotorhealth", (int) (100.0F - (float) this.rotorSlot.get().getItemDamage() / this.rotorSlot.get().getMaxDamage() * 100.0F));
		return "";
	}

	public int maxrequestKineticEnergyTick(EnumFacing directionFrom)
	{
		return getConnectionBandwidth(directionFrom);
	}

	public int getConnectionBandwidth(EnumFacing side)
	{
		return facingMatchesDirection(side.getOpposite()) ? getKuOutput() : 0;
	}

	public int requestKineticEnergy(EnumFacing directionFrom, int requestKineticEnergy)
	{
		return drawKineticEnergy(directionFrom, requestKineticEnergy, false);
	}

	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		if (facingMatchesDirection(side.getOpposite()))
			return Math.min(request, getKuOutput());
		return 0;
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}

	public int checkSpace(int length, boolean onlyrotor)
	{
		int box = getRotorDiameter() / 2;
		int lentemp = 0;
		if (onlyrotor)
		{
			length = 1;
			lentemp = length + 1;
		}
		if (!onlyrotor)
			box *= 2;
		EnumFacing fwdDir = getFacing();
		EnumFacing rightDir = fwdDir.rotateAround(EnumFacing.DOWN.getAxis());
		int xMaxDist = Math.abs(length * fwdDir.getFrontOffsetX() + box * rightDir.getFrontOffsetX());
		int zMaxDist = Math.abs(length * fwdDir.getFrontOffsetZ() + box * rightDir.getFrontOffsetZ());
		ChunkCache chunkCache = new ChunkCache(getWorld(), this.pos.add(-xMaxDist, -box, -zMaxDist), this.pos.add(xMaxDist, box, zMaxDist), 0);
		int ret = 0;
		int xCoord = this.pos.getX();
		int yCoord = this.pos.getY();
		int zCoord = this.pos.getZ();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int up = -box; up <= box; up++)
		{
			int y = yCoord + up;
			for (int right = -box; right <= box; right++)
			{
				boolean occupied = false;
				for (int fwd = lentemp - length; fwd <= length; fwd++)
				{
					int x = xCoord + fwd * fwdDir.getFrontOffsetX() + right * rightDir.getFrontOffsetX();
					int z = zCoord + fwd * fwdDir.getFrontOffsetZ() + right * rightDir.getFrontOffsetZ();
					pos.setPos(x, y, z);
					assert Math.abs(x - xCoord) <= xMaxDist;
					assert Math.abs(z - zCoord) <= zMaxDist;
					IBlockState state = chunkCache.getBlockState(pos);
					Block block = state.getBlock();
					if (!block.isAir(state, chunkCache, pos))
					{
						occupied = true;
						if ((up != 0 || right != 0 || fwd != 0) && chunkCache.getTileEntity(pos) instanceof TileEntityWindKineticGenerator && !onlyrotor)
							return -1;
					}
				}
				if (occupied)
					ret++;
			}
		}
		return ret;
	}

	public boolean hasRotor()
	{
		return !this.rotorSlot.isEmpty();
	}

	public boolean rotorHasSpace()
	{
		return (checkSpace(1, true) == 0);
	}

	private void setRotationSpeed(float speed)
	{
		if (this.rotationSpeed != speed)
		{
			this.rotationSpeed = speed;
			IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
		}
	}

	public int getTickRate()
	{
		return 32;
	}

	public double calcWindStrength()
	{
		double windStr = (WorldData.get(getWorld())).windSim.getWindAt(this.pos.getY());
		windStr *= 1.0D - Math.pow((double) this.obstructedCrossSection / this.crossSection, 2.0D);
		return Math.max(0.0D, windStr);
	}

	public float getAngle()
	{
		if (this.rotationSpeed != 0.0F)
		{
			this.angle += (float) (System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed;
			this.angle %= 360.0F;
		}
		this.lastcheck = System.currentTimeMillis();
		return this.angle;
	}

	public float getEfficiency()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getEfficiency(stack);
		return 0.0F;
	}

	public int getMinWindStrength()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getMinWindStrength(stack);
		return 0;
	}

	public int getMaxWindStrength()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getMaxWindStrength(stack);
		return 0;
	}

	public int getRotorDiameter()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getDiameter(stack);
		return 0;
	}

	public ResourceLocation getRotorRenderTexture()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getRotorRenderTexture(stack);
		return woodenRotorTexture;
	}

	public boolean isRotorOverloaded()
	{
		return (hasRotor() && rotorHasSpace() && isWindStrongEnough() && this.windStrength > getMaxWindStrength());
	}

	public boolean isWindStrongEnough()
	{
		return (this.windStrength >= getMinWindStrength());
	}

	public int getKuOutput()
	{
		if (this.windStrength >= getMinWindStrength() && getActive())
			return (int) (this.windStrength * outputModifier * getEfficiency());
		return 0;
	}

	public int getObstructions()
	{
		return this.obstructedCrossSection;
	}

	public void setActive(boolean active)
	{
		if (active != getActive())
			IC2.network.get(true).updateTileEntityField(this, "rotorSlot");
		super.setActive(active);
	}

	private float angle = 0.0F;

	public static final float outputModifier = 10.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/wind");

	private static final ResourceLocation woodenRotorTexture = new ResourceLocation("ic2", "textures/items/rotor/wood_rotor_model.png");

	private double windStrength;

	private int obstructedCrossSection;

	private int crossSection;

	private float rotationSpeed;

	private long lastcheck;

}
