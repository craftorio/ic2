// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import java.util.HashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.api.tile.IRotorProvider;
import net.minecraft.client.model.ModelBase;
import java.util.Map;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class KineticGeneratorRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T>
{
    private static final Map<Integer, ModelBase> rotorModels;
    
    protected void renderBlockRotor(final IRotorProvider windGen, final World world, BlockPos pos) {
        final int diameter = windGen.getRotorDiameter();
        if (diameter == 0) {
            return;
        }
        final float angle = windGen.getAngle();
        final ResourceLocation rotorRL = windGen.getRotorRenderTexture();
        ModelBase model = KineticGeneratorRenderer.rotorModels.get(diameter);
        if (model == null) {
            model = new KineticGeneratorRotor(diameter);
            KineticGeneratorRenderer.rotorModels.put(diameter, model);
        }
        final EnumFacing facing = windGen.getFacing();
        pos = pos.offset(facing);
        final int light = world.getCombinedLight(pos, 0);
        final int blockLight = light % 65536;
        final int skyLight = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)blockLight, (float)skyLight);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f, 0.5f);
        switch (facing) {
            case NORTH: {
                GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
                break;
            }
            case EAST: {
                GL11.glRotatef(-180.0f, 0.0f, 1.0f, 0.0f);
                break;
            }
            case SOUTH: {
                GL11.glRotatef(-270.0f, 0.0f, 1.0f, 0.0f);
                break;
            }
            case UP: {
                GL11.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
        }
        GlStateManager.rotate(angle, 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(-0.2f, 0.0f, 0.0f);
        this.bindTexture(rotorRL);
        model.render((Entity)null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
        GlStateManager.popMatrix();
    }
    
    public void render(final T te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        if (te instanceof IRotorProvider) {
            this.renderBlockRotor((IRotorProvider)te, te.getWorld(), te.getPos());
        }
        GL11.glPopMatrix();
    }
    
    static {
        rotorModels = new HashMap<Integer, ModelBase>();
    }
}
