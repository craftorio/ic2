// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.event;

import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class RetextureEvent extends WorldEvent
{
    public final BlockPos pos;
    public final IBlockState state;
    public final EnumFacing side;
    public final EntityPlayer player;
    public final IBlockState refState;
    public final String refVariant;
    public final EnumFacing refSide;
    public final int[] refColorMultipliers;
    public boolean applied;
    
    public RetextureEvent(final World world, final BlockPos pos, final IBlockState state, final EnumFacing side, final EntityPlayer player, final IBlockState refState, final String refVariant, final EnumFacing refSide, final int[] refColorMultipliers) {
        super(world);
        this.applied = false;
        if (world == null) {
            throw new NullPointerException("null world");
        }
        if (world.isRemote) {
            throw new IllegalStateException("remote world");
        }
        if (pos == null) {
            throw new NullPointerException("null pos");
        }
        if (state == null) {
            throw new NullPointerException("null state");
        }
        if (side == null) {
            throw new NullPointerException("null side");
        }
        if (refState == null) {
            throw new NullPointerException("null refState");
        }
        if (refVariant == null) {
            throw new NullPointerException("null refVariant");
        }
        if (refSide == null) {
            throw new NullPointerException("null refSide");
        }
        if (refColorMultipliers == null) {
            throw new NullPointerException("null refColorMultipliers");
        }
        this.pos = pos;
        this.state = state;
        this.side = side;
        this.player = player;
        this.refState = refState;
        this.refVariant = refVariant;
        this.refSide = refSide;
        this.refColorMultipliers = refColorMultipliers;
    }
}
