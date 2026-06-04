// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import ic2.core.block.state.IIdProvider;
import ic2.core.IC2;
import net.minecraft.world.DimensionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.ITerraformingBP;
import ic2.core.item.ItemMulti;

public class Tfbp extends ItemMulti<TfbpType> implements ITerraformingBP
{
    public static void init() {
        for (final TfbpType tfbp : TfbpType.values()) {
            if (tfbp.logic != null) {
                tfbp.logic.init();
            }
        }
    }
    
    public Tfbp() {
        super(ItemName.tfbp, TfbpType.class);
        this.setMaxStackSize(1);
    }
    
    @Override
    public double getConsume(final ItemStack stack) {
        final TfbpType type = this.getType(stack);
        return (type == null) ? 0.0 : type.consume;
    }
    
    @Override
    public int getRange(final ItemStack stack) {
        final TfbpType type = this.getType(stack);
        return (type == null) ? 0 : type.range;
    }
    
    @Override
    public boolean canInsert(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos) {
        final TfbpType type = this.getType(stack);
        if (type == null) {
            return false;
        }
        if (type == TfbpType.cultivation && world.provider.getDimensionType() == DimensionType.THE_END) {
            IC2.achievements.issueAchievement(player, "terraformEndCultivation");
        }
        return true;
    }
    
    @Override
    public boolean terraform(final ItemStack stack, final World world, final BlockPos pos) {
        final TfbpType type = this.getType(stack);
        return type != null && type.logic != null && type.logic.terraform(world, pos);
    }
    
    public enum TfbpType implements IIdProvider
    {
        blank(0.0, 0, (TerraformerBase)null), 
        chilling(2000.0, 50, (TerraformerBase)new Chilling()), 
        cultivation(4000.0, 40, (TerraformerBase)new Cultivation()), 
        desertification(2500.0, 40, (TerraformerBase)new Desertification()), 
        flatification(4000.0, 40, (TerraformerBase)new Flatification()), 
        irrigation(3000.0, 60, (TerraformerBase)new Irrigation()), 
        mushroom(8000.0, 25, (TerraformerBase)new Mushroom());
        
        public final double consume;
        public final int range;
        final TerraformerBase logic;
        
        private TfbpType(final double consume, final int range, final TerraformerBase logic) {
            this.consume = consume;
            this.range = range;
            this.logic = logic;
        }
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            return this.ordinal();
        }
    }
}
