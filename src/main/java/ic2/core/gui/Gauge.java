// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import ic2.core.GuiIC2;

public abstract class Gauge<T extends Gauge<T>> extends GuiElement<T>
{
    protected final GaugeProperties properties;
    
    protected Gauge(final GuiIC2<?> gui, final int x, final int y, final GaugeProperties properties) {
        super(gui, x + properties.hoverXOffset, y + properties.hoverYOffset, properties.hoverWidth, properties.hoverHeight);
        this.properties = properties;
    }
    
    protected abstract double getRatio();
    
    protected boolean isActive(final double ratio) {
        return ratio > 0.0;
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        double ratio = this.getRatio();
        if (ratio <= 0.0 && this.properties.bgWidth <= 0) {
            return;
        }
        GuiElement.bindTexture(this.properties.texture);
        double x = this.x - this.properties.hoverXOffset;
        double y = this.y - this.properties.hoverYOffset;
        if (this.properties.bgWidth >= 0) {
            final boolean active = this.isActive(ratio);
            this.gui.drawTexturedRect(x + this.properties.bgXOffset, y + this.properties.bgYOffset, this.properties.bgWidth, this.properties.bgHeight, active ? ((double)this.properties.uBgActive) : ((double)this.properties.uBgInactive), active ? ((double)this.properties.vBgActive) : ((double)this.properties.vBgInactive));
            if (ratio <= 0.0) {
                return;
            }
        }
        ratio = Math.min(ratio, 1.0);
        double u = this.properties.uInner;
        double v = this.properties.vInner;
        double width = this.properties.innerWidth;
        double height = this.properties.innerHeight;
        final double size = this.properties.vertical ? height : width;
        double renderSize = ratio * size;
        if (!this.properties.smooth) {
            renderSize = (double)Math.round(renderSize);
        }
        if (renderSize <= 0.0) {
            return;
        }
        if (this.properties.vertical) {
            if (this.properties.reverse) {
                v += height - renderSize;
                y += height - renderSize;
            }
            height = renderSize;
        }
        else {
            if (this.properties.reverse) {
                u += width - renderSize;
                x += width - renderSize;
            }
            width = renderSize;
        }
        this.gui.drawTexturedRect(x, y, width, height, u, v);
    }
    
    public static class GaugePropertyBuilder
    {
        private final short uInner;
        private final short vInner;
        private final short innerWidth;
        private final short innerHeight;
        private short hoverXOffset;
        private short hoverYOffset;
        private short hoverWidth;
        private short hoverHeight;
        private short bgXOffset;
        private short bgYOffset;
        private short bgWidth;
        private short bgHeight;
        private short uBgInactive;
        private short vBgInactive;
        private short uBgActive;
        private short vBgActive;
        private final boolean vertical;
        private final boolean reverse;
        private boolean smooth;
        private ResourceLocation texture;
        
        public GaugePropertyBuilder(final int uInner, final int vInner, final int innerWidth, final int innerHeight, final GaugeOrientation dir) {
            this.smooth = true;
            this.texture = GuiElement.commonTexture;
            this.uInner = toShort(uInner);
            this.vInner = toShort(vInner);
            final short short1 = toShort(innerWidth);
            this.hoverWidth = short1;
            this.innerWidth = short1;
            final short short2 = toShort(innerHeight);
            this.hoverHeight = short2;
            this.innerHeight = short2;
            this.vertical = dir.vertical;
            this.reverse = dir.reverse;
        }
        
        public GaugePropertyBuilder withHoverBorder(final int border) {
            this.hoverXOffset = toShort(-border);
            this.hoverYOffset = toShort(-border);
            this.hoverWidth = toShort(this.innerWidth + 2 * border);
            this.hoverHeight = toShort(this.innerHeight + 2 * border);
            return this;
        }
        
        public GaugePropertyBuilder withHover(final int hoverXOffset, final int hoverYOffset, final int hoverWidth, final int hoverHeight) {
            this.hoverXOffset = toShort(hoverXOffset);
            this.hoverYOffset = toShort(hoverYOffset);
            this.hoverWidth = toShort(hoverWidth);
            this.hoverHeight = toShort(hoverHeight);
            return this;
        }
        
        public GaugePropertyBuilder withBackground(final int uBg, final int vBg) {
            return this.withBackground(0, 0, this.innerWidth, this.innerHeight, uBg, vBg);
        }
        
        public GaugePropertyBuilder withBackground(final int bgXOffset, final int bgYOffset, final int bgWidth, final int bgHeight, final int uBg, final int vBg) {
            return this.withBackground(bgXOffset, bgYOffset, bgWidth, bgHeight, uBg, vBg, uBg, vBg);
        }
        
        public GaugePropertyBuilder withBackground(final int uBgInactive, final int vBgInactive, final int uBgActive, final int vBgActive) {
            return this.withBackground(0, 0, this.innerWidth, this.innerHeight, uBgInactive, vBgInactive, uBgActive, vBgActive);
        }
        
        public GaugePropertyBuilder withBackground(final int bgXOffset, final int bgYOffset, final int bgWidth, final int bgHeight, final int uBgInactive, final int vBgInactive, final int uBgActive, final int vBgActive) {
            this.bgXOffset = toShort(bgXOffset);
            this.bgYOffset = toShort(bgYOffset);
            this.bgWidth = toShort(bgWidth);
            this.bgHeight = toShort(bgHeight);
            this.uBgInactive = toShort(uBgInactive);
            this.vBgInactive = toShort(vBgInactive);
            this.uBgActive = toShort(uBgActive);
            this.vBgActive = toShort(vBgActive);
            return this;
        }
        
        public GaugePropertyBuilder withSmooth(final boolean smooth) {
            this.smooth = smooth;
            return this;
        }
        
        public GaugePropertyBuilder withTexture(final ResourceLocation texture) {
            this.texture = texture;
            return this;
        }
        
        public GaugeProperties build() {
            return new GaugeProperties(this.uInner, this.vInner, this.innerWidth, this.innerHeight, this.hoverXOffset, this.hoverYOffset, this.hoverWidth, this.hoverHeight, this.bgXOffset, this.bgYOffset, this.bgWidth, this.bgHeight, this.uBgInactive, this.vBgInactive, this.uBgActive, this.vBgActive, this.vertical, this.reverse, this.smooth, this.texture);
        }
        
        private static short toShort(final int value) {
            return (short)value;
        }
        
        public enum GaugeOrientation
        {
            Up(true, true), 
            Down(true, false), 
            Left(false, true), 
            Right(false, false);
            
            final boolean vertical;
            final boolean reverse;
            
            private GaugeOrientation(final boolean vertical, final boolean reverse) {
                this.vertical = vertical;
                this.reverse = reverse;
            }
        }
    }
    
    public static class GaugeProperties
    {
        public final short uInner;
        public final short vInner;
        public final short innerWidth;
        public final short innerHeight;
        public final short hoverXOffset;
        public final short hoverYOffset;
        public final short hoverWidth;
        public final short hoverHeight;
        public final short bgXOffset;
        public final short bgYOffset;
        public final short bgWidth;
        public final short bgHeight;
        public final short uBgInactive;
        public final short vBgInactive;
        public final short uBgActive;
        public final short vBgActive;
        public final boolean vertical;
        public final boolean reverse;
        public final boolean smooth;
        public final ResourceLocation texture;
        
        public GaugeProperties(final int uInner, final int vInner, final int innerWidth, final int innerHeight, final int hoverXOffset, final int hoverYOffset, final int hoverWidth, final int hoverHeight, final int bgXOffset, final int bgYOffset, final int bgWidth, final int bgHeight, final int uBgInactive, final int vBgInactive, final int uBgActive, final int vBgActive, final boolean vertical, final boolean reverse, final boolean smooth, final ResourceLocation texture) {
            this.uInner = (short)uInner;
            this.vInner = (short)vInner;
            this.innerWidth = (short)innerWidth;
            this.innerHeight = (short)innerHeight;
            this.hoverXOffset = (short)hoverXOffset;
            this.hoverYOffset = (short)hoverYOffset;
            this.hoverWidth = (short)hoverWidth;
            this.hoverHeight = (short)hoverHeight;
            this.bgXOffset = (short)bgXOffset;
            this.bgYOffset = (short)bgYOffset;
            this.bgWidth = (short)bgWidth;
            this.bgHeight = (short)bgHeight;
            this.uBgInactive = (short)uBgInactive;
            this.vBgInactive = (short)vBgInactive;
            this.uBgActive = (short)uBgActive;
            this.vBgActive = (short)vBgActive;
            this.vertical = vertical;
            this.reverse = reverse;
            this.smooth = smooth;
            this.texture = texture;
        }
    }
    
    public enum GaugeStyle implements IGaugeStyle
    {
        Fuel(new GaugePropertyBuilder(112, 80, 13, 13, GaugePropertyBuilder.GaugeOrientation.Up).withHover(0, 0, 14, 14).withBackground(0, 0, 16, 16, 96, 80).build()), 
        Bucket(new GaugePropertyBuilder(110, 111, 14, 16, GaugePropertyBuilder.GaugeOrientation.Up).withBackground(96, 111).build()), 
        ProgressWind(new GaugePropertyBuilder(242, 91, 13, 13, GaugePropertyBuilder.GaugeOrientation.Up).withBackground(242, 63, 242, 77).build()), 
        ProgressArrow(new GaugePropertyBuilder(165, 16, 22, 15, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-5, 0, 32, 16, 160, 0).build()), 
        ProgressArrowModern(new GaugePropertyBuilder(86, 234, 16, 10, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(0, 0, 16, 10, 70, 234).build()), 
        ProgressArrowModernReversed(new GaugePropertyBuilder(70, 244, 16, 10, GaugePropertyBuilder.GaugeOrientation.Left).withBackground(0, 0, 16, 10, 86, 244).build()), 
        ProgressCrush(new GaugePropertyBuilder(165, 52, 21, 11, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-5, -3, 32, 16, 160, 32).build()), 
        ProgressTriangle(new GaugePropertyBuilder(165, 80, 22, 15, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-5, 0, 32, 16, 160, 64).build()), 
        ProgressDrop(new GaugePropertyBuilder(165, 112, 22, 15, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-5, 0, 32, 16, 160, 96).build()), 
        ProgressRecycler(new GaugePropertyBuilder(133, 80, 18, 15, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-5, 0, 32, 16, 128, 64).build()), 
        ProgressMetalFormer(new GaugePropertyBuilder(200, 19, 46, 9, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-8, -3, 64, 16, 192, 0).build()), 
        ProgressCentrifuge(new GaugePropertyBuilder(252, 33, 3, 28, GaugePropertyBuilder.GaugeOrientation.Up).withBackground(-1, -1, 5, 30, 246, 32).build()), 
        HeatCentrifuge(new GaugePropertyBuilder(225, 54, 20, 4, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-1, -1, 22, 6, 224, 47).build()), 
        HeatNuclearReactor(new GaugePropertyBuilder(0, 243, 100, 13, GaugePropertyBuilder.GaugeOrientation.Right).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUINuclearReactor.png")).build()), 
        HeatSteamGenerator(new GaugePropertyBuilder(177, 1, 7, 76, GaugePropertyBuilder.GaugeOrientation.Up).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUISteamGenerator.png")).build()), 
        CalcificationSteamGenerator(new GaugePropertyBuilder(187, 1, 7, 58, GaugePropertyBuilder.GaugeOrientation.Up).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUISteamGenerator.png")).build()), 
        ProgressCondenser(new GaugePropertyBuilder(1, 185, 82, 7, GaugePropertyBuilder.GaugeOrientation.Right).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUICondenser.png")).build()), 
        HeatFermenter(new GaugePropertyBuilder(177, 10, 40, 3, GaugePropertyBuilder.GaugeOrientation.Right).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUIFermenter.png")).build()), 
        ProgressFermenter(new GaugePropertyBuilder(177, 1, 40, 7, GaugePropertyBuilder.GaugeOrientation.Right).withHoverBorder(1).withTexture(new ResourceLocation("ic2", "textures/gui/GUIFermenter.png")).build()), 
        ProgressOreWasher(new GaugePropertyBuilder(177, 118, 18, 18, GaugePropertyBuilder.GaugeOrientation.Right).withTexture(new ResourceLocation("ic2", "textures/gui/GUIOreWashingPlant.png")).withBackground(-1, -1, 20, 19, 102, 38).build()), 
        ProgressBlockCutter(new GaugePropertyBuilder(176, 15, 46, 17, GaugePropertyBuilder.GaugeOrientation.Right).withTexture(new ResourceLocation("ic2", "textures/gui/GUIBlockCutter.png")).withBackground(55, 33).build()), 
        ProgressLongArrow(new GaugePropertyBuilder(176, 15, 34, 13, GaugePropertyBuilder.GaugeOrientation.Right).withTexture(new ResourceLocation("ic2", "textures/gui/GUI_Canner_Classic.png")).withBackground(74, 36).build());
        
        private static final Map<String, IGaugeStyle> map;
        private final String name;
        public final GaugeProperties properties;
        
        private GaugeStyle(final GaugeProperties properties) {
            this.name = this.name().toLowerCase(Locale.ENGLISH);
            this.properties = properties;
        }
        
        @Override
        public GaugeProperties getProperties() {
            return this.properties;
        }
        
        public static void addStyle(final String name, final IGaugeStyle style) {
            assert name != null : "Cannot add null name";
            assert style != null : "Cannot add null style";
            if (GaugeStyle.map.containsKey(name)) {
                throw new RuntimeException("Duplicate style name for " + name + '!');
            }
            GaugeStyle.map.put(name, style);
        }
        
        public static IGaugeStyle get(final String name) {
            return GaugeStyle.map.get(name);
        }
        
        private static Map<String, IGaugeStyle> getMap() {
            final GaugeStyle[] values = values();
            final Map<String, IGaugeStyle> ret = new HashMap<String, IGaugeStyle>(values.length);
            for (final GaugeStyle style : values) {
                ret.put(style.name, style);
            }
            return ret;
        }
        
        static {
            map = getMap();
        }
    }
    
    public interface IGaugeStyle
    {
        GaugeProperties getProperties();
    }
}
