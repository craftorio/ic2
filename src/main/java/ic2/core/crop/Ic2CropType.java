package ic2.core.crop;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropType;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum Ic2CropType implements StringRepresentable, ICropType
{
	none("none", null, 0),
	weed("weed", Ic2Blocks.WEED_CROP, 4),
	wheat("wheat", Ic2Blocks.WHEAT_CROP, 7),
	carrots("carrots", Ic2Blocks.CARROTS_CROP, 3),
	potato("potato", Ic2Blocks.POTATO_CROP, 3),
	beetroots("beetroots", Ic2Blocks.BEETROOTS_CROP, 3),
	pumpkin("pumpkin", Ic2Blocks.PUMPKIN_CROP, 3),
	melon("melon", Ic2Blocks.MELON_CROP, 3),
	dandelion("dandelion", Ic2Blocks.DANDELION_CROP, 3),
	poppy("poppy", Ic2Blocks.POPPY_CROP, 3),
	blackthorn("blackthorn", Ic2Blocks.BLACKTHORN_CROP, 3),
	tulip("tulip", Ic2Blocks.TULIP_CROP, 3),
	cyazint("cyazint", Ic2Blocks.CYAZINT_CROP, 3),
	venomilia("venomilia", Ic2Blocks.VENOMILIA_CROP, 5),
	reed("reed", Ic2Blocks.REED_CROP, 2),
	stickyReed("sticky_reed", Ic2Blocks.STICKY_REED_CROP, 3),
	cocoa("cocoa", Ic2Blocks.COCOA_CROP, 3),
	flax("flax", Ic2Blocks.FLAX_CROP, 3),
	redMushroom("red_mushroom", Ic2Blocks.RED_MUSHROOM_CROP, 2),
	brownMushroom("brown_mushroom", Ic2Blocks.BROWN_MUSHROOM_CROP, 2),
	netherWart("nether_wart", Ic2Blocks.NETHER_WART_CROP, 2),
	terraWart("terra_wart", Ic2Blocks.TERRA_WART_CROP, 2),
	oakSapling("oak_sapling", Ic2Blocks.OAK_SAPLING_CROP, 4),
	spruceSapling("spruce_sapling", Ic2Blocks.SPRUCE_SAPLING_CROP, 4),
	birchSapling("birch_sapling", Ic2Blocks.BIRCH_SAPLING_CROP, 4),
	jungleSapling("jungle_sapling", Ic2Blocks.JUNGLE_SAPLING_CROP, 4),
	acaciaSapling("acacia_sapling", Ic2Blocks.ACACIA_SAPLING_CROP, 4),
	darkOakSapling("dark_oak_sapling", Ic2Blocks.DARK_OAK_SAPLING_CROP, 4),
	ferru("ferru", Ic2Blocks.FERRU_CROP, 3),
	cyprium("cyprium", Ic2Blocks.CYPRIUM_CROP, 3),
	stagnium("stagnium", Ic2Blocks.STAGNIUM_CROP, 3),
	plumbiscus("plumbiscus", Ic2Blocks.PLUMBISCUS_CROP, 3),
	aurelia("aurelia", Ic2Blocks.AURELIA_CROP, 3),
	shining("shining", Ic2Blocks.SHINING_CROP, 3),
	redWheat("red_wheat", Ic2Blocks.RED_WHEAT_CROP, 6),
	coffee("coffee", Ic2Blocks.COFFEE_CROP, 4),
	hops("hops", Ic2Blocks.HOPS_CROP, 6),
	eatingPlant("eating_plant", Ic2Blocks.EATING_PLANT_CROP, 5),
	blazereed("blazereed", Ic2Blocks.BLAZEREED_CROP, 3),
	bobsYerUncleRanksBerries("bobs_yer_uncle_ranks_berries", Ic2Blocks.BOBS_YER_UNCLE_RANKS_BERRIES_CROP, 3),
	corium("corium", Ic2Blocks.CORIUM_CROP, 3),
	corpse_plant("corpse_plant", Ic2Blocks.CORPSE_PLANT_CROP, 3),
	creeper_weed("creeper_weed", Ic2Blocks.CREEPER_WEED_CROP, 3),
	diareed("diareed", Ic2Blocks.DIAREED_CROP, 3),
	egg_plant("egg_plant", Ic2Blocks.EGG_PLANT_CROP, 2),
	ender_blossom("ender_blossom", Ic2Blocks.ENDER_BLOSSOM_CROP, 3),
	meat_rose("meat_rose", Ic2Blocks.MEAT_ROSE_CROP, 3),
	milk_wart("milk_wart", Ic2Blocks.MILK_WART_CROP, 2),
	oil_berries("oil_berries", Ic2Blocks.OIL_BERRIES_CROP, 2),
	slime_plant("slime_plant", Ic2Blocks.SLIME_PLANT_CROP, 3),
	spidernip("spidernip", Ic2Blocks.SPIDERNIP_CROP, 3),
	tearstalks("tearstalks", Ic2Blocks.TEARSTALKS_CROP, 3),
	withereed("withereed", Ic2Blocks.WITHEREED_CROP, 3);

	private static final Ic2CropType[] values = values();
	private final String owner;
	private final String name;
	private final Block cropBlock;
	private final int maxAge;

	Ic2CropType(String cropName, Block cropBlock, int maxAge)
	{
		this.name = cropName;
		this.owner = "ic2";
		this.cropBlock = cropBlock;
		this.maxAge = maxAge;
	}

	Ic2CropType(String cropName, String owner, Block cropBlock, int maxAge)
	{
		this.name = cropName;
		this.owner = owner;
		this.cropBlock = cropBlock;
		this.maxAge = maxAge;
	}

	public static CropCard getCropCard(Ic2CropType cropType)
	{
		return Crops.instance.getCropCard(cropType.getOwner(), cropType.getName());
	}

	public static Ic2CropType getCropType(CropCard cropCard)
	{
		return valueOf(Util.toCamel(cropCard.getId()));
	}

	public String getSerializedName()
	{
		return this.name;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getOwner()
	{
		return this.owner;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public int getMaxAge()
	{
		return this.maxAge;
	}
}
