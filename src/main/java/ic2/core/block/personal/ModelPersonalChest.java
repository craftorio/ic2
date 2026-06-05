package ic2.core.block.personal;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPersonalChest extends ModelBase {
   private final ModelRenderer door;

   public ModelPersonalChest() {
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.door = new ModelRenderer(this, 30, 0);
      this.door.addBox(2.0F, 1.0F, 2.0F, 12, 14, 1, true);
      this.door.setTextureSize(64, 64);
   }

   public void render() {
      this.door.render(0.0625F);
   }
}
