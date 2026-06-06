package ic2.core.item.block;

import ic2.core.block.BlockMultiID;
import ic2.core.block.state.IIdProvider;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockMulti extends ItemBlockIC2
{
	public ItemBlockMulti(Block block)
	{
		super(block);
		this.setHasSubtypes(true);
	}

	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		String name = ((IIdProvider) (
			(Enum) this.block.getStateFromMeta(stack.getMetadata()).getValue(((BlockMultiID) this.block).getTypeProperty())
		))
			.getName();
		return super.getUnlocalizedName(stack) + "." + name;
	}
}
