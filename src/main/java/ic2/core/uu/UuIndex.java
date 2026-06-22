package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.init.IC2Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class UuIndex
{
	public static final UuIndex instance = new UuIndex();
	protected final List<IRecipeResolver> resolvers = new ArrayList<>();
	protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();

	private UuIndex()
	{
	}

	public void addResolver(IRecipeResolver resolver)
	{
		this.resolvers.add(resolver);
	}

	public void addResolver(ILateRecipeResolver resolver)
	{
		this.lateResolvers.add(resolver);
	}

	public void add(ItemStack stack, double value)
	{
		if (stack != null)
		{
			UuGraph.set(stack, value);
		} else
		{
			throw new NullPointerException("invalid item stack to add");
		}
	}

	public double get(ItemStack request)
	{
		return UuGraph.get(request);
	}

	public double getInBuckets(ItemStack request)
	{
		double ret = UuGraph.get(request);
		return ret * 1.0E-5;
	}

	public void init()
	{
	}

	public void refresh(boolean reset)
	{
		List<? extends String> worldScanList = IC2Config.balance.uuValues.worldScan.get();
		if (worldScanList.isEmpty())
		{
			IC2.log.info(LogCategory.Uu, "Loading predefined UU world scan values, run /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.");
			loadScanValuesFromLegacyIni();
		} else
		{
			IC2.log.debug(LogCategory.Uu, "Loading UU world scan values from the user config.");
			for (String entry : worldScanList)
			{
				parseUuEntry(entry, "world scan");
			}
		}

		for (String entry : IC2Config.balance.uuValues.predefined.get())
		{
			parseUuEntry(entry, "predefined");
		}

		UuGraph.build(reset);
	}

	private void parseUuEntry(String entry, String category)
	{
		int splitPos = entry.lastIndexOf(' ');
		if (splitPos <= 0) return;

		String itemName = entry.substring(0, splitPos);
		String valueStr = entry.substring(splitPos + 1);

		ItemStack stack;
		try
		{
			stack = ConfigUtil.asStack(itemName);
		} catch (ParseException e)
		{
			throw new RuntimeException(e);
		}

		if (stack == null)
		{
			IC2.log.warn(LogCategory.Uu, "UU %s config: Can't find ItemStack for %s.", category, itemName);
			return;
		}

		try
		{
			this.add(stack, Double.parseDouble(valueStr));
		} catch (NumberFormatException e)
		{
			IC2.log.warn(LogCategory.Uu, "UU %s config: Invalid value %s for %s.", category, valueStr, itemName);
		}
	}

	private void loadScanValuesFromLegacyIni()
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
			IC2.class.getResourceAsStream("/assets/ic2/config/uu_scan_values.ini"), StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty() || line.startsWith(";")) continue;

				int eqPos = line.indexOf('=');
				if (eqPos <= 0) continue;

				String itemName = line.substring(0, eqPos).trim();
				String valueStr = line.substring(eqPos + 1).trim();

				ItemStack stack = ConfigUtil.asStack(itemName);
				if (stack != null)
				{
					try
					{
						this.add(stack, Double.parseDouble(valueStr));
					} catch (NumberFormatException e)
					{
						IC2.log.warn(LogCategory.Uu, "UU scan default config: Invalid value %s for %s.", valueStr, itemName);
					}
				} else
				{
					IC2.log.warn(LogCategory.Uu, "UU scan default config: Can't find ItemStack for %s.", itemName);
				}
			}
		} catch (Exception e)
		{
			throw new RuntimeException("Error loading default UU scan values", e);
		}
	}
}
