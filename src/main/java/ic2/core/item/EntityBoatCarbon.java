package ic2.core.item;

import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBoatCarbon extends EntityIC2Boat
{
	public EntityBoatCarbon(World world)
	{
		super(world);
	}

	@Override
	protected ItemStack getItem()
	{
		return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.carbon);
	}

	@Override
	public String getTexture()
	{
		return "textures/models/boat_carbon.png";
	}
}
