package ic2.api.crops;

import ic2.core.proxy.EnvProxy;

import java.util.Collection;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;

public abstract class Crops
{
	public static Crops instance;
	public static CropCard weed;

	public abstract void addBiomenutrientsBonus(EnvProxy.BiomeType var1, int var2);

	public abstract void addBiomehumidityBonus(EnvProxy.BiomeType var1, int var2);

	public abstract int getHumidityBiomeBonus(Holder<Biome> var1);

	public abstract int getNutrientBiomeBonus(Holder<Biome> var1);

	public abstract CropCard getCropCard(String var1, String var2);

	public abstract CropCard getCropCard(ItemStack var1);

	public abstract CropCard getCropCard(Block var1);

	public abstract Collection<CropCard> getCrops();

	public abstract void registerCrop(CropCard var1);

	public abstract void registerBaseSeed(ItemStack var1, CropCard var2, int var3, int var4, int var5, int var6);

	public abstract BaseSeed getBaseSeed(ItemStack var1);

	public static class CropRegisterEvent extends Event
	{
		public void register(CropCard crop)
		{
			Crops.instance.registerCrop(crop);
		}

		public void register(CropCard... crops)
		{
			for (CropCard crop : crops)
			{
				this.register(crop);
			}
		}
	}
}
