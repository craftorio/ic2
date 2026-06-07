package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityNuke extends TileEntityBridgeNuke implements IHasGui
{
	public int RadiationRange;
	public final InvSlotConsumable outsideSlot;
	public final InvSlotConsumable insideSlot = new InvSlotConsumableId(
		this,
		"insideSlot",
		1,
		Ic2Items.URANIUM_BLOCK,
		Ic2Items.URANIUM_238,
		Ic2Items.URANIUM_235,
		Ic2Items.SMALL_URANIUM_235,
		Ic2Items.PLUTONIUM,
		Ic2Items.SMALL_PLUTONIUM
	);

	public TileEntityNuke(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.NUKE, pos, state);
		this.outsideSlot = new InvSlotConsumableId(this, "outsideSlot", 1, Ic2Items.ITNT);
	}

	@Override
	public int getRadiationRange()
	{
		return this.RadiationRange;
	}

	public void setRadiationRange(int range)
	{
		if (range != this.RadiationRange)
		{
			this.RadiationRange = range;
		}
	}

	@Override
	public float getNukeExplosivePower()
	{
		if (this.outsideSlot.isEmpty())
		{
			return -1.0F;
		}

		int itntCount = StackUtil.getSize(this.outsideSlot.get());
		double ret = 5.0 * Math.pow(itntCount, 0.3333333333333333);
		if (this.insideSlot.isEmpty())
		{
			this.setRadiationRange(0);
		} else
		{
			ItemStack insideStack = this.insideSlot.get();
			Item inside = insideStack.getItem();
			int insideCount = StackUtil.getSize(insideStack);
			if (inside == Ic2Items.URANIUM_238)
			{
				this.setRadiationRange(itntCount);
			} else if (inside == Ic2Items.URANIUM_BLOCK)
			{
				this.setRadiationRange(itntCount * 6);
			} else if (inside == Ic2Items.SMALL_URANIUM_235)
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 64)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 1.6);
				}
			} else if (inside == Ic2Items.URANIUM_235)
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 32)
				{
					ret += 0.5 * Math.pow(insideCount, 1.4);
				}
			} else if (inside == Ic2Items.SMALL_PLUTONIUM)
			{
				this.setRadiationRange(itntCount * 3);
				if (itntCount >= 32)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 2.0);
				}
			} else if (inside == Ic2Items.PLUTONIUM)
			{
				this.setRadiationRange(itntCount * 4);
				if (itntCount >= 16)
				{
					ret += 0.5 * Math.pow(insideCount, 1.8);
				}
			}
		}

		ret = Math.min(ret, ConfigUtil.getFloat(MainConfig.get(), "protection/nukeExplosionPowerLimit"));
		return (float) ret;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	protected void onIgnite(LivingEntity igniter)
	{
		super.onIgnite(igniter);
		this.outsideSlot.clear();
		this.insideSlot.clear();
	}
}
