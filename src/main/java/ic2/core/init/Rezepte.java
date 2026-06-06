package ic2.core.init;

import ic2.api.recipe.ICraftingRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.block.machine.EmptyFluidContainerRecipeManager;
import ic2.core.block.machine.FillFluidContainerRecipeManager;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.MiscResourceType;
import ic2.core.profile.ProfileManager;
import ic2.core.recipe.AdvCraftingRecipeManager;
import ic2.core.recipe.AdvRecipe;
import ic2.core.recipe.AdvShapelessRecipe;
import ic2.core.recipe.ArmorDyeingRecipe;
import ic2.core.recipe.ColourCarryingRecipe;
import ic2.core.recipe.GradualRecipe;
import ic2.core.recipe.RecipeInputItemStack;
import ic2.core.recipe.RecipeInputOreDict;
import ic2.core.recipe.SmeltingRecipeManager;
import ic2.core.ref.ItemName;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BooleanSupplier;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.commons.lang3.ArrayUtils;

public class Rezepte
{
	private static int recipeID;
	private static List<IRecipeInput> disabledRecipeOutputs;
	private static final Queue<BooleanSupplier> pendingRecipes = new ArrayDeque<>();

	public static void registerWithSorter()
	{
		RecipeSorter.Category shaped = RecipeSorter.Category.SHAPED;
		RecipeSorter.Category shapeless = RecipeSorter.Category.SHAPELESS;
		RecipeSorter.register("ic2:shaped", AdvRecipe.class, shaped, "after:minecraft:shapeless");
		RecipeSorter.register("ic2:shapeless", AdvShapelessRecipe.class, shapeless, "after:ic2:shaped");
		RecipeSorter.register("ic2:gradual", GradualRecipe.class, shapeless, "after:ic2:shapeless");
		RecipeSorter.register("ic2:armorDyeing", ArmorDyeingRecipe.class, shapeless, "after:ic2:shapeless");
		RecipeSorter.register("ic2:colourCarrying", ColourCarryingRecipe.class, shaped, "after:ic2:shaped");
		RecipeSorter.register("ic2:jetpackAttachement", JetpackAttachmentRecipe.class, shapeless, "before:minecraft:shapeless");
	}

	static void loadRecipes()
	{
		Recipes.advRecipes = new AdvCraftingRecipeManager();
		Recipes.furnace = new SmeltingRecipeManager();
		Recipes.emptyFluidContainer = new EmptyFluidContainerRecipeManager();
		Recipes.fillFluidContainer = new FillFluidContainerRecipeManager();
		Config shapedRecipes = new Config("shaped recipes");
		Config shapelessRecipes = new Config("shapeless recipes");
		Config uuRecipes = new Config("uu recipes");
		Config furnaceRecipes = new Config("furnace recipes");
		Config blastfurnace = new Config("blast furnace recipes");
		Config blockCutter = new Config("block cutter recipes");
		Config compressor = new Config("compressor recipes");
		Config extractor = new Config("extractor recipes");
		Config macerator = new Config("macerator recipes");
		Config mfcutting = new Config("metal former cutting recipes");
		Config mfextruding = new Config("metal former extruding recipes");
		Config mfrolling = new Config("metal former rolling recipes");
		Config oreWashing = new Config("ore washing recipes");
		Config centrifuge = new Config("thermal centrifuge recipes");

		try
		{
			shapedRecipes.load(getConfigFile("shaped_recipes"));
			shapelessRecipes.load(getConfigFile("shapeless_recipes"));
			uuRecipes.load(getConfigFile("uu_recipes"));
			furnaceRecipes.load(getConfigFile("furnace"));
			blastfurnace.load(getConfigFile("blast_furnace"));
			blockCutter.load(getConfigFile("block_cutter"));
			compressor.load(getConfigFile("compressor"));
			extractor.load(getConfigFile("extractor"));
			macerator.load(getConfigFile("macerator"));
			mfcutting.load(getConfigFile("metal_former_cutting"));
			mfextruding.load(getConfigFile("metal_former_extruding"));
			mfrolling.load(getConfigFile("metal_former_rolling"));
			oreWashing.load(getConfigFile("ore_washer"));
			centrifuge.load(getConfigFile("thermal_centrifuge"));
		} catch (Exception e)
		{
			IC2.log.warn(LogCategory.Recipe, e, "Recipe loading failed.");
		}

		disabledRecipeOutputs = ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/disable");
		if (!MainConfig.get().get("recipes/allowCoinCrafting").getBool())
		{
			disabledRecipeOutputs.add(Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.coin)));
		}

		loadCraftingRecipes(shapedRecipes, true);
		loadCraftingRecipes(shapelessRecipes, false);
		loadUuRecipes(uuRecipes);
		loadMachineRecipes(furnaceRecipes, SmeltingRecipeManager.SmeltingBridge.INSTANCE, Rezepte.MachineType.Furnace);
		loadMachineRecipes(blastfurnace, Recipes.blastfurnace, Rezepte.MachineType.BlastFurnace);
		loadMachineRecipes(blockCutter, Recipes.blockcutter, Rezepte.MachineType.BlockCutter);
		loadMachineRecipes(compressor, Recipes.compressor, Rezepte.MachineType.Normal);
		loadMachineRecipes(extractor, Recipes.extractor, Rezepte.MachineType.Normal);
		loadMachineRecipes(macerator, Recipes.macerator, Rezepte.MachineType.Normal);
		loadMachineRecipes(mfcutting, Recipes.metalformerCutting, Rezepte.MachineType.Normal);
		loadMachineRecipes(mfextruding, Recipes.metalformerExtruding, Rezepte.MachineType.Normal);
		loadMachineRecipes(mfrolling, Recipes.metalformerRolling, Rezepte.MachineType.Normal);
		loadMachineRecipes(oreWashing, Recipes.oreWashing, Rezepte.MachineType.OreWashingPlant);
		loadMachineRecipes(centrifuge, Recipes.centrifuge, Rezepte.MachineType.ThermalCentrifuge);
		IC2.log.debug(LogCategory.Recipe, "%d recipes failed to load in the first pass.", pendingRecipes.size());
	}

	public static void loadFailedRecipes()
	{
		int amount = pendingRecipes.size();
		int successful = 0;

		BooleanSupplier recipe;
		while ((recipe = pendingRecipes.poll()) != null)
		{
			if (recipe.getAsBoolean())
			{
				successful++;
			}
		}

		try
		{
			Config blockCutter = new Config("block cutter late recipes");
			blockCutter.load(getConfigFile("block_cutter_late"));

			for (Iterator<Config.Value> it = blockCutter.valueIterator(); it.hasNext(); amount++)
			{
				if (loadMachineRecipe(it.next(), Recipes.blockcutter, Rezepte.MachineType.BlockCutter, true))
				{
					successful++;
				}
			}
		} catch (Exception e)
		{
			IC2.log.warn(LogCategory.Recipe, e, "Late recipe loading failed.");
		}

		IC2.log.debug(LogCategory.Recipe, "Successfully loaded %d out of %d recipes in the second pass.", successful, amount);
	}

	private static void loadCraftingRecipes(Config config, boolean shaped) throws Config.ParseException
	{
		int amount = 0;
		int successful = 0;

		for (Iterator<Config.Value> it = config.valueIterator(); it.hasNext(); amount++)
		{
			Config.Value value = it.next();
			if (loadCraftingRecipe(value, shaped, false))
			{
				successful++;
			}
		}

		IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for " + config.name);
	}

	private static boolean loadCraftingRecipe(Config.Value value, boolean shaped, boolean lastAttempt)
	{
		String outputString = value.getString();
		boolean hidden = outputString.contains("@hidden");
		boolean consuming = outputString.contains("@consuming");
		boolean filler = outputString.contains("@filler*");
		boolean fixedSize = outputString.contains("@fixed");
		int fillAmount = -1;

		ItemStack output;
		try
		{
			if (hidden)
			{
				outputString = outputString.replace("@hidden", "").trim();
			}

			if (consuming)
			{
				outputString = outputString.replace("@consuming", "").trim();
			}

			if (filler)
			{
				int fillerLoc = outputString.indexOf("@filler*");
				int end = outputString.indexOf(32, fillerLoc);
				String fillerString = outputString.substring(fillerLoc, end == -1 ? outputString.length() : end);
				fillAmount = Integer.parseInt(fillerString.substring(8));
				outputString = outputString.replace(fillerString, "").trim();
			}

			if (fixedSize)
			{
				outputString = outputString.replace("@fixed", "").trim();
			}

			output = ConfigUtil.asStackWithAmount(outputString);
		} catch (ParseException e)
		{
			throw new Config.ParseException("invalid key", value, e);
		} catch (NumberFormatException e)
		{
			throw new Config.ParseException("Invalid filler amount", value, e);
		}

		if (output == null)
		{
			if (lastAttempt)
			{
				IC2.log
					.warn(
						LogCategory.Recipe,
						new Config.ParseException("invalid output specified: " + value.getString(), value),
						"Skipping recipe for %s due to unresolvable output.",
						value.name
					);
			} else
			{
				pendingRecipes.add(() -> loadCraftingRecipe(value, shaped, true));
			}

			return false;
		} else
		{
			for (IRecipeInput disable : disabledRecipeOutputs)
			{
				if (disable.matches(output))
				{
					return true;
				}
			}

			boolean requireIc2Circuits = ConfigUtil.getBool(MainConfig.get(), "recipes/requireIc2Circuits");

			try
			{
				boolean isShapeSpec = shaped;
				List<Object> inputs = new ArrayList<>();

				for (String part : splitWhitespace(value.name))
				{
					if (part.startsWith("@"))
					{
						if (part.equals("@hidden"))
						{
							hidden = true;
						} else
						{
							if (!part.startsWith("@filler*"))
							{
								throw new Config.ParseException("invalid attribute: " + part, value);
							}

							try
							{
								fillAmount = Integer.parseInt(part.substring("@filler*".length()));
								filler = true;
							} catch (NumberFormatException e)
							{
								throw new Config.ParseException("Invalid filler amount", value, e);
							}
						}
					} else if (isShapeSpec)
					{
						if (filler)
						{
							throw new Config.ParseException("Filler recipes can only be shapeless", value);
						}

						isShapeSpec = false;
						if (part.startsWith("\""))
						{
							if (!part.endsWith("\""))
							{
								throw new Config.ParseException("missing end quote: " + part, value);
							}

							part = part.substring(1, part.length() - 1);
						}

						String[] rows = part.split("\\|");
						Integer width = null;

						for (String row : rows)
						{
							if (width != null && width != row.length())
							{
								throw new Config.ParseException("inconsistent recipe row width", value);
							}

							width = row.length();
						}

						inputs.addAll(Arrays.asList(rows));
					} else
					{
						List<IRecipeInput> input = new ArrayList<>();
						boolean isPatternIndex = shaped;
						String[] var17 = part.split("\\s*\\|\\s*");
						int var18 = var17.length;
						int var19 = 0;

						while (true)
						{
							if (var19 < var18)
							{
								String subPart = var17[var19];
								String ingredient = subPart;
								if (isPatternIndex)
								{
									isPatternIndex = false;
									int pos = ingredient.indexOf(":");
									if (pos != 1)
									{
										throw new Config.ParseException("no valid pattern index character found: " + part, value);
									}

									inputs.add(ingredient.charAt(0));
									ingredient = ingredient.substring(2);
								}

								IRecipeInput cInput = ConfigUtil.asRecipeInput(ingredient);
								if (cInput != null)
								{
									if (cInput instanceof RecipeInputOreDict)
									{
										RecipeInputOreDict odInput = (RecipeInputOreDict) cInput;
										if (odInput.input.equals("circuitBasic") && requireIc2Circuits)
										{
											cInput = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.circuit));
										} else if (odInput.input.equals("circuitAdvanced") && requireIc2Circuits)
										{
											cInput = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.advanced_circuit));
										}
									}

									input.add(cInput);
									var19++;
									continue;
								}

								if (lastAttempt)
								{
									IC2.log
										.warn(
											LogCategory.Recipe,
											new Config.ParseException("invalid ingredient specified: " + ingredient, value),
											"Skipping recipe for %s due to unresolvable input.",
											value.name
										);
								} else
								{
									pendingRecipes.add(() -> loadCraftingRecipe(value, shaped, true));
								}
							}

							if (input.size() == 1)
							{
								inputs.add(input.get(0));
							} else
							{
								inputs.add(input);
							}
							break;
						}
					}
				}

				if (hidden || consuming || fixedSize)
				{
					inputs.add(new ICraftingRecipeManager.AttributeContainer(hidden, consuming, fixedSize));
				}

				if (filler)
				{
					GradualRecipe.addAndRegister(output, fillAmount, inputs.toArray());
				} else if (shaped)
				{
					AdvRecipe.addAndRegister(output, inputs.toArray());
				} else
				{
					AdvShapelessRecipe.addAndRegister(output, inputs.toArray());
				}

				return true;
			} catch (Config.ParseException e)
			{
				throw e;
			} catch (Exception e)
			{
				throw new Config.ParseException("generic parse error", value, e);
			}
		}
	}

	private static void loadUuRecipes(Config config)
	{
		int amount = 0;
		int successful = 0;

		for (Iterator<Config.Value> it = config.valueIterator(); it.hasNext(); amount++)
		{
			Config.Value value = it.next();
			if (loadUuRecipe(value, false))
			{
				successful++;
			}
		}

		IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for solid uu recipes");
	}

	private static boolean loadUuRecipe(Config.Value value, boolean lastAttempt)
	{
		String outputString = value.getString();

		ItemStack output;
		try
		{
			output = ConfigUtil.asStackWithAmount(outputString);
		} catch (ParseException e)
		{
			throw new Config.ParseException("Invalid output", value, e);
		}

		if (output == null)
		{
			if (lastAttempt)
			{
				IC2.log
					.warn(
						LogCategory.Recipe,
						new Config.ParseException("invalid output specified: " + value.getString(), value),
						"Skipping recipe for %s due to unresolvable output.",
						value.name
					);
			} else
			{
				pendingRecipes.add(() -> loadUuRecipe(value, true));
			}

			return false;
		} else
		{
			for (IRecipeInput disable : disabledRecipeOutputs)
			{
				if (disable.matches(output))
				{
					return true;
				}
			}

			try
			{
				boolean isShapeSpec = true;
				List<Object> inputs = new ArrayList<>();

				for (String part : splitWhitespace(value.name))
				{
					if (isShapeSpec)
					{
						isShapeSpec = false;
						if (part.startsWith("\""))
						{
							if (!part.endsWith("\""))
							{
								throw new Config.ParseException("missing end quote: " + part, value);
							}

							part = part.substring(1, part.length() - 1);
						}

						String[] rows = part.split("\\|");
						Integer width = null;

						for (String row : rows)
						{
							if (width != null && width != row.length())
							{
								throw new Config.ParseException("inconsistent recipe row width", value);
							}

							width = row.length();
						}

						inputs.addAll(Arrays.asList(rows));
					} else
					{
						List<IRecipeInput> input = new ArrayList<>();
						boolean isPatternIndex = true;
						String[] var10 = part.split("\\s*\\|\\s*");
						int var11 = var10.length;
						int var12 = 0;

						while (true)
						{
							if (var12 < var11)
							{
								String subPart = var10[var12];
								String ingredient = subPart;
								if (isPatternIndex)
								{
									isPatternIndex = false;
									int pos = ingredient.indexOf(":");
									if (pos != 1)
									{
										throw new Config.ParseException("no valid pattern index character found: " + part, value);
									}

									inputs.add(ingredient.charAt(0));
									ingredient = ingredient.substring(2);
								}

								IRecipeInput cInput = ConfigUtil.asRecipeInput(ingredient);
								if (cInput != null)
								{
									input.add(cInput);
									var12++;
									continue;
								}

								if (lastAttempt)
								{
									IC2.log
										.warn(
											LogCategory.Recipe,
											new Config.ParseException("invalid ingredient specified: " + ingredient, value),
											"Skipping recipe for %s due to unresolvable input.",
											value.name
										);
								} else
								{
									pendingRecipes.add(() -> loadUuRecipe(value, true));
								}
							}

							if (input.size() == 1)
							{
								inputs.add(input.get(0));
							} else
							{
								inputs.add(input);
							}
							break;
						}
					}
				}

				boolean foundUU = false;
				boolean foundOther = false;

				for (Object input : inputs)
				{
					if (input instanceof RecipeInputItemStack
						&& input.equals(Recipes.inputFactory.forStack(ItemName.misc_resource.getItemStack(MiscResourceType.matter))))
					{
						foundUU = true;
					} else if (input instanceof IRecipeInput || input instanceof List)
					{
						foundOther = true;
					}
				}

				if (!foundUU)
				{
					IC2.log
						.warn(
							LogCategory.Recipe,
							new Config.ParseException("Missing UU from UU recipe", value),
							"Skipping UU recipe for %s due to missing UU.",
							value.name
						);
				}

				if (foundOther)
				{
					TileEntityAssemblyBench.RECIPES.add(new AdvRecipe(output, inputs.toArray()));
				} else
				{
					TileEntityAssemblyBench.RECIPES.add(TileEntityAssemblyBench.UuRecipe.create(output, inputs.toArray()));
				}

				return true;
			} catch (Config.ParseException e)
			{
				throw e;
			} catch (Exception e)
			{
				throw new Config.ParseException("Generic parse error", value, e);
			}
		}
	}

	private static void loadMachineRecipes(Config config, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> machine, Rezepte.MachineType type)
	{
		int amount = 0;
		int successful = 0;

		for (Iterator<Config.Value> it = config.valueIterator(); it.hasNext(); amount++)
		{
			Config.Value value = it.next();
			if (loadMachineRecipe(value, machine, type, false))
			{
				successful++;
			}
		}

		IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for " + config.name);
	}

	private static boolean loadMachineRecipe(
		Config.Value value, IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> machine, Rezepte.MachineType type, boolean lastAttempt
	)
	{
		boolean exact = value.name.contains("@exact");
		List<ItemStack> outputs = new ArrayList<>();
		NBTTagCompound metadata = new NBTTagCompound();

		IRecipeInput input;
		try
		{
			if (exact)
			{
				input = Recipes.inputFactory.forExactStack(ConfigUtil.asStackWithAmount(value.name.replace("@exact", "").trim()));
			} else
			{
				input = ConfigUtil.asRecipeInputWithAmount(value.name);
			}
		} catch (ParseException e)
		{
			throw new Config.ParseException("invalid key", value, e);
		}

		if (input == null)
		{
			if (lastAttempt)
			{
				IC2.log
					.warn(
						LogCategory.Recipe,
						new Config.ParseException("invalid input specified: " + value.name, value),
						"Skipping recipe due to unresolvable input %s.",
						value.name
					);
			} else
			{
				pendingRecipes.add(() -> loadMachineRecipe(value, machine, type, true));
			}

			return false;
		} else
		{
			try
			{
				for (String part : splitWhitespace(value.getString()))
				{
					if (!part.startsWith("@"))
					{
						ItemStack cOutput = ConfigUtil.asStackWithAmount(part);
						if (cOutput == null)
						{
							if (lastAttempt)
							{
								IC2.log
									.warn(
										LogCategory.Recipe,
										new Config.ParseException("invalid output specified: " + part, value),
										"Skipping recipe using %s due to unresolvable output %s.",
										value.name,
										part
									);
							} else
							{
								pendingRecipes.add(() -> loadMachineRecipe(value, machine, type, true));
							}

							return false;
						}

						for (IRecipeInput disable : disabledRecipeOutputs)
						{
							if (disable.matches(cOutput))
							{
								return true;
							}
						}

						outputs.add(cOutput);
					} else if (part.startsWith("@ignoreSameInputOutput"))
					{
						metadata.setBoolean("ignoreSameInputOutput", true);
					} else if (part.startsWith("@xp:") && type == Rezepte.MachineType.Furnace)
					{
						metadata.setFloat("experience", Float.parseFloat(part.substring(4)));
					} else if (part.startsWith("@hardness:") && type == Rezepte.MachineType.BlockCutter)
					{
						metadata.setInteger("hardness", Integer.parseInt(part.substring(10)));
					} else if (part.startsWith("@heat:") && type == Rezepte.MachineType.ThermalCentrifuge)
					{
						metadata.setInteger("minHeat", Integer.parseInt(part.substring(6)));
					} else if (part.startsWith("@fluid:") && type == Rezepte.MachineType.OreWashingPlant)
					{
						metadata.setInteger("amount", Integer.parseInt(part.substring(7)));
					} else if (!part.startsWith("@fluid:") || type != Rezepte.MachineType.BlastFurnace)
					{
						if (!part.startsWith("@duration:") || type != Rezepte.MachineType.BlastFurnace)
						{
							throw new Config.ParseException("invalid attribute: " + part, value);
						}

						metadata.setInteger("duration", Integer.parseInt(part.substring(10)));
					} else
					{
						metadata.setInteger("fluid", Integer.parseInt(part.substring(7)));
					}
				}

				if (type.tagsRequired.isEmpty() || !metadata.hasNoTags() && type.hasRequiredTags(metadata))
				{
					if (metadata.hasNoTags())
					{
						metadata = null;
					}

					if (machine.addRecipe(input, outputs, metadata, false))
					{
						return true;
					} else
					{
						throw new Config.ParseException("Conflicting recipe", value);
					}
				} else
				{
					IC2.log.warn(LogCategory.Recipe, "Could not add machine recipe: " + value.name + " missing tag.");
					return false;
				}
			} catch (Config.ParseException e)
			{
				throw e;
			} catch (Exception e)
			{
				throw new Config.ParseException("generic parse error", value, e);
			}
		}
	}

	private static List<String> splitWhitespace(String str)
	{
		String dummy = str.replaceAll("\\\\.", "xx");
		List<String> ret = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;

		for (int i = 0; i < str.length(); i++)
		{
			char c = dummy.charAt(i);
			if (c == '"')
			{
				quoted = !quoted;
			}

			boolean split = false;
			if (!quoted && Character.isWhitespace(c))
			{
				split = true;
			}

			if (split)
			{
				if (current.length() > 0)
				{
					ret.add(current.toString());
					current.setLength(0);
				}
			} else
			{
				current.append(str.charAt(i));
			}
		}

		if (current.length() > 0)
		{
			ret.add(current.toString());
		}

		return ret;
	}

	public static InputStream getConfigFile(String name) throws FileNotFoundException
	{
		File file = new File(IC2.platform.getMinecraftDir(), "config/ic2/" + name + ".ini");
		return file.canRead() && file.isFile() ? new FileInputStream(file) : ProfileManager.getRecipeConfig(name);
	}

	public static InputStream getDefaultConfigFile(String name)
	{
		return Rezepte.class.getResourceAsStream("/assets/ic2/config/" + name + ".ini");
	}

	public static void registerRecipe(ResourceLocation rl, IRecipe recipe)
	{
		recipe.setRegistryName(rl);
		ForgeRegistries.RECIPES.register(recipe);
	}

	public static void registerRecipe(IRecipe recipe)
	{
		registerRecipe(new ResourceLocation("ic2", "" + recipeID++), recipe);
	}

	public static void registerRecipes()
	{
		loadRecipes();
		if (!IC2.version.isClassic())
		{
			registerRecipe(new ArmorDyeingRecipe(ItemArmorQuantumSuit.class));
			registerRecipe(new JetpackAttachmentRecipe());
		}
	}

	private enum MachineType
	{
		Normal(),
		Furnace(),
		BlockCutter("hardness"),
		ThermalCentrifuge("minHeat"),
		OreWashingPlant("amount"),
		BlastFurnace("fluid", "duration");

		final Set<String> tagsRequired;

		MachineType(String... tagsRequired)
		{
			this.tagsRequired = new HashSet<>(Arrays.asList(ArrayUtils.nullToEmpty(tagsRequired)));
		}

		boolean hasRequiredTags(NBTTagCompound metadata)
		{
			for (String key : this.tagsRequired)
			{
				if (!metadata.hasKey(key))
				{
					return false;
				}
			}

			return true;
		}
	}
}
