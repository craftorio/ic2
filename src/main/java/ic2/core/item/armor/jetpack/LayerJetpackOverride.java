// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;

@SideOnly(Side.CLIENT)
public class LayerJetpackOverride extends LayerBipedArmor
{
    private final RenderLivingBase<?> renderer;
    
    public LayerJetpackOverride(final RenderLivingBase<?> renderer) {
        super((RenderLivingBase)null);
        this.renderer = renderer;
    }
    
    public void doRenderLayer(final EntityLivingBase entity, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        final ModelBiped model = this.getArmorModelHook(entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, (ModelBiped)this.getModelFromSlot(EntityEquipmentSlot.CHEST));
        model.setModelAttributes(this.renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        this.setModelSlotVisible(model, EntityEquipmentSlot.CHEST);
        this.renderer.bindTexture(this.getArmorResource((Entity)entity, JetpackHandler.jetpack, EntityEquipmentSlot.CHEST, (String)null));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        model.render((Entity)entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
