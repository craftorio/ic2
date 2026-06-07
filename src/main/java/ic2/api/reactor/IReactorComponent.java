package ic2.api.reactor;

import net.minecraft.world.item.ItemStack;

public interface IReactorComponent extends IBaseReactorComponent
{
	void processChamber(ItemStack var1, IReactor var2, int var3, int var4, boolean var5);

	boolean acceptUraniumPulse(ItemStack var1, IReactor var2, ItemStack var3, int var4, int var5, int var6, int var7, boolean var8);

	boolean canStoreHeat(ItemStack var1, IReactor var2, int var3, int var4);

	int getMaxHeat(ItemStack var1, IReactor var2, int var3, int var4);

	int getCurrentHeat(ItemStack var1, IReactor var2, int var3, int var4);

	int alterHeat(ItemStack var1, IReactor var2, int var3, int var4, int var5);

	float influenceExplosion(ItemStack var1, IReactor var2);
}
