// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

public class EnumCycleHandler<E extends Enum<E>> extends CycleHandler
{
    protected final E[] set;
    
    public EnumCycleHandler(final E[] set) {
        this(set, set[0]);
    }
    
    public EnumCycleHandler(final E[] set, final E start) {
        this(0, 0, 0, 0, 0, false, set, start);
    }
    
    public EnumCycleHandler(final int uS, final int vS, final int uE, final int vE, final int overlayStep, final boolean vertical, final E[] set, final E start) {
        super(uS, vS, uE, vE, overlayStep, vertical, set.length, new INumericValueHandler() {
            private E currentValue = start;
            private final int[] index = this.makeIndexMap();
            
            private int[] makeIndexMap() {
                final int[] ret = new int[Collections.max((Collection<? extends Enum>)Arrays.asList((Enum[])set)).ordinal() + 1];
                for (int index = 0; index < set.length; ++index) {
                    ret[set[index].ordinal()] = index;
                }
                return ret;
            }
            
            @Override
            public void onChange(final int value) {
                assert value >= 0 && value < set.length;
                this.currentValue = set[value];
            }
            
            @Override
            public int getValue() {
                return this.index[this.currentValue.ordinal()];
            }
        });
        this.set = set;
    }
    
    public E getCurrentValue() {
        return this.set[this.getValue()];
    }
}
