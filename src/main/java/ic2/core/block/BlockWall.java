// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.Ic2Player;
import ic2.api.event.RetextureEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2Color;

public class BlockWall extends BlockMultiID<Ic2Color>
{
    public static final Ic2Color defaultColor;
    
    public static BlockWall create() {
        return BlockMultiID.create(BlockWall.class, Ic2Color.class, new Object[0]);
    }
    
    private BlockWall() {
        super(BlockName.wall, Material.ROCK);
        this.setHardness(3.0f);
        this.setResistance(30.0f);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)this.typeProperty, (Comparable)BlockWall.defaultColor));
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    public boolean recolorBlock(final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color) {
        final IBlockState state = world.getBlockState(pos);
        final Ic2Color type = this.getType(state);
        if (type == null) {
            return false;
        }
        final Ic2Color newColor = Ic2Color.get(color);
        if (type != newColor) {
            world.setBlockState(pos, state.withProperty((IProperty)this.typeProperty, (Comparable)newColor));
            return true;
        }
        return false;
    }
    
    @SubscribeEvent
    public void onRetexture(final RetextureEvent event) {
        if (event.state.getBlock() != this) {
            return;
        }
        final World world = event.getWorld();
        final Ic2Color color = (Ic2Color)event.state.getValue((IProperty)this.typeProperty);
        if (!ItemBlockTileEntity.placeTeBlock(null, (EntityLivingBase)Ic2Player.get(world), world, event.pos, EnumFacing.DOWN, new TileEntityWall(color))) {
            return;
        }
        final IBlockState newState = BlockName.te.getInstance().getDefaultState();
        final RetextureEvent event2 = new RetextureEvent(world, event.pos, newState, event.side, event.player, event.refState, event.refVariant, event.refSide, event.refColorMultipliers);
        MinecraftForge.EVENT_BUS.post((Event)event2);
        if (event2.applied) {
            event.setCanceled(event.applied = true);
        }
        else {
            world.setBlockState(event.pos, event.state);
        }
    }
    
    static {
        defaultColor = Ic2Color.light_gray;
    }
}
