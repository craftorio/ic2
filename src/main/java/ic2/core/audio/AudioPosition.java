package ic2.core.audio;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AudioPosition
{
	private final WeakReference<World> worldRef;

	public final float x;

	public final float y;

	public final float z;

	public static AudioPosition getFrom(Object obj, PositionSpec positionSpec)
	{
		if (obj instanceof AudioPosition)
			return (AudioPosition) obj;
		if (obj instanceof Entity)
		{
			Entity e = (Entity) obj;
			return new AudioPosition(e.func_130014_f_(), (float) e.field_70165_t, (float) e.field_70163_u, (float) e.field_70161_v);
		}
		if (obj instanceof TileEntity)
		{
			TileEntity te = (TileEntity) obj;
			return new AudioPosition(te.getWorld(), te.getPos().func_177958_n() + 0.5F, te.getPos().func_177956_o() + 0.5F, te.getPos().func_177952_p() + 0.5F);
		}
		return null;
	}

	public AudioPosition(World world, float x, float y, float z)
	{
		this.worldRef = new WeakReference<>(world);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public AudioPosition(World world, BlockPos pos)
	{
		this(world, pos.func_177958_n() + 0.5F, pos.func_177956_o() + 0.5F, pos.func_177952_p() + 0.5F);
	}

	public World getWorld()
	{
		return this.worldRef.get();
	}
}
