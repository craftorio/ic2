package ic2.core;

import com.mojang.blaze3d.systems.RenderSystem;
import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IItemHudProvider;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.LinkedList;
import java.util.List;

public class GuiOverlayer
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guioverlay.png");
	private final Minecraft mc;

	public GuiOverlayer(Minecraft mc)
	{
		this.mc = mc;
	}

	public void render(GuiGraphics guiGraphics)
	{
		assert this.mc.player != null;
		ItemStack helm = this.mc.player.getItemBySlot(EquipmentSlot.HEAD);
		if (!StackUtil.isEmpty(helm) && helm.getItem() instanceof IItemHudProvider && ((IItemHudProvider) helm.getItem()).doesProvideHUD(helm))
		{
			HudMode hudMode = ((IItemHudProvider) helm.getItem()).getHudMode(helm);
			if (hudMode.shouldDisplay())
			{
				ItemStack boots = this.mc.player.getItemBySlot(EquipmentSlot.FEET);
				ItemStack legs = this.mc.player.getItemBySlot(EquipmentSlot.LEGS);
				ItemStack chestplate = this.mc.player.getItemBySlot(EquipmentSlot.CHEST);

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				guiGraphics.blit(BACKGROUND, 0, 0, 0, 0, 71, 69, 256, 256);
				guiGraphics.renderItem(helm, 5, 4);
				guiGraphics.drawString(this.mc.font, mapCharge(helm) + "%", 25, 9, 0xFFFFFF);
				if (StackUtil.getOrCreateNbtData(helm).getBoolean("night_vision"))
				{
					guiGraphics.renderItem(Ic2Items.NIGHT_VISION_GOGGLES.getDefaultInstance(), 50, 4);
				}

				if (!StackUtil.isEmpty(chestplate))
				{
					int charge = getCharge(chestplate);
					if (charge >= 0)
					{
						guiGraphics.drawString(this.mc.font, charge + "%", 25, 25, 0xFFFFFF);
						guiGraphics.renderItem(chestplate, 5, 20);

						Item chestItem = chestplate.getItem();
						if (chestItem instanceof IJetpack jetpack && jetpack.isJetpackActive(chestplate))
						{
							boolean hoverMode = StackUtil.getOrCreateNbtData(chestplate).getBoolean("hover_mode");
							ItemStack jetpackIcon = hoverMode ? Ic2Items.JETPACK_ELECTRIC.getDefaultInstance() : Ic2Items.JETPACK.getDefaultInstance();
							guiGraphics.renderItem(jetpackIcon, 50, 20);
						}
					}
				}

				if (!StackUtil.isEmpty(legs))
				{
					int charge = getCharge(legs);
					if (charge >= 0)
					{
						guiGraphics.drawString(this.mc.font, charge + "%", 25, 41, 0xFFFFFF);
						guiGraphics.renderItem(legs, 5, 36);
					}
				}

				if (!StackUtil.isEmpty(boots))
				{
					int charge = getCharge(boots);
					if (charge >= 0)
					{
						guiGraphics.drawString(this.mc.font, charge + "%", 25, 56, 0xFFFFFF);
						guiGraphics.renderItem(boots, 5, 52);
					}
				}

				if (hudMode.hasTooltip())
				{
					ItemStack rightItem = this.mc.player.getMainHandItem();
					ItemStack leftItem = this.mc.player.getOffhandItem();
					int nextLine = 83;

					if (!StackUtil.isEmpty(rightItem))
					{
						guiGraphics.renderItem(rightItem, 5, 74);
						guiGraphics.drawString(this.mc.font, rightItem.getHoverName().getString(), 30, 78, 0xFFFFFF);

						List<String> info = new LinkedList<>();
						if (rightItem.getItem() instanceof IItemHudInfo)
						{
							info.addAll(((IItemHudInfo) rightItem.getItem()).getHudInfo(rightItem, hudMode == HudMode.ADVANCED));
							if (!info.isEmpty())
							{
								for (int l = 0; l < info.size(); l++)
								{
									guiGraphics.drawString(this.mc.font, info.get(l), 8, 83 + (l + 1) * 14, 0xFFFFFF);
								}
							}

							nextLine += (info.size() + 1) * 14;
						}
						else
						{
							List<Component> tooltip = rightItem.getTooltipLines(this.mc.player, hudMode == HudMode.ADVANCED ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
							if (tooltip.size() > 1)
							{
								for (int l = 1; l < tooltip.size(); l++)
								{
									guiGraphics.drawString(this.mc.font, tooltip.get(l).getString(), 8, 83 + l * 14, 0xFFFFFF);
								}
							}

							nextLine += tooltip.size() * 14;
						}

						nextLine += 8;
					}

					if (!StackUtil.isEmpty(leftItem))
					{
						guiGraphics.renderItem(leftItem, 5, nextLine - 9);
						guiGraphics.drawString(this.mc.font, leftItem.getHoverName().getString(), 30, nextLine - 5, 0xFFFFFF);

						List<String> info = new LinkedList<>();
						if (leftItem.getItem() instanceof IItemHudInfo)
						{
							info.addAll(((IItemHudInfo) leftItem.getItem()).getHudInfo(leftItem, hudMode == HudMode.ADVANCED));
							if (!info.isEmpty())
							{
								for (int l = 0; l < info.size(); l++)
								{
									guiGraphics.drawString(this.mc.font, info.get(l), 8, nextLine + (l + 1) * 14, 0xFFFFFF);
								}
							}
						}
						else
						{
							List<Component> tooltip = leftItem.getTooltipLines(this.mc.player, hudMode == HudMode.ADVANCED ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
							if (tooltip.size() > 1)
							{
								for (int l = 1; l < tooltip.size(); l++)
								{
									guiGraphics.drawString(this.mc.font, tooltip.get(l).getString(), 8, nextLine + l * 14, 0xFFFFFF);
								}
							}
						}
					}
				}
			}
		}
	}

	private static int getCharge(ItemStack stack)
	{
		Item item = stack.getItem();
		if (item instanceof IItemHudProvider.IItemHudBarProvider)
		{
			return ((IItemHudProvider.IItemHudBarProvider) item).getBarPercent(stack);
		}
		else if (item instanceof IElectricItem)
		{
			return mapCharge(stack);
		}
		else
		{
			return stack.isDamageableItem() ? (int) Util.map(1.0 - (double) stack.getDamageValue() / stack.getMaxDamage(), 1.0, 100.0) : -1;
		}
	}

	private static int mapCharge(ItemStack stack)
	{
		double charge = ElectricItem.manager.getCharge(stack);
		double maxCharge = charge + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
		return (int) Util.map(charge, maxCharge, 100.0);
	}
}
