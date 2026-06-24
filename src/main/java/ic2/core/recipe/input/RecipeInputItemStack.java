package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import ic2.core.util.StackUtil;

import java.util.Arrays;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class RecipeInputItemStack extends RecipeInputBase
{
	public final ItemStack input;

	public RecipeInputItemStack(ItemStack input)
	{
		if (StackUtil.isEmpty(input))
		{
			throw new IllegalArgumentException("invalid input stack");
		}

		this.input = input.copy();
	}

	@Override
	public boolean matches(ItemStack subject)
	{
		if (this.input.getItem() != subject.getItem()) return false;

		CompoundTag requiredNbt = this.input.getTag();
		if (requiredNbt == null || requiredNbt.isEmpty()) return true;

		CompoundTag subjectNbt = subject.getTag();
		if (subjectNbt == null)
		{
			// Subject has no NBT — check that all required keys map to default values (0 for numbers, etc.)
			for (String key : requiredNbt.getAllKeys())
			{
				if (!isDefaultTag(requiredNbt.get(key))) return false;
			}

			return true;
		}

		// Check required ⊆ subject (partial match)
		for (String key : requiredNbt.getAllKeys())
		{
			if (!subjectNbt.contains(key)) return false;
			if (!subjectNbt.get(key).equals(requiredNbt.get(key))) return false;
		}

		return true;
	}

	private static boolean isDefaultTag(Tag tag)
	{
		if (tag instanceof NumericTag num)
		{
			return num.getAsNumber().doubleValue() == 0.0;
		}

		if (tag instanceof StringTag str)
		{
			return str.getAsString().isEmpty();
		}

		if (tag instanceof ByteArrayTag byteArray)
		{
			return byteArray.isEmpty();
		}

		if (tag instanceof IntArrayTag intArray)
		{
			return intArray.isEmpty();
		}

		if (tag instanceof LongArrayTag longArray)
		{
			return longArray.isEmpty();
		}

		if (tag instanceof ListTag list)
		{
			return list.isEmpty();
		}

		if (tag instanceof CompoundTag compound)
		{
			return compound.isEmpty();
		}

		return false;
	}

	@Override
	public int getAmount()
	{
		return this.input.getCount();
	}

	@Override
	protected List<ItemStack> listStacks()
	{
		return Arrays.asList(this.input);
	}

	@Override
	public String toString()
	{
		return "RInputItemStack<" + StackUtil.setImmutableSize(this.input, this.getAmount()) + ">";
	}

	@Override
	public boolean equals(Object obj)
	{
		RecipeInputItemStack other;
		return obj != null
			&& this.getClass() == obj.getClass()
			&& StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStack) obj).input, this.input)
			&& other.getAmount() == this.getAmount();
	}

	@Override
	public JsonElement toJson()
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("item", BuiltInRegistries.ITEM.getKey(this.input.getItem()).toString());
		obj.add("data", (JsonElement) NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, this.input.getTag()));
		if (this.input.getCount() != 1)
		{
			obj.addProperty("count", this.input.getCount());
		}

		return obj;
	}
}
