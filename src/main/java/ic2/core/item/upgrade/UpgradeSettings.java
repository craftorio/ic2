package ic2.core.item.upgrade;

import net.minecraft.nbt.CompoundTag;

public class UpgradeSettings
{
	public final boolean active;
	public final ComparisonType comparison;
	public final String mainBox;
	public final String extraBox;
	public final ComparisonSettings main;
	public final ComparisonSettings extra;

	public UpgradeSettings(CompoundTag nbt)
	{
		this.active = nbt.getBoolean("active");
		if (!this.active)
		{
			this.comparison = ComparisonType.IGNORED;
			this.mainBox = this.extraBox = "";
			this.main = this.extra = ComparisonSettings.DEFAULT;
		} else
		{
			if (!nbt.contains("type", 1))
			{
				this.comparison = ComparisonType.DIRECT;
			} else
			{
				this.comparison = ComparisonType.getFromNBT(nbt.getByte("type"));
			}

			switch (this.comparison)
			{
				case DIRECT:
					this.mainBox = this.extraBox = "";
					this.main = this.extra = ComparisonSettings.DEFAULT;
					break;
				case COMPARISON:
					this.mainBox = nbt.m_128461_("normal");
					this.extraBox = "";
					this.main = ComparisonSettings.getFromNBT(nbt.getByte("normalComp"));
					this.extra = ComparisonSettings.DEFAULT;
					break;
				case RANGE:
					this.mainBox = nbt.m_128461_("normal");
					this.extraBox = nbt.m_128461_("extra");
					this.main = ComparisonSettings.getFromNBT(nbt.getByte("normalComp"));
					this.extra = ComparisonSettings.getFromNBT(nbt.getByte("extraComp"));
					break;
				default:
					throw new IllegalStateException("Unexpected comparison type " + this.comparison);
			}
		}
	}

	public boolean doComparison(int value)
	{
		switch (this.comparison)
		{
			case COMPARISON:
				return this.main.compare(Integer.parseInt(this.mainBox), value);
			case RANGE:
				return this.main.compare(Integer.parseInt(this.mainBox), value) && this.extra.compare(value, Integer.parseInt(this.extraBox));
			default:
				throw new IllegalStateException("Unexpected comparison type " + this.comparison);
		}
	}
}
