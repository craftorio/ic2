package ic2.core.gui;

import java.util.Arrays;
import java.util.Collections;

public class EnumCycleHandler<E extends Enum<E>> extends CycleHandler
{
	protected final E[] set;

	public EnumCycleHandler(E[] set)
	{
		this(set, set[0]);
	}

	public EnumCycleHandler(E[] set, E start)
	{
		this(0, 0, 0, 0, 0, false, set, start);
	}

	public EnumCycleHandler(int uS, int vS, int uE, int vE, int overlayStep, boolean vertical, final E[] set, final E start)
	{
		super(uS, vS, uE, vE, overlayStep, vertical, set.length, new INumericValueHandler()
		{
			private E currentValue = start;
			private final int[] index = this.makeIndexMap();

			private int[] makeIndexMap()
			{
				int[] ret = new int[Collections.max(Arrays.asList(set)).ordinal() + 1];
				int index = 0;

				while (index < set.length)
				{
					ret[set[index].ordinal()] = index++;
				}

				return ret;
			}

			@Override
			public void onChange(int value)
			{
				assert value >= 0 && value < set.length;
				this.currentValue = set[value];
			}

			@Override
			public int getValue()
			{
				return this.index[this.currentValue.ordinal()];
			}
		});
		this.set = set;
	}

	public E getCurrentValue()
	{
		return this.set[this.getValue()];
	}
}
