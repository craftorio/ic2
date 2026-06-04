package ic2.core.block;

import ic2.api.tile.IRotorProvider;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class KineticGeneratorRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
  protected void renderBlockRotor(IRotorProvider windGen, World world, BlockPos pos) {
    int diameter = windGen.getRotorDiameter();
    if (diameter == 0)
      return; 
    float angle = windGen.getAngle();
    ResourceLocation rotorRL = windGen.getRotorRenderTexture();
    ModelBase model = rotorModels.get(Integer.valueOf(diameter));
    if (model == null) {
      model = new KineticGeneratorRotor(diameter);
      rotorModels.put(Integer.valueOf(diameter), model);
    } 
    EnumFacing facing = windGen.getFacing();
    pos = pos.func_177972_a(facing);
    int light = world.func_175626_b(pos, 0);
    int blockLight = light % 65536;
    int skyLight = light / 65536;
    OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, blockLight, skyLight);
    GlStateManager.func_179094_E();
    GlStateManager.func_179109_b(0.5F, 0.5F, 0.5F);
    switch (facing) {
      case NORTH:
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        break;
      case EAST:
        GL11.glRotatef(-180.0F, 0.0F, 1.0F, 0.0F);
        break;
      case SOUTH:
        GL11.glRotatef(-270.0F, 0.0F, 1.0F, 0.0F);
        break;
      case UP:
        GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        break;
    } 
    GlStateManager.func_179114_b(angle, 1.0F, 0.0F, 0.0F);
    GlStateManager.func_179109_b(-0.2F, 0.0F, 0.0F);
    func_147499_a(rotorRL);
    model.func_78088_a(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
    GlStateManager.func_179121_F();
  }
  
  public void func_192841_a(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    GL11.glPushMatrix();
    GL11.glTranslatef((float)x, (float)y, (float)z);
    if (te instanceof IRotorProvider)
      renderBlockRotor((IRotorProvider)te, te.getWorld(), te.getPos()); 
    GL11.glPopMatrix();
  }
  
  private static final Map<Integer, ModelBase> rotorModels = new HashMap<>();
}
