// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public abstract class Crops
{
    public static Crops instance;
    public static CropCard weed;
    
    public abstract void addBiomenutrientsBonus(final BiomeDictionary.Type p0, final int p1);
    
    public abstract void addBiomehumidityBonus(final BiomeDictionary.Type p0, final int p1);
    
    public abstract int getHumidityBiomeBonus(final Biome p0);
    
    public abstract int getNutrientBiomeBonus(final Biome p0);
    
    public abstract CropCard getCropCard(final String p0, final String p1);
    
    public abstract CropCard getCropCard(final ItemStack p0);
    
    public abstract Collection<CropCard> getCrops();
    
    public abstract void registerCrop(final CropCard p0);
    
    @SideOnly(Side.CLIENT)
    public abstract void registerCropTextures(final Map<ResourceLocation, TextureAtlasSprite> p0);
    
    public abstract boolean registerBaseSeed(final ItemStack p0, final CropCard p1, final int p2, final int p3, final int p4, final int p5);
    
    public abstract BaseSeed getBaseSeed(final ItemStack p0);
    
    public static class CropRegisterEvent extends Event
    {
        public void register(final CropCard crop) {
            Crops.instance.registerCrop(crop);
        }
        
        public void register(final CropCard... crops) {
            for (final CropCard crop : crops) {
                this.register(crop);
            }
        }
    }
}
