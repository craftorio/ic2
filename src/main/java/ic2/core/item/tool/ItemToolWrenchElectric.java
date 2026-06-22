package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.item.PriorityUsableItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class ItemToolWrenchElectric extends ItemElectricTool implements PriorityUsableItem, IBoxable
{
	public ItemToolWrenchElectric(Properties settings)
	{
		super(settings, 100);
		this.tier = 1;
		this.maxCharge = 12000;
		this.transferLimit = 250;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		if (!this.canTakeDamage(stack, 1.0))
		{
			return InteractionResult.FAIL;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		int useResult = ItemToolWrench.onWrenchUse(player, context, this.canTakeDamage(stack, 10.0));
		switch (useResult)
		{
			case -2:
				return InteractionResult.PASS;
			case -1:
				return InteractionResult.FAIL;
			default:
				this.consumeEnergy(stack, useResult, player);
				return InteractionResult.SUCCESS;
		}
	}

	public boolean canTakeDamage(ItemStack stack, double amount)
	{
		amount *= 100.0;
		return ElectricItem.manager.getCharge(stack) >= amount;
	}

	@Override
	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		double operationEnergyCost = 100.0 * amount;
		return super.consumeEnergy(stack, operationEnergyCost, entity);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return true;
	}
}
