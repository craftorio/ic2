package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.IKineticRotor;
import ic2.api.tile.IRotorProvider;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableKineticRotor;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityWaterKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui
{
	public InvSlotConsumableClass rotorSlot;
	public TileEntityWaterKineticGenerator.BiomeState type = TileEntityWaterKineticGenerator.BiomeState.UNKNOWN;
	protected int updateTicker;
	private boolean rightFacing;
	private int distanceToNormalBiome;
	private int crossSection;
	private int obstructedCrossSection;
	private int waterFlow;
	private long lastcheck;
	private float angle = 0.0F;
	private float rotationSpeed;
	private static final float rotationModifier = 0.1F;
	private static final double efficiencyRollOffExponent = 2.0;
	private static final float outputModifier = 0.2F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/water");
	private static final ResourceLocation woodenRotorTexture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/items/rotor/wood_rotor_model.png");

	public TileEntityWaterKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.WATER_KINETIC_GENERATOR, pos, state);
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
		this.rotorSlot = new InvSlotConsumableKineticRotor(
			this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WATER, "rotorSlot"
		);
	}

	protected int getTickRate()
	{
		return 20;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSeaInfo();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			Level world = this.getLevel();
			if (this.type == TileEntityWaterKineticGenerator.BiomeState.UNKNOWN)
			{
				Holder<Biome> biome = BiomeUtil.getBiome(world, this.worldPosition);
				if (biome.m_203656_(BiomeTags.f_207603_))
				{
					this.type = TileEntityWaterKineticGenerator.BiomeState.OCEAN;
				} else if (biome.m_203656_(BiomeTags.f_207602_))
				{
					this.type = TileEntityWaterKineticGenerator.BiomeState.DEAP_OCEAN;
				} else
				{
					if (!biome.m_203656_(BiomeTags.f_207605_))
					{
						this.type = TileEntityWaterKineticGenerator.BiomeState.INVALID;
						return;
					}

					this.type = TileEntityWaterKineticGenerator.BiomeState.RIVER;
				}
			}

			boolean nextActive = this.getActive();
			boolean needsInvUpdate = false;
			if (!this.rotorSlot.isEmpty() && this.checkSpace(1, true) == 0)
			{
				if (!nextActive)
				{
					needsInvUpdate = true;
					nextActive = true;
				}
			} else if (nextActive)
			{
				nextActive = false;
				needsInvUpdate = true;
			}

			if (nextActive)
			{
				this.crossSection = Util.square(this.getRotorDiameter() / 2 * 2 * 2 + 1);
				this.obstructedCrossSection = this.checkSpace(this.getRotorDiameter() * 3, false);
				if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (this.getRotorDiameter() + 1) / 2)
				{
					this.obstructedCrossSection = 0;
				}

				int rotorDamage = 0;
				if (this.obstructedCrossSection < 0)
				{
					this.stopSpinning();
				} else if (this.type == TileEntityWaterKineticGenerator.BiomeState.OCEAN)
				{
					float diff = (float) Math.sin(world.m_46468_() * Math.PI / 6000.0);
					diff *= Math.abs(diff);
					this.rotationSpeed = (float) (
						diff * this.distanceToNormalBiome / 100.0F * (1.0 - Math.pow((double) this.obstructedCrossSection / this.crossSection, 2.0))
					);
					this.waterFlow = (int) (this.rotationSpeed * 3000.0F);
					if (this.rightFacing)
					{
						this.rotationSpeed *= -1.0F;
					}

					IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
					this.waterFlow = (int) (this.waterFlow * this.getEfficiency());
					rotorDamage = 2;
				} else if (this.type == TileEntityWaterKineticGenerator.BiomeState.DEAP_OCEAN)
				{
					float diff = (float) Math.sin(world.m_46468_() * Math.PI / 6000.0);
					diff *= Math.abs(diff);
					this.rotationSpeed = (float) (
						diff * this.distanceToNormalBiome / 100.0F * (1.0 - Math.pow((double) this.obstructedCrossSection / this.crossSection, 2.0))
					);
					this.waterFlow = (int) (this.rotationSpeed * 4000.0F);
					if (this.rightFacing)
					{
						this.rotationSpeed *= -1.0F;
					}

					IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
					this.waterFlow = (int) (this.waterFlow * this.getEfficiency());
					rotorDamage = 3;
				} else if (this.type == TileEntityWaterKineticGenerator.BiomeState.RIVER)
				{
					this.rotationSpeed = Util.limit(this.distanceToNormalBiome, 20, 50) / 50.0F;
					this.waterFlow = (int) (this.rotationSpeed * 1000.0F);
					if (this.getFacing() == Direction.EAST || this.getFacing() == Direction.NORTH)
					{
						this.rotationSpeed *= -1.0F;
					}

					IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
					this.waterFlow = (int) (
						this.waterFlow
							* (this.getEfficiency() * (1.0F - 0.3F * world.random.nextFloat() - 0.1F * ((float) this.obstructedCrossSection / this.crossSection)))
					);
					rotorDamage = 1;
				}

				this.rotorSlot.damage(rotorDamage, false);
			} else
			{
				this.stopSpinning();
			}

			this.setActive(nextActive);
			if (needsInvUpdate)
			{
				this.setChanged();
			}
		}
	}

	protected void stopSpinning()
	{
		boolean update = this.rotationSpeed != 0.0F;
		this.rotationSpeed = 0.0F;
		this.waterFlow = 0;
		if (update)
		{
			IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
		}
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateSeaInfo();
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("rotationSpeed");
		ret.add("rotorSlot");
		return ret;
	}

	@Override
	public int getRotorDiameter()
	{
		ItemStack stack = this.rotorSlot.get();
		if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof IKineticRotor))
		{
			return 0;
		} else
		{
			return this.type == TileEntityWaterKineticGenerator.BiomeState.OCEAN
				? ((IKineticRotor) stack.getItem()).getDiameter(stack)
				: (((IKineticRotor) stack.getItem()).getDiameter(stack) + 1) * 2 / 3;
		}
	}

	public int checkSpace(int length, boolean onlyrotor)
	{
		int box = this.getRotorDiameter() / 2;
		int lentemp = 0;
		if (onlyrotor)
		{
			length = 1;
			lentemp = length + 1;
		} else
		{
			box *= 2;
		}

		Direction fwdDir = this.getFacing();
		Direction rightDir = fwdDir.m_175362_(Axis.Y);
		int ret = 0;
		int xCoord = this.worldPosition.getX();
		int yCoord = this.worldPosition.getY();
		int zCoord = this.worldPosition.getZ();
		Level world = this.getLevel();
		MutableBlockPos pos = new MutableBlockPos();

		for (int up = -box; up <= box; up++)
		{
			int y = yCoord + up;

			for (int right = -box; right <= box; right++)
			{
				boolean occupied = false;

				for (int fwd = lentemp - length; fwd <= length; fwd++)
				{
					int x = xCoord + fwd * fwdDir.m_122429_() + right * rightDir.m_122429_();
					int z = zCoord + fwd * fwdDir.m_122431_() + right * rightDir.m_122431_();
					pos.set(x, y, z);
					if (world.getBlockState(pos).getBlock() != Blocks.f_49990_)
					{
						occupied = true;
						if ((up != 0 || right != 0 || fwd != 0) && world.getBlockEntity(pos) instanceof TileEntityWaterKineticGenerator && !onlyrotor)
						{
							return -1;
						}
					}
				}

				if (occupied)
				{
					ret++;
				}
			}
		}

		return ret;
	}

	public void updateSeaInfo()
	{
		Level world = this.getLevel();
		Direction facing = this.getFacing();

		for (int distance = 1; distance < 200; distance++)
		{
			Holder<Biome> biomeTemp = BiomeUtil.getBiome(world, this.worldPosition.m_5484_(facing, distance));
			if (!this.isValidBiome(biomeTemp))
			{
				this.distanceToNormalBiome = distance;
				this.rightFacing = true;
				return;
			}

			biomeTemp = BiomeUtil.getBiome(world, this.worldPosition.m_5484_(facing, -distance));
			if (!this.isValidBiome(biomeTemp))
			{
				this.distanceToNormalBiome = distance;
				this.rightFacing = false;
				return;
			}
		}

		this.distanceToNormalBiome = 200;
		this.rightFacing = true;
	}

	public boolean isValidBiome(Holder<Biome> biome)
	{
		return biome.m_203656_(BiomeTags.f_207605_) || biome.m_203656_(BiomeTags.f_207603_) || biome.m_203656_(BiomeTags.f_207602_);
	}

	@Override
	public int maxrequestkineticenergyTick(Direction directionFrom)
	{
		return this.getConnectionBandwidth(directionFrom);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return side.m_122424_() == this.getFacing() ? this.getKuOutput() : 0;
	}

	@Override
	public int requestkineticenergy(Direction directionFrom, int requestkineticenergy)
	{
		return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		return side.m_122424_() == this.getFacing() ? Math.min(request, this.getKuOutput()) : 0;
	}

	public int getKuOutput()
	{
		return this.getActive() ? (int) Math.abs(this.waterFlow * outputModifier) : 0;
	}

	public float getEfficiency()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor) stack.getItem()).getEfficiency(stack) : 0.0F;
	}

	@Override
	public ContainerBase<TileEntityWaterKineticGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerWaterKineticGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerWaterKineticGenerator(syncId, inventory, this);
	}

	public String getRotorHealth()
	{
		return !this.rotorSlot.isEmpty()
			? Localization.translate(
			"ic2.WaterKineticGenerator.gui.rotorhealth", (int) (100.0F - (float) this.rotorSlot.get().getDamageValue() / this.rotorSlot.get().m_41776_() * 100.0F)
		)
			: "";
	}

	@Override
	public ResourceLocation getRotorRenderTexture()
	{
		ItemStack stack = this.rotorSlot.get();
		return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor
			? ((IKineticRotor) stack.getItem()).getRotorRenderTexture(stack)
			: woodenRotorTexture;
	}

	@Override
	public float getAngle()
	{
		if (this.rotationSpeed != 0.0F)
		{
			this.angle = this.angle + (float) (System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed * 0.1F;
			this.angle %= 360.0F;
		}

		this.lastcheck = System.currentTimeMillis();
		return this.angle;
	}

	public enum BiomeState
	{
		UNKNOWN,
		OCEAN,
		DEAP_OCEAN,
		RIVER,
		INVALID;
	}
}
