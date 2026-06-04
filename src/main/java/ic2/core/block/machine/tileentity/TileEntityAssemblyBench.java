// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraft.util.ResourceLocation;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.world.World;
import net.minecraft.inventory.InventoryCrafting;
import java.util.Arrays;
import ic2.core.util.StackUtil;
import java.util.Queue;
import ic2.core.recipe.AdvRecipe;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import ic2.core.ref.TeBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import net.minecraft.item.crafting.IRecipe;
import java.util.List;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityAssemblyBench extends TileEntityBatchCrafter implements IHasGui, IUpgradableBlock
{
    public static final List<IRecipe> RECIPES;
    
    @Override
    protected IRecipe findRecipe() {
        for (final IRecipe recipe : TileEntityAssemblyBench.RECIPES) {
            if (recipe.matches(this.crafting, this.getWorld())) {
                return recipe;
            }
        }
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add("You probably want the " + Localization.translate(this.getBlockType().getUnlocalizedName() + '.' + TeBlock.replicator.getName()));
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    static {
        RECIPES = new ArrayList<IRecipe>();
    }
    
    public static class UuRecipe implements IRecipe
    {
        protected final ItemStack output;
        protected final boolean[][] shape;
        
        public static UuRecipe create(final ItemStack output, final Object... args) {
            final Queue<String> inputArrangement = new ArrayDeque<String>();
            for (final Object arg : args) {
                if (arg instanceof String) {
                    final String str = (String)arg;
                    if (str.isEmpty() || str.length() > 3) {
                        AdvRecipe.displayError("none or too many crafting columns", "Input: " + str + "\nSize: " + str.length(), output, false);
                    }
                    inputArrangement.add(str);
                }
            }
            final boolean[][] shape = new boolean[3][3];
            for (int y = 0; y < 3; ++y) {
                final String layer = inputArrangement.poll();
                for (int x = 0; x < 3; ++x) {
                    shape[y][x] = (layer.charAt(x) != ' ');
                }
            }
            return new UuRecipe(output, shape);
        }
        
        public UuRecipe(final ItemStack output, final boolean[][] shape) {
            if (StackUtil.isEmpty(output)) {
                AdvRecipe.displayError("Empty result", "UU recipe with shape " + Arrays.deepToString(shape), output, false);
            }
            final int inputWidth = shape[0].length;
            for (final boolean[] col : shape) {
                if (col.length != inputWidth) {
                    AdvRecipe.displayError("Inconsistent recipe shape", "UU recipe with shape " + Arrays.deepToString(shape), output, false);
                }
            }
            this.output = output;
            this.shape = shape;
        }
        
        public boolean matches(final InventoryCrafting inv, final World world) {
            final ItemStack uu = ItemName.misc_resource.getItemStack(MiscResourceType.matter);
            int y = 0;
            final int height = inv.getHeight();
            final int width = inv.getWidth();
            while (y < height) {
                final boolean[] layer = this.shape[y];
                for (int x = 0; x < width; ++x) {
                    final ItemStack stack = inv.getStackInRowAndColumn(x, y);
                    if (layer[x]) {
                        if (!StackUtil.checkItemEquality(stack, uu)) {
                            return false;
                        }
                    }
                    else if (!StackUtil.isEmpty(stack)) {
                        return false;
                    }
                }
                ++y;
            }
            return true;
        }
        
        public ItemStack getRecipeOutput() {
            return this.output;
        }
        
        public ItemStack getCraftingResult(final InventoryCrafting inv) {
            return this.getRecipeOutput();
        }
        
        public boolean canFit(final int width, final int height) {
            throw new UnsupportedOperationException();
        }
        
        public IRecipe setRegistryName(final ResourceLocation name) {
            throw new UnsupportedOperationException();
        }
        
        public ResourceLocation getRegistryName() {
            throw new UnsupportedOperationException();
        }
        
        public Class<IRecipe> getRegistryType() {
            throw new UnsupportedOperationException();
        }
    }
}
