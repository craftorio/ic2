package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum ItemName
{
	boat(ItemFolder.boat, ItemName.PathStyle.FolderAndNameWithPrefix),
	crushed(ItemFolder.resource),
	purified(ItemFolder.resource),
	dust(ItemFolder.resource),
	ingot(ItemFolder.resource),
	plate(ItemFolder.resource),
	casing(ItemFolder.resource),
	nuclear(ItemFolder.resource),
	misc_resource(ItemFolder.resource, ItemName.PathStyle.FolderAndSubName),
	block_cutting_blade(ItemFolder.crafting, ItemName.PathStyle.FolderAndNameWithPrefix),
	crafting(ItemFolder.root),
	crystal_memory(ItemFolder.crafting),
	upgrade_kit(ItemFolder.crafting, ItemName.PathStyle.FolderAndNameWithPrefix),
	crop_res(ItemFolder.crop, ItemName.PathStyle.FolderAndSubName),
	terra_wart(ItemFolder.crop),
	re_battery(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	advanced_re_battery(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	energy_crystal(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	lapotron_crystal(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	single_use_battery(ItemFolder.battery),
	charging_re_battery(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	advanced_charging_re_battery(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	charging_energy_crystal(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	charging_lapotron_crystal(ItemFolder.battery, ItemName.PathStyle.FolderAndNameWithSuffix),
	heat_storage(ItemFolder.reactor),
	tri_heat_storage(ItemFolder.reactor),
	hex_heat_storage(ItemFolder.reactor),
	plating(ItemFolder.reactor),
	heat_plating(ItemFolder.reactor),
	containment_plating(ItemFolder.reactor),
	heat_exchanger(ItemFolder.reactor),
	reactor_heat_exchanger(ItemFolder.reactor),
	component_heat_exchanger(ItemFolder.reactor),
	advanced_heat_exchanger(ItemFolder.reactor),
	heat_vent(ItemFolder.reactor),
	reactor_heat_vent(ItemFolder.reactor),
	overclocked_heat_vent(ItemFolder.reactor),
	component_heat_vent(ItemFolder.reactor),
	advanced_heat_vent(ItemFolder.reactor),
	neutron_reflector(ItemFolder.reactor),
	thick_neutron_reflector(ItemFolder.reactor),
	iridium_reflector(ItemFolder.reactor),
	rsh_condensator(ItemFolder.reactor),
	lzh_condensator(ItemFolder.reactor),
	heatpack(ItemFolder.reactor),
	uranium_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	dual_uranium_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	quad_uranium_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	mox_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	dual_mox_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	quad_mox_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	lithium_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	tritium_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	depleted_isotope_fuel_rod(ItemFolder.reactorFuelRod, ItemName.PathStyle.FolderAndNameM2WithSuffix),
	tfbp(ItemFolder.tfbp, ItemName.PathStyle.FolderAndSubName),
	bronze_axe(ItemFolder.tool),
	bronze_hoe(ItemFolder.tool),
	bronze_pickaxe(ItemFolder.tool),
	bronze_shovel(ItemFolder.tool),
	bronze_sword(ItemFolder.tool),
	containment_box(ItemFolder.tool),
	cutter(ItemFolder.tool),
	debug_item(ItemFolder.tool),
	foam_sprayer(ItemFolder.tool),
	forge_hammer(ItemFolder.tool),
	frequency_transmitter(ItemFolder.tool),
	meter(ItemFolder.tool),
	remote(ItemFolder.tool),
	tool_box(ItemFolder.tool, ItemName.PathStyle.FolderAndNameWithSuffix),
	treetap(ItemFolder.tool),
	wrench(ItemFolder.tool),
	wrench_new(ItemFolder.tool),
	crowbar(ItemFolder.tool),
	barrel(ItemFolder.brewing),
	booze_mug(ItemFolder.brewing, ItemName.PathStyle.FolderAndNameWithSuffix),
	mug(ItemFolder.brewing, ItemName.PathStyle.FolderAndNameWithSuffix),
	crop_stick(ItemFolder.crop),
	cropnalyzer(ItemFolder.crop),
	crop_seed_bag(ItemFolder.crop),
	weeding_trowel(ItemFolder.crop),
	advanced_scanner(ItemFolder.toolElectric),
	chainsaw(ItemFolder.toolElectric),
	diamond_drill(ItemFolder.toolElectric),
	drill(ItemFolder.toolElectric),
	electric_hoe(ItemFolder.toolElectric),
	electric_treetap(ItemFolder.toolElectric),
	electric_wrench(ItemFolder.toolElectric),
	iridium_drill(ItemFolder.toolElectric),
	mining_laser(ItemFolder.toolElectric),
	nano_saber(ItemFolder.toolElectric, ItemName.PathStyle.FolderAndNameWithSuffix),
	obscurator(ItemFolder.toolElectric),
	plasma_launcher(ItemFolder.toolElectric),
	scanner(ItemFolder.toolElectric),
	wind_meter(ItemFolder.toolElectric),
	painter(ItemFolder.toolPainter, ItemName.PathStyle.FolderAndNameWithSuffix),
	fluid_cell(ItemFolder.cell),
	cell(ItemFolder.cell, ItemName.PathStyle.FolderAndNameWithPrefix),
	cable(ItemFolder.cable),
	upgrade(ItemFolder.upgrade, ItemName.PathStyle.FolderAndSubName),
	advanced_batpack(ItemFolder.armor),
	alloy_chestplate(ItemFolder.armor),
	batpack(ItemFolder.armor),
	bronze_boots(ItemFolder.armor),
	bronze_chestplate(ItemFolder.armor),
	bronze_helmet(ItemFolder.armor),
	bronze_leggings(ItemFolder.armor),
	cf_pack(ItemFolder.armor),
	energy_pack(ItemFolder.armor),
	hazmat_chestplate(ItemFolder.armor),
	hazmat_helmet(ItemFolder.armor),
	hazmat_leggings(ItemFolder.armor),
	jetpack(ItemFolder.armor),
	jetpack_electric(ItemFolder.armor),
	lappack(ItemFolder.armor),
	nano_boots(ItemFolder.armor),
	nano_chestplate(ItemFolder.armor),
	nano_helmet(ItemFolder.armor),
	nano_leggings(ItemFolder.armor),
	nightvision_goggles(ItemFolder.armor),
	quantum_boots(ItemFolder.armor),
	quantum_chestplate(ItemFolder.armor),
	quantum_helmet(ItemFolder.armor),
	quantum_leggings(ItemFolder.armor),
	rubber_boots(ItemFolder.armor),
	solar_helmet(ItemFolder.armor),
	static_boots(ItemFolder.armor),
	filled_tin_can(ItemFolder.root),
	filled_fuel_can(ItemFolder.root),
	iodine_tablet(ItemFolder.root),
	rotor_wood(ItemFolder.rotor),
	rotor_bronze(ItemFolder.rotor),
	rotor_iron(ItemFolder.rotor),
	rotor_carbon(ItemFolder.rotor),
	rotor_steel(ItemFolder.rotor),
	dynamite(ItemFolder.root),
	dynamite_sticky(ItemFolder.root),
	pipe(ItemFolder.pipe),
	item_pipe(ItemFolder.pipe),
	cover(ItemFolder.pipe),
	extractor_cover(ItemFolder.pipe),
	coke(ItemFolder.resource),
	ingot2(ItemFolder.resource),
	test_pick(ItemFolder.tool);

	private final ItemFolder folder;
	private final ItemName.PathStyle pathStyle;
	private Item instance;
	public static final ItemName[] values = values();

	ItemName(ItemFolder folder)
	{
		this(folder, ItemName.PathStyle.FolderAndNameAndSubName);
	}

	ItemName(ItemFolder folder, ItemName.PathStyle pathStyle)
	{
		if (folder == null)
		{
			throw new NullPointerException("null folder");
		}

		this.folder = folder;
		this.pathStyle = pathStyle;
	}

	public String getPath(String extraName)
	{
		StringBuilder ret = new StringBuilder();
		if (this.folder.path != null)
		{
			ret.append(this.folder.path);
			ret.append('/');
		}

		if (this.pathStyle == ItemName.PathStyle.FolderAndNameWithPrefix && extraName != null)
		{
			ret.append(extraName);
			ret.append('_');
		}

		if (this.pathStyle != ItemName.PathStyle.FolderAndSubName)
		{
			String name = this.getName();
			if (this.pathStyle == ItemName.PathStyle.FolderAndNameM2WithSuffix)
			{
				int pos = name.lastIndexOf(95, name.lastIndexOf(95) - 1);
				ret.append(name.substring(0, pos));
			} else
			{
				ret.append(name);
			}
		}

		if (this.pathStyle != ItemName.PathStyle.FolderAndNameWithPrefix && extraName != null)
		{
			if (this.pathStyle != ItemName.PathStyle.FolderAndSubName)
			{
				if (this.pathStyle != ItemName.PathStyle.FolderAndNameWithSuffix && this.pathStyle != ItemName.PathStyle.FolderAndNameM2WithSuffix)
				{
					ret.append('/');
				} else
				{
					ret.append('_');
				}
			}

			ret.append(extraName);
		}

		if (ret.length() == 0)
		{
			throw new IllegalArgumentException("empty name for " + this + " (" + this.pathStyle + ") with extraName=" + extraName);
		} else
		{
			return ret.toString();
		}
	}

	private String getName()
	{
		return this.name();
	}

	public boolean hasInstance()
	{
		return this.instance != null;
	}

	public <T extends Item & IItemModelProvider> T getInstance()
	{
		if (this.instance == null)
		{
			throw new IllegalStateException("the requested item instance for " + this.name() + " isn't set (yet)");
		} else
		{
			return (T) this.instance;
		}
	}

	public <T extends Item & IItemModelProvider> void setInstance(T instance)
	{
		if (this.instance != null)
		{
			throw new IllegalStateException("conflicting instance");
		}

		this.instance = instance;
	}

	public ItemStack getItemStack()
	{
		return this.getItemStack((String) null);
	}

	public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(T variant)
	{
		if (this.instance == null)
		{
			return null;
		} else if (this.instance instanceof IMultiItem)
		{
			IMultiItem<T> multiItem = (IMultiItem<T>) this.instance;
			return multiItem.getItemStack(variant);
		} else if (variant == null)
		{
			return new ItemStack(this.instance);
		} else
		{
			throw new IllegalArgumentException("not applicable");
		}
	}

	public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(String variant)
	{
		if (this.instance == null)
		{
			return null;
		} else if (this.instance instanceof IMultiItem)
		{
			IMultiItem<T> multiItem = (IMultiItem<T>) this.instance;
			return multiItem.getItemStack(variant);
		} else if (variant == null)
		{
			return new ItemStack(this.instance);
		} else
		{
			throw new IllegalArgumentException("not applicable");
		}
	}

	public String getVariant(ItemStack stack)
	{
		if (this.instance == null)
		{
			return null;
		} else
		{
			return this.instance instanceof IMultiItem ? ((IMultiItem) this.instance).getVariant(stack) : null;
		}
	}

	private enum PathStyle
	{
		FolderAndNameAndSubName,
		FolderAndSubName,
		FolderAndNameWithPrefix,
		FolderAndNameWithSuffix,
		FolderAndNameM2WithSuffix;
	}
}
