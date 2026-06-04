// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import ic2.core.crop.cropcard.CropBeetroot;
import ic2.core.crop.cropcard.CropEating;
import ic2.core.crop.cropcard.CropPotato;
import ic2.core.crop.cropcard.CropCarrots;
import ic2.core.crop.cropcard.CropHops;
import ic2.core.crop.cropcard.CropCoffee;
import ic2.core.crop.cropcard.CropRedWheat;
import ic2.core.crop.cropcard.CropBaseMetalUncommon;
import ic2.core.crop.cropcard.CropBaseMetalCommon;
import ic2.core.crop.cropcard.CropBaseSapling;
import ic2.core.crop.cropcard.CropTerraWart;
import ic2.core.crop.cropcard.CropNetherWart;
import ic2.core.crop.cropcard.CropBaseMushroom;
import ic2.core.crop.cropcard.CropFlax;
import ic2.core.crop.cropcard.CropCocoa;
import ic2.core.crop.cropcard.CropStickreed;
import ic2.core.crop.cropcard.CropReed;
import ic2.core.crop.cropcard.CropVenomilia;
import ic2.core.crop.cropcard.CropColorFlower;
import ic2.core.crop.cropcard.CropMelon;
import ic2.core.crop.cropcard.CropPumpkin;
import ic2.core.crop.cropcard.CropWheat;
import java.util.function.Function;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import java.util.Locale;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import java.util.NoSuchElementException;
import java.util.AbstractCollection;
import java.util.Collection;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Iterator;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ic2.core.item.type.CropResItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.init.Items;
import ic2.api.crops.CropProperties;
import ic2.core.crop.cropcard.GenericCropCard;
import ic2.core.crop.cropcard.CropWeed;
import java.util.HashMap;
import java.util.IdentityHashMap;
import ic2.api.crops.CropCard;
import ic2.api.crops.BaseSeed;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;
import java.util.Map;
import ic2.api.crops.Crops;

public class IC2Crops extends Crops
{
    private final Map<BiomeDictionary.Type, Integer> humidityBiomeTypeBonus;
    private final Map<BiomeDictionary.Type, Integer> nutrientBiomeTypeBonus;
    private final Map<ItemStack, BaseSeed> baseSeeds;
    public static CropCard cropWheat;
    public static CropCard cropPumpkin;
    public static CropCard cropMelon;
    public static CropCard cropYellowFlower;
    public static CropCard cropRedFlower;
    public static CropCard cropBlackFlower;
    public static CropCard cropPurpleFlower;
    public static CropCard cropBlueFlower;
    public static CropCard cropVenomilia;
    public static CropCard cropReed;
    public static CropCard cropStickReed;
    public static CropCard cropCocoa;
    public static CropCard cropFlax;
    public static CropCard cropRedMushroom;
    public static CropCard cropBrownMushroom;
    public static CropCard cropNetherWart;
    public static CropCard cropTerraWart;
    public static CropCard cropOakSapling;
    public static CropCard cropSpruceSapling;
    public static CropCard cropBirchSapling;
    public static CropCard cropJungleSapling;
    public static CropCard cropAcaciaSapling;
    public static CropCard cropDarkOakSapling;
    public static CropCard cropFerru;
    public static CropCard cropCyprium;
    public static CropCard cropStagnium;
    public static CropCard cropPlumbiscus;
    public static CropCard cropAurelia;
    public static CropCard cropShining;
    public static CropCard cropRedwheat;
    public static CropCard cropCoffee;
    public static CropCard cropHops;
    public static CropCard cropCarrots;
    public static CropCard cropPotato;
    public static CropCard cropEatingPlant;
    public static CropCard cropBeetroot;
    static boolean needsToPost;
    private final Map<String, Map<String, CropCard>> cropMap;
    
    public IC2Crops() {
        this.humidityBiomeTypeBonus = new IdentityHashMap<BiomeDictionary.Type, Integer>();
        this.nutrientBiomeTypeBonus = new IdentityHashMap<BiomeDictionary.Type, Integer>();
        this.baseSeeds = new HashMap<ItemStack, BaseSeed>();
        this.cropMap = new HashMap<String, Map<String, CropCard>>();
    }
    
    public static void init() {
        Crops.instance = new IC2Crops();
        Crops.weed = new CropWeed();
        Crops.instance.addBiomehumidityBonus(BiomeDictionary.Type.WATER, 10);
        Crops.instance.addBiomehumidityBonus(BiomeDictionary.Type.WET, 10);
        Crops.instance.addBiomehumidityBonus(BiomeDictionary.Type.DRY, -10);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.JUNGLE, 10);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.SWAMP, 10);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.MUSHROOM, 5);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.FOREST, 5);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.RIVER, 2);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.PLAINS, 0);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.SAVANNA, -2);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.HILLS, -5);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.MOUNTAIN, -5);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.WASTELAND, -8);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.END, -10);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.NETHER, -10);
        Crops.instance.addBiomenutrientsBonus(BiomeDictionary.Type.DEAD, -10);
        registerCrops();
        registerBaseSeeds();
    }
    
    public static void registerCrops() {
        Crops.instance.registerCrop(IC2Crops.weed);
        Crops.instance.registerCrop(IC2Crops.cropWheat);
        Crops.instance.registerCrop(IC2Crops.cropPumpkin);
        Crops.instance.registerCrop(IC2Crops.cropMelon);
        Crops.instance.registerCrop(IC2Crops.cropYellowFlower);
        Crops.instance.registerCrop(IC2Crops.cropRedFlower);
        Crops.instance.registerCrop(IC2Crops.cropBlackFlower);
        Crops.instance.registerCrop(IC2Crops.cropPurpleFlower);
        Crops.instance.registerCrop(IC2Crops.cropBlueFlower);
        Crops.instance.registerCrop(IC2Crops.cropVenomilia);
        Crops.instance.registerCrop(IC2Crops.cropReed);
        Crops.instance.registerCrop(IC2Crops.cropStickReed);
        Crops.instance.registerCrop(IC2Crops.cropCocoa);
        Crops.instance.registerCrop(IC2Crops.cropFlax);
        Crops.instance.registerCrop(IC2Crops.cropFerru);
        Crops.instance.registerCrop(IC2Crops.cropAurelia);
        Crops.instance.registerCrop(IC2Crops.cropRedwheat);
        Crops.instance.registerCrop(IC2Crops.cropNetherWart);
        Crops.instance.registerCrop(IC2Crops.cropTerraWart);
        Crops.instance.registerCrop(IC2Crops.cropCoffee);
        Crops.instance.registerCrop(IC2Crops.cropHops);
        Crops.instance.registerCrop(IC2Crops.cropCarrots);
        Crops.instance.registerCrop(IC2Crops.cropPotato);
        Crops.instance.registerCrop(IC2Crops.cropRedMushroom);
        Crops.instance.registerCrop(IC2Crops.cropBrownMushroom);
        Crops.instance.registerCrop(IC2Crops.cropEatingPlant);
        Crops.instance.registerCrop(IC2Crops.cropCyprium);
        Crops.instance.registerCrop(IC2Crops.cropStagnium);
        Crops.instance.registerCrop(IC2Crops.cropPlumbiscus);
        Crops.instance.registerCrop(IC2Crops.cropShining);
        Crops.instance.registerCrop(IC2Crops.cropBeetroot);
        Crops.instance.registerCrop(IC2Crops.cropOakSapling);
        Crops.instance.registerCrop(IC2Crops.cropSpruceSapling);
        Crops.instance.registerCrop(IC2Crops.cropBirchSapling);
        Crops.instance.registerCrop(IC2Crops.cropJungleSapling);
        Crops.instance.registerCrop(IC2Crops.cropAcaciaSapling);
        Crops.instance.registerCrop(IC2Crops.cropDarkOakSapling);
        GenericCropCard.create("blazereed").setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Fire", "Blaze", "Reed", "Sulfur" }).setMaxSize(4).setDrops(new ItemStack(Items.BLAZE_POWDER)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BLAZE_ROD), ItemName.dust.getItemStack(DustResourceType.sulfur) }).setAfterHarvestSize(1).register();
        GenericCropCard.create("bobs_yer_uncle_ranks_berries").setDiscoveredBy("GenerikB").setProperties(new CropProperties(11, 4, 0, 8, 2, 9)).setAttributes(new String[] { "Shiny", "Vine", "Emerald", "Berylium", "Crystal" }).setMaxSize(4).setDrops(ItemName.crop_res.getItemStack(CropResItemType.bobs_yer_uncle_ranks_berry)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.EMERALD) }).setAfterHarvestSize(1).register();
        GenericCropCard.create("corium").setDiscoveredBy("Gregorius Techneticies").setProperties(new CropProperties(6, 0, 2, 3, 1, 0)).setAttributes(new String[] { "Cow", "Silk", "Vine" }).setMaxSize(4).setDrops(new ItemStack(Items.LEATHER)).setAfterHarvestSize(1).register();
        GenericCropCard.create("corpse_plant").setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(5, 0, 2, 1, 0, 3)).setAttributes(new String[] { "Toxic", "Undead", "Vine", "Edible", "Rotten" }).setMaxSize(4).setDrops(new ItemStack(Items.ROTTEN_FLESH)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BONE), new ItemStack(Items.DYE, 1, 15), new ItemStack(Items.DYE, 1, 15) }).setAfterHarvestSize(1).register();
        GenericCropCard.create("creeper_weed").setDiscoveredBy("General Spaz").setProperties(new CropProperties(7, 3, 0, 5, 1, 3)).setAttributes(new String[] { "Creeper", "Vine", "Explosive", "Fire", "Sulfur", "Saltpeter", "Coal" }).setMaxSize(4).setDrops(new ItemStack(Items.GUNPOWDER)).setAfterHarvestSize(1).register();
        GenericCropCard.create("diareed").setDiscoveredBy("Diareed").setProperties(new CropProperties(12, 5, 0, 10, 2, 10)).setAttributes(new String[] { "Fire", "Shiny", "Reed", "Coal", "Diamond", "Crystal" }).setMaxSize(4).setDrops(ItemName.dust.getItemStack(DustResourceType.small_diamond)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.DIAMOND) }).setAfterHarvestSize(1).register();
        GenericCropCard.create("egg_plant").setDiscoveredBy("Link").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Chicken", "Egg", "Edible", "Feather", "Flower", "Addictive" }).setMaxSize(3).setDrops(new ItemStack(Items.EGG)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.CHICKEN), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER) }).setGrowthSpeed(900).setAfterHarvestSize(2).register();
        GenericCropCard.create("ender_blossom").setDiscoveredBy("RichardG").setProperties(new CropProperties(10, 5, 0, 2, 1, 6)).setAttributes(new String[] { "Ender", "Flower", "Shiny" }).setMaxSize(4).setDrops(ItemName.dust.getItemStack(DustResourceType.ender_pearl)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE) }).setAfterHarvestSize(1).register();
        GenericCropCard.create("meat_rose").setDiscoveredBy("VintageBeef").setProperties(new CropProperties(7, 0, 4, 1, 3, 0)).setAttributes(new String[] { "Edible", "Flower", "Cow", "Chicken", "Pig", "Sheep" }).setMaxSize(4).setDrops(new ItemStack(Items.DYE, 1, 9)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BEEF), new ItemStack(Items.PORKCHOP), new ItemStack(Items.CHICKEN), new ItemStack(Items.MUTTON) }).setGrowthSpeed(1500).setAfterHarvestSize(1).register();
        GenericCropCard.create("milk_wart").setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 3, 0, 1, 0)).setAttributes(new String[] { "Edible", "Milk", "Cow" }).setMaxSize(3).setDrops(ItemName.crop_res.getItemStack(CropResItemType.milk_wart)).setGrowthSpeed(900).setAfterHarvestSize(1).addBaseSeed(ItemName.crop_res.getItemStack(CropResItemType.milk_wart)).register();
        GenericCropCard.create("oil_berries").setDiscoveredBy("Spacetoad").setProperties(new CropProperties(9, 6, 1, 2, 1, 12)).setAttributes(new String[] { "Fire", "Dark", "Reed", "Rotten", "Coal", "Oil" }).setMaxSize(3).setDrops(ItemName.crop_res.getItemStack(CropResItemType.oil_berry)).setAfterHarvestSize(1).register();
        GenericCropCard.create("slime_plant").setDiscoveredBy("Neowulf").setProperties(new CropProperties(6, 3, 0, 0, 0, 2)).setAttributes(new String[] { "Slime", "Bouncy", "Sticky", "Bush" }).setMaxSize(4).setDrops(new ItemStack(Items.SLIME_BALL)).setAfterHarvestSize(3).register();
        GenericCropCard.create("spidernip").setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(4, 2, 1, 4, 1, 3)).setAttributes(new String[] { "Toxic", "Silk", "Spider", "Flower", "Ingredient", "Addictive" }).setMaxSize(4).setDrops(new ItemStack(Items.STRING)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.SPIDER_EYE), new ItemStack(Blocks.WEB) }).setGrowthSpeed(600).setAfterHarvestSize(1).register();
        GenericCropCard.create("tearstalks").setDiscoveredBy("Neowulf").setProperties(new CropProperties(8, 1, 2, 0, 0, 0)).setAttributes(new String[] { "Healing", "Nether", "Ingredient", "Reed", "Ghast" }).setMaxSize(4).setDrops(new ItemStack(Items.GHAST_TEAR)).setAfterHarvestSize(1).register();
        GenericCropCard.create("withereed").setDiscoveredBy("CovertJaguar").setProperties(new CropProperties(8, 2, 0, 4, 1, 3)).setAttributes(new String[] { "Fire", "Undead", "Reed", "Coal", "Rotten", "Wither" }).setMaxSize(4).setDrops(ItemName.dust.getItemStack(DustResourceType.coal)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.COAL), new ItemStack(Items.COAL) }).setAfterHarvestSize(1).register();
    }
    
    public static void registerBaseSeeds() {
        Crops.instance.registerBaseSeed(new ItemStack(Items.WHEAT_SEEDS, 1, 32767), IC2Crops.cropWheat, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.PUMPKIN_SEEDS, 1, 32767), IC2Crops.cropPumpkin, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.MELON_SEEDS, 1, 32767), IC2Crops.cropMelon, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.NETHER_WART, 1, 32767), IC2Crops.cropNetherWart, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(ItemName.terra_wart.getItemStack(), IC2Crops.cropTerraWart, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(ItemName.crop_res.getItemStack(CropResItemType.coffee_beans), IC2Crops.cropCoffee, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.REEDS, 1, 32767), IC2Crops.cropReed, 1, 3, 0, 2);
        Crops.instance.registerBaseSeed(new ItemStack(Items.DYE, 1, 3), IC2Crops.cropCocoa, 1, 0, 0, 0);
        Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.RED_FLOWER, 4, 32767), IC2Crops.cropRedFlower, 4, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.YELLOW_FLOWER, 4, 32767), IC2Crops.cropYellowFlower, 4, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.CARROT, 1, 32767), IC2Crops.cropCarrots, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.POTATO, 1, 32767), IC2Crops.cropPotato, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.BROWN_MUSHROOM, 4, 32767), IC2Crops.cropBrownMushroom, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.RED_MUSHROOM, 4, 32767), IC2Crops.cropRedMushroom, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.CACTUS, 1, 32767), IC2Crops.cropEatingPlant, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Items.BEETROOT_SEEDS, 1, 32767), IC2Crops.cropBeetroot, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 0), IC2Crops.cropOakSapling, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 1), IC2Crops.cropSpruceSapling, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 2), IC2Crops.cropBirchSapling, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 3), IC2Crops.cropJungleSapling, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 4), IC2Crops.cropAcaciaSapling, 1, 1, 1, 1);
        Crops.instance.registerBaseSeed(new ItemStack(Blocks.SAPLING, 1, 5), IC2Crops.cropDarkOakSapling, 1, 1, 1, 1);
    }
    
    public static void ensureInit() {
        if (IC2Crops.needsToPost) {
            MinecraftForge.EVENT_BUS.post((Event)new CropRegisterEvent());
        }
    }
    
    @Override
    public void addBiomenutrientsBonus(final BiomeDictionary.Type type, final int nutrientsBonus) {
        this.nutrientBiomeTypeBonus.put(type, nutrientsBonus);
    }
    
    @Override
    public void addBiomehumidityBonus(final BiomeDictionary.Type type, final int humidityBonus) {
        this.humidityBiomeTypeBonus.put(type, humidityBonus);
    }
    
    @Override
    public int getHumidityBiomeBonus(final Biome biome) {
        Integer ret = 0;
        for (final BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
            final Integer val = this.humidityBiomeTypeBonus.get(type);
            if (val != null && val > ret) {
                ret = val;
            }
        }
        return ret;
    }
    
    @Override
    public int getNutrientBiomeBonus(final Biome biome) {
        Integer ret = 0;
        for (final BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
            final Integer val = this.nutrientBiomeTypeBonus.get(type);
            if (val != null && val > ret) {
                ret = val;
            }
        }
        return ret;
    }
    
    @Override
    public CropCard getCropCard(final String owner, final String name) {
        final Map<String, CropCard> map = this.cropMap.get(owner);
        if (map == null) {
            return null;
        }
        return map.get(name);
    }
    
    @Override
    public CropCard getCropCard(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        final NBTTagCompound nbt = stack.getTagCompound();
        if (nbt.hasKey("owner") && nbt.hasKey("id")) {
            return this.getCropCard(nbt.getString("owner"), nbt.getString("id"));
        }
        return null;
    }
    
    @Override
    public Collection<CropCard> getCrops() {
        return new AbstractCollection<CropCard>() {
            @Override
            public Iterator<CropCard> iterator() {
                return new Iterator<CropCard>() {
                    private final Iterator<Map<String, CropCard>> mapIterator = IC2Crops.this.cropMap.values().iterator();
                    private Iterator<CropCard> iterator = this.getNextIterator();
                    
                    @Override
                    public boolean hasNext() {
                        return this.iterator != null && this.iterator.hasNext();
                    }
                    
                    @Override
                    public CropCard next() {
                        if (this.iterator == null) {
                            throw new NoSuchElementException("no more elements");
                        }
                        final CropCard ret = this.iterator.next();
                        if (!this.iterator.hasNext()) {
                            this.iterator = this.getNextIterator();
                        }
                        return ret;
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("This iterator is read-only.");
                    }
                    
                    private Iterator<CropCard> getNextIterator() {
                        Iterator<CropCard> ret;
                        for (ret = null; this.mapIterator.hasNext() && ret == null; ret = null) {
                            ret = this.mapIterator.next().values().iterator();
                            if (!ret.hasNext()) {}
                        }
                        return ret;
                    }
                };
            }
            
            @Override
            public int size() {
                int ret = 0;
                for (final Map<String, CropCard> map : IC2Crops.this.cropMap.values()) {
                    ret += map.size();
                }
                return ret;
            }
        };
    }
    
    @Override
    public void registerCrop(final CropCard crop) {
        final List<String> disabledCrops = ConfigUtil.asList(ConfigUtil.getString(MainConfig.get(), "agriculture/disabledCrops"));
        final String owner = crop.getOwner();
        final String id = crop.getId();
        if (crop != IC2Crops.weed && disabledCrops.contains(owner + ":" + id)) {
            IC2.log.info(LogCategory.Crop, "Crop " + owner + ":" + id + " has been disabled");
            return;
        }
        if (!owner.equals(owner.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-lower case owner");
        }
        final Map<String, CropCard> map = this.cropMap.computeIfAbsent(owner, key -> new HashMap());
        final CropCard prev = map.put(id, crop);
        if (prev != null) {
            throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-unique owner+id pair");
        }
    }
    
    @Override
    public boolean registerBaseSeed(final ItemStack stack, final CropCard crop, final int size, final int growth, final int gain, final int resistance) {
        final List<String> disabledCrops = ConfigUtil.asList(ConfigUtil.getString(MainConfig.get(), "agriculture/disabledCrops"));
        final String owner = crop.getOwner();
        final String id = crop.getId();
        if (crop != IC2Crops.weed && disabledCrops.contains(owner + ":" + id)) {
            return false;
        }
        for (final ItemStack key : this.baseSeeds.keySet()) {
            if (key.getItem() == stack.getItem() && key.getItemDamage() == stack.getItemDamage()) {
                return false;
            }
        }
        this.baseSeeds.put(stack, new BaseSeed(crop, size, growth, gain, resistance));
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerCropTextures(final Map<ResourceLocation, TextureAtlasSprite> extraTextures) {
        extraTextures.forEach(CropModel.textures::putIfAbsent);
    }
    
    @Override
    public BaseSeed getBaseSeed(final ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return this.baseSeeds.keySet().stream().filter(key -> key.getItem() == stack.getItem() && (key.getItemDamage() == 32767 || key.getItemDamage() == stack.getItemDamage())).findFirst().map((Function<? super Object, ? extends BaseSeed>)this.baseSeeds::get).orElse(null);
    }
    
    static {
        IC2Crops.cropWheat = new CropWheat();
        IC2Crops.cropPumpkin = new CropPumpkin();
        IC2Crops.cropMelon = new CropMelon();
        IC2Crops.cropYellowFlower = new CropColorFlower("dandelion", new String[] { "Yellow", "Flower" }, 11);
        IC2Crops.cropRedFlower = new CropColorFlower("rose", new String[] { "Red", "Flower", "Rose" }, 1);
        IC2Crops.cropBlackFlower = new CropColorFlower("blackthorn", new String[] { "Black", "Flower", "Rose" }, 0);
        IC2Crops.cropPurpleFlower = new CropColorFlower("tulip", new String[] { "Purple", "Flower", "Tulip" }, 5);
        IC2Crops.cropBlueFlower = new CropColorFlower("cyazint", new String[] { "Blue", "Flower" }, 6);
        IC2Crops.cropVenomilia = new CropVenomilia();
        IC2Crops.cropReed = new CropReed();
        IC2Crops.cropStickReed = new CropStickreed();
        IC2Crops.cropCocoa = new CropCocoa();
        IC2Crops.cropFlax = new CropFlax();
        IC2Crops.cropRedMushroom = new CropBaseMushroom("red_mushroom", new String[] { "Red", "Food", "Mushroom" }, new ItemStack((Block)Blocks.RED_MUSHROOM));
        IC2Crops.cropBrownMushroom = new CropBaseMushroom("brown_mushroom", new String[] { "Brown", "Food", "Mushroom" }, new ItemStack((Block)Blocks.BROWN_MUSHROOM));
        IC2Crops.cropNetherWart = new CropNetherWart();
        IC2Crops.cropTerraWart = new CropTerraWart();
        IC2Crops.cropOakSapling = new CropBaseSapling("oak_sapling", "acorns", new ItemStack(Blocks.LOG), new ItemStack(Blocks.SAPLING, 1, 0));
        IC2Crops.cropSpruceSapling = new CropBaseSapling("spruce_sapling", "pine_cones", new ItemStack(Blocks.LOG, 1, 1), new ItemStack(Blocks.SAPLING, 1, 1));
        IC2Crops.cropBirchSapling = new CropBaseSapling("birch_sapling", "catkins", new ItemStack(Blocks.LOG, 1, 2), new ItemStack(Blocks.SAPLING, 1, 2));
        IC2Crops.cropJungleSapling = new CropBaseSapling("jungle_sapling", "seedling", new ItemStack(Blocks.LOG, 1, 3), new ItemStack(Blocks.SAPLING, 1, 3));
        IC2Crops.cropAcaciaSapling = new CropBaseSapling("acacia_sapling", "seedling", new ItemStack(Blocks.LOG2), new ItemStack(Blocks.SAPLING, 1, 4));
        IC2Crops.cropDarkOakSapling = new CropBaseSapling("dark_oak_sapling", "acorns", new ItemStack(Blocks.LOG2, 1, 1), new ItemStack(Blocks.SAPLING, 1, 5));
        IC2Crops.cropFerru = new CropBaseMetalCommon("ferru", new String[] { "Gray", "Leaves", "Metal" }, new String[] { "oreIron", "blockIron" }, ItemName.dust.getItemStack(DustResourceType.small_iron));
        IC2Crops.cropCyprium = new CropBaseMetalCommon("cyprium", new String[] { "Orange", "Leaves", "Metal" }, new String[] { "oreCopper", "blockCopper" }, ItemName.dust.getItemStack(DustResourceType.small_copper));
        IC2Crops.cropStagnium = new CropBaseMetalCommon("stagnium", new String[] { "Shiny", "Leaves", "Metal" }, new String[] { "oreTin", "blockTin" }, ItemName.dust.getItemStack(DustResourceType.small_tin));
        IC2Crops.cropPlumbiscus = new CropBaseMetalCommon("plumbiscus", new String[] { "Dense", "Leaves", "Metal" }, new String[] { "oreLead", "blockLead" }, ItemName.dust.getItemStack(DustResourceType.small_lead));
        IC2Crops.cropAurelia = new CropBaseMetalUncommon("aurelia", new String[] { "Gold", "Leaves", "Metal" }, new String[] { "oreGold", "blockGold" }, ItemName.dust.getItemStack(DustResourceType.small_gold));
        IC2Crops.cropShining = new CropBaseMetalUncommon("shining", new String[] { "Silver", "Leaves", "Metal" }, new String[] { "oreSilver", "blockSilver" }, ItemName.dust.getItemStack(DustResourceType.small_silver));
        IC2Crops.cropRedwheat = new CropRedWheat();
        IC2Crops.cropCoffee = new CropCoffee();
        IC2Crops.cropHops = new CropHops();
        IC2Crops.cropCarrots = new CropCarrots();
        IC2Crops.cropPotato = new CropPotato();
        IC2Crops.cropEatingPlant = new CropEating();
        IC2Crops.cropBeetroot = new CropBeetroot();
        IC2Crops.needsToPost = true;
    }
}
