package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySteamRepressurizer extends TileEntityInventory implements IHasGui {
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  @GuiSynced
  protected final FluidTank input = (FluidTank)this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(new Fluid[] { FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance() }));
  
  @GuiSynced
  protected final FluidTank output = (FluidTank)this.fluids.addTankExtract("output", 10000);
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.currentHeat = nbt.func_74762_e("heat");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74768_a("heat", this.currentHeat);
    return nbt;
  }
  
  public static boolean hasSteam() {
    return (STEAM != null);
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (!hasSteam())
      return; 
    if (this.input.getFluidAmount() >= 10) {
      if (this.currentHeat < this.input.getFluidAmount() / 10)
        getHeat(); 
      int amount = getOutput();
      while (this.currentHeat > 0 && this.input.getFluidAmount() >= 10 && canOutput(amount)) {
        this.currentHeat--;
        this.input.drainInternal(10, true);
        this.output.fillInternal(new FluidStack(STEAM, amount), true);
      } 
    } 
  }
  
  protected void getHeat() {
    int aim = this.input.getFluidAmount() / 10;
    if (aim > 0) {
      World world = getWorld();
      int targetHeat = aim;
      for (EnumFacing dir : EnumFacing.field_82609_l) {
        TileEntity target = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
        if (target instanceof IHeatSource) {
          IHeatSource hs = (IHeatSource)target;
          int request = hs.drawHeat(dir.func_176734_d(), targetHeat, true);
          if (request > 0) {
            targetHeat -= hs.drawHeat(dir.func_176734_d(), request, false);
            if (targetHeat <= 0)
              break; 
          } 
        } 
      } 
      this.currentHeat += aim - targetHeat;
    } 
  }
  
  protected int getOutput() {
    assert this.input.getFluid() != null;
    Fluid fluid = this.input.getFluid().getFluid();
    if (fluid == FluidName.steam.getInstance())
      return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSteam"); 
    if (fluid == FluidName.superheated_steam.getInstance())
      return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSuperSteam"); 
    throw new IllegalStateException("Unknown tank contents: " + fluid);
  }
  
  protected boolean canOutput(int amount) {
    return (this.output.fillInternal(new FluidStack(STEAM, amount), false) == amount);
  }
  
  public ContainerBase<TileEntitySteamRepressurizer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntitySteamRepressurizer>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public boolean getGuiState(String name) {
    if ("valid".equals(name))
      return hasSteam(); 
    return super.getGuiState(name);
  }
  
  public static final Fluid STEAM = FluidRegistry.getFluid("steam");
  
  protected int currentHeat;
  
  protected static final int CONSUMPTION = 10;
}
