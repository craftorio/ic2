package ic2.core.item.armor.jetpack;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerJetpackOverride extends LayerBipedArmor {
  private final RenderLivingBase<?> renderer;
  
  public LayerJetpackOverride(RenderLivingBase<?> renderer) {
    super(null);
    this.renderer = renderer;
  }
  
  public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    ModelBiped model = getArmorModelHook(entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, (ModelBiped)getModelFromSlot(EntityEquipmentSlot.CHEST));
    model.setModelAttributes(this.renderer.getMainModel());
    model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
    setModelSlotVisible(model, EntityEquipmentSlot.CHEST);
    this.renderer.bindTexture(getArmorResource((Entity)entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, null));
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    model.render((Entity)entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
  }
}
