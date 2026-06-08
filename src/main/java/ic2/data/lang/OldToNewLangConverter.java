package ic2.data.lang;

import com.google.common.base.CaseFormat;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public class OldToNewLangConverter implements DataProvider
{
	private final DataGenerator generator;
	private final Map<String, String> mapping = createMappings();

	public OldToNewLangConverter(DataGenerator generator)
	{
		this.generator = generator;
	}

	public String getName()
	{
		return "IC2 Localization";
	}

	public boolean isValidGuiLang(String oldKey)
	{
		return oldKey.contains("gui") && !oldKey.contains(".name")
			|| oldKey.contains("SteamRepressurizer") && !oldKey.contains("config")
			|| oldKey.contains("personalTrader")
			|| oldKey.startsWith("dir.");
	}

	public String convertPainterKey(String oldKey)
	{
		String color = oldKey.split("\\.")[1];
		return "item.ic2." + color + "_painter";
	}

	public void run(CachedOutput cache) throws IOException
	{
		Path outputPath = this.generator.getOutputFolder();
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Path oldLangPath = outputPath.getParent().resolve("resources").resolve("assets/ic2/lang_ic2");
		Files.walk(oldLangPath, 1)
			.forEach(
				path ->
				{
					if (!Files.isDirectory(path))
					{
						try
						{
							String language = path.getFileName().toString().replace(".properties", "");
							Path oldFile = oldLangPath.resolve(language + ".properties");
							Path newFile = outputPath.resolve("assets/ic2/lang/" + language.toLowerCase(Locale.ROOT) + ".json");
							Properties oldLang = new Properties();
							oldLang.load(Files.newBufferedReader(oldFile));
							List<Pair<String, String>> list = new ArrayList<>();
							NewLangGenerator newLangGenerator = new NewLangGenerator(language, list);
							String[] guiTools = new String[] { "meter", "tool_box", "advanced_scanner", "scanner" };
							oldLang.forEach(
								(o1, o2) ->
								{
									String oldKey = (String) o1;
									String translation = (String) o2;
									if (oldKey.contains("energy_o_mat") || oldKey.contains("uu_assembly_bench"))
									{
										list.add(Pair.of("container.ic2." + oldKey.replace("te.", ""), translation));
									}

									if (oldKey.equals("te.mfsu") || oldKey.equals("te.mfe") || oldKey.equals("te.electrolyzer"))
									{
										list.add(Pair.of("block.ic2.classic_" + oldKey.replace("te.", ""), translation));
										list.add(Pair.of("container.ic2.classic_" + oldKey.replace("te.", ""), translation));
									}

									if (oldKey.contains("te.nuke"))
									{
										list.add(Pair.of("block.ic2.classic_" + oldKey.replace("te.", ""), translation));
									}

									if (oldKey.startsWith("itemToolMEter."))
									{
										list.add(Pair.of("ic2.meter." + oldKey.replace("itemToolMEter.", ""), translation));
									}

									if (Arrays.asList(guiTools).contains(oldKey))
									{
										String newKey = "container.ic2." + oldKey;
										list.add(Pair.of(newKey, translation));
									}

									if (this.mapping.containsKey(oldKey))
									{
										String newKey = this.mapping.get(oldKey);
										if (!newLangGenerator.overrideOldTranslation(oldKey, newKey, translation))
										{
											list.add(Pair.of(newKey, translation));
										}
									} else if (oldKey.startsWith("tooltip.")
										|| oldKey.startsWith("generic.")
										|| oldKey.startsWith("wind_meter.")
										|| this.isValidGuiLang(oldKey))
									{
										list.add(Pair.of("ic2." + oldKey, translation));
									} else if (oldKey.startsWith("achievement."))
									{
										try
										{
											String name = oldKey.split("[.]", 2)[1];
											list.add(Pair.of("advancements.ic2." + name, translation));
										} catch (ArrayIndexOutOfBoundsException var9x)
										{
										}
									} else if (oldKey.startsWith("painter."))
									{
										list.add(Pair.of(this.convertPainterKey(oldKey), translation));
									}
								}
							);
							newLangGenerator.generate();
							list.removeIf(pairx -> pairx.left().startsWith("item.") && !Registry.ITEM.containsKey(extractIdentifier(pairx.left())));
							list.removeIf(pairx -> pairx.left().startsWith("block.") && !Registry.BLOCK.containsKey(extractIdentifier(pairx.left())));
							list.addAll(
								list.stream()
									.filter(pairx -> pairx.left().startsWith("block.") && pairx.left().split("[.]").length == 3)
									.filter(pairx ->
									{
										ResourceLocation id = extractIdentifier(pairx.left());
										boolean hasContainer = this.mapping.containsKey("container.ic2." + id.getPath());
										return !hasContainer;
									})
									.map(pairx -> Pair.of("container.ic2." + extractIdentifier(pairx.left()).getPath(), pairx.right()))
									.filter(
										pairx ->
										{
											if (Registry.MENU.containsKey(extractIdentifier(pairx.left())))
											{
												return true;
											}

											String[] extraContainers = new String[] {
												"mfe",
												"mfsu",
												"rt_generator",
												"lv_transformer",
												"mv_transformer",
												"hv_transformer",
												"ev_transformer",
												"tank",
												"batbox_chargepad",
												"cesu_chargepad",
												"mfe_chargepad",
												"mfsu_chargepad"
											};

											for (String container : extraContainers)
											{
												if (pairx.left().contains(container))
												{
													return true;
												}
											}

											return false;
										}
									)
									.toList()
							);
							list.sort(
								((Comparator<Pair<String, String>>) (o1, o2) -> tryCompareByRawId(o1, o2, "block", Registry.BLOCK))
									.thenComparing((o1, o2) -> tryCompareByRawId(o1, o2, "item", Registry.ITEM))
									.thenComparing((o1, o2) -> tryCompareByRawId(o1, o2, "container", Registry.BLOCK))
									.thenComparing(pairx -> pairx.left())
							);
							JsonObject newLang = new JsonObject();

							for (Pair<String, String> pair : list)
							{
								newLang.addProperty(pair.left(), pair.right());
							}

							writeToPath(cache, newLang, newFile);
						} catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
				}
			);
	}

	private static void writeToPath(CachedOutput writer, JsonElement json, Path path) throws IOException
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		HashingOutputStream hashingStream = new HashingOutputStream(Hashing.sha1(), byteStream);
		Writer writer2 = new OutputStreamWriter(hashingStream, StandardCharsets.UTF_8);
		JsonWriter jsonWriter = new JsonWriter(writer2);
		jsonWriter.setSerializeNulls(false);
		jsonWriter.setIndent("  ");
		GsonHelper.writeValue(jsonWriter, json, null);
		jsonWriter.close();
		writer.writeIfNeeded(path, byteStream.toByteArray(), hashingStream.hash());
	}

	private static Map<String, String> createMappings()
	{
		Map<String, String> map = new HashMap<>();
		map.put("itemGroup.IC2", "itemGroup.ic2.general");
		String[] resources = new String[] {
			"basalt",
			"copper_ore",
			"lead_ore",
			"tin_ore",
			"uranium_ore",
			"bronze_block",
			"copper_block",
			"lead_block",
			"silver_block",
			"steel_block",
			"tin_block",
			"uranium_block",
			"reinforced_stone",
			"machine",
			"advanced_machine",
			"reactor_vessel"
		};

		for (String resource : resources)
		{
			map.put("resource." + resource, "block.ic2." + resource);
		}

		String[] miscResources = new String[] { "ashes", "iridium_ore", "iridium_shard", "matter", "resin", "slag", "iodine", "water_sheet", "lava_sheet" };

		for (String resource : miscResources)
		{
			map.put("misc_resource." + resource, "item.ic2." + resource);
		}

		String[] materials = new String[] {
			"stone",
			"lead",
			"obsidian",
			"lapis",
			"lithium",
			"sulfur",
			"silicon_dioxide",
			"diamond",
			"energium",
			"coal",
			"coal_fuel",
			"iron",
			"gold",
			"copper",
			"tin",
			"tin_hydrated",
			"bronze",
			"clay",
			"silver",
			"netherrack",
			"alloy",
			"uranium",
			"refined_iron",
			"steel",
			"hydrated_tin"
		};

		for (String material : materials)
		{
			map.put("crushed." + material, "item.ic2.crushed_" + material);
			map.put("purified." + material, "item.ic2.purified_" + material);
			map.put("plate." + material, "item.ic2." + material + "_plate");
			map.put("plate.dense_" + material, "item.ic2.dense_" + material + "_plate");
			map.put("casing." + material, "item.ic2." + material + "_casing");
			map.put("ingot." + material, "item.ic2." + material + "_ingot");
			map.put("dust." + material, "item.ic2." + material + "_dust");
			map.put("dust.small_" + material, "item.ic2.small_" + material + "_dust");
			map.put("itemCrushed" + toCamelCase(material) + "Ore", "item.ic2.crushed_" + material);
			map.put("itemPurifiedCrushed" + toCamelCase(material) + "Ore", "item.ic2.purified_" + material);
			map.put("itemPlate" + toCamelCase(material), "item.ic2." + material + "_plate");
			map.put("itemDensePlate" + toCamelCase(material), "item.ic2.dense_" + material + "_plate");
			map.put("itemCasing" + toCamelCase(material), "item.ic2." + material + "_casing");
			map.put("itemIngot" + toCamelCase(material), "item.ic2." + material + "_ingot");
			map.put("itemDust" + toCamelCase(material), "item.ic2." + material + "_dust");
			map.put("itemDust" + material, "item.ic2." + material + "_dust");
			map.put("itemDust" + toCamelCase(material) + "Small", "item.ic2.small_" + material + "_dust");
			map.put("blockMetal" + toCamelCase(material), "block.ic2." + material + "_block");
			map.put("blockOre" + toCamelCase(material), "block.ic2." + material + "_ore");
		}

		map.put("itemPlateAdvIron", "item.ic2.steel_plate");
		map.put("itemDensePlateAdvIron", "item.ic2.dense_steel_plate");
		map.put("itemCasingAdvIron", "item.ic2.steel_casing");
		map.put("itemIngotAdvIron", "item.ic2.steel_ingot");
		map.put("blockMetalAdvIron", "block.ic2.steel_block");
		map.put("itemPlateLapi", "item.ic2.lapis_plate");
		map.put("itemDensePlateLapi", "item.ic2.dense_lapis_plate");
		map.put("itemDustLapi", "item.ic2.lapis_dust");
		String[] fluids = new String[] {
			"empty",
			"lava",
			"water",
			"air",
			"hydrated_coal",
			"biomass",
			"coalfuel",
			"biogas",
			"biofuel",
			"electrolyzed_water",
			"coolant",
			"hydration",
			"bio",
			"weed_ex",
			"pahoehoe_lava",
			"distilled_water"
		};

		for (String fluid : fluids)
		{
			map.put("cell." + fluid, "item.ic2." + fluid + "_cell");
			map.put("itemCell" + toCamelCase(fluid), "item.ic2." + fluid + "_cell");
			map.put("fluid" + toCamelCase(fluid), "block.ic2." + fluid);
		}

		String[] items = new String[] {
			"rubber",
			"circuit",
			"advanced_circuit",
			"alloy",
			"scrap",
			"scrap_box",
			"coin",
			"carbon_fibre",
			"carbon_mesh",
			"carbon_plate",
			"iridium",
			"coal_ball",
			"coal_block",
			"coal_chunk",
			"industrial_diamond",
			"fertilizer",
			"bio_chaff",
			"heat_conductor",
			"copper_boiler",
			"cf_powder",
			"pellet",
			"coil",
			"electric_motor",
			"power_unit",
			"small_power_unit",
			"raw_crystal_memory",
			"bronze_shaft",
			"iron_shaft",
			"steel_shaft",
			"steam_turbine",
			"steam_turbine_blade",
			"wood_rotor_blade",
			"bronze_rotor_blade",
			"iron_rotor_blade",
			"steel_rotor_blade",
			"carbon_rotor_blade",
			"empty_fuel_can",
			"compressed_hydrated_coal",
			"plant_ball",
			"compressed_plants",
			"tin_can",
			"crystal_memory",
			"fuel_rod",
			"wooden_rotor_blade",
			"jetpack_attachment_plate"
		};

		for (String item : items)
		{
			map.put("crafting." + item, "item.ic2." + item);
			map.put("item" + toCamelCase(item), "item.ic2." + item);
			map.put("item" + item.replaceAll("_", ""), "item.ic2." + item);
		}

		map.put("crystal_memory", "item.ic2.crystal_memory");
		map.put("item.CrystalMemory.tooltip.Empty", "item.ic2.crystal_memory.tooltip.empty");
		map.put("item.CrystalMemory.tooltip.Item", "item.ic2.crystal_memory.tooltip.item");
		map.put("item.CrystalMemory.tooltip.Energy", "item.ic2.crystal_memory.tooltip.energy");
		map.put("item.CrystalMemory.tooltip.UU-Matter", "item.ic2.crystal_memory.tooltip.uu_matter");
		String[] cropResources = new String[] { "fertilizer", "coffee_beans", "coffee_powder", "hops", "grin_powder", "weed" };

		for (String item : cropResources)
		{
			map.put("crop_res." + item, "item.ic2." + item);
			map.put("item" + toCamelCase(item), "item.ic2." + item);
			map.put("item" + item.replaceAll("_", ""), "item.ic2." + item);
		}

		String[] armorMaterials = new String[] { "bronze", "alloy", "hazmat", "rubber", "static", "solar", "nano", "quantum" };

		for (String armorMaterial : armorMaterials)
		{
			map.put(armorMaterial + "_helmet", "item.ic2." + armorMaterial + "_helmet");
			map.put(armorMaterial + "_chestplate", "item.ic2." + armorMaterial + "_chestplate");
			map.put(armorMaterial + "_leggings", "item.ic2." + armorMaterial + "_leggings");
			map.put(armorMaterial + "_boots", "item.ic2." + armorMaterial + "_boots");
			map.put("itemArmor" + toCamelCase(armorMaterial) + "Helmet", "item.ic2." + armorMaterial + "_helmet");
			map.put("itemArmor" + toCamelCase(armorMaterial) + "Chestplate", "item.ic2." + armorMaterial + "_chestplate");
			map.put("itemArmor" + toCamelCase(armorMaterial) + "Leggings", "item.ic2." + armorMaterial + "_leggings");
			map.put("itemArmor" + toCamelCase(armorMaterial) + "Boots", "item.ic2." + armorMaterial + "_boots");
		}

		map.put("cf_pack", "item.ic2.cf_pack");
		map.put("nightvision_goggles", "item.ic2.nightvision_goggles");
		map.put("jetpack", "item.ic2.jetpack");
		map.put("itemArmorJetpack", "item.ic2.jetpack");
		map.put("itemArmorJetpackElectric", "item.ic2.electric_jetpack");
		map.put("itemArmorRubBoots", "item.ic2.rubber_boots");
		map.put("itemArmorCFPack", "item.ic2.cf_pack");
		map.put("itemNightvisionGoggles", "item.ic2.nightvision_goggles");
		map.put("itemSolarHelmet", "item.ic2.solar_helmet");
		String[] tools = new String[] {
			"drill",
			"diamond_drill",
			"iridium_drill",
			"chainsaw",
			"scanner",
			"advanced_scanner",
			"wrench",
			"wrench_new",
			"forge_hammer",
			"electric_hoe",
			"mining_laser",
			"bronze_pickaxe",
			"bronze_axe",
			"bronze_sword",
			"bronze_shovel",
			"bronze_hoe",
			"cutter",
			"nano_saber",
			"treetap",
			"foam_sprayer",
			"electric_wrench",
			"electric_treetap",
			"frequency_transmitter",
			"debug_item",
			"obscurator",
			"cropnalyzer",
			"wind_meter",
			"lathing_tool",
			"weeding_trowel",
			"tool_box",
			"crowbar",
			"painter",
			"meter",
			"terra_wart",
			"rotor_wood",
			"rotor_bronze",
			"rotor_iron",
			"rotor_steel",
			"rotor_carbon",
			"filled_tin_can",
			"filled_fuel_can"
		};

		for (String tool : tools)
		{
			map.put(tool, "item.ic2." + tool);
			map.put("item" + toCamelCase(tool), "item.ic2." + tool);
			map.put("itemTool" + toCamelCase(tool), "item.ic2." + tool);
		}

		map.put("item.ItemTool.tooltip.UsesLeft", "ic2.tooltip.tool.uses_left");
		String[] components = new String[] {
			"uranium_fuel_rod",
			"dual_uranium_fuel_rod",
			"quad_uranium_fuel_rod",
			"mox_fuel_rod",
			"dual_mox_fuel_rod",
			"quad_mox_fuel_rod",
			"depleted_uranium",
			"depleted_dual_uranium",
			"depleted_quad_uranium",
			"depleted_mox",
			"depleted_dual_mox",
			"depleted_quad_mox",
			"lithium_fuel_rod",
			"tritium_cell",
			"uranium",
			"mox",
			"plutonium",
			"small_plutonium",
			"uranium_235",
			"small_uranium_235",
			"uranium_238",
			"small_uranium_238",
			"uranium_pellet",
			"mox_pellet",
			"rtg_pellet",
			"near_depleted_uranium",
			"depleted_isotope_fuel_rod",
			"re_enriched_uranium",
			"heat_storage",
			"tri_heat_storage",
			"hex_heat_storage",
			"plating",
			"heat_plating",
			"containment_plating",
			"heat_exchanger",
			"reactor_heat_exchanger",
			"component_heat_exchanger",
			"advanced_heat_exchanger",
			"heat_vent",
			"reactor_heat_vent",
			"overclocked_heat_vent",
			"component_heat_vent",
			"advanced_heat_vent",
			"neutron_reflector",
			"iridium_reflector",
			"thick_neutron_reflector",
			"rsh_condensator",
			"lzh_condensator",
			"heatpack"
		};

		for (String component : components)
		{
			map.put(component, "item.ic2." + component);
			map.put("nuclear." + component, "item.ic2." + component);
			map.put("item" + toCamelCase(component), "item.ic2." + component);
			map.put("reactor" + toCamelCase(component), "item.ic2." + component);
		}

		map.put("reactoritem.durability", "ic2.reactoritem.durability");
		String[] upgrades = new String[] {
			"overclocker",
			"transformer",
			"energy_storage",
			"ejector",
			"advanced_ejector",
			"pulling",
			"advanced_pulling",
			"fluid_ejector",
			"fluid_pulling",
			"redstone_inverter",
			"remote_interface"
		};

		for (String upgrade : upgrades)
		{
			map.put("upgrade." + upgrade, "item.ic2." + upgrade + "_upgrade");
			map.put(toCamelCase(upgrade) + "Upgrade", "item.ic2." + upgrade + "_upgrade");
		}

		String[] tfbps = new String[] { "blank", "cultivation", "irrigation", "desertification", "chilling", "flatification", "mushroom" };

		for (String tfbp : tfbps)
		{
			map.put("tfbp." + tfbp, "item.ic2." + tfbp + "_tfbp");
			map.put("itemTFBP" + toCamelCase(tfbp), "item.ic2." + tfbp + "_tfbp");
		}

		String[] baterries = new String[] {
			"single_use_battery",
			"re_battery",
			"advanced_re_battery",
			"energy_crystal",
			"lapotron_crystal",
			"charging_re_battery",
			"advanced_charging_re_battery",
			"charging_energy_crystal",
			"charging_lapotron_crystal"
		};

		for (String battery : baterries)
		{
			map.put(battery, "item.ic2." + battery);
		}

		map.put("itemBatSU", "item.ic2.single_use_battery");
		map.put("itemBatRE", "item.ic2.re_battery");
		map.put("itemBatCrystal", "item.ic2.energy_crystal");
		map.put("itemBatLamaCrystal", "item.ic2.lapotron_crystal");
		String[] mugs = new String[] { "empty", "cold_coffee", "dark_coffee", "coffee" };

		for (String mug : mugs)
		{
			map.put("mug." + mug, "item.ic2." + mug + "_mug");
		}

		map.put("itemMugEmpty", "item.ic2.empty_mug");
		map.put("itemMugCoffee_0", "item.ic2.cold_coffee_mug");
		map.put("itemMugCoffee_1", "item.ic2.dark_coffee_mug");
		map.put("itemMugCoffee_2", "item.ic2.coffee_mug");
		map.put("rubber_wood", "block.ic2.rubber_log");
		map.put("leaves.rubber", "block.ic2.rubber_leaves");
		map.put("sapling.rubber", "block.ic2.rubber_sapling");
		map.put("mining_pipe.pipe", "block.ic2.mining_pipe");
		map.put("scaffold.wood", "block.ic2.wooden_scaffold");
		map.put("scaffold.reinforced_wood", "block.ic2.reinforced_wooden_scaffold");
		map.put("scaffold.iron", "block.ic2.iron_scaffold");
		map.put("scaffold.reinforced_iron", "block.ic2.reinforced_iron_scaffold");
		map.put("fence.iron", "block.ic2.iron_fence");
		map.put("sheet.rubber", "block.ic2.rubber_sheet");
		map.put("sheet.resin", "block.ic2.resin_sheet");
		map.put("sheet.wool", "block.ic2.wool_sheet");
		map.put("glass.reinforced", "block.ic2.reinforced_glass");
		map.put("foam.normal", "block.ic2.foam");
		map.put("foam.reinforced", "block.ic2.reinforced_foam");

		for (DyeColor color : DyeColor.values())
		{
			map.put("wall." + color.getName(), "block.ic2." + color.getName() + "_wall");
		}

		map.put("reinforced_door", "block.ic2.reinforced_door");

		for (String te : new String[] { "itnt", "nuke" })
		{
			map.put("te." + te, "block.ic2." + te);
		}

		for (String cable : new String[] { "copper", "gold", "iron", "tin", "detector", "splitter" })
		{
			map.put("cable." + cable + "_cable", "block.ic2." + cable + "_cable");
			map.put("cable." + cable + "_cable_0", "block.ic2." + cable + "_cable");
			map.put("cable." + cable + "_cable_1", "block.ic2.insulated_" + cable + "_cable");
			map.put("cable." + cable + "_cable_2", "block.ic2.double_insulated_" + cable + "_cable");
			map.put("cable." + cable + "_cable_3", "block.ic2.triple_insulated_" + cable + "_cable");
		}

		map.put("cable.glass_cable", "block.ic2.glass_fibre_cable");
		map.put("cable.tooltip.loss ", "ic2.cable.tooltip.loss");

		for (Block block : Registry.BLOCK)
		{
			ResourceLocation id = Registry.BLOCK.getKey(block);
			if (id.getNamespace().equals("ic2"))
			{
				String name = id.getPath();

				String oldName = switch (name)
				{
					case "uu_scanner" -> "scanner";
					case "classic_cropmatron" -> "cropmatron";
					default -> name;
				};
				map.put("te." + oldName, "block.ic2." + name);
				map.put("block" + toCamelCase(oldName), "block.ic2." + name);

				String alternativeName = switch (oldName)
				{
					case "advanced_miner" -> "AdvMiner";
					case "electric_furnace" -> "ElecFurnace";
					case "induction_furnace" -> "Induction";
					case "matter_generator" -> "Matter";
					case "ore_washing_plant" -> "OreWashingPlan";
					case "crop_harvester" -> "CropHavester";
					case "bat_box" -> "BatBox";
					case "cesu" -> "CESU";
					case "mfe" -> "MFE";
					case "mfsu" -> "MFSU";
					default -> toCamelCase(oldName);
				};
				map.put(alternativeName + ".gui.name", "container.ic2." + name);
				map.put("block" + alternativeName, "block.ic2." + name);
			}
		}

		for (String chargepad : new String[] { "batbox", "cesu", "mfe", "mfsu" })
		{
			map.put("te.chargepad_" + chargepad, "block.ic2." + chargepad + "_chargepad");
			String alternativeName = "batbox".equals(chargepad) ? "BatBox" : chargepad.toUpperCase(Locale.ROOT);
			map.put("blockChargepad" + alternativeName, "block.ic2." + chargepad + "_chargepad");
		}

		map.put("crop_stick", "block.ic2.crop");
		map.put("blockCrop", "block.ic2.crop");
		map.replaceAll((key, value) -> fix(value));
		return map;
	}

	private static String fix(String name)
	{
		return switch (name)
		{
			case "item.ic2.tin_hydrated_dust" -> "item.ic2.hydrated_tin_dust";
			case "item.ic2.alloy_ingot" -> "item.ic2.mixed_metal_ingot";
			case "item.ic2.depleted_uranium" -> "item.ic2.depleted_uranium_fuel_rod";
			case "item.ic2.depleted_dual_uranium" -> "item.ic2.depleted_dual_uranium_fuel_rod";
			case "item.ic2.depleted_quad_uranium" -> "item.ic2.depleted_quad_uranium_fuel_rod";
			case "item.ic2.depleted_mox" -> "item.ic2.depleted_mox_fuel_rod";
			case "item.ic2.depleted_dual_mox" -> "item.ic2.depleted_dual_mox_fuel_rod";
			case "item.ic2.depleted_quad_mox" -> "item.ic2.depleted_quad_mox_fuel_rod";
			case "item.ic2.iridium_reflector" -> "item.ic2.iridium_neutron_reflector";
			case "item.ic2.heat_storage" -> "item.ic2.reactor_coolant_cell";
			case "item.ic2.tri_heat_storage" -> "item.ic2.triple_reactor_coolant_cell";
			case "item.ic2.hex_heat_storage" -> "item.ic2.sextuple_reactor_coolant_cell";
			case "item.ic2.plating" -> "item.ic2.reactor_plating";
			case "item.ic2.heat_plating" -> "item.ic2.reactor_heat_plating";
			case "item.ic2.containment_plating" -> "item.ic2.containment_reactor_plating";
			case "item.ic2.matter" -> "item.ic2.uu_matter";
			case "item.ic2.wood_rotor_blade" -> "item.ic2.wooden_rotor_blade";
			case "item.ic2.rotor_wood" -> "item.ic2.wooden_rotor";
			case "item.ic2.rotor_bronze" -> "item.ic2.bronze_rotor";
			case "item.ic2.rotor_iron" -> "item.ic2.iron_rotor";
			case "item.ic2.rotor_steel" -> "item.ic2.steel_rotor";
			case "item.ic2.rotor_carbon" -> "item.ic2.carbon_rotor";
			default -> name;
		};
	}

	private static String toCamelCase(String string)
	{
		return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(string);
	}

	private static <T> int tryCompareByRawId(Pair<String, String> o1, Pair<String, String> o2, String entryType, Registry<T> registry)
	{
		if (o1.left().startsWith(entryType + ".ic2"))
		{
			if (o2.left().startsWith(entryType + ".ic2"))
			{
				ResourceLocation id1 = extractIdentifier(o1.left());
				ResourceLocation id2 = extractIdentifier(o2.left());
				int item1 = registry.getOptional(id1).map(registry::getId).orElse(Integer.MAX_VALUE);
				int item2 = registry.getOptional(id2).map(registry::getId).orElse(Integer.MAX_VALUE);
				return Integer.compare(item1, item2);
			} else
			{
				return -1;
			}
		} else
		{
			return o2.left().startsWith(entryType + ".ic2") ? 1 : 0;
		}
	}

	private static ResourceLocation extractIdentifier(String translationKey)
	{
		String[] parts = translationKey.split("\\.");
		return ResourceLocation.fromNamespaceAndPath(parts[1], parts[2]);
	}
}
