package ic2.core.gui;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

public class Ic2GuiFactory implements IModGuiFactory
{
	public void initialize(Minecraft mc)
	{
	}

	@Override
	public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return Collections.emptySet();
	}

	@Override
	public boolean hasConfigGui()
	{
		return true;
	}

	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new Ic2GuiFactory.IC2ConfigGuiScreen(parentScreen);
	}

	public static class IC2ConfigGuiScreen extends GuiConfig
	{
		static final int LANG_KEY_LENGTH = "ic2.config.".length();
		static final Joiner COMMA_JOINER = Joiner.on(", ");
		static final Pattern ITEM_PATTERN = Pattern.compile("^[A-Za-z0-9_]+:[A-Za-z0-9_]+(#[A-Za-z0-9_]+|(@(\\d+|\\*)))?$");
		private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", 2);
		private static final Pattern IS_INT = Pattern.compile("\\d");
		private static final Pattern IS_DOUBLE = Pattern.compile("\\d\\.\\d");

		public IC2ConfigGuiScreen(GuiScreen parent)
		{
			super(parent, sinkCategoryLevel(MainConfig.get(), "."), "ic2", false, false, "IC2 Configuration");
		}

		private static List<IConfigElement> sinkCategoryLevel(Config config, String parentName)
		{
			List<IConfigElement> list = new ArrayList<>(config.getNumberOfSections() + config.getNumberOfConfigs());
			if (config.hasChildSection())
			{
				Iterator<Config> configCategories = config.sectionIterator();

				while (configCategories.hasNext())
				{
					Config category = configCategories.next();
					if ("predefined".equals(category.name) && ".balance.uu-values.".equals(parentName))
					{
						list.add(new Ic2GuiFactory.IC2ConfigGuiScreen.UUListElement());
					} else
					{
						list.add(
							new DummyConfigElement.DummyCategoryElement(
								category.name, "ic2.config.sub." + category.name, sinkCategoryLevel(category, parentName + category.name + '.')
							)
						);
					}
				}

				if (!config.isEmptySection())
				{
					getConfigs(list, config.valueIterator(), parentName);
				}
			} else
			{
				getConfigs(list, config.valueIterator(), parentName);
			}

			return list;
		}

		private static void getConfigs(List<IConfigElement> list, Iterator<Config.Value> configs, String parentName)
		{
			while (configs.hasNext())
			{
				Config.Value conf = configs.next();
				Config.Value defaultConf = MainConfig.getDefault(parentName.substring(1).replace('.', '/') + conf.name);
				if (defaultConf != null)
				{
					if (!defaultConf.value.isEmpty() && !defaultConf.value.contains(",") && !defaultConf.comment.contains("comma"))
					{
						ConfigGuiType type;
						if (IS_DOUBLE.matcher(conf.value).matches())
						{
							type = ConfigGuiType.DOUBLE;
						} else if (IS_INT.matcher(conf.value).matches())
						{
							type = ConfigGuiType.INTEGER;
						} else if (IS_BOOLEAN.matcher(conf.value).matches())
						{
							type = ConfigGuiType.BOOLEAN;
						} else
						{
							type = ConfigGuiType.STRING;
						}

						list.add(
							new Ic2GuiFactory.IC2ConfigGuiScreen.ConfigElement(conf.name, conf.value, defaultConf.value, type, "ic2.config" + parentName + conf.name)
						);
					} else
					{
						list.add(
							new Ic2GuiFactory.IC2ConfigGuiScreen.ListElement(
								conf.name, conf.value, defaultConf.value, "ic2.config" + parentName + conf.name, ITEM_PATTERN
							)
								.setArrayEntryClass(Ic2GuiFactory.IC2ConfigGuiScreen.ItemEntry.class)
						);
					}
				}
			}
		}

		public void onGuiClosed()
		{
			for (IConfigElement config : this.configElements)
			{
				this.saveConfig(config);
			}

			MainConfig.save();
			super.onGuiClosed();
		}

		private void saveConfig(IConfigElement config)
		{
			if (config.getChildElements() != null)
			{
				for (IConfigElement subConfig : config.getChildElements())
				{
					this.saveConfig(subConfig);
				}
			}

			if (config.isProperty())
			{
				if (config.isList())
				{
					assert config instanceof Ic2GuiFactory.IC2ConfigGuiScreen.ListElement : "Unexpected class type: " + config.getClass();
					((Ic2GuiFactory.IC2ConfigGuiScreen.ListElement) config).save();
				} else
				{
					assert config.getClass() == Ic2GuiFactory.IC2ConfigGuiScreen.ConfigElement.class : "Unexpected class type: " + config.getClass();
					if (!Objects.equals(config.get(), ((Ic2GuiFactory.IC2ConfigGuiScreen.ConfigElement) config).previous))
					{
						MainConfig.get().set(config.getLanguageKey().substring(LANG_KEY_LENGTH).replace('.', '/'), config.get());
					}
				}
			}
		}

		private static class ConfigElement extends DummyConfigElement
		{
			Object previous;

			ConfigElement(String name, Object value, Object defaultValue, ConfigGuiType type, String langKey)
			{
				super(name, defaultValue, type, langKey);
				this.previous = this.value = value;
			}

			@Override
			public void set(Object value)
			{
				this.previous = this.value;
				this.value = value;
			}

			@Override
			public void setToDefault()
			{
				this.previous = this.value;
				super.setToDefault();
			}

			@Override
			public String toString()
			{
				return "ConfigElement<" + this.name + '>';
			}
		}

		public static class ItemEntry extends GuiEditArrayEntries.StringEntry
		{
			private static final Field ENABLED = ReflectionUtil.getField(GuiEditArray.class, boolean.class);
			private static final Method TOOLTIP = ReflectionUtil.getMethod(
				GuiScreen.class, new String[] { "renderToolTip", "renderToolTip", "a" }, ItemStack.class, int.class, int.class
			);
			protected ItemStack stack;
			protected int stackX;
			protected int stackY;

			public ItemEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value)
			{
				super(owningScreen, owningEntryList, configElement, value);
				assert this.isValidated;
				if (this.isValidValue)
				{
					this.updateStack();
				}
			}

			public String getValue()
			{
				return (String) super.getValue();
			}

			protected String getStack()
			{
				return this.getValue();
			}

			protected void updateStack()
			{
				try
				{
					this.stack = ConfigUtil.asStack(this.getStack());
					this.isValidValue = !StackUtil.isEmpty(this.stack);
				} catch (ParseException e)
				{
					this.isValidValue = false;
				}
			}

			protected boolean isEnabled()
			{
				try
				{
					return ENABLED.getBoolean(this.owningScreen);
				} catch (Exception e)
				{
					throw new RuntimeException("Error checking owningScreen enabled!", e);
				}
			}

			@Override
			public void keyTyped(char eventChar, int eventKey)
			{
				super.keyTyped(eventChar, eventKey);
				if (this.isValidValue && this.isEnabled())
				{
					this.updateStack();
				}
			}

			public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial)
			{
				if (this.isValidValue)
				{
					this.isValidated = false;
				}

				super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
				this.isValidated = true;
				assert this.getValue() != null;
				if (this.isValidValue)
				{
					RenderHelper.enableGUIStandardItemLighting();
					this.owningEntryList
						.getMC()
						.getRenderItem()
						.renderItemIntoGUI(this.stack, this.stackX = listWidth / 4 - 16 - 1, this.stackY = y + slotHeight / 2 - 8);
					RenderHelper.disableStandardItemLighting();
				}
			}

			@Override
			public void drawToolTip(int var1, int var2)
			{
				// $VF: Couldn't be decompiled
				// Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
				// java.lang.IllegalStateException: Could not find destination nodes for stat id {Block}:13 from source 30
				//   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.setEdges(FlattenStatementsHelper.java:563)
				//   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.buildDirectGraph(FlattenStatementsHelper.java:50)
				//   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.<init>(VarDefinitionHelper.java:151)
				//   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.<init>(VarDefinitionHelper.java:52)
				//   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.setVarDefinitions(VarProcessor.java:52)
				//   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:458)
				//
				// Bytecode:
				// 00: aload 0
				// 01: iload 1
				// 02: iload 2
				// 03: invokespecial net/minecraftforge/fml/client/config/GuiEditArrayEntries$StringEntry.drawToolTip (II)V
				// 06: aload 0
				// 07: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stack Lnet/minecraft/item/ItemStack;
				// 0a: invokestatic ic2/core/util/StackUtil.isEmpty (Lnet/minecraft/item/ItemStack;)Z
				// 0d: ifne 87
				// 10: aload 0
				// 11: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stackX I
				// 14: iload 1
				// 15: if_icmpgt 87
				// 18: aload 0
				// 19: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stackX I
				// 1c: bipush 16
				// 1e: iadd
				// 1f: iload 1
				// 20: if_icmplt 87
				// 23: aload 0
				// 24: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stackY I
				// 27: iload 2
				// 28: if_icmpgt 87
				// 2b: aload 0
				// 2c: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stackY I
				// 2f: bipush 16
				// 31: iadd
				// 32: iload 2
				// 33: if_icmplt 87
				// 36: getstatic ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.$assertionsDisabled Z
				// 39: ifne 54
				// 3c: aload 0
				// 3d: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
				// 40: ldc ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry
				// 42: if_acmpne 54
				// 45: aload 0
				// 46: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.isValidValue Z
				// 49: ifne 54
				// 4c: new java/lang/AssertionError
				// 4f: dup
				// 50: invokespecial java/lang/AssertionError.<init> ()V
				// 53: athrow
				// 54: getstatic ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.TOOLTIP Ljava/lang/reflect/Method;
				// 57: aload 0
				// 58: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.owningScreen Lnet/minecraftforge/fml/client/config/GuiEditArray;
				// 5b: bipush 3
				// 5c: anewarray 45
				// 5f: dup
				// 60: bipush 0
				// 61: aload 0
				// 62: getfield ic2/core/gui/Ic2GuiFactory$IC2ConfigGuiScreen$ItemEntry.stack Lnet/minecraft/item/ItemStack;
				// 65: aastore
				// 66: dup
				// 67: bipush 1
				// 68: iload 1
				// 69: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
				// 6c: aastore
				// 6d: dup
				// 6e: bipush 2
				// 6f: iload 2
				// 70: invokestatic java/lang/Integer.valueOf (I)Ljava/lang/Integer;
				// 73: aastore
				// 74: invokevirtual java/lang/reflect/Method.invoke (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
				// 77: pop
				// 78: goto 87
				// 7b: astore 3
				// 7c: new java/lang/RuntimeException
				// 7f: dup
				// 80: ldc "Error drawing tooltip!"
				// 82: aload 3
				// 83: invokespecial java/lang/RuntimeException.<init> (Ljava/lang/String;Ljava/lang/Throwable;)V
				// 86: athrow
				// 87: return
				// try (41 -> 63): 64 java/lang/Exception
			}
		}

		private static class ListElement extends DummyConfigElement.DummyListElement
		{
			protected Object[] previous;
			private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

			protected ListElement(String name, CharSequence value, CharSequence defaultValues, String langKey, Pattern pattern)
			{
				super(name, COMMA_SPLITTER.splitToList(defaultValues).toArray(), ConfigGuiType.STRING, langKey, pattern);
				this.previous = this.values = COMMA_SPLITTER.splitToList(value).toArray();
			}

			protected void save()
			{
				if (!this.previous.equals(this.getList()))
				{
					MainConfig.get()
						.set(
							this.getLanguageKey().substring(Ic2GuiFactory.IC2ConfigGuiScreen.LANG_KEY_LENGTH).replace('.', '/'),
							Ic2GuiFactory.IC2ConfigGuiScreen.COMMA_JOINER.join(this.getList())
						);
				}
			}

			@Override
			public void set(Object[] values)
			{
				this.previous = this.values;
				this.values = values;
			}

			@Override
			public void setToDefault()
			{
				this.previous = this.values;
				super.setToDefault();
			}

			@Override
			public String toString()
			{
				return "Config" + this.getClass().getSimpleName() + '<' + this.name + '>';
			}
		}

		private static class UUListElement extends Ic2GuiFactory.IC2ConfigGuiScreen.ListElement
		{
			private static CharSequence getValues(Iterator<Config.Value> sub)
			{
				return Ic2GuiFactory.IC2ConfigGuiScreen.COMMA_JOINER.join(Iterators.transform(sub, new Function<Config.Value, String>()
				{
					public String apply(Config.Value input)
					{
						return input.name + ':' + input.value;
					}
				}));
			}

			UUListElement()
			{
				super(
					"predefined",
					getValues(MainConfig.get().getSub("balance/uu-values/predefined").valueIterator()),
					getValues(MainConfig.getDefaults("balance/uu-values/predefined")),
					"ic2.config.sub.predefined",
					Ic2GuiFactory.IC2ConfigGuiScreen.ITEM_PATTERN
				);
				this.setConfigEntryClass(Ic2GuiFactory.IC2ConfigGuiScreen.UUListElement.ArrayCategory.class);
				this.setArrayEntryClass(Ic2GuiFactory.IC2ConfigGuiScreen.UUListElement.UUEntry.class);
			}

			@Override
			public void set(Object[] values)
			{
				super.set(values);
				this.save();
				MainConfig.save();
			}

			@Override
			protected void save()
			{
				Config config = MainConfig.get().getSub("balance/uu-values/predefined");

				for (Object line : this.getList())
				{
					String part = (String) line;
					System.out.println("Trying to save part: " + part);
					int split = part.lastIndexOf(58);
					config.set(part.substring(0, split), part.substring(split + 1));
				}
			}

			public static class ArrayCategory extends GuiConfigEntries.CategoryEntry
			{
				private int index;
				protected Object[] currentValues;
				protected final Object[] beforeValues;

				public ArrayCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
				{
					super(owningScreen, owningEntryList, configElement);
					this.beforeValues = configElement.getList();
					this.currentValues = configElement.getList();
					this.childScreen = this.buildChildScreen();
				}

				public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
				{
					this.index = index;
					return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
				}

				protected GuiScreen buildChildScreen()
				{
					return new GuiEditArray(this.owningScreen, this.configElement, this.index, this.currentValues, this.enabled());
				}

				@Override
				public boolean isDefault()
				{
					return Arrays.deepEquals(this.configElement.getDefaults(), this.currentValues);
				}

				@Override
				public void setToDefault()
				{
					this.currentValues = this.configElement.getDefaults();
				}

				@Override
				public boolean isChanged()
				{
					return !Arrays.deepEquals(this.beforeValues, this.currentValues);
				}

				@Override
				public void undoChanges()
				{
					this.currentValues = this.beforeValues;
				}

				@Override
				public boolean saveConfigElement()
				{
					if (this.isChanged())
					{
						this.configElement.set(this.currentValues);
						return this.configElement.requiresMcRestart();
					} else
					{
						return false;
					}
				}
			}

			public static class UUEntry extends Ic2GuiFactory.IC2ConfigGuiScreen.ItemEntry
			{
				protected boolean hasValidStack;
				protected final GuiTextField uuValue;

				public UUEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value)
				{
					super(owningScreen, owningEntryList, configElement, value);
					int totalSpace = this.textFieldValue.width;
					int textSpace = Math.round(totalSpace * 3 / 4.0F);
					int numSpace = totalSpace - textSpace;
					this.textFieldValue.width = textSpace - 1;
					int var10005 = this.textFieldValue.x + textSpace;
					this.uuValue = new GuiTextField(1, owningEntryList.getMC().fontRenderer, var10005, 0, numSpace, 16);
					this.uuValue.setMaxStringLength(25);
					this.uuValue.setText(value.toString());
					this.uuValue.setValidator(new Predicate<String>()
					{
						public boolean apply(String input)
						{
							try
							{
								return Double.parseDouble(input) >= 0.0;
							} catch (NumberFormatException e)
							{
								return input.isEmpty();
							}
						}
					});
					String val = value.toString();
					int split = val.lastIndexOf(58);
					if (split > -1)
					{
						this.textFieldValue.setText(val.substring(0, split));
						this.uuValue.setText(val.substring(split + 1));
					} else
					{
						assert this.textFieldValue.getText().isEmpty() : "Expected empty textFieldValue but found: " + this.textFieldValue.getText();
						assert this.uuValue.getText().isEmpty() : "Expected empty uuValue but found: " + this.uuValue.getText();
					}

					assert configElement.getValidationPattern() != null;
					this.updateState();
				}

				protected void updateState()
				{
					if (this.configElement.getValidationPattern().matcher(this.getStack()).matches())
					{
						this.updateStack();
						this.hasValidStack = this.isValidValue;
					} else
					{
						this.hasValidStack = false;
					}

					this.isValidValue = this.isValidValue & !this.uuValue.getText().trim().isEmpty();
				}

				@Override
				public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial)
				{
					boolean previous = this.isValidValue;
					this.isValidValue = this.hasValidStack;
					super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
					this.isValidValue = previous;
					this.uuValue.setVisible(slotIndex != this.owningEntryList.listEntries.size() - 1);
					this.uuValue.y = y + 1;
					this.uuValue.drawTextBox();
				}

				@Override
				public void keyTyped(char eventChar, int eventKey)
				{
					boolean enabled = this.isEnabled();
					if (enabled || eventKey == 203 || eventKey == 205 || eventKey == 199 || eventKey == 207)
					{
						this.textFieldValue.textboxKeyTyped(enabled ? eventChar : '\u0000', eventKey);
						this.uuValue.textboxKeyTyped(enabled ? eventChar : '\u0000', eventKey);
						if (enabled)
						{
							this.updateState();
						}
					}
				}

				@Override
				public void updateCursorCounter()
				{
					super.updateCursorCounter();
					this.uuValue.updateCursorCounter();
				}

				@Override
				public void mouseClicked(int x, int y, int mouseEvent)
				{
					super.mouseClicked(x, y, mouseEvent);
					this.uuValue.mouseClicked(x, y, mouseEvent);
				}

				@Override
				public String getValue()
				{
					return this.getStack() + ':' + this.uuValue.getText().trim();
				}

				@Override
				protected String getStack()
				{
					return super.getValue();
				}
			}
		}
	}
}
