// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import ic2.core.gui.SlotGrid;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.gui.TextBox;
import com.google.common.base.Predicate;
import ic2.core.gui.IEnableHandler;
import ic2.core.init.Localization;
import com.google.common.base.Supplier;
import ic2.core.gui.IClickHandler;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.gui.MouseButton;
import ic2.core.gui.EnumCycleHandler;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.GuiElement;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiDefaultBackground;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotHologramSlot;
import ic2.api.network.ClientModifiable;
import ic2.core.item.ContainerHandHeldInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

public class HandHeldValueConfig extends HandHeldUpgradeOption
{
    protected final ComparisonType initialComparisonType;
    protected final String initialNormalBox;
    protected final String initialExtraBox;
    protected final ComparisonSettings initialNormalSetting;
    protected final ComparisonSettings initialExtraSetting;
    
    public HandHeldValueConfig(final HandHeldAdvancedUpgrade upgradeGUI, final String type) {
        super(upgradeGUI, type);
        final Settings settings = new Settings(this.getNBT());
        this.initialComparisonType = settings.comparison;
        this.initialNormalBox = settings.mainBox;
        this.initialExtraBox = settings.extraBox;
        this.initialNormalSetting = settings.main;
        this.initialExtraSetting = settings.extra;
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerValueConfig();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiValueConfig();
    }
    
    public class ContainerValueConfig extends ContainerHandHeldInventory<HandHeldValueConfig>
    {
        @ClientModifiable
        protected ComparisonType comparisonType;
        @ClientModifiable
        protected String normalBox;
        @ClientModifiable
        protected String extraBox;
        @ClientModifiable
        protected ComparisonSettings normalSetting;
        @ClientModifiable
        protected ComparisonSettings extraSetting;
        
        public ContainerValueConfig() {
            super(HandHeldValueConfig.this);
            this.comparisonType = HandHeldValueConfig.this.initialComparisonType;
            this.normalBox = HandHeldValueConfig.this.initialNormalBox;
            this.extraBox = HandHeldValueConfig.this.initialExtraBox;
            this.normalSetting = HandHeldValueConfig.this.initialNormalSetting;
            this.extraSetting = HandHeldValueConfig.this.initialExtraSetting;
            this.addPlayerInventorySlots(HandHeldValueConfig.this.player, 166);
            for (byte slot = 0; slot < 9; ++slot) {
                this.addSlotToContainer((Slot)new SlotHologramSlot(HandHeldValueConfig.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldValueConfig.this.makeSaveCallback()));
            }
        }
        
        @Override
        public void onContainerClosed(final EntityPlayer player) {
            final NBTTagCompound nbt = HandHeldValueConfig.this.getNBT();
            nbt.setBoolean("active", this.comparisonType.enabled());
            ComparisonType saveType = this.comparisonType;
            switch (this.comparisonType) {
                case COMPARISON:
                    Label_0104: {
                        if (this.normalBox.isEmpty()) {
                            saveType = ComparisonType.DIRECT;
                            break Label_0104;
                        }
                        nbt.setString("normal", this.normalBox);
                        nbt.setByte("normalComp", this.normalSetting.getForNBT());
                        break Label_0104;
                    }
                case RANGE: {
                    if (this.normalBox.isEmpty()) {
                        if (this.extraBox.isEmpty()) {
                            saveType = ComparisonType.DIRECT;
                            break;
                        }
                        saveType = ComparisonType.COMPARISON;
                        nbt.setString("normal", this.extraBox);
                        nbt.setByte("normalComp", this.extraSetting.getForNBT());
                        break;
                    }
                    else {
                        nbt.setString("normal", this.normalBox);
                        nbt.setByte("normalComp", this.normalSetting.getForNBT());
                        if (this.extraBox.isEmpty()) {
                            saveType = ComparisonType.COMPARISON;
                            break;
                        }
                        nbt.setString("extra", this.extraBox);
                        nbt.setByte("extraComp", this.extraSetting.getForNBT());
                        break;
                    }
                    break;
                }
            }
            nbt.setByte("type", saveType.getForNBT());
            super.onContainerClosed(player);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public class GuiValueConfig extends GuiDefaultBackground<ContainerValueConfig>
    {
        public GuiValueConfig() {
            super(new ContainerValueConfig());
            this.addElement(HandHeldValueConfig.this.getBackButton(this, 10, 62));
            this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 10, 25, 75, 15, new EnumCycleHandler<ComparisonType>(ComparisonType.VALUES, ((ContainerValueConfig)this.container).comparisonType) {
                @Override
                public void onClick(final MouseButton button) {
                    super.onClick(button);
                    ((ContainerValueConfig)GuiValueConfig.this.container).comparisonType = this.getCurrentValue();
                    IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "comparisonType");
                }
            }).withText((Supplier<String>)new Supplier<String>() {
                public String get() {
                    return Localization.translate(((ContainerValueConfig)GuiValueConfig.this.container).comparisonType.name);
                }
            })).withTooltip((Supplier<String>)new Supplier<String>() {
                private final String name = Localization.translate("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName());
                
                public String get() {
                    return Localization.translate(((ContainerValueConfig)GuiValueConfig.this.container).comparisonType.name + ".desc", this.name);
                }
            }));
            final IEnableHandler rangeEnabled = new IEnableHandler() {
                @Override
                public boolean isEnabled() {
                    return ((ContainerValueConfig)GuiValueConfig.this.container).comparisonType == ComparisonType.RANGE;
                }
            };
            final IEnableHandler filtersEnabled = new IEnableHandler() {
                @Override
                public boolean isEnabled() {
                    return !((ContainerValueConfig)GuiValueConfig.this.container).comparisonType.ignoreFilters();
                }
            };
            this.addElement(((GuiElement<GuiElement<?>>)new MoveableButton(this, 75, 43, 60, 43, 17, 15, new EnumCycleHandler<ComparisonSettings>(ComparisonSettings.VALUES, ((ContainerValueConfig)this.container).normalSetting) {
                @Override
                public void onClick(final MouseButton button) {
                    super.onClick(button);
                    ((ContainerValueConfig)GuiValueConfig.this.container).normalSetting = this.getCurrentValue();
                    IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "normalSetting");
                    switch (this.getCurrentValue()) {
                        case LESS:
                        case LESS_OR_EQUAL: {
                            if (((ContainerValueConfig)GuiValueConfig.this.container).extraSetting != ComparisonSettings.LESS && ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting != ComparisonSettings.LESS_OR_EQUAL) {
                                ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS;
                                IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
                                break;
                            }
                            break;
                        }
                        case GREATER:
                        case GREATER_OR_EQUAL: {
                            if (((ContainerValueConfig)GuiValueConfig.this.container).extraSetting != ComparisonSettings.GREATER && ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting != ComparisonSettings.GREATER_OR_EQUAL) {
                                ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER;
                                IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
                                break;
                            }
                            break;
                        }
                        default: {
                            throw new IllegalStateException("Unexpected other setting: " + ((EnumCycleHandler<Object>)this).getCurrentValue());
                        }
                    }
                }
            }).withMoveHandler(rangeEnabled).withEnableHandler(filtersEnabled).withText((Supplier<String>)new Supplier<String>() {
                public String get() {
                    return ((ContainerValueConfig)GuiValueConfig.this.container).normalSetting.symbol;
                }
            })).withTooltip((Supplier<String>)new Supplier<String>() {
                public String get() {
                    return Localization.translate(((ContainerValueConfig)GuiValueConfig.this.container).normalSetting.name);
                }
            }));
            this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 105, 43, 17, 15, new IClickHandler() {
                @Override
                public void onClick(final MouseButton button) {
                    if (button == MouseButton.left || button == MouseButton.right) {
                        switch (((ContainerValueConfig)GuiValueConfig.this.container).normalSetting) {
                            case LESS:
                            case LESS_OR_EQUAL: {
                                if (((ContainerValueConfig)GuiValueConfig.this.container).extraSetting == ComparisonSettings.LESS) {
                                    ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS_OR_EQUAL;
                                    break;
                                }
                                ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS;
                                break;
                            }
                            case GREATER:
                            case GREATER_OR_EQUAL: {
                                if (((ContainerValueConfig)GuiValueConfig.this.container).extraSetting == ComparisonSettings.GREATER) {
                                    ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER_OR_EQUAL;
                                    break;
                                }
                                ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER;
                                break;
                            }
                            default: {
                                throw new IllegalStateException("Unexpected other setting: " + ((ContainerValueConfig)GuiValueConfig.this.container).normalSetting);
                            }
                        }
                        IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
                    }
                }
            }).withEnableHandler(rangeEnabled).withText((Supplier<String>)new Supplier<String>() {
                public String get() {
                    return ((ContainerValueConfig)GuiValueConfig.this.container).extraSetting.symbol;
                }
            })).withTooltip((Supplier<String>)new Supplier<String>() {
                public String get() {
                    return Localization.translate(((ContainerValueConfig)GuiValueConfig.this.container).extraSetting.name);
                }
            }));
            final Predicate<String> numberOnly = (Predicate<String>)new Predicate<String>() {
                public boolean apply(final String input) {
                    try {
                        return Integer.parseInt(input) >= 0;
                    }
                    catch (final NumberFormatException e) {
                        return input.isEmpty();
                    }
                }
            };
            final MoveableTextBox textBox = new MoveableTextBox(this, 40, 43, 25, 43, 30, 15, ((ContainerValueConfig)this.container).normalBox);
            this.addElement(((GuiElement<GuiElement<?>>)textBox.withMoveHandler(rangeEnabled).withTextWatcher(new TextBox.ITextBoxWatcher() {
                @Override
                public void onChanged(final String oldValue, final String newValue) {
                    ((ContainerValueConfig)GuiValueConfig.this.container).normalBox = newValue;
                    IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "normalBox");
                }
            }).withTextValidator(numberOnly)).withEnableHandler(filtersEnabled));
            this.addElement(((GuiElement<GuiElement<?>>)new TextBox(this, 125, 43, 30, 15, ((ContainerValueConfig)this.container).extraBox).withTextWatcher(new TextBox.ITextBoxWatcher() {
                @Override
                public void onChanged(final String oldValue, final String newValue) {
                    ((ContainerValueConfig)GuiValueConfig.this.container).extraBox = newValue;
                    IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraBox");
                }
            }).withTextValidator(numberOnly)).withEnableHandler(rangeEnabled));
            this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 100, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false)).withEnableHandler(new IEnableHandler() {
                @Override
                public boolean isEnabled() {
                    return textBox.isEnabled() && !textBox.isMoved();
                }
            }));
            this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 80, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false)).withEnableHandler(new IEnableHandler() {
                @Override
                public boolean isEnabled() {
                    return textBox.isEnabled() && textBox.isMoved();
                }
            }));
            this.addElement(new SlotGrid(this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
            this.addElement(new SlotGrid(this, 7, 83, 9, 3, SlotGrid.SlotStyle.Normal));
            this.addElement(new SlotGrid(this, 7, 141, 9, 1, SlotGrid.SlotStyle.Normal));
        }
    }
}
