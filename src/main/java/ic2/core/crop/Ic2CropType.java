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
	eatingPlant("eating_plant", Ic2Blocks.EATING_PLANT_CROP, 5);

	private final String owner;
	private final String name;
	private final Block cropBlock;
	private final int maxAge;
	private static final Ic2CropType[] values = values();

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

	public String m_7912_()
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

	public static CropCard getCropCard(Ic2CropType cropType)
	{
		return Crops.instance.getCropCard(cropType.getOwner(), cropType.getName());
	}

	public static Ic2CropType getCropType(CropCard cropCard)
	{
		return valueOf(Util.toCamel(cropCard.getId()));
	}
}
