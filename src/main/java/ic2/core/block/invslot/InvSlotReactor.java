package ic2.core.block.invslot;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import net.minecraft.world.item.ItemStack;

public class InvSlotReactor extends InvSlot
{
	private final int rows = 6;
	private final int maxCols = 9;

	public InvSlotReactor(TileEntityNuclearReactorElectric base1, String name1, int count)
	{
		super(base1, name1, InvSlot.Access.IO, count);
		this.setStackSizeLimit(1);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return ((TileEntityNuclearReactorElectric) this.base).isUsefulItem(stack, true);
	}

	@Override
	public int size()
	{
		return ((TileEntityNuclearReactorElectric) this.base).getReactorSize() * 6;
	}

	public int rawSize()
	{
		return super.size();
	}

	@Override
	public ItemStack get(int index)
	{
		return super.get(this.mapIndex(index));
	}

	public ItemStack get(int x, int y)
	{
		return super.get(y * 9 + x);
	}

	@Override
	protected void putFromNBT(int index, ItemStack content)
	{
		super.putFromNBT(this.mapIndex(index), content);
	}

	@Override
	public void put(int index, ItemStack content)
	{
		super.put(this.mapIndex(index), content);
	}

	public void put(int x, int y, ItemStack content)
	{
		super.put(y * 9 + x, content);
	}

	private int mapIndex(int index)
	{
		int size = this.size();
		int cols = size / 6;
		if (index < size)
		{
			int row = index / cols;
			int col = index % cols;
			return row * 9 + col;
		} else
		{
			index -= size;
			int remCols = 9 - cols;
			int row = index / remCols;
			int col = cols + index % remCols;
			return row * 9 + col;
		}
	}
}
