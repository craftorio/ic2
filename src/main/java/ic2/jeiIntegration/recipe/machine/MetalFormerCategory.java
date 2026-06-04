package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.core.block.ITeBlock;
import ic2.core.block.wiring.CableType;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import mezz.jei.api.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public final class MetalFormerCategory extends DynamicCategory<IBasicMachineRecipeManager> {
  private final int mode;
  
  private static final ItemStack[] icon = new ItemStack[] { ItemName.cable
      .getItemStack((Enum)CableType.copper), ItemName.forge_hammer
      .getItemStack(), ItemName.cutter
      .getItemStack() };
  
  public MetalFormerCategory(IBasicMachineRecipeManager recipeManager, int mode, IGuiHelper guiHelper) {
    super((ITeBlock)TeBlock.metal_former, recipeManager, guiHelper);
    this.mode = mode;
  }
  
  public String getUid() {
    return super.getUid() + this.mode;
  }
  
  public void draw(Minecraft minecraft) {
    super.draw(minecraft);
    minecraft.getRenderItem().renderItemAndEffectIntoGUI(icon[this.mode], 70, 35);
  }
}
