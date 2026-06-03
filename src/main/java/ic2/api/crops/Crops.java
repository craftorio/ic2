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

public abstract class Crops {
  public static Crops instance;
  
  public static CropCard weed;
  
  public abstract void addBiomenutrientsBonus(BiomeDictionary.Type paramType, int paramInt);
  
  public abstract void addBiomehumidityBonus(BiomeDictionary.Type paramType, int paramInt);
  
  public abstract int getHumidityBiomeBonus(Biome paramBiome);
  
  public abstract int getNutrientBiomeBonus(Biome paramBiome);
  
  public abstract CropCard getCropCard(String paramString1, String paramString2);
  
  public abstract CropCard getCropCard(ItemStack paramItemStack);
  
  public abstract Collection<CropCard> getCrops();
  
  public abstract void registerCrop(CropCard paramCropCard);
  
  @SideOnly(Side.CLIENT)
  public abstract void registerCropTextures(Map<ResourceLocation, TextureAtlasSprite> paramMap);
  
  public abstract boolean registerBaseSeed(ItemStack paramItemStack, CropCard paramCropCard, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  public abstract BaseSeed getBaseSeed(ItemStack paramItemStack);
  
  public static class CropRegisterEvent extends Event {
    public void register(CropCard crop) {
      Crops.instance.registerCrop(crop);
    }
    
    public void register(CropCard... crops) {
      for (CropCard crop : crops)
        register(crop); 
    }
  }
}
