package ic2.core.item;

import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIC2Boat extends RenderBoat {
  public RenderIC2Boat(RenderManager manager) {
    super(manager);
  }
  
  protected ResourceLocation func_110775_a(EntityBoat entity) {
    return new ResourceLocation("ic2", ((EntityIC2Boat)entity).getTexture());
  }
}
