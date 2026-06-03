package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.block.machine.gui.GuiSortingMachine;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySortingMachine extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock {
  public static final int defaultTier = 2;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public final InvSlot buffer;
  
  private final ItemStack[][] filters;
  
  public EnumFacing defaultRoute;
  
  public TileEntitySortingMachine() {
    super(15000, 2, false);
    this.defaultRoute = EnumFacing.DOWN;
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 3);
    this.buffer = new InvSlot((IInventorySlotHolder)this, "Buffer", InvSlot.Access.IO, 11);
    this.filters = new ItemStack[6][7];
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    NBTTagList filtersTag = nbt.func_150295_c("filters", 10);
    for (int i = 0; i < filtersTag.func_74745_c(); i++) {
      NBTTagCompound filterTag = filtersTag.func_150305_b(i);
      int index = filterTag.func_74771_c("index") & 0xFF;
      ItemStack stack = new ItemStack(filterTag);
      this.filters[index / 7][index % 7] = stack;
    } 
    int defaultRouteIdx = nbt.func_74771_c("defaultroute");
    if (defaultRouteIdx >= 0 && defaultRouteIdx < EnumFacing.field_82609_l.length)
      this.defaultRoute = EnumFacing.field_82609_l[defaultRouteIdx]; 
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    NBTTagList filtersTag = new NBTTagList();
    for (int i = 0; i < 42; i++) {
      ItemStack stack = this.filters[i / 7][i % 7];
      if (stack != null) {
        NBTTagCompound contentTag = new NBTTagCompound();
        contentTag.func_74774_a("index", (byte)i);
        stack.func_77955_b(contentTag);
        filtersTag.func_74742_a((NBTBase)contentTag);
      } 
    } 
    nbt.func_74782_a("filters", (NBTBase)filtersTag);
    nbt.func_74774_a("defaultroute", (byte)this.defaultRoute.ordinal());
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (IC2.platform.isSimulating())
      setUpgradableBlock(); 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    int index;
    label57: for (index = 0; index < this.buffer.size(); index++) {
      if (this.energy.getEnergy() < 20.0D)
        return; 
      ItemStack stack = this.buffer.get(index);
      if (!StackUtil.isEmpty(stack))
        for (StackUtil.AdjacentInv inv : StackUtil.getAdjacentInventories((TileEntity)this)) {
          if (inv.dir != this.defaultRoute) {
            for (ItemStack filterStack : getFilterSlots(inv.dir)) {
              if (!StackUtil.isEmpty(filterStack)) {
                int filterSize = StackUtil.getSize(filterStack);
                if (StackUtil.getSize(stack) >= filterSize && StackUtil.checkItemEquality(filterStack, stack) && this.energy.canUseEnergy((filterSize * 20))) {
                  ItemStack transferStack = StackUtil.copyWithSize(stack, filterSize);
                  int amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, true);
                  if (amount == filterSize) {
                    amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, false);
                    stack = StackUtil.decSize(stack, amount);
                    this.buffer.put(index, stack);
                    this.energy.useEnergy((amount * 20));
                    if (StackUtil.isEmpty(stack))
                      continue label57; 
                  } 
                  break;
                } 
              } 
            } 
            continue;
          } 
          boolean inFilter = false;
          label53: for (ItemStack[] sideFilters : this.filters) {
            for (ItemStack filter : sideFilters) {
              if (StackUtil.checkItemEquality(filter, stack)) {
                inFilter = true;
                break label53;
              } 
            } 
          } 
          if (!inFilter) {
            int amount = StackUtil.putInInventory(inv.te, inv.dir, StackUtil.copyWithSize(stack, 1), false);
            if (amount > 0) {
              stack = StackUtil.decSize(stack, amount);
              this.buffer.put(index, stack);
              this.energy.useEnergy(20.0D);
              if (StackUtil.isEmpty(stack));
            } 
            break;
          } 
        }  
    } 
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event >= 0 && event <= 5)
      this.defaultRoute = EnumFacing.field_82609_l[event]; 
  }
  
  public ContainerBase<TileEntitySortingMachine> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntitySortingMachine>)new ContainerSortingMachine(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiSortingMachine(new ContainerSortingMachine(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Transformer);
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (IC2.platform.isSimulating())
      setUpgradableBlock(); 
  }
  
  public void setUpgradableBlock() {
    this.energy.setSinkTier(this.upgradeSlot.getTier(2));
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public ItemStack[] getFilterSlots(EnumFacing side) {
    return this.filters[side.ordinal()];
  }
}
