package ic2.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IEntityAttackableItem
{
	boolean onAttackEntity(Player var1, Entity var2);
}
