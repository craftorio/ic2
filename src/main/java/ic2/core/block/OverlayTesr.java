// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class OverlayTesr extends TileEntitySpecialRenderer<TileEntityBlock>
{
    public void render(final TileEntityBlock te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha) {
        final IBlockState state = te.getBlockType().getDefaultState();
        GL11.glPushAttrib(64);
        GL11.glPushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        }
        else {
            GlStateManager.shadeModel(7424);
        }
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        final float zScale = 1.001f;
        GlStateManager.translate((float)(x + 0.5), (float)(y + 0.5), (float)(z + 0.5));
        GlStateManager.scale(zScale, zScale, zScale);
        GlStateManager.translate((float)(-(x + 0.5)), (float)(-(y + 0.5)), (float)(-(z + 0.5)));
        final BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder wr = tessellator.getBuffer();
        wr.begin(7, DefaultVertexFormats.BLOCK);
        wr.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());
        renderer.getBlockModelRenderer().renderModel((IBlockAccess)te.getWorld(), renderer.getModelForState(state), state, te.getPos(), wr, true);
        wr.setTranslation(0.0, 0.0, 0.0);
        tessellator.draw();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
