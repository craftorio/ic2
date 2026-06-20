package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.ElectricItemTooltipHandler;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class ItemArmorElectric extends ItemArmorIC2 implements IElectricItem
{
	public static final UUID[] MODIFIERS = new UUID[] {
		UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
		UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
		UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
		UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
	};
	protected final double maxCharge;
	protected final double transferLimit;
	protected final int tier;

	public ItemArmorElectric(Holder<ArmorMaterial> material, EquipmentSlot slot, Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(material, slot, settings);
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
	}

	public abstract int getEnergyPerDamage();

	public abstract double getDamageAbsorptionRatio();

	protected double getBaseAbsorptionRatio()
	{
		return switch (this.getEquipmentSlot())
		{
			case HEAD -> 0.15;
			case CHEST -> 0.4;
			case LEGS -> 0.3;
			case FEET -> 0.15;
			default -> 0.0;
		};
	}

	public static float damageArmor(Player entity, DamageSource source, float amount)
	{
		if (source == null || amount <= 0.0F || source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS))
		{
			return amount;
		}

		float remainingDamage = amount;

		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
			{
				continue;
			}

			ItemStack stack = entity.getItemBySlot(slot);
			if (stack.getItem() instanceof ItemArmorElectric electricArmor)
			{
				double absorptionRatio = electricArmor.getBaseAbsorptionRatio() * electricArmor.getDamageAbsorptionRatio();
				if (absorptionRatio <= 0.0)
				{
					continue;
				}

				int energyPerDamage = electricArmor.getEnergyPerDamage();
				if (energyPerDamage <= 0)
				{
					continue;
				}

				double availableEnergy = ElectricItem.manager.getCharge(stack);
				double maxAbsorbDamage = availableEnergy / energyPerDamage;
				double absorbedDamage = Math.min(remainingDamage * absorptionRatio, maxAbsorbDamage);

				if (absorbedDamage > 0.0)
				{
					electricArmor.damageArmor(entity, stack, source, absorbedDamage, slot);
					remainingDamage -= (float) absorbedDamage;
					if (remainingDamage <= 0.0F)
					{
						break;
					}
				}
			}
		}

		return remainingDamage;
	}

	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		ElectricItemManager.addChargeVariants(this, subItems);
	}

	public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag context)
	{
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
	}

	public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, double damage, EquipmentSlot slot)
	{
		ElectricItem.manager.discharge(stack, damage * this.getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return this.maxCharge;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return this.tier;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return this.transferLimit;
	}

	public boolean isBarVisible(ItemStack stack)
	{
		return ElectricItem.manager.getChargeLevel(stack) < 1.0;
	}

	public int getBarWidth(ItemStack stack)
	{
		return (int) Math.round(ElectricItem.manager.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(ItemStack stack)
	{
		return Mth.hsvToRgb((float) (ElectricItem.manager.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	public ItemAttributeModifiers getAttributeModifiers(ItemStack stack, EquipmentSlot slot)
	{
		if (slot != this.getEquipmentSlot())
		{
			return this.getDefaultAttributeModifiers();
		}

		boolean hasCharge = ElectricItem.manager.getCharge(stack) >= ((ItemArmorElectric) stack.getItem()).getEnergyPerDamage();
		if (!hasCharge)
		{
			return this.getDefaultAttributeModifiers();
		}

		Item armor = stack.getItem();
		int protection;
		if (armor instanceof ItemArmorNanoSuit)
		{
			protection = ItemArmorNanoSuit.CHARGED_PROTECTION[slot.ordinal()];
		} else
		{
			if (!(armor instanceof ItemArmorQuantumSuit))
			{
				return this.getDefaultAttributeModifiers();
			}

			protection = ItemArmorQuantumSuit.CHARGED_PROTECTION[slot.ordinal()];
		}

		ItemAttributeModifiers defaults = this.getDefaultAttributeModifiers();
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		UUID uuid = MODIFIERS[slot.ordinal()];
		EquipmentSlotGroup eSlotGroup = EquipmentSlotGroup.bySlot(slot);

		defaults.forEach(eSlotGroup, (attribute, modifier) -> {
			if (!attribute.equals(Attributes.ARMOR) || !modifier.id().equals(uuid))
			{
				builder.add(attribute, modifier, eSlotGroup);
			}
		});

		builder.add(Attributes.ARMOR, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ic2", "armor." + slot.getName()), protection, AttributeModifier.Operation.ADD_VALUE), eSlotGroup);
		return builder.build();
	}

	public ItemAttributeModifiers getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		return this.getAttributeModifiers(stack, slot);
	}
}
