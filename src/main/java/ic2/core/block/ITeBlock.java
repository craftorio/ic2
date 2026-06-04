// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.creativetab.CreativeTabs;
import ic2.core.item.block.ItemBlockTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import ic2.core.ref.TeBlock;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import mcp.MethodsReturnNonnullByDefault;
import ic2.core.block.state.IIdProvider;

@MethodsReturnNonnullByDefault
public interface ITeBlock extends IIdProvider
{
    ResourceLocation getIdentifier();
    
    boolean hasItem();
    
    @Nullable
    Class<? extends TileEntityBlock> getTeClass();
    
    boolean hasActive();
    
    Set<EnumFacing> getSupportedFacings();
    
    float getHardness();
    
    float getExplosionResistance();
    
    TeBlock.HarvestTool getHarvestTool();
    
    TeBlock.DefaultDrop getDefaultDrop();
    
    EnumRarity getRarity();
    
    boolean allowWrenchRotating();
    
    default Material getMaterial() {
        return TeBlockRegistry.getInfo(this.getIdentifier()).getDefaultMaterial();
    }
    
    default boolean isTransparent() {
        return false;
    }
    
    default void setPlaceHandler(final TeBlock.ITePlaceHandler handler) {
        throw new UnsupportedOperationException();
    }
    
    @Nullable
    default TeBlock.ITePlaceHandler getPlaceHandler() {
        return null;
    }
    
    @Nullable
    @Deprecated
    TileEntityBlock getDummyTe();
    
    public interface ITeBlockCreativeRegisterer
    {
        void addSubBlocks(final NonNullList<ItemStack> p0, final BlockTileEntity p1, final ItemBlockTileEntity p2, final CreativeTabs p3);
    }
}
