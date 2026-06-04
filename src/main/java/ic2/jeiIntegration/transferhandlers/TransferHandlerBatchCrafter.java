// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.transferhandlers;

import net.minecraft.inventory.Container;
import java.util.Map;
import mezz.jei.api.gui.IGuiItemStackGroup;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import mezz.jei.api.gui.IRecipeLayout;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

public class TransferHandlerBatchCrafter implements IRecipeTransferHandler<ContainerBatchCrafter>
{
    public Class<ContainerBatchCrafter> getContainerClass() {
        return ContainerBatchCrafter.class;
    }
    
    public IRecipeTransferError transferRecipe(final ContainerBatchCrafter container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        final IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        final Map<Integer, ? extends IGuiIngredient<ItemStack>> slotToStackMap = stacks.getGuiIngredients();
        for (int i = 0; i < 9; ++i) {
            final IGuiIngredient<ItemStack> currentIngredient = (IGuiIngredient<ItemStack>)slotToStackMap.get(i + 1);
            ItemStack set;
            if (currentIngredient != null) {
                set = (ItemStack)currentIngredient.getDisplayedIngredient();
            }
            else {
                set = StackUtil.emptyStack;
            }
            ((TileEntityBatchCrafter)container.base).craftingGrid[i] = set;
        }
        IC2.network.get(false).updateTileEntityField((TileEntity)container.base, "craftingGrid");
        IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)container.base, 0);
        return null;
    }
}
