// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.audio;

import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.lang.ref.WeakReference;

public class AudioPosition
{
    private final WeakReference<World> worldRef;
    public final float x;
    public final float y;
    public final float z;
    
    public static AudioPosition getFrom(final Object obj, final PositionSpec positionSpec) {
        if (obj instanceof AudioPosition) {
            return (AudioPosition)obj;
        }
        if (obj instanceof Entity) {
            final Entity e = (Entity)obj;
            return new AudioPosition(e.getEntityWorld(), (float)e.posX, (float)e.posY, (float)e.posZ);
        }
        if (obj instanceof TileEntity) {
            final TileEntity te = (TileEntity)obj;
            return new AudioPosition(te.getWorld(), te.getPos().getX() + 0.5f, te.getPos().getY() + 0.5f, te.getPos().getZ() + 0.5f);
        }
        return null;
    }
    
    public AudioPosition(final World world, final float x, final float y, final float z) {
        this.worldRef = new WeakReference<World>(world);
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public AudioPosition(final World world, final BlockPos pos) {
        this(world, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
    }
    
    public World getWorld() {
        return this.worldRef.get();
    }
}
