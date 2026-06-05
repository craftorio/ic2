package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.ref.TeBlock;
import java.util.Set;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface ITeBlock extends IIdProvider {
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

   default void setPlaceHandler(TeBlock.ITePlaceHandler handler) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   default TeBlock.ITePlaceHandler getPlaceHandler() {
      return null;
   }

   @Nullable
   @Deprecated
   TileEntityBlock getDummyTe();

   interface ITeBlockCreativeRegisterer {
      void addSubBlocks(NonNullList<ItemStack> var1, BlockTileEntity var2, ItemBlockTileEntity var3, CreativeTabs var4);
   }
}
