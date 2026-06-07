package ic2.core.ref;

import java.util.function.Supplier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum Ic2ToolMaterials implements Tier
{
	BRONZE(2, 350, 6.0F, 2.0F, 14, () -> Ingredient.m_43929_(new ItemLike[] { Ic2Items.BRONZE_INGOT })),
	CHAINSAW(3, 250, 12.0F, 9.0F, 14, Ingredient::m_151265_);

	private final int miningLevel;
	private final int itemDurability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairIngredient;

	Ic2ToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient)
	{
		this.miningLevel = miningLevel;
		this.itemDurability = itemDurability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairIngredient = repairIngredient;
	}

	public int m_6609_()
	{
		return this.itemDurability;
	}

	public float m_6624_()
	{
		return this.miningSpeed;
	}

	public float m_6631_()
	{
		return this.attackDamage;
	}

	public int m_6604_()
	{
		return this.miningLevel;
	}

	public int m_6601_()
	{
		return this.enchantability;
	}

	public Ingredient m_6282_()
	{
		return this.repairIngredient.get();
	}
}
