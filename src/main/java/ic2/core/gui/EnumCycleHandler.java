package ic2.core.gui;

import java.util.Arrays;
import java.util.Collections;

public class EnumCycleHandler<E extends Enum<E>> extends CycleHandler {
  protected final E[] set;
  
  public EnumCycleHandler(E[] set) {
    this(set, set[0]);
  }
  
  public EnumCycleHandler(E[] set, E start) {
    this(0, 0, 0, 0, 0, false, set, start);
  }
  
  public EnumCycleHandler(int uS, int vS, int uE, int vE, int overlayStep, boolean vertical, E[] set, E start) {
    super(uS, vS, uE, vE, overlayStep, vertical, set.length, new INumericValueHandler((Enum[])set, (Enum)start) {
          private int[] makeIndexMap() {
            int[] ret = new int[((Enum)Collections.max(Arrays.asList((T[])set))).ordinal() + 1];
            for (int index = 0; index < set.length; index++)
              ret[set[index].ordinal()] = index; 
            return ret;
          }
          
          public void onChange(int value) {
            assert value >= 0 && value < set.length;
            this.currentValue = (E)set[value];
          }
          
          public int getValue() {
            return this.index[this.currentValue.ordinal()];
          }
          
          private E currentValue = (E)start;
          
          private final int[] index = makeIndexMap();
        });
    this.set = set;
  }
  
  public E getCurrentValue() {
    return this.set[getValue()];
  }
}
