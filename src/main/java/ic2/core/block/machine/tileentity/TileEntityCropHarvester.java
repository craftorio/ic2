package ic2.core.block.machine.tileentity;

import ic2.api.crops.ICropTile;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.block.machine.gui.GuiCropHarvester;
import ic2.core.crop.TileEntityCrop;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityCropHarvester extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock {
  public final InvSlot contentSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public int scanX;
  
  public int scanY;
  
  public int scanZ;
  
  public TileEntityCropHarvester() {
    super(10000, 1, false);
    this.scanX = -4;
    this.scanY = -1;
    this.scanZ = -4;
    this.contentSlot = new InvSlot((IInventorySlotHolder)this, "content", InvSlot.Access.IO, 15);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.upgradeSlot.tick();
    if (this.field_145850_b.func_82737_E() % 10L == 0L && this.energy.getEnergy() >= 21.0D)
      scan(); 
  }
  
  public void scan() {
    this.scanX++;
    if (this.scanX > 4) {
      this.scanX = -4;
      this.scanZ++;
      if (this.scanZ > 4) {
        this.scanZ = -4;
        this.scanY++;
        if (this.scanY > 1)
          this.scanY = -1; 
      } 
    } 
    this.energy.useEnergy(1.0D);
    World world = func_145831_w();
    TileEntity tileEntity = world.func_175625_s(this.field_174879_c.func_177982_a(this.scanX, this.scanY, this.scanZ));
    if (tileEntity instanceof TileEntityCrop && !isInvFull()) {
      TileEntityCrop crop = (TileEntityCrop)tileEntity;
      if (crop.getCrop() != null) {
        List<ItemStack> drops = null;
        if (crop.getCurrentSize() == crop.getCrop().getOptimalHarvestSize((ICropTile)crop)) {
          drops = crop.performHarvest();
        } else if (crop.getCurrentSize() == crop.getCrop().getMaxSize()) {
          drops = crop.performHarvest();
        } 
        if (drops != null)
          drops.forEach(drop -> {
                if (StackUtil.putInInventory((TileEntity)this, EnumFacing.WEST, drop, true) == 0) {
                  StackUtil.dropAsEntity(world, this.field_174879_c, drop);
                } else {
                  StackUtil.putInInventory((TileEntity)this, EnumFacing.WEST, drop, false);
                } 
                this.energy.useEnergy(20.0D);
              }); 
      } 
    } 
  }
  
  private boolean isInvFull() {
    for (int i = 0; i < this.contentSlot.size(); i++) {
      ItemStack stack = this.contentSlot.get(i);
      if (StackUtil.isEmpty(stack) || StackUtil.getSize(stack) < Math.min(stack.func_77976_d(), this.contentSlot.getStackSizeLimit()))
        return false; 
    } 
    return true;
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemProducing);
  }
  
  public ContainerBase<TileEntityCropHarvester> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCropHarvester>)new ContainerCropHarvester(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiCropHarvester(new ContainerCropHarvester(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}
