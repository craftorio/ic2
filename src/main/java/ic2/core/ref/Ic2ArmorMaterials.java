package ic2.core.ref;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public final class Ic2ArmorMaterials
{
	private static EnumMap<ArmorItem.Type, Integer> createDefense(int boots, int leggings, int chestplate, int helmet)
	{
		EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
		map.put(ArmorItem.Type.BOOTS, boots);
		map.put(ArmorItem.Type.LEGGINGS, leggings);
		map.put(ArmorItem.Type.CHESTPLATE, chestplate);
		map.put(ArmorItem.Type.HELMET, helmet);
		map.put(ArmorItem.Type.BODY, 0);
		return map;
	}

	private static Holder<ArmorMaterial> create(String name, EnumMap<ArmorItem.Type, Integer> defense, int enchantValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient)
	{
		return Holder.direct(new ArmorMaterial(
			defense,
			enchantValue,
			equipSound,
			repairIngredient,
			List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("ic2", name))),
			toughness,
			knockbackResistance
		));
	}

	public static final Holder<ArmorMaterial> BRONZE = create("ic2_bronze", createDefense(2, 5, 6, 2), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Ic2Items.BRONZE_INGOT));
	public static final Holder<ArmorMaterial> ALLOY = create("ic2_alloy", createDefense(4, 7, 9, 4), 12, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, () -> Ingredient.of(Ic2Items.ALLOY));
	public static final Holder<ArmorMaterial> NANO_SUIT = create("ic2_nano", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> QUANTUM_SUIT = create("ic2_quantum", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> NIGHT_VISION_GOGGLES = create("ic2_night_vision", createDefense(3, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> HAZMAT = create("ic2_hazmat", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> CF_PACK = create("ic2_cf_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> JET_PACK = create("ic2_jet_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> JET_PACK_ELECTRIC = create("ic2_jet_pack_electric", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> BAT_PACK = create("ic2_bat_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> ADVANCED_BAT_PACK = create("ic2_advanced_bat_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> ENERGY_PACK = create("ic2_energy_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);
	public static final Holder<ArmorMaterial> LAP_PACK = create("ic2_lap_pack", createDefense(0, 0, 0, 0), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient::of);

	private Ic2ArmorMaterials()
	{
	}
}
