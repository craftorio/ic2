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

public class Ic2GuiFactory implements IModGuiFactory {
  public static class IC2ConfigGuiScreen extends GuiConfig {
    public IC2ConfigGuiScreen(GuiScreen parent) {
      super(parent, sinkCategoryLevel(MainConfig.get(), "."), "ic2", false, false, "IC2 Configuration");
    }
    
    private static List<IConfigElement> sinkCategoryLevel(Config config, String parentName) {
      List<IConfigElement> list = new ArrayList<>(config.getNumberOfSections() + config.getNumberOfConfigs());
      if (config.hasChildSection()) {
        for (Iterator<Config> configCategories = config.sectionIterator(); configCategories.hasNext(); ) {
          Config category = configCategories.next();
          if ("predefined".equals(category.name) && ".balance.uu-values.".equals(parentName)) {
            list.add(new UUListElement());
            continue;
          } 
          list.add(new DummyConfigElement.DummyCategoryElement(category.name, "ic2.config.sub." + category.name, sinkCategoryLevel(category, parentName + category.name + '.')));
        } 
        if (!config.isEmptySection())
          getConfigs(list, config.valueIterator(), parentName); 
      } else {
        getConfigs(list, config.valueIterator(), parentName);
      } 
      return list;
    }
    
    private static void getConfigs(List<IConfigElement> list, Iterator<Config.Value> configs, String parentName) {
      while (configs.hasNext()) {
        ConfigGuiType type;
        Config.Value conf = configs.next();
        Config.Value defaultConf = MainConfig.getDefault(parentName.substring(1).replace('.', '/') + conf.name);
        if (defaultConf == null)
          continue; 
        if (defaultConf.value.isEmpty() || defaultConf.value.contains(",") || defaultConf.comment.contains("comma")) {
          list.add((new ListElement(conf.name, conf.value, defaultConf.value, "ic2.config" + parentName + conf.name, ITEM_PATTERN)).setArrayEntryClass(ItemEntry.class));
          continue;
        } 
        if (IS_DOUBLE.matcher(conf.value).matches()) {
          type = ConfigGuiType.DOUBLE;
        } else if (IS_INT.matcher(conf.value).matches()) {
          type = ConfigGuiType.INTEGER;
        } else if (IS_BOOLEAN.matcher(conf.value).matches()) {
          type = ConfigGuiType.BOOLEAN;
        } else {
          type = ConfigGuiType.STRING;
        } 
        list.add(new ConfigElement(conf.name, conf.value, defaultConf.value, type, "ic2.config" + parentName + conf.name));
      } 
    }
    
    public void onGuiClosed() {
      for (IConfigElement config : this.configElements)
        saveConfig(config); 
      MainConfig.save();
      super.onGuiClosed();
    }
    
    private void saveConfig(IConfigElement config) {
      if (config.getChildElements() != null)
        for (IConfigElement subConfig : config.getChildElements())
          saveConfig(subConfig);  
      if (config.isProperty())
        if (config.isList()) {
          assert config instanceof ListElement : "Unexpected class type: " + config.getClass();
          ((ListElement)config).save();
        } else {
          assert config.getClass() == ConfigElement.class : "Unexpected class type: " + config.getClass();
          if (!Objects.equals(config.get(), ((ConfigElement)config).previous))
            MainConfig.get().set(config.getLanguageKey().substring(LANG_KEY_LENGTH).replace('.', '/'), config.get()); 
        }  
    }
    
    private static class ConfigElement extends DummyConfigElement {
      Object previous;
      
      ConfigElement(String name, Object value, Object defaultValue, ConfigGuiType type, String langKey) {
        super(name, defaultValue, type, langKey);
        this.previous = this.value = value;
      }
      
      public void set(Object value) {
        this.previous = this.value;
        this.value = value;
      }
      
      public void setToDefault() {
        this.previous = this.value;
        super.setToDefault();
      }
      
      public String toString() {
        return "ConfigElement<" + this.name + '>';
      }
    }
    
    private static class ListElement extends DummyConfigElement.DummyListElement {
      protected Object[] previous;
      
      protected ListElement(String name, CharSequence value, CharSequence defaultValues, String langKey, Pattern pattern) {
        super(name, COMMA_SPLITTER.splitToList(defaultValues).toArray(), ConfigGuiType.STRING, langKey, pattern);
        this.previous = this.values = COMMA_SPLITTER.splitToList(value).toArray();
      }
      
      protected void save() {
        if (!this.previous.equals(getList()))
          MainConfig.get().set(getLanguageKey().substring(Ic2GuiFactory.IC2ConfigGuiScreen.LANG_KEY_LENGTH).replace('.', '/'), Ic2GuiFactory.IC2ConfigGuiScreen.COMMA_JOINER.join(getList())); 
      }
      
      public void set(Object[] values) {
        this.previous = this.values;
        this.values = values;
      }
      
      public void setToDefault() {
        this.previous = this.values;
        super.setToDefault();
      }
      
      public String toString() {
        return "Config" + getClass().getSimpleName() + '<' + this.name + '>';
      }
      
      private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    }
    
    public static class ItemEntry extends GuiEditArrayEntries.StringEntry {
      private static final Field ENABLED = ReflectionUtil.getField(GuiEditArray.class, boolean.class);
      
      private static final Method TOOLTIP = ReflectionUtil.getMethod(GuiScreen.class, new String[] { "renderToolTip", "renderToolTip", "a" }, new Class[] { ItemStack.class, int.class, int.class });
      
      protected ItemStack stack;
      
      protected int stackX;
      
      protected int stackY;
      
      public ItemEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value) {
        super(owningScreen, owningEntryList, configElement, value);
        assert this.isValidated;
        if (this.isValidValue)
          updateStack(); 
      }
      
      public String getValue() {
        return (String)super.getValue();
      }
      
      protected String getStack() {
        return getValue();
      }
      
      protected void updateStack() {
        try {
          this.stack = ConfigUtil.asStack(getStack());
          this.isValidValue = !StackUtil.isEmpty(this.stack);
        } catch (ParseException e) {
          this.isValidValue = false;
        } 
      }
      
      protected boolean isEnabled() {
        try {
          return ENABLED.getBoolean(this.owningScreen);
        } catch (Exception e) {
          throw new RuntimeException("Error checking owningScreen enabled!", e);
        } 
      }
      
      public void keyTyped(char eventChar, int eventKey) {
        super.keyTyped(eventChar, eventKey);
        if (this.isValidValue && isEnabled())
          updateStack(); 
      }
      
      public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
        if (this.isValidValue)
          this.isValidated = false; 
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
        this.isValidated = true;
        assert getValue() != null;
        if (this.isValidValue) {
          RenderHelper.enableGUIStandardItemLighting();
          this.owningEntryList.getMC().getRenderItem().renderItemIntoGUI(this.stack, this.stackX = listWidth / 4 - 16 - 1, this.stackY = y + slotHeight / 2 - 8);
          RenderHelper.disableStandardItemLighting();
        } 
      }
      
      public void drawToolTip(int mouseX, int mouseY) {
        super.drawToolTip(mouseX, mouseY);
        if (!StackUtil.isEmpty(this.stack) && this.stackX <= mouseX && this.stackX + 16 >= mouseX && this.stackY <= mouseY && this.stackY + 16 >= mouseY) {
          assert getClass() != ItemEntry.class || this.isValidValue;
          try {
            TOOLTIP.invoke(this.owningScreen, new Object[] { this.stack, Integer.valueOf(mouseX), Integer.valueOf(mouseY) });
          } catch (Exception e) {
            throw new RuntimeException("Error drawing tooltip!", e);
          } 
        } 
      }
    }
    
    private static class UUListElement extends ListElement {
      public static class ArrayCategory extends GuiConfigEntries.CategoryEntry {
        private int index;
        
        protected Object[] currentValues;
        
        protected final Object[] beforeValues;
        
        public ArrayCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
          super(owningScreen, owningEntryList, configElement);
          this.beforeValues = configElement.getList();
          this.currentValues = configElement.getList();
          this.childScreen = buildChildScreen();
        }
        
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
          this.index = index;
          return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
        }
        
        protected GuiScreen buildChildScreen() {
          return (GuiScreen)new GuiEditArray((GuiScreen)this.owningScreen, this.configElement, this.index, this.currentValues, enabled());
        }
        
        public boolean isDefault() {
          return Arrays.deepEquals(this.configElement.getDefaults(), this.currentValues);
        }
        
        public void setToDefault() {
          this.currentValues = this.configElement.getDefaults();
        }
        
        public boolean isChanged() {
          return !Arrays.deepEquals(this.beforeValues, this.currentValues);
        }
        
        public void undoChanges() {
          this.currentValues = this.beforeValues;
        }
        
        public boolean saveConfigElement() {
          if (isChanged()) {
            this.configElement.set(this.currentValues);
            return this.configElement.requiresMcRestart();
          } 
          return false;
        }
      }
      
      public static class UUEntry extends Ic2GuiFactory.IC2ConfigGuiScreen.ItemEntry {
        protected boolean hasValidStack;
        
        protected final GuiTextField uuValue;
        
        public UUEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value) {
          super(owningScreen, owningEntryList, configElement, value);
          int totalSpace = this.textFieldValue.width;
          int textSpace = Math.round((totalSpace * 3) / 4.0F);
          int numSpace = totalSpace - textSpace;
          this.textFieldValue.width = textSpace - 1;
          this.uuValue = new GuiTextField(1, (owningEntryList.getMC()).fontRenderer, this.textFieldValue.x + textSpace, 0, numSpace, 16);
          this.uuValue.setMaxStringLength(25);
          this.uuValue.setText(value.toString());
          this.uuValue.setValidator(new Predicate<String>() {
                public boolean apply(String input) {
                  try {
                    return (Double.parseDouble(input) >= 0.0D);
                  } catch (NumberFormatException e) {
                    return input.isEmpty();
                  } 
                }
              });
          String val = value.toString();
          int split = val.lastIndexOf(':');
          if (split > -1) {
            this.textFieldValue.setText(val.substring(0, split));
            this.uuValue.setText(val.substring(split + 1));
          } else {
            assert this.textFieldValue.getText().isEmpty() : "Expected empty textFieldValue but found: " + this.textFieldValue.getText();
            assert this.uuValue.getText().isEmpty() : "Expected empty uuValue but found: " + this.uuValue.getText();
          } 
          assert configElement.getValidationPattern() != null;
          updateState();
        }
        
        protected void updateState() {
          if (this.configElement.getValidationPattern().matcher(getStack()).matches()) {
            updateStack();
            this.hasValidStack = this.isValidValue;
          } else {
            this.hasValidStack = false;
          } 
          this.isValidValue &= !this.uuValue.getText().trim().isEmpty() ? 1 : 0;
        }
        
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
          boolean previous = this.isValidValue;
          this.isValidValue = this.hasValidStack;
          super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
          this.isValidValue = previous;
          this.uuValue.setVisible((slotIndex != this.owningEntryList.listEntries.size() - 1));
          this.uuValue.y = y + 1;
          this.uuValue.drawTextBox();
        }
        
        public void keyTyped(char eventChar, int eventKey) {
          boolean enabled = isEnabled();
          if (enabled || eventKey == 203 || eventKey == 205 || eventKey == 199 || eventKey == 207) {
            this.textFieldValue.textboxKeyTyped(enabled ? eventChar : Character.MIN_VALUE, eventKey);
            this.uuValue.textboxKeyTyped(enabled ? eventChar : Character.MIN_VALUE, eventKey);
            if (enabled)
              updateState(); 
          } 
        }
        
        public void updateCursorCounter() {
          super.updateCursorCounter();
          this.uuValue.updateCursorCounter();
        }
        
        public void mouseClicked(int x, int y, int mouseEvent) {
          super.mouseClicked(x, y, mouseEvent);
          this.uuValue.mouseClicked(x, y, mouseEvent);
        }
        
        public String getValue() {
          return getStack() + ':' + this.uuValue.getText().trim();
        }
        
        protected String getStack() {
          return super.getValue();
        }
      }
      
      private static CharSequence getValues(Iterator<Config.Value> sub) {
        return Ic2GuiFactory.IC2ConfigGuiScreen.COMMA_JOINER.join(Iterators.transform(sub, new Function<Config.Value, String>() {
                public String apply(Config.Value input) {
                  return input.name + ':' + input.value;
                }
              }));
      }
      
      UUListElement() {
        super("predefined", getValues(MainConfig.get().getSub("balance/uu-values/predefined").valueIterator()), 
            getValues(MainConfig.getDefaults("balance/uu-values/predefined")), "ic2.config.sub.predefined", Ic2GuiFactory.IC2ConfigGuiScreen.ITEM_PATTERN);
        setConfigEntryClass(ArrayCategory.class);
        setArrayEntryClass(UUEntry.class);
      }
      
      public void set(Object[] values) {
        super.set(values);
        save();
        MainConfig.save();
      }
      
      protected void save() {
        Config config = MainConfig.get().getSub("balance/uu-values/predefined");
        for (Object line : getList()) {
          String part = (String)line;
          System.out.println("Trying to save part: " + part);
          int split = part.lastIndexOf(':');
          config.set(part.substring(0, split), part.substring(split + 1));
        } 
      }
    }
    
    static final int LANG_KEY_LENGTH = "ic2.config.".length();
    
    static final Joiner COMMA_JOINER = Joiner.on(", ");
    
    static final Pattern ITEM_PATTERN = Pattern.compile("^[A-Za-z0-9_]+:[A-Za-z0-9_]+(#[A-Za-z0-9_]+|(@(\\d+|\\*)))?$");
    
    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", 2);
    
    private static final Pattern IS_INT = Pattern.compile("\\d");
    
    private static final Pattern IS_DOUBLE = Pattern.compile("\\d\\.\\d");
  }
  
  public void initialize(Minecraft mc) {}
  
  public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
    return Collections.emptySet();
  }
  
  public boolean hasConfigGui() {
    return true;
  }
  
  public GuiScreen createConfigGui(GuiScreen parentScreen) {
    return (GuiScreen)new IC2ConfigGuiScreen(parentScreen);
  }
}
