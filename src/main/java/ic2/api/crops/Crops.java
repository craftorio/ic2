package ic2.api.crops;

import java.util.Collection;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Crops
{
	public static Crops instance;
	public static CropCard weed;

	public abstract void addBiomenutrientsBonus(BiomeDictionary.Type var1, int var2);

	public abstract void addBiomehumidityBonus(BiomeDictionary.Type var1, int var2);

	public abstract int getHumidityBiomeBonus(Biome var1);

	public abstract int getNutrientBiomeBonus(Biome var1);

	public abstract CropCard getCropCard(String var1, String var2);

	public abstract CropCard getCropCard(ItemStack var1);

	public abstract Collection<CropCard> getCrops();

	public abstract void registerCrop(CropCard var1);

	@SideOnly(Side.CLIENT)
	public abstract void registerCropTextures(Map<ResourceLocation, TextureAtlasSprite> var1);

	public abstract boolean registerBaseSeed(ItemStack var1, CropCard var2, int var3, int var4, int var5, int var6);

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
