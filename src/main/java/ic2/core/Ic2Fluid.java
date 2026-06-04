package ic2.core;

import com.google.common.collect.UnmodifiableIterator;
import ic2.core.ref.FluidName;
import ic2.core.ref.IFluidModelProvider;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ic2Fluid extends Fluid implements IFluidModelProvider {
  public Ic2Fluid(FluidName name) {
    super(name.getName(), name
        .getTextureLocation(false), name
        .getTextureLocation(true));
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(FluidName name) {
    if (!name.getInstance().canBePlacedInWorld())
      return; 
    final String variant = "type=" + name.name();
    ModelLoader.setCustomStateMapper(getBlock(), new IStateMapper() {
          public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
            Map<IBlockState, ModelResourceLocation> ret = new IdentityHashMap<>();
            ModelResourceLocation loc = new ModelResourceLocation(Ic2Fluid.fluidLocation, variant);
            for (UnmodifiableIterator<IBlockState> unmodifiableIterator = Ic2Fluid.this.getBlock().getBlockState().getValidStates().iterator(); unmodifiableIterator.hasNext(); ) {
              IBlockState state = unmodifiableIterator.next();
              ret.put(state, loc);
            } 
            return ret;
          }
        });
    Item item = Item.getItemFromBlock(getBlock());
    if (item != null && item != Items.AIR)
      ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(fluidLocation, variant)); 
  }
  
  public String getUnlocalizedName() {
    return "ic2." + super.getUnlocalizedName().substring(6);
  }
  
  private static final ResourceLocation fluidLocation = new ResourceLocation("ic2", "fluid");
}
