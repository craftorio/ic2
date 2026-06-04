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
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

@NotClassic
public class TileEntityBlockCutter extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    @GuiSynced
    private boolean bladeTooWeak;
    public final InvSlotConsumableClass cutterSlot;
    
    public TileEntityBlockCutter() {
        super(4, 450, 1);
        this.bladeTooWeak = false;
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.blockcutter);
        this.cutterSlot = new InvSlotConsumableClass(this, "cutterInputSlot", 1, IBlockCuttingBlade.class);
    }
    
    public static void init() {
        Recipes.blockcutter = new BasicMachineRecipeManager();
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
        if (this.cutterSlot.isEmpty()) {
            if (!this.bladeTooWeak) {
                this.bladeTooWeak = true;
            }
            return null;
        }
        if (this.bladeTooWeak) {
            this.bladeTooWeak = false;
        }
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
        if (ret == null || ret.getRecipe().getMetaData() == null) {
            return null;
        }
        final ItemStack bladeStack = this.cutterSlot.get();
        final IBlockCuttingBlade blade = (IBlockCuttingBlade)bladeStack.getItem();
        if (ret.getRecipe().getMetaData().getInteger("hardness") > blade.getHardness(bladeStack)) {
            if (!this.bladeTooWeak) {
                this.bladeTooWeak = true;
            }
            return null;
        }
        if (this.bladeTooWeak) {
            this.bladeTooWeak = false;
        }
        return ret;
    }
    
    @Override
    public ContainerBase<TileEntityBlockCutter> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    public boolean getGuiState(final String name) {
        if ("isBladeTooWeak".equals(name)) {
            return this.bladeTooWeak;
        }
        return super.getGuiState(name);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
}
