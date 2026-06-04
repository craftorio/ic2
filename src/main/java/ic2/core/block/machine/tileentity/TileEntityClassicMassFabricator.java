// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.util.Util;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.audio.PositionSpec;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.network.GuiSynced;
import ic2.core.audio.AudioSource;
import ic2.core.ref.TeBlock;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.core.IHasGui;

@TeBlock.Delegated(current = TileEntityMassFabricator.class, old = TileEntityClassicMassFabricator.class)
public class TileEntityClassicMassFabricator extends TileEntityElectricMachine implements IHasGui, IExplosionPowerOverride
{
    private AudioSource audioSource;
    private AudioSource audioSourceScrap;
    @GuiSynced
    public int scrap;
    private double lastEnergy;
    private final int StateIdle = 0;
    private final int StateRunning = 1;
    private final int StateRunningScrap = 2;
    private int state;
    private int prevState;
    public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
    public final InvSlotOutput outputSlot;
    protected final Redstone redstone;
    
    public TileEntityClassicMassFabricator() {
        super(Math.round(1000000.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), TileEntityMassFabricator.DEFAULT_TIER, false);
        this.scrap = 0;
        this.state = 0;
        this.prevState = 0;
        this.amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>((IInventorySlotHolder)this, "scrap", 1, (IMachineRecipeManager)Recipes.matterAmplifier) {
            @Override
            protected ItemStack getInput(final ItemStack stack) {
                return stack;
            }
            
            @Override
            protected void setInput(final ItemStack input) {
                this.put(input);
            }
        };
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        (this.redstone = this.addComponent(new Redstone(this))).subscribe(newLevel -> this.energy.setEnabled(newLevel == 0));
        this.comparator.setUpdate(() -> {
            final int count = TileEntityInventory.calcRedstoneFromInvSlots(this.amplifierSlot);
            if (count > 0) {
                return count;
            }
            else if (this.scrap > 0) {
                return 1;
            }
            else {
                return 0;
            }
        });
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.scrap = nbt.getInteger("scrap");
        this.lastEnergy = nbt.getDouble("lastEnergy");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("scrap", this.scrap);
        nbt.setDouble("lastEnergy", this.lastEnergy);
        return nbt;
    }
    
    protected void onUnloaded() {
        if (this.world.isRemote && (this.audioSource != null || this.audioSourceScrap != null)) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
            this.audioSourceScrap = null;
        }
        super.onUnloaded();
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.redstone.hasRedstoneInput() || this.energy.getEnergy() <= 0.0) {
            this.setState(0);
            this.setActive(false);
        }
        else {
            if (this.scrap > 0) {
                final double bonus = Math.min(this.scrap, this.energy.getEnergy() - this.lastEnergy);
                if (bonus > 0.0) {
                    this.energy.forceAddEnergy(5.0 * bonus);
                    this.scrap -= (int)bonus;
                }
                this.setState(2);
            }
            else {
                this.setState(1);
            }
            this.setActive(true);
            if (this.scrap < 10000) {
                final MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
                if (recipe != null) {
                    this.amplifierSlot.consume(recipe);
                    this.scrap += recipe.getOutput();
                }
            }
            if (this.energy.getEnergy() >= this.energy.getCapacity()) {
                needsInvUpdate = this.attemptGeneration();
            }
            this.lastEnergy = this.energy.getEnergy();
            if (needsInvUpdate) {
                this.markDirty();
            }
        }
    }
    
    public boolean amplificationIsAvailable() {
        if (this.scrap > 0) {
            return true;
        }
        final MachineRecipeResult<? extends IRecipeInput, ? extends Integer, ? extends ItemStack> recipe = this.amplifierSlot.process();
        return recipe != null && (int)recipe.getOutput() > 0;
    }
    
    public boolean attemptGeneration() {
        if (this.outputSlot.add(ItemName.misc_resource.getItemStack(MiscResourceType.matter)) == 0) {
            this.energy.useEnergy(this.energy.getCapacity());
            return true;
        }
        return false;
    }
    
    private void setState(final int aState) {
        this.state = aState;
        if (this.prevState != this.state) {
            IC2.network.get(true).updateTileEntityField(this, "state");
        }
        this.prevState = this.state;
    }
    
    public void onNetworkUpdate(final String field) {
        if (field.equals("state") && this.prevState != this.state) {
            switch (this.state) {
                case 0: {
                    if (this.audioSource != null) {
                        this.audioSource.stop();
                    }
                    if (this.audioSourceScrap != null) {
                        this.audioSourceScrap.stop();
                        break;
                    }
                    break;
                }
                case 1: {
                    if (this.audioSource == null) {
                        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (this.audioSource != null) {
                        this.audioSource.play();
                    }
                    if (this.audioSourceScrap != null) {
                        this.audioSourceScrap.stop();
                        break;
                    }
                    break;
                }
                case 2: {
                    if (this.audioSource == null) {
                        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (this.audioSourceScrap == null) {
                        this.audioSourceScrap = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabScrapSolo.ogg", true, false, IC2.audioManager.getDefaultVolume());
                    }
                    if (this.audioSource != null) {
                        this.audioSource.play();
                    }
                    if (this.audioSourceScrap != null) {
                        this.audioSourceScrap.play();
                        break;
                    }
                    break;
                }
            }
            this.prevState = this.state;
        }
        super.onNetworkUpdate(field);
    }
    
    private GuiParser.GuiNode getXML() {
        final ResourceLocation loc = new ResourceLocation(this.teBlock.getIdentifier().getResourceDomain(), "guidef/" + this.teBlock.getName() + "_classic.xml");
        try {
            return GuiParser.parse(loc, this.teBlock.getTeClass());
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, this.getXML());
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, this.getXML());
    }
    
    public String getProgressAsString() {
        final int p = (int)Math.min(100.0 * this.energy.getFillRatio(), 100.0);
        return "" + p + "%";
    }
    
    public boolean getGuiState(final String name) {
        if ("scrap".equals(name)) {
            return this.scrap > 0;
        }
        if ("dev".equals(name)) {
            return Util.inDev();
        }
        return super.getGuiState(name);
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public boolean shouldExplode() {
        return true;
    }
    
    @Override
    public float getExplosionPower(final int tier, final float defaultPower) {
        return 15.0f;
    }
}
