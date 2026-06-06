package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.init.Localization;
import ic2.core.item.ItemGradualInt;
import ic2.core.ref.ItemName;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractDamageableReactorComponent extends ItemGradualInt implements IReactorComponent
{
	protected AbstractDamageableReactorComponent(ItemName name, int maxDamage)
	{
		super(name, maxDamage);
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun)
	{
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		return false;
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return false;
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return 0;
	}

	@Override
	public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return 0;
	}

	@Override
	public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		return heat;
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return 0.0F;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, world, tooltip, advanced);
		tooltip.add(
			Localization.translate("ic2.reactoritem.durability")
				+ " "
				+ (this.getMaxCustomDamage(stack) - this.getCustomDamage(stack))
				+ "/"
				+ this.getMaxCustomDamage(stack)
		);
	}

	@Override
	public boolean canBePlacedIn(ItemStack stack, IReactor reactor)
	{
		return true;
	}
}
