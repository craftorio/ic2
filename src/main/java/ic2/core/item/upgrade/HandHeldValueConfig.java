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
import ic2.core.gui.TextBox;
import ic2.core.gui.TextLabel;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotHologramSlot;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HandHeldValueConfig extends HandHeldUpgradeOption
{
	protected final ComparisonType initialComparisonType;
	protected final String initialNormalBox;
	protected final String initialExtraBox;
	protected final ComparisonSettings initialNormalSetting;
	protected final ComparisonSettings initialExtraSetting;

	public HandHeldValueConfig(HandHeldAdvancedUpgrade upgradeGUI, String type)
	{
		super(upgradeGUI, type);
		UpgradeSettings settings = new UpgradeSettings(this.getNBT());
		this.initialComparisonType = settings.comparison;
		this.initialNormalBox = settings.mainBox;
		this.initialExtraBox = settings.extraBox;
		this.initialNormalSetting = settings.main;
		this.initialExtraSetting = settings.extra;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new HandHeldValueConfig.ContainerValueConfig(syncId);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new HandHeldValueConfig.ContainerValueConfig(syncId);
	}

	public class ContainerValueConfig extends ContainerHandHeldInventory<HandHeldValueConfig>
	{
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

		public ContainerValueConfig(int syncId)
		{
			super(Ic2ScreenHandlers.ADVANCED_UPGRADE_VALUE_CONFIG, syncId, HandHeldValueConfig.this);
			this.addPlayerInventorySlots(this.player.getInventory(), 166);

			for (byte slot = 0; slot < 9; slot++)
			{
				this.m_38897_(new SlotHologramSlot(HandHeldValueConfig.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldValueConfig.this.makeSaveCallback()));
			}
		}

		@Override
		public void removed(Player player)
		{
			CompoundTag nbt = HandHeldValueConfig.this.getNBT();
			nbt.putBoolean("active", this.comparisonType.enabled());
			ComparisonType saveType = this.comparisonType;
			switch (this.comparisonType)
			{
				case COMPARISON:
					if (this.normalBox.isEmpty())
					{
						saveType = ComparisonType.DIRECT;
					} else
					{
						nbt.m_128359_("normal", this.normalBox);
						nbt.putByte("normalComp", this.normalSetting.getForNBT());
					}
				case RANGE:
					if (this.normalBox.isEmpty())
					{
						if (this.extraBox.isEmpty())
						{
							saveType = ComparisonType.DIRECT;
						} else
						{
							saveType = ComparisonType.COMPARISON;
							nbt.m_128359_("normal", this.extraBox);
							nbt.putByte("normalComp", this.extraSetting.getForNBT());
						}
					} else
					{
						nbt.m_128359_("normal", this.normalBox);
						nbt.putByte("normalComp", this.normalSetting.getForNBT());
						if (this.extraBox.isEmpty())
						{
							saveType = ComparisonType.COMPARISON;
						} else
						{
							nbt.m_128359_("extra", this.extraBox);
							nbt.putByte("extraComp", this.extraSetting.getForNBT());
						}
					}
				default:
					nbt.putByte("type", saveType.getForNBT());
					super.removed(player);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class GuiValueConfig extends GuiDefaultBackground<HandHeldValueConfig.ContainerValueConfig>
	{
		public GuiValueConfig(HandHeldValueConfig.ContainerValueConfig container, Inventory playerInventory, Component title)
		{
			super(container, playerInventory, title);
			this.addElement(container.base.getBackButton(this, 10, 62));
			this.addElement(new VanillaButton(this, 10, 25, 75, 15, new EnumCycleHandler<ComparisonType>(ComparisonType.VALUES, container.comparisonType)
			{
				@Override
				public void onClick(MouseButton button)
				{
					super.onClick(button);
					((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).comparisonType = this.getCurrentValue();
					IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "comparisonType");
				}
			}).withText(new Supplier<String>()
			{
				public String get()
				{
					return Localization.translate(((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).comparisonType.name);
				}
			}).withTooltip(new Supplier<String>()
			{
				private final String name;

				{
					this.name = Localization.translate("ic2.upgrade.advancedGUI." + container.base.name);
				}

				public String get()
				{
					return Localization.translate(((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).comparisonType.name + ".desc", this.name);
				}
			}));
			IEnableHandler rangeEnabled = new IEnableHandler()
			{
				@Override
				public boolean isEnabled()
				{
					return ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).comparisonType == ComparisonType.RANGE;
				}
			};
			IEnableHandler filtersEnabled = new IEnableHandler()
			{
				@Override
				public boolean isEnabled()
				{
					return !((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).comparisonType.ignoreFilters();
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
					new EnumCycleHandler<ComparisonSettings>(ComparisonSettings.VALUES, container.normalSetting)
					{
						@Override
						public void onClick(MouseButton button)
						{
							super.onClick(button);
							((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalSetting = this.getCurrentValue();
							IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "normalSetting");
							switch ((ComparisonSettings) this.getCurrentValue())
							{
								case LESS:
								case LESS_OR_EQUAL:
									if (((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting != ComparisonSettings.LESS
										&& ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting != ComparisonSettings.LESS_OR_EQUAL)
									{
										((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.LESS;
										IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "extraSetting");
									}
									break;
								case GREATER:
								case GREATER_OR_EQUAL:
									if (((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting != ComparisonSettings.GREATER
										&& ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting
										!= ComparisonSettings.GREATER_OR_EQUAL)
									{
										((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.GREATER;
										IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "extraSetting");
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
					.withText(new Supplier<String>()
					{
						public String get()
						{
							return ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalSetting.symbol;
						}
					})
					.withTooltip(new Supplier<String>()
					{
						public String get()
						{
							return Localization.translate(((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalSetting.name);
						}
					})
			);
			this.addElement(
				new VanillaButton(
					this,
					105,
					43,
					17,
					15,
					new IClickHandler()
					{
						@Override
						public void onClick(MouseButton button)
						{
							if (button == MouseButton.left || button == MouseButton.right)
							{
								switch (((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalSetting)
								{
									case LESS:
									case LESS_OR_EQUAL:
										if (((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting == ComparisonSettings.LESS)
										{
											((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.LESS_OR_EQUAL;
										} else
										{
											((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.LESS;
										}
										break;
									case GREATER:
									case GREATER_OR_EQUAL:
										if (((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting == ComparisonSettings.GREATER)
										{
											((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.GREATER_OR_EQUAL;
										} else
										{
											((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting = ComparisonSettings.GREATER;
										}
										break;
									default:
										throw new IllegalStateException(
											"Unexpected other setting: " + ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalSetting
										);
								}

								IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "extraSetting");
							}
						}
					}
				)
					.withEnableHandler(rangeEnabled)
					.withText(new Supplier<String>()
					{
						public String get()
						{
							return ((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting.symbol;
						}
					})
					.withTooltip(new Supplier<String>()
					{
						public String get()
						{
							return Localization.translate(((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraSetting.name);
						}
					})
			);
			Predicate<String> numberOnly = new Predicate<String>()
			{
				public boolean apply(String input)
				{
					try
					{
						return Integer.parseInt(input) >= 0;
					} catch (NumberFormatException e)
					{
						return input.isEmpty();
					}
				}
			};
			final MoveableTextBox textBox = new MoveableTextBox(this, 40, 43, 25, 43, 30, 15, ((HandHeldValueConfig.ContainerValueConfig) this.menu).normalBox);
			this.addElement(textBox.withMoveHandler(rangeEnabled).withTextWatcher(new TextBox.ITextBoxWatcher()
			{
				@Override
				public void onChanged(String oldValue, String newValue)
				{
					((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).normalBox = newValue;
					IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "normalBox");
				}
			}).withTextValidator(numberOnly).withEnableHandler(filtersEnabled));
			this.addElement(
				new TextBox(this, 125, 43, 30, 15, ((HandHeldValueConfig.ContainerValueConfig) this.menu).extraBox)
					.withTextWatcher(new TextBox.ITextBoxWatcher()
					{
						@Override
						public void onChanged(String oldValue, String newValue)
						{
							((HandHeldValueConfig.ContainerValueConfig) GuiValueConfig.this.menu).extraBox = newValue;
							IC2.network.get(false).sendContainerField((ContainerBase<?>) GuiValueConfig.this.menu, "extraBox");
						}
					})
					.withTextValidator(numberOnly)
					.withEnableHandler(rangeEnabled)
			);
			this.addElement(
				TextLabel.create(this, 100, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + container.base.name), 4210752, false)
					.withEnableHandler(new IEnableHandler()
					{
						@Override
						public boolean isEnabled()
						{
							return textBox.isEnabled() && !textBox.isMoved();
						}
					})
			);
			this.addElement(
				TextLabel.create(this, 80, 47, TextProvider.ofTranslated("ic2.upgrade.advancedGUI." + container.base.name), 4210752, false)
					.withEnableHandler(new IEnableHandler()
					{
						@Override
						public boolean isEnabled()
						{
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
