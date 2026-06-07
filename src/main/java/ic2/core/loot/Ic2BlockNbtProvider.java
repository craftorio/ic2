package ic2.core.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ic2.core.block.tileentity.Ic2TileEntity;

import java.util.Set;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import org.jetbrains.annotations.Nullable;

public class Ic2BlockNbtProvider implements NbtProvider
{
	public static final Ic2BlockNbtProvider BLOCK_NBT = new Ic2BlockNbtProvider();

	@Nullable
	public Tag m_142301_(LootContext context)
	{
		BlockEntity blockEntity = (BlockEntity) context.m_78953_(LootContextParams.f_81462_);
		BlockState state = (BlockState) context.m_78953_(LootContextParams.f_81461_);
		if (state == null)
		{
			return null;
		}

		if (blockEntity instanceof Ic2TileEntity tileEntity)
		{
			ItemStack stack = tileEntity.adjustDrop(state.getBlock().m_5456_().m_7968_(), false);
			if (stack.m_41782_())
			{
				return stack.getTag();
			}
		}

		return null;
	}

	public Set<LootContextParam<?>> m_142677_()
	{
		return ImmutableSet.of();
	}

	public LootNbtProviderType m_142624_()
	{
		return Ic2LootNbtProviderTypes.BLOCK_NBT;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<Ic2BlockNbtProvider>
	{
		public void toJson(JsonObject jsonObject, Ic2BlockNbtProvider ic2BlockNbtProvider, JsonSerializationContext jsonSerializationContext)
		{
		}

		public Ic2BlockNbtProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext)
		{
			return new Ic2BlockNbtProvider();
		}
	}
}
