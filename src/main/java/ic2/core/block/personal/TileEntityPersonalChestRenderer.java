// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

@SideOnly(Side.CLIENT)
public class TileEntityPersonalChestRenderer extends TileEntitySpecialRenderer<TileEntityPersonalChest>
{
    private static final ResourceLocation texture;
    private final ModelPersonalChest model;
    
    public TileEntityPersonalChestRenderer() {
        this.model = new ModelPersonalChest();
    }
    
    public void render(final TileEntityPersonalChest te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha) {
        this.bindTexture(TileEntityPersonalChestRenderer.texture);
        final float doorHingeX = 0.84375f;
        final float doorHingeZ = 0.15625f;
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5f, 0.5f, 0.5f);
        float angle = 0.0f;
        switch (te.getFacing()) {
            case SOUTH: {
                angle = 180.0f;
                break;
            }
            case WEST: {
                angle = 90.0f;
                break;
            }
            case EAST: {
                angle = -90.0f;
                break;
            }
            default: {
                angle = 0.0f;
                break;
            }
        }
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-0.5f, -0.5f, -0.5f);
        angle = te.getLidAngle(partialTicks);
        angle = 1.0f - angle * angle * angle;
        GlStateManager.translate(0.84375f, 0.0f, 0.15625f);
        GlStateManager.rotate(angle * 90.0f - 90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-0.84375f, 0.0f, -0.15625f);
        this.model.render();
        GlStateManager.popMatrix();
    }
    
    static {
        texture = new ResourceLocation("ic2", "textures/models/newsafe.png");
    }
}
