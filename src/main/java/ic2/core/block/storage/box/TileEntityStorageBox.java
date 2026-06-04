package ic2.core.block.storage.box;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.SoundType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityStorageBox extends TileEntityInventory implements IHasGui {
  protected final InvSlot inventory;
  
  public TileEntityStorageBox(int inventorySize) {
    this.inventory = new InvSlot((IInventorySlotHolder)this, "inventory", InvSlot.Access.IO, inventorySize, InvSlot.InvSide.ANY);
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    return Collections.emptyList();
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (!(getWorld()).isRemote) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      this.inventory.readFromNbt(nbt);
    } 
  }
  
  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
    if (!this.inventory.isEmpty())
      this.inventory.writeToNbt(nbt); 
    return drop;
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> info, ITooltipFlag advanced) {
    info.add("Stores items even when broken");
    info.add("Inventory size: " + this.inventory.size());
  }
  
  protected SoundType getBlockSound(Entity entity) {
    return SoundType.METAL;
  }
  
  public ContainerBase<? extends TileEntityStorageBox> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<? extends TileEntityStorageBox>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
