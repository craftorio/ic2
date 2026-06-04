package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public abstract class InvSlotProcessable<RI, RO, I> extends InvSlotConsumable
{
	protected IMachineRecipeManager<RI, RO, I> recipeManager;

	public InvSlotProcessable(IInventorySlotHolder<?> base, String name, int count, IMachineRecipeManager<RI, RO, I> recipeManager)
	{
		super(base, name, count);
		this.recipeManager = recipeManager;
	}

	public boolean accepts(ItemStack stack)
	{
		if (stack.getItem() instanceof ic2.core.item.upgrade.ItemUpgradeModule)
			return false;
		ItemStack tmp = StackUtil.copyWithSize(stack, 2147483647);
		return (getOutputFor(getInput(tmp), true) != null);
	}

	public MachineRecipeResult<RI, RO, I> process()
	{
		ItemStack input = get();
		if (StackUtil.isEmpty(input) && !allowEmptyInput())
			return null;
		return getOutputFor(getInput(input), false);
	}

	public void consume(MachineRecipeResult<RI, RO, I> result)
	{
		if (result == null)
			throw new NullPointerException("null result");
		ItemStack input = get();
		if (StackUtil.isEmpty(input) && !allowEmptyInput())
			throw new IllegalStateException("consume from empty slot");
		setInput(result.getAdjustedInput());
	}

	public void setRecipeManager(IMachineRecipeManager<RI, RO, I> recipeManager)
	{
		this.recipeManager = recipeManager;
	}

	protected boolean allowEmptyInput()
	{
		return false;
	}

	protected MachineRecipeResult<RI, RO, I> getOutputFor(I input, boolean forAccept)
	{
		return this.recipeManager.apply(input, forAccept);
	}

	protected abstract I getInput(ItemStack paramItemStack);

	protected abstract void setInput(I paramI);
}
