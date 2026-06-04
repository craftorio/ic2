package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.gui.EnumCycleHandler;
import ic2.core.gui.GuiDefaultBackground;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Text;
import ic2.core.gui.TextBox;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.network.NetworkManager;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldValueConfig extends HandHeldUpgradeOption {
  protected final ComparisonType initialComparisonType;
  
  protected final String initialNormalBox;
  
  protected final String initialExtraBox;
  
  protected final ComparisonSettings initialNormalSetting;
  
  protected final ComparisonSettings initialExtraSetting;
  
  public class ContainerValueConfig extends ContainerHandHeldInventory<HandHeldValueConfig> {
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
      addPlayerInventorySlots(HandHeldValueConfig.this.player, 166);
      for (byte slot = 0; slot < 9; slot = (byte)(slot + 1))
        addSlotToContainer((Slot)new SlotHologramSlot(HandHeldValueConfig.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldValueConfig.this.makeSaveCallback())); 
    }
    
    public void onContainerClosed(EntityPlayer player) {
      NBTTagCompound nbt = HandHeldValueConfig.this.getNBT();
      nbt.setBoolean("active", this.comparisonType.enabled());
      ComparisonType saveType = this.comparisonType;
      switch (this.comparisonType) {
        case LESS:
          if (this.normalBox.isEmpty()) {
            saveType = ComparisonType.DIRECT;
          } else {
            nbt.setString("normal", this.normalBox);
            nbt.setByte("normalComp", this.normalSetting.getForNBT());
          } 
        case LESS_OR_EQUAL:
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
      nbt.setByte("type", saveType.getForNBT());
      super.onContainerClosed(player);
    }
  }
  
  @SideOnly(Side.CLIENT)
  public class GuiValueConfig extends GuiDefaultBackground<ContainerValueConfig> {
    public GuiValueConfig() {
      super((ContainerBase)new HandHeldValueConfig.ContainerValueConfig(HandHeldValueConfig.this));
      addElement((GuiElement)HandHeldValueConfig.this.getBackButton((GuiIC2<?>)this, 10, 62));
      addElement(((VanillaButton)(new VanillaButton((GuiIC2)this, 10, 25, 75, 15, (IClickHandler)new EnumCycleHandler<ComparisonType>(ComparisonType.VALUES, ((HandHeldValueConfig.ContainerValueConfig)this.container).comparisonType) {
              public void onClick(MouseButton button) {
                super.onClick(button);
                ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).comparisonType = (ComparisonType)getCurrentValue();
                ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "comparisonType");
              }
            })).withText(new Supplier<String>() {
              public String get() {
                return Localization.translate(((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).comparisonType.name);
              }
            })).withTooltip(new Supplier<String>() {
              private final String name = Localization.translate("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName());
              
              public String get() {
                return Localization.translate(((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).comparisonType.name + ".desc", new Object[] { this.name });
              }
            }));
      IEnableHandler rangeEnabled = new IEnableHandler() {
          public boolean isEnabled() {
            return (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).comparisonType == ComparisonType.RANGE);
          }
        };
      IEnableHandler filtersEnabled = new IEnableHandler() {
          public boolean isEnabled() {
            return !((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).comparisonType.ignoreFilters();
          }
        };
      addElement(((VanillaButton)((VanillaButton)(new MoveableButton((GuiIC2<?>)this, 75, 43, 60, 43, 17, 15, (IClickHandler)new EnumCycleHandler<ComparisonSettings>(ComparisonSettings.VALUES, ((HandHeldValueConfig.ContainerValueConfig)this.container).normalSetting) {
              public void onClick(MouseButton button) {
                super.onClick(button);
                ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalSetting = (ComparisonSettings)getCurrentValue();
                ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "normalSetting");
                switch ((ComparisonSettings)getCurrentValue()) {
                  case LESS:
                  case LESS_OR_EQUAL:
                    if (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting != ComparisonSettings.LESS && ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting != ComparisonSettings.LESS_OR_EQUAL) {
                      ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS;
                      ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "extraSetting");
                    } 
                    return;
                  case GREATER:
                  case GREATER_OR_EQUAL:
                    if (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting != ComparisonSettings.GREATER && ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting != ComparisonSettings.GREATER_OR_EQUAL) {
                      ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER;
                      ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "extraSetting");
                    } 
                    return;
                } 
                throw new IllegalStateException("Unexpected other setting: " + getCurrentValue());
              }
            })).withMoveHandler(rangeEnabled).withEnableHandler(filtersEnabled)).withText(new Supplier<String>() {
              public String get() {
                return ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalSetting.symbol;
              }
            })).withTooltip(new Supplier<String>() {
              public String get() {
                return Localization.translate(((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalSetting.name);
              }
            }));
      addElement(((VanillaButton)((VanillaButton)(new VanillaButton((GuiIC2)this, 105, 43, 17, 15, new IClickHandler() {
              public void onClick(MouseButton button) {
                if (button == MouseButton.left || button == MouseButton.right) {
                  switch (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalSetting) {
                    case LESS:
                    case LESS_OR_EQUAL:
                      if (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting == ComparisonSettings.LESS) {
                        ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS_OR_EQUAL;
                        break;
                      } 
                      ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.LESS;
                      break;
                    case GREATER:
                    case GREATER_OR_EQUAL:
                      if (((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting == ComparisonSettings.GREATER) {
                        ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER_OR_EQUAL;
                        break;
                      } 
                      ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting = ComparisonSettings.GREATER;
                      break;
                    default:
                      throw new IllegalStateException("Unexpected other setting: " + ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalSetting);
                  } 
                  ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "extraSetting");
                } 
              }
            })).withEnableHandler(rangeEnabled)).withText(new Supplier<String>() {
              public String get() {
                return ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting.symbol;
              }
            })).withTooltip(new Supplier<String>() {
              public String get() {
                return Localization.translate(((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraSetting.name);
              }
            }));
      Predicate<String> numberOnly = new Predicate<String>() {
          public boolean apply(String input) {
            try {
              return (Integer.parseInt(input) >= 0);
            } catch (NumberFormatException e) {
              return input.isEmpty();
            } 
          }
        };
      final MoveableTextBox textBox = new MoveableTextBox((GuiIC2<?>)this, 40, 43, 25, 43, 30, 15, ((HandHeldValueConfig.ContainerValueConfig)this.container).normalBox);
      addElement(textBox.withMoveHandler(rangeEnabled).withTextWatcher(new TextBox.ITextBoxWatcher() {
              public void onChanged(String oldValue, String newValue) {
                ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).normalBox = newValue;
                ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "normalBox");
              }
            }).withTextValidator(numberOnly).withEnableHandler(filtersEnabled));
      addElement((new TextBox((GuiIC2)this, 125, 43, 30, 15, ((HandHeldValueConfig.ContainerValueConfig)this.container).extraBox)).withTextWatcher(new TextBox.ITextBoxWatcher() {
              public void onChanged(String oldValue, String newValue) {
                ((HandHeldValueConfig.ContainerValueConfig)HandHeldValueConfig.GuiValueConfig.this.container).extraBox = newValue;
                ((NetworkManager)IC2.network.get(false)).sendContainerField(HandHeldValueConfig.GuiValueConfig.this.container, "extraBox");
              }
            }).withTextValidator(numberOnly).withEnableHandler(rangeEnabled));
      addElement(Text.create((GuiIC2)this, 100, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false).withEnableHandler(new IEnableHandler() {
              public boolean isEnabled() {
                return (textBox.isEnabled() && !textBox.isMoved());
              }
            }));
      addElement(Text.create((GuiIC2)this, 80, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false).withEnableHandler(new IEnableHandler() {
              public boolean isEnabled() {
                return (textBox.isEnabled() && textBox.isMoved());
              }
            }));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 83, 9, 3, SlotGrid.SlotStyle.Normal));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 141, 9, 1, SlotGrid.SlotStyle.Normal));
    }
  }
  
  public HandHeldValueConfig(HandHeldAdvancedUpgrade upgradeGUI, String type) {
    super(upgradeGUI, type);
    Settings settings = new Settings(getNBT());
    this.initialComparisonType = settings.comparison;
    this.initialNormalBox = settings.mainBox;
    this.initialExtraBox = settings.extraBox;
    this.initialNormalSetting = settings.main;
    this.initialExtraSetting = settings.extra;
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerValueConfig();
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiValueConfig();
  }
}
