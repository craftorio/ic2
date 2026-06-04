// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.ContainerBase;
import ic2.core.init.Localization;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.util.Util;
import net.minecraftforge.common.BiomeDictionary;
import ic2.core.util.BiomeUtil;
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
public class TileEntityWaterKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui
{
    public InvSlotConsumableClass rotorSlot;
    public BiomeState type;
    protected int updateTicker;
    private boolean rightFacing;
    private int distanceToNormalBiome;
    private int crossSection;
    private int obstructedCrossSection;
    private int waterFlow;
    private long lastcheck;
    private float angle;
    private float rotationSpeed;
    private static final float rotationModifier = 0.1f;
    private static final double efficiencyRollOffExponent = 2.0;
    private static final float outputModifier;
    private static final ResourceLocation woodenRotorTexture;
    
    public TileEntityWaterKineticGenerator() {
        this.type = BiomeState.UNKNOWN;
        this.angle = 0.0f;
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
        this.rotorSlot = new InvSlotConsumableKineticRotor(this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WATER, "rotorSlot");
    }
    
    protected int getTickRate() {
        return 20;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateSeaInfo();
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        final World world = this.getWorld();
        if (this.type == BiomeState.UNKNOWN) {
            final Biome biome = BiomeUtil.getBiome(world, this.pos);
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
                this.type = BiomeState.OCEAN;
            }
            else {
                if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)) {
                    this.type = BiomeState.INVALID;
                    return;
                }
                this.type = BiomeState.RIVER;
            }
        }
        boolean nextActive = this.getActive();
        boolean needsInvUpdate = false;
        if (!this.rotorSlot.isEmpty() && this.checkSpace(1, true) == 0) {
            if (!nextActive) {
                needsInvUpdate = (nextActive = true);
            }
        }
        else if (nextActive) {
            nextActive = false;
            needsInvUpdate = true;
        }
        if (nextActive) {
            this.crossSection = Util.square(this.getRotorDiameter() / 2 * 2 * 2 + 1);
            this.obstructedCrossSection = this.checkSpace(this.getRotorDiameter() * 3, false);
            if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (this.getRotorDiameter() + 1) / 2) {
                this.obstructedCrossSection = 0;
            }
            int rotorDamage = 0;
            if (this.obstructedCrossSection < 0) {
                this.stopSpinning();
            }
            else if (this.type == BiomeState.OCEAN) {
                float diff = (float)Math.sin(world.getWorldTime() * 3.141592653589793 / 6000.0);
                diff *= Math.abs(diff);
                this.rotationSpeed = (float)(diff * this.distanceToNormalBiome / 100.0f * (1.0 - Math.pow(this.obstructedCrossSection / (double)this.crossSection, 2.0)));
                this.waterFlow = (int)(this.rotationSpeed * 3000.0f);
                if (this.rightFacing) {
                    this.rotationSpeed *= -1.0f;
                }
                IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
                this.waterFlow *= (int)this.getEfficiency();
                rotorDamage = 2;
            }
            else if (this.type == BiomeState.RIVER) {
                this.rotationSpeed = Util.limit(this.distanceToNormalBiome, 20, 50) / 50.0f;
                this.waterFlow = (int)(this.rotationSpeed * 1000.0f);
                if (this.getFacing() == EnumFacing.EAST || this.getFacing() == EnumFacing.NORTH) {
                    this.rotationSpeed *= -1.0f;
                }
                IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
                this.waterFlow *= (int)(this.getEfficiency() * (1.0f - 0.3f * world.rand.nextFloat() - 0.1f * (this.obstructedCrossSection / (float)this.crossSection)));
                rotorDamage = 1;
            }
            this.rotorSlot.damage(rotorDamage, false);
        }
        else {
            this.stopSpinning();
        }
        this.setActive(nextActive);
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    protected void stopSpinning() {
        final boolean update = this.rotationSpeed != 0.0f;
        this.rotationSpeed = 0.0f;
        this.waterFlow = 0;
        if (update) {
            IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
        }
    }
    
    public void setFacing(final EnumFacing side) {
        super.setFacing(side);
        this.updateSeaInfo();
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("rotationSpeed");
        ret.add("rotorSlot");
        return ret;
    }
    
    @Override
    public int getRotorDiameter() {
        final ItemStack stack = this.rotorSlot.get();
        if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof IKineticRotor)) {
            return 0;
        }
        if (this.type == BiomeState.OCEAN) {
            return ((IKineticRotor)stack.getItem()).getDiameter(stack);
        }
        return (((IKineticRotor)stack.getItem()).getDiameter(stack) + 1) * 2 / 3;
    }
    
    public int checkSpace(int length, final boolean onlyrotor) {
        int box = this.getRotorDiameter() / 2;
        int lentemp = 0;
        if (onlyrotor) {
            length = 1;
            lentemp = length + 1;
        }
        else {
            box *= 2;
        }
        final EnumFacing fwdDir = this.getFacing();
        final EnumFacing rightDir = fwdDir.rotateAround(EnumFacing.DOWN.getAxis());
        int ret = 0;
        final int xCoord = this.pos.getX();
        final int yCoord = this.pos.getY();
        final int zCoord = this.pos.getZ();
        final World world = this.getWorld();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int up = -box; up <= box; ++up) {
            final int y = yCoord + up;
            for (int right = -box; right <= box; ++right) {
                boolean occupied = false;
                for (int fwd = lentemp - length; fwd <= length; ++fwd) {
                    final int x = xCoord + fwd * fwdDir.getFrontOffsetX() + right * rightDir.getFrontOffsetX();
                    final int z = zCoord + fwd * fwdDir.getFrontOffsetZ() + right * rightDir.getFrontOffsetZ();
                    pos.setPos(x, y, z);
                    if (world.getBlockState((BlockPos)pos).getBlock() != Blocks.WATER) {
                        occupied = true;
                        if ((up != 0 || right != 0 || fwd != 0) && world.getTileEntity((BlockPos)pos) instanceof TileEntityWaterKineticGenerator && !onlyrotor) {
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
    
    public void updateSeaInfo() {
        final World world = this.getWorld();
        final EnumFacing facing = this.getFacing();
        for (int distance = 1; distance < 200; ++distance) {
            Biome biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, distance));
            if (!this.isValidBiome(biomeTemp)) {
                this.distanceToNormalBiome = distance;
                this.rightFacing = true;
                return;
            }
            biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, -distance));
            if (!this.isValidBiome(biomeTemp)) {
                this.distanceToNormalBiome = distance;
                this.rightFacing = false;
                return;
            }
        }
        this.distanceToNormalBiome = 200;
        this.rightFacing = true;
    }
    
    public boolean isValidBiome(final Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
    }
    
    @Override
    public int maxrequestkineticenergyTick(final EnumFacing directionFrom) {
        return this.getConnectionBandwidth(directionFrom);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        return (side.getOpposite() == this.getFacing()) ? this.getKuOutput() : 0;
    }
    
    @Override
    public int requestkineticenergy(final EnumFacing directionFrom, final int requestkineticenergy) {
        return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
    }
    
    @Override
    public int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        if (side.getOpposite() == this.getFacing()) {
            return Math.min(request, this.getKuOutput());
        }
        return 0;
    }
    
    public int getKuOutput() {
        if (this.getActive()) {
            return (int)Math.abs(this.waterFlow * TileEntityWaterKineticGenerator.outputModifier);
        }
        return 0;
    }
    
    public float getEfficiency() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getEfficiency(stack);
        }
        return 0.0f;
    }
    
    @Override
    public ContainerWaterKineticGenerator getGuiContainer(final EntityPlayer player) {
        return new ContainerWaterKineticGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiWaterKineticGenerator(this.getGuiContainer(player));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public String getRotorHealth() {
        if (!this.rotorSlot.isEmpty()) {
            return Localization.translate("ic2.WaterKineticGenerator.gui.rotorhealth", (int)(100.0f - this.rotorSlot.get().getItemDamage() / (float)this.rotorSlot.get().getMaxDamage() * 100.0f));
        }
        return "";
    }
    
    @Override
    public ResourceLocation getRotorRenderTexture() {
        final ItemStack stack = this.rotorSlot.get();
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor) {
            return ((IKineticRotor)stack.getItem()).getRotorRenderTexture(stack);
        }
        return TileEntityWaterKineticGenerator.woodenRotorTexture;
    }
    
    @Override
    public float getAngle() {
        if (this.rotationSpeed != 0.0f) {
            this.angle += (System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed * 0.1f;
            this.angle %= 360.0f;
        }
        this.lastcheck = System.currentTimeMillis();
        return this.angle;
    }
    
    static {
        outputModifier = 0.2f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/water");
        woodenRotorTexture = new ResourceLocation("ic2", "textures/items/rotor/wood_rotor_model.png");
    }
    
    public enum BiomeState
    {
        UNKNOWN, 
        OCEAN, 
        RIVER, 
        INVALID;
    }
}
