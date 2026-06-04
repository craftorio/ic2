package ic2.core.block.machine.tileentity;

import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.block.machine.gui.GuiWeightedFluidDistributor;
import ic2.core.network.NetworkManager;
import ic2.core.profile.NotClassic;
import ic2.core.util.LiquidUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWeightedFluidDistributor extends TileEntityFluidDistributor implements IWeightedDistributor {
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    if (!this.priority.isEmpty()) {
      int[] indexes = new int[this.priority.size()];
      for (int i = 0; i < indexes.length; i++)
        indexes[i] = ((EnumFacing)this.priority.get(i)).getIndex(); 
      nbt.setIntArray("priority", indexes);
    } 
    return nbt;
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    int[] indexes = nbt.getIntArray("priority");
    if (indexes.length > 0)
      for (int index : indexes)
        this.priority.add(EnumFacing.getFront(index));  
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("priority");
    return ret;
  }
  
  protected void updateConnectivity() {
    if (!(getWorld()).isRemote && !this.priority.isEmpty() && this.priority.remove(getFacing()))
      updatePriority(true); 
    this.fluids.changeConnectivity(this.fluidTank, Collections.singleton(getFacing()), Collections.emptySet());
  }
  
  protected void moveFluid() {
    if (!this.priority.isEmpty()) {
      int tankAmount = this.fluidTank.getFluidAmount();
      for (EnumFacing dir : this.priority) {
        assert dir != getFacing();
        TileEntity target = this.world.getTileEntity(this.pos.offset(dir));
        EnumFacing side = dir.getOpposite();
        if (LiquidUtil.isFluidTile(target, side)) {
          int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), false);
          if (amount > 0) {
            tankAmount -= amount;
            this.fluidTank.drainInternal(amount, true);
            if (tankAmount <= 0)
              break; 
          } 
        } 
      } 
    } 
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerWeightedFluidDistributor(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiWeightedFluidDistributor(new ContainerWeightedFluidDistributor(player, this));
  }
  
  @SideOnly(Side.CLIENT)
  public List<EnumFacing> getPriority() {
    return this.priority;
  }
  
  public void updatePriority(boolean server) {
    ((NetworkManager)IC2.network.get(server)).updateTileEntityField((TileEntity)this, "priority");
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    int position = event / 10;
    EnumFacing facing = EnumFacing.getFront(event % 10 & 0x6);
    assert position >= 0 && position <= this.priority.size() : "Position was " + position;
    assert facing != getFacing();
    if (position == this.priority.size()) {
      this.priority.add(facing);
    } else {
      this.priority.set(position, facing);
    } 
  }
  
  @ClientModifiable
  protected List<EnumFacing> priority = new ArrayList<>(5);
}
