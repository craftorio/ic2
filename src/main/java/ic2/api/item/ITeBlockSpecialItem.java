package ic2.api.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public interface ITeBlockSpecialItem
{
	boolean doesOverrideDefault(ItemStack paramItemStack);

	ModelResourceLocation getModelLocation(ItemStack paramItemStack);
}
