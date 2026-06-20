package ic2.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

public interface IEntityAttackableItem
{
	boolean onAttackEntity(Player var1, Entity var2);
}
