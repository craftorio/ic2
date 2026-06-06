package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.init.MainConfig;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityNuke extends TileEntityBridgeNuke implements IHasGui
{
	public int RadiationRange;
	public final InvSlotConsumable outsideSlot;
	public final InvSlotConsumable insideSlot = new InvSlotConsumableItemStack(
		this,
		"insideSlot",
		1,
		BlockName.resource.getItemStack(ResourceBlock.uranium_block),
		ItemName.nuclear.getItemStack(NuclearResourceType.uranium_238),
		ItemName.nuclear.getItemStack(NuclearResourceType.uranium_235),
		ItemName.nuclear.getItemStack(NuclearResourceType.small_uranium_235),
		ItemName.nuclear.getItemStack(NuclearResourceType.plutonium),
		ItemName.nuclear.getItemStack(NuclearResourceType.small_plutonium)
	);

	public static Class<? extends TileEntityBridgeNuke> delegate()
	{
		return IC2.version.isClassic() ? TileEntityBridgeNuke.TileEntityClassicNuke.class : TileEntityNuke.class;
	}

	public TileEntityNuke()
	{
		this.outsideSlot = new InvSlotConsumableItemStack(this, "outsideSlot", 1, this.getBlockType().getItemStack(TeBlock.itnt));
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
			ItemStack inside = this.insideSlot.get();
			int insideCount = StackUtil.getSize(inside);
			if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack(NuclearResourceType.uranium_238)))
			{
				this.setRadiationRange(itntCount);
			} else if (StackUtil.checkItemEquality(inside, BlockName.resource.getItemStack(ResourceBlock.uranium_block)))
			{
				this.setRadiationRange(itntCount * 6);
			} else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack(NuclearResourceType.small_uranium_235)))
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 64)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 1.6);
				}
			} else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack(NuclearResourceType.uranium_235)))
			{
				this.setRadiationRange(itntCount * 2);
				if (itntCount >= 32)
				{
					ret += 0.5 * Math.pow(insideCount, 1.4);
				}
			} else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack(NuclearResourceType.small_plutonium)))
			{
				this.setRadiationRange(itntCount * 3);
				if (itntCount >= 32)
				{
					ret += 0.05555555555555555 * Math.pow(insideCount, 2.0);
				}
			} else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack(NuclearResourceType.plutonium)))
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
	public ContainerBase<TileEntityNuke> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityNuke>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	protected void onIgnite(EntityLivingBase igniter)
	{
		super.onIgnite(igniter);
		this.outsideSlot.clear();
		this.insideSlot.clear();
	}
}
