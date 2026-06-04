// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.audio.PositionSpec;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.network.GuiSynced;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityIronFurnace extends TileEntityInventory implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener
{
    public final InvSlotProcessableSmelting inputSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableFuel fuelSlot;
    protected AudioSource audioSource;
    @GuiSynced
    public int fuel;
    @GuiSynced
    public int totalFuel;
    @GuiSynced
    public short progress;
    protected double xp;
    public static final short operationLength = 160;
    
    public TileEntityIronFurnace() {
        this.fuel = 0;
        this.totalFuel = 0;
        this.progress = 0;
        this.xp = 0.0;
        this.inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, true);
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        super.onUnloaded();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.fuel = nbt.getInteger("fuel");
        this.totalFuel = nbt.getInteger("totalFuel");
        this.progress = nbt.getShort("progress");
        this.xp = nbt.getDouble("xp");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("fuel", this.fuel);
        nbt.setInteger("totalFuel", this.totalFuel);
        nbt.setShort("progress", this.progress);
        nbt.setDouble("xp", this.xp);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.fuel <= 0 && this.canOperate()) {
            final int consumeFuel = this.fuelSlot.consumeFuel();
            this.totalFuel = consumeFuel;
            this.fuel = consumeFuel;
            if (this.fuel > 0) {
                needsInvUpdate = true;
            }
        }
        if (this.fuel > 0 && this.canOperate()) {
            ++this.progress;
            if (this.progress >= 160) {
                this.progress = 0;
                this.operate();
                needsInvUpdate = true;
            }
        }
        else {
            this.progress = 0;
        }
        if (this.fuel > 0) {
            --this.fuel;
            this.setActive(true);
        }
        else {
            this.setActive(false);
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive()) {
            final World world = this.getWorld();
            showFlames(world, this.pos, this.getFacing());
            if (world.rand.nextDouble() < 0.1) {
                world.playSound(this.pos.getX() + 0.5, (double)this.pos.getY(), this.pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }
        }
    }
    
    public static void showFlames(final World world, final BlockPos pos, final EnumFacing facing) {
        if (world.rand.nextInt(8) != 0) {
            return;
        }
        final double width = 0.625;
        final double height = 0.375;
        final double depthOffset = 0.02;
        double x = pos.getX() + (facing.getFrontOffsetX() * 1.04 + 1.0) / 2.0;
        final double y = pos.getY() + world.rand.nextFloat() * 0.375;
        double z = pos.getZ() + (facing.getFrontOffsetZ() * 1.04 + 1.0) / 2.0;
        if (facing.getAxis() == EnumFacing.Axis.X) {
            z += world.rand.nextFloat() * 0.625 - 0.3125;
        }
        else {
            x += world.rand.nextFloat() * 0.625 - 0.3125;
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0, new int[0]);
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0, new int[0]);
    }
    
    public static double spawnXP(final EntityPlayer player, final double xp) {
        final World world = player.getEntityWorld();
        long balls = (long)Math.floor(xp);
        while (balls > 0L) {
            int amount;
            if (balls < 2477L) {
                amount = EntityXPOrb.getXPSplit((int)balls);
            }
            else {
                amount = 2477;
            }
            balls -= amount;
            world.spawnEntity((Entity)new EntityXPOrb(world, player.posX, player.posY + 0.5, player.posZ + 0.5, amount));
        }
        return xp - Math.floor(xp);
    }
    
    private void operate() {
        final MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
        final ItemStack output = result.getOutput();
        this.outputSlot.add(output);
        this.inputSlot.consume(result);
        this.xp += result.getRecipe().getMetaData().getFloat("experience");
    }
    
    private boolean canOperate() {
        final MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
        return result != null && this.outputSlot.canAdd(result.getOutput());
    }
    
    public double getProgress() {
        return this.progress / 160.0;
    }
    
    public double getFuelRatio() {
        if (this.fuel <= 0) {
            return 0.0;
        }
        return this.fuel / (double)this.totalFuel;
    }
    
    @Override
    public ContainerBase<TileEntityIronFurnace> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if (name.equals("fuel")) {
            return (this.fuel == 0) ? 0.0 : (this.fuel / (double)this.totalFuel);
        }
        if (name.equals("progress")) {
            return (this.progress == 0) ? 0.0 : (this.progress / 160.0);
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (event == 0) {
            assert !this.getWorld().isRemote;
            this.xp = spawnXP(player, this.xp);
        }
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        if (field.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/IronFurnaceOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
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
}
