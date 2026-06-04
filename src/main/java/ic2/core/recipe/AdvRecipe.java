package ic2.core.recipe;

import ic2.api.item.ElectricItem;
import ic2.api.recipe.ICraftingRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

public class AdvRecipe implements IShapedRecipe {
  public static void addAndRegister(ItemStack result, Object... args) {
    try {
      Rezepte.registerRecipe((IRecipe)new AdvRecipe(result, args));
    } catch (RuntimeException e) {
      if (!MainConfig.ignoreInvalidRecipes)
        throw e; 
    } 
  }
  
  public AdvRecipe(ItemStack result, Object... args) {
    if (StackUtil.isEmpty(result))
      displayError("null result", null, result, false); 
    Map<Character, IRecipeInput> charMapping = new HashMap<>();
    List<String> inputArrangement = new ArrayList<>();
    Character lastChar = null;
    boolean isHidden = false;
    boolean isConsuming = false;
    boolean isFixedSize = false;
    for (Object arg : args) {
      if (arg instanceof String) {
        if (lastChar == null) {
          if (!charMapping.isEmpty())
            displayError("oredict name without preceding char", "Name: " + arg, result, false); 
          String str = (String)arg;
          if (str.isEmpty() || str.length() > 3)
            displayError("none or too many crafting columns", "Input: " + str + "\nSize: " + str.length(), result, false); 
          inputArrangement.add(str);
        } else {
          charMapping.put(lastChar, getRecipeObject(arg));
          lastChar = null;
        } 
      } else if (arg instanceof Character) {
        if (lastChar != null)
          displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false); 
        lastChar = (Character)arg;
      } else if (arg instanceof Boolean) {
        isHidden = ((Boolean)arg).booleanValue();
      } else if (arg instanceof ICraftingRecipeManager.AttributeContainer) {
        isHidden = ((ICraftingRecipeManager.AttributeContainer)arg).hidden;
        isConsuming = ((ICraftingRecipeManager.AttributeContainer)arg).consuming;
        isFixedSize = ((ICraftingRecipeManager.AttributeContainer)arg).fixedSize;
      } else {
        if (lastChar == null)
          displayError("two consecutive char definitions", "Input: " + arg + "\nprev. Input: " + lastChar, result, false); 
        try {
          IRecipeInput last = charMapping.put(lastChar, getRecipeObject(arg));
          if (last != null)
            displayError("duplicate char mapping", "Char: " + lastChar + "\nInput: " + arg + "\nType: " + arg.getClass().getName(), result, false); 
          lastChar = null;
        } catch (Exception e) {
          e.printStackTrace();
          displayError("unknown type", "Input: " + arg + "\nType: " + arg.getClass().getName(), result, false);
        } 
      } 
    } 
    this.hidden = isHidden;
    this.consuming = isConsuming;
    this.inputHeight = inputArrangement.size();
    if (lastChar != null)
      displayError("one or more unused mapping chars", "Letter: " + lastChar, result, false); 
    if (this.inputHeight == 0 || this.inputHeight > 3)
      displayError("none or too many crafting rows", "Size: " + inputArrangement.size(), result, false); 
    if (charMapping.size() == 0)
      displayError("no mapping chars", null, result, false); 
    this.inputWidth = ((String)inputArrangement.get(0)).length();
    if (debug && !isFixedSize) {
      if (StringUtils.containsOnly(inputArrangement.get(0), new char[] { ' ' }))
        IC2.log.warn(LogCategory.Recipe, "Leading empty row in shaped recipe for %s (%s), from %s.", new Object[] { result, result.func_82833_r(), getCaller() }); 
      if (StringUtils.containsOnly(inputArrangement.get(this.inputHeight - 1), new char[] { ' ' }))
        IC2.log.warn(LogCategory.Recipe, "Trailing empty row in shaped recipe for %s (%s), from %s.", new Object[] { result, result.func_82833_r(), getCaller() }); 
      for (int pass = 0; pass < 2; pass++) {
        boolean found = true;
        for (int j = 0; j < this.inputHeight; j++) {
          String str = inputArrangement.get(j);
          if ((pass == 0 && str.charAt(0) != ' ') || (pass == 1 && str
            .charAt(this.inputWidth - 1) != ' ')) {
            found = false;
            break;
          } 
        } 
        if (found)
          if (pass == 0) {
            IC2.log.warn(LogCategory.Recipe, "Leading empty column in shaped recipe for %s (%s), from %s.", new Object[] { result, result.func_82833_r(), getCaller() });
          } else {
            IC2.log.warn(LogCategory.Recipe, "Trailing empty column in shaped recipe for %s (%s), from %s.", new Object[] { result, result.func_82833_r(), getCaller() });
          }  
      } 
    } 
    int xMasks = -this.inputWidth + 4;
    int yMasks = -this.inputHeight + 4;
    int mask = 0;
    List<Object> inputs = new ArrayList();
    for (int y = 0; y < 3; y++) {
      String str = null;
      if (y < this.inputHeight) {
        str = inputArrangement.get(y);
        if (str.length() != this.inputWidth)
          displayError("no fixed width", "Expected: " + this.inputWidth + "\nGot: " + str.length(), result, false); 
      } 
      for (int x = 0; x < 3; x++) {
        mask <<= 1;
        if (x < this.inputWidth && str != null) {
          char c = str.charAt(x);
          if (c != ' ') {
            if (!charMapping.containsKey(Character.valueOf(c)))
              displayError("missing char mapping", "Letter: " + c, result, false); 
            inputs.add(charMapping.get(Character.valueOf(c)));
            mask |= 0x1;
          } 
        } 
      } 
    } 
    this.input = inputs.<IRecipeInput>toArray(new IRecipeInput[0]);
    boolean mirror = false;
    if (this.inputWidth != 1)
      for (String s : inputArrangement) {
        if (s.charAt(0) != s.charAt(this.inputWidth - 1)) {
          mirror = true;
          break;
        } 
      }  
    if (!mirror) {
      this.inputMirrored = null;
    } else {
      IRecipeInput[] tmp = new IRecipeInput[9];
      for (int k = 0, j = 0; k < 9; k++) {
        if ((mask & 1 << 8 - k) != 0) {
          tmp[k] = this.input[j];
          j++;
        } 
      } 
      IRecipeInput old = tmp[0];
      tmp[0] = tmp[2];
      tmp[2] = old;
      old = tmp[3];
      tmp[3] = tmp[5];
      tmp[5] = old;
      old = tmp[6];
      tmp[6] = tmp[8];
      tmp[8] = old;
      this.inputMirrored = new IRecipeInput[this.input.length];
      for (int m = 0, n = 0; m < 9; m++) {
        if (tmp[m] != null) {
          this.inputMirrored[n] = tmp[m];
          n++;
        } 
      } 
    } 
    this.masks = new int[xMasks * yMasks];
    if (!mirror) {
      this.masksMirrored = null;
    } else {
      this.masksMirrored = new int[this.masks.length];
    } 
    for (int i = 0; i < yMasks; i++) {
      int yMask = mask >>> i * 3;
      for (int x = 0; x < xMasks; x++) {
        int xyMask = yMask >>> x;
        this.masks[x + i * xMasks] = xyMask;
        if (mirror)
          this.masksMirrored[x + i * xMasks] = xyMask << 2 & 0x124 | xyMask & 0x92 | xyMask >>> 2 & 0x49; 
      } 
    } 
    this.output = result;
  }
  
  public boolean func_77569_a(InventoryCrafting inventorycrafting, World world) {
    return (func_77572_b(inventorycrafting) != StackUtil.emptyStack);
  }
  
  public ItemStack func_77572_b(InventoryCrafting inventorycrafting) {
    int size = inventorycrafting.func_70302_i_();
    int mask = 0;
    for (int i = 0; i < size; i++) {
      mask <<= 1;
      if (!StackUtil.isEmpty(inventorycrafting.func_70301_a(i)))
        mask |= 0x1; 
    } 
    if (size == 4)
      mask = (mask & 0xC) << 5 | (mask & 0x3) << 4; 
    if (checkMask(mask, this.masks)) {
      ItemStack ret = checkItems((IInventory)inventorycrafting, this.input);
      if (!StackUtil.isEmpty(ret))
        return ret; 
    } 
    if (this.masksMirrored != null && checkMask(mask, this.masksMirrored)) {
      ItemStack ret = checkItems((IInventory)inventorycrafting, this.inputMirrored);
      if (!StackUtil.isEmpty(ret))
        return ret; 
    } 
    return StackUtil.emptyStack;
  }
  
  public ItemStack func_77571_b() {
    return this.output;
  }
  
  public static boolean canShow(Object[] input, ItemStack output, boolean hidden) {
    return (!hidden || !ConfigUtil.getBool(MainConfig.get(), "misc/hideSecretRecipes"));
  }
  
  public boolean canShow() {
    return canShow((Object[])this.input, this.output, this.hidden);
  }
  
  public static List<ItemStack> expand(Object o) {
    List<ItemStack> ret = new ArrayList<>();
    if (o instanceof IRecipeInput) {
      ret.addAll(((IRecipeInput)o).getInputs());
    } else if (o instanceof String) {
      String s = (String)o;
      if (s.startsWith("liquid$")) {
        String name = s.substring(7);
        Fluid fluid = FluidRegistry.getFluid(name);
        ret.addAll(RecipeInputFluidContainer.getFluidContainer(fluid));
      } else {
        for (ItemStack stack : OreDictionary.getOres((String)o)) {
          if (!StackUtil.isEmpty(stack))
            ret.add(stack); 
        } 
      } 
    } else if (o instanceof ItemStack) {
      if (!StackUtil.isEmpty((ItemStack)o))
        ret.add((ItemStack)o); 
    } else if (o.getClass().isArray()) {
      assert Array.getLength(o) != 0 : "empty array";
      for (int i = 0; i < Array.getLength(o); i++)
        ret.addAll(expand(Array.get(o, i))); 
    } else if (o instanceof Iterable) {
      assert ((Iterable)o).iterator().hasNext() : "emtpy iterable";
      for (Object o2 : o)
        ret.addAll(expand(o2)); 
    } else {
      displayError("unknown type", "Input: " + o + "\nType: " + o.getClass().getName(), null, false);
      return null;
    } 
    return ret;
  }
  
  public static List<ItemStack>[] expandArray(Object[] array) {
    List[] arrayOfList = new List[array.length];
    for (int i = 0; i < array.length; i++) {
      if (array[i] == null) {
        arrayOfList[i] = null;
      } else {
        arrayOfList[i] = expand(array[i]);
      } 
    } 
    return (List<ItemStack>[])arrayOfList;
  }
  
  public static void displayError(String cause, String tech, ItemStack result, boolean shapeless) {
    String msg = "An invalid crafting recipe was attempted to be added. This could happen due to a bug in IndustrialCraft 2 or an addon.\n\n(Technical information: Adv" + (shapeless ? "Shapeless" : "") + "Recipe, " + cause + ")\n" + ((result != null) ? ("Output: " + result + "\n") : "") + ((tech != null) ? (tech + "\n") : "") + "Source: " + getCaller();
    if (MainConfig.ignoreInvalidRecipes) {
      IC2.log.warn(LogCategory.Recipe, msg);
      throw new RuntimeException(msg);
    } 
    IC2.platform.displayError(msg, new Object[0]);
  }
  
  private static String getCaller() {
    String ret = "unknown";
    for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
      String className = st.getClassName();
      int pkgSeparator = className.lastIndexOf('.');
      String pkg = (pkgSeparator == -1) ? "" : className.substring(0, pkgSeparator);
      if (!className.equals("ic2.core.recipe.AdvRecipe") && !className.equals("ic2.core.recipe.AdvShapelessRecipe") && 
        !className.equals("ic2.core.recipe.AdvCraftingRecipeManager") && 
        !pkg.startsWith("ic2.api") && !pkg.startsWith("java.")) {
        ret = className + "." + st.getMethodName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")";
        break;
      } 
    } 
    return ret;
  }
  
  private static boolean checkMask(int mask, int[] request) {
    for (int cmpMask : request) {
      if (mask == cmpMask)
        return true; 
    } 
    return false;
  }
  
  static IRecipeInput getRecipeObject(Object o) {
    if (o == null)
      throw new NullPointerException("Null recipe input object."); 
    if (o instanceof IRecipeInput)
      return (IRecipeInput)o; 
    if (o instanceof ItemStack)
      return Recipes.inputFactory.forStack((ItemStack)o); 
    if (o instanceof Block)
      return Recipes.inputFactory.forStack(new ItemStack((Block)o)); 
    if (o instanceof Item)
      return Recipes.inputFactory.forStack(new ItemStack((Item)o)); 
    if (o instanceof String)
      return Recipes.inputFactory.forOreDict((String)o); 
    if (o instanceof Fluid)
      return Recipes.inputFactory.forFluidContainer((Fluid)o); 
    if (o instanceof FluidStack)
      return Recipes.inputFactory.forFluidContainer(((FluidStack)o).getFluid(), ((FluidStack)o).amount); 
    if (o instanceof Iterable) {
      List<IRecipeInput> list = new ArrayList<>();
      for (Object o1 : o)
        list.add(getRecipeObject(o1)); 
      return Recipes.inputFactory.forAny(list);
    } 
    if (o.getClass().isArray()) {
      IRecipeInput[] inputs = new IRecipeInput[Array.getLength(o)];
      for (int i = 0; i < inputs.length; i++)
        inputs[i] = getRecipeObject(Array.get(o, i)); 
      return Recipes.inputFactory.forAny(inputs);
    } 
    throw new IllegalArgumentException("Invalid object found as RecipeInput: " + o);
  }
  
  private ItemStack checkItems(IInventory inventory, IRecipeInput[] request) {
    int size = inventory.func_70302_i_();
    double outputCharge = 0.0D;
    for (int i = 0, j = 0; i < size; i++) {
      ItemStack offer = inventory.func_70301_a(i);
      if (!StackUtil.isEmpty(offer))
        if (request[j++].matches(offer)) {
          outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
        } else {
          return StackUtil.emptyStack;
        }  
    } 
    ItemStack ret = this.output.copy();
    ElectricItem.manager.charge(ret, outputCharge, 2147483647, true, false);
    return ret;
  }
  
  public NonNullList<ItemStack> func_179532_b(InventoryCrafting inv) {
    return this.consuming ? NonNullList.func_191197_a(inv.func_70302_i_(), StackUtil.emptyStack) : ForgeHooks.defaultRecipeGetRemainingItems(inv);
  }
  
  public IRecipe setRegistryName(ResourceLocation name) {
    this.name = name;
    return (IRecipe)this;
  }
  
  public ResourceLocation getRegistryName() {
    return this.name;
  }
  
  public Class<IRecipe> getRegistryType() {
    return IRecipe.class;
  }
  
  public boolean func_194133_a(int x, int y) {
    return (this.inputWidth <= x && this.inputHeight <= y);
  }
  
  public int getRecipeWidth() {
    return this.inputWidth;
  }
  
  public int getRecipeHeight() {
    return this.inputHeight;
  }
  
  public NonNullList<Ingredient> func_192400_c() {
    NonNullList<Ingredient> list = NonNullList.func_191196_a();
    if (!this.hidden) {
      int mask = this.masks[0];
      int actualIngredient = 0;
      for (int x = 0; x < 9; x++) {
        if ((mask >>> 8 - x & 0x1) != 0) {
          list.add(this.input[actualIngredient++].getIngredient());
        } else {
          list.add(Ingredient.field_193370_a);
        } 
      } 
    } 
    return list;
  }
  
  public boolean func_192399_d() {
    return this.hidden;
  }
  
  private static final boolean debug = Util.hasAssertions();
  
  public final ItemStack output;
  
  public final IRecipeInput[] input;
  
  public final IRecipeInput[] inputMirrored;
  
  public final int[] masks;
  
  public final int[] masksMirrored;
  
  public final int inputWidth;
  
  public final int inputHeight;
  
  public final boolean hidden;
  
  public final boolean consuming;
  
  private ResourceLocation name;
}
