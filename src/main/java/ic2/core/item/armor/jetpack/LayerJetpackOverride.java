package ic2.core.item.armor.jetpack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.FastColor;

@OnlyIn(Dist.CLIENT)
public class LayerJetpackOverride extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/models/armor/ic2_jet_pack_layer_1.png");

	public LayerJetpackOverride(RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> renderer)
	{
		super(renderer);
	}

	public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch)
	{
		ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (JetpackHandler.hasJetpackAttached(chestStack))
		{
			HumanoidModel<LivingEntity> model = this.getParentModel();
			boolean[] saved = saveVisibility(model);
			model.setAllVisible(false);
			model.body.visible = true;
			model.rightArm.visible = true;
			model.leftArm.visible = true;
			model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

			VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(TEXTURE), chestStack.hasFoil());
			model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F));
			restoreVisibility(model, saved);
		}
	}

	private boolean[] saveVisibility(HumanoidModel<LivingEntity> model)
	{
		return new boolean[] { model.head.visible, model.hat.visible, model.body.visible, model.rightArm.visible, model.leftArm.visible, model.rightLeg.visible, model.leftLeg.visible };
	}

	private void restoreVisibility(HumanoidModel<LivingEntity> model, boolean[] saved)
	{
		model.head.visible = saved[0];
		model.hat.visible = saved[1];
		model.body.visible = saved[2];
		model.rightArm.visible = saved[3];
		model.leftArm.visible = saved[4];
		model.rightLeg.visible = saved[5];
		model.leftLeg.visible = saved[6];
	}
}
