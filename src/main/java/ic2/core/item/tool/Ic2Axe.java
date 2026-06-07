package ic2.core.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class Ic2Axe extends AxeItem
{
	public Ic2Axe(Tier material, float attackDamage, float attackSpeed, Properties settings)
	{
		super(material, attackDamage, attackSpeed, settings);
	}
}
