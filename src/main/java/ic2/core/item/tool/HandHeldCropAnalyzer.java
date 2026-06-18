package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.StackUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldCropAnalyzer extends HandHeldInventory
{
	public static final int SLOT_INPUT = 0;
	public static final int SLOT_OUTPUT = 1;
	public static final int SLOT_BATTERY = 2;

	public HandHeldCropAnalyzer(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, containerStack, 3);
	}

	public int getScannedLevel()
	{
		ItemStack output = this.inventory[SLOT_OUTPUT];
		return !StackUtil.isEmpty(output) && output.getItem() instanceof ICropSeed ? ((ICropSeed) output.getItem()).getScannedFromStack(output) : -1;
	}

	public CropCard getCrop()
	{
		return Crops.instance.getCropCard(this.inventory[SLOT_OUTPUT]);
	}

	public String getSeedName()
	{
		CropCard crop = this.getCrop();
		return crop != null ? crop.getUnlocalizedName() : "UNKNOWN";
	}

	public String getSeedTier()
	{
		CropCard crop = this.getCrop();
		if (crop == null) return "0";
		int tier = crop.getProperties().getTier();
		return switch (tier)
		{
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			case 6 -> "VI";
			case 7 -> "VII";
			case 8 -> "VIII";
			case 9 -> "IX";
			case 10 -> "X";
			case 11 -> "XI";
			case 12 -> "XII";
			case 13 -> "XIII";
			case 14 -> "XIV";
			case 15 -> "XV";
			case 16 -> "XVI";
			default -> "0";
		};
	}

	public String getSeedDiscoveredBy()
	{
		CropCard crop = this.getCrop();
		return crop != null ? crop.getDiscoveredBy() : "";
	}

	public String getSeedDesc(int i)
	{
		CropCard crop = this.getCrop();
		return crop != null ? crop.desc(i) : "";
	}

	public int getSeedGrowth()
	{
		ItemStack output = this.inventory[SLOT_OUTPUT];
		return !StackUtil.isEmpty(output) && output.getItem() instanceof ICropSeed ? ((ICropSeed) output.getItem()).getGrowthFromStack(output) : -1;
	}

	public int getSeedGain()
	{
		ItemStack output = this.inventory[SLOT_OUTPUT];
		return !StackUtil.isEmpty(output) && output.getItem() instanceof ICropSeed ? ((ICropSeed) output.getItem()).getGainFromStack(output) : -1;
	}

	public int getSeedResistance()
	{
		ItemStack output = this.inventory[SLOT_OUTPUT];
		return !StackUtil.isEmpty(output) && output.getItem() instanceof ICropSeed ? ((ICropSeed) output.getItem()).getResistanceFromStack(output) : -1;
	}

	public static int energyForLevel(int level)
	{
		return switch (level)
		{
			case 1 -> 90;
			case 2 -> 900;
			case 3 -> 9000;
			default -> 10;
		};
	}

	public void tryScan()
	{
		ItemStack input = this.inventory[SLOT_INPUT];
		ItemStack output = this.inventory[SLOT_OUTPUT];

		if (!StackUtil.isEmpty(output) || StackUtil.isEmpty(input) || !(input.getItem() instanceof ICropSeed seed))
		{
			return;
		}

		int level = seed.getScannedFromStack(input);
		if (level >= 4)
		{
			this.inventory[SLOT_OUTPUT] = input;
			this.inventory[SLOT_INPUT] = StackUtil.emptyStack;
			this.save();
			return;
		}

		double need = energyForLevel(level);
		if (!ElectricItem.manager.use(this.containerStack, need, this.player))
		{
			return;
		}

		seed.incrementScannedFromStack(input);
		this.inventory[SLOT_OUTPUT] = input;
		this.inventory[SLOT_INPUT] = StackUtil.emptyStack;
		this.save();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerAnalyzer(syncId, this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerAnalyzer(syncId, this);
	}
}
