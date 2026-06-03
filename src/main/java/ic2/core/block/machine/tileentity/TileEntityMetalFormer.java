package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.machine.gui.GuiMetalFormer;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.util.ConfigUtil;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityMetalFormer extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> implements INetworkClientTileEntityEventListener {
  private int mode;
  
  public static final int EventSwitch = 0;
  
  public TileEntityMetalFormer() {
    super(10, 200, 1);
    this.inputSlot = (InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>)new InvSlotProcessableGeneric((IInventorySlotHolder)this, "input", 1, (IMachineRecipeManager)Recipes.metalformerExtruding);
  }
  
  public static void init() {
    Recipes.metalformerExtruding = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
    Recipes.metalformerCutting = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
    Recipes.metalformerRolling = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
    if (ConfigUtil.getBool(MainConfig.get(), "recipes/allowCoinCrafting"));
  }
  
  public static void addRecipeCutting(ItemStack input, int amount, ItemStack output) {
    addRecipeCutting(Recipes.inputFactory.forStack(input, amount), output);
  }
  
  public static void addRecipeCutting(IRecipeInput input, ItemStack output) {
    Recipes.metalformerCutting.addRecipe(input, null, false, new ItemStack[] { output });
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    setMode(nbt.func_74762_e("mode"));
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("mode", this.mode);
    return nbt;
  }
  
  public ContainerBase<TileEntityMetalFormer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityMetalFormer>)new ContainerMetalFormer(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiMetalFormer(new ContainerMetalFormer(player, this));
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    switch (event) {
      case 0:
        cycleMode();
        break;
    } 
  }
  
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (field.equals("mode"))
      setMode(this.mode); 
  }
  
  public int getMode() {
    return this.mode;
  }
  
  public void setMode(int mode1) {
    InvSlotProcessableGeneric slot = (InvSlotProcessableGeneric)this.inputSlot;
    switch (mode1) {
      case 0:
        slot.setRecipeManager((IMachineRecipeManager)Recipes.metalformerExtruding);
        break;
      case 1:
        slot.setRecipeManager((IMachineRecipeManager)Recipes.metalformerRolling);
        break;
      case 2:
        slot.setRecipeManager((IMachineRecipeManager)Recipes.metalformerCutting);
        break;
      default:
        throw new RuntimeException("invalid mode: " + mode1);
    } 
    this.mode = mode1;
  }
  
  private void cycleMode() {
    setMode((getMode() + 1) % 3);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  }
}
