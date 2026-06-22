package ic2.core.item.armor.jetpack;

import com.google.gson.JsonObject;
import ic2.api.item.ElectricItem;
import ic2.core.init.IC2Config;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.ConfigUtil;

import java.text.ParseException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class JetpackAttachmentRecipe implements CraftingRecipe
{
	public static final Set<Item> blacklistedItems = Collections.newSetFromMap(new IdentityHashMap<>());
	private final ResourceLocation id;

	public JetpackAttachmentRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	public static void init()
	{
		try
		{
			for (ItemStack stack : ConfigUtil.asStackList(IC2Config.recipes.jetpackAttachmentBlacklist.get()))
			{
				blacklistedItems.add(stack.getItem());
			}
		} catch (ParseException pe)
		{
			throw new RuntimeException(pe);
		}

		blacklistedItems.add(Ic2Items.JETPACK);
		blacklistedItems.add(Ic2Items.JETPACK_ELECTRIC);
		blacklistedItems.add(Ic2Items.QUANTUM_CHESTPLATE);
		blacklistedItems.add(Items.ELYTRA);
	}

	public boolean matches(@NotNull CraftingContainer inv, @NotNull Level world)
	{
		return !this.assemble(inv, null).isEmpty();
	}

	public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, RegistryAccess registryAccess)
	{
		ItemStack jetpack = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		boolean attachmentPlate = false;

		for (int i = 0; i < inv.getContainerSize(); i++)
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
				} else if (Mob.getEquipmentSlotForItem(currentStack) == EquipmentSlot.CHEST && !blacklistedItems.contains(item))
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

	public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess)
	{
		return ItemStack.EMPTY;
	}

	public boolean canCraftInDimensions(int x, int y)
	{
		return x * y >= 3;
	}

	public boolean isSpecial()
	{
		return true;
	}

	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.create();
	}

	public @NotNull RecipeSerializer<?> getSerializer()
	{
		return Ic2RecipeSerializers.JETPACK_ATTACHMENT;
	}

	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

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
	}
}
