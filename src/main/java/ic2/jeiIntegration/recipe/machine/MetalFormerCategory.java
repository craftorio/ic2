// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import ic2.core.block.wiring.CableType;
import ic2.core.ref.ItemName;
import net.minecraft.client.Minecraft;
import ic2.core.block.ITeBlock;
import ic2.core.ref.TeBlock;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IBasicMachineRecipeManager;

public final class MetalFormerCategory extends DynamicCategory<IBasicMachineRecipeManager>
{
    private final int mode;
    private static final ItemStack[] icon;
    
    public MetalFormerCategory(final IBasicMachineRecipeManager recipeManager, final int mode, final IGuiHelper guiHelper) {
        super(TeBlock.metal_former, recipeManager, guiHelper);
        this.mode = mode;
    }
    
    @Override
    public String getUid() {
        return super.getUid() + this.mode;
    }
    
    @Override
    public void draw(final Minecraft minecraft) {
        super.draw(minecraft);
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(MetalFormerCategory.icon[this.mode], 70, 35);
    }
    
    static {
        icon = new ItemStack[] { ItemName.cable.getItemStack(CableType.copper), ItemName.forge_hammer.getItemStack(), ItemName.cutter.getItemStack() };
    }
}
