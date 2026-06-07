package ic2.core.block.wiring.tileentity;

import com.mojang.math.Vector3f;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.wiring.ContainerChargepadBlock;
import ic2.core.init.Localization;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2Items;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileEntityChargepadBlock extends TileEntityElectricBlock
{
	private static final List<AABB> aabbs = Arrays.asList(new AABB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0));
	private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.2F, 0.2F, 1.0F), 1.0F);
	private int updateTicker;
	private Player player = null;
	public static byte redstoneModes = 2;

	public TileEntityChargepadBlock(
		BlockEntityType<? extends TileEntityChargepadBlock> type, BlockPos pos, BlockState state, int tier, int output, int maxStorage
	)
	{
		super(type, pos, state, tier, output, maxStorage);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(Direction.DOWN));
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing(), Direction.UP)), EnumSet.of(this.getFacing()));
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return aabbs;
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if (!this.getLevel().isClientSide && entity instanceof Player)
		{
			this.updatePlayer((Player) entity);
		}
	}

	private void updatePlayer(Player entity)
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
				this.setChanged();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		Level world = this.getLevel();
		RandomSource rnd = world.random;
		if (rnd.nextInt(8) == 0)
		{
			if (this.getActive())
			{
				for (int particles = 20; particles > 0; particles--)
				{
					double x = this.worldPosition.getX() + 0.0F + rnd.nextFloat();
					double y = this.worldPosition.getY() + 0.9F + rnd.nextFloat();
					double z = this.worldPosition.getZ() + 0.0F + rnd.nextFloat();
					world.addParticle(effect, x, y, z, 0.0, 0.1, 0.0);
				}
			}
		}
	}

	protected abstract void getItems(Player var1);

	@Override
	protected boolean shouldEmitRedstone()
	{
		return this.redstoneMode == 0 && this.getActive() || this.redstoneMode == 1 && !this.getActive();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(facing, Direction.UP)), EnumSet.of(facing));
		this.superSetFacing(world, facing);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerChargepadBlock(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerChargepadBlock(syncId, inventory, this);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		this.redstoneMode++;
		if (this.redstoneMode >= redstoneModes)
		{
			this.redstoneMode = 0;
		}

		IC2.sideProxy.messagePlayer(player, this.getRedstoneMode());
	}

	@Override
	public String getRedstoneMode()
	{
		return this.redstoneMode <= 1 && this.redstoneMode >= 0 ? Localization.translate("ic2.blockChargepad.gui.mod.redstone" + this.redstoneMode) : "";
	}

	protected void chargeItem(ItemStack stack, int chargeFactor)
	{
		if (stack.getItem() != Ic2Items.DEBUG_ITEM)
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
