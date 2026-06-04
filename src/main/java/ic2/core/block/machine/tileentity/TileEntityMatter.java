// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.audio.PositionSpec;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiMatter;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.recipe.MatterAmplifierRecipeManager;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.invslot.InvSlot;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.audio.AudioSource;
import ic2.core.profile.NotClassic;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityMatter extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride
{
    private static final int DEFAULT_TIER;
    public int scrap;
    private double lastEnergy;
    private static final int StateIdle = 0;
    private static final int StateRunning = 1;
    private static final int StateRunningScrap = 2;
    private int state;
    private int prevState;
    public boolean redstonePowered;
    private AudioSource audioSource;
    private AudioSource audioSourceScrap;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotConsumableLiquid containerslot;
    @GuiSynced
    public final FluidTank fluidTank;
    protected final Redstone redstone;
    protected final Fluids fluids;
    
    public TileEntityMatter() {
        super(Math.round(1000000.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), TileEntityMatter.DEFAULT_TIER);
        this.scrap = 0;
        this.state = 0;
        this.prevState = 0;
        this.redstonePowered = false;
        this.amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>(this, "scrap", 1, Recipes.matterAmplifier) {
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
        this.containerslot = new InvSlotConsumableLiquidByList(this, "container", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill, new Fluid[] { FluidName.uu_matter.getInstance() });
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        (this.redstone = this.addComponent(new Redstone(this))).subscribe(new Redstone.IRedstoneChangeHandler() {
            @Override
            public void onRedstoneChange(final int newLevel) {
                TileEntityMatter.this.energy.setEnabled(newLevel == 0);
            }
        });
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 8000, Fluids.fluidPredicate(FluidName.uu_matter.getInstance()));
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
    
    public static void init() {
        Recipes.matterAmplifier = new MatterAmplifierRecipeManager();
        addAmplifier(ItemName.crafting.getItemStack(CraftingItemType.scrap), 1, 5000);
        addAmplifier(ItemName.crafting.getItemStack(CraftingItemType.scrap_box), 1, 45000);
    }
    
    public static void addAmplifier(final ItemStack input, final int amount, final int amplification) {
        addAmplifier(Recipes.inputFactory.forStack(input, amount), amplification);
    }
    
    public static void addAmplifier(final String input, final int amount, final int amplification) {
        addAmplifier(Recipes.inputFactory.forOreDict(input, amount), amplification);
    }
    
    public static void addAmplifier(final IRecipeInput input, final int amplification) {
        Recipes.matterAmplifier.addRecipe(input, amplification, null, false);
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
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.setUpgradestat();
        }
    }
    
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
            this.audioSourceScrap = null;
        }
        super.onUnloaded();
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.redstonePowered = false;
        boolean needsInvUpdate = false;
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
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
            needsInvUpdate |= this.containerslot.processFromTank((IFluidTank)this.fluidTank, this.outputSlot);
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
        if (this.fluidTank.getFluidAmount() + 1 > this.fluidTank.getCapacity()) {
            return false;
        }
        this.fluidTank.fillInternal(new FluidStack(FluidName.uu_matter.getInstance(), 1), true);
        this.energy.useEnergy(this.energy.getCapacity());
        return true;
    }
    
    public String getProgressAsString() {
        final int p = (int)Math.min(100.0 * this.energy.getFillRatio(), 100.0);
        return "" + p + "%";
    }
    
    @Override
    public ContainerBase<TileEntityMatter> getGuiContainer(final EntityPlayer player) {
        return new ContainerMatter(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiMatter(new ContainerMatter(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    private void setState(final int aState) {
        this.state = aState;
        if (this.prevState != this.state) {
            IC2.network.get(true).updateTileEntityField(this, "state");
        }
        this.prevState = this.state;
    }
    
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>();
        ret.add("state");
        ret.addAll(super.getNetworkedFields());
        return ret;
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
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setUpgradestat();
        }
    }
    
    public void setUpgradestat() {
        this.upgradeSlot.onChanged();
        this.energy.setSinkTier(applyModifier(TileEntityMatter.DEFAULT_TIER, this.upgradeSlot.extraTier, 1.0));
    }
    
    private static int applyModifier(final int base, final int extra, final double multiplier) {
        final double ret = (double)Math.round((base + (double)extra) * multiplier);
        return (ret > 2.147483647E9) ? Integer.MAX_VALUE : ((int)ret);
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
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
    }
    
    @Override
    public boolean shouldExplode() {
        return true;
    }
    
    @Override
    public float getExplosionPower(final int tier, final float defaultPower) {
        return 15.0f;
    }
    
    static {
        DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/matterFabricatorTier");
    }
}
