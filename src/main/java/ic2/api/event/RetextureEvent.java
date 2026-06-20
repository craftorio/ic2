package ic2.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class RetextureEvent extends LevelEvent implements ICancellableEvent {

    public final BlockPos pos;

    public final BlockState state;

    public final Direction side;

    public final Player player;

    public final BlockState refState;

    public final String refVariant;

    public final Direction refSide;

    public final int[] refColorMultipliers;

    public boolean applied = false;

    public RetextureEvent(Level world, BlockPos pos, BlockState state, Direction side, Player player, BlockState refState, String refVariant, Direction refSide, int[] refColorMultipliers) {
        super(world);
        if (world == null) {
            throw new NullPointerException("null world");
        }
        if (world.isClientSide) {
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
