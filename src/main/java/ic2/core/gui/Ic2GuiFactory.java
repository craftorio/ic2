// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import com.google.common.base.Predicate;
import net.minecraft.client.gui.GuiTextField;
import java.util.Arrays;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import com.google.common.collect.Iterators;
import com.google.common.base.Function;
import ic2.core.util.ReflectionUtil;
import net.minecraft.client.renderer.RenderHelper;
import java.text.ParseException;
import ic2.core.util.StackUtil;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraft.item.ItemStack;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import com.google.common.base.Splitter;
import java.util.Objects;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import java.util.Iterator;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import java.util.ArrayList;
import net.minecraftforge.fml.client.config.IConfigElement;
import ic2.core.util.Config;
import java.util.List;
import ic2.core.init.MainConfig;
import java.util.regex.Pattern;
import com.google.common.base.Joiner;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.IModGuiFactory;

public class Ic2GuiFactory implements IModGuiFactory
{
    public void initialize(final Minecraft mc) {
    }
    
    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return Collections.emptySet();
    }
    
    public boolean hasConfigGui() {
        return true;
    }
    
    public GuiScreen createConfigGui(final GuiScreen parentScreen) {
        return (GuiScreen)new IC2ConfigGuiScreen(parentScreen);
    }
    
    public static class IC2ConfigGuiScreen extends GuiConfig
    {
        static final int LANG_KEY_LENGTH;
        static final Joiner COMMA_JOINER;
        static final Pattern ITEM_PATTERN;
        private static final Pattern IS_BOOLEAN;
        private static final Pattern IS_INT;
        private static final Pattern IS_DOUBLE;
        
        public IC2ConfigGuiScreen(final GuiScreen parent) {
            super(parent, (List)sinkCategoryLevel(MainConfig.get(), "."), "ic2", false, false, "IC2 Configuration");
        }
        
        private static List<IConfigElement> sinkCategoryLevel(final Config config, final String parentName) {
            final List<IConfigElement> list = new ArrayList<IConfigElement>(config.getNumberOfSections() + config.getNumberOfConfigs());
            if (config.hasChildSection()) {
                final Iterator<Config> configCategories = config.sectionIterator();
                while (configCategories.hasNext()) {
                    final Config category = configCategories.next();
                    if ("predefined".equals(category.name) && ".balance.uu-values.".equals(parentName)) {
                        list.add((IConfigElement)new UUListElement());
                    }
                    else {
                        list.add((IConfigElement)new DummyConfigElement.DummyCategoryElement(category.name, "ic2.config.sub." + category.name, (List)sinkCategoryLevel(category, parentName + category.name + '.')));
                    }
                }
                if (!config.isEmptySection()) {
                    getConfigs(list, config.valueIterator(), parentName);
                }
            }
            else {
                getConfigs(list, config.valueIterator(), parentName);
            }
            return list;
        }
        
        private static void getConfigs(final List<IConfigElement> list, final Iterator<Config.Value> configs, final String parentName) {
            while (configs.hasNext()) {
                final Config.Value conf = configs.next();
                final Config.Value defaultConf = MainConfig.getDefault(parentName.substring(1).replace('.', '/') + conf.name);
                if (defaultConf == null) {
                    continue;
                }
                if (defaultConf.value.isEmpty() || defaultConf.value.contains(",") || defaultConf.comment.contains("comma")) {
                    list.add(new ListElement(conf.name, conf.value, defaultConf.value, "ic2.config" + parentName + conf.name, IC2ConfigGuiScreen.ITEM_PATTERN).setArrayEntryClass((Class)ItemEntry.class));
                }
                else {
                    ConfigGuiType type;
                    if (IC2ConfigGuiScreen.IS_DOUBLE.matcher(conf.value).matches()) {
                        type = ConfigGuiType.DOUBLE;
                    }
                    else if (IC2ConfigGuiScreen.IS_INT.matcher(conf.value).matches()) {
                        type = ConfigGuiType.INTEGER;
                    }
                    else if (IC2ConfigGuiScreen.IS_BOOLEAN.matcher(conf.value).matches()) {
                        type = ConfigGuiType.BOOLEAN;
                    }
                    else {
                        type = ConfigGuiType.STRING;
                    }
                    list.add((IConfigElement)new ConfigElement(conf.name, conf.value, defaultConf.value, type, "ic2.config" + parentName + conf.name));
                }
            }
        }
        
        public void onGuiClosed() {
            for (final IConfigElement config : this.configElements) {
                this.saveConfig(config);
            }
            MainConfig.save();
            super.onGuiClosed();
        }
        
        private void saveConfig(final IConfigElement config) {
            if (config.getChildElements() != null) {
                for (final IConfigElement subConfig : config.getChildElements()) {
                    this.saveConfig(subConfig);
                }
            }
            if (config.isProperty()) {
                if (config.isList()) {
                    assert config instanceof ListElement : "Unexpected class type: " + config.getClass();
                    ((ListElement)config).save();
                }
                else {
                    assert config.getClass() == ConfigElement.class : "Unexpected class type: " + config.getClass();
                    if (!Objects.equals(config.get(), ((ConfigElement)config).previous)) {
                        MainConfig.get().set(config.getLanguageKey().substring(IC2ConfigGuiScreen.LANG_KEY_LENGTH).replace('.', '/'), config.get());
                    }
                }
            }
        }
        
        static {
            LANG_KEY_LENGTH = "ic2.config.".length();
            COMMA_JOINER = Joiner.on(", ");
            ITEM_PATTERN = Pattern.compile("^[A-Za-z0-9_]+:[A-Za-z0-9_]+(#[A-Za-z0-9_]+|(@(\\d+|\\*)))?$");
            IS_BOOLEAN = Pattern.compile("true|false", 2);
            IS_INT = Pattern.compile("\\d");
            IS_DOUBLE = Pattern.compile("\\d\\.\\d");
        }
        
        private static class ConfigElement extends DummyConfigElement
        {
            Object previous;
            
            ConfigElement(final String name, final Object value, final Object defaultValue, final ConfigGuiType type, final String langKey) {
                super(name, defaultValue, type, langKey);
                this.value = value;
                this.previous = value;
            }
            
            public void set(final Object value) {
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
        
        private static class ListElement extends DummyConfigElement.DummyListElement
        {
            protected Object[] previous;
            private static final Splitter COMMA_SPLITTER;
            
            protected ListElement(final String name, final CharSequence value, final CharSequence defaultValues, final String langKey, final Pattern pattern) {
                super(name, ListElement.COMMA_SPLITTER.splitToList(defaultValues).toArray(), ConfigGuiType.STRING, langKey, pattern);
                final Object[] array = ListElement.COMMA_SPLITTER.splitToList(value).toArray();
                this.values = array;
                this.previous = array;
            }
            
            protected void save() {
                if (!this.previous.equals(this.getList())) {
                    MainConfig.get().set(this.getLanguageKey().substring(IC2ConfigGuiScreen.LANG_KEY_LENGTH).replace('.', '/'), IC2ConfigGuiScreen.COMMA_JOINER.join(this.getList()));
                }
            }
            
            public void set(final Object[] values) {
                this.previous = this.values;
                this.values = values;
            }
            
            public void setToDefault() {
                this.previous = this.values;
                super.setToDefault();
            }
            
            public String toString() {
                return "Config" + this.getClass().getSimpleName() + '<' + this.name + '>';
            }
            
            static {
                COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
            }
        }
        
        public static class ItemEntry extends GuiEditArrayEntries.StringEntry
        {
            private static final Field ENABLED;
            private static final Method TOOLTIP;
            protected ItemStack stack;
            protected int stackX;
            protected int stackY;
            
            public ItemEntry(final GuiEditArray owningScreen, final GuiEditArrayEntries owningEntryList, final IConfigElement configElement, final Object value) {
                super(owningScreen, owningEntryList, configElement, value);
                assert this.isValidated;
                if (this.isValidValue) {
                    this.updateStack();
                }
            }
            
            public String getValue() {
                return (String)super.getValue();
            }
            
            protected String getStack() {
                return this.getValue();
            }
            
            protected void updateStack() {
                try {
                    this.stack = ConfigUtil.asStack(this.getStack());
                    this.isValidValue = !StackUtil.isEmpty(this.stack);
                }
                catch (final ParseException e) {
                    this.isValidValue = false;
                }
            }
            
            protected boolean isEnabled() {
                try {
                    return ItemEntry.ENABLED.getBoolean(this.owningScreen);
                }
                catch (final Exception e) {
                    throw new RuntimeException("Error checking owningScreen enabled!", e);
                }
            }
            
            public void keyTyped(final char eventChar, final int eventKey) {
                super.keyTyped(eventChar, eventKey);
                if (this.isValidValue && this.isEnabled()) {
                    this.updateStack();
                }
            }
            
            public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partial) {
                if (this.isValidValue) {
                    this.isValidated = false;
                }
                super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
                this.isValidated = true;
                assert this.getValue() != null;
                if (this.isValidValue) {
                    RenderHelper.enableGUIStandardItemLighting();
                    this.owningEntryList.getMC().getRenderItem().renderItemIntoGUI(this.stack, this.stackX = listWidth / 4 - 16 - 1, this.stackY = y + slotHeight / 2 - 8);
                    RenderHelper.disableStandardItemLighting();
                }
            }
            
            public void drawToolTip(final int mouseX, final int mouseY) {
                super.drawToolTip(mouseX, mouseY);
                if (!StackUtil.isEmpty(this.stack) && this.stackX <= mouseX && this.stackX + 16 >= mouseX && this.stackY <= mouseY && this.stackY + 16 >= mouseY) {
                    assert !(!this.isValidValue);
                    try {
                        ItemEntry.TOOLTIP.invoke(this.owningScreen, this.stack, mouseX, mouseY);
                    }
                    catch (final Exception e) {
                        throw new RuntimeException("Error drawing tooltip!", e);
                    }
                }
            }
            
            static {
                ENABLED = ReflectionUtil.getField(GuiEditArray.class, Boolean.TYPE);
                TOOLTIP = ReflectionUtil.getMethod(GuiScreen.class, new String[] { "renderToolTip", "renderToolTip", "a" }, ItemStack.class, Integer.TYPE, Integer.TYPE);
            }
        }
        
        private static class UUListElement extends ListElement
        {
            private static CharSequence getValues(final Iterator<Config.Value> sub) {
                return IC2ConfigGuiScreen.COMMA_JOINER.join(Iterators.transform((Iterator)sub, (Function)new Function<Config.Value, String>() {
                    public String apply(final Config.Value input) {
                        return input.name + ':' + input.value;
                    }
                }));
            }
            
            UUListElement() {
                super("predefined", getValues(MainConfig.get().getSub("balance/uu-values/predefined").valueIterator()), getValues(MainConfig.getDefaults("balance/uu-values/predefined")), "ic2.config.sub.predefined", IC2ConfigGuiScreen.ITEM_PATTERN);
                this.setConfigEntryClass((Class)ArrayCategory.class);
                this.setArrayEntryClass((Class)UUEntry.class);
            }
            
            @Override
            public void set(final Object[] values) {
                super.set(values);
                this.save();
                MainConfig.save();
            }
            
            @Override
            protected void save() {
                final Config config = MainConfig.get().getSub("balance/uu-values/predefined");
                for (final Object line : this.getList()) {
                    final String part = (String)line;
                    System.out.println("Trying to save part: " + part);
                    final int split = part.lastIndexOf(58);
                    config.set(part.substring(0, split), part.substring(split + 1));
                }
            }
            
            public static class ArrayCategory extends GuiConfigEntries.CategoryEntry
            {
                private int index;
                protected Object[] currentValues;
                protected final Object[] beforeValues;
                
                public ArrayCategory(final GuiConfig owningScreen, final GuiConfigEntries owningEntryList, final IConfigElement configElement) {
                    super(owningScreen, owningEntryList, configElement);
                    this.beforeValues = configElement.getList();
                    this.currentValues = configElement.getList();
                    this.childScreen = this.buildChildScreen();
                }
                
                public boolean mousePressed(final int index, final int x, final int y, final int mouseEvent, final int relativeX, final int relativeY) {
                    this.index = index;
                    return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
                }
                
                protected GuiScreen buildChildScreen() {
                    return (GuiScreen)new GuiEditArray((GuiScreen)this.owningScreen, this.configElement, this.index, this.currentValues, this.enabled());
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
                    if (this.isChanged()) {
                        this.configElement.set(this.currentValues);
                        return this.configElement.requiresMcRestart();
                    }
                    return false;
                }
            }
            
            public static class UUEntry extends ItemEntry
            {
                protected boolean hasValidStack;
                protected final GuiTextField uuValue;
                
                public UUEntry(final GuiEditArray owningScreen, final GuiEditArrayEntries owningEntryList, final IConfigElement configElement, final Object value) {
                    super(owningScreen, owningEntryList, configElement, value);
                    final int totalSpace = this.textFieldValue.width;
                    final int textSpace = Math.round(totalSpace * 3 / 4.0f);
                    final int numSpace = totalSpace - textSpace;
                    this.textFieldValue.width = textSpace - 1;
                    (this.uuValue = new GuiTextField(1, owningEntryList.getMC().fontRenderer, this.textFieldValue.x + textSpace, 0, numSpace, 16)).setMaxStringLength(25);
                    this.uuValue.setText(value.toString());
                    this.uuValue.setValidator((Predicate)new Predicate<String>() {
                        public boolean apply(final String input) {
                            try {
                                return Double.parseDouble(input) >= 0.0;
                            }
                            catch (final NumberFormatException e) {
                                return input.isEmpty();
                            }
                        }
                    });
                    final String val = value.toString();
                    final int split = val.lastIndexOf(58);
                    if (split > -1) {
                        this.textFieldValue.setText(val.substring(0, split));
                        this.uuValue.setText(val.substring(split + 1));
                    }
                    else {
                        assert this.textFieldValue.getText().isEmpty() : "Expected empty textFieldValue but found: " + this.textFieldValue.getText();
                        assert this.uuValue.getText().isEmpty() : "Expected empty uuValue but found: " + this.uuValue.getText();
                    }
                    assert configElement.getValidationPattern() != null;
                    this.updateState();
                }
                
                protected void updateState() {
                    if (this.configElement.getValidationPattern().matcher(this.getStack()).matches()) {
                        this.updateStack();
                        this.hasValidStack = this.isValidValue;
                    }
                    else {
                        this.hasValidStack = false;
                    }
                    this.isValidValue &= !this.uuValue.getText().trim().isEmpty();
                }
                
                @Override
                public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected, final float partial) {
                    final boolean previous = this.isValidValue;
                    this.isValidValue = this.hasValidStack;
                    super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
                    this.isValidValue = previous;
                    this.uuValue.setVisible(slotIndex != this.owningEntryList.listEntries.size() - 1);
                    this.uuValue.y = y + 1;
                    this.uuValue.drawTextBox();
                }
                
                @Override
                public void keyTyped(final char eventChar, final int eventKey) {
                    final boolean enabled = this.isEnabled();
                    if (enabled || eventKey == 203 || eventKey == 205 || eventKey == 199 || eventKey == 207) {
                        this.textFieldValue.textboxKeyTyped(enabled ? eventChar : '\0', eventKey);
                        this.uuValue.textboxKeyTyped(enabled ? eventChar : '\0', eventKey);
                        if (enabled) {
                            this.updateState();
                        }
                    }
                }
                
                public void updateCursorCounter() {
                    super.updateCursorCounter();
                    this.uuValue.updateCursorCounter();
                }
                
                public void mouseClicked(final int x, final int y, final int mouseEvent) {
                    super.mouseClicked(x, y, mouseEvent);
                    this.uuValue.mouseClicked(x, y, mouseEvent);
                }
                
                @Override
                public String getValue() {
                    return this.getStack() + ':' + this.uuValue.getText().trim();
                }
                
                @Override
                protected String getStack() {
                    return super.getValue();
                }
            }
        }
    }
}
