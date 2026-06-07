package ic2.core.crop;

import ic2.api.crops.BaseSeed;
import ic2.api.crops.CropCard;
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
import net.minecraft.core.Registry;
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

public class Ic2Crops extends Crops
{
	private final Map<EnvProxy.BiomeType, Integer> humidityBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<EnvProxy.BiomeType, Integer> nutrientBiomeTypeBonus = new IdentityHashMap<>();
	private final Map<ItemStack, BaseSeed> baseSeeds = new HashMap<>();
	public static CropCard cropWheat = new CropWheat(Ic2CropType.wheat);
	public static CropCard cropPumpkin = new CropPumpkin(Ic2CropType.pumpkin);
	public static CropCard cropMelon = new CropMelon(Ic2CropType.melon);
	public static CropCard cropYellowFlower = new CropColorFlower(
		Ic2CropType.dandelion, Ic2Blocks.DANDELION_CROP, new String[] { "Yellow", "Flower" }, DyeColor.YELLOW
	);
	public static CropCard cropRedFlower = new CropColorFlower(Ic2CropType.poppy, Ic2Blocks.POPPY_CROP, new String[] { "Red", "Flower", "Rose" }, DyeColor.RED);
	public static CropCard cropBlackFlower = new CropColorFlower(
		Ic2CropType.blackthorn, Ic2Blocks.BLACKTHORN_CROP, new String[] { "Black", "Flower", "Rose" }, DyeColor.BLACK
	);
	public static CropCard cropPurpleFlower = new CropColorFlower(
		Ic2CropType.tulip, Ic2Blocks.TULIP_CROP, new String[] { "Purple", "Flower", "Tulip" }, DyeColor.PURPLE
	);
	public static CropCard cropBlueFlower = new CropColorFlower(Ic2CropType.cyazint, Ic2Blocks.CYAZINT_CROP, new String[] { "Blue", "Flower" }, DyeColor.CYAN);
	public static CropCard cropVenomilia = new CropVenomilia(Ic2CropType.venomilia);
	public static CropCard cropReed = new CropReed(Ic2CropType.reed);
	public static CropCard cropStickyReed = new CropStickyReed(Ic2CropType.stickyReed);
	public static CropCard cropCocoa = new CropCocoa(Ic2CropType.cocoa);
	public static CropCard cropFlax = new CropFlax(Ic2CropType.flax);
	public static CropCard cropRedMushroom = new CropBaseMushroom(
		Ic2CropType.redMushroom, Ic2Blocks.RED_MUSHROOM_CROP, new String[] { "Red", "Food", "Mushroom" }, new ItemStack(Blocks.f_50073_)
	);
	public static CropCard cropBrownMushroom = new CropBaseMushroom(
		Ic2CropType.brownMushroom, Ic2Blocks.BROWN_MUSHROOM_CROP, new String[] { "Brown", "Food", "Mushroom" }, new ItemStack(Blocks.f_50072_)
	);
	public static CropCard cropNetherWart = new CropNetherWart(Ic2CropType.netherWart);
	public static CropCard cropTerraWart = new CropTerraWart(Ic2CropType.terraWart);
	public static CropCard cropOakSapling = new CropBaseSapling(
		Ic2CropType.oakSapling, Ic2Blocks.OAK_SAPLING_CROP, "acorns", new ItemStack(Blocks.f_49999_), new ItemStack(Blocks.f_50746_)
	);
	public static CropCard cropSpruceSapling = new CropBaseSapling(
		Ic2CropType.spruceSapling, Ic2Blocks.SPRUCE_SAPLING_CROP, "pine_cones", new ItemStack(Blocks.f_50000_), new ItemStack(Blocks.f_50747_)
	);
	public static CropCard cropBirchSapling = new CropBaseSapling(
		Ic2CropType.birchSapling, Ic2Blocks.BIRCH_SAPLING_CROP, "catkins", new ItemStack(Blocks.f_50001_), new ItemStack(Blocks.f_50748_)
	);
	public static CropCard cropJungleSapling = new CropBaseSapling(
		Ic2CropType.jungleSapling, Ic2Blocks.JUNGLE_SAPLING_CROP, "seedling", new ItemStack(Blocks.f_50002_), new ItemStack(Blocks.f_50749_)
	);
	public static CropCard cropAcaciaSapling = new CropBaseSapling(
		Ic2CropType.acaciaSapling, Ic2Blocks.ACACIA_SAPLING_CROP, "seedling", new ItemStack(Blocks.f_50003_), new ItemStack(Blocks.f_50750_)
	);
	public static CropCard cropDarkOakSapling = new CropBaseSapling(
		Ic2CropType.darkOakSapling, Ic2Blocks.DARK_OAK_SAPLING_CROP, "acorns", new ItemStack(Blocks.f_50004_), new ItemStack(Blocks.f_50751_)
	);
	public static CropCard cropFerru = new CropBaseMetalCommon(
		Ic2CropType.ferru,
		Ic2Blocks.FERRU_CROP,
		new String[] { "Gray", "Leaves", "Metal" },
		Arrays.asList(BlockTags.f_144258_, Ic2BlockTags.IRON_BLOCKS),
		new ItemStack(Ic2Items.SMALL_IRON_DUST)
	);
	public static CropCard cropCyprium = new CropBaseMetalCommon(
		Ic2CropType.cyprium,
		Ic2Blocks.CYPRIUM_CROP,
		new String[] { "Orange", "Leaves", "Metal" },
		Arrays.asList(BlockTags.f_144264_, Ic2BlockTags.COPPER_BLOCKS),
		new ItemStack(Ic2Items.SMALL_COPPER_DUST)
	);
	public static CropCard cropStagnium = new CropBaseMetalCommon(
		Ic2CropType.stagnium,
		Ic2Blocks.STAGNIUM_CROP,
		new String[] { "Shiny", "Leaves", "Metal" },
		Arrays.asList(Ic2BlockTags.TIN_ORES, Ic2BlockTags.TIN_BLOCKS),
		new ItemStack(Ic2Items.SMALL_TIN_DUST)
	);
	public static CropCard cropPlumbiscus = new CropBaseMetalCommon(
		Ic2CropType.plumbiscus,
		Ic2Blocks.PLUMBISCUS_CROP,
		new String[] { "Dense", "Leaves", "Metal" },
		Arrays.asList(Ic2BlockTags.LEAD_ORES, Ic2BlockTags.LEAD_BLOCKS),
		new ItemStack(Ic2Items.SMALL_LEAD_DUST)
	);
	public static CropCard cropAurelia = new CropBaseMetalUncommon(
		Ic2CropType.aurelia,
		Ic2Blocks.AURELIA_CROP,
		new String[] { "Gold", "Leaves", "Metal" },
		Arrays.asList(BlockTags.f_13043_, Ic2BlockTags.GOLD_BLOCKS),
		new ItemStack(Ic2Items.SMALL_GOLD_DUST)
	);
	public static CropCard cropShining = new CropBaseMetalUncommon(
		Ic2CropType.shining,
		Ic2Blocks.SHINING_CROP,
		new String[] { "Silver", "Leaves", "Metal" },
		Arrays.asList(Ic2BlockTags.SILVER_ORES, Ic2BlockTags.SILVER_BLOCKS),
		new ItemStack(Ic2Items.SMALL_SILVER_DUST)
	);
	public static CropCard cropRedWheat = new CropRedWheat(Ic2CropType.redWheat);
	public static CropCard cropCoffee = new CropCoffee(Ic2CropType.coffee);
	public static CropCard cropHops = new CropHops(Ic2CropType.hops);
	public static CropCard cropCarrots = new CropCarrots(Ic2CropType.carrots);
	public static CropCard cropPotato = new CropPotato(Ic2CropType.potato);
	public static CropCard cropEatingPlant = new CropEating(Ic2CropType.eatingPlant);
	public static CropCard cropBeetroots = new CropBeetroots(Ic2CropType.beetroots);
	static boolean needsToPost = true;
	private final Map<String, Map<String, CropCard>> cropMap = new HashMap<>();

	public static void init()
	{
		Crops.instance = new Ic2Crops();
		Crops.weed = new CropWeed(Ic2CropType.weed);
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
	}

	public static void registerBaseSeeds()
	{
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42404_), cropWheat, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42577_), cropPumpkin, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42578_), cropMelon, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42588_), cropNetherWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2Items.TERRA_WART), cropTerraWart, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Ic2Items.COFFEE_BEANS), cropCoffee, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_41909_), cropReed, 0, 3, 0, 2);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42533_), cropCocoa, 0, 0, 0, 0);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_41940_, 4), cropRedFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_41939_, 4), cropYellowFlower, 3, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42619_), cropCarrots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42620_, 1), cropPotato, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50072_, 4), cropBrownMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50073_, 4), cropRedMushroom, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50128_), cropEatingPlant, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Items.f_42733_), cropBeetroots, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50746_), cropOakSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50747_), cropSpruceSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50748_), cropBirchSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50749_), cropJungleSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50750_), cropAcaciaSapling, 0, 1, 1, 1);
		Crops.instance.registerBaseSeed(new ItemStack(Blocks.f_50751_), cropDarkOakSapling, 0, 1, 1, 1);
	}

	public static void ensureInit()
	{
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
		Integer ret = 0;

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
		Integer ret = 0;

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
		ResourceLocation identifier = Registry.f_122827_.getKey(stack.getItem());
		if (stack.m_204117_(ItemTags.f_13180_) && identifier.m_135827_().equals("minecraft"))
		{
			return this.getCropCard("ic2", identifier.m_135815_());
		} else
		{
			CompoundTag nbt = stack.getTag();
			if (nbt == null)
			{
				return null;
			} else
			{
				return nbt.contains("owner") && nbt.contains("id") ? this.getCropCard(nbt.m_128461_("owner"), nbt.m_128461_("id")) : null;
			}
		}
	}

	@Override
	public CropCard getCropCard(Block cropBlock)
	{
		ResourceLocation cropIdentifier = Registry.BLOCK.getKey(cropBlock);
		String cropOwner = cropIdentifier.m_135827_();
		String cropName = cropIdentifier.m_135815_().replace("_crop", "");
		return this.getCropCard(cropOwner, cropName);
	}

	@Override
	public Collection<CropCard> getCrops()
	{
		return new AbstractCollection<CropCard>()
		{
			@Override
			public Iterator<CropCard> iterator()
			{
				return new Iterator<CropCard>()
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
						throw new UnsupportedOperationException("This iterator is read-only.");
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

		Map<String, CropCard> map = this.cropMap.get(owner);
		if (map == null)
		{
			map = new HashMap<>();
			this.cropMap.put(owner, map);
		}

		CropCard prev = map.put(id, crop);
		if (prev != null)
		{
			throw new IllegalArgumentException("The crop owner=" + owner + " id=" + id + " uses a non-unique owner+id pair");
		}
	}

	@Override
	public boolean registerBaseSeed(ItemStack stack, CropCard crop, int size, int growth, int gain, int resistance)
	{
		for (ItemStack key : this.baseSeeds.keySet())
		{
			if (key.getItem() == stack.getItem() && key.getDamageValue() == stack.getDamageValue())
			{
				return false;
			}
		}

		this.baseSeeds.put(stack, new BaseSeed(crop, size, growth, gain, resistance));
		return true;
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
