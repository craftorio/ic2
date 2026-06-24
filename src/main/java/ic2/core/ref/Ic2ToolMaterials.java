package ic2.core.ref;

import java.util.function.Supplier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public enum Ic2ToolMaterials implements Tier
{
	BRONZE(2, 350, 6.0F, 2.0F, 14, () -> Ingredient.of(Ic2Items.BRONZE_INGOT)),
	CHAINSAW(3, 250, 12.0F, 9.0F, 14, Ingredient::of);

	private final int miningLevel;
	private final int itemDurability;
	private final float miningSpeed;
	private final float attackDamage;
	private final int enchantAbility;
	private final Supplier<Ingredient> repairIngredient;

	Ic2ToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantAbility, Supplier<Ingredient> repairIngredient)
	{
		this.miningLevel = miningLevel;
		this.itemDurability = itemDurability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantAbility = enchantAbility;
		this.repairIngredient = repairIngredient;
	}

	public int getUses()
	{
		return this.itemDurability;
	}

	public float getSpeed()
	{
		return this.miningSpeed;
	}

	public float getAttackDamageBonus()
	{
		return this.attackDamage;
	}

	public int getLevel()
	{
		return this.miningLevel;
	}

	public int getEnchantmentValue()
	{
		return this.enchantAbility;
	}

	public @NotNull Ingredient getRepairIngredient()
	{
		return this.repairIngredient.get();
	}
}
