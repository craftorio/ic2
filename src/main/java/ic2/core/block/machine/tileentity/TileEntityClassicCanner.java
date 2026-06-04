// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.audio.PositionSpec;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiClassicCanner;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import ic2.core.item.type.CellType;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.Item;
import ic2.core.util.StackUtil;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlot;
import ic2.core.audio.AudioSource;
import ic2.core.ref.TeBlock;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;

@TeBlock.Delegated(current = TileEntityCanner.class, old = TileEntityClassicCanner.class)
public class TileEntityClassicCanner extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, INetworkTileEntityEventListener
{
    public short progress;
    public int energyConsume;
    public int operationLength;
    private int fuelQuality;
    protected Mode mode;
    protected AudioSource audioSource;
    public final InvSlot resInputSlot;
    public final InvSlotConsumable inputSlot;
    public final InvSlotOutput outputSlot;
    
    public TileEntityClassicCanner() {
        super(600, 1);
        this.progress = 0;
        this.fuelQuality = 0;
        this.energyConsume = 1;
        this.operationLength = 600;
        this.resInputSlot = new InvSlot(this, "input", InvSlot.Access.I, 1);
        this.inputSlot = new InvSlotConsumableItemStack(this, "canInput", 1, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.tin_can), ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can) }) {
            @Override
            public boolean accepts(final ItemStack stack) {
                if (StackUtil.isEmpty(stack)) {
                    return false;
                }
                final Item item = stack.getItem();
                return item == ItemName.jetpack.getInstance() || item == ItemName.cf_pack.getInstance() || super.accepts(stack);
            }
            
            @Override
            public void onChanged() {
                super.onChanged();
                TileEntityClassicCanner.this.mode = TileEntityClassicCanner.this.getMode();
            }
        };
        this.outputSlot = new InvSlotOutput(this, "output", 1);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.fuelQuality = nbt.getInteger("fuelQuality");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("fuelQuality", this.fuelQuality);
        return nbt;
    }
    
    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("progress".equals(name)) {
            int l = this.operationLength;
            if (this.mode == Mode.FOOD && !this.resInputSlot.isEmpty()) {
                final int food = getFoodValue(this.resInputSlot.get());
                if (food > 0) {
                    l = 50 * food;
                }
            }
            if (this.mode == Mode.CF) {
                l = 50;
            }
            return this.progress / (double)l;
        }
        throw new IllegalArgumentException("Unexpected name: " + name);
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        final boolean canOperate = this.canOperate();
        if (canOperate && this.energy.useEnergy(this.energyConsume)) {
            this.setActive(true);
            if (this.progress == 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
            }
            ++this.progress;
            if ((this.mode == Mode.FOOD && this.progress >= getFoodValue(this.resInputSlot.get()) * 50) || (this.mode == Mode.FUEL && this.progress > 0 && this.progress % 100 == 0) || (this.mode == Mode.CF && this.progress >= 50)) {
                if (this.mode != Mode.FUEL || this.progress >= 600) {
                    this.operate(false);
                    this.fuelQuality = 0;
                    this.progress = 0;
                }
                else {
                    this.operate(true);
                }
                needsInvUpdate = true;
                IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
            }
            else if (this.progress % 50 == 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
                IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
            }
        }
        else {
            if (this.getActive() && this.progress > 0) {
                IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
            }
            if (!canOperate && this.mode != Mode.FUEL) {
                this.fuelQuality = 0;
                this.progress = 0;
            }
            this.setActive(false);
        }
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    public void operate(final boolean incremental) {
        switch (this.mode) {
            case FOOD: {
                final MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle.apply(new ICannerBottleRecipeManager.RawInput(this.inputSlot.get(), this.resInputSlot.get()), false);
                this.outputSlot.add(result.getOutput());
                final ICannerBottleRecipeManager.RawInput newInput = result.getAdjustedInput();
                this.inputSlot.put(newInput.container);
                this.resInputSlot.put(newInput.fill);
                break;
            }
            case FUEL: {
                final int fuel = this.getFuelValue(this.resInputSlot.get());
                this.resInputSlot.put(StackUtil.decSize(this.resInputSlot.get()));
                this.fuelQuality += fuel;
                if (incremental) {
                    break;
                }
                if (StackUtil.checkItemEquality(this.inputSlot.get(), ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can))) {
                    this.inputSlot.consume(1);
                    final ItemStack result2 = ItemName.filled_fuel_can.getItemStack();
                    final NBTTagCompound data = StackUtil.getOrCreateNbtData(result2);
                    data.setInteger("value", this.fuelQuality);
                    this.outputSlot.add(result2);
                    break;
                }
                int damage = this.inputSlot.get().getItemDamage();
                damage -= this.fuelQuality;
                if (damage < 1) {
                    damage = 1;
                }
                this.inputSlot.clear();
                this.outputSlot.add(new ItemStack(ItemName.jetpack.getInstance(), 1, damage));
                break;
            }
            case CF: {
                this.resInputSlot.put(StackUtil.decSize(this.resInputSlot.get()));
                final ItemStack cfPack = this.inputSlot.get();
                cfPack.setItemDamage(cfPack.getItemDamage() + 2);
                if (this.resInputSlot.isEmpty() || cfPack.getItemDamage() > cfPack.getMaxDamage() - 2) {
                    this.outputSlot.add(cfPack);
                    this.inputSlot.clear();
                    break;
                }
                this.inputSlot.put(cfPack);
                break;
            }
            default: {
                assert false;
                break;
            }
        }
    }
    
    public boolean canOperate() {
        if (this.inputSlot.isEmpty() || this.resInputSlot.isEmpty()) {
            return false;
        }
        switch (this.mode) {
            case FOOD: {
                return ((IMachineRecipeManager<Object, Object, ICannerBottleRecipeManager.RawInput>)Recipes.cannerBottle).apply(new ICannerBottleRecipeManager.RawInput(this.inputSlot.get(), this.resInputSlot.get()), false) != null;
            }
            case FUEL: {
                final int fuel = this.getFuelValue(this.resInputSlot.get());
                return fuel > 0 && this.outputSlot.canAdd(ItemName.jetpack.getItemStack());
            }
            case CF: {
                final ItemStack cfPack = this.inputSlot.get();
                return cfPack.getItemDamage() <= cfPack.getMaxDamage() - 2 && getPelletValue(this.resInputSlot.get()) > 0 && this.outputSlot.canAdd(cfPack);
            }
            default: {
                assert false;
                return false;
            }
        }
    }
    
    public Mode getMode() {
        if (!this.inputSlot.isEmpty()) {
            final ItemStack input = this.inputSlot.get();
            if (StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.tin_can))) {
                return Mode.FOOD;
            }
            if (StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can)) || input.getItem() == ItemName.jetpack.getInstance()) {
                return Mode.FUEL;
            }
            if (input.getItem() == ItemName.cf_pack.getInstance()) {
                return Mode.CF;
            }
        }
        return Mode.NONE;
    }
    
    public static int getFoodValue(final ItemStack stack) {
        final MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle.apply(new ICannerBottleRecipeManager.RawInput(StackUtil.copyWithSize(ItemName.crafting.getItemStack(CraftingItemType.tin_can), Integer.MAX_VALUE), stack), false);
        return (result == null) ? 0 : StackUtil.getSize(result.getOutput());
    }
    
    public int getFuelValue(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.coalfuel))) {
            return 2548;
        }
        if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.biofuel))) {
            return 868;
        }
        if (stack.getItem() == Items.REDSTONE && this.fuelQuality > 0) {
            return (int)(this.fuelQuality * 0.2);
        }
        if (stack.getItem() == Items.GLOWSTONE_DUST && this.fuelQuality > 0) {
            return (int)(this.fuelQuality * 0.3);
        }
        if (stack.getItem() == Items.GUNPOWDER && this.fuelQuality > 0) {
            return (int)(this.fuelQuality * 0.4);
        }
        return 0;
    }
    
    public static int getPelletValue(final ItemStack item) {
        if (StackUtil.isEmpty(item)) {
            return 0;
        }
        return StackUtil.checkItemEquality(item, ItemName.crafting.getItemStack(CraftingItemType.pellet)) ? StackUtil.getSize(item) : 0;
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerClassicCanner(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiClassicCanner(new ContainerClassicCanner(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer entityPlayer) {
    }
    
    public String getStartSoundFile() {
        return "Machines/CannerOp.ogg";
    }
    
    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }
    
    @Override
    public void onNetworkEvent(final int event) {
        if (this.audioSource == null) {
            this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
        }
        switch (event) {
            case 0: {
                if (this.audioSource != null) {
                    this.audioSource.play();
                    break;
                }
                break;
            }
            case 2: {
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    break;
                }
                break;
            }
            case 1: {
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
                    break;
                }
                break;
            }
        }
    }
    
    private enum Mode
    {
        NONE, 
        FOOD, 
        FUEL, 
        CF;
    }
}
