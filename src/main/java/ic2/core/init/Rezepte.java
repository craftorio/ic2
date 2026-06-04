// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.init;

import java.util.HashSet;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Set;
import java.util.ArrayDeque;
import net.minecraft.item.ItemArmor;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.util.ResourceLocation;
import java.io.FileNotFoundException;
import ic2.core.profile.ProfileManager;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.crafting.IRecipe;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.item.type.MiscResourceType;
import ic2.core.recipe.RecipeInputItemStack;
import ic2.api.recipe.ICraftingRecipeManager;
import ic2.core.recipe.RecipeInputOreDict;
import java.util.Arrays;
import java.util.ArrayList;
import java.text.ParseException;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.Config;
import ic2.core.block.machine.FillFluidContainerRecipeManager;
import ic2.core.block.machine.EmptyFluidContainerRecipeManager;
import ic2.core.recipe.SmeltingRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.recipe.AdvCraftingRecipeManager;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.recipe.ColourCarryingRecipe;
import ic2.core.recipe.ArmorDyeingRecipe;
import ic2.core.recipe.GradualRecipe;
import ic2.core.recipe.AdvShapelessRecipe;
import ic2.core.recipe.AdvRecipe;
import net.minecraftforge.oredict.RecipeSorter;
import java.util.function.BooleanSupplier;
import java.util.Queue;
import ic2.api.recipe.IRecipeInput;
import java.util.List;

public class Rezepte
{
    private static int recipeID;
    private static List<IRecipeInput> disabledRecipeOutputs;
    private static Queue<BooleanSupplier> pendingRecipes;
    
    public static void registerWithSorter() {
        final RecipeSorter.Category shaped = RecipeSorter.Category.SHAPED;
        final RecipeSorter.Category shapeless = RecipeSorter.Category.SHAPELESS;
        RecipeSorter.register("ic2:shaped", (Class)AdvRecipe.class, shaped, "after:minecraft:shapeless");
        RecipeSorter.register("ic2:shapeless", (Class)AdvShapelessRecipe.class, shapeless, "after:ic2:shaped");
        RecipeSorter.register("ic2:gradual", (Class)GradualRecipe.class, shapeless, "after:ic2:shapeless");
        RecipeSorter.register("ic2:armorDyeing", (Class)ArmorDyeingRecipe.class, shapeless, "after:ic2:shapeless");
        RecipeSorter.register("ic2:colourCarrying", (Class)ColourCarryingRecipe.class, shaped, "after:ic2:shaped");
        RecipeSorter.register("ic2:jetpackAttachement", (Class)JetpackAttachmentRecipe.class, shapeless, "before:minecraft:shapeless");
    }
    
    static void loadRecipes() {
        Recipes.advRecipes = new AdvCraftingRecipeManager();
        Recipes.furnace = new SmeltingRecipeManager();
        Recipes.emptyFluidContainer = new EmptyFluidContainerRecipeManager();
        Recipes.fillFluidContainer = new FillFluidContainerRecipeManager();
        final Config shapedRecipes = new Config("shaped recipes");
        final Config shapelessRecipes = new Config("shapeless recipes");
        final Config uuRecipes = new Config("uu recipes");
        final Config furnaceRecipes = new Config("furnace recipes");
        final Config blastfurnace = new Config("blast furnace recipes");
        final Config blockCutter = new Config("block cutter recipes");
        final Config compressor = new Config("compressor recipes");
        final Config extractor = new Config("extractor recipes");
        final Config macerator = new Config("macerator recipes");
        final Config mfcutting = new Config("metal former cutting recipes");
        final Config mfextruding = new Config("metal former extruding recipes");
        final Config mfrolling = new Config("metal former rolling recipes");
        final Config oreWashing = new Config("ore washing recipes");
        final Config centrifuge = new Config("thermal centrifuge recipes");
        try {
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
        }
        catch (final Exception e) {
            IC2.log.warn(LogCategory.Recipe, e, "Recipe loading failed.");
        }
        Rezepte.disabledRecipeOutputs = ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/disable");
        if (!MainConfig.get().get("recipes/allowCoinCrafting").getBool()) {
            Rezepte.disabledRecipeOutputs.add(Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.coin)));
        }
        loadCraftingRecipes(shapedRecipes, true);
        loadCraftingRecipes(shapelessRecipes, false);
        loadUuRecipes(uuRecipes);
        loadMachineRecipes(furnaceRecipes, SmeltingRecipeManager.SmeltingBridge.INSTANCE, MachineType.Furnace);
        loadMachineRecipes(blastfurnace, Recipes.blastfurnace, MachineType.BlastFurnace);
        loadMachineRecipes(blockCutter, Recipes.blockcutter, MachineType.BlockCutter);
        loadMachineRecipes(compressor, Recipes.compressor, MachineType.Normal);
        loadMachineRecipes(extractor, Recipes.extractor, MachineType.Normal);
        loadMachineRecipes(macerator, Recipes.macerator, MachineType.Normal);
        loadMachineRecipes(mfcutting, Recipes.metalformerCutting, MachineType.Normal);
        loadMachineRecipes(mfextruding, Recipes.metalformerExtruding, MachineType.Normal);
        loadMachineRecipes(mfrolling, Recipes.metalformerRolling, MachineType.Normal);
        loadMachineRecipes(oreWashing, Recipes.oreWashing, MachineType.OreWashingPlant);
        loadMachineRecipes(centrifuge, Recipes.centrifuge, MachineType.ThermalCentrifuge);
        IC2.log.debug(LogCategory.Recipe, "%d recipes failed to load in the first pass.", Rezepte.pendingRecipes.size());
    }
    
    public static void loadFailedRecipes() {
        int amount = Rezepte.pendingRecipes.size();
        int successful = 0;
        BooleanSupplier recipe;
        while ((recipe = Rezepte.pendingRecipes.poll()) != null) {
            if (recipe.getAsBoolean()) {
                ++successful;
            }
        }
        try {
            final Config blockCutter = new Config("block cutter late recipes");
            blockCutter.load(getConfigFile("block_cutter_late"));
            final Iterator<Config.Value> it = blockCutter.valueIterator();
            while (it.hasNext()) {
                if (loadMachineRecipe(it.next(), Recipes.blockcutter, MachineType.BlockCutter, true)) {
                    ++successful;
                }
                ++amount;
            }
        }
        catch (final Exception e) {
            IC2.log.warn(LogCategory.Recipe, e, "Late recipe loading failed.");
        }
        IC2.log.debug(LogCategory.Recipe, "Successfully loaded %d out of %d recipes in the second pass.", successful, amount);
    }
    
    private static void loadCraftingRecipes(final Config config, final boolean shaped) throws Config.ParseException {
        int amount = 0;
        int successful = 0;
        final Iterator<Config.Value> it = config.valueIterator();
        while (it.hasNext()) {
            final Config.Value value = it.next();
            if (loadCraftingRecipe(value, shaped, false)) {
                ++successful;
            }
            ++amount;
        }
        IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for " + config.name);
    }
    
    private static boolean loadCraftingRecipe(final Config.Value value, final boolean shaped, final boolean lastAttempt) {
        String outputString = value.getString();
        boolean hidden = outputString.contains("@hidden");
        final boolean consuming = outputString.contains("@consuming");
        boolean filler = outputString.contains("@filler*");
        final boolean fixedSize = outputString.contains("@fixed");
        int fillAmount = -1;
        ItemStack output;
        try {
            if (hidden) {
                outputString = outputString.replace("@hidden", "").trim();
            }
            if (consuming) {
                outputString = outputString.replace("@consuming", "").trim();
            }
            if (filler) {
                final int fillerLoc = outputString.indexOf("@filler*");
                final int end = outputString.indexOf(32, fillerLoc);
                final String fillerString = outputString.substring(fillerLoc, (end == -1) ? outputString.length() : end);
                fillAmount = Integer.parseInt(fillerString.substring(8));
                outputString = outputString.replace(fillerString, "").trim();
            }
            if (fixedSize) {
                outputString = outputString.replace("@fixed", "").trim();
            }
            output = ConfigUtil.asStackWithAmount(outputString);
        }
        catch (final ParseException e) {
            throw new Config.ParseException("invalid key", value, e);
        }
        catch (final NumberFormatException e2) {
            throw new Config.ParseException("Invalid filler amount", value, e2);
        }
        if (output == null) {
            if (lastAttempt) {
                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid output specified: " + value.getString(), value), "Skipping recipe for %s due to unresolvable output.", value.name);
            }
            else {
                Rezepte.pendingRecipes.add(() -> loadCraftingRecipe(value, shaped, true));
            }
            return false;
        }
        for (final IRecipeInput disable : Rezepte.disabledRecipeOutputs) {
            if (disable.matches(output)) {
                return true;
            }
        }
        final boolean requireIc2Circuits = ConfigUtil.getBool(MainConfig.get(), "recipes/requireIc2Circuits");
        try {
            boolean isShapeSpec = shaped;
            final List<Object> inputs = new ArrayList<Object>();
            for (String part : splitWhitespace(value.name)) {
                if (part.startsWith("@")) {
                    if (!part.equals("@hidden")) {
                        if (part.startsWith("@filler*")) {
                            try {
                                fillAmount = Integer.parseInt(part.substring("@filler*".length()));
                                filler = true;
                                continue;
                            }
                            catch (final NumberFormatException e3) {
                                throw new Config.ParseException("Invalid filler amount", value, e3);
                            }
                        }
                        throw new Config.ParseException("invalid attribute: " + part, value);
                    }
                    hidden = true;
                }
                else if (isShapeSpec) {
                    if (filler) {
                        throw new Config.ParseException("Filler recipes can only be shapeless", value);
                    }
                    isShapeSpec = false;
                    if (part.startsWith("\"")) {
                        if (!part.endsWith("\"")) {
                            throw new Config.ParseException("missing end quote: " + part, value);
                        }
                        part = part.substring(1, part.length() - 1);
                    }
                    final String[] rows = part.split("\\|");
                    Integer width = null;
                    for (final String row : rows) {
                        if (width != null && width != row.length()) {
                            throw new Config.ParseException("inconsistent recipe row width", value);
                        }
                        width = row.length();
                    }
                    inputs.addAll(Arrays.asList(rows));
                }
                else {
                    final List<IRecipeInput> input = new ArrayList<IRecipeInput>();
                    boolean isPatternIndex = shaped;
                    final String[] split = part.split("\\s*\\|\\s*");
                    final int length2 = split.length;
                    int j = 0;
                    while (j < length2) {
                        String ingredient;
                        final String subPart = ingredient = split[j];
                        if (isPatternIndex) {
                            isPatternIndex = false;
                            final int pos = ingredient.indexOf(":");
                            if (pos != 1) {
                                throw new Config.ParseException("no valid pattern index character found: " + part, value);
                            }
                            inputs.add(ingredient.charAt(0));
                            ingredient = ingredient.substring(2);
                        }
                        IRecipeInput cInput = ConfigUtil.asRecipeInput(ingredient);
                        if (cInput == null) {
                            if (lastAttempt) {
                                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid ingredient specified: " + ingredient, value), "Skipping recipe for %s due to unresolvable input.", value.name);
                                break;
                            }
                            Rezepte.pendingRecipes.add(() -> loadCraftingRecipe(value, shaped, true));
                            break;
                        }
                        else {
                            if (cInput instanceof RecipeInputOreDict) {
                                final RecipeInputOreDict odInput = (RecipeInputOreDict)cInput;
                                if (odInput.input.equals("circuitBasic") && requireIc2Circuits) {
                                    cInput = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.circuit));
                                }
                                else if (odInput.input.equals("circuitAdvanced") && requireIc2Circuits) {
                                    cInput = Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.advanced_circuit));
                                }
                            }
                            input.add(cInput);
                            ++j;
                        }
                    }
                    if (input.size() == 1) {
                        inputs.add(input.get(0));
                    }
                    else {
                        inputs.add(input);
                    }
                }
            }
            if (hidden || consuming || fixedSize) {
                inputs.add(new ICraftingRecipeManager.AttributeContainer(hidden, consuming, fixedSize));
            }
            if (filler) {
                GradualRecipe.addAndRegister(output, fillAmount, inputs.toArray());
            }
            else if (shaped) {
                AdvRecipe.addAndRegister(output, inputs.toArray());
            }
            else {
                AdvShapelessRecipe.addAndRegister(output, inputs.toArray());
            }
            return true;
        }
        catch (final Config.ParseException e4) {
            throw e4;
        }
        catch (final Exception e5) {
            throw new Config.ParseException("generic parse error", value, e5);
        }
    }
    
    private static void loadUuRecipes(final Config config) {
        int amount = 0;
        int successful = 0;
        final Iterator<Config.Value> it = config.valueIterator();
        while (it.hasNext()) {
            final Config.Value value = it.next();
            if (loadUuRecipe(value, false)) {
                ++successful;
            }
            ++amount;
        }
        IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for solid uu recipes");
    }
    
    private static boolean loadUuRecipe(final Config.Value value, final boolean lastAttempt) {
        final String outputString = value.getString();
        ItemStack output;
        try {
            output = ConfigUtil.asStackWithAmount(outputString);
        }
        catch (final ParseException e) {
            throw new Config.ParseException("Invalid output", value, e);
        }
        if (output == null) {
            if (lastAttempt) {
                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid output specified: " + value.getString(), value), "Skipping recipe for %s due to unresolvable output.", value.name);
            }
            else {
                Rezepte.pendingRecipes.add(() -> loadUuRecipe(value, true));
            }
            return false;
        }
        for (final IRecipeInput disable : Rezepte.disabledRecipeOutputs) {
            if (disable.matches(output)) {
                return true;
            }
        }
        try {
            boolean isShapeSpec = true;
            final List<Object> inputs = new ArrayList<Object>();
            for (String part : splitWhitespace(value.name)) {
                if (isShapeSpec) {
                    isShapeSpec = false;
                    if (part.startsWith("\"")) {
                        if (!part.endsWith("\"")) {
                            throw new Config.ParseException("missing end quote: " + part, value);
                        }
                        part = part.substring(1, part.length() - 1);
                    }
                    final String[] rows = part.split("\\|");
                    Integer width = null;
                    for (final String row : rows) {
                        if (width != null && width != row.length()) {
                            throw new Config.ParseException("inconsistent recipe row width", value);
                        }
                        width = row.length();
                    }
                    inputs.addAll(Arrays.asList(rows));
                }
                else {
                    final List<IRecipeInput> input = new ArrayList<IRecipeInput>();
                    boolean isPatternIndex = true;
                    final String[] split = part.split("\\s*\\|\\s*");
                    final int length2 = split.length;
                    int j = 0;
                    while (j < length2) {
                        String ingredient;
                        final String subPart = ingredient = split[j];
                        if (isPatternIndex) {
                            isPatternIndex = false;
                            final int pos = ingredient.indexOf(":");
                            if (pos != 1) {
                                throw new Config.ParseException("no valid pattern index character found: " + part, value);
                            }
                            inputs.add(ingredient.charAt(0));
                            ingredient = ingredient.substring(2);
                        }
                        final IRecipeInput cInput = ConfigUtil.asRecipeInput(ingredient);
                        if (cInput == null) {
                            if (lastAttempt) {
                                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid ingredient specified: " + ingredient, value), "Skipping recipe for %s due to unresolvable input.", value.name);
                                break;
                            }
                            Rezepte.pendingRecipes.add(() -> loadUuRecipe(value, true));
                            break;
                        }
                        else {
                            input.add(cInput);
                            ++j;
                        }
                    }
                    if (input.size() == 1) {
                        inputs.add(input.get(0));
                    }
                    else {
                        inputs.add(input);
                    }
                }
            }
            boolean foundUU = false;
            boolean foundOther = false;
            for (final Object input2 : inputs) {
                if (input2 instanceof RecipeInputItemStack && input2.equals(Recipes.inputFactory.forStack(ItemName.misc_resource.getItemStack(MiscResourceType.matter)))) {
                    foundUU = true;
                }
                else {
                    if (!(input2 instanceof IRecipeInput) && !(input2 instanceof List)) {
                        continue;
                    }
                    foundOther = true;
                }
            }
            if (!foundUU) {
                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("Missing UU from UU recipe", value), "Skipping UU recipe for %s due to missing UU.", value.name);
            }
            if (foundOther) {
                TileEntityAssemblyBench.RECIPES.add((IRecipe)new AdvRecipe(output, inputs.toArray()));
            }
            else {
                TileEntityAssemblyBench.RECIPES.add((IRecipe)TileEntityAssemblyBench.UuRecipe.create(output, inputs.toArray()));
            }
            return true;
        }
        catch (final Config.ParseException e2) {
            throw e2;
        }
        catch (final Exception e3) {
            throw new Config.ParseException("Generic parse error", value, e3);
        }
    }
    
    private static void loadMachineRecipes(final Config config, final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> machine, final MachineType type) {
        int amount = 0;
        int successful = 0;
        final Iterator<Config.Value> it = config.valueIterator();
        while (it.hasNext()) {
            final Config.Value value = it.next();
            if (loadMachineRecipe(value, machine, type, false)) {
                ++successful;
            }
            ++amount;
        }
        IC2.log.info(LogCategory.Recipe, "Successfully loaded " + successful + " out of " + amount + " recipes for " + config.name);
    }
    
    private static boolean loadMachineRecipe(final Config.Value value, final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> machine, final MachineType type, final boolean lastAttempt) {
        final boolean exact = value.name.contains("@exact");
        final List<ItemStack> outputs = new ArrayList<ItemStack>();
        NBTTagCompound metadata = new NBTTagCompound();
        IRecipeInput input;
        try {
            if (exact) {
                input = Recipes.inputFactory.forExactStack(ConfigUtil.asStackWithAmount(value.name.replace("@exact", "").trim()));
            }
            else {
                input = ConfigUtil.asRecipeInputWithAmount(value.name);
            }
        }
        catch (final ParseException e) {
            throw new Config.ParseException("invalid key", value, e);
        }
        if (input == null) {
            if (lastAttempt) {
                IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid input specified: " + value.name, value), "Skipping recipe due to unresolvable input %s.", value.name);
            }
            else {
                Rezepte.pendingRecipes.add(() -> loadMachineRecipe(value, machine, type, true));
            }
            return false;
        }
        try {
            for (final String part : splitWhitespace(value.getString())) {
                if (part.startsWith("@")) {
                    if (part.startsWith("@ignoreSameInputOutput")) {
                        metadata.setBoolean("ignoreSameInputOutput", true);
                    }
                    else if (part.startsWith("@xp:") && type == MachineType.Furnace) {
                        metadata.setFloat("experience", Float.parseFloat(part.substring(4)));
                    }
                    else if (part.startsWith("@hardness:") && type == MachineType.BlockCutter) {
                        metadata.setInteger("hardness", Integer.parseInt(part.substring(10)));
                    }
                    else if (part.startsWith("@heat:") && type == MachineType.ThermalCentrifuge) {
                        metadata.setInteger("minHeat", Integer.parseInt(part.substring(6)));
                    }
                    else if (part.startsWith("@fluid:") && type == MachineType.OreWashingPlant) {
                        metadata.setInteger("amount", Integer.parseInt(part.substring(7)));
                    }
                    else if (part.startsWith("@fluid:") && type == MachineType.BlastFurnace) {
                        metadata.setInteger("fluid", Integer.parseInt(part.substring(7)));
                    }
                    else {
                        if (!part.startsWith("@duration:") || type != MachineType.BlastFurnace) {
                            throw new Config.ParseException("invalid attribute: " + part, value);
                        }
                        metadata.setInteger("duration", Integer.parseInt(part.substring(10)));
                    }
                }
                else {
                    final ItemStack cOutput = ConfigUtil.asStackWithAmount(part);
                    if (cOutput == null) {
                        if (lastAttempt) {
                            IC2.log.warn(LogCategory.Recipe, new Config.ParseException("invalid output specified: " + part, value), "Skipping recipe using %s due to unresolvable output %s.", value.name, part);
                        }
                        else {
                            Rezepte.pendingRecipes.add(() -> loadMachineRecipe(value, machine, type, true));
                        }
                        return false;
                    }
                    for (final IRecipeInput disable : Rezepte.disabledRecipeOutputs) {
                        if (disable.matches(cOutput)) {
                            return true;
                        }
                    }
                    outputs.add(cOutput);
                }
            }
            if (!type.tagsRequired.isEmpty() && (metadata.hasNoTags() || !type.hasRequiredTags(metadata))) {
                IC2.log.warn(LogCategory.Recipe, "Could not add machine recipe: " + value.name + " missing tag.");
                return false;
            }
            if (metadata.hasNoTags()) {
                metadata = null;
            }
            if (machine.addRecipe(input, outputs, metadata, false)) {
                return true;
            }
            throw new Config.ParseException("Conflicting recipe", value);
        }
        catch (final Config.ParseException e2) {
            throw e2;
        }
        catch (final Exception e3) {
            throw new Config.ParseException("generic parse error", value, e3);
        }
    }
    
    private static List<String> splitWhitespace(final String str) {
        final String dummy = str.replaceAll("\\\\.", "xx");
        final List<String> ret = new ArrayList<String>();
        final StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < str.length(); ++i) {
            final char c = dummy.charAt(i);
            if (c == '\"') {
                quoted = !quoted;
            }
            boolean split = false;
            if (!quoted && Character.isWhitespace(c)) {
                split = true;
            }
            if (split) {
                if (current.length() > 0) {
                    ret.add(current.toString());
                    current.setLength(0);
                }
            }
            else {
                current.append(str.charAt(i));
            }
        }
        if (current.length() > 0) {
            ret.add(current.toString());
        }
        return ret;
    }
    
    public static InputStream getConfigFile(final String name) throws FileNotFoundException {
        final File file = new File(IC2.platform.getMinecraftDir(), "config/ic2/" + name + ".ini");
        if (file.canRead() && file.isFile()) {
            return new FileInputStream(file);
        }
        return ProfileManager.getRecipeConfig(name);
    }
    
    public static InputStream getDefaultConfigFile(final String name) {
        return Rezepte.class.getResourceAsStream("/assets/ic2/config/" + name + ".ini");
    }
    
    public static void registerRecipe(final ResourceLocation rl, final IRecipe recipe) {
        recipe.setRegistryName(rl);
        ForgeRegistries.RECIPES.register((IForgeRegistryEntry)recipe);
    }
    
    public static void registerRecipe(final IRecipe recipe) {
        registerRecipe(new ResourceLocation("ic2", "" + Rezepte.recipeID++), recipe);
    }
    
    public static void registerRecipes() {
        loadRecipes();
        if (!IC2.version.isClassic()) {
            registerRecipe((IRecipe)new ArmorDyeingRecipe(ItemArmorQuantumSuit.class));
            registerRecipe((IRecipe)new JetpackAttachmentRecipe());
        }
    }
    
    static {
        Rezepte.pendingRecipes = new ArrayDeque<BooleanSupplier>();
    }
    
    private enum MachineType
    {
        Normal(new String[0]), 
        Furnace(new String[0]), 
        BlockCutter(new String[] { "hardness" }), 
        ThermalCentrifuge(new String[] { "minHeat" }), 
        OreWashingPlant(new String[] { "amount" }), 
        BlastFurnace(new String[] { "fluid", "duration" });
        
        final Set<String> tagsRequired;
        
        private MachineType(final String[] tagsRequired) {
            this.tagsRequired = new HashSet<String>(Arrays.asList(ArrayUtils.nullToEmpty(tagsRequired)));
        }
        
        boolean hasRequiredTags(final NBTTagCompound metadata) {
            for (final String key : this.tagsRequired) {
                if (!metadata.hasKey(key)) {
                    return false;
                }
            }
            return true;
        }
    }
}
