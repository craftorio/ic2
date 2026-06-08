package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.StackUtil;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemArmorNightVisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo
{
	public ItemArmorNightVisionGoggles(Properties settings)
	{
		super(Ic2ArmorMaterials.NIGHT_VISION_GOGGLES, settings, EquipmentSlot.HEAD);
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

	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player player)
		{
			if (slot == this.slot.getIndex())
			{
				CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
				boolean active = nbtData.getBoolean("active");
				byte toggleTimer = nbtData.getByte("toggleTimer");
				if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					active = !active;
					if (IC2.sideProxy.isSimulating())
					{
						nbtData.putBoolean("active", active);
						if (active)
						{
							IC2.sideProxy.messagePlayer(player, "Nightvision enabled.");
						} else
						{
							IC2.sideProxy.messagePlayer(player, "Nightvision disabled.");
						}
					}
				}

				if (IC2.sideProxy.isSimulating() && toggleTimer > 0)
				{
					nbtData.putByte("toggleTimer", --toggleTimer);
				}

				if (active && IC2.sideProxy.isSimulating())
				{
					int skylight = player.getCommandSenderWorld().getMaxLocalRawBrightness(new BlockPos(player.position()));
					if (skylight <= 8 && ElectricItem.manager.use(stack, 1.0, player))
					{
						player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
						return;
					}
				}

				if (IC2.sideProxy.isSimulating())
				{
					player.removeEffect(MobEffects.NIGHT_VISION);
				}
			}
		}
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		if (this.allowedIn(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public boolean isValidRepairItem(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}
}
