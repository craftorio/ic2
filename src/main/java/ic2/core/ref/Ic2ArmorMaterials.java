package ic2.core.ref;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum Ic2ArmorMaterials implements ArmorMaterial
{
	BRONZE("ic2_bronze", 15, new int[] { 2, 5, 6, 2 }, 9, SoundEvents.f_11677_, 0.0F, 0.0F, () -> Ingredient.m_43929_(new ItemLike[] { Ic2Items.BRONZE_INGOT })),
	ALLOY("ic2_alloy", 50, new int[] { 4, 7, 9, 4 }, 12, SoundEvents.f_11677_, 2.0F, 0.0F, () -> Ingredient.m_43929_(new ItemLike[] { Ic2Items.ALLOY })),
	NANO_SUIT("ic2_nano", 0, new int[] { 0, 0, 0, 0 }, 0, SoundEvents.f_11677_, 2.0F, 0.0F, Ingredient::m_151265_),
	QUANTUM_SUIT("ic2_quantum", 0, new int[] { 0, 0, 0, 0 }, 0, SoundEvents.f_11677_, 2.0F, 0.0F, Ingredient::m_151265_),
	NIGHT_VISION_GOGGLES("ic2_night_vision", 0, new int[] { 3, 0, 0, 0 }, 0, SoundEvents.f_11677_, 2.0F, 0.0F, Ingredient::m_151265_),
	HAZMAT("ic2_hazmat", SoundEvents.f_11678_),
	CF_PACK("ic2_cf_pack", SoundEvents.f_11677_),
	JET_PACK("ic2_jet_pack", SoundEvents.f_11677_);

	private static final int[] BASE_DURABILITY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int durabilityMultiplier;
	private final int[] protectionAmounts;
	private final int enchantability;
	private final SoundEvent equipSound;
	private final float toughness;
	private final float knockbackResistance;
	private final Supplier<Ingredient> repairIngredientSupplier;

	Ic2ArmorMaterials(
		String name,
		int durabilityMultiplier,
		int[] protectionAmounts,
		int enchantability,
		SoundEvent equipSound,
		float toughness,
		float knockbackResistance,
		Supplier<Ingredient> repairIngredientSupplier
	)
	{
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionAmounts = protectionAmounts;
		this.enchantability = enchantability;
		this.equipSound = equipSound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
		this.repairIngredientSupplier = repairIngredientSupplier;
	}

	Ic2ArmorMaterials(String name, SoundEvent equipSound)
	{
		this.name = name;
		this.durabilityMultiplier = 0;
		this.enchantability = 0;
		this.equipSound = equipSound;
		this.knockbackResistance = 0.0F;
		this.protectionAmounts = new int[] { 0, 0, 0, 0 };
		this.toughness = 0.0F;
		this.repairIngredientSupplier = Suppliers.memoize(Ingredient::m_151265_);
	}

	public int m_7366_(EquipmentSlot slot)
	{
		return BASE_DURABILITY[slot.m_20749_()] * this.durabilityMultiplier;
	}

	public int m_7365_(EquipmentSlot slot)
	{
		return this.protectionAmounts[slot.m_20749_()];
	}

	public int m_6646_()
	{
		return this.enchantability;
	}

	public SoundEvent m_7344_()
	{
		return this.equipSound;
	}

	public Ingredient m_6230_()
	{
		return this.repairIngredientSupplier.get();
	}

	public String m_6082_()
	{
		return this.name;
	}

	public float m_6651_()
	{
		return this.toughness;
	}

	public float m_6649_()
	{
		return this.knockbackResistance;
	}
}
