// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.entity.Entity;
import ic2.core.util.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;

@SideOnly(Side.CLIENT)
public class RenderExplosiveBlock extends Render<EntityIC2Explosive>
{
    public RenderExplosiveBlock(final RenderManager manager) {
        super(manager);
        this.shadowSize = 0.5f;
    }
    
    public void doRender(final EntityIC2Explosive entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y + 0.5f, (float)z);
        if (entity.fuse - partialTicks + 1.0f < 10.0f) {
            float scale = 1.0f - (entity.fuse - partialTicks + 1.0f) / 10.0f;
            scale = Util.limit(scale, 0.0f, 1.0f);
            scale = Util.square(Util.square(scale));
            scale = 1.0f + scale * 0.3f;
            GlStateManager.scale(scale, scale, scale);
        }
        final float alpha = (1.0f - (entity.fuse - partialTicks + 1.0f) / 100.0f) * 0.8f;
        this.bindEntityTexture((Entity)entity);
        GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-0.5f, -0.5f, 0.5f);
        blockRenderer.renderBlockBrightness(entity.renderBlockState, entity.getBrightness());
        GlStateManager.translate(0.0f, 0.0f, 1.0f);
        if (entity.fuse / 5 % 2 == 0) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 772);
            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
            GlStateManager.doPolygonOffset(-3.0f, -3.0f);
            GlStateManager.enablePolygonOffset();
            blockRenderer.renderBlockBrightness(entity.renderBlockState, 1.0f);
            GlStateManager.doPolygonOffset(0.0f, 0.0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
        GlStateManager.popMatrix();
        super.doRender((Entity)entity, x, y, z, entityYaw, partialTicks);
    }
    
    protected ResourceLocation getEntityTexture(final EntityIC2Explosive entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
