package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemArmorNightvisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo
{
	public ItemArmorNightvisionGoggles()
	{
		super(ItemName.nightvision_goggles, "nightvision", EntityEquipmentSlot.HEAD);
		this.setMaxDamage(27);
		this.setNoRepair();
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return 200000.0;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return 1;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return 200.0;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		return info;
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
	{
		NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
		boolean active = nbtData.getBoolean("active");
		byte toggleTimer = nbtData.getByte("toggleTimer");
		if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
		{
			toggleTimer = 10;
			active = !active;
			if (IC2.platform.isSimulating())
			{
				nbtData.setBoolean("active", active);
				if (active)
				{
					IC2.platform.messagePlayer(player, "Nightvision enabled.");
				} else
				{
					IC2.platform.messagePlayer(player, "Nightvision disabled.");
				}
			}
		}

		if (IC2.platform.isSimulating() && toggleTimer > 0)
		{
			nbtData.setByte("toggleTimer", --toggleTimer);
		}

		boolean ret = false;
		if (active && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, player))
		{
			int skylight = player.getEntityWorld().getLightFromNeighbors(new BlockPos(player));
			if (skylight > 8)
			{
				IC2.platform.removePotion(player, MobEffects.NIGHT_VISION);
				player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
			} else
			{
				IC2.platform.removePotion(player, MobEffects.BLINDNESS);
				player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
			}

			ret = true;
		}

		if (ret)
		{
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}
}
