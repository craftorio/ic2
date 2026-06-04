// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;
import ic2.core.IHasGui;
import java.util.Collection;
import java.util.Collections;
import ic2.core.util.Util;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.ICustomDamageItem;
import net.minecraft.nbt.NBTTagList;
import java.util.HashSet;
import com.google.common.base.Predicate;
import net.minecraft.tileentity.TileEntity;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.init.Localization;
import ic2.api.upgrade.IUpgradableBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.LinkedList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.util.EnumFacing;
import ic2.core.item.ItemIC2;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.ItemMeshDefinition;
import ic2.api.upgrade.UpgradeRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.List;
import java.text.DecimalFormat;
import ic2.api.item.IItemHudInfo;
import ic2.core.item.IHandHeldSubInventory;
import ic2.api.upgrade.IFullUpgrade;
import ic2.core.item.ItemMulti;

public class ItemUpgradeModule extends ItemMulti<UpgradeType> implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo
{
    private static final DecimalFormat decimalformat;
    private static final List<StackUtil.AdjacentInv> emptyInvList;
    private static final List<LiquidUtil.AdjacentFluidHandler> emptyFhList;
    
    public ItemUpgradeModule() {
        super(ItemName.upgrade, UpgradeType.class);
        this.setHasSubtypes(true);
        for (final UpgradeType type : UpgradeType.values()) {
            UpgradeRegistry.register(new ItemStack((Item)this, 1, type.getId()));
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final UpgradeType type = ItemUpgradeModule.this.getType(stack);
                if (type == null) {
                    return new ModelResourceLocation("builtin/missing", "missing");
                }
                final EnumFacing dir;
                if (type.directional && (dir = getDirection(stack)) != null) {
                    return ItemIC2.getModelLocation(name, type.getName() + '_' + dir.getName());
                }
                return ItemIC2.getModelLocation(name, type.getName());
            }
        });
        for (final UpgradeType type : this.typeProperty.getAllowedValues()) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, type.getName()) });
            if (type.directional) {
                for (final EnumFacing dir : EnumFacing.VALUES) {
                    ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, type.getName() + '_' + dir.getName()) });
                }
            }
        }
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add("Machine Upgrade");
        return info;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)tooltip, advanced);
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return;
        }
        switch (type) {
            case overclocker: {
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.time", ItemUpgradeModule.decimalformat.format(100.0 * Math.pow(this.getProcessTimeMultiplier(stack, null), StackUtil.getSize(stack)))));
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.power", ItemUpgradeModule.decimalformat.format(100.0 * Math.pow(this.getEnergyDemandMultiplier(stack, null), StackUtil.getSize(stack)))));
                break;
            }
            case transformer: {
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.transformer", this.getExtraTier(stack, null) * StackUtil.getSize(stack)));
                break;
            }
            case energy_storage: {
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.storage", this.getExtraEnergyStorage(stack, null) * StackUtil.getSize(stack)));
                break;
            }
            case ejector:
            case advanced_ejector: {
                final String side = getSideName(stack);
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
                break;
            }
            case pulling:
            case advanced_pulling: {
                final String side = getSideName(stack);
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
                break;
            }
            case fluid_ejector: {
                final String side = getSideName(stack);
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
                break;
            }
            case fluid_pulling: {
                final String side = getSideName(stack);
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
                break;
            }
            case redstone_inverter: {
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.redstone"));
                break;
            }
            case remote_interface: {
                tooltip.add(Localization.translate("ic2.tooltip.upgrade.remote_interface", StackUtil.getSize(stack)));
                break;
            }
        }
    }
    
    private static String getSideName(final ItemStack stack) {
        final EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return "ic2.tooltip.upgrade.ejector.anyside";
        }
        switch (dir) {
            case WEST: {
                return "ic2.dir.west";
            }
            case EAST: {
                return "ic2.dir.east";
            }
            case DOWN: {
                return "ic2.dir.bottom";
            }
            case UP: {
                return "ic2.dir.top";
            }
            case NORTH: {
                return "ic2.dir.north";
            }
            case SOUTH: {
                return "ic2.dir.south";
            }
            default: {
                throw new RuntimeException("invalid dir: " + dir);
            }
        }
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        final ItemStack stack = StackUtil.get(player, hand);
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return EnumActionResult.PASS;
        }
        if (type.directional) {
            final int dir = 1 + side.ordinal();
            final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            if (nbtData.getByte("dir") == dir) {
                nbtData.setByte("dir", (byte)0);
            }
            else {
                nbtData.setByte("dir", (byte)dir);
            }
            if (IC2.platform.isRendering()) {
                switch (type) {
                    case ejector:
                    case advanced_ejector: {
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    }
                    case pulling:
                    case advanced_pulling: {
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    }
                    case fluid_ejector: {
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    }
                    case fluid_pulling: {
                        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))), new Object[0]);
                        break;
                    }
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    public boolean onDroppedByPlayer(final ItemStack stack, final EntityPlayer player) {
        final UpgradeType type = this.getType(stack);
        if (type != null) {
            switch (type) {
                case advanced_ejector:
                case advanced_pulling: {
                    if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof DynamicHandHeldContainer) {
                        final HandHeldInventory base = (HandHeldInventory)((DynamicHandHeldContainer)player.openContainer).base;
                        if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack)) {
                            base.saveAsThrown(stack);
                            player.closeScreen();
                        }
                        break;
                    }
                    break;
                }
            }
        }
        return true;
    }
    
    public boolean isSuitableFor(final ItemStack stack, final Set<UpgradableProperty> types) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        }
        switch (type) {
            case ejector:
            case advanced_ejector: {
                return types.contains(UpgradableProperty.ItemProducing);
            }
            case pulling:
            case advanced_pulling: {
                return types.contains(UpgradableProperty.ItemConsuming);
            }
            case fluid_ejector: {
                return types.contains(UpgradableProperty.FluidProducing);
            }
            case fluid_pulling: {
                return types.contains(UpgradableProperty.FluidConsuming);
            }
            case energy_storage: {
                return types.contains(UpgradableProperty.EnergyStorage);
            }
            case overclocker: {
                return types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable);
            }
            case redstone_inverter: {
                return types.contains(UpgradableProperty.RedstoneSensitive);
            }
            case transformer: {
                return types.contains(UpgradableProperty.Transformer);
            }
            case remote_interface: {
                return types.contains(UpgradableProperty.RemotelyAccessible);
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public int getAugmentation(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case overclocker: {
                return 1;
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public int getExtraProcessTime(final ItemStack stack, final IUpgradableBlock parent) {
        return 0;
    }
    
    @Override
    public double getProcessTimeMultiplier(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return 1.0;
        }
        switch (type) {
            case overclocker: {
                return 0.7;
            }
            default: {
                return 1.0;
            }
        }
    }
    
    @Override
    public int getExtraEnergyDemand(final ItemStack stack, final IUpgradableBlock parent) {
        return 0;
    }
    
    @Override
    public double getEnergyDemandMultiplier(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return 1.0;
        }
        switch (type) {
            case overclocker: {
                return 1.6;
            }
            default: {
                return 1.0;
            }
        }
    }
    
    @Override
    public int getExtraEnergyStorage(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case energy_storage: {
                return 10000;
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public double getEnergyStorageMultiplier(final ItemStack stack, final IUpgradableBlock parent) {
        return 1.0;
    }
    
    @Override
    public int getExtraTier(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case transformer: {
                return 1;
            }
            default: {
                return 0;
            }
        }
    }
    
    @Override
    public boolean modifiesRedstoneInput(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        }
        switch (type) {
            case redstone_inverter: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public int getRedstoneInput(final ItemStack stack, final IUpgradableBlock parent, final int externalInput) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return externalInput;
        }
        switch (type) {
            case redstone_inverter: {
                return 15 - externalInput;
            }
            default: {
                return externalInput;
            }
        }
    }
    
    @Override
    public int getRangeAmplification(final ItemStack stack, final IUpgradableBlock parent, final int existingRange) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return existingRange;
        }
        switch (type) {
            case remote_interface: {
                return existingRange << 1;
            }
            default: {
                return existingRange;
            }
        }
    }
    
    public boolean onTick(final ItemStack stack, final IUpgradableBlock parent) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return false;
        }
        final int size = StackUtil.getSize(stack);
        final TileEntity te = (TileEntity)parent;
        final boolean ret = false;
        switch (type) {
            case ejector: {
                final int amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                for (final StackUtil.AdjacentInv inv : getTargetInventories(stack, te)) {
                    StackUtil.transfer(te, inv.te, inv.dir, amount);
                }
                break;
            }
            case advanced_ejector: {
                final int amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                for (final StackUtil.AdjacentInv inv : getTargetInventories(stack, te)) {
                    StackUtil.transfer(te, inv.te, inv.dir, amount, stackChecker(stack));
                }
                break;
            }
            case pulling: {
                final int amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                for (final StackUtil.AdjacentInv inv : getTargetInventories(stack, te)) {
                    StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount);
                }
                break;
            }
            case advanced_pulling: {
                final int amount = (int)Math.pow(4.0, Math.min(4, size - 1));
                for (final StackUtil.AdjacentInv inv : getTargetInventories(stack, te)) {
                    StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount, stackChecker(stack));
                }
                break;
            }
            case fluid_ejector: {
                if (!LiquidUtil.isFluidTile(te, null)) {
                    return false;
                }
                final int amount = (int)(50.0 * Math.pow(4.0, Math.min(4, size - 1)));
                for (final LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te)) {
                    LiquidUtil.transfer(te, fh.dir, fh.handler, amount);
                }
                break;
            }
            case fluid_pulling: {
                if (!LiquidUtil.isFluidTile(te, null)) {
                    return false;
                }
                final int amount = (int)(50.0 * Math.pow(4.0, Math.min(4, size - 1)));
                for (final LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te)) {
                    LiquidUtil.transfer(fh.handler, fh.dir.getOpposite(), te, amount);
                }
                break;
            }
            default: {
                return false;
            }
        }
        return ret;
    }
    
    private static Predicate<ItemStack> stackChecker(final ItemStack stack) {
        return (Predicate<ItemStack>)new Predicate<ItemStack>() {
            private boolean hasInitialised = false;
            private Set<ItemStack> filters;
            private Settings meta;
            private Settings damage;
            private Settings energy;
            private NbtSettings nbt;
            
            private void initalise() {
                assert !this.hasInitialised;
                final NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
                this.filters = this.getFilterStacks(tag);
                this.meta = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "meta"));
                this.damage = null;
                this.nbt = NbtSettings.getFromNBT(HandHeldAdvancedUpgrade.getTag(tag, "nbt").getByte("type"));
                this.energy = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "energy"));
                this.hasInitialised = true;
            }
            
            private Set<ItemStack> getFilterStacks(final NBTTagCompound nbt) {
                final Set<ItemStack> ret = new HashSet<ItemStack>();
                final NBTTagList contentList = nbt.getTagList("Items", 10);
                for (int tag = 0; tag < contentList.tagCount(); ++tag) {
                    final NBTTagCompound slotNbt = contentList.getCompoundTagAt(tag);
                    final int slot = slotNbt.getByte("Slot");
                    if (slot >= 0 && slot < 9) {
                        final ItemStack filter = new ItemStack(slotNbt);
                        if (!StackUtil.isEmpty(filter)) {
                            ret.add(filter);
                        }
                    }
                }
                return ret;
            }
            
            private boolean checkMeta(final ItemStack stack, final ItemStack filter) {
                assert this.meta.active;
                assert this.meta.comparison == ComparisonType.DIRECT;
                return stack.getMetadata() == filter.getMetadata();
            }
            
            private boolean checkDamage(final ItemStack stack, final ItemStack filter, final boolean customStack) {
                assert this.damage.active;
                assert this.damage.comparison == ComparisonType.DIRECT;
                return (customStack && filter.getItem() instanceof ICustomDamageItem) ? (((ICustomDamageItem)stack.getItem()).getCustomDamage(stack) == ((ICustomDamageItem)filter.getItem()).getCustomDamage(filter)) : (filter.getItemDamage() == stack.getItemDamage());
            }
            
            private boolean checkNBT(final ItemStack stack, final ItemStack filter) {
                switch (this.nbt) {
                    case IGNORED: {
                        return true;
                    }
                    case FUZZY: {
                        return StackUtil.checkNbtEquality(stack.getTagCompound(), filter.getTagCompound());
                    }
                    case EXACT: {
                        return StackUtil.checkNbtEqualityStrict(stack, filter);
                    }
                    default: {
                        throw new IllegalStateException("Unexpected NBT state: " + this.nbt);
                    }
                }
            }
            
            private boolean checkEnergy(final ItemStack stack, final ItemStack filter) {
                assert this.energy.active;
                assert this.energy.comparison == ComparisonType.DIRECT;
                return filter.getItem() instanceof IElectricItem && Util.isSimilar(ElectricItem.manager.getCharge(stack), ElectricItem.manager.getCharge(filter));
            }
            
            public boolean apply(final ItemStack stack) {
                if (!this.hasInitialised) {
                    this.initalise();
                }
                boolean checkMeta;
                if (!this.meta.comparison.ignoreFilters()) {
                    if (!this.meta.doComparison(stack.getMetadata())) {
                        return false;
                    }
                    checkMeta = false;
                }
                else {
                    checkMeta = this.meta.active;
                }
                final boolean customStack = stack.getItem() instanceof ICustomDamageItem;
                final boolean checkDamage = false;
                boolean checkEnergy;
                if (!this.energy.comparison.ignoreFilters()) {
                    if (!(stack.getItem() instanceof IElectricItem) || !this.energy.doComparison((int)ElectricItem.manager.getCharge(stack))) {
                        return false;
                    }
                    checkEnergy = false;
                }
                else {
                    checkEnergy = this.energy.active;
                    if (checkEnergy && !(stack.getItem() instanceof IElectricItem)) {
                        return false;
                    }
                }
                for (final ItemStack filter : this.filters) {
                    if (filter.getItem() == stack.getItem() && (!checkMeta || this.checkMeta(stack, filter)) && (!checkDamage || this.checkDamage(stack, filter, customStack)) && this.checkNBT(stack, filter) && (!checkEnergy || this.checkEnergy(stack, filter))) {
                        return true;
                    }
                }
                return this.filters.isEmpty() && this.meta.active && !checkMeta && this.energy.active && !checkEnergy;
            }
        };
    }
    
    private static List<StackUtil.AdjacentInv> getTargetInventories(final ItemStack stack, final TileEntity parent) {
        final EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return StackUtil.getAdjacentInventories(parent);
        }
        final StackUtil.AdjacentInv inv = StackUtil.getAdjacentInventory(parent, dir);
        if (inv == null) {
            return ItemUpgradeModule.emptyInvList;
        }
        return Collections.singletonList(inv);
    }
    
    private static List<LiquidUtil.AdjacentFluidHandler> getTargetFluidHandlers(final ItemStack stack, final TileEntity parent) {
        final EnumFacing dir = getDirection(stack);
        if (dir == null) {
            return LiquidUtil.getAdjacentHandlers(parent);
        }
        final LiquidUtil.AdjacentFluidHandler fh = LiquidUtil.getAdjacentHandler(parent, dir);
        if (fh == null) {
            return ItemUpgradeModule.emptyFhList;
        }
        return Collections.singletonList(fh);
    }
    
    public Collection<ItemStack> onProcessEnd(final ItemStack stack, final IUpgradableBlock parent, final Collection<ItemStack> output) {
        return output;
    }
    
    @Override
    public IHasGui getInventory(final EntityPlayer player, final ItemStack stack) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return null;
        }
        switch (type) {
            case advanced_ejector:
            case advanced_pulling: {
                return new HandHeldAdvancedUpgrade(player, stack);
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    public IHasGui getSubInventory(final EntityPlayer player, final ItemStack stack, final int ID) {
        final UpgradeType type = this.getType(stack);
        if (type == null) {
            return null;
        }
        switch (type) {
            case advanced_ejector:
            case advanced_pulling: {
                return HandHeldAdvancedUpgrade.delegate(player, stack, ID);
            }
            default: {
                return null;
            }
        }
    }
    
    private static EnumFacing getDirection(final ItemStack stack) {
        final int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
        if (rawDir < 1 || rawDir > 6) {
            return null;
        }
        return EnumFacing.VALUES[rawDir - 1];
    }
    
    static {
        decimalformat = new DecimalFormat("0.##");
        emptyInvList = Collections.emptyList();
        emptyFhList = Collections.emptyList();
    }
    
    public enum UpgradeType implements IIdProvider
    {
        overclocker(false), 
        transformer(false), 
        energy_storage(false), 
        redstone_inverter(false), 
        ejector(true), 
        @NotClassic
        advanced_ejector(true), 
        pulling(true), 
        @NotClassic
        advanced_pulling(true), 
        fluid_ejector(true), 
        fluid_pulling(true), 
        @NotClassic
        remote_interface(false);
        
        public final boolean directional;
        
        private UpgradeType(final boolean directional) {
            this.directional = directional;
        }
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            return this.ordinal();
        }
    }
}
