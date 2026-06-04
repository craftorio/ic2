package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.gui.GuiSolarDestiller;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.util.BiomeUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySolarDestiller extends TileEntityInventory implements IHasGui, IUpgradableBlock {
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  public final FluidTank inputTank = (FluidTank)this.fluids.addTankInsert("inputTank", 10000, Fluids.fluidPredicate(new Fluid[] { FluidRegistry.WATER }));
  
  public final FluidTank outputTank = (FluidTank)this.fluids.addTankExtract("outputTank", 10000);
  
  public final InvSlotConsumableLiquidByList waterinputSlot = new InvSlotConsumableLiquidByList((IInventorySlotHolder)this, "waterInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, new Fluid[] { FluidRegistry.WATER });
  
  public final InvSlotConsumableLiquidByTank destiwaterinputSlot = new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "destilledWaterInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
  
  public final InvSlotOutput wateroutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "waterOutput", 1);
  
  public final InvSlotOutput destiwateroutputSlott = new InvSlotOutput((IInventorySlotHolder)this, "destilledWaterOutput", 1);
  
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 3);
  
  private int tickrate;
  
  private int updateTicker;
  
  private float skyLight;
  
  protected void onLoaded() {
    super.onLoaded();
    this.tickrate = getTickRate();
    this.updateTicker = IC2.random.nextInt(this.tickrate);
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.waterinputSlot.processIntoTank((IFluidTank)this.inputTank, this.wateroutputSlot);
    if (++this.updateTicker >= this.tickrate) {
      updateSunVisibility();
      if (canWork()) {
        this.inputTank.drainInternal(1, true);
        this.outputTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
      } 
      this.updateTicker = 0;
    } 
    this.destiwaterinputSlot.processFromTank((IFluidTank)this.outputTank, this.destiwateroutputSlott);
    this.upgradeSlot.tick();
  }
  
  public void updateSunVisibility() {
    this.skyLight = TileEntitySolarGenerator.getSkyLight(getWorld(), this.field_174879_c.func_177984_a());
  }
  
  public ContainerBase<TileEntitySolarDestiller> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntitySolarDestiller>)new ContainerSolarDestiller(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiSolarDestiller(new ContainerSolarDestiller(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public int getTickRate() {
    Biome biome = BiomeUtil.getBiome(getWorld(), this.field_174879_c);
    if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT) == true)
      return 36; 
    if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD) == true)
      return 144; 
    return 72;
  }
  
  public int gaugeLiquidScaled(int i, int tank) {
    switch (tank) {
      case 0:
        if (this.inputTank.getFluidAmount() <= 0)
          return 0; 
        return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
      case 1:
        if (this.outputTank.getFluidAmount() <= 0)
          return 0; 
        return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
    } 
    return 0;
  }
  
  public boolean canWork() {
    return (this.inputTank.getFluidAmount() > 0 && this.outputTank
      .getFluidAmount() < this.outputTank.getCapacity() && this.skyLight > 0.5D);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
  }
  
  public double getEnergy() {
    return 40.0D;
  }
  
  public boolean useEnergy(double amount) {
    return true;
  }
}
