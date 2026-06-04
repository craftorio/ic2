// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import ic2.api.item.ElectricItem;
import net.minecraft.item.Item;
import ic2.core.util.Util;
import ic2.api.item.IElectricItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Collection;
import ic2.api.item.HudMode;
import ic2.api.item.IItemHudInfo;
import java.util.LinkedList;
import ic2.core.ref.ItemName;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import ic2.api.item.IItemHudProvider;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiOverlayer extends Gui
{
    private final Minecraft mc;
    private static final ResourceLocation background;
    
    public GuiOverlayer(final Minecraft mc) {
        this.mc = mc;
    }
    
    @SubscribeEvent
    public void onRenderHotBar(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        final ItemStack helm = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (StackUtil.isEmpty(helm) || !(helm.getItem() instanceof IItemHudProvider) || !((IItemHudProvider)helm.getItem()).doesProvideHUD(helm)) {
            return;
        }
        final HudMode hudMode = ((IItemHudProvider)helm.getItem()).getHudMode(helm);
        if (!hudMode.shouldDisplay()) {
            return;
        }
        final ItemStack boots = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        final ItemStack legs = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        final ItemStack chestplate = this.mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(2896);
        final RenderItem renderItem = this.mc.getRenderItem();
        RenderHelper.enableGUIStandardItemLighting();
        this.mc.getTextureManager().bindTexture(GuiOverlayer.background);
        this.drawTexturedModalRect(0, 0, 0, 0, 71, 69);
        renderItem.renderItemIntoGUI(helm, 5, 4);
        this.mc.fontRenderer.drawString(mapCharge(helm) + "%", 25, 9, 16777215);
        if (StackUtil.getOrCreateNbtData(helm).getBoolean("Nightvision")) {
            renderItem.renderItemIntoGUI(ItemName.nightvision_goggles.getItemStack(), 50, 4);
        }
        if (!StackUtil.isEmpty(chestplate)) {
            final int charge = getCharge(chestplate);
            if (charge >= 0) {
                this.mc.fontRenderer.drawString(charge + "%", 25, 25, 16777215);
                renderItem.renderItemIntoGUI(chestplate, 5, 20);
                final NBTTagCompound nbtDatachestplate = StackUtil.getOrCreateNbtData(chestplate);
                if (nbtDatachestplate.getBoolean("jetpack")) {
                    ItemStack jetpack;
                    if (nbtDatachestplate.getBoolean("hoverMode")) {
                        jetpack = ItemName.jetpack_electric.getItemStack();
                    }
                    else {
                        jetpack = ItemName.jetpack.getItemStack();
                    }
                    renderItem.renderItemIntoGUI(jetpack, 50, 20);
                }
            }
        }
        if (!StackUtil.isEmpty(legs)) {
            final int charge = getCharge(legs);
            if (charge >= 0) {
                this.mc.fontRenderer.drawString(charge + "%", 25, 41, 16777215);
                renderItem.renderItemIntoGUI(legs, 5, 36);
            }
        }
        if (!StackUtil.isEmpty(boots)) {
            final int charge = getCharge(boots);
            if (charge >= 0) {
                this.mc.fontRenderer.drawString(charge + "%", 25, 56, 16777215);
                renderItem.renderItemIntoGUI(boots, 5, 52);
            }
        }
        if (hudMode.hasTooltip()) {
            final ItemStack rightItem = this.mc.player.getHeldItemMainhand();
            final ItemStack leftItem = this.mc.player.getHeldItemOffhand();
            int nextLine = 83;
            if (!StackUtil.isEmpty(rightItem)) {
                renderItem.renderItemIntoGUI(rightItem, 5, 74);
                this.mc.fontRenderer.drawString(rightItem.getDisplayName(), 30, 78, 16777215);
                final List<String> info = new LinkedList<String>();
                if (rightItem.getItem() instanceof IItemHudInfo) {
                    info.addAll(((IItemHudInfo)rightItem.getItem()).getHudInfo(rightItem, hudMode == HudMode.ADVANCED));
                    if (info.size() > 0) {
                        for (int l = 0; l < info.size(); ++l) {
                            this.mc.fontRenderer.drawString((String)info.get(l), 8, 83 + (l + 1) * 14, 16777215);
                        }
                    }
                    nextLine += (info.size() + 1) * 14;
                }
                else {
                    info.addAll(rightItem.getTooltip((EntityPlayer)this.mc.player, () -> hudMode == HudMode.ADVANCED));
                    if (info.size() > 1) {
                        for (int l = 1; l < info.size(); ++l) {
                            this.mc.fontRenderer.drawString((String)info.get(l), 8, 83 + l * 14, 16777215);
                        }
                    }
                    nextLine += info.size() * 14;
                }
                nextLine += 8;
            }
            if (!StackUtil.isEmpty(leftItem)) {
                renderItem.renderItemIntoGUI(leftItem, 5, nextLine - 9);
                this.mc.fontRenderer.drawString(leftItem.getDisplayName(), 30, nextLine - 5, 16777215);
                final List<String> info = new LinkedList<String>();
                if (leftItem.getItem() instanceof IItemHudInfo) {
                    info.addAll(((IItemHudInfo)leftItem.getItem()).getHudInfo(leftItem, hudMode == HudMode.ADVANCED));
                    if (info.size() > 0) {
                        for (int l = 0; l < info.size(); ++l) {
                            this.mc.fontRenderer.drawString((String)info.get(l), 8, nextLine + (l + 1) * 14, 16777215);
                        }
                    }
                }
                else {
                    info.addAll(leftItem.getTooltip((EntityPlayer)this.mc.player, () -> hudMode == HudMode.ADVANCED));
                    if (info.size() > 1) {
                        for (int l = 1; l < info.size(); ++l) {
                            this.mc.fontRenderer.drawString((String)info.get(l), 8, nextLine + l * 14, 16777215);
                        }
                    }
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
    }
    
    private static final int getCharge(final ItemStack stack) {
        final Item item = stack.getItem();
        assert item != null;
        if (item instanceof IItemHudProvider.IItemHudBarProvider) {
            return ((IItemHudProvider.IItemHudBarProvider)item).getBarPercent(stack);
        }
        if (item instanceof IElectricItem) {
            return mapCharge(stack);
        }
        if (item.isDamageable()) {
            return (int)Util.map(1.0 - item.getDurabilityForDisplay(stack), 1.0, 100.0);
        }
        return -1;
    }
    
    private static final int mapCharge(final ItemStack stack) {
        assert stack.getItem() instanceof IElectricItem;
        final double charge = ElectricItem.manager.getCharge(stack);
        final double maxCharge = charge + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
        return (int)Util.map(charge, maxCharge, 100.0);
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIOverlay.png");
    }
}
