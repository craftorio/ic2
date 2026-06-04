package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.IKineticRotor;
import ic2.api.tile.IRotorProvider;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableKineticRotor;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.network.NetworkManager;
import ic2.core.profile.NotClassic;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWaterKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui
{
	public InvSlotConsumableClass rotorSlot;

	public BiomeState type;

	protected int updateTicker;

	private boolean rightFacing;

	private int distanceToNormalBiome;

	private int waterFlow;

	private long lastcheck;

	private float angle;

	private float rotationSpeed;

	public enum BiomeState
	{
		UNKNOWN, OCEAN, RIVER, INVALID
	}

	public TileEntityWaterKineticGenerator()
	{
		this.type = BiomeState.UNKNOWN;
		this.angle = 0.0F;
		this.updateTicker = IC2.random.nextInt(getTickRate());
		this.rotorSlot = new InvSlotConsumableKineticRotor(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WATER, "rotorSlot");
	}

	protected int getTickRate()
	{
		return 20;
	}

	protected void onLoaded()
	{
		super.onLoaded();
		updateSeaInfo();
	}

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % getTickRate() != 0)
			return;
		World world = getWorld();
		if (this.type == BiomeState.UNKNOWN)
		{
			Biome biome = BiomeUtil.getBiome(world, this.pos);
			if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN))
			{
				this.type = BiomeState.OCEAN;
			} else if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER))
			{
				this.type = BiomeState.RIVER;
			} else
			{
				this.type = BiomeState.INVALID;
				return;
			}
		}
		boolean nextActive = getActive();
		boolean needsInvUpdate = false;
		if (!this.rotorSlot.isEmpty() && checkSpace(1, true) == 0)
		{
			if (!nextActive)
				nextActive = needsInvUpdate = true;
		} else if (nextActive)
		{
			nextActive = false;
			needsInvUpdate = true;
		}
		if (nextActive)
		{
			int crossSection = Util.square(getRotorDiameter() / 2 * 2 * 2 + 1);
			int obstructedCrossSection = checkSpace(getRotorDiameter() * 3, false);
			if (obstructedCrossSection > 0 && obstructedCrossSection <= (getRotorDiameter() + 1) / 2)
				obstructedCrossSection = 0;
			int rotorDamage = 0;
			if (obstructedCrossSection < 0)
			{
				stopSpinning();
			} else if (this.type == BiomeState.OCEAN)
			{
				float diff = (float) Math.sin(world.getWorldTime() * Math.PI / 6000.0D);
				diff *= Math.abs(diff);
				this.rotationSpeed = (float) ((diff * this.distanceToNormalBiome / 100.0F) * (1.0D - Math.pow((double) obstructedCrossSection / crossSection, 2.0D)));
				this.waterFlow = (int) (this.rotationSpeed * 3000.0F);
				if (this.rightFacing)
					this.rotationSpeed *= -1.0F;
				IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
				this.waterFlow = (int) (this.waterFlow * getEfficiency());
				rotorDamage = 2;
			} else if (this.type == BiomeState.RIVER)
			{
				this.rotationSpeed = Util.limit(this.distanceToNormalBiome, 20, 50) / 50.0F;
				this.waterFlow = (int) (this.rotationSpeed * 1000.0F);
				if (getFacing() == EnumFacing.EAST || getFacing() == EnumFacing.NORTH)
					this.rotationSpeed *= -1.0F;
				IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
				this.waterFlow = (int) (this.waterFlow * getEfficiency() * (1.0F - 0.3F * world.rand.nextFloat() - 0.1F * obstructedCrossSection / crossSection));
				rotorDamage = 1;
			}
			this.rotorSlot.damage(rotorDamage, false);
		} else
		{
			stopSpinning();
		}
		setActive(nextActive);
		if (needsInvUpdate)
			markDirty();
	}

	protected void stopSpinning()
	{
		boolean update = (this.rotationSpeed != 0.0F);
		this.rotationSpeed = 0.0F;
		this.waterFlow = 0;
		if (update)
			IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
	}

	public void setFacing(EnumFacing side)
	{
		super.setFacing(side);
		updateSeaInfo();
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("rotationSpeed");
		ret.add("rotorSlot");
		return ret;
	}

	public int getRotorDiameter()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
		{
			if (this.type == BiomeState.OCEAN)
				return ((IKineticRotor) stack.getItem()).getDiameter(stack);
			return (((IKineticRotor) stack.getItem()).getDiameter(stack) + 1) * 2 / 3;
		}
		return 0;
	}

	public int checkSpace(int length, boolean onlyrotor)
	{
		int box = getRotorDiameter() / 2;
		int lentemp = 0;
		if (onlyrotor)
		{
			length = 1;
			lentemp = length + 1;
		} else
		{
			box *= 2;
		}
		EnumFacing fwdDir = getFacing();
		EnumFacing rightDir = fwdDir.rotateAround(EnumFacing.DOWN.getAxis());
		int ret = 0;
		int xCoord = this.pos.getX();
		int yCoord = this.pos.getY();
		int zCoord = this.pos.getZ();
		World world = getWorld();
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
					if (world.getBlockState(pos).getBlock() != Blocks.WATER)
					{
						occupied = true;
						if ((up != 0 || right != 0 || fwd != 0) && world.getTileEntity(pos) instanceof TileEntityWaterKineticGenerator && !onlyrotor)
							return -1;
					}
				}
				if (occupied)
					ret++;
			}
		}
		return ret;
	}

	public void updateSeaInfo()
	{
		World world = getWorld();
		EnumFacing facing = getFacing();
		for (int distance = 1; distance < 200; distance++)
		{
			Biome biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, distance));
			if (!isValidBiome(biomeTemp))
			{
				this.distanceToNormalBiome = distance;
				this.rightFacing = true;
				return;
			}
			biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, -distance));
			if (!isValidBiome(biomeTemp))
			{
				this.distanceToNormalBiome = distance;
				this.rightFacing = false;
				return;
			}
		}
		this.distanceToNormalBiome = 200;
		this.rightFacing = true;
	}

	public boolean isValidBiome(Biome biome)
	{
		return (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER));
	}

	public int maxrequestKineticEnergyTick(EnumFacing directionFrom)
	{
		return getConnectionBandwidth(directionFrom);
	}

	public int getConnectionBandwidth(EnumFacing side)
	{
		return (side.getOpposite() == getFacing()) ? getKuOutput() : 0;
	}

	public int requestKineticEnergy(EnumFacing directionFrom, int requestKineticEnergy)
	{
		return drawKineticEnergy(directionFrom, requestKineticEnergy, false);
	}

	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		if (side.getOpposite() == getFacing())
			return Math.min(request, getKuOutput());
		return 0;
	}

	public int getKuOutput()
	{
		if (getActive())
			return (int) Math.abs(this.waterFlow * outputModifier);
		return 0;
	}

	public float getEfficiency()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getEfficiency(stack);
		return 0.0F;
	}

	public ContainerWaterKineticGenerator getGuiContainer(EntityPlayer player)
	{
		return new ContainerWaterKineticGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiWaterKineticGenerator(getGuiContainer(player));
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}

	public String getRotorHealth()
	{
		if (!this.rotorSlot.isEmpty())
			return Localization.translate("ic2.WaterKineticGenerator.gui.rotorhealth", (int) (100.0F - (float) this.rotorSlot.get().getItemDamage() / this.rotorSlot.get().getMaxDamage() * 100.0F));
		return "";
	}

	public ResourceLocation getRotorRenderTexture()
	{
		ItemStack stack = this.rotorSlot.get();
		if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor)
			return ((IKineticRotor) stack.getItem()).getRotorRenderTexture(stack);
		return woodenRotorTexture;
	}

	public float getAngle()
	{
		if (this.rotationSpeed != 0.0F)
		{
			this.angle += (float) (System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed * 0.1F;
			this.angle %= 360.0F;
		}
		this.lastcheck = System.currentTimeMillis();
		return this.angle;
	}

	private static final float outputModifier = 0.2F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/water");

	private static final ResourceLocation woodenRotorTexture = new ResourceLocation("ic2", "textures/items/rotor/wood_rotor_model.png");
}
