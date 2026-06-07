package ic2.core.block.invslot;

import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.item.DamageHandler;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class InvSlotConsumable extends InvSlot
{
	public InvSlotConsumable(IInventorySlotHolder<?> base, String name, int count)
	{
		super(base, name, InvSlot.Access.I, count, InvSlot.InvSide.TOP);
	}

	public InvSlotConsumable(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide preferredSide)
	{
		super(base, name, access, count, preferredSide);
	}

	@Override
	public abstract boolean accepts(ItemStack var1);

	@Override
	public boolean canOutput()
	{
		return super.canOutput() || this.access != InvSlot.Access.NONE && !this.isEmpty() && !this.accepts(this.get());
	}

	public ItemStack consume(int amount)
	{
		return this.consume(amount, false, false);
	}

	public ItemStack consume(int amount, boolean simulate, boolean consumeContainers)
	{
		ItemStack ret = null;

		for (int i = 0; i < this.size(); i++)
		{
			ItemStack stack = this.get(i);
			if (StackUtil.getSize(stack) >= 1
				&& this.accepts(stack)
				&& (ret == null || StackUtil.checkItemEqualityStrict(stack, ret))
				&& (StackUtil.getSize(stack) == 1 || consumeContainers || !IC2.envProxy.hasRecipeRemainder(stack)))
			{
				int currentAmount = Math.min(amount, StackUtil.getSize(stack));
				amount -= currentAmount;
				if (!simulate)
				{
					if (StackUtil.getSize(stack) == currentAmount)
					{
						if (!consumeContainers && IC2.envProxy.hasRecipeRemainder(stack))
						{
							ItemStack container = IC2.envProxy.getRecipeRemainder(stack);
							if (container != null && container.m_41763_() && DamageHandler.getDamage(container) > DamageHandler.getMaxDamage(container))
							{
								container = null;
							}

							this.put(i, container);
						} else
						{
							this.clear(i);
						}
					} else
					{
						this.put(i, StackUtil.decSize(stack, currentAmount));
					}
				}

				if (ret == null)
				{
					ret = StackUtil.copyWithSize(stack, currentAmount);
				} else
				{
					ret = StackUtil.incSize(ret, currentAmount);
				}

				if (amount == 0)
				{
					break;
				}
			}
		}

		return ret;
	}

	public int damage(int amount, boolean simulate)
	{
		return this.damage(amount, simulate, null);
	}

	public int damage(int amount, boolean simulate, LivingEntity src)
	{
		int damageApplied = 0;
		ItemStack target = null;

		for (int i = 0; i < this.size() && amount > 0; i++)
		{
			ItemStack stack = this.get(i);
			if (!StackUtil.isEmpty(stack))
			{
				Item item = stack.getItem();
				if (this.accepts(stack) && item.m_41465_() && (target == null || item == target.getItem() && ItemStack.m_41658_(stack, target)))
				{
					if (target == null)
					{
						target = stack.m_41777_();
					}

					if (simulate)
					{
						stack = stack.m_41777_();
					}

					int maxDamage = DamageHandler.getMaxDamage(stack);

					do
					{
						int currentAmount = Math.min(amount, maxDamage - DamageHandler.getDamage(stack));
						DamageHandler.damage(stack, currentAmount, src, null);
						damageApplied += currentAmount;
						amount -= currentAmount;
						if (DamageHandler.getDamage(stack) >= maxDamage)
						{
							stack = StackUtil.decSize(stack);
							if (StackUtil.isEmpty(stack))
							{
								break;
							}

							DamageHandler.setDamage(stack, 0, false);
						}
					} while (amount > 0 && !StackUtil.isEmpty(stack));

					if (!simulate)
					{
						this.put(i, stack);
					}
				}
			}
		}

		return damageApplied;
	}
}
