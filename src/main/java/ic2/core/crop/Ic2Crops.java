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
import ic2.core.crop.cropcard.CropBeetroots;
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
import ic2.core.crop.cropcard.CropStickyReed;
import ic2.core.crop.cropcard.CropTerraWart;
import ic2.core.crop.cropcard.CropVenomilia;
import ic2.core.crop.cropcard.CropWeed;
import ic2.core.crop.cropcard.CropWheat;
import ic2.core.crop.cropcard.GenericCropCard;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class Ic2Crops extends Crops
{
	private final Map<EnvProxy.BiomeType, Integer> humidityBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<EnvProxy.BiomeType, Integer> nutrientBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<ItemStack, BaseSeed> baseSeeds = new HashMap<>();
	public static CropCard cropWheat = new CropWheat(Ic2CropType.wheat);
	public static CropCard cropPumpkin = new CropPumpkin(Ic2CropType.pumpkin);
	public static CropCard cropMelon = new CropMelon(Ic2CropType.melon);
	public static CropCard cropYellowFlower = new CropColorFlower(Ic2CropType.dandelion, Ic2Blocks.DANDELION_CROP, new String[] { "Yellow", "Flower" }, DyeColor.YELLOW);
	public static CropCard cropRedFlower = new CropColorFlower(Ic2CropType.poppy, Ic2Blocks.POPPY_CROP, new String[] { "Red", "Flower", "Rose" }, DyeColor.RED);
	public static CropCard cropBlackFlower = new CropColorFlower(Ic2CropType.blackthorn, Ic2Blocks.BLACKTHORN_CROP, new String[] { "Black", "Flower", "Rose" }, DyeColor.BLACK);
	public static CropCard cropPurpleFlower = new CropColorFlower(Ic2CropType.tulip, Ic2Blocks.TULIP_CROP, new String[] { "Purple", "Flower", "Tulip" }, DyeColor.PURPLE);
	public static CropCard cropBlueFlower = new CropColorFlower(Ic2CropType.cyazint, Ic2Blocks.CYAZINT_CROP, new String[] { "Blue", "Flower" }, DyeColor.CYAN);
	public static CropCard cropVenomilia = new CropVenomilia(Ic2CropType.venomilia);
	public static CropCard cropReed = new CropReed(Ic2CropType.reed);
	public static CropCard cropStickyReed = new CropStickyReed(Ic2CropType.stickyReed);
	public static CropCard cropCocoa = new CropCocoa(Ic2CropType.cocoa);
	public static CropCard cropFlax = new CropFlax(Ic2CropType.flax);
	public static CropCard cropRedMushroom = new CropBaseMushroom(Ic2CropType.redMushroom, Ic2Blocks.RED_MUSHROOM_CROP, new String[] { "Red", "Food", "Mushroom" }, new ItemStack(Blocks.RED_MUSHROOM));
	public static CropCard cropBrownMushroom = new CropBaseMushroom(Ic2CropType.brownMushroom, Ic2Blocks.BROWN_MUSHROOM_CROP, new String[] { "Brown", "Food", "Mushroom" }, new ItemStack(Blocks.BROWN_MUSHROOM));
	public static CropCard cropNetherWart = new CropNetherWart(Ic2CropType.netherWart);
	public static CropCard cropTerraWart = new CropTerraWart(Ic2CropType.terraWart);
	public static CropCard cropOakSapling = new CropBaseSapling(Ic2CropType.oakSapling, Ic2Blocks.OAK_SAPLING_CROP, "seedling", new ItemStack(Blocks.OAK_LOG), new ItemStack(Blocks.OAK_SAPLING));
	public static CropCard cropSpruceSapling = new CropBaseSapling(Ic2CropType.spruceSapling, Ic2Blocks.SPRUCE_SAPLING_CROP, "seedling", new ItemStack(Blocks.SPRUCE_LOG), new ItemStack(Blocks.SPRUCE_SAPLING));
	public static CropCard cropBirchSapling = new CropBaseSapling(Ic2CropType.birchSapling, Ic2Blocks.BIRCH_SAPLING_CROP, "seedling", new ItemStack(Blocks.BIRCH_LOG), new ItemStack(Blocks.BIRCH_SAPLING));
	public static CropCard cropJungleSapling = new CropBaseSapling(Ic2CropType.jungleSapling, Ic2Blocks.JUNGLE_SAPLING_CROP, "seedling", new ItemStack(Blocks.JUNGLE_LOG), new ItemStack(Blocks.JUNGLE_SAPLING));
	public static CropCard cropAcaciaSapling = new CropBaseSapling(Ic2CropType.acaciaSapling, Ic2Blocks.ACACIA_SAPLING_CROP, "seedling", new ItemStack(Blocks.ACACIA_LOG), new ItemStack(Blocks.ACACIA_SAPLING));
	public static CropCard cropDarkOakSapling = new CropBaseSapling(Ic2CropType.darkOakSapling, Ic2Blocks.DARK_OAK_SAPLING_CROP, "seedling", new ItemStack(Blocks.DARK_OAK_LOG), new ItemStack(Blocks.DARK_OAK_SAPLING));
	public static CropCard cropFerru = new CropBaseMetalCommon(Ic2CropType.ferru, Ic2Blocks.FERRU_CROP, new String[] { "Gray", "Leaves", "Metal" }, Arrays.asList(BlockTags.IRON_ORES, Ic2BlockTags.IRON_BLOCKS), new ItemStack(Ic2Items.SMALL_IRON_DUST));
	public static CropCard cropCyprium = new CropBaseMetalCommon(Ic2CropType.cyprium, Ic2Blocks.CYPRIUM_CROP, new String[] { "Orange", "Leaves", "Metal" }, Arrays.asList(BlockTags.COPPER_ORES, Ic2BlockTags.COPPER_BLOCKS), new ItemStack(Ic2Items.SMALL_COPPER_DUST));
	public static CropCard cropStagnium = new CropBaseMetalCommon(Ic2CropType.stagnium, Ic2Blocks.STAGNIUM_CROP, new String[] { "Shiny", "Leaves", "Metal" }, Arrays.asList(Ic2BlockTags.TIN_ORES, Ic2BlockTags.TIN_BLOCKS), new ItemStack(Ic2Items.SMALL_TIN_DUST));
	public static CropCard cropPlumbiscus = new CropBaseMetalCommon(Ic2CropType.plumbiscus, Ic2Blocks.PLUMBISCUS_CROP, new String[] { "Dense", "Leaves", "Metal" }, Arrays.asList(Ic2BlockTags.LEAD_ORES, Ic2BlockTags.LEAD_BLOCKS), new ItemStack(Ic2Items.SMALL_LEAD_DUST));
	public static CropCard cropAurelia = new CropBaseMetalUncommon(Ic2CropType.aurelia, Ic2Blocks.AURELIA_CROP, new String[] { "Gold", "Leaves", "Metal" }, Arrays.asList(BlockTags.GOLD_ORES, Ic2BlockTags.GOLD_BLOCKS), new ItemStack(Ic2Items.SMALL_GOLD_DUST));
	public static CropCard cropShining = new CropBaseMetalUncommon(Ic2CropType.shining, Ic2Blocks.SHINING_CROP, new String[] { "Silver", "Leaves", "Metal" }, Arrays.asList(Ic2BlockTags.SILVER_ORES, Ic2BlockTags.SILVER_BLOCKS), new ItemStack(Ic2Items.SMALL_SILVER_DUST));
	public static CropCard cropRedWheat = new CropRedWheat(Ic2CropType.redWheat);
	public static CropCard cropCoffee = new CropCoffee(Ic2CropType.coffee);
	public static CropCard cropHops = new CropHops(Ic2CropType.hops);
	public static CropCard cropCarrots = new CropCarrots(Ic2CropType.carrots);
	public static CropCard cropPotato = new CropPotato(Ic2CropType.potato);
	public static CropCard cropEatingPlant = new CropEating(Ic2CropType.eatingPlant);
	public static CropCard cropBeetroots = new CropBeetroots(Ic2CropType.beetroots);
	public static CropCard cropBlazereed = GenericCropCard.create(Ic2CropType.blazereed, Ic2Blocks.BLAZEREED_CROP).setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Fire", "Blaze", "Reed", "Sulfur" }).setMaxSize(4).setDrops(new ItemStack(Items.BLAZE_POWDER)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BLAZE_ROD), new ItemStack(Ic2Items.SULFUR_DUST) }).setAfterHarvestSize(1);
	public static CropCard cropBobsYerUncleRanksBerries = GenericCropCard.create(Ic2CropType.bobsYerUncleRanksBerries, Ic2Blocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP).setDiscoveredBy("GenerikB").setProperties(new CropProperties(11, 4, 0, 8, 2, 9)).setAttributes(new String[] { "Shiny", "Vine", "Emerald", "Berylium", "Crystal" }).setMaxSize(4).setDrops(new ItemStack(Ic2Items.BOBS_YER_UNCLE_RANKS_BERRY)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.EMERALD) }).setAfterHarvestSize(1);
	public static CropCard cropCorium = GenericCropCard.create(Ic2CropType.corium, Ic2Blocks.CORIUM_CROP).setDiscoveredBy("Gregorius Techneticies").setProperties(new CropProperties(6, 0, 2, 3, 1, 0)).setAttributes(new String[] { "Cow", "Silk", "Vine" }).setMaxSize(4).setDrops(new ItemStack(Items.LEATHER)).setAfterHarvestSize(1);
	public static CropCard cropCorpsePlant = GenericCropCard.create(Ic2CropType.corpse_plant, Ic2Blocks.CORPSE_PLANT_CROP).setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(5, 0, 2, 1, 0, 3)).setAttributes(new String[] { "Toxic", "Undead", "Vine", "Edible", "Rotten" }).setMaxSize(4).setDrops(new ItemStack(Items.ROTTEN_FLESH)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BONE), new ItemStack(Items.BONE_MEAL), new ItemStack(Items.BONE_MEAL) }).setAfterHarvestSize(1);
	public static CropCard cropCreeperWeed = GenericCropCard.create(Ic2CropType.creeper_weed, Ic2Blocks.CREEPER_WEED_CROP).setDiscoveredBy("General Spaz").setProperties(new CropProperties(7, 3, 0, 5, 1, 3)).setAttributes(new String[] { "Creeper", "Vine", "Explosive", "Fire", "Sulfur", "Saltpeter", "Coal" }).setMaxSize(4).setDrops(new ItemStack(Items.GUNPOWDER)).setAfterHarvestSize(1);
	public static CropCard cropDiareed = GenericCropCard.create(Ic2CropType.diareed, Ic2Blocks.DIAREED_CROP).setDiscoveredBy("Diareed").setProperties(new CropProperties(12, 5, 0, 10, 2, 10)).setAttributes(new String[] { "Fire", "Shiny", "Reed", "Coal", "Diamond", "Crystal" }).setMaxSize(4).setDrops(new ItemStack(Ic2Items.SMALL_DIAMOND_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.DIAMOND) }).setAfterHarvestSize(1);
	public static CropCard cropEggPlant = GenericCropCard.create(Ic2CropType.egg_plant, Ic2Blocks.EGG_PLANT_CROP).setDiscoveredBy("Link").setProperties(new CropProperties(6, 0, 4, 1, 0, 0)).setAttributes(new String[] { "Chicken", "Egg", "Edible", "Feather", "Flower", "Addictive" }).setMaxSize(3).setDrops(new ItemStack(Items.EGG)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.CHICKEN), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER), new ItemStack(Items.FEATHER) }).setGrowthSpeed(900).setAfterHarvestSize(2);
	public static CropCard cropEnderBlossom = GenericCropCard.create(Ic2CropType.ender_blossom, Ic2Blocks.ENDER_BLOSSOM_CROP).setDiscoveredBy("RichardG").setProperties(new CropProperties(10, 5, 0, 2, 1, 6)).setAttributes(new String[] { "Ender", "Flower", "Shiny" }).setMaxSize(4).setDrops(new ItemStack(Ic2Items.ENDER_PEARL_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.ENDER_EYE) }).setAfterHarvestSize(1);
	public static CropCard cropMeatRose = GenericCropCard.create(Ic2CropType.meat_rose, Ic2Blocks.MEAT_ROSE_CROP).setDiscoveredBy("VintageBeef").setProperties(new CropProperties(7, 0, 4, 1, 3, 0)).setAttributes(new String[] { "Edible", "Flower", "Cow", "Chicken", "Pig", "Sheep" }).setMaxSize(4).setDrops(new ItemStack(Items.PINK_DYE)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.BEEF), new ItemStack(Items.PORKCHOP), new ItemStack(Items.CHICKEN), new ItemStack(Items.MUTTON) }).setGrowthSpeed(1500).setAfterHarvestSize(1);
	public static CropCard cropMilkWart = GenericCropCard.create(Ic2CropType.milk_wart, Ic2Blocks.MILK_WART_CROP).setDiscoveredBy("Mr. Brain").setProperties(new CropProperties(6, 0, 3, 0, 1, 0)).setAttributes(new String[] { "Edible", "Milk", "Cow" }).setMaxSize(3).setDrops(new ItemStack(Ic2Items.MILK_WART)).setGrowthSpeed(900).setAfterHarvestSize(1).addBaseSeed(new ItemStack(Ic2Items.MILK_WART));
	public static CropCard cropOilBerries = GenericCropCard.create(Ic2CropType.oil_berries, Ic2Blocks.OIL_BERRIES_CROP).setDiscoveredBy("Spacetoad").setProperties(new CropProperties(9, 6, 1, 2, 1, 12)).setAttributes(new String[] { "Fire", "Dark", "Reed", "Rotten", "Coal", "Oil" }).setMaxSize(3).setDrops(new ItemStack(Ic2Items.OIL_BERRY)).setAfterHarvestSize(1);
	public static CropCard cropSlimePlant = GenericCropCard.create(Ic2CropType.slime_plant, Ic2Blocks.SLIME_PLANT_CROP).setDiscoveredBy("Neowulf").setProperties(new CropProperties(6, 3, 0, 0, 0, 2)).setAttributes(new String[] { "Slime", "Bouncy", "Sticky", "Bush" }).setMaxSize(4).setDrops(new ItemStack(Items.SLIME_BALL)).setAfterHarvestSize(3);
	public static CropCard cropSpidernip = GenericCropCard.create(Ic2CropType.spidernip, Ic2Blocks.SPIDERNIP_CROP).setDiscoveredBy("Mr. Kenny").setProperties(new CropProperties(4, 2, 1, 4, 1, 3)).setAttributes(new String[] { "Toxic", "Silk", "Spider", "Flower", "Ingredient", "Addictive" }).setMaxSize(4).setDrops(new ItemStack(Items.STRING)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.SPIDER_EYE), new ItemStack(Blocks.COBWEB) }).setGrowthSpeed(600).setAfterHarvestSize(1);
	public static CropCard cropTearstalks = GenericCropCard.create(Ic2CropType.tearstalks, Ic2Blocks.TEARSTALKS_CROP).setDiscoveredBy("Neowulf").setProperties(new CropProperties(8, 1, 2, 0, 0, 0)).setAttributes(new String[] { "Healing", "Nether", "Ingredient", "Reed", "Ghast" }).setMaxSize(4).setDrops(new ItemStack(Items.GHAST_TEAR)).setAfterHarvestSize(1);
	public static CropCard cropWithereed = GenericCropCard.create(Ic2CropType.withereed, Ic2Blocks.WITHEREED_CROP).setDiscoveredBy("CovertJaguar").setProperties(new CropProperties(8, 2, 0, 4, 1, 3)).setAttributes(new String[] { "Fire", "Undead", "Reed", "Coal", "Rotten", "Wither" }).setMaxSize(4).setDrops(new ItemStack(Ic2Items.COAL_DUST)).setSpecialDrops(new ItemStack[] { new ItemStack(Items.COAL), new ItemStack(Items.COAL) }).setAfterHarvestSize(1);
	private final Map<String, Map<String, CropCard>> cropMap = new HashMap<>();

	public static void init()
	{
		Crops.instance = new Ic2Crops();
		Crops.weed = new CropWeed(Ic2CropType.weed);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.WATER, 10);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.WET, 10);
		Crops.instance.addBiomehumidityBonus(EnvProxy.BiomeType.DRY, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.JUNGLE, 10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.SWAMP, 10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.MUSHROOM, 5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.FOREST, 5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.RIVER, 2);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.PLAINS, 0);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.SAVANNA, -2);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.HILLS, -5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.MOUNTAIN, -5);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.WASTELAND, -8);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.END, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.NETHER, -10);
		Crops.instance.addBiomenutrientsBonus(EnvProxy.BiomeType.DEAD, -10);
		registerCrops();
		registerBaseSeeds();
	}

	public static void registerCrops()
	{
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
		Crops.instance.registerCrop(cropStickyReed);
		Crops.instance.registerCrop(cropCocoa);
		Crops.instance.registerCrop(cropFlax);
		Crops.instance.registerCrop(cropFerru);
		Crops.instance.registerCrop(cropAurelia);
		Crops.instance.registerCrop(cropRedWheat);
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
		Crops.instance.registerCrop(cropBeetroots);
		Crops.instance.registerCrop(cropOakSapling);
		Crops.instance.registerCrop(cropSpruceSapling);
		Crops.instance.registerCrop(cropBirchSapling);
		Crops.instance.registerCrop(cropJungleSapling);
		Crops.instance.registerCrop(cropAcaciaSapling);
		Crops.instance.registerCrop(cropDarkOakSapling);
		Crops.instance.registerCrop(cropBlazereed);
		Crops.instance.registerCrop(cropBobsYerUncleRanksBerries);
		Crops.instance.registerCrop(cropCorium);
		Crops.instance.registerCrop(cropCorpsePlant);
		Crops.instance.registerCrop(cropCreeperWeed);
		Crops.instance.registerCrop(cropDiareed);
		Crops.instance.registerCrop(cropEggPlant);
		Crops.instance.registerCrop(cropEnderBlossom);
		Crops.instance.registerCrop(cropMeatRose);
		Crops.instance.registerCrop(cropMilkWart);
		Crops.instance.registerCrop(cropOilBerries);
		Crops.instance.registerCrop(cropSlimePlant);
		Crops.instance.registerCrop(cropSpidernip);
		Crops.instance.registerCrop(cropTearstalks);
		Crops.instance.registerCrop(cropWithereed);
	}

	public static void registerBaseSeeds()
	{
		Crops.instance.registerBaseSeed(new ItemStack(Items.WHEAT_SEEDS), cropWheat, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.PUMPKIN_SEEDS), cropPumpkin, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.MELON_SEEDS), cropMelon, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.NETHER_WART), cropNetherWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2Items.TERRA_WART), cropTerraWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2Items.COFFEE_BEANS), cropCoffee, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.SUGAR_CANE), cropReed, 0, 3, 0, 2);
		Crops.instance.registerBaseSeed(new ItemStack(Items.COCOA_BEANS), cropCocoa, 0, 0, 0, 0);
		Crops.instance.registerBaseSeed(new ItemStack(Items.POPPY, 4), cropRedFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.DANDELION, 4), cropYellowFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.CARROT), cropCarrots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.POTATO, 1), cropPotato, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.BROWN_MUSHROOM, 4), cropBrownMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.RED_MUSHROOM, 4), cropRedMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.CACTUS), cropEatingPlant, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.BEETROOT_SEEDS), cropBeetroots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.OAK_SAPLING), cropOakSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.SPRUCE_SAPLING), cropSpruceSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.BIRCH_SAPLING), cropBirchSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.JUNGLE_SAPLING), cropJungleSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.ACACIA_SAPLING), cropAcaciaSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.DARK_OAK_SAPLING), cropDarkOakSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2Items.MILK_WART), cropMilkWart, 0, 1, 1, 1);
	}

	@Override
	public void addBiomenutrientsBonus(EnvProxy.BiomeType type, int nutrientsBonus)
	{
		this.nutrientBiomeTypeBonus.put(type, nutrientsBonus);
	}

	@Override
	public void addBiomehumidityBonus(EnvProxy.BiomeType type, int humidityBonus)
	{
		this.humidityBiomeTypeBonus.put(type, humidityBonus);
	}

	@Override
	public int getHumidityBiomeBonus(Holder<Biome> biome)
	{
		int ret = 0;

		for (EnvProxy.BiomeType type : IC2.envProxy.getBiomeTypes(biome))
		{
			Integer val = this.humidityBiomeTypeBonus.get(type);
			if (val != null && val > ret)
			{
				ret = val;
			}
		}

		return ret;
	}

	@Override
	public int getNutrientBiomeBonus(Holder<Biome> biome)
	{
		int ret = 0;

		for (EnvProxy.BiomeType type : IC2.envProxy.getBiomeTypes(biome))
		{
			Integer val = this.nutrientBiomeTypeBonus.get(type);
			if (val != null && val > ret)
			{
				ret = val;
			}
		}

		return ret;
	}

	@Override
	public CropCard getCropCard(String owner, String name)
	{
		Map<String, CropCard> map = this.cropMap.get(owner);
		return map == null ? null : map.get(name);
	}

	@Override
	public CropCard getCropCard(ItemStack stack)
	{
		ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
		if (stack.is(ItemTags.SAPLINGS) && identifier.getNamespace().equals("minecraft"))
		{
			return this.getCropCard("ic2", identifier.getPath());
		} else
		{
			CompoundTag nbt = stack.getTag();
			if (nbt == null)
			{
				return null;
			} else
			{
				return nbt.contains("owner") && nbt.contains("id") ? this.getCropCard(nbt.getString("owner"), nbt.getString("id")) : null;
			}
		}
	}

	@Override
	public CropCard getCropCard(Block cropBlock)
	{
		ResourceLocation cropIdentifier = ForgeRegistries.BLOCKS.getKey(cropBlock);
		String cropOwner = cropIdentifier.getNamespace();
		String cropName = cropIdentifier.getPath().replace("_crop", "");
		return this.getCropCard(cropOwner, cropName);
	}

	@Override
	public Collection<CropCard> getCrops()
	{
		return new AbstractCollection<>()
		{
			@Override
			public @NotNull Iterator<CropCard> iterator()
			{
				return new Iterator<>()
				{
					private final Iterator<Map<String, CropCard>> mapIterator = Ic2Crops.this.cropMap.values().iterator();
					private Iterator<CropCard> iterator = this.getNextIterator();

					@Override
					public boolean hasNext()
					{
						return this.iterator != null && this.iterator.hasNext();
					}

					public CropCard next()
					{
						if (this.iterator == null)
						{
							throw new NoSuchElementException("no more elements");
						}

						CropCard ret = this.iterator.next();
						if (!this.iterator.hasNext())
						{
							this.iterator = this.getNextIterator();
						}

						return ret;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException("This iterator is fromJson-only.");
					}

					private Iterator<CropCard> getNextIterator()
					{
						Iterator<CropCard> ret = null;

						while (this.mapIterator.hasNext() && ret == null)
						{
							ret = this.mapIterator.next().values().iterator();
							if (!ret.hasNext())
							{
								ret = null;
							}
						}

						return ret;
					}
				};
			}

			@Override
			public int size()
			{
				int ret = 0;

				for (Map<String, CropCard> map : Ic2Crops.this.cropMap.values())
				{
					ret += map.size();
				}

				return ret;
			}
		};
	}

	@Override
	public void registerCrop(CropCard crop)
	{
		String owner = crop.getOwner();
		String id = crop.getId();
		if (!owner.equals(owner.toLowerCase(Locale.ENGLISH)))
		{
			throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-lower case owner");
		}

		Map<String, CropCard> map = this.cropMap.computeIfAbsent(owner, k -> new HashMap<>());

		CropCard prev = map.put(id, crop);
		if (prev != null)
		{
			throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-unique owner+id pair");
		}
	}

	@Override
	public void registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance)
	{
		for (ItemStack key : this.baseSeeds.keySet())
		{
			if (key.getItem() == stack.getItem() && key.getDamageValue() == stack.getDamageValue())
			{
				return;
			}
		}

		this.baseSeeds.put(stack, new BaseSeed(crop, size, growth, gain, resistance));
	}

	@Override
	public BaseSeed getBaseSeed(ItemStack stack)
	{
		if (stack == null)
		{
			return null;
		}

		for (Entry<ItemStack, BaseSeed> entry : this.baseSeeds.entrySet())
		{
			if (entry.getKey().getItem() == stack.getItem())
			{
				return entry.getValue();
			}
		}

		return null;
	}
}
