package ic2.core;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IItemHudProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GuiOverlayer extends Gui
{
	private final Minecraft mc;
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIOverlay.png");

	public GuiOverlayer(Minecraft mc)
	{
		this.mc = mc;
	}

	@SubscribeEvent
	public void onRenderHotBar(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
		{
			ItemStack helm = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if (!StackUtil.isEmpty(helm) && helm.getItem() instanceof IItemHudProvider && ((IItemHudProvider) helm.getItem()).doesProvideHUD(helm))
			{
				HudMode hudMode = ((IItemHudProvider) helm.getItem()).getHudMode(helm);
				if (hudMode.shouldDisplay())
				{
					ItemStack boots = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
					ItemStack legs = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
					ItemStack chestplate = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glDisable(2896);
					RenderItem renderItem = this.mc.getRenderItem();
					RenderHelper.enableGUIStandardItemLighting();
					this.mc.getTextureManager().bindTexture(background);
					this.drawTexturedModalRect(0, 0, 0, 0, 71, 69);
					renderItem.renderItemIntoGUI(helm, 5, 4);
					this.mc.fontRenderer.drawString(mapCharge(helm) + "%", 25, 9, 16777215);
					if (StackUtil.getOrCreateNbtData(helm).getBoolean("Nightvision"))
					{
						renderItem.renderItemIntoGUI(ItemName.nightvision_goggles.getItemStack(), 50, 4);
					}

					if (!StackUtil.isEmpty(chestplate))
					{
						int charge = getCharge(chestplate);
						if (charge >= 0)
						{
							this.mc.fontRenderer.drawString(charge + "%", 25, 25, 16777215);
							renderItem.renderItemIntoGUI(chestplate, 5, 20);
							NBTTagCompound nbtDatachestplate = StackUtil.getOrCreateNbtData(chestplate);
							if (nbtDatachestplate.getBoolean("jetpack"))
							{
								ItemStack jetpack;
								if (nbtDatachestplate.getBoolean("hoverMode"))
								{
									jetpack = ItemName.jetpack_electric.getItemStack();
								} else
								{
									jetpack = ItemName.jetpack.getItemStack();
								}

								renderItem.renderItemIntoGUI(jetpack, 50, 20);
							}
						}
					}

					if (!StackUtil.isEmpty(legs))
					{
						int charge = getCharge(legs);
						if (charge >= 0)
						{
							this.mc.fontRenderer.drawString(charge + "%", 25, 41, 16777215);
							renderItem.renderItemIntoGUI(legs, 5, 36);
						}
					}

					if (!StackUtil.isEmpty(boots))
					{
						int charge = getCharge(boots);
						if (charge >= 0)
						{
							this.mc.fontRenderer.drawString(charge + "%", 25, 56, 16777215);
							renderItem.renderItemIntoGUI(boots, 5, 52);
						}
					}

					if (hudMode.hasTooltip())
					{
						ItemStack rightItem = this.mc.player.getHeldItemMainhand();
						ItemStack leftItem = this.mc.player.getHeldItemOffhand();
						int nextLine = 83;
						if (!StackUtil.isEmpty(rightItem))
						{
							renderItem.renderItemIntoGUI(rightItem, 5, 74);
							this.mc.fontRenderer.drawString(rightItem.getDisplayName(), 30, 78, 16777215);
							List<String> info = new LinkedList<>();
							if (rightItem.getItem() instanceof IItemHudInfo)
							{
								info.addAll(((IItemHudInfo) rightItem.getItem()).getHudInfo(rightItem, hudMode == HudMode.ADVANCED));
								if (info.size() > 0)
								{
									for (int l = 0; l < info.size(); l++)
									{
										this.mc.fontRenderer.drawString(info.get(l), 8, 83 + (l + 1) * 14, 16777215);
									}
								}

								nextLine += (info.size() + 1) * 14;
							} else
							{
								info.addAll(rightItem.getTooltip(this.mc.player, () -> hudMode == HudMode.ADVANCED));
								if (info.size() > 1)
								{
									for (int l = 1; l < info.size(); l++)
									{
										this.mc.fontRenderer.drawString(info.get(l), 8, 83 + l * 14, 16777215);
									}
								}

								nextLine += info.size() * 14;
							}

							nextLine += 8;
						}

						if (!StackUtil.isEmpty(leftItem))
						{
							renderItem.renderItemIntoGUI(leftItem, 5, nextLine - 9);
							this.mc.fontRenderer.drawString(leftItem.getDisplayName(), 30, nextLine - 5, 16777215);
							List<String> info = new LinkedList<>();
							if (leftItem.getItem() instanceof IItemHudInfo)
							{
								info.addAll(((IItemHudInfo) leftItem.getItem()).getHudInfo(leftItem, hudMode == HudMode.ADVANCED));
								if (info.size() > 0)
								{
									for (int l = 0; l < info.size(); l++)
									{
										this.mc.fontRenderer.drawString(info.get(l), 8, nextLine + (l + 1) * 14, 16777215);
									}
								}
							} else
							{
								info.addAll(leftItem.getTooltip(this.mc.player, () -> hudMode == HudMode.ADVANCED));
								if (info.size() > 1)
								{
									for (int l = 1; l < info.size(); l++)
									{
										this.mc.fontRenderer.drawString(info.get(l), 8, nextLine + l * 14, 16777215);
									}
								}
							}
						}
					}

					RenderHelper.disableStandardItemLighting();
				}
			}
		}
	}

	private static final int getCharge(ItemStack stack)
	{
		Item item = stack.getItem();
		assert item != null;
		if (item instanceof IItemHudProvider.IItemHudBarProvider)
		{
			return ((IItemHudProvider.IItemHudBarProvider) item).getBarPercent(stack);
		} else if (item instanceof IElectricItem)
		{
			return mapCharge(stack);
		} else
		{
			return item.isDamageable() ? (int) Util.map(1.0 - item.getDurabilityForDisplay(stack), 1.0, 100.0) : -1;
		}
	}

	private static final int mapCharge(ItemStack stack)
	{
		assert stack.getItem() instanceof IElectricItem;
		double charge = ElectricItem.manager.getCharge(stack);
		double maxCharge = charge + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
		return (int) Util.map(charge, maxCharge, 100.0);
	}
}
