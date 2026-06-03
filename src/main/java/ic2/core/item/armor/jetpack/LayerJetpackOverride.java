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
  
  public void func_177141_a(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    ModelBiped model = getArmorModelHook(entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, (ModelBiped)func_188360_a(EntityEquipmentSlot.CHEST));
    model.func_178686_a(this.renderer.func_177087_b());
    model.func_78086_a(entity, limbSwing, limbSwingAmount, partialTicks);
    func_188359_a(model, EntityEquipmentSlot.CHEST);
    this.renderer.func_110776_a(getArmorResource((Entity)entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, null));
    GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
    model.func_78088_a((Entity)entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
  }
}
