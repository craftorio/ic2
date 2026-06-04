// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiMetalFormer;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

@NotClassic
public class TileEntityMetalFormer extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> implements INetworkClientTileEntityEventListener
{
    private int mode;
    public static final int EventSwitch = 0;
    
    public TileEntityMetalFormer() {
        super(10, 200, 1);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.metalformerExtruding);
    }
    
    public static void init() {
        Recipes.metalformerExtruding = new BasicMachineRecipeManager();
        Recipes.metalformerCutting = new BasicMachineRecipeManager();
        Recipes.metalformerRolling = new BasicMachineRecipeManager();
        if (ConfigUtil.getBool(MainConfig.get(), "recipes/allowCoinCrafting")) {}
    }
    
    public static void addRecipeCutting(final ItemStack input, final int amount, final ItemStack output) {
        addRecipeCutting(Recipes.inputFactory.forStack(input, amount), output);
    }
    
    public static void addRecipeCutting(final IRecipeInput input, final ItemStack output) {
        Recipes.metalformerCutting.addRecipe(input, null, false, output);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.setMode(nbt.getInteger("mode"));
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("mode", this.mode);
        return nbt;
    }
    
    @Override
    public ContainerBase<TileEntityMetalFormer> getGuiContainer(final EntityPlayer player) {
        return new ContainerMetalFormer(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiMetalFormer(new ContainerMetalFormer(player, this));
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0: {
                this.cycleMode();
                break;
            }
        }
    }
    
    public void onNetworkUpdate(final String field) {
        super.onNetworkUpdate(field);
        if (field.equals("mode")) {
            this.setMode(this.mode);
        }
    }
    
    public int getMode() {
        return this.mode;
    }
    
    public void setMode(final int mode1) {
        final InvSlotProcessableGeneric slot = (InvSlotProcessableGeneric)this.inputSlot;
        switch (mode1) {
            case 0: {
                slot.setRecipeManager(Recipes.metalformerExtruding);
                break;
            }
            case 1: {
                slot.setRecipeManager(Recipes.metalformerRolling);
                break;
            }
            case 2: {
                slot.setRecipeManager(Recipes.metalformerCutting);
                break;
            }
            default: {
                throw new RuntimeException("invalid mode: " + mode1);
            }
        }
        this.mode = mode1;
    }
    
    private void cycleMode() {
        this.setMode((this.getMode() + 1) % 3);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
