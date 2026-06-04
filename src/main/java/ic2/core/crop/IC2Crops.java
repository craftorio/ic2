package ic2.core.crop;

import ic2.api.crops.BaseSeed;
import ic2.api.crops.CropCard;
import ic2.api.crops.CropProperties;
import ic2.api.crops.Crops;
import ic2.core.IC2;
import ic2.core.crop.cropcard.CropBaseMetalCommon;
import ic2.core.crop.cropcard.CropBaseMetalUncommon;
import ic2.core.crop.cropcard.CropBaseMushroom;
import ic2.core.crop.cropcard.CropBaseSapling;
import ic2.core.crop.cropcard.CropBeetroot;
import ic2.core.crop.cropcard.CropCarrots;
import ic2.core.crop.cropcard.CropCocoa;
import ic2.core.crop.cropcard.CropCoffee;
import ic2.core.crop.cropcard.CropColorFlower;
import ic2.core.crop.cropcard.CropEating;
import ic2.core.crop.cropcard.CropFlax;
import ic2.core.crop.cropcard.CropHops;
import ic2.core.crop.cropcard.CropMelon;
import ic2.core.crop.cropcard.CropNetherWart;
import ic2.core.crop.cropcard.CropPotato;
import ic2.core.crop.cropcard.CropPumpkin;
import ic2.core.crop.cropcard.CropRedWheat;
import ic2.core.crop.cropcard.CropReed;
import ic2.core.crop.cropcard.CropStickreed;
import ic2.core.crop.cropcard.CropTerraWart;
import ic2.core.crop.cropcard.CropVenomilia;
import ic2.core.crop.cropcard.CropWeed;
import ic2.core.crop.cropcard.CropWheat;
import ic2.core.crop.cropcard.GenericCropCard;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CropResItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IC2Crops extends Crops {
  private final Map<BiomeDictionary.Type, Integer> humidityBiomeTypeBonus = new IdentityHashMap<>();
  
  private final Map<BiomeDictionary.Type, Integer> nutrientBiomeTypeBonus = new IdentityHashMap<>();
  
  private final Map<ItemStack, BaseSeed> baseSeeds = new HashMap<>();
  
  public static CropCard cropWheat = (CropCard)new CropWheat();
  
  public static CropCard cropPumpkin = (CropCard)new CropPumpkin();
  
  public static CropCard cropMelon = (CropCard)new CropMelon();
  
  public static CropCard cropYellowFlower = (CropCard)new CropColorFlower("dandelion", new String[] { "Yellow", "Flower" }, 11);
  
  public static CropCard cropRedFlower = (CropCard)new CropColorFlower("rose", new String[] { "Red", "Flower", "Rose" }, 1);
  
  public static CropCard cropBlackFlower = (CropCard)new CropColorFlower("blackthorn", new String[] { "Black", "Flower", "Rose" }, 0);
  
  public static CropCard cropPurpleFlower = (CropCard)new CropColorFlower("tulip", new String[] { "Purple", "Flower", "Tulip" }, 5);
  
  public static CropCard cropBlueFlower = (CropCard)new CropColorFlower("cyazint", new String[] { "Blue", "Flower" }, 6);
  
  public static CropCard cropVenomilia = (CropCard)new CropVenomilia();
  
  public static CropCard cropReed = (CropCard)new CropReed();
  
  public static CropCard cropStickReed = (CropCard)new CropStickreed();
  
  public static CropCard cropCocoa = (CropCard)new CropCocoa();
  
  public static CropCard cropFlax = (CropCard)new CropFlax();
  
  public static CropCard cropRedMushroom = (CropCard)new CropBaseMushroom("red_mushroom", new String[] { "Red", "Food", "Mushroom" }, new ItemStack((Block)Blocks.field_150337_Q));
  
  public static CropCard cropBrownMushroom = (CropCard)new CropBaseMushroom("brown_mushroom", new String[] { "Brown", "Food", "Mushroom" }, new ItemStack((Block)Blocks.field_150338_P));
  
  public static CropCard cropNetherWart = (CropCard)new CropNetherWart();
  
  public static CropCard cropTerraWart = (CropCard)new CropTerraWart();
  
  public static CropCard cropOakSapling = (CropCard)new CropBaseSapling("oak_sapling", "acorns", new ItemStack(Blocks.field_150364_r), new ItemStack(Blocks.field_150345_g, 1, 0));
  
  public static CropCard cropSpruceSapling = (CropCard)new CropBaseSapling("spruce_sapling", "pine_cones", new ItemStack(Blocks.field_150364_r, 1, 1), new ItemStack(Blocks.field_150345_g, 1, 1));
  
  public static CropCard cropBirchSapling = (CropCard)new CropBaseSapling("birch_sapling", "catkins", new ItemStack(Blocks.field_150364_r, 1, 2), new ItemStack(Blocks.field_150345_g, 1, 2));
  
  public static CropCard cropJungleSapling = (CropCard)new CropBaseSapling("jungle_sapling", "seedling", new ItemStack(Blocks.field_150364_r, 1, 3), new ItemStack(Blocks.field_150345_g, 1, 3));
  
  public static CropCard cropAcaciaSapling = (CropCard)new CropBaseSapling("acacia_sapling", "seedling", new ItemStack(Blocks.field_150363_s), new ItemStack(Blocks.field_150345_g, 1, 4));
  
  public static CropCard cropDarkOakSapling = (CropCard)new CropBaseSapling("dark_oak_sapling", "acorns", new ItemStack(Blocks.field_150363_s, 1, 1), new ItemStack(Blocks.field_150345_g, 1, 5));
  
  public static CropCard cropFerru = (CropCard)new CropBaseMetalCommon("ferru", new String[] { "Gray", "Leaves", "Metal" }, new String[] { "oreIron", "blockIron" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_iron));
  
  public static CropCard cropCyprium = (CropCard)new CropBaseMetalCommon("cyprium", new String[] { "Orange", "Leaves", "Metal" }, new String[] { "oreCopper", "blockCopper" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_copper));
  
  public static CropCard cropStagnium = (CropCard)new CropBaseMetalCommon("stagnium", new String[] { "Shiny", "Leaves", "Metal" }, new String[] { "oreTin", "blockTin" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_tin));
  
  public static CropCard cropPlumbiscus = (CropCard)new CropBaseMetalCommon("plumbiscus", new String[] { "Dense", "Leaves", "Metal" }, new String[] { "oreLead", "blockLead" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_lead));
  
  public static CropCard cropAurelia = (CropCard)new CropBaseMetalUncommon("aurelia", new String[] { "Gold", "Leaves", "Metal" }, new String[] { "oreGold", "blockGold" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_gold));
  
  public static CropCard cropShining = (CropCard)new CropBaseMetalUncommon("shining", new String[] { "Silver", "Leaves", "Metal" }, new String[] { "oreSilver", "blockSilver" }, ItemName.dust
      .getItemStack((Enum)DustResourceType.small_silver));
  
  public static CropCard cropRedwheat = (CropCard)new CropRedWheat();
  
  public static CropCard cropCoffee = (CropCard)new CropCoffee();
  
  public static CropCard cropHops = (CropCard)new CropHops();
  
  public static CropCard cropCarrots = (CropCard)new CropCarrots();
  
  public static CropCard cropPotato = (CropCard)new CropPotato();
  
  public static CropCard cropEatingPlant = (CropCard)new CropEating();
  
  public static CropCard cropBeetroot = (CropCard)new CropBeetroot();
  
  public static void init() {
    Crops.instance = new IC2Crops();
    Crops.weed = (CropCard)new CropWeed();
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
    Crops.instance.registerCrop(weed);
    Crops.instance.registerCrop(cropWheat);
    Crops.instance.registerCrop(cropPumpkin);
    Crops.instance.registerCrop(cropMelon);
    Crops.instance.registerCrop(cropYellowFlower);
    Crops.instance.registerCrop(cropRedFlower);
    Crops.instance.registerCrop(cropBlackFlower);
    Crops.instance.registerCrop(cropPurpleFlower);
    Crops.instance.registerCrop(cropBlueFlower);
    Crops.instance.registerCrop(cropVenomilia);
    Crops.instance.registerCrop(cropReed);
    Crops.instance.registerCrop(cropStickReed);
    Crops.instance.registerCrop(cropCocoa);
    Crops.instance.registerCrop(cropFlax);
    Crops.instance.registerCrop(cropFerru);
    Crops.instance.registerCrop(cropAurelia);
    Crops.instance.registerCrop(cropRedwheat);
    Crops.instance.registerCrop(cropNetherWart);
    Crops.instance.registerCrop(cropTerraWart);
    Crops.instance.registerCrop(cropCoffee);
    Crops.instance.registerCrop(cropHops);
    Crops.instance.registerCrop(cropCarrots);
    Crops.instance.registerCrop(cropPotato);
    Crops.instance.registerCrop(cropRedMushroom);
    Crops.instance.registerCrop(cropBrownMushroom);
    Crops.instance.registerCrop(cropEatingPlant);
    Crops.instance.registerCrop(cropCyprium);
    Crops.instance.registerCrop(cropStagnium);
    Crops.instance.registerCrop(cropPlumbiscus);
    Crops.instance.registerCrop(cropShining);
    Crops.instance.registerCrop(cropBeetroot);
    Crops.instance.registerCrop(cropOakSapling);
    Crops.instance.registerCrop(cropSpruceSapling);
    Crops.instance.registerCrop(cropBirchSapling);
    Crops.instance.registerCrop(cropJungleSapling);
    Crops.instance.registerCrop(cropAcaciaSapling);
    Crops.instance.registerCrop(cropDarkOakSapling);
    GenericCropCard.create("blazereed")
      .setDiscoveredBy("Mr. Brain")
      .setProperties(new CropProperties(6, 0, 4, 1, 0, 0))
      .setAttributes(new String[] { "Fire", "Blaze", "Reed", "Sulfur" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151065_br))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151072_bj), ItemName.dust.getItemStack((Enum)DustResourceType.sulfur) }).setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("bobs_yer_uncle_ranks_berries")
      .setDiscoveredBy("GenerikB")
      .setProperties(new CropProperties(11, 4, 0, 8, 2, 9))
      .setAttributes(new String[] { "Shiny", "Vine", "Emerald", "Berylium", "Crystal" }).setMaxSize(4)
      .setDrops(ItemName.crop_res.getItemStack((Enum)CropResItemType.bobs_yer_uncle_ranks_berry))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151166_bC) }).setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("corium")
      .setDiscoveredBy("Gregorius Techneticies")
      .setProperties(new CropProperties(6, 0, 2, 3, 1, 0))
      .setAttributes(new String[] { "Cow", "Silk", "Vine" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151116_aA))
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("corpse_plant")
      .setDiscoveredBy("Mr. Kenny")
      .setProperties(new CropProperties(5, 0, 2, 1, 0, 3))
      .setAttributes(new String[] { "Toxic", "Undead", "Vine", "Edible", "Rotten" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151078_bh))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151103_aS), new ItemStack(Items.field_151100_aR, 1, 15), new ItemStack(Items.field_151100_aR, 1, 15) }).setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("creeper_weed")
      .setDiscoveredBy("General Spaz")
      .setProperties(new CropProperties(7, 3, 0, 5, 1, 3))
      .setAttributes(new String[] { "Creeper", "Vine", "Explosive", "Fire", "Sulfur", "Saltpeter", "Coal" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151016_H))
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("diareed")
      .setDiscoveredBy("Diareed")
      .setProperties(new CropProperties(12, 5, 0, 10, 2, 10))
      .setAttributes(new String[] { "Fire", "Shiny", "Reed", "Coal", "Diamond", "Crystal" }).setMaxSize(4)
      .setDrops(ItemName.dust.getItemStack((Enum)DustResourceType.small_diamond))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.DIAMOND) }).setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("egg_plant")
      .setDiscoveredBy("Link")
      .setProperties(new CropProperties(6, 0, 4, 1, 0, 0))
      .setAttributes(new String[] { "Chicken", "Egg", "Edible", "Feather", "Flower", "Addictive" }).setMaxSize(3)
      .setDrops(new ItemStack(Items.field_151110_aK))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151076_bf), new ItemStack(Items.field_151008_G), new ItemStack(Items.field_151008_G), new ItemStack(Items.field_151008_G) }).setGrowthSpeed(900)
      .setAfterHarvestSize(2)
      .register();
    GenericCropCard.create("ender_blossom")
      .setDiscoveredBy("RichardG")
      .setProperties(new CropProperties(10, 5, 0, 2, 1, 6))
      .setAttributes(new String[] { "Ender", "Flower", "Shiny" }).setMaxSize(4)
      .setDrops(ItemName.dust.getItemStack((Enum)DustResourceType.ender_pearl))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151079_bi), new ItemStack(Items.field_151079_bi), new ItemStack(Items.field_151061_bv) }).setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("meat_rose")
      .setDiscoveredBy("VintageBeef")
      .setProperties(new CropProperties(7, 0, 4, 1, 3, 0))
      .setAttributes(new String[] { "Edible", "Flower", "Cow", "Chicken", "Pig", "Sheep" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151100_aR, 1, 9))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151082_bd), new ItemStack(Items.field_151147_al), new ItemStack(Items.field_151076_bf), new ItemStack(Items.field_179561_bm) }).setGrowthSpeed(1500)
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("milk_wart")
      .setDiscoveredBy("Mr. Brain")
      .setProperties(new CropProperties(6, 0, 3, 0, 1, 0))
      .setAttributes(new String[] { "Edible", "Milk", "Cow" }).setMaxSize(3)
      .setDrops(ItemName.crop_res.getItemStack((Enum)CropResItemType.milk_wart))
      .setGrowthSpeed(900)
      .setAfterHarvestSize(1)
      .addBaseSeed(ItemName.crop_res.getItemStack((Enum)CropResItemType.milk_wart))
      .register();
    GenericCropCard.create("oil_berries")
      .setDiscoveredBy("Spacetoad")
      .setProperties(new CropProperties(9, 6, 1, 2, 1, 12))
      .setAttributes(new String[] { "Fire", "Dark", "Reed", "Rotten", "Coal", "Oil" }).setMaxSize(3)
      .setDrops(ItemName.crop_res.getItemStack((Enum)CropResItemType.oil_berry))
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("slime_plant")
      .setDiscoveredBy("Neowulf")
      .setProperties(new CropProperties(6, 3, 0, 0, 0, 2))
      .setAttributes(new String[] { "Slime", "Bouncy", "Sticky", "Bush" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151123_aH))
      .setAfterHarvestSize(3)
      .register();
    GenericCropCard.create("spidernip")
      .setDiscoveredBy("Mr. Kenny")
      .setProperties(new CropProperties(4, 2, 1, 4, 1, 3))
      .setAttributes(new String[] { "Toxic", "Silk", "Spider", "Flower", "Ingredient", "Addictive" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151007_F))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151070_bp), new ItemStack(Blocks.field_150321_G) }).setGrowthSpeed(600)
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("tearstalks")
      .setDiscoveredBy("Neowulf")
      .setProperties(new CropProperties(8, 1, 2, 0, 0, 0))
      .setAttributes(new String[] { "Healing", "Nether", "Ingredient", "Reed", "Ghast" }).setMaxSize(4)
      .setDrops(new ItemStack(Items.field_151073_bk))
      .setAfterHarvestSize(1)
      .register();
    GenericCropCard.create("withereed")
      .setDiscoveredBy("CovertJaguar")
      .setProperties(new CropProperties(8, 2, 0, 4, 1, 3))
      .setAttributes(new String[] { "Fire", "Undead", "Reed", "Coal", "Rotten", "Wither" }).setMaxSize(4)
      .setDrops(ItemName.dust.getItemStack((Enum)DustResourceType.coal))
      .setSpecialDrops(new ItemStack[] { new ItemStack(Items.field_151044_h), new ItemStack(Items.field_151044_h) }).setAfterHarvestSize(1)
      .register();
  }
  
  public static void registerBaseSeeds() {
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151014_N, 1, 32767), cropWheat, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151080_bb, 1, 32767), cropPumpkin, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151081_bc, 1, 32767), cropMelon, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151075_bm, 1, 32767), cropNetherWart, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(ItemName.terra_wart.getItemStack(), cropTerraWart, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(ItemName.crop_res.getItemStack((Enum)CropResItemType.coffee_beans), cropCoffee, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151120_aE, 1, 32767), cropReed, 1, 3, 0, 2);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151100_aR, 1, 3), cropCocoa, 1, 0, 0, 0);
    Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.field_150328_O, 4, 32767), cropRedFlower, 4, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.field_150327_N, 4, 32767), cropYellowFlower, 4, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151172_bF, 1, 32767), cropCarrots, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_151174_bG, 1, 32767), cropPotato, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.field_150338_P, 4, 32767), cropBrownMushroom, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.field_150337_Q, 4, 32767), cropRedMushroom, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack((Block)Blocks.field_150434_aF, 1, 32767), cropEatingPlant, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Items.field_185163_cU, 1, 32767), cropBeetroot, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 0), cropOakSapling, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 1), cropSpruceSapling, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 2), cropBirchSapling, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 3), cropJungleSapling, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 4), cropAcaciaSapling, 1, 1, 1, 1);
    Crops.instance.registerBaseSeed(new ItemStack(Blocks.field_150345_g, 1, 5), cropDarkOakSapling, 1, 1, 1, 1);
  }
  
  public static void ensureInit() {
    if (needsToPost)
      MinecraftForge.EVENT_BUS.post((Event)new Crops.CropRegisterEvent()); 
  }
  
  public void addBiomenutrientsBonus(BiomeDictionary.Type type, int nutrientsBonus) {
    this.nutrientBiomeTypeBonus.put(type, Integer.valueOf(nutrientsBonus));
  }
  
  public void addBiomehumidityBonus(BiomeDictionary.Type type, int humidityBonus) {
    this.humidityBiomeTypeBonus.put(type, Integer.valueOf(humidityBonus));
  }
  
  public int getHumidityBiomeBonus(Biome biome) {
    Integer ret = Integer.valueOf(0);
    for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
      Integer val = this.humidityBiomeTypeBonus.get(type);
      if (val != null && val.intValue() > ret.intValue())
        ret = val; 
    } 
    return ret.intValue();
  }
  
  public int getNutrientBiomeBonus(Biome biome) {
    Integer ret = Integer.valueOf(0);
    for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
      Integer val = this.nutrientBiomeTypeBonus.get(type);
      if (val != null && val.intValue() > ret.intValue())
        ret = val; 
    } 
    return ret.intValue();
  }
  
  public CropCard getCropCard(String owner, String name) {
    Map<String, CropCard> map = this.cropMap.get(owner);
    if (map == null)
      return null; 
    return map.get(name);
  }
  
  public CropCard getCropCard(ItemStack stack) {
    if (!stack.func_77942_o())
      return null; 
    NBTTagCompound nbt = stack.func_77978_p();
    if (nbt.func_74764_b("owner") && nbt.func_74764_b("id"))
      return getCropCard(nbt.func_74779_i("owner"), nbt.func_74779_i("id")); 
    return null;
  }
  
  public Collection<CropCard> getCrops() {
    return new AbstractCollection<CropCard>() {
        public Iterator<CropCard> iterator() {
          return new Iterator<CropCard>() {
              public boolean hasNext() {
                return (this.iterator != null && this.iterator.hasNext());
              }
              
              public CropCard next() {
                if (this.iterator == null)
                  throw new NoSuchElementException("no more elements"); 
                CropCard ret = this.iterator.next();
                if (!this.iterator.hasNext())
                  this.iterator = getNextIterator(); 
                return ret;
              }
              
              public void remove() {
                throw new UnsupportedOperationException("This iterator is read-only.");
              }
              
              private Iterator<CropCard> getNextIterator() {
                Iterator<CropCard> ret = null;
                while (this.mapIterator.hasNext() && ret == null) {
                  ret = ((Map)this.mapIterator.next()).values().iterator();
                  if (!ret.hasNext())
                    ret = null; 
                } 
                return ret;
              }
              
              private final Iterator<Map<String, CropCard>> mapIterator = IC2Crops.this.cropMap.values().iterator();
              
              private Iterator<CropCard> iterator = getNextIterator();
            };
        }
        
        public int size() {
          int ret = 0;
          for (Map<String, CropCard> map : (Iterable<Map<String, CropCard>>)IC2Crops.this.cropMap.values())
            ret += map.size(); 
          return ret;
        }
      };
  }
  
  public void registerCrop(CropCard crop) {
    List<String> disabledCrops = ConfigUtil.asList(ConfigUtil.getString(MainConfig.get(), "agriculture/disabledCrops"));
    String owner = crop.getOwner();
    String id = crop.getId();
    if (crop != weed && disabledCrops.contains(owner + ":" + id)) {
      IC2.log.info(LogCategory.Crop, "Crop " + owner + ":" + id + " has been disabled");
      return;
    } 
    if (!owner.equals(owner.toLowerCase(Locale.ENGLISH)))
      throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-lower case owner"); 
    Map<String, CropCard> map = this.cropMap.computeIfAbsent(owner, key -> new HashMap<>());
    CropCard prev = map.put(id, crop);
    if (prev != null)
      throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-unique owner+id pair"); 
  }
  
  public boolean registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance) {
    List<String> disabledCrops = ConfigUtil.asList(ConfigUtil.getString(MainConfig.get(), "agriculture/disabledCrops"));
    String owner = crop.getOwner();
    String id = crop.getId();
    if (crop != weed && disabledCrops.contains(owner + ":" + id))
      return false; 
    for (ItemStack key : this.baseSeeds.keySet()) {
      if (key.getItem() == stack.getItem() && key.func_77952_i() == stack.func_77952_i())
        return false; 
    } 
    this.baseSeeds.put(stack, new BaseSeed(crop, size, growth, gain, resistance));
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public void registerCropTextures(Map<ResourceLocation, TextureAtlasSprite> extraTextures) {
    extraTextures.forEach(CropModel.textures::putIfAbsent);
  }
  
  public BaseSeed getBaseSeed(ItemStack stack) {
    if (stack == null)
      return null; 
    return this.baseSeeds.keySet()
      .stream()
      .filter(key -> (key.getItem() == stack.getItem() && (key.func_77952_i() == 32767 || key.func_77952_i() == stack.func_77952_i())))
      
      .findFirst()
      .map(this.baseSeeds::get)
      .orElse(null);
  }
  
  static boolean needsToPost = true;
  
  private final Map<String, Map<String, CropCard>> cropMap = new HashMap<>();
}
