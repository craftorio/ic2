package ic2.core.ref;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public enum Ic2ArmorMaterials implements ArmorMaterial
{
	BRONZE("ic2:ic2_bronze", 15, new int[] { 2, 5, 6, 2 }, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Ic2Items.BRONZE_INGOT)),
	ALLOY("ic2:ic2_alloy", 50, new int[] { 4, 7, 9, 4 }, 12, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, () -> Ingredient.of(Ic2Items.ALLOY)),
	NANO_SUIT("ic2:ic2_nano", 0, new int[] { 0, 0, 0, 0 }, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of),
	QUANTUM_SUIT("ic2:ic2_quantum", 0, new int[] { 0, 0, 0, 0 }, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of),
	NIGHT_VISION_GOGGLES("ic2:ic2_night_vision", 0, new int[] { 3, 0, 0, 0 }, 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of),
	HAZMAT("ic2:ic2_hazmat", SoundEvents.ARMOR_EQUIP_LEATHER),
	CF_PACK("ic2:ic2_cf_pack", SoundEvents.ARMOR_EQUIP_IRON),
	JET_PACK("ic2:ic2_jet_pack", SoundEvents.ARMOR_EQUIP_IRON),
	BAT_PACK("ic2:ic2_bat_pack", SoundEvents.ARMOR_EQUIP_IRON),
	ADVANCED_BAT_PACK("ic2:ic2_advanced_bat_pack", SoundEvents.ARMOR_EQUIP_IRON),
	ENERGY_PACK("ic2:ic2_energy_pack", SoundEvents.ARMOR_EQUIP_IRON),
	LAP_PACK("ic2:ic2_lap_pack", SoundEvents.ARMOR_EQUIP_IRON);

	private static final int[] BASE_DURABILITY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int durabilityMultiplier;
	private final int[] protectionAmounts;
	private final int enchantAbility;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final Supplier<Ingredient> repairIngredientSupplier;

	Ic2ArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantAbility, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredientSupplier)
	{
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionAmounts = protectionAmounts;
		this.enchantAbility = enchantAbility;
		this.equipSound = equipSound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
		this.repairIngredientSupplier = repairIngredientSupplier;
	}

	Ic2ArmorMaterials(String name, SoundEvent equipSound)
	{
		this.name = name;
		this.durabilityMultiplier = 0;
		this.enchantAbility = 0;
		this.equipSound = equipSound;
		this.knockbackResistance = 0.0F;
		this.protectionAmounts = new int[] { 0, 0, 0, 0 };
		this.toughness = 0.0F;
		this.repairIngredientSupplier = Suppliers.memoize(Ingredient::of);
	}

	public int getDurabilityForType(ArmorItem.Type type)
	{
		return BASE_DURABILITY[type.getSlot().getIndex()] * this.durabilityMultiplier;
	}

	public int getDefenseForType(ArmorItem.Type type)
	{
		return this.protectionAmounts[type.getSlot().getIndex()];
	}

	public int getEnchantmentValue()
	{
		return this.enchantAbility;
	}

	public @NotNull SoundEvent getEquipSound()
	{
		return this.equipSound;
	}

	public @NotNull Ingredient getRepairIngredient()
	{
		return this.repairIngredientSupplier.get();
	}

	public @NotNull String getName()
	{
		return this.name;
	}

	public float getToughness()
	{
		return this.toughness;
	}

	public float getKnockbackResistance()
	{
		return this.knockbackResistance;
	}
}
