// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.beam;

import java.util.Collections;
import java.util.EnumMap;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import net.minecraft.util.EnumFacing;
import java.util.Map;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

public class TileAccelerator extends TileEntityElectricMachine
{
    private static final Map<EnumFacing.Axis, List<AxisAlignedBB>> FACING_AABBs;
    
    public TileAccelerator() {
        super(5000, 2);
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        return TileAccelerator.FACING_AABBs.get(this.getFacing().getAxis());
    }
    
    @Override
    protected int getLightOpacity() {
        return 0;
    }
    
    private static Map<EnumFacing.Axis, List<AxisAlignedBB>> makeAABBMap() {
        final Map<EnumFacing.Axis, List<AxisAlignedBB>> ret = new EnumMap<EnumFacing.Axis, List<AxisAlignedBB>>(EnumFacing.Axis.class);
        ret.put(EnumFacing.Axis.X, Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.25, 1.0, 1.0, 0.75)));
        ret.put(EnumFacing.Axis.Y, Collections.singletonList(new AxisAlignedBB(0.0, 0.25, 0.0, 1.0, 0.75, 1.0)));
        ret.put(EnumFacing.Axis.Z, Collections.singletonList(new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.0, 1.0)));
        return ret;
    }
    
    static {
        FACING_AABBs = makeAABBMap();
    }
}
