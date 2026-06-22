package ic2.core.item.tfbp;

import ic2.api.item.ITerraformingBP;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class Tfbp extends Item implements ITerraformingBP
{
	private static final List<Tfbp> instances = new ArrayList<>();
	public final double consume;
	public final int range;
	final TerraformerBase logic;

	public static void init()
	{
		for (Tfbp instance : instances)
		{
			if (instance.logic != null)
			{
				instance.logic.init();
			}
		}
	}

	public Tfbp(Properties settings, double consume, int range, TerraformerBase logic)
	{
		super(settings);
		this.consume = consume;
		this.range = range;
		this.logic = logic;
		instances.add(this);
	}

	@Override
	public double getConsume(ItemStack stack)
	{
		return this.consume;
	}

	@Override
	public int getRange(ItemStack stack)
	{
		return this.range;
	}

	@Override
	public boolean canInsert(ItemStack stack, Player player, Level world, BlockPos pos)
	{
		if (this == Ic2Items.CULTIVATION_TFBP && world.dimension() == Level.END)
		{
			IC2.grantAdvancement(player, "ic2/build_generator/build_compressor/build_matter_gen/build_terraformer/terraform_end_cultivation");
		}

		return true;
	}

	@Override
	public boolean terraform(ItemStack stack, Level world, BlockPos pos)
	{
		return this.logic != null && this.logic.terraform(world, pos);
	}
}
