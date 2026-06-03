package ic2.core;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GuiOverlayer extends Gui {
  private final Minecraft mc;
  
  public GuiOverlayer(Minecraft mc) {
    this.mc = mc;
  }
  
  @SubscribeEvent
  public void onRenderHotBar(RenderGameOverlayEvent.Post event) {
    if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR)
      return; 
    ItemStack helm = this.mc.field_71439_g.func_184582_a(EntityEquipmentSlot.HEAD);
    if (StackUtil.isEmpty(helm) || !(helm.func_77973_b() instanceof IItemHudProvider) || !((IItemHudProvider)helm.func_77973_b()).doesProvideHUD(helm))
      return; 
    HudMode hudMode = ((IItemHudProvider)helm.func_77973_b()).getHudMode(helm);
    if (!hudMode.shouldDisplay())
      return; 
    ItemStack boots = this.mc.field_71439_g.func_184582_a(EntityEquipmentSlot.FEET);
    ItemStack legs = this.mc.field_71439_g.func_184582_a(EntityEquipmentSlot.LEGS);
    ItemStack chestplate = this.mc.field_71439_g.func_184582_a(EntityEquipmentSlot.CHEST);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glDisable(2896);
    RenderItem renderItem = this.mc.func_175599_af();
    RenderHelper.func_74520_c();
    this.mc.func_110434_K().func_110577_a(background);
    func_73729_b(0, 0, 0, 0, 71, 69);
    renderItem.func_175042_a(helm, 5, 4);
    this.mc.field_71466_p.func_78276_b(mapCharge(helm) + "%", 25, 9, 16777215);
    if (StackUtil.getOrCreateNbtData(helm).func_74767_n("Nightvision"))
      renderItem.func_175042_a(ItemName.nightvision_goggles.getItemStack(), 50, 4); 
    if (!StackUtil.isEmpty(chestplate)) {
      int charge = getCharge(chestplate);
      if (charge >= 0) {
        this.mc.field_71466_p.func_78276_b(charge + "%", 25, 25, 16777215);
        renderItem.func_175042_a(chestplate, 5, 20);
        NBTTagCompound nbtDatachestplate = StackUtil.getOrCreateNbtData(chestplate);
        if (nbtDatachestplate.func_74767_n("jetpack")) {
          ItemStack jetpack;
          if (nbtDatachestplate.func_74767_n("hoverMode")) {
            jetpack = ItemName.jetpack_electric.getItemStack();
          } else {
            jetpack = ItemName.jetpack.getItemStack();
          } 
          renderItem.func_175042_a(jetpack, 50, 20);
        } 
      } 
    } 
    if (!StackUtil.isEmpty(legs)) {
      int charge = getCharge(legs);
      if (charge >= 0) {
        this.mc.field_71466_p.func_78276_b(charge + "%", 25, 41, 16777215);
        renderItem.func_175042_a(legs, 5, 36);
      } 
    } 
    if (!StackUtil.isEmpty(boots)) {
      int charge = getCharge(boots);
      if (charge >= 0) {
        this.mc.field_71466_p.func_78276_b(charge + "%", 25, 56, 16777215);
        renderItem.func_175042_a(boots, 5, 52);
      } 
    } 
    if (hudMode.hasTooltip()) {
      ItemStack rightItem = this.mc.field_71439_g.func_184614_ca();
      ItemStack leftItem = this.mc.field_71439_g.func_184592_cb();
      int nextLine = 83;
      if (!StackUtil.isEmpty(rightItem)) {
        renderItem.func_175042_a(rightItem, 5, 74);
        this.mc.field_71466_p.func_78276_b(rightItem.func_82833_r(), 30, 78, 16777215);
        List<String> info = new LinkedList<>();
        if (rightItem.func_77973_b() instanceof IItemHudInfo) {
          info.addAll(((IItemHudInfo)rightItem.func_77973_b()).getHudInfo(rightItem, (hudMode == HudMode.ADVANCED)));
          if (info.size() > 0)
            for (int l = 0; l < info.size(); l++)
              this.mc.field_71466_p.func_78276_b(info.get(l), 8, 83 + (l + 1) * 14, 16777215);  
          nextLine += (info.size() + 1) * 14;
        } else {
          info.addAll(rightItem.func_82840_a((EntityPlayer)this.mc.field_71439_g, () -> (hudMode == HudMode.ADVANCED)));
          if (info.size() > 1)
            for (int l = 1; l < info.size(); l++)
              this.mc.field_71466_p.func_78276_b(info.get(l), 8, 83 + l * 14, 16777215);  
          nextLine += info.size() * 14;
        } 
        nextLine += 8;
      } 
      if (!StackUtil.isEmpty(leftItem)) {
        renderItem.func_175042_a(leftItem, 5, nextLine - 9);
        this.mc.field_71466_p.func_78276_b(leftItem.func_82833_r(), 30, nextLine - 5, 16777215);
        List<String> info = new LinkedList<>();
        if (leftItem.func_77973_b() instanceof IItemHudInfo) {
          info.addAll(((IItemHudInfo)leftItem.func_77973_b()).getHudInfo(leftItem, (hudMode == HudMode.ADVANCED)));
          if (info.size() > 0)
            for (int l = 0; l < info.size(); l++)
              this.mc.field_71466_p.func_78276_b(info.get(l), 8, nextLine + (l + 1) * 14, 16777215);  
        } else {
          info.addAll(leftItem.func_82840_a((EntityPlayer)this.mc.field_71439_g, () -> (hudMode == HudMode.ADVANCED)));
          if (info.size() > 1)
            for (int l = 1; l < info.size(); l++)
              this.mc.field_71466_p.func_78276_b(info.get(l), 8, nextLine + l * 14, 16777215);  
        } 
      } 
    } 
    RenderHelper.func_74518_a();
  }
  
  private static final int getCharge(ItemStack stack) {
    Item item = stack.func_77973_b();
    assert item != null;
    if (item instanceof IItemHudProvider.IItemHudBarProvider)
      return ((IItemHudProvider.IItemHudBarProvider)item).getBarPercent(stack); 
    if (item instanceof ic2.api.item.IElectricItem)
      return mapCharge(stack); 
    if (item.func_77645_m())
      return (int)Util.map(1.0D - item.getDurabilityForDisplay(stack), 1.0D, 100.0D); 
    return -1;
  }
  
  private static final int mapCharge(ItemStack stack) {
    assert stack.func_77973_b() instanceof ic2.api.item.IElectricItem;
    double charge = ElectricItem.manager.getCharge(stack);
    double maxCharge = charge + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, 2147483647, true, true);
    return (int)Util.map(charge, maxCharge, 100.0D);
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIOverlay.png");
}
