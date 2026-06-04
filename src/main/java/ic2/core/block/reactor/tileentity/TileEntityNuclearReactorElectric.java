// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import net.minecraftforge.fluids.FluidTank;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.type.ResourceBlock;
import net.minecraft.world.ChunkCache;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ic2.core.util.WorldSearchUtil;
import net.minecraft.entity.Entity;
import ic2.core.ExplosionIC2;
import ic2.core.util.Util;
import org.apache.logging.log4j.Level;
import ic2.core.util.LogCategory;
import ic2.core.audio.PositionSpec;
import ic2.core.block.reactor.gui.GuiNuclearReactor;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.ContainerBase;
import ic2.core.ref.TeBlock;
import ic2.core.ref.BlockName;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.reactor.IReactorComponent;
import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.DamageSource;
import ic2.core.IC2DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.StackUtil;
import net.minecraft.item.Item;
import ic2.api.reactor.IBaseReactorComponent;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import net.minecraft.item.ItemStack;
import java.util.Random;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import java.util.Collections;
import java.util.Collection;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IEnergyAcceptor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import java.util.ArrayList;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotReactor;
import ic2.api.energy.tile.IEnergyTile;
import java.util.List;
import ic2.core.block.comp.Fluids;
import ic2.core.audio.AudioSource;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.reactor.IReactor;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityNuclearReactorElectric extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IGuiValueProvider
{
    public AudioSource audioSourceMain;
    public AudioSource audioSourceGeiger;
    private float lastOutput;
    public final Fluids.InternalFluidTank inputTank;
    public final Fluids.InternalFluidTank outputTank;
    private final List<IEnergyTile> subTiles;
    public final InvSlotReactor reactorSlot;
    public final InvSlotOutput coolantoutputSlot;
    public final InvSlotOutput hotcoolantoutputSlot;
    public final InvSlotConsumableLiquidByManager coolantinputSlot;
    public final InvSlotConsumableLiquidByTank hotcoolinputSlot;
    public final Redstone redstone;
    protected final Fluids fluids;
    public float output;
    public int updateTicker;
    public int heat;
    public int maxHeat;
    public float hem;
    private int EmitHeatbuffer;
    public int EmitHeat;
    private boolean fluidCooled;
    public boolean addedToEnergyNet;
    private static final float huOutputModifier;
    
    public TileEntityNuclearReactorElectric() {
        this.lastOutput = 0.0f;
        this.subTiles = new ArrayList<IEnergyTile>();
        this.output = 0.0f;
        this.heat = 0;
        this.maxHeat = 10000;
        this.hem = 1.0f;
        this.EmitHeatbuffer = 0;
        this.EmitHeat = 0;
        this.fluidCooled = false;
        this.addedToEnergyNet = false;
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
        this.fluids = this.addComponent(new Fluids(this));
        this.inputTank = this.fluids.addTank("inputTank", 10000, InvSlot.Access.NONE, InvSlot.InvSide.ANY, Fluids.fluidPredicate(Recipes.liquidHeatupManager));
        this.outputTank = this.fluids.addTank("outputTank", 10000, InvSlot.Access.NONE);
        this.reactorSlot = new InvSlotReactor(this, "reactor", 54);
        this.coolantinputSlot = new InvSlotConsumableLiquidByManager(this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidHeatupManager);
        this.hotcoolinputSlot = new InvSlotConsumableLiquidByTank(this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
        this.coolantoutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1);
        this.hotcoolantoutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
        this.redstone = this.addComponent(new Redstone(this));
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote && !this.isFluidCooled()) {
            this.refreshChambers();
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
        }
        this.createChamberRedstoneLinks();
        if (this.isFluidCooled()) {
            this.createCasingRedstoneLinks();
            this.openTanks();
        }
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isRendering()) {
            IC2.audioManager.removeSources(this);
            this.audioSourceMain = null;
            this.audioSourceGeiger = null;
        }
        if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }
        super.onUnloaded();
    }
    
    public int gaugeHeatScaled(final int i) {
        return i * this.heat / (this.maxHeat / 100 * 85);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heat = nbt.getInteger("heat");
        this.output = nbt.getShort("output");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setInteger("heat", this.heat);
        nbt.setShort("output", (short)this.getReactorEnergyOutput());
        return nbt;
    }
    
    @Override
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        if (this.addedToEnergyNet) {
            this.refreshChambers();
        }
    }
    
    @Override
    public void drawEnergy(final double amount) {
    }
    
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing direction) {
        return true;
    }
    
    @Override
    public double getOfferedEnergy() {
        return this.getReactorEnergyOutput() * 5.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear");
    }
    
    @Override
    public int getSourceTier() {
        return 5;
    }
    
    @Override
    public double getReactorEUEnergyOutput() {
        return this.getOfferedEnergy();
    }
    
    @Override
    public List<IEnergyTile> getSubTiles() {
        return Collections.unmodifiableList((List<? extends IEnergyTile>)new ArrayList<IEnergyTile>(this.subTiles));
    }
    
    private void processfluidsSlots() {
        this.coolantinputSlot.processIntoTank((IFluidTank)this.inputTank, this.coolantoutputSlot);
        this.hotcoolinputSlot.processFromTank((IFluidTank)this.outputTank, this.hotcoolantoutputSlot);
    }
    
    public void refreshChambers() {
        final World world = this.getWorld();
        final List<IEnergyTile> newSubTiles = new ArrayList<IEnergyTile>();
        newSubTiles.add(this);
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityReactorChamberElectric && !te.isInvalid()) {
                newSubTiles.add((TileEntityReactorChamberElectric)te);
            }
        }
        if (!newSubTiles.equals(this.subTiles)) {
            if (this.addedToEnergyNet) {
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            }
            this.subTiles.clear();
            this.subTiles.addAll(newSubTiles);
            if (this.addedToEnergyNet) {
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            }
        }
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        if (!this.getWorld().isAreaLoaded(this.pos, 8)) {
            this.output = 0.0f;
        }
        else {
            final boolean toFluidCooled = this.isFluidReactor();
            if (this.fluidCooled != toFluidCooled) {
                if (toFluidCooled) {
                    this.enableFluidMode();
                }
                else {
                    this.disableFluidMode();
                }
                this.fluidCooled = toFluidCooled;
            }
            this.dropAllUnfittingStuff();
            this.output = 0.0f;
            this.maxHeat = 10000;
            this.hem = 1.0f;
            this.processChambers();
            if (this.fluidCooled) {
                this.processfluidsSlots();
                final FluidStack inputFluid = this.inputTank.getFluid();
                assert !(!Recipes.liquidHeatupManager.acceptsFluid(this.inputTank.getFluid().getFluid()));
                int huOtput = (int)(TileEntityNuclearReactorElectric.huOutputModifier * this.EmitHeatbuffer);
                final int outputroom = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
                this.EmitHeatbuffer = 0;
                if (outputroom > 0 && inputFluid != null) {
                    final ILiquidHeatExchangerManager.HeatExchangeProperty prop = Recipes.liquidHeatupManager.getHeatExchangeProperty(inputFluid.getFluid());
                    final int fluidOutput = huOtput / prop.huPerMB;
                    final FluidStack add = new FluidStack(prop.outputFluid, fluidOutput);
                    if (this.outputTank.canFillFluidType(add)) {
                        FluidStack draincoolant;
                        if (fluidOutput < outputroom) {
                            this.EmitHeatbuffer = (int)(huOtput % prop.huPerMB / TileEntityNuclearReactorElectric.huOutputModifier);
                            this.EmitHeat = (int)(huOtput / TileEntityNuclearReactorElectric.huOutputModifier);
                            draincoolant = this.inputTank.drainInternal(fluidOutput, false);
                        }
                        else {
                            this.EmitHeat = outputroom * prop.huPerMB;
                            draincoolant = this.inputTank.drainInternal(outputroom, false);
                        }
                        if (draincoolant != null) {
                            this.EmitHeat = draincoolant.amount * prop.huPerMB;
                            huOtput -= this.inputTank.drainInternal(draincoolant.amount, true).amount * prop.huPerMB;
                            this.outputTank.fillInternal(new FluidStack(prop.outputFluid, draincoolant.amount), true);
                        }
                        else {
                            this.EmitHeat = 0;
                        }
                    }
                }
                else {
                    this.EmitHeat = 0;
                }
                this.addHeat((int)(huOtput / TileEntityNuclearReactorElectric.huOutputModifier));
            }
            if (this.calculateHeatEffects()) {
                return;
            }
            this.setActive(this.heat >= 1000 || this.output > 0.0f);
            this.markDirty();
        }
        IC2.network.get(true).updateTileEntityField(this, "output");
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        showHeatEffects(this.getWorld(), this.pos, this.heat);
    }
    
    public static void showHeatEffects(final World world, final BlockPos pos, final int heat) {
        final Random rnd = world.rand;
        if (rnd.nextInt(8) != 0) {
            return;
        }
        int puffs = heat / 1000;
        if (puffs > 0) {
            puffs = rnd.nextInt(puffs);
            for (int n = 0; n < puffs; ++n) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)(pos.getX() + rnd.nextFloat()), (double)(pos.getY() + 0.95f), (double)(pos.getZ() + rnd.nextFloat()), 0.0, 0.0, 0.0, new int[0]);
            }
            puffs -= rnd.nextInt(4) + 3;
            for (int n = 0; n < puffs; ++n) {
                world.spawnParticle(EnumParticleTypes.FLAME, (double)(pos.getX() + rnd.nextFloat()), (double)(pos.getY() + 1), (double)(pos.getZ() + rnd.nextFloat()), 0.0, 0.0, 0.0, new int[0]);
            }
        }
    }
    
    public void dropAllUnfittingStuff() {
        for (int i = 0; i < this.reactorSlot.size(); ++i) {
            final ItemStack stack = this.reactorSlot.get(i);
            if (stack != null && !this.isUsefulItem(stack, false)) {
                this.reactorSlot.put(i, null);
                this.eject(stack);
            }
        }
        for (int i = this.reactorSlot.size(); i < this.reactorSlot.rawSize(); ++i) {
            final ItemStack stack = this.reactorSlot.get(i);
            this.reactorSlot.put(i, null);
            this.eject(stack);
        }
    }
    
    public boolean isUsefulItem(final ItemStack stack, final boolean forInsertion) {
        final Item item = stack.getItem();
        return item != null && (!forInsertion || !this.fluidCooled || item.getClass() != ItemReactorHeatStorage.class || ((ItemReactorHeatStorage)item).getCustomDamage(stack) <= 0) && item instanceof IBaseReactorComponent && (!forInsertion || ((IBaseReactorComponent)item).canBePlacedIn(stack, this));
    }
    
    public void eject(final ItemStack drop) {
        if (!IC2.platform.isSimulating() || drop == null) {
            return;
        }
        StackUtil.dropAsEntity(this.getWorld(), this.pos, drop);
    }
    
    public boolean calculateHeatEffects() {
        if (this.heat < 4000 || !IC2.platform.isSimulating() || ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit") <= 0.0f) {
            return false;
        }
        final float power = this.heat / (float)this.maxHeat;
        if (power >= 1.0f) {
            this.explode();
            return true;
        }
        final World world = this.getWorld();
        if (power >= 0.85f && world.rand.nextFloat() <= 0.2f * this.hem) {
            final BlockPos coord = this.getRandCoord(2);
            final IBlockState state = world.getBlockState(coord);
            final Block block = state.getBlock();
            if (block.isAir(state, (IBlockAccess)world, coord)) {
                world.setBlockState(coord, Blocks.FIRE.getDefaultState());
            }
            else if (state.getBlockHardness(world, coord) >= 0.0f && world.getTileEntity(coord) == null) {
                final Material mat = state.getMaterial();
                if (mat == Material.ROCK || mat == Material.IRON || mat == Material.LAVA || mat == Material.GROUND || mat == Material.CLAY) {
                    world.setBlockState(coord, Blocks.FLOWING_LAVA.getDefaultState());
                }
                else {
                    world.setBlockState(coord, Blocks.FIRE.getDefaultState());
                }
            }
        }
        if (power >= 0.7f) {
            final List<EntityLivingBase> nearByEntities = world.getEntitiesWithinAABB((Class)EntityLivingBase.class, new AxisAlignedBB((double)(this.pos.getX() - 3), (double)(this.pos.getY() - 3), (double)(this.pos.getZ() - 3), (double)(this.pos.getX() + 4), (double)(this.pos.getY() + 4), (double)(this.pos.getZ() + 4)));
            for (final EntityLivingBase entity : nearByEntities) {
                entity.attackEntityFrom((DamageSource)IC2DamageSource.radiation, (float)(int)(world.rand.nextInt(4) * this.hem));
            }
        }
        if (power >= 0.5f && world.rand.nextFloat() <= this.hem) {
            final BlockPos coord = this.getRandCoord(2);
            final IBlockState state = world.getBlockState(coord);
            if (state.getMaterial() == Material.WATER) {
                world.setBlockToAir(coord);
            }
        }
        if (power >= 0.4f && world.rand.nextFloat() <= this.hem) {
            final BlockPos coord = this.getRandCoord(2);
            if (world.getTileEntity(coord) == null) {
                final IBlockState state = world.getBlockState(coord);
                final Material mat2 = state.getMaterial();
                if (mat2 == Material.WOOD || mat2 == Material.LEAVES || mat2 == Material.CLOTH) {
                    world.setBlockState(coord, Blocks.FIRE.getDefaultState());
                }
            }
        }
        return false;
    }
    
    public BlockPos getRandCoord(final int radius) {
        if (radius <= 0) {
            return null;
        }
        final World world = this.getWorld();
        BlockPos ret;
        do {
            ret = this.pos.add(world.rand.nextInt(2 * radius + 1) - radius, world.rand.nextInt(2 * radius + 1) - radius, world.rand.nextInt(2 * radius + 1) - radius);
        } while (ret.equals((Object)this.pos));
        return ret;
    }
    
    public void processChambers() {
        final int size = this.getReactorSize();
        for (int pass = 0; pass < 2; ++pass) {
            for (int y = 0; y < 6; ++y) {
                for (int x = 0; x < size; ++x) {
                    final ItemStack stack = this.reactorSlot.get(x, y);
                    if (stack != null && stack.getItem() instanceof IReactorComponent) {
                        final IReactorComponent comp = (IReactorComponent)stack.getItem();
                        comp.processChamber(stack, this, x, y, pass == 0);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean produceEnergy() {
        return this.redstone.hasRedstoneInput() && ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear") > 0.0f;
    }
    
    public int getReactorSize() {
        final World world = this.getWorld();
        if (world == null) {
            return 9;
        }
        int cols = 3;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof TileEntityReactorChamberElectric) {
                ++cols;
            }
        }
        return cols;
    }
    
    private boolean isFullSize() {
        return this.getReactorSize() == 9;
    }
    
    @Override
    public int getTickRate() {
        return 20;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return !StackUtil.checkItemEquality(StackUtil.get(player, hand), BlockName.te.getItemStack(TeBlock.reactor_chamber)) && super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    @Override
    public ContainerBase<TileEntityNuclearReactorElectric> getGuiContainer(final EntityPlayer player) {
        return new ContainerNuclearReactor(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiNuclearReactor(new ContainerNuclearReactor(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        if (field.equals("output")) {
            if (this.output > 0.0f) {
                if (this.lastOutput <= 0.0f) {
                    if (this.audioSourceMain == null) {
                        this.audioSourceMain = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/NuclearReactorLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (this.audioSourceMain != null) {
                        this.audioSourceMain.play();
                    }
                }
                if (this.output < 40.0f) {
                    if (this.lastOutput <= 0.0f || this.lastOutput >= 40.0f) {
                        if (this.audioSourceGeiger != null) {
                            this.audioSourceGeiger.remove();
                        }
                        this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerLowEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
                        if (this.audioSourceGeiger != null) {
                            this.audioSourceGeiger.play();
                        }
                    }
                }
                else if (this.output < 80.0f) {
                    if (this.lastOutput < 40.0f || this.lastOutput >= 80.0f) {
                        if (this.audioSourceGeiger != null) {
                            this.audioSourceGeiger.remove();
                        }
                        this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerMedEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
                        if (this.audioSourceGeiger != null) {
                            this.audioSourceGeiger.play();
                        }
                    }
                }
                else if (this.output >= 80.0f && this.lastOutput < 80.0f) {
                    if (this.audioSourceGeiger != null) {
                        this.audioSourceGeiger.remove();
                    }
                    this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerHighEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    if (this.audioSourceGeiger != null) {
                        this.audioSourceGeiger.play();
                    }
                }
            }
            else if (this.lastOutput > 0.0f) {
                if (this.audioSourceMain != null) {
                    this.audioSourceMain.stop();
                }
                if (this.audioSourceGeiger != null) {
                    this.audioSourceGeiger.stop();
                }
            }
            this.lastOutput = this.output;
        }
        super.onNetworkUpdate(field);
    }
    
    @Override
    public TileEntity getCoreTe() {
        return this;
    }
    
    public BlockPos getPosition() {
        return this.pos;
    }
    
    public World getWorldObj() {
        return this.getWorld();
    }
    
    @Override
    public int getHeat() {
        return this.heat;
    }
    
    @Override
    public void setHeat(final int heat) {
        this.heat = heat;
    }
    
    @Override
    public int addHeat(final int amount) {
        return this.heat += amount;
    }
    
    @Override
    public ItemStack getItemAt(final int x, final int y) {
        if (x < 0 || x >= this.getReactorSize() || y < 0 || y >= 6) {
            return null;
        }
        return this.reactorSlot.get(x, y);
    }
    
    @Override
    public void setItemAt(final int x, final int y, final ItemStack item) {
        if (x < 0 || x >= this.getReactorSize() || y < 0 || y >= 6) {
            return;
        }
        this.reactorSlot.put(x, y, item);
    }
    
    @Override
    public void explode() {
        float boomPower = 10.0f;
        float boomMod = 1.0f;
        for (int i = 0; i < this.reactorSlot.size(); ++i) {
            final ItemStack stack = this.reactorSlot.get(i);
            if (stack != null && stack.getItem() instanceof IReactorComponent) {
                final float f = ((IReactorComponent)stack.getItem()).influenceExplosion(stack, this);
                if (f > 0.0f && f < 1.0f) {
                    boomMod *= f;
                }
                else {
                    boomPower += f;
                }
            }
            this.reactorSlot.put(i, null);
        }
        boomPower *= this.hem * boomMod;
        IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuclear Reactor at %s melted (raw explosion power %f)", Util.formatPosition(this), boomPower);
        boomPower = Math.min(boomPower, ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit"));
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof TileEntityReactorChamberElectric) {
                world.setBlockToAir(target.getPos());
            }
        }
        world.setBlockToAir(this.pos);
        final ExplosionIC2 explosion = new ExplosionIC2(world, null, this.pos, boomPower, 0.01f, ExplosionIC2.Type.Nuclear);
        explosion.doExplosion();
    }
    
    @Override
    public void addEmitHeat(final int heat) {
        this.EmitHeatbuffer += heat;
    }
    
    @Override
    public int getMaxHeat() {
        return this.maxHeat;
    }
    
    @Override
    public void setMaxHeat(final int newMaxHeat) {
        this.maxHeat = newMaxHeat;
    }
    
    @Override
    public float getHeatEffectModifier() {
        return this.hem;
    }
    
    @Override
    public void setHeatEffectModifier(final float newHEM) {
        this.hem = newHEM;
    }
    
    @Override
    public float getReactorEnergyOutput() {
        return this.output;
    }
    
    @Override
    public float addOutput(final float energy) {
        return this.output += energy;
    }
    
    @Override
    public boolean isFluidCooled() {
        return this.fluidCooled;
    }
    
    private void createChamberRedstoneLinks() {
        final World world = this.getWorld();
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final BlockPos cPos = this.pos.offset(facing);
            final TileEntity te = world.getTileEntity(cPos);
            if (te instanceof TileEntityReactorChamberElectric) {
                final TileEntityReactorChamberElectric chamber = (TileEntityReactorChamberElectric)te;
                if (chamber.redstone.isLinked() && chamber.redstone.getLinkReceiver() != this.redstone) {
                    chamber.destoryChamber(true);
                }
                else {
                    chamber.redstone.linkTo(this.redstone);
                }
            }
        }
    }
    
    private void createCasingRedstoneLinks() {
        WorldSearchUtil.findTileEntities(this.getWorld(), this.pos, 2, new WorldSearchUtil.ITileEntityResultHandler() {
            @Override
            public boolean onMatch(final TileEntity te) {
                if (te instanceof TileEntityReactorRedstonePort) {
                    ((TileEntityReactorRedstonePort)te).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone);
                }
                return false;
            }
        });
    }
    
    private void removeCasingRedstoneLinks() {
        for (final Redstone rs : this.redstone.getLinkedOrigins()) {
            if (rs.getParent() instanceof TileEntityReactorRedstonePort) {
                rs.unlinkOutbound();
            }
        }
    }
    
    private void enableFluidMode() {
        if (this.addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }
        this.createCasingRedstoneLinks();
        this.openTanks();
    }
    
    private void disableFluidMode() {
        if (!this.addedToEnergyNet) {
            this.refreshChambers();
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
        }
        this.removeCasingRedstoneLinks();
        this.closeTanks();
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
        if (!this.isFullSize()) {
            return false;
        }
        if (!this.hasFluidChamber()) {
            return false;
        }
        final int range = 2;
        final MutableBoolean foundConflict = new MutableBoolean();
        WorldSearchUtil.findTileEntities(this.getWorld(), this.pos, 4, new WorldSearchUtil.ITileEntityResultHandler() {
            @Override
            public boolean onMatch(final TileEntity te) {
                if (!(te instanceof TileEntityNuclearReactorElectric)) {
                    return false;
                }
                if (te == TileEntityNuclearReactorElectric.this) {
                    return false;
                }
                final TileEntityNuclearReactorElectric reactor = (TileEntityNuclearReactorElectric)te;
                if (reactor.isFullSize() && reactor.hasFluidChamber()) {
                    foundConflict.setTrue();
                    return true;
                }
                return false;
            }
        });
        return !foundConflict.getValue();
    }
    
    private boolean hasFluidChamber() {
        final int range = 2;
        final ChunkCache cache = new ChunkCache(this.getWorld(), this.pos.add(-2, -2, -2), this.pos.add(2, 2, 2), 0);
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 2; ++i) {
            final int y = this.pos.getY() + 2 * (i * 2 - 1);
            for (int z = this.pos.getZ() - 2; z <= this.pos.getZ() + 2; ++z) {
                for (int x = this.pos.getX() - 2; x <= this.pos.getX() + 2; ++x) {
                    cPos.setPos(x, y, z);
                    if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos)) {
                        return false;
                    }
                }
            }
        }
        for (int i = 0; i < 2; ++i) {
            final int z2 = this.pos.getZ() + 2 * (i * 2 - 1);
            for (int y2 = this.pos.getY() - 2 + 1; y2 <= this.pos.getY() + 2 - 1; ++y2) {
                for (int x = this.pos.getX() - 2; x <= this.pos.getX() + 2; ++x) {
                    cPos.setPos(x, y2, z2);
                    if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos)) {
                        return false;
                    }
                }
            }
        }
        for (int i = 0; i < 2; ++i) {
            final int x2 = this.pos.getX() + 2 * (i * 2 - 1);
            for (int y2 = this.pos.getY() - 2 + 1; y2 <= this.pos.getY() + 2 - 1; ++y2) {
                for (int z3 = this.pos.getZ() - 2 + 1; z3 <= this.pos.getZ() + 2 - 1; ++z3) {
                    cPos.setPos(x2, y2, z3);
                    if (!isFluidChamberBlock((IBlockAccess)cache, (BlockPos)cPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean isFluidChamberBlock(final IBlockAccess world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        if (state == BlockName.resource.getBlockState(ResourceBlock.reactor_vessel)) {
            return true;
        }
        final TileEntity te = world.getTileEntity(pos);
        return te != null && te instanceof IReactorChamber && ((IReactorChamber)te).isWall();
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("heat".equals(name)) {
            return (this.maxHeat == 0) ? 0.0 : (this.heat / (double)this.maxHeat);
        }
        throw new IllegalArgumentException("Invalid value: " + name);
    }
    
    public int gaugeLiquidScaled(final int i, final int tank) {
        switch (tank) {
            case 0: {
                if (this.inputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
            }
            case 1: {
                if (this.outputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
            }
            default: {
                return 0;
            }
        }
    }
    
    public FluidTank getinputtank() {
        return this.inputTank;
    }
    
    public FluidTank getoutputtank() {
        return this.outputTank;
    }
    
    @Override
    public int getInventoryStackLimit() {
        return 1;
    }
    
    static {
        huOutputModifier = 40.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/FluidReactor/outputModifier");
    }
}
