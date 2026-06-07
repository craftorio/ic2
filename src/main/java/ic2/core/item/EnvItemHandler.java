package ic2.core.item;

import com.mojang.authlib.GameProfile;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

public interface EnvItemHandler
{
	int fetch(BlockEntity var1, ItemStack var2, boolean var3);

	int deposit(BlockEntity var1, Direction var2, ItemStack var3, GameProfile var4, boolean var5);

	int deposit(EnvItemHandler.AdjacentInventory var1, ItemStack var2, boolean var3);

	int distribute(BlockEntity var1, ItemStack var2, boolean var3);

	@Nullable
	EnvItemHandler.AdjacentInventory getAdjacentInventory(BlockEntity var1, Direction var2);

	List<? extends EnvItemHandler.AdjacentInventory> getAdjacentInventories(BlockEntity var1);

	EnvItemHandler.AdjacentInventory wrapInventory(BlockEntity var1, Direction var2);

	int transfer(EnvItemHandler.AdjacentInventory var1, EnvItemHandler.AdjacentInventory var2, int var3);

	int transfer(EnvItemHandler.AdjacentInventory var1, EnvItemHandler.AdjacentInventory var2, int var3, Predicate<ItemStack> var4);

	@NonExtendable
	interface AdjacentInventory
	{
		Direction getSide();
	}
}
