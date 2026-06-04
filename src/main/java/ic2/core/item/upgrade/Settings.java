// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import net.minecraft.nbt.NBTTagCompound;

public class Settings
{
    public final boolean active;
    public final ComparisonType comparison;
    public final String mainBox;
    public final String extraBox;
    public final ComparisonSettings main;
    public final ComparisonSettings extra;
    
    public Settings(final NBTTagCompound nbt) {
        if (!(this.active = nbt.getBoolean("active"))) {
            this.comparison = ComparisonType.IGNORED;
            final String s = "";
            this.extraBox = s;
            this.mainBox = s;
            final ComparisonSettings default1 = ComparisonSettings.DEFAULT;
            this.extra = default1;
            this.main = default1;
        }
        else {
            if (!nbt.hasKey("type", 1)) {
                this.comparison = ComparisonType.DIRECT;
            }
            else {
                this.comparison = ComparisonType.getFromNBT(nbt.getByte("type"));
            }
            switch (this.comparison) {
                case DIRECT: {
                    final String s2 = "";
                    this.extraBox = s2;
                    this.mainBox = s2;
                    final ComparisonSettings default2 = ComparisonSettings.DEFAULT;
                    this.extra = default2;
                    this.main = default2;
                    break;
                }
                case COMPARISON: {
                    this.mainBox = nbt.getString("normal");
                    this.extraBox = "";
                    this.main = ComparisonSettings.getFromNBT(nbt.getByte("normalComp"));
                    this.extra = ComparisonSettings.DEFAULT;
                    break;
                }
                case RANGE: {
                    this.mainBox = nbt.getString("normal");
                    this.extraBox = nbt.getString("extra");
                    this.main = ComparisonSettings.getFromNBT(nbt.getByte("normalComp"));
                    this.extra = ComparisonSettings.getFromNBT(nbt.getByte("extraComp"));
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected comparison type " + this.comparison);
                }
            }
        }
    }
    
    public boolean doComparison(final int value) {
        switch (this.comparison) {
            case COMPARISON: {
                return this.main.compare(Integer.parseInt(this.mainBox), value);
            }
            case RANGE: {
                return this.main.compare(Integer.parseInt(this.mainBox), value) && this.extra.compare(value, Integer.parseInt(this.extraBox));
            }
            default: {
                throw new IllegalStateException("Unexpected comparison type " + this.comparison);
            }
        }
    }
}
