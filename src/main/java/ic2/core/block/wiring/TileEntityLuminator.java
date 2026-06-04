// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.Arrays;
import java.util.EnumMap;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.Entity;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.EnergyNet;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.world.IBlockAccess;
import ic2.core.Ic2Player;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import ic2.core.IWorldTickCallback;
import ic2.core.IC2;
import java.util.Collections;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.Energy;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import net.minecraft.util.EnumFacing;
import java.util.Map;
import ic2.core.block.TileEntityBlock;

public class TileEntityLuminator extends TileEntityBlock
{
    private static final int manualChargeCapacity = 10000;
    private static final Map<EnumFacing, List<AxisAlignedBB>> aabbMap;
    private final Energy energy;
    private final Redstone redstone;
    private final ComparatorEmitter comparator;
    private boolean invertRedstone;
    public static boolean ignoreBlockStay;
    
    public TileEntityLuminator() {
        this.energy = this.addComponent(Energy.asBasicSink(this, 5.0));
        this.redstone = this.addComponent(new Redstone(this));
        (this.comparator = this.addComponent(new ComparatorEmitter(this))).setUpdate(this.energy::getComparatorValue);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.invertRedstone = nbt.getBoolean("invert");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("invert", this.invertRedstone);
        return nbt;
    }
    
    public void onLoaded() {
        this.energy.setDirections(Collections.singleton(this.getFacing().getOpposite()), Collections.emptySet());
        super.onLoaded();
        IC2.tickHandler.requestSingleWorldTick(this.getWorld(), new IWorldTickCallback() {
            @Override
            public void onTick(final World world) {
                TileEntityLuminator.this.checkPlacement();
            }
        });
    }
    
    @Override
    protected EnumFacing getPlacementFacing(final EntityLivingBase placer, final EnumFacing facing) {
        return facing;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final boolean lit = this.isLit() && this.energy.useEnergy(0.25);
        if (this.getActive() != lit) {
            this.setActive(lit);
            this.updateLight();
        }
    }
    
    private boolean isLit() {
        return this.redstone.hasRedstoneInput() != this.invertRedstone;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (!this.getWorld().isRemote) {
            final ItemStack stack = StackUtil.get(player, hand);
            double amount = 10000.0 - this.energy.getEnergy();
            if (stack != null && amount > 0.0 && (amount = ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, false)) > 0.0) {
                this.energy.forceAddEnergy(amount);
            }
            else {
                this.invertRedstone = !this.invertRedstone;
                IC2.network.get(true).updateTileEntityField(this, "invertRedstone");
            }
        }
        return true;
    }
    
    @Override
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        this.checkPlacement();
    }
    
    private void checkPlacement() {
        final World world = this.getWorld();
        if (!isValidPosition(world, this.pos.offset(this.getFacing().getOpposite()), this.getFacing())) {
            this.getBlockType().harvestBlock(world, Ic2Player.get(world), this.pos, world.getBlockState(this.pos), (TileEntity)this, StackUtil.emptyStack);
            world.setBlockToAir(this.pos);
        }
    }
    
    public static boolean isValidPosition(final World world, final BlockPos pos, final EnumFacing side) {
        if (world.isRemote || TileEntityLuminator.ignoreBlockStay) {
            return true;
        }
        if (world.getBlockState(pos).getBlockFaceShape((IBlockAccess)world, pos, side) == BlockFaceShape.SOLID) {
            return true;
        }
        final IEnergyTile tile = EnergyNet.instance.getSubTile(world, pos);
        return tile instanceof IEnergyEmitter;
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        return TileEntityLuminator.aabbMap.get(this.getFacing());
    }
    
    public int getLightValue() {
        return this.getActive() ? 15 : 0;
    }
    
    @Override
    protected void onEntityCollision(final Entity entity) {
        super.onEntityCollision(entity);
        if (this.getActive() && entity instanceof EntityMob) {
            final boolean isUndead = entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
            entity.setFire(isUndead ? 20 : 10);
        }
    }
    
    @Override
    protected boolean canSetFacingWrench(final EnumFacing facing, final EntityPlayer player) {
        return true;
    }
    
    @Override
    protected boolean setFacingWrench(final EnumFacing facing, final EntityPlayer player) {
        this.invertRedstone = !this.invertRedstone;
        return true;
    }
    
    public boolean wrenchCanRemove(final EntityPlayer player) {
        return false;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        super.onNetworkUpdate(field);
        if (field.equals("active")) {
            this.updateLight();
        }
    }
    
    private void updateLight() {
        this.getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.pos);
    }
    
    private static Map<EnumFacing, List<AxisAlignedBB>> getAabbMap() {
        final Map<EnumFacing, List<AxisAlignedBB>> ret = new EnumMap<EnumFacing, List<AxisAlignedBB>>(EnumFacing.class);
        final double height = 0.0625;
        final double remHeight = 0.9375;
        for (final EnumFacing side : EnumFacing.VALUES) {
            final int dx = side.getFrontOffsetX();
            final int dy = side.getFrontOffsetY();
            final int dz = side.getFrontOffsetZ();
            final double xS = (dx + 1) / 2 * 0.9375;
            final double yS = (dy + 1) / 2 * 0.9375;
            final double zS = (dz + 1) / 2 * 0.9375;
            final double xE = 0.0625 + (dx + 2) / 2 * 0.9375;
            final double yE = 0.0625 + (dy + 2) / 2 * 0.9375;
            final double zE = 0.0625 + (dz + 2) / 2 * 0.9375;
            ret.put(side.getOpposite(), Arrays.asList(new AxisAlignedBB(xS, yS, zS, xE, yE, zE)));
        }
        return ret;
    }
    
    static {
        aabbMap = getAabbMap();
        TileEntityLuminator.ignoreBlockStay = false;
    }
}
