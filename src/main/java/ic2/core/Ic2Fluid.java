// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import com.google.common.collect.UnmodifiableIterator;
import java.util.IdentityHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import ic2.core.ref.FluidName;
import net.minecraft.util.ResourceLocation;
import ic2.core.ref.IFluidModelProvider;
import net.minecraftforge.fluids.Fluid;

public class Ic2Fluid extends Fluid implements IFluidModelProvider
{
    private static final ResourceLocation fluidLocation;
    
    public Ic2Fluid(final FluidName name) {
        super(name.getName(), name.getTextureLocation(false), name.getTextureLocation(true));
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final FluidName name) {
        if (!name.getInstance().canBePlacedInWorld()) {
            return;
        }
        final String variant = "type=" + name.name();
        ModelLoader.setCustomStateMapper(this.getBlock(), (IStateMapper)new IStateMapper() {
            public Map<IBlockState, ModelResourceLocation> putStateModelLocations(final Block blockIn) {
                final Map<IBlockState, ModelResourceLocation> ret = new IdentityHashMap<IBlockState, ModelResourceLocation>();
                final ModelResourceLocation loc = new ModelResourceLocation(Ic2Fluid.fluidLocation, variant);
                for (final IBlockState state : Ic2Fluid.this.getBlock().getBlockState().getValidStates()) {
                    ret.put(state, loc);
                }
                return ret;
            }
        });
        final Item item = Item.getItemFromBlock(this.getBlock());
        if (item != null && item != Items.AIR) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Ic2Fluid.fluidLocation, variant));
        }
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(6);
    }
    
    static {
        fluidLocation = new ResourceLocation("ic2", "fluid");
    }
}
