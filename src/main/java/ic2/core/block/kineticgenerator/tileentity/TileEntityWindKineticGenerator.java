// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.item.ItemStack;
import ic2.core.util.StackUtil;
import ic2.core.WorldData;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import ic2.core.init.Localization;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import ic2.core.util.Util;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableKineticRotor;
import ic2.api.item.IKineticRotor;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IC2;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.api.tile.IRotorProvider;
import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityWindKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui
{
    public final InvSlotConsumableClass rotorSlot;
    private double windStrength;
    private int obstructedCrossSection;
    private int crossSection;
    private int updateTicker;
    private float rotationSpeed;
    private float angle;
    private long lastcheck;
    private static final double efficiencyRollOffExponent = 2.0;
    public static final float outputModifier;
    private static final ResourceLocation woodenRotorTexture;
    
    public TileEntityWindKineticGenerator() {
        this.angle = 0.0f;
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
        this.rotorSlot = new InvSlotConsumableKineticRotor(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WIND, "rotorSlot");
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        boolean needsInvUpdate = false;
        boolean isActive = this.getActive();
        if ((this.hasRotor() && this.rotorHasSpace()) != isActive) {
            this.setActive(isActive = !isActive);
            needsInvUpdate = true;
        }
        if (isActive) {
            this.crossSection = Util.square(this.getRotorDiameter() / 2 * 2 * 2 + 1);
            this.obstructedCrossSection = this.checkSpace(this.getRotorDiameter() * 3, false);
            if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (this.getRotorDiameter() + 1) / 2) {
                this.obstructedCrossSection = 0;
            }
            if (this.obstructedCrossSection < 0) {
                this.windStrength = 0.0;
                this.setRotationSpeed(0.0f);
            }
            else {
                this.windStrength = this.calcWindStrength();
                final float speed = (float)Util.limit((this.windStrength - this.getMinWindStrength()) / this.getMaxWindStrength(), 0.0, 2.0);
                this.setRotationSpeed(speed);
                if (this.windStrength >= this.getMinWindStrength()) {
                    if (this.windStrength <= this.getMaxWindStrength()) {
                        this.rotorSlot.damage(1, false);
                    }
                    else {
                        this.rotorSlot.damage(4, false);
                    }
                    needsInvUpdate = true;
                }
            }
        }
        else {
            this.setRotationSpeed(0.0f);
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("rotationSpeed");
        ret.add("rotorSlot");
        return ret;
    }
    
    @Override
    public ContainerBase<TileEntityWindKineticGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerWindKineticGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiWindKineticGenerator(new ContainerWindKineticGenerator(player, this));
    }
    
    public boolean facingMatchesDirection(final EnumFacing direction) {
        return direction == this.getFacing();
    }
    
    public String getRotorHealth() {
        if (!this.rotorSlot.isEmpty()) {
            return Localization.translate("ic2.WindKineticGenerator.gui.rotorhealth", (int)(100.0f - this.rotorSlot.get().getItemDamage() / (float)this.rotorSlot.get().getMaxDamage() * 100.0f));
        }
        return "";
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return this.getConnectionBandwidth(directionFrom);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        return this.facingMatchesDirection(side.getOpposite()) ? this.getKuOutput() : 0;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        if (this.facingMatchesDirection(side.getOpposite())) {
            return Math.min(request, this.getKuOutput());
        }
        return 0;
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public int checkSpace(int length, final boolean onlyrotor) {
        int box = this.getRotorDiameter() / 2;
        int lentemp = 0;
        if (onlyrotor) {
            length = 1;
            lentemp = length + 1;
        }
        if (!onlyrotor) {
            box *= 2;
        }
        final EnumFacing fwdDir = this.getFacing();
        final EnumFacing rightDir = fwdDir.rotateAround(EnumFacing.DOWN.getAxis());
        final int xMaxDist = Math.abs(length * fwdDir.getFrontOffsetX() + box * rightDir.getFrontOffsetX());
        final int zMaxDist = Math.abs(length * fwdDir.getFrontOffsetZ() + box * rightDir.getFrontOffsetZ());
        final ChunkCache chunkCache = new ChunkCache(this.getWorld(), this.pos.add(-xMaxDist, -box, -zMaxDist), this.pos.add(xMaxDist, box, zMaxDist), 0);
        int ret = 0;
        final int xCoord = this.pos.getX();
        final int yCoord = this.pos.getY();
        final int zCoord = this.pos.getZ();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int up = -box; up <= box; ++up) {
            final int y = yCoord + up;
            for (int right = -box; right <= box; ++right) {
                boolean occupied = false;
                for (int fwd = lentemp - length; fwd <= length; ++fwd) {
                    final int x = xCoord + fwd * fwdDir.getFrontOffsetX() + right * rightDir.getFrontOffsetX();
                    final int z = zCoord + fwd * fwdDir.getFrontOffsetZ() + right * rightDir.getFrontOffsetZ();
                    pos.setPos(x, y, z);
                    assert Math.abs(x - xCoord) <= xMaxDist;
                    assert Math.abs(z - zCoord) <= zMaxDist;
                    final IBlockState state = chunkCache.getBlockState((BlockPos)pos);
                    final Block block = state.getBlock();
                    if (!block.isAir(state, (IBlockAccess)chunkCache, (BlockPos)pos)) {
                        occupied = true;
                        if ((up != 0 || right != 0 || fwd != 0) && chunkCache.getTileEntity((BlockPos)pos) instanceof TileEntityWindKineticGenerator && !onlyrotor) {
                            return -1;
                        }
                    }
                }
                if (occupied) {
                    ++ret;
                }
            }
        }
        return ret;
    }
    
    public boolean hasRotor() {
        return !this.rotorSlot.isEmpty();
    }
    
    public boolean rotorHasSpace() {
        return this.checkSpace(1, true) == 0;
    }
    
    private void setRotationSpeed(final float speed) {
        if (this.rotationSpeed != speed) {
            this.rotationSpeed = speed;
            IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
        }
    }
    
    public int getTickRate() {
        return 32;
    }
    
    public double calcWindStrength() {
        double windStr = WorldData.get(this.getWorld()).windSim.getWindAt(this.pos.getY());
        windStr *= 1.0 - Math.pow(this.obstructedCrossSection / (double)this.crossSection, 2.0);
        return Math.max(0.0, windStr);
    }
    
    @Override
    public float getAngle() {
        if (this.rotationSpeed != 0.0f) {
            this.angle += (System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed;
            this.angle %= 360.0f;
        }
        this.lastcheck = System.currentTimeMillis();
        return this.angle;
    }
    
    public float getEfficiency() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getEfficiency(stack);
        }
        return 0.0f;
    }
    
    public int getMinWindStrength() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getMinWindStrength(stack);
        }
        return 0;
    }
    
    public int getMaxWindStrength() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getMaxWindStrength(stack);
        }
        return 0;
    }
    
    @Override
    public int getRotorDiameter() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getDiameter(stack);
        }
        return 0;
    }
    
    @Override
    public ResourceLocation getRotorRenderTexture() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getRotorRenderTexture(stack);
        }
        return TileEntityWindKineticGenerator.woodenRotorTexture;
    }
    
    public boolean isRotorOverloaded() {
        return this.hasRotor() && this.rotorHasSpace() && this.isWindStrongEnough() && this.windStrength > this.getMaxWindStrength();
    }
    
    public boolean isWindStrongEnough() {
        return this.windStrength >= this.getMinWindStrength();
    }
    
    public int getKuOutput() {
        if (this.windStrength >= this.getMinWindStrength() && this.getActive()) {
            return (int)(this.windStrength * TileEntityWindKineticGenerator.outputModifier * this.getEfficiency());
        }
        return 0;
    }
    
    public int getWindStrength() {
        return (int)this.windStrength;
    }
    
    public int getObstructions() {
        return this.obstructedCrossSection;
    }
    
    @Override
    public void setActive(final boolean active) {
        if (active != this.getActive()) {
            IC2.network.get(true).updateTileEntityField(this, "rotorSlot");
        }
        super.setActive(active);
    }
    
    static {
        outputModifier = 10.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/wind");
        woodenRotorTexture = new ResourceLocation("ic2", "textures/items/rotor/wood_rotor_model.png");
    }
}
