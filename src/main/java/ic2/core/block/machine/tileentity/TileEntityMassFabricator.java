// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import ic2.core.util.Util;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.audio.PositionSpec;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.Recipes;
import ic2.api.energy.EnergyNet;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.IC2;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.audio.AudioSource;
import ic2.core.network.GuiSynced;
import ic2.core.ref.TeBlock;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@TeBlock.Delegated(current = TileEntityMassFabricator.class, old = TileEntityClassicMassFabricator.class)
public class TileEntityMassFabricator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride
{
    @GuiSynced
    public int scrap;
    @GuiSynced
    public int consumedScrap;
    protected double maxScrapConsumption;
    public static final int DEFAULT_TIER;
    private static final int REQUIRED_SCRAP;
    private static final int SCRAP_FACTOR = 10;
    private AudioSource audioSource;
    private AudioSource audioSourceScrap;
    private byte scrapCounter;
    public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotUpgrade upgradeSlot;
    protected final Redstone redstone;
    
    public static Class<? extends TileEntityElectricMachine> delegate() {
        return (Class<? extends TileEntityElectricMachine>)(IC2.version.isClassic() ? TileEntityClassicMassFabricator.class : TileEntityMassFabricator.class);
    }
    
    public TileEntityMassFabricator() {
        super(Math.round(1000000.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), TileEntityMassFabricator.DEFAULT_TIER, false);
        this.scrap = 0;
        this.consumedScrap = 0;
        this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(TileEntityMassFabricator.DEFAULT_TIER);
        this.scrapCounter = 0;
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
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
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
        this.consumedScrap = nbt.getInteger("consumedScrap");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("scrap", this.scrap);
        nbt.setInteger("consumedScrap", this.consumedScrap);
        return nbt;
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.updateUpgrades();
        }
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.getWorld().isRemote) {
            this.updateUpgrades();
        }
    }
    
    public void updateUpgrades() {
        this.upgradeSlot.onChanged();
        final int tier = this.upgradeSlot.getTier(TileEntityMassFabricator.DEFAULT_TIER);
        this.energy.setSinkTier(tier);
        this.dischargeSlot.setTier(tier);
        this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(tier);
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
        boolean needsInvUpdate = this.upgradeSlot.tickNoMark();
        if (this.redstone.hasRedstoneInput() || this.energy.getEnergy() <= 0.0) {
            this.setActive(false);
        }
        else {
            if (this.scrap < 100000) {
                final MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
                if (recipe != null) {
                    this.amplifierSlot.consume(recipe);
                    this.scrap += recipe.getOutput() * 10;
                }
            }
            assert this.scrap >= 0;
            final double scrapConversion = Math.min(Math.min(this.scrap, this.energy.getEnergy() - this.consumedScrap), this.maxScrapConsumption);
            assert scrapConversion >= 0.0;
            boolean newActivity = false;
            if (scrapConversion > 0.0) {
                this.consumedScrap += (int)scrapConversion;
                this.scrap -= (int)scrapConversion;
                newActivity = true;
                if (this.energy.getEnergy() >= this.energy.getCapacity() && this.consumedScrap >= TileEntityMassFabricator.REQUIRED_SCRAP) {
                    if (this.outputSlot.canAdd(ItemName.misc_resource.getItemStack(MiscResourceType.matter))) {
                        this.outputSlot.add(ItemName.misc_resource.getItemStack(MiscResourceType.matter));
                        this.energy.useEnergy(this.energy.getCapacity());
                        this.consumedScrap = 0;
                        needsInvUpdate = true;
                    }
                    else {
                        newActivity = false;
                    }
                }
            }
            this.setActive(newActivity);
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive() && ++this.scrapCounter > 40) {
            this.scrapCounter = 0;
            if (this.audioSourceScrap == null) {
                this.audioSourceScrap = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabScrapSolo.ogg", false, false, IC2.audioManager.getDefaultVolume());
            }
            else {
                this.audioSourceScrap.stop();
            }
            if (this.audioSourceScrap != null) {
                this.audioSourceScrap.play();
            }
        }
    }
    
    public void onNetworkUpdate(final String field) {
        if ("active".equals(field)) {
            if (this.getActive()) {
                if (this.audioSource == null) {
                    this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
                }
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            }
            else {
                this.scrapCounter = 0;
                if (this.audioSource != null) {
                    this.audioSource.stop();
                }
                if (this.audioSourceScrap != null) {
                    this.audioSourceScrap.stop();
                }
            }
        }
        super.onNetworkUpdate(field);
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    public int getScrap() {
        return this.scrap / 10;
    }
    
    public int getScrapProgress() {
        return (int)Math.min(100.0f * (this.consumedScrap / (float)TileEntityMassFabricator.REQUIRED_SCRAP), 100.0f);
    }
    
    public int getEnergyProgress() {
        return (int)Math.min(100.0 * this.energy.getFillRatio(), 100.0);
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
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add("You probably want the " + Localization.translate(this.getBlockType().getUnlocalizedName() + '.' + TeBlock.matter_generator.getName()));
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
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
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
        DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/massFabricatorTier");
        REQUIRED_SCRAP = Util.roundToNegInf(1000000.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor"));
    }
}
