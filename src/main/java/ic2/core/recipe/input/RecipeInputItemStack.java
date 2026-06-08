package ic2.core.recipe.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
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
		return StackUtil.checkItemEqualityStrict(this.input, subject);
	}

	@Override
	public int getAmount()
	{
		return this.input.getCount();
	}

	@Override
	protected List<ItemStack> listStacks()
	{
		return List.of(this.input);
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
