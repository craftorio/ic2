package ic2.core.block.personal;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityPersonalChestRenderer extends TileEntitySpecialRenderer<TileEntityPersonalChest> {
   private static final ResourceLocation texture = new ResourceLocation("ic2", "textures/models/newsafe.png");
   private final ModelPersonalChest model = new ModelPersonalChest();

   public void render(TileEntityPersonalChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      this.bindTexture(texture);
      float doorHingeX = 0.84375F;
      float doorHingeZ = 0.15625F;
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.translate(x, y, z);
      GlStateManager.translate(0.5F, 0.5F, 0.5F);
      float angle;
      switch (te.getFacing()) {
         case SOUTH:
            angle = 180.0F;
            break;
         case WEST:
            angle = 90.0F;
            break;
         case EAST:
            angle = -90.0F;
            break;
         default:
            angle = 0.0F;
      }

      GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      angle = te.getLidAngle(partialTicks);
      angle = 1.0F - angle * angle * angle;
      GlStateManager.translate(0.84375F, 0.0F, 0.15625F);
      GlStateManager.rotate(angle * 90.0F - 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.84375F, 0.0F, -0.15625F);
      this.model.render();
      GlStateManager.popMatrix();
   }
}
