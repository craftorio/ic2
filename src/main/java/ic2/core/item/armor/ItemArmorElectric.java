package ic2.core.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.ElectricItemTooltipHandler;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
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

	public ItemArmorElectric(ArmorMaterial material, EquipmentSlot slot, Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(material, slot, settings);
		this.maxCharge = maxCharge;
		this.transferLimit = transferLimit;
		this.tier = tier;
	}

	public abstract int getEnergyPerDamage();

	public static void damageArmor(Player entity, DamageSource source, float amount)
	{
		if (!(amount <= 0.0F) && !source.isBypassMagic())
		{
			float damage = amount / 4.0F;
			if (damage < 1.0F)
			{
				damage = 1.0F;
			}

			for (EquipmentSlot slot : EquipmentSlot.values())
			{
				ItemStack stack = entity.getItemBySlot(slot);
				if (stack.getItem() instanceof ItemArmorElectric electricArmor)
				{
					electricArmor.damageArmor(entity, stack, source, damage, slot);
				}
			}
		}
	}

	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		if (this.allowedIn(tab))
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context)
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

	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot)
	{
		if (slot != this.slot)
		{
			return this.getDefaultAttributeModifiers(slot);
		}

		boolean hasCharge = ElectricItem.manager.getCharge(stack) >= ((ItemArmorElectric) stack.getItem()).getEnergyPerDamage();
		if (!hasCharge)
		{
			return this.getDefaultAttributeModifiers(slot);
		}

		Item armor = stack.getItem();
		int protection;
		if (armor instanceof ItemArmorNanoSuit)
		{
			protection = ItemArmorNanoSuit.CHARGED_PROTECTION[slot.getIndex()];
		} else
		{
			if (!(armor instanceof ItemArmorQuantumSuit))
			{
				return this.getDefaultAttributeModifiers(slot);
			}

			protection = ItemArmorQuantumSuit.CHARGED_PROTECTION[slot.getIndex()];
		}

		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		Attribute attr = Attributes.ARMOR;
		UUID uuid = MODIFIERS[slot.getIndex()];
		Collection<AttributeModifier> plain = this.getDefaultAttributeModifiers(slot).get(attr);
		if (plain != null)
		{
			for (AttributeModifier modifier : plain)
			{
				if (!modifier.getId().equals(uuid))
				{
					builder.put(attr, modifier);
				}
			}
		}

		builder.put(attr, new AttributeModifier(uuid, "Armor modifier", protection, Operation.ADDITION));
		return builder.build();
	}

	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		return this.getAttributeModifiers(stack, slot);
	}
}
