package ic2.core.item.tool;

import ic2.api.item.IMiningDrill;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDrill extends ItemElectricTool implements IMiningDrill, IHitSoundOverride
{
	public ItemDrill(ItemName name, int operationEnergyCost, HarvestLevel harvestLevel, int maxCharge, int transferLimit, int tier, float efficiency)
	{
		super(name, operationEnergyCost, harvestLevel, EnumSet.of(ToolClass.Pickaxe, ToolClass.Shovel));
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
		this.efficiency = efficiency;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack)
	{
		if (player.capabilities.isCreativeMode)
		{
			return null;
		}

		IBlockState state = world.getBlockState(pos);
		float hardness = state.getBlockHardness(world, pos);
		return !(hardness > 1.0F) && !(hardness < 0.0F) ? "Tools/Drill/DrillSoft.ogg" : "Tools/Drill/DrillHard.ogg";
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		float speed = super.getDestroySpeed(stack, state);
		EntityPlayer player = getPlayerHoldingItem(stack);
		if (player != null)
		{
			if (player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(player))
			{
				speed *= 5.0F;
			}

			if (!player.onGround)
			{
				speed *= 5.0F;
			}
		}

		return speed;
	}

	private static EntityPlayer getPlayerHoldingItem(ItemStack stack)
	{
		if (IC2.platform.isRendering())
		{
			EntityPlayer player = IC2.platform.getPlayerInstance();
			if (player != null && player.inventory.getCurrentItem() == stack)
			{
				return player;
			}
		} else
		{
			for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
			{
				if (player.inventory.getCurrentItem() == stack)
				{
					return player;
				}
			}
		}

		return null;
	}

	@Override
	public int energyUse(ItemStack stack, World world, BlockPos pos, IBlockState state)
	{
		if (stack.getItem() == ItemName.drill.getInstance())
		{
			return 6;
		} else if (stack.getItem() == ItemName.diamond_drill.getInstance())
		{
			return 20;
		} else if (stack.getItem() == ItemName.iridium_drill.getInstance())
		{
			return 200;
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}

	@Override
	public int breakTime(ItemStack stack, World world, BlockPos pos, IBlockState state)
	{
		if (stack.getItem() == ItemName.drill.getInstance())
		{
			return 200;
		} else if (stack.getItem() == ItemName.diamond_drill.getInstance())
		{
			return 50;
		} else if (stack.getItem() == ItemName.iridium_drill.getInstance())
		{
			return 20;
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}

	@Override
	public boolean breakBlock(ItemStack stack, World world, BlockPos pos, IBlockState state)
	{
		if (stack.getItem() == ItemName.drill.getInstance())
		{
			return this.tryUsePower(stack, 50.0);
		} else if (stack.getItem() == ItemName.diamond_drill.getInstance())
		{
			return this.tryUsePower(stack, 80.0);
		} else if (stack.getItem() == ItemName.iridium_drill.getInstance())
		{
			return this.tryUsePower(stack, 800.0);
		} else
		{
			throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
		}
	}
}
