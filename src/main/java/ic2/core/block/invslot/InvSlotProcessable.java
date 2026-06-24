package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public abstract class InvSlotProcessable<RI, RO, I> extends InvSlotConsumable
{
	protected Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> recipeManager;

	public InvSlotProcessable(IInventorySlotHolder<?> base, String name, int count, Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> recipeManager)
	{
		super(base, name, count);
		this.recipeManager = recipeManager;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		ItemStack tmp = StackUtil.copyWithSize(stack, Integer.MAX_VALUE);
		return this.getOutputFor(this.getInput(tmp), true) != null;
	}

	public MachineRecipeResult<RI, RO, I> process()
	{
		ItemStack input = this.get();
		return StackUtil.isEmpty(input) && !this.allowEmptyInput() ? null : this.getOutputFor(this.getInput(input), false);
	}

	public void consume(MachineRecipeResult<RI, RO, I> result)
	{
		if (result == null)
		{
			throw new NullPointerException("null result");
		}

		ItemStack input = this.get();
		if (StackUtil.isEmpty(input) && !this.allowEmptyInput())
		{
			throw new IllegalStateException("consume from empty slot");
		}

		this.setInput(result.adjustedInput());
	}

	public void setRecipeManager(Recipes.IGetter<? extends IMachineRecipeManager<RI, RO, I>> recipeManager)
	{
		this.recipeManager = recipeManager;
	}

	protected boolean allowEmptyInput()
	{
		return false;
	}

	protected MachineRecipeResult<RI, RO, I> getOutputFor(I input, boolean forAccept)
	{
		return this.recipeManager.get(this.base.getParent().getLevel()).apply(input, forAccept);
	}

	protected abstract I getInput(ItemStack var1);

	protected abstract void setInput(I var1);
}
