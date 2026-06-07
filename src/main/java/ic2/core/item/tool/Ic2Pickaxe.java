package ic2.core.item.tool;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class Ic2Pickaxe extends PickaxeItem
{
	public Ic2Pickaxe(Tier material, int attackDamage, float attackSpeed, Properties settings)
	{
		super(material, attackDamage, attackSpeed, settings);
	}
}
