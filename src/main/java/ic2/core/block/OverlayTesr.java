package ic2.core.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

public class OverlayTesr extends TileEntitySpecialRenderer<TileEntityBlock> {
  public void func_192841_a(TileEntityBlock te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    IBlockState state = te.func_145838_q().func_176223_P();
    GL11.glPushAttrib(64);
    GL11.glPushMatrix();
    RenderHelper.func_74518_a();
    GlStateManager.func_179112_b(770, 771);
    GlStateManager.func_179147_l();
    GlStateManager.func_179129_p();
    if (Minecraft.func_71379_u()) {
      GlStateManager.func_179103_j(7425);
    } else {
      GlStateManager.func_179103_j(7424);
    } 
    func_147499_a(TextureMap.field_110575_b);
    float zScale = 1.001F;
    GlStateManager.func_179109_b((float)(x + 0.5D), (float)(y + 0.5D), (float)(z + 0.5D));
    GlStateManager.func_179152_a(zScale, zScale, zScale);
    GlStateManager.func_179109_b((float)-(x + 0.5D), (float)-(y + 0.5D), (float)-(z + 0.5D));
    BlockRendererDispatcher renderer = Minecraft.func_71410_x().func_175602_ab();
    Tessellator tessellator = Tessellator.func_178181_a();
    BufferBuilder wr = tessellator.func_178180_c();
    wr.func_181668_a(7, DefaultVertexFormats.field_176600_a);
    wr.func_178969_c(x - te.getPos().func_177958_n(), y - te.getPos().func_177956_o(), z - te.getPos().func_177952_p());
    renderer.func_175019_b().func_178267_a((IBlockAccess)te.getWorld(), renderer
        .func_184389_a(state), state, te
        .getPos(), wr, true);
    wr.func_178969_c(0.0D, 0.0D, 0.0D);
    tessellator.func_78381_a();
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }
}
