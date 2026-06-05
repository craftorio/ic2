package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.gui.EnumCycleHandler;
import ic2.core.gui.GuiDefaultBackground;
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
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldValueConfig extends HandHeldUpgradeOption {
   protected final ComparisonType initialComparisonType;
   protected final String initialNormalBox;
   protected final String initialExtraBox;
   protected final ComparisonSettings initialNormalSetting;
   protected final ComparisonSettings initialExtraSetting;

   public HandHeldValueConfig(HandHeldAdvancedUpgrade upgradeGUI, String type) {
      super(upgradeGUI, type);
      Settings settings = new Settings(this.getNBT());
      this.initialComparisonType = settings.comparison;
      this.initialNormalBox = settings.mainBox;
      this.initialExtraBox = settings.extraBox;
      this.initialNormalSetting = settings.main;
      this.initialExtraSetting = settings.extra;
   }

   @Override
   public ContainerBase<?> getGuiContainer(EntityPlayer player) {
      return new HandHeldValueConfig.ContainerValueConfig();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new HandHeldValueConfig.GuiValueConfig();
   }

   public class ContainerValueConfig extends ContainerHandHeldInventory<HandHeldValueConfig> {
      @ClientModifiable
      protected ComparisonType comparisonType = HandHeldValueConfig.this.initialComparisonType;
      @ClientModifiable
      protected String normalBox = HandHeldValueConfig.this.initialNormalBox;
      @ClientModifiable
      protected String extraBox = HandHeldValueConfig.this.initialExtraBox;
      @ClientModifiable
      protected ComparisonSettings normalSetting = HandHeldValueConfig.this.initialNormalSetting;
      @ClientModifiable
      protected ComparisonSettings extraSetting = HandHeldValueConfig.this.initialExtraSetting;

      public ContainerValueConfig() {
         super(HandHeldValueConfig.this);
         this.addPlayerInventorySlots(HandHeldValueConfig.this.player, 166);

         for (byte slot = 0; slot < 9; slot++) {
            this.addSlotToContainer(new SlotHologramSlot(HandHeldValueConfig.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldValueConfig.this.makeSaveCallback()));
         }
      }

      @Override
      public void onContainerClosed(EntityPlayer player) {
         NBTTagCompound nbt = HandHeldValueConfig.this.getNBT();
         nbt.setBoolean("active", this.comparisonType.enabled());
         ComparisonType saveType = this.comparisonType;
         switch (this.comparisonType) {
            case COMPARISON:
               if (this.normalBox.isEmpty()) {
                  saveType = ComparisonType.DIRECT;
               } else {
                  nbt.setString("normal", this.normalBox);
                  nbt.setByte("normalComp", this.normalSetting.getForNBT());
               }
            case RANGE:
               if (this.normalBox.isEmpty()) {
                  if (this.extraBox.isEmpty()) {
                     saveType = ComparisonType.DIRECT;
                  } else {
                     saveType = ComparisonType.COMPARISON;
                     nbt.setString("normal", this.extraBox);
                     nbt.setByte("normalComp", this.extraSetting.getForNBT());
                  }
               } else {
                  nbt.setString("normal", this.normalBox);
                  nbt.setByte("normalComp", this.normalSetting.getForNBT());
                  if (this.extraBox.isEmpty()) {
                     saveType = ComparisonType.COMPARISON;
                  } else {
                     nbt.setString("extra", this.extraBox);
                     nbt.setByte("extraComp", this.extraSetting.getForNBT());
                  }
               }
            default:
               nbt.setByte("type", saveType.getForNBT());
               super.onContainerClosed(player);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public class GuiValueConfig extends GuiDefaultBackground<HandHeldValueConfig.ContainerValueConfig> {
      public GuiValueConfig() {
         super(HandHeldValueConfig.this.new ContainerValueConfig());
         this.addElement(HandHeldValueConfig.this.getBackButton(this, 10, 62));
         this.addElement(new VanillaButton(this, 10, 25, 75, 15, new EnumCycleHandler<ComparisonType>(ComparisonType.VALUES, this.container.comparisonType) {
            @Override
            public void onClick(MouseButton button) {
               super.onClick(button);
               GuiValueConfig.this.container.comparisonType = this.getCurrentValue();
               IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "comparisonType");
            }
         }).withText(new Supplier<String>() {
            public String get() {
               return Localization.translate(GuiValueConfig.this.container.comparisonType.name);
            }
         }).withTooltip(new Supplier<String>() {
            private final String name = Localization.translate("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName());

            public String get() {
               return Localization.translate(GuiValueConfig.this.container.comparisonType.name + ".desc", this.name);
            }
         }));
         IEnableHandler rangeEnabled = new IEnableHandler() {
            @Override
            public boolean isEnabled() {
               return GuiValueConfig.this.container.comparisonType == ComparisonType.RANGE;
            }
         };
         IEnableHandler filtersEnabled = new IEnableHandler() {
            @Override
            public boolean isEnabled() {
               return !GuiValueConfig.this.container.comparisonType.ignoreFilters();
            }
         };
         this.addElement(
            new MoveableButton(
                  this,
                  75,
                  43,
                  60,
                  43,
                  17,
                  15,
                  new EnumCycleHandler<ComparisonSettings>(ComparisonSettings.VALUES, this.container.normalSetting) {
                     @Override
                     public void onClick(MouseButton button) {
                        super.onClick(button);
                        GuiValueConfig.this.container.normalSetting = this.getCurrentValue();
                        IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "normalSetting");
                        switch ((ComparisonSettings)this.getCurrentValue()) {
                           case LESS:
                           case LESS_OR_EQUAL:
                              if (GuiValueConfig.this.container.extraSetting != ComparisonSettings.LESS
                                 && GuiValueConfig.this.container.extraSetting != ComparisonSettings.LESS_OR_EQUAL) {
                                 GuiValueConfig.this.container.extraSetting = ComparisonSettings.LESS;
                                 IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
                              }
                              break;
                           case GREATER:
                           case GREATER_OR_EQUAL:
                              if (GuiValueConfig.this.container.extraSetting != ComparisonSettings.GREATER
                                 && GuiValueConfig.this.container.extraSetting != ComparisonSettings.GREATER_OR_EQUAL) {
                                 GuiValueConfig.this.container.extraSetting = ComparisonSettings.GREATER;
                                 IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
                              }
                              break;
                           default:
                              throw new IllegalStateException("Unexpected other setting: " + this.getCurrentValue());
                        }
                     }
                  }
               )
               .withMoveHandler(rangeEnabled)
               .withEnableHandler(filtersEnabled)
               .withText(new Supplier<String>() {
                  public String get() {
                     return GuiValueConfig.this.container.normalSetting.symbol;
                  }
               })
               .withTooltip(new Supplier<String>() {
                  public String get() {
                     return Localization.translate(GuiValueConfig.this.container.normalSetting.name);
                  }
               })
         );
         this.addElement(new VanillaButton(this, 105, 43, 17, 15, new IClickHandler() {
            @Override
            public void onClick(MouseButton button) {
               if (button == MouseButton.left || button == MouseButton.right) {
                  switch (GuiValueConfig.this.container.normalSetting) {
                     case LESS:
                     case LESS_OR_EQUAL:
                        if (GuiValueConfig.this.container.extraSetting == ComparisonSettings.LESS) {
                           GuiValueConfig.this.container.extraSetting = ComparisonSettings.LESS_OR_EQUAL;
                        } else {
                           GuiValueConfig.this.container.extraSetting = ComparisonSettings.LESS;
                        }
                        break;
                     case GREATER:
                     case GREATER_OR_EQUAL:
                        if (GuiValueConfig.this.container.extraSetting == ComparisonSettings.GREATER) {
                           GuiValueConfig.this.container.extraSetting = ComparisonSettings.GREATER_OR_EQUAL;
                        } else {
                           GuiValueConfig.this.container.extraSetting = ComparisonSettings.GREATER;
                        }
                        break;
                     default:
                        throw new IllegalStateException("Unexpected other setting: " + GuiValueConfig.this.container.normalSetting);
                  }

                  IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraSetting");
               }
            }
         }).withEnableHandler(rangeEnabled).withText(new Supplier<String>() {
            public String get() {
               return GuiValueConfig.this.container.extraSetting.symbol;
            }
         }).withTooltip(new Supplier<String>() {
            public String get() {
               return Localization.translate(GuiValueConfig.this.container.extraSetting.name);
            }
         }));
         Predicate<String> numberOnly = new Predicate<String>() {
            public boolean apply(String input) {
               try {
                  return Integer.parseInt(input) >= 0;
               } catch (NumberFormatException e) {
                  return input.isEmpty();
               }
            }
         };
         final MoveableTextBox textBox = new MoveableTextBox(this, 40, 43, 25, 43, 30, 15, this.container.normalBox);
         this.addElement(textBox.withMoveHandler(rangeEnabled).withTextWatcher(new TextBox.ITextBoxWatcher() {
            @Override
            public void onChanged(String oldValue, String newValue) {
               GuiValueConfig.this.container.normalBox = newValue;
               IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "normalBox");
            }
         }).withTextValidator(numberOnly).withEnableHandler(filtersEnabled));
         this.addElement(new TextBox(this, 125, 43, 30, 15, this.container.extraBox).withTextWatcher(new TextBox.ITextBoxWatcher() {
            @Override
            public void onChanged(String oldValue, String newValue) {
               GuiValueConfig.this.container.extraBox = newValue;
               IC2.network.get(false).sendContainerField(GuiValueConfig.this.container, "extraBox");
            }
         }).withTextValidator(numberOnly).withEnableHandler(rangeEnabled));
         this.addElement(
            Text.create(this, 100, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false)
               .withEnableHandler(new IEnableHandler() {
                  @Override
                  public boolean isEnabled() {
                     return textBox.isEnabled() && !textBox.isMoved();
                  }
               })
         );
         this.addElement(
            Text.create(this, 80, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + HandHeldValueConfig.this.getName()), 4210752, false)
               .withEnableHandler(new IEnableHandler() {
                  @Override
                  public boolean isEnabled() {
                     return textBox.isEnabled() && textBox.isMoved();
                  }
               })
         );
         this.addElement(new SlotGrid(this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
         this.addElement(new SlotGrid(this, 7, 83, 9, 3, SlotGrid.SlotStyle.Normal));
         this.addElement(new SlotGrid(this, 7, 141, 9, 1, SlotGrid.SlotStyle.Normal));
      }
   }
}
