// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.Arrays;
import ic2.api.item.ElectricItem;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.init.Localization;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.ParticleManager;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.client.particle.Particle;
import ic2.core.util.EntityIC2FX;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import java.util.Set;
import net.minecraft.util.EnumFacing;
import java.util.Collection;
import java.util.EnumSet;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;

public abstract class TileEntityChargepadBlock extends TileEntityElectricBlock
{
    private static final List<AxisAlignedBB> aabbs;
    private int updateTicker;
    private EntityPlayer player;
    public static byte redstoneModes;
    
    public TileEntityChargepadBlock(final int tier1, final int output1, final int maxStorage1) {
        super(tier1, output1, maxStorage1);
        this.player = null;
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.copyOf((Collection<E>)Util.verticalFacings)), EnumSet.of(EnumFacing.DOWN));
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        this.superReadFromNBT(nbt);
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing(), (E)EnumFacing.UP)), EnumSet.of(this.getFacing()));
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        return TileEntityChargepadBlock.aabbs;
    }
    
    @Override
    protected void onEntityCollision(final Entity entity) {
        super.onEntityCollision(entity);
        if (!this.getWorld().isRemote && entity instanceof EntityPlayer) {
            this.updatePlayer((EntityPlayer)entity);
        }
    }
    
    private void updatePlayer(final EntityPlayer entity) {
        this.player = entity;
    }
    
    protected int getTickRate() {
        return 2;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        if (this.player != null && this.energy.getEnergy() >= 1.0) {
            if (!this.getActive()) {
                this.setActive(true);
            }
            this.getItems(this.player);
            this.player = null;
            needsInvUpdate = true;
        }
        else if (this.getActive()) {
            this.setActive(false);
            needsInvUpdate = true;
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        final World world = this.getWorld();
        final Random rnd = world.rand;
        if (rnd.nextInt(8) != 0) {
            return;
        }
        if (this.getActive()) {
            final ParticleManager effect = FMLClientHandler.instance().getClient().effectRenderer;
            for (int particles = 20; particles > 0; --particles) {
                final double x = this.pos.getX() + 0.0f + rnd.nextFloat();
                final double y = this.pos.getY() + 0.9f + rnd.nextFloat();
                final double z = this.pos.getZ() + 0.0f + rnd.nextFloat();
                effect.addEffect((Particle)new EntityIC2FX(world, x, y, z, 60, new double[] { 0.0, 0.1, 0.0 }, new float[] { 0.2f, 0.2f, 1.0f }));
            }
        }
    }
    
    protected abstract void getItems(final EntityPlayer p0);
    
    @Override
    protected boolean shouldEmitRedstone() {
        return (this.redstoneMode == 0 && this.getActive()) || (this.redstoneMode == 1 && !this.getActive());
    }
    
    @Override
    public void setFacing(final EnumFacing facing) {
        this.energy.setDirections((Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)facing, (E)EnumFacing.UP)), EnumSet.of(facing));
        this.superSetFacing(facing);
    }
    
    @Override
    public ContainerBase<TileEntityChargepadBlock> getGuiContainer(final EntityPlayer player) {
        return new ContainerChargepadBlock(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiChargepadBlock(new ContainerChargepadBlock(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        ++this.redstoneMode;
        if (this.redstoneMode >= TileEntityChargepadBlock.redstoneModes) {
            this.redstoneMode = 0;
        }
        IC2.platform.messagePlayer(player, this.getRedstoneMode(), new Object[0]);
    }
    
    @Override
    public String getRedstoneMode() {
        if (this.redstoneMode > 1 || this.redstoneMode < 0) {
            return "";
        }
        return Localization.translate("ic2.blockChargepad.gui.mod.redstone" + this.redstoneMode);
    }
    
    protected void chargeItem(final ItemStack stack, final int chargeFactor) {
        if (stack.getItem() == ItemName.debug_item.getInstance()) {
            return;
        }
        final double freeAmount = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.energy.getSourceTier(), true, true);
        double charge = 0.0;
        if (freeAmount >= 0.0) {
            if (freeAmount >= chargeFactor * this.getTickRate()) {
                charge = chargeFactor * this.getTickRate();
            }
            else {
                charge = freeAmount;
            }
            if (this.energy.getEnergy() < charge) {
                charge = this.energy.getEnergy();
            }
            this.energy.useEnergy(ElectricItem.manager.charge(stack, charge, this.energy.getSourceTier(), true, false));
        }
    }
    
    static {
        aabbs = Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0));
        TileEntityChargepadBlock.redstoneModes = 2;
    }
}
