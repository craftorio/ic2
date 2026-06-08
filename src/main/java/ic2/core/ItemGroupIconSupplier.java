package ic2.core;

import ic2.core.ref.Ic2Items;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ItemGroupIconSupplier implements Supplier<ItemStack>
{
	private static ItemStack a;
	private static ItemStack b;
	private static ItemStack z;
	private int ticker;
	private Ic2ItemGroupType groupType;

	public ItemGroupIconSupplier(Ic2ItemGroupType groupType)
	{
		this.groupType = groupType;
	}

	public ItemStack get()
	{
		if (this.groupType == Ic2ItemGroupType.GENERAL || this.groupType == Ic2ItemGroupType.TOOLS_AND_UTILITIES && IC2.seasonal)
		{
			if (a == null)
			{
				a = new ItemStack(Items.ZOMBIE_HEAD);
			}

			if (b == null)
			{
				b = new ItemStack(Items.SKELETON_SKULL);
			}

			if (z == null)
			{
				z = Ic2Items.NANO_CHESTPLATE.getDefaultInstance();
			}

			if (++this.ticker >= 5000)
			{
				this.ticker = 0;
			}

			if (this.ticker >= 2500)
			{
				return this.ticker < 3000 ? a : (this.ticker < 4500 ? b : z);
			}
		}
		return switch (this.groupType)
		{
			case GENERAL -> new ItemStack(Ic2Items.MACHINE);
			case REACTOR -> new ItemStack(Ic2Items.NUCLEAR_REACTOR);
			case MACHINES -> new ItemStack(Ic2Items.MACERATOR);
			case GENERATORS_AND_WIRING -> new ItemStack(Ic2Items.GENERATOR);
			case TOOLS_AND_UTILITIES -> new ItemStack(Ic2Items.WRENCH);
			case COMBAT -> new ItemStack(Ic2Items.NANO_SABER).setHoverName(Component.nullToEmpty("ic2:tab_icon"));
			case MATERIALS -> new ItemStack(Ic2Items.RUBBER);
			case FARMING -> new ItemStack(Ic2Items.CROP_SEED_BACK);
		};
	}
}
