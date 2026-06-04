package ic2.core.block.reactor.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.reactor.IBaseReactorComponent;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.reactor.IReactorComponent;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotReactor;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.reactor.gui.GuiNuclearReactor;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.WorldSearchUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.Level;

public class TileEntityNuclearReactorElectric extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IGuiValueProvider {
  public int updateTicker = IC2.random.nextInt(getTickRate());
  
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  public final Fluids.InternalFluidTank inputTank = this.fluids.addTank("inputTank", 10000, InvSlot.Access.NONE, InvSlot.InvSide.ANY, Fluids.fluidPredicate((ILiquidAcceptManager)Recipes.liquidHeatupManager));
  
  public final Fluids.InternalFluidTank outputTank = this.fluids.addTank("outputTank", 10000, InvSlot.Access.NONE);
  
  public final InvSlotReactor reactorSlot = new InvSlotReactor(this, "reactor", 54);
  
  public final InvSlotConsumableLiquidByManager coolantinputSlot = new InvSlotConsumableLiquidByManager((IInventorySlotHolder)this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, (ILiquidAcceptManager)Recipes.liquidHeatupManager);
  
  public final InvSlotConsumableLiquidByTank hotcoolinputSlot = new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
  
  public final InvSlotOutput coolantoutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "coolantoutputSlot", 1);
  
  public final InvSlotOutput hotcoolantoutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "hotcoolantoutputSlot", 1);
  
  public final Redstone redstone = (Redstone)addComponent((TileEntityComponent)new Redstone((TileEntityBlock)this));
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(getWorld()).isRemote && !isFluidCooled()) {
      refreshChambers();
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
      this.addedToEnergyNet = true;
    } 
    createChamberRedstoneLinks();
    if (isFluidCooled()) {
      createCasingRedstoneLinks();
      openTanks();
    } 
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isRendering()) {
      IC2.audioManager.removeSources(this);
      this.audioSourceMain = null;
      this.audioSourceGeiger = null;
    } 
    if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this));
      this.addedToEnergyNet = false;
    } 
    super.onUnloaded();
  }
  
  public int gaugeHeatScaled(int i) {
    return i * this.heat / this.maxHeat / 100 * 85;
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.heat = nbt.func_74762_e("heat");
    this.output = nbt.func_74765_d("output");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt = super.writeToNBT(nbt);
    nbt.func_74768_a("heat", this.heat);
    nbt.func_74777_a("output", (short)(int)getReactorEnergyOutput());
    return nbt;
  }
  
  protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
    if (this.addedToEnergyNet)
      refreshChambers(); 
  }
  
  public void drawEnergy(double amount) {}
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
    return true;
  }
  
  public double getOfferedEnergy() {
    return (getReactorEnergyOutput() * 5.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear"));
  }
  
  public int getSourceTier() {
    return 5;
  }
  
  public double getReactorEUEnergyOutput() {
    return getOfferedEnergy();
  }
  
  public List<IEnergyTile> getSubTiles() {
    return Collections.unmodifiableList(new ArrayList<>(this.subTiles));
  }
  
  private void processfluidsSlots() {
    this.coolantinputSlot.processIntoTank((IFluidTank)this.inputTank, this.coolantoutputSlot);
    this.hotcoolinputSlot.processFromTank((IFluidTank)this.outputTank, this.hotcoolantoutputSlot);
  }
  
  public void refreshChambers() {
    World world = getWorld();
    List<IEnergyTile> newSubTiles = new ArrayList<>();
    newSubTiles.add(this);
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (te instanceof TileEntityReactorChamberElectric && !te.func_145837_r())
        newSubTiles.add(te); 
    } 
    if (!newSubTiles.equals(this.subTiles)) {
      if (this.addedToEnergyNet)
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this)); 
      this.subTiles.clear();
      this.subTiles.addAll(newSubTiles);
      if (this.addedToEnergyNet)
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this)); 
    } 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.updateTicker++ % getTickRate() != 0)
      return; 
    if (!getWorld().func_175697_a(this.field_174879_c, 8)) {
      this.output = 0.0F;
    } else {
      boolean toFluidCooled = isFluidReactor();
      if (this.fluidCooled != toFluidCooled) {
        if (toFluidCooled) {
          enableFluidMode();
        } else {
          disableFluidMode();
        } 
        this.fluidCooled = toFluidCooled;
      } 
      dropAllUnfittingStuff();
      this.output = 0.0F;
      this.maxHeat = 10000;
      this.hem = 1.0F;
      processChambers();
      if (this.fluidCooled) {
        processfluidsSlots();
        FluidStack inputFluid = this.inputTank.getFluid();
        assert inputFluid == null || Recipes.liquidHeatupManager.acceptsFluid(this.inputTank.getFluid().getFluid());
        int huOtput = (int)(huOutputModifier * this.EmitHeatbuffer);
        int outputroom = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
        this.EmitHeatbuffer = 0;
        if (outputroom > 0 && inputFluid != null) {
          ILiquidHeatExchangerManager.HeatExchangeProperty prop = Recipes.liquidHeatupManager.getHeatExchangeProperty(inputFluid.getFluid());
          int fluidOutput = huOtput / prop.huPerMB;
          FluidStack add = new FluidStack(prop.outputFluid, fluidOutput);
          if (this.outputTank.canFillFluidType(add)) {
            FluidStack draincoolant;
            if (fluidOutput < outputroom) {
              this.EmitHeatbuffer = (int)((huOtput % prop.huPerMB) / huOutputModifier);
              this.EmitHeat = (int)(huOtput / huOutputModifier);
              draincoolant = this.inputTank.drainInternal(fluidOutput, false);
            } else {
              this.EmitHeat = outputroom * prop.huPerMB;
              draincoolant = this.inputTank.drainInternal(outputroom, false);
            } 
            if (draincoolant != null) {
              this.EmitHeat = draincoolant.amount * prop.huPerMB;
              huOtput -= (this.inputTank.drainInternal(draincoolant.amount, true)).amount * prop.huPerMB;
              this.outputTank.fillInternal(new FluidStack(prop.outputFluid, draincoolant.amount), true);
            } else {
              this.EmitHeat = 0;
            } 
          } 
        } else {
          this.EmitHeat = 0;
        } 
        addHeat((int)(huOtput / huOutputModifier));
      } 
      if (calculateHeatEffects())
        return; 
      setActive((this.heat >= 1000 || this.output > 0.0F));
      func_70296_d();
    } 
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "output");
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    showHeatEffects(getWorld(), this.field_174879_c, this.heat);
  }
  
  public static void showHeatEffects(World world, BlockPos pos, int heat) {
    Random rnd = world.field_73012_v;
    if (rnd.nextInt(8) != 0)
      return; 
    int puffs = heat / 1000;
    if (puffs > 0) {
      puffs = rnd.nextInt(puffs);
      int n;
      for (n = 0; n < puffs; n++)
        world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, (pos.getX() + rnd.nextFloat()), (pos.getY() + 0.95F), (pos.getZ() + rnd.nextFloat()), 0.0D, 0.0D, 0.0D, new int[0]); 
      puffs -= rnd.nextInt(4) + 3;
      for (n = 0; n < puffs; n++)
        world.func_175688_a(EnumParticleTypes.FLAME, (pos.getX() + rnd.nextFloat()), (pos.getY() + 1), (pos.getZ() + rnd.nextFloat()), 0.0D, 0.0D, 0.0D, new int[0]); 
    } 
  }
  
  public void dropAllUnfittingStuff() {
    int i;
    for (i = 0; i < this.reactorSlot.size(); i++) {
      ItemStack stack = this.reactorSlot.get(i);
      if (stack != null && !isUsefulItem(stack, false)) {
        this.reactorSlot.put(i, null);
        eject(stack);
      } 
    } 
    for (i = this.reactorSlot.size(); i < this.reactorSlot.rawSize(); i++) {
      ItemStack stack = this.reactorSlot.get(i);
      this.reactorSlot.put(i, null);
      eject(stack);
    } 
  }
  
  public boolean isUsefulItem(ItemStack stack, boolean forInsertion) {
    Item item = stack.getItem();
    if (item == null)
      return false; 
    if (forInsertion && this.fluidCooled && 
      item.getClass() == ItemReactorHeatStorage.class && (
      (ItemReactorHeatStorage)item).getCustomDamage(stack) > 0)
      return false; 
    return (item instanceof IBaseReactorComponent && (!forInsertion || ((IBaseReactorComponent)item).canBePlacedIn(stack, this)));
  }
  
  public void eject(ItemStack drop) {
    if (!IC2.platform.isSimulating() || drop == null)
      return; 
    StackUtil.dropAsEntity(getWorld(), this.field_174879_c, drop);
  }
  
  public boolean calculateHeatEffects() {
    if (this.heat < 4000 || !IC2.platform.isSimulating() || ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit") <= 0.0F)
      return false; 
    float power = this.heat / this.maxHeat;
    if (power >= 1.0F) {
      explode();
      return true;
    } 
    World world = getWorld();
    if (power >= 0.85F && world.field_73012_v.nextFloat() <= 0.2F * this.hem) {
      BlockPos coord = getRandCoord(2);
      IBlockState state = world.getBlockState(coord);
      Block block = state.getBlock();
      if (block.isAir(state, (IBlockAccess)world, coord)) {
        world.func_175656_a(coord, Blocks.field_150480_ab.getDefaultState());
      } else if (state.func_185887_b(world, coord) >= 0.0F && world
        .func_175625_s(coord) == null) {
        Material mat = state.func_185904_a();
        if (mat == Material.field_151576_e || mat == Material.field_151573_f || mat == Material.field_151587_i || mat == Material.field_151578_c || mat == Material.field_151571_B) {
          world.func_175656_a(coord, Blocks.field_150356_k.getDefaultState());
        } else {
          world.func_175656_a(coord, Blocks.field_150480_ab.getDefaultState());
        } 
      } 
    } 
    if (power >= 0.7F) {
      List<EntityLivingBase> nearByEntities = world.func_72872_a(EntityLivingBase.class, new AxisAlignedBB((this.field_174879_c.getX() - 3), (this.field_174879_c.getY() - 3), (this.field_174879_c.getZ() - 3), (this.field_174879_c
            .getX() + 4), (this.field_174879_c.getY() + 4), (this.field_174879_c.getZ() + 4)));
      for (EntityLivingBase entity : nearByEntities)
        entity.func_70097_a((DamageSource)IC2DamageSource.radiation, (int)(world.field_73012_v.nextInt(4) * this.hem)); 
    } 
    if (power >= 0.5F && world.field_73012_v.nextFloat() <= this.hem) {
      BlockPos coord = getRandCoord(2);
      IBlockState state = world.getBlockState(coord);
      if (state.func_185904_a() == Material.field_151586_h)
        world.func_175698_g(coord); 
    } 
    if (power >= 0.4F && world.field_73012_v.nextFloat() <= this.hem) {
      BlockPos coord = getRandCoord(2);
      if (world.func_175625_s(coord) == null) {
        IBlockState state = world.getBlockState(coord);
        Material mat = state.func_185904_a();
        if (mat == Material.field_151575_d || mat == Material.field_151584_j || mat == Material.field_151580_n)
          world.func_175656_a(coord, Blocks.field_150480_ab.getDefaultState()); 
      } 
    } 
    return false;
  }
  
  public BlockPos getRandCoord(int radius) {
    BlockPos ret;
    if (radius <= 0)
      return null; 
    World world = getWorld();
    do {
      ret = this.field_174879_c.func_177982_a(world.field_73012_v.nextInt(2 * radius + 1) - radius, world.field_73012_v
          .nextInt(2 * radius + 1) - radius, world.field_73012_v
          .nextInt(2 * radius + 1) - radius);
    } while (ret.equals(this.field_174879_c));
    return ret;
  }
  
  public void processChambers() {
    int size = getReactorSize();
    for (int pass = 0; pass < 2; pass++) {
      for (int y = 0; y < 6; y++) {
        for (int x = 0; x < size; x++) {
          ItemStack stack = this.reactorSlot.get(x, y);
          if (stack != null && stack.getItem() instanceof IReactorComponent) {
            IReactorComponent comp = (IReactorComponent)stack.getItem();
            comp.processChamber(stack, this, x, y, (pass == 0));
          } 
        } 
      } 
    } 
  }
  
  public boolean produceEnergy() {
    return (this.redstone.hasRedstoneInput() && ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear") > 0.0F);
  }
  
  public int getReactorSize() {
    World world = getWorld();
    if (world == null)
      return 9; 
    int cols = 3;
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity target = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (target instanceof TileEntityReactorChamberElectric)
        cols++; 
    } 
    return cols;
  }
  
  private boolean isFullSize() {
    return (getReactorSize() == 9);
  }
  
  public int getTickRate() {
    return 20;
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (StackUtil.checkItemEquality(StackUtil.get(player, hand), BlockName.te.getItemStack((Enum)TeBlock.reactor_chamber)))
      return false; 
    return super.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  public ContainerBase<TileEntityNuclearReactorElectric> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityNuclearReactorElectric>)new ContainerNuclearReactor(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiNuclearReactor(new ContainerNuclearReactor(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void onNetworkUpdate(String field) {
    if (field.equals("output")) {
      if (this.output > 0.0F) {
        if (this.lastOutput <= 0.0F) {
          if (this.audioSourceMain == null)
            this.audioSourceMain = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/NuclearReactorLoop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
          if (this.audioSourceMain != null)
            this.audioSourceMain.play(); 
        } 
        if (this.output < 40.0F) {
          if (this.lastOutput <= 0.0F || this.lastOutput >= 40.0F) {
            if (this.audioSourceGeiger != null)
              this.audioSourceGeiger.remove(); 
            this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerLowEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
            if (this.audioSourceGeiger != null)
              this.audioSourceGeiger.play(); 
          } 
        } else if (this.output < 80.0F) {
          if (this.lastOutput < 40.0F || this.lastOutput >= 80.0F) {
            if (this.audioSourceGeiger != null)
              this.audioSourceGeiger.remove(); 
            this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerMedEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
            if (this.audioSourceGeiger != null)
              this.audioSourceGeiger.play(); 
          } 
        } else if (this.output >= 80.0F && 
          this.lastOutput < 80.0F) {
          if (this.audioSourceGeiger != null)
            this.audioSourceGeiger.remove(); 
          this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerHighEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
          if (this.audioSourceGeiger != null)
            this.audioSourceGeiger.play(); 
        } 
      } else if (this.lastOutput > 0.0F) {
        if (this.audioSourceMain != null)
          this.audioSourceMain.stop(); 
        if (this.audioSourceGeiger != null)
          this.audioSourceGeiger.stop(); 
      } 
      this.lastOutput = this.output;
    } 
    super.onNetworkUpdate(field);
  }
  
  private float lastOutput = 0.0F;
  
  public TileEntity getCoreTe() {
    return (TileEntity)this;
  }
  
  public BlockPos getPosition() {
    return this.field_174879_c;
  }
  
  public World getWorldObj() {
    return getWorld();
  }
  
  public int getHeat() {
    return this.heat;
  }
  
  public void setHeat(int heat) {
    this.heat = heat;
  }
  
  public int addHeat(int amount) {
    this.heat += amount;
    return this.heat;
  }
  
  public ItemStack getItemAt(int x, int y) {
    if (x < 0 || x >= getReactorSize() || y < 0 || y >= 6)
      return null; 
    return this.reactorSlot.get(x, y);
  }
  
  public void setItemAt(int x, int y, ItemStack item) {
    if (x < 0 || x >= getReactorSize() || y < 0 || y >= 6)
      return; 
    this.reactorSlot.put(x, y, item);
  }
  
  public void explode() {
    float boomPower = 10.0F;
    float boomMod = 1.0F;
    for (int i = 0; i < this.reactorSlot.size(); i++) {
      ItemStack stack = this.reactorSlot.get(i);
      if (stack != null && stack.getItem() instanceof IReactorComponent) {
        float f = ((IReactorComponent)stack.getItem()).influenceExplosion(stack, this);
        if (f > 0.0F && f < 1.0F) {
          boomMod *= f;
        } else {
          boomPower += f;
        } 
      } 
      this.reactorSlot.put(i, null);
    } 
    boomPower *= this.hem * boomMod;
    IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuclear Reactor at %s melted (raw explosion power %f)", new Object[] { Util.formatPosition((TileEntity)this), Float.valueOf(boomPower) });
    boomPower = Math.min(boomPower, ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit"));
    World world = getWorld();
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity target = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (target instanceof TileEntityReactorChamberElectric)
        world.func_175698_g(target.getPos()); 
    } 
    world.func_175698_g(this.field_174879_c);
    ExplosionIC2 explosion = new ExplosionIC2(world, null, this.field_174879_c, boomPower, 0.01F, ExplosionIC2.Type.Nuclear);
    explosion.doExplosion();
  }
  
  public void addEmitHeat(int heat) {
    this.EmitHeatbuffer += heat;
  }
  
  public int getMaxHeat() {
    return this.maxHeat;
  }
  
  public void setMaxHeat(int newMaxHeat) {
    this.maxHeat = newMaxHeat;
  }
  
  public float getHeatEffectModifier() {
    return this.hem;
  }
  
  public void setHeatEffectModifier(float newHEM) {
    this.hem = newHEM;
  }
  
  public float getReactorEnergyOutput() {
    return this.output;
  }
  
  public float addOutput(float energy) {
    return this.output += energy;
  }
  
  public boolean isFluidCooled() {
    return this.fluidCooled;
  }
  
  private void createChamberRedstoneLinks() {
    World world = getWorld();
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      BlockPos cPos = this.field_174879_c.func_177972_a(facing);
      TileEntity te = world.func_175625_s(cPos);
      if (te instanceof TileEntityReactorChamberElectric) {
        TileEntityReactorChamberElectric chamber = (TileEntityReactorChamberElectric)te;
        if (chamber.redstone.isLinked() && chamber.redstone.getLinkReceiver() != this.redstone) {
          chamber.destoryChamber(true);
        } else {
          chamber.redstone.linkTo(this.redstone);
        } 
      } 
    } 
  }
  
  private void createCasingRedstoneLinks() {
    WorldSearchUtil.findTileEntities(getWorld(), this.field_174879_c, 2, new WorldSearchUtil.ITileEntityResultHandler() {
          public boolean onMatch(TileEntity te) {
            if (te instanceof TileEntityReactorRedstonePort)
              ((TileEntityReactorRedstonePort)te).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone); 
            return false;
          }
        });
  }
  
  private void removeCasingRedstoneLinks() {
    for (Redstone rs : this.redstone.getLinkedOrigins()) {
      if (rs.getParent() instanceof TileEntityReactorRedstonePort)
        rs.unlinkOutbound(); 
    } 
  }
  
  private void enableFluidMode() {
    if (this.addedToEnergyNet) {
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this));
      this.addedToEnergyNet = false;
    } 
    createCasingRedstoneLinks();
    openTanks();
  }
  
  private void disableFluidMode() {
    if (!this.addedToEnergyNet) {
      refreshChambers();
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
      this.addedToEnergyNet = true;
    } 
    removeCasingRedstoneLinks();
    closeTanks();
  }
  
  private void openTanks() {
    this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.I, InvSlot.InvSide.ANY);
    this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.O, InvSlot.InvSide.ANY);
  }
  
  private void closeTanks() {
    this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
    this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
  }
  
  private boolean isFluidReactor() {
    if (!isFullSize())
      return false; 
    if (!hasFluidChamber())
      return false; 
    int range = 2;
    final MutableBoolean foundConflict = new MutableBoolean();
    WorldSearchUtil.findTileEntities(getWorld(), this.field_174879_c, 4, new WorldSearchUtil.ITileEntityResultHandler() {
          public boolean onMatch(TileEntity te) {
            if (!(te instanceof TileEntityNuclearReactorElectric))
              return false; 
            if (te == TileEntityNuclearReactorElectric.this)
              return false; 
            TileEntityNuclearReactorElectric reactor = (TileEntityNuclearReactorElectric)te;
            if (reactor.isFullSize() && reactor.hasFluidChamber()) {
              foundConflict.setTrue();
              return true;
            } 
            return false;
          }
        });
    return !foundConflict.getValue().booleanValue();
  }
  
  private boolean hasFluidChamber() {
    int range = 2;
    ChunkCache cache = new ChunkCache(getWorld(), this.field_174879_c.func_177982_a(-2, -2, -2), this.field_174879_c.func_177982_a(2, 2, 2), 0);
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    int i;
    for (i = 0; i < 2; i++) {
      int y = this.field_174879_c.getY() + 2 * (i * 2 - 1);
      for (int z = this.field_174879_c.getZ() - 2; z <= this.field_174879_c.getZ() + 2; z++) {
        for (int x = this.field_174879_c.getX() - 2; x <= this.field_174879_c.getX() + 2; x++) {
          cPos.func_181079_c(x, y, z);
          if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos))
            return false; 
        } 
      } 
    } 
    for (i = 0; i < 2; i++) {
      int z = this.field_174879_c.getZ() + 2 * (i * 2 - 1);
      for (int y = this.field_174879_c.getY() - 2 + 1; y <= this.field_174879_c.getY() + 2 - 1; y++) {
        for (int x = this.field_174879_c.getX() - 2; x <= this.field_174879_c.getX() + 2; x++) {
          cPos.func_181079_c(x, y, z);
          if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos))
            return false; 
        } 
      } 
    } 
    for (i = 0; i < 2; i++) {
      int x = this.field_174879_c.getX() + 2 * (i * 2 - 1);
      for (int y = this.field_174879_c.getY() - 2 + 1; y <= this.field_174879_c.getY() + 2 - 1; y++) {
        for (int z = this.field_174879_c.getZ() - 2 + 1; z <= this.field_174879_c.getZ() + 2 - 1; z++) {
          cPos.func_181079_c(x, y, z);
          if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos))
            return false; 
        } 
      } 
    } 
    return true;
  }
  
  private static boolean isFluidChamberBlock(IBlockAccess world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    if (state == BlockName.resource.getBlockState((IIdProvider)ResourceBlock.reactor_vessel))
      return true; 
    TileEntity te = world.func_175625_s(pos);
    if (te == null)
      return false; 
    return (te instanceof IReactorChamber && ((IReactorChamber)te).isWall());
  }
  
  public double getGuiValue(String name) {
    if ("heat".equals(name))
      return (this.maxHeat == 0) ? 0.0D : (this.heat / this.maxHeat); 
    throw new IllegalArgumentException("Invalid value: " + name);
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
  
  public FluidTank getinputtank() {
    return (FluidTank)this.inputTank;
  }
  
  public FluidTank getoutputtank() {
    return (FluidTank)this.outputTank;
  }
  
  public int func_70297_j_() {
    return 1;
  }
  
  private final List<IEnergyTile> subTiles = new ArrayList<>();
  
  public float output = 0.0F;
  
  public int heat = 0;
  
  public int maxHeat = 10000;
  
  public float hem = 1.0F;
  
  private int EmitHeatbuffer = 0;
  
  public int EmitHeat = 0;
  
  private boolean fluidCooled = false;
  
  public boolean addedToEnergyNet = false;
  
  private static final float huOutputModifier = 40.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/FluidReactor/outputModifier");
  
  public AudioSource audioSourceMain;
  
  public AudioSource audioSourceGeiger;
}
