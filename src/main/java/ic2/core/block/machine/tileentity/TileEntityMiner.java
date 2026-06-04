// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.audio.PositionSpec;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiMiner;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.ContainerBase;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import java.util.Iterator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.Block;
import ic2.core.util.LiquidUtil;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import ic2.core.Ic2Player;
import ic2.core.util.StackUtil;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import ic2.core.util.Ic2BlockPos;
import ic2.core.block.machine.BlockMiningPipe;
import ic2.core.ref.BlockName;
import ic2.api.item.ElectricItem;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.ref.ItemName;
import net.minecraft.item.Item;
import ic2.core.block.TileEntityInventory;
import ic2.core.InvSlotConsumableBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.api.item.IMiningDrill;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlot;
import ic2.core.audio.AudioSource;
import net.minecraft.util.math.BlockPos;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

public class TileEntityMiner extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
    private Mode lastMode;
    public int progress;
    private int scannedLevel;
    private int scanRange;
    private int lastX;
    private int lastZ;
    public boolean pumpMode;
    public boolean canProvideLiquid;
    public BlockPos liquidPos;
    private AudioSource audioSource;
    public final InvSlot buffer;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotConsumable drillSlot;
    public final InvSlotConsumable pipeSlot;
    public final InvSlotConsumable scannerSlot;
    boolean tickingUpgrades;
    
    public TileEntityMiner() {
        super(1000, ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), false);
        this.lastMode = Mode.None;
        this.progress = 0;
        this.scannedLevel = -1;
        this.scanRange = 0;
        this.pumpMode = false;
        this.canProvideLiquid = false;
        this.tickingUpgrades = false;
        this.drillSlot = new InvSlotConsumableClass(this, "drill", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, IMiningDrill.class) {
            @Override
            public boolean canOutput() {
                return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
            }
        };
        this.pipeSlot = new InvSlotConsumableBlock(this, "pipe", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP) {
            @Override
            public boolean canOutput() {
                return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
            }
        };
        this.scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new Item[] { ItemName.scanner.getInstance(), ItemName.advanced_scanner.getInstance() }) {
            @Override
            public boolean canOutput() {
                return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
            }
        };
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 1);
        this.buffer = new InvSlot(this, "buffer", InvSlot.Access.IO, 15, InvSlot.InvSide.SIDE);
    }
    
    protected void onLoaded() {
        super.onLoaded();
        this.scannedLevel = -1;
        this.lastX = this.pos.getX();
        this.lastZ = this.pos.getZ();
        this.canProvideLiquid = false;
    }
    
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        super.onUnloaded();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.lastMode = Mode.values()[nbtTagCompound.getInteger("lastMode")];
        this.progress = nbtTagCompound.getInteger("progress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("lastMode", this.lastMode.ordinal());
        nbt.setInteger("progress", this.progress);
        return nbt;
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.chargeTools();
        this.tickingUpgrades = true;
        this.upgradeSlot.tick();
        this.tickingUpgrades = false;
        if (this.work()) {
            this.markDirty();
            this.setActive(true);
        }
        else {
            this.setActive(false);
        }
    }
    
    private void chargeTools() {
        if (!this.scannerSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), 2, false, false));
        }
        if (!this.drillSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.drillSlot.get(), this.energy.getEnergy(), 3, false, false));
        }
    }
    
    private boolean work() {
        final Ic2BlockPos operatingPos = this.getOperationPos();
        if (this.drillSlot.isEmpty()) {
            return this.withDrawPipe(operatingPos);
        }
        if (operatingPos.isBelowMap()) {
            return false;
        }
        final World world = this.getWorld();
        IBlockState state = world.getBlockState((BlockPos)operatingPos);
        if (state != BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip)) {
            return operatingPos.getY() > 0 && this.digDown(operatingPos, state, false);
        }
        final MineResult result = this.mineLevel(operatingPos.getY());
        if (result == MineResult.Done) {
            operatingPos.moveDown();
            state = world.getBlockState((BlockPos)operatingPos);
            return this.digDown(operatingPos, state, true);
        }
        return result == MineResult.Working;
    }
    
    private Ic2BlockPos getOperationPos() {
        final Ic2BlockPos ret = new Ic2BlockPos((Vec3i)this.pos).moveDown();
        final World world = this.getWorld();
        final IBlockState pipeState = BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe);
        while (!ret.isBelowMap()) {
            final IBlockState state = ret.getBlockState((IBlockAccess)world);
            if (state != pipeState) {
                return ret;
            }
            ret.moveDown();
        }
        return ret;
    }
    
    private boolean withDrawPipe(final Ic2BlockPos operatingPos) {
        if (this.lastMode != Mode.Withdraw) {
            this.lastMode = Mode.Withdraw;
            this.progress = 0;
        }
        if (operatingPos.isBelowMap() || this.getWorld().getBlockState((BlockPos)operatingPos) != BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip)) {
            operatingPos.moveUp();
        }
        if (operatingPos.getY() != this.pos.getY() && this.energy.getEnergy() >= 3.0) {
            if (this.progress < 20) {
                this.energy.useEnergy(3.0);
                ++this.progress;
            }
            else {
                this.progress = 0;
                this.removePipe(operatingPos);
            }
            return true;
        }
        return false;
    }
    
    private void removePipe(final Ic2BlockPos operatingPos) {
        final World world = this.getWorld();
        world.setBlockToAir((BlockPos)operatingPos);
        this.storeDrop(BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe));
        final ItemStack pipe = this.pipeSlot.consume(1, true, false);
        if (pipe != null && !StackUtil.checkItemEquality(pipe, BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe))) {
            final ItemStack filler = this.pipeSlot.consume(1);
            final Item fillerItem = filler.getItem();
            final EntityPlayer player = Ic2Player.get(world);
            player.setHeldItem(EnumHand.MAIN_HAND, filler);
            try {
                if (fillerItem instanceof ItemBlock) {
                    ((ItemBlock)fillerItem).onItemUse(player, world, operatingPos.up(), EnumHand.MAIN_HAND, EnumFacing.DOWN, 0.0f, 0.0f, 0.0f);
                }
            }
            finally {
                player.setHeldItem(EnumHand.MAIN_HAND, StackUtil.emptyStack);
            }
        }
    }
    
    private boolean digDown(final Ic2BlockPos operatingPos, final IBlockState state, final boolean removeTipAbove) {
        final ItemStack pipe = this.pipeSlot.consume(1, true, false);
        if (pipe == null || !StackUtil.checkItemEquality(pipe, BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe))) {
            return false;
        }
        if (operatingPos.isBelowMap()) {
            if (removeTipAbove) {
                this.getWorld().setBlockState((BlockPos)operatingPos.setY(0), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
            }
            return false;
        }
        final MineResult result = this.mineBlock(operatingPos, state);
        if (result == MineResult.Failed_Temp || result == MineResult.Failed_Perm) {
            if (removeTipAbove) {
                this.getWorld().setBlockState((BlockPos)operatingPos.moveUp(), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
            }
            return false;
        }
        if (result == MineResult.Done) {
            if (removeTipAbove) {
                this.getWorld().setBlockState(operatingPos.up(), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
            }
            this.pipeSlot.consume(1);
            this.getWorld().setBlockState((BlockPos)operatingPos, BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip));
        }
        return true;
    }
    
    private MineResult mineLevel(final int y) {
        if (this.scannerSlot.isEmpty()) {
            return MineResult.Done;
        }
        if (this.scannedLevel != y) {
            this.scanRange = ((ItemScanner)this.scannerSlot.get().getItem()).startLayerScan(this.scannerSlot.get());
        }
        if (this.scanRange > 0) {
            this.scannedLevel = y;
            final BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
            final World world = this.getWorld();
            final EntityPlayer player = Ic2Player.get(world);
            for (int x = this.pos.getX() - this.scanRange; x <= this.pos.getX() + this.scanRange; ++x) {
                for (int z = this.pos.getZ() - this.scanRange; z <= this.pos.getZ() + this.scanRange; ++z) {
                    target.setPos(x, y, z);
                    final IBlockState state = world.getBlockState((BlockPos)target);
                    boolean isValidTarget = false;
                    if ((OreValues.get(StackUtil.getDrops((IBlockAccess)world, (BlockPos)target, state, 0)) > 0 || OreValues.get(StackUtil.getPickStack(world, (BlockPos)target, state, player)) > 0) && this.canMine((BlockPos)target, state)) {
                        isValidTarget = true;
                    }
                    else if (this.pumpMode) {
                        final LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, (BlockPos)target);
                        if (liquid != null && this.canPump((BlockPos)target)) {
                            isValidTarget = true;
                        }
                    }
                    if (isValidTarget) {
                        final MineResult result = this.mineTowards((BlockPos)target);
                        if (result == MineResult.Done) {
                            return MineResult.Working;
                        }
                        if (result != MineResult.Failed_Perm) {
                            return result;
                        }
                    }
                }
            }
            return MineResult.Done;
        }
        return MineResult.Failed_Temp;
    }
    
    private MineResult mineTowards(final BlockPos dst) {
        final int dx = Math.abs(dst.getX() - this.pos.getX());
        final int sx = (this.pos.getX() < dst.getX()) ? 1 : -1;
        final int dz = -Math.abs(dst.getZ() - this.pos.getZ());
        final int sz = (this.pos.getZ() < dst.getZ()) ? 1 : -1;
        int err = dx + dz;
        final BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        int cx = this.pos.getX();
        int cz = this.pos.getZ();
        while (cx != dst.getX() || cz != dst.getZ()) {
            final boolean isCurrentPos = cx == this.lastX && cz == this.lastZ;
            final int e2 = 2 * err;
            if (e2 > dz) {
                err += dz;
                cx += sx;
            }
            else if (e2 < dx) {
                err += dx;
                cz += sz;
            }
            target.setPos(cx, dst.getY(), cz);
            final World world = this.getWorld();
            final IBlockState state = world.getBlockState((BlockPos)target);
            boolean isBlocking = false;
            if (isCurrentPos) {
                isBlocking = true;
            }
            else if (!state.getBlock().isAir(state, (IBlockAccess)world, (BlockPos)target)) {
                final LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, (BlockPos)target);
                if (liquid == null || liquid.isSource || (this.pumpMode && this.canPump((BlockPos)target))) {
                    isBlocking = true;
                }
            }
            if (isBlocking) {
                final MineResult result = this.mineBlock((BlockPos)target, state);
                if (result == MineResult.Done) {
                    this.lastX = cx;
                    this.lastZ = cz;
                }
                return result;
            }
        }
        this.lastX = this.pos.getX();
        this.lastZ = this.pos.getZ();
        return MineResult.Done;
    }
    
    private MineResult mineBlock(final BlockPos target, final IBlockState state) {
        final World world = this.getWorld();
        final Block block = state.getBlock();
        boolean isAirBlock = true;
        if (!block.isAir(state, (IBlockAccess)world, target)) {
            isAirBlock = false;
            final LiquidUtil.LiquidData liquidData = LiquidUtil.getLiquid(world, target);
            if (liquidData != null) {
                if (liquidData.isSource || (this.pumpMode && this.canPump(target))) {
                    this.liquidPos = new BlockPos((Vec3i)target);
                    this.canProvideLiquid = true;
                    return (this.pumpMode || this.canMine(target, state)) ? MineResult.Failed_Temp : MineResult.Failed_Perm;
                }
            }
            else if (!this.canMine(target, state)) {
                return MineResult.Failed_Perm;
            }
        }
        this.canProvideLiquid = false;
        Mode mode;
        int energyPerTick;
        int duration;
        if (isAirBlock) {
            mode = Mode.MineAir;
            energyPerTick = 3;
            duration = 20;
        }
        else if (this.drillSlot.get().getItem() == ItemName.drill.getInstance()) {
            mode = Mode.MineDrill;
            energyPerTick = 6;
            duration = 200;
        }
        else if (this.drillSlot.get().getItem() == ItemName.diamond_drill.getInstance()) {
            mode = Mode.MineDDrill;
            energyPerTick = 20;
            duration = 50;
        }
        else if (this.drillSlot.get().getItem() == ItemName.iridium_drill.getInstance()) {
            mode = Mode.MineIDrill;
            energyPerTick = 200;
            duration = 20;
        }
        else {
            if (!(this.drillSlot.get().getItem() instanceof IMiningDrill)) {
                throw new IllegalStateException("invalid drill: " + this.drillSlot.get());
            }
            mode = Mode.MineCustomDrill;
            final IMiningDrill drill = (IMiningDrill)this.drillSlot.get().getItem();
            energyPerTick = drill.energyUse(this.drillSlot.get(), world, target, state);
            duration = drill.breakTime(this.drillSlot.get(), world, target, state);
        }
        if (this.lastMode != mode) {
            this.lastMode = mode;
            this.progress = 0;
        }
        if (this.progress < duration) {
            if (this.energy.useEnergy(energyPerTick)) {
                ++this.progress;
                return MineResult.Working;
            }
        }
        else if (isAirBlock || this.harvestBlock(target, state)) {
            this.progress = 0;
            return MineResult.Done;
        }
        return MineResult.Failed_Temp;
    }
    
    private boolean harvestBlock(final BlockPos target, final IBlockState state) {
        final int energyCost = 2 * (this.pos.getY() - target.getY());
        if (this.energy.getEnergy() < energyCost) {
            return false;
        }
        final World world = this.getWorld();
        switch (this.lastMode) {
            case MineDrill: {
                if (!ElectricItem.manager.use(this.drillSlot.get(), 50.0, null)) {
                    return false;
                }
                break;
            }
            case MineDDrill: {
                if (!ElectricItem.manager.use(this.drillSlot.get(), 80.0, null)) {
                    return false;
                }
                break;
            }
            case MineIDrill: {
                if (!ElectricItem.manager.use(this.drillSlot.get(), 800.0, null)) {
                    return false;
                }
                break;
            }
            case MineCustomDrill: {
                if (!((IMiningDrill)this.drillSlot.get().getItem()).breakBlock(this.drillSlot.get(), world, target, state)) {
                    return false;
                }
                break;
            }
            default: {
                throw new IllegalStateException("Invalid mode " + this.lastMode + " with drill: " + this.drillSlot.get());
            }
        }
        this.energy.useEnergy(energyCost);
        for (final ItemStack drop : StackUtil.getDrops((IBlockAccess)world, target, state, (this.lastMode == Mode.MineIDrill) ? 3 : 0)) {
            this.storeDrop(drop);
        }
        world.setBlockToAir(target);
        return true;
    }
    
    private void storeDrop(final ItemStack stack) {
        if (StackUtil.putInInventory(this, EnumFacing.WEST, stack, true) == 0) {
            StackUtil.dropAsEntity(this.getWorld(), this.pos, stack);
        }
        else {
            StackUtil.putInInventory(this, EnumFacing.WEST, stack, false);
        }
    }
    
    public boolean canPump(final BlockPos target) {
        return false;
    }
    
    public boolean canMine(final BlockPos target, final IBlockState state) {
        final Block block = state.getBlock();
        if (block.isAir(state, (IBlockAccess)this.getWorld(), target)) {
            return true;
        }
        if (block == BlockName.mining_pipe.getInstance() || block == Blocks.CHEST) {
            return false;
        }
        if (block instanceof IFluidBlock && this.isPumpConnected(target)) {
            return true;
        }
        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) && this.isPumpConnected(target)) {
            return true;
        }
        final World world = this.getWorld();
        return state.getBlockHardness(world, target) >= 0.0f && ((block.canCollideCheck(state, false) && state.getMaterial().isToolNotRequired()) || block == Blocks.WEB || (!this.drillSlot.isEmpty() && (ForgeHooks.canToolHarvestBlock((IBlockAccess)world, target, this.drillSlot.get()) || this.drillSlot.get().canHarvestBlock(state))));
    }
    
    public boolean isPumpConnected(final BlockPos target) {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityPump && ((TileEntityPump)te).pump(target, true, this) != null) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isAnyPumpConnected() {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityPump) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ContainerBase<TileEntityMiner> getGuiContainer(final EntityPlayer player) {
        return new ContainerMiner(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiMiner(new ContainerMiner(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public void onNetworkUpdate(final String field) {
        if (field.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/MinerOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
            }
            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            }
            else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }
        super.onNetworkUpdate(field);
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    enum Mode
    {
        None, 
        Withdraw, 
        MineAir, 
        MineDrill, 
        MineDDrill, 
        MineIDrill, 
        MineCustomDrill;
    }
    
    enum MineResult
    {
        Working, 
        Done, 
        Failed_Temp, 
        Failed_Perm;
    }
}
