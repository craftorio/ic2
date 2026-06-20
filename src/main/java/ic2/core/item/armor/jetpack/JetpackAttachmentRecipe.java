package ic2.core.item.armor.jetpack;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.item.ElectricItem;
import ic2.core.init.MainConfig;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.ConfigUtil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record JetpackAttachmentRecipe(ResourceLocation id) implements CraftingRecipe
{
	public static final Set<Item> blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void init()
	{
		for (ItemStack stack : ConfigUtil.asStackList(MainConfig.get(), "recipes/jetpackAttachmentBlacklist"))
		{
			blacklistedItems.add(stack.getItem());
		}

		blacklistedItems.add(Ic2Items.JETPACK);
		blacklistedItems.add(Ic2Items.JETPACK_ELECTRIC);
		blacklistedItems.add(Ic2Items.QUANTUM_CHESTPLATE);
		blacklistedItems.add(Items.ELYTRA);
	}

	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world)
	{
		return !this.assemble(inv, null).isEmpty();
	}

	public @NotNull ItemStack assemble(@NotNull CraftingInput inv, HolderLookup.@NotNull Provider registryAccess)
	{
		ItemStack jetpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		boolean attachmentPlate = false;

		for (int i = 0; i < inv.size(); i++)
		{
			ItemStack currentStack = inv.getItem(i);
			if (!currentStack.isEmpty())
			{
				Item item = currentStack.getItem();
				if (item == Ic2Items.JETPACK_ELECTRIC)
				{
					if (!jetpack.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					jetpack = currentStack;
				} else if (currentStack.getEquipmentSlot() == EquipmentSlot.CHEST && !blacklistedItems.contains(item))
				{
					if (!armor.isEmpty())
					{
						return ItemStack.EMPTY;
					}

					armor = currentStack;
				} else
				{
					if (item != Ic2Items.JETPACK_ATTACHMENT_PLATE || attachmentPlate)
					{
						return ItemStack.EMPTY;
					}

					attachmentPlate = true;
				}
			}
		}

		if (!jetpack.isEmpty() && !armor.isEmpty() && attachmentPlate && !JetpackHandler.hasJetpackAttached(armor))
		{
			ItemStack ret = armor.copy();
			JetpackHandler.setJetpackAttached(ret, true);
			ElectricItem.manager.charge(ret, ElectricItem.manager.getCharge(jetpack), Integer.MAX_VALUE, true, false);
			return ret;
		} else
		{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int x, int y)
	{
		return x * y >= 3;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.create();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return Ic2RecipeSerializers.JETPACK_ATTACHMENT;
	}

	@Override
	public @NotNull ResourceLocation id()
	{
		return this.id;
	}

	@Override
	public @NotNull CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}

	public static final class Serializer implements RecipeSerializer<JetpackAttachmentRecipe>
	{
		public @NotNull JetpackAttachmentRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json)
		{
			return new JetpackAttachmentRecipe(id);
		}

		public JetpackAttachmentRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf)
		{
			return new JetpackAttachmentRecipe(id);
		}

		public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull JetpackAttachmentRecipe recipe)
		{
		}

		@Override
		public MapCodec<JetpackAttachmentRecipe> codec()
		{
			return new MapCodec<>()
			{
				@Override
				public <T> Stream<T> keys(DynamicOps<T> ops)
				{
					return Stream.of(ops.createString("type"));
				}

				@Override
				public <T> DataResult<JetpackAttachmentRecipe> decode(DynamicOps<T> ops, MapLike<T> input)
				{
					try
					{
						return DataResult.success(fromJson(ResourceLocation.fromNamespaceAndPath("ic2", "jetpack_attachment"), new JsonObject()));
					} catch (Exception e)
					{
						return DataResult.error(() -> "Failed to decode JetpackAttachmentRecipe: " + e.getMessage());
					}
				}

				@Override
				public <T> RecordBuilder<T> encode(JetpackAttachmentRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix)
				{
					return prefix.withErrorsFrom(DataResult.error(() -> "Encoding JetpackAttachmentRecipe not supported"));
				}
			};
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, JetpackAttachmentRecipe> streamCodec()
		{
			return StreamCodec.of(
				this::toNetwork,
				buf -> this.fromNetwork(ResourceLocation.fromNamespaceAndPath("ic2", "jetpack_attachment"), buf)
			);
		}
	}
}
