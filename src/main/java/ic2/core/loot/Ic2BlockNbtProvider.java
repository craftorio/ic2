package ic2.core.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import ic2.core.block.tileentity.Ic2TileEntity;

import java.util.Set;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ic2BlockNbtProvider implements NbtProvider
{
	public static final Ic2BlockNbtProvider BLOCK_NBT = new Ic2BlockNbtProvider();

	public static final MapCodec<Ic2BlockNbtProvider> CODEC = MapCodec.unit(BLOCK_NBT);

	@Nullable
	public Tag get(LootContext context)
	{
		BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null)
		{
			return null;
		}

		if (blockEntity instanceof Ic2TileEntity tileEntity)
		{
			ItemStack stack = tileEntity.adjustDrop(state.getBlock().asItem().getDefaultInstance(), false);
			if (stack.has(DataComponents.CUSTOM_DATA))
			{
				return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
			}
		}

		return null;
	}

	public @NotNull Set<LootContextParam<?>> getReferencedContextParams()
	{
		return ImmutableSet.of();
	}

	public @NotNull LootNbtProviderType getType()
	{
		return Ic2LootNbtProviderTypes.BLOCK_NBT;
	}
}
