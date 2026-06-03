package ic2.core.block.machine.tileentity;

import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import ic2.core.block.machine.gui.GuiWeightedItemDistributor;
import ic2.core.network.NetworkManager;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWeightedItemDistributor extends TileEntityInventory implements IHasGui, IWeightedDistributor {
  public final InvSlot buffer = new InvSlot((IInventorySlotHolder)this, "buffer", InvSlot.Access.I, 9);
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    if (!this.priority.isEmpty()) {
      int[] indexes = new int[this.priority.size()];
      for (int i = 0; i < indexes.length; i++)
        indexes[i] = ((EnumFacing)this.priority.get(i)).func_176745_a(); 
      nbt.func_74783_a("priority", indexes);
    } 
    return nbt;
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    int[] indexes = nbt.func_74759_k("priority");
    if (indexes.length > 0)
      for (int index : indexes)
        this.priority.add(EnumFacing.func_82600_a(index));  
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("priority");
    return ret;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateConnectivity();
  }
  
  protected void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    updateConnectivity();
  }
  
  protected void updateConnectivity() {
    if (!(func_145831_w()).field_72995_K && !this.priority.isEmpty() && this.priority.remove(getFacing()))
      updatePriority(true); 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (!this.priority.isEmpty() && !this.buffer.isEmpty()) {
      World world = func_145831_w();
      boolean hasChanged = false;
      for (EnumFacing facing : this.priority) {
        TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(facing));
        EnumFacing side = facing.func_176734_d();
        if (StackUtil.isInventoryTile(te, side)) {
          boolean empty = true;
          for (int index = 0; index < this.buffer.size(); index++) {
            if (!this.buffer.isEmpty(index)) {
              ItemStack stack = this.buffer.get(index);
              ItemStack transferStack = StackUtil.copy(stack);
              int amount = StackUtil.putInInventory(te, side, transferStack, true);
              if (amount > 0) {
                amount = StackUtil.putInInventory(te, side, transferStack, false);
                stack = StackUtil.decSize(stack, amount);
                this.buffer.put(index, stack);
                hasChanged = true;
                empty &= StackUtil.isEmpty(stack);
              } 
            } 
          } 
          if (hasChanged && empty)
            break; 
        } 
      } 
      if (hasChanged)
        func_70296_d(); 
    } 
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerWeightedItemDistributor(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiWeightedItemDistributor(new ContainerWeightedItemDistributor(player, this));
  }
  
  @SideOnly(Side.CLIENT)
  public List<EnumFacing> getPriority() {
    return this.priority;
  }
  
  public void updatePriority(boolean server) {
    ((NetworkManager)IC2.network.get(server)).updateTileEntityField((TileEntity)this, "priority");
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  @ClientModifiable
  protected List<EnumFacing> priority = new ArrayList<>(5);
}
