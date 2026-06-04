// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.ref.TeBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiAdvMiner;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.ContainerBase;
import java.util.Iterator;
import ic2.core.init.OreValues;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraftforge.fluids.IFluidBlock;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.Vec3i;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.ref.ItemName;
import net.minecraft.item.Item;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableId;
import net.minecraft.util.math.BlockPos;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityAdvMiner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
    private int maxBlockScanCount;
    public final int defaultTier;
    public final int workTick;
    public boolean blacklist;
    public boolean silkTouch;
    public boolean redstonePowered;
    private final int scanEnergy = 64;
    private final int mineEnergy = 512;
    private BlockPos mineTarget;
    private short ticker;
    public final InvSlotConsumableId scannerSlot;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlot filterSlot;
    protected final Redstone redstone;
    
    public TileEntityAdvMiner() {
        this(Math.min(2 + ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), 5));
    }
    
    public TileEntityAdvMiner(final int tier) {
        super(4000000, tier);
        this.blacklist = true;
        this.silkTouch = false;
        this.redstonePowered = false;
        this.ticker = 0;
        this.scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new Item[] { ItemName.scanner.getInstance(), ItemName.advanced_scanner.getInstance() });
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.filterSlot = new InvSlot(this, "list", null, 15);
        this.defaultTier = tier;
        this.workTick = 20;
        this.redstone = this.addComponent(new Redstone(this));
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.setUpgradestat();
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("mineTargetX")) {
            this.mineTarget = new BlockPos(nbt.getInteger("mineTargetX"), nbt.getInteger("mineTargetY"), nbt.getInteger("mineTargetZ"));
        }
        this.blacklist = nbt.getBoolean("blacklist");
        this.silkTouch = nbt.getBoolean("silkTouch");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.mineTarget != null) {
            nbt.setInteger("mineTargetX", this.mineTarget.getX());
            nbt.setInteger("mineTargetY", this.mineTarget.getY());
            nbt.setInteger("mineTargetZ", this.mineTarget.getZ());
        }
        nbt.setBoolean("blacklist", this.blacklist);
        nbt.setBoolean("silkTouch", this.silkTouch);
        return nbt;
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.getWorld().isRemote) {
            this.setUpgradestat();
        }
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.chargeTool();
        if (this.work()) {
            super.markDirty();
            this.setActive(true);
        }
        else {
            this.setActive(false);
        }
    }
    
    private boolean work() {
        if (!this.energy.canUseEnergy(512.0)) {
            return false;
        }
        if (this.redstone.hasRedstoneInput()) {
            return false;
        }
        if (this.mineTarget != null && this.mineTarget.getY() < 0) {
            return false;
        }
        final ItemStack scanner = this.scannerSlot.get();
        if (StackUtil.isEmpty(scanner) || !ElectricItem.manager.canUse(scanner, 64.0)) {
            return false;
        }
        if (++this.ticker != this.workTick) {
            return true;
        }
        this.ticker = 0;
        int range;
        if (scanner.getItem() instanceof ItemScannerAdv) {
            range = 32;
        }
        else if (scanner.getItem() instanceof ItemScanner) {
            range = 16;
        }
        else {
            range = 0;
        }
        if (this.mineTarget == null) {
            this.mineTarget = new BlockPos(this.pos.getX() - range - 1, this.pos.getY() - 1, this.pos.getZ() - range);
            if (this.mineTarget.getY() < 0) {
                return false;
            }
        }
        int blockScanCount = this.maxBlockScanCount;
        final World world = this.getWorld();
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos(this.mineTarget.getX(), this.mineTarget.getY(), this.mineTarget.getZ());
        do {
            if (scanPos.getX() < this.pos.getX() + range) {
                scanPos = new BlockPos.MutableBlockPos(scanPos.getX() + 1, scanPos.getY(), scanPos.getZ());
            }
            else if (scanPos.getZ() < this.pos.getZ() + range) {
                scanPos = new BlockPos.MutableBlockPos(this.pos.getX() - range, scanPos.getY(), scanPos.getZ() + 1);
            }
            else {
                scanPos = new BlockPos.MutableBlockPos(this.pos.getX() - range, scanPos.getY() - 1, this.pos.getZ() - range);
                if (scanPos.getY() < 0) {
                    this.mineTarget = new BlockPos((Vec3i)scanPos);
                    return true;
                }
            }
            ElectricItem.manager.discharge(scanner, 64.0, Integer.MAX_VALUE, true, false, false);
            final IBlockState state = world.getBlockState((BlockPos)scanPos);
            final Block block = state.getBlock();
            if (!block.isAir(state, (IBlockAccess)world, (BlockPos)scanPos) && this.canMine((BlockPos)scanPos, block, state)) {
                this.doMine(this.mineTarget = new BlockPos((Vec3i)scanPos), block, state);
                break;
            }
            this.mineTarget = new BlockPos((Vec3i)scanPos);
        } while (--blockScanCount > 0 && ElectricItem.manager.canUse(scanner, 64.0));
        return true;
    }
    
    private void chargeTool() {
        if (!this.scannerSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), this.energy.getSinkTier(), false, false));
        }
    }
    
    public void doMine(final BlockPos pos, final Block block, final IBlockState state) {
        final World world = this.getWorld();
        StackUtil.distributeDrops(this, new ArrayList<ItemStack>(StackUtil.getDrops((IBlockAccess)world, pos, state, null, 0, this.silkTouch)));
        world.setBlockToAir(pos);
        this.energy.useEnergy(512.0);
    }
    
    public boolean canMine(final BlockPos pos, final Block block, final IBlockState state) {
        if (block instanceof IFluidBlock || block instanceof BlockStaticLiquid || block instanceof BlockDynamicLiquid) {
            return false;
        }
        final World world = this.getWorld();
        if (state.getBlockHardness(world, pos) < 0.0f) {
            return false;
        }
        final List<ItemStack> drops = StackUtil.getDrops((IBlockAccess)world, pos, state, null, 0, this.silkTouch);
        if (drops.isEmpty()) {
            return false;
        }
        if (block.hasTileEntity(state) && OreValues.get(drops) <= 0) {
            return false;
        }
        if (this.blacklist) {
            for (final ItemStack drop : drops) {
                for (final ItemStack filter : this.filterSlot) {
                    if (StackUtil.checkItemEquality(drop, filter)) {
                        return false;
                    }
                }
            }
            return true;
        }
        for (final ItemStack drop : drops) {
            for (final ItemStack filter : this.filterSlot) {
                if (StackUtil.checkItemEquality(drop, filter)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                this.mineTarget = null;
                break;
            }
            case 1: {
                if (!this.getActive()) {
                    this.blacklist = !this.blacklist;
                    break;
                }
                break;
            }
            case 2: {
                if (!this.getActive()) {
                    this.silkTouch = !this.silkTouch;
                    break;
                }
                break;
            }
        }
    }
    
    public void setUpgradestat() {
        this.upgradeSlot.onChanged();
        final int tier = this.upgradeSlot.getTier(this.defaultTier);
        this.energy.setSinkTier(tier);
        this.dischargeSlot.setTier(tier);
        this.maxBlockScanCount = 5 * (this.upgradeSlot.augmentation + 1);
    }
    
    @Override
    public ContainerBase<TileEntityAdvMiner> getGuiContainer(final EntityPlayer player) {
        return new ContainerAdvMiner(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiAdvMiner(new ContainerAdvMiner(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    public BlockPos getMineTarget() {
        return this.mineTarget;
    }
    
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.getWorld().isRemote) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            this.energy.addEnergy(nbt.getDouble("energy"));
        }
    }
    
    protected ItemStack adjustDrop(ItemStack drop, final boolean wrench) {
        drop = super.adjustDrop(drop, wrench);
        if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self) {
            final double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
            if (retainedRatio > 0.0) {
                final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
                nbt.setDouble("energy", this.energy.getEnergy() * retainedRatio);
            }
        }
        return drop;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer);
    }
}
