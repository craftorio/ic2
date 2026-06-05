package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.block.machine.gui.GuiItemBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock {
   public final InvSlot rightcontentSlot;
   public final InvSlot leftcontentSlot;
   public final InvSlotUpgrade upgradeSlot;
   private boolean tick = true;

   public TileEntityItemBuffer() {
      this.rightcontentSlot = new InvSlot(this, "rightcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.SIDE);
      this.leftcontentSlot = new InvSlot(this, "leftcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.NOTSIDE);
      this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
      this.comparator.setUpdate(() -> calcRedstoneFromInvSlots(this.rightcontentSlot, this.leftcontentSlot));
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      ItemStack upgradeleft = this.upgradeSlot.get(0);
      ItemStack upgraderight = this.upgradeSlot.get(1);
      if (!StackUtil.isEmpty(upgradeleft) && !StackUtil.isEmpty(upgraderight)) {
         if (this.tick) {
            if (((IUpgradeItem)upgradeleft.getItem()).onTick(upgradeleft, this)) {
               super.markDirty();
            }
         } else if (((IUpgradeItem)upgraderight.getItem()).onTick(upgraderight, this)) {
            super.markDirty();
         }

         this.tick = !this.tick;
      } else {
         if (!StackUtil.isEmpty(upgradeleft)) {
            this.tick = true;
            if (((IUpgradeItem)upgradeleft.getItem()).onTick(upgradeleft, this)) {
               super.markDirty();
            }
         }

         if (!StackUtil.isEmpty(upgraderight)) {
            this.tick = false;
            if (((IUpgradeItem)upgraderight.getItem()).onTick(upgraderight, this)) {
               super.markDirty();
            }
         }
      }
   }

   @Override
   public ContainerBase<TileEntityItemBuffer> getGuiContainer(EntityPlayer player) {
      return new ContainerItemBuffer(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiItemBuffer(new ContainerItemBuffer(player, this));
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(UpgradableProperty.ItemProducing);
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public double getEnergy() {
      return 40.0;
   }

   @Override
   public boolean useEnergy(double amount) {
      return true;
   }
}
