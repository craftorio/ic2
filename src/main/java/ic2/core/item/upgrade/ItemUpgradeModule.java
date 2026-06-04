package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import ic2.api.item.ElectricItem;
import ic2.api.item.ICustomDamageItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.upgrade.IFullUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.state.IIdProvider;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.init.Localization;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUpgradeModule extends ItemMulti<ItemUpgradeModule.UpgradeType> implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo {
  public ItemUpgradeModule() {
    super(ItemName.upgrade, UpgradeType.class);
    func_77627_a(true);
    for (UpgradeType type : UpgradeType.values())
      UpgradeRegistry.register(new ItemStack((Item)this, 1, type.getId())); 
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(final ItemName name) {
    ModelLoader.setCustomMeshDefinition((Item)this, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            ItemUpgradeModule.UpgradeType type = (ItemUpgradeModule.UpgradeType)ItemUpgradeModule.this.getType(stack);
            if (type == null)
              return new ModelResourceLocation("builtin/missing", "missing"); 
            EnumFacing dir;
            if (type.directional && (dir = ItemUpgradeModule.getDirection(stack)) != null)
              return ItemIC2.getModelLocation(name, type.getName() + '_' + dir.func_176610_l()); 
            return ItemIC2.getModelLocation(name, type.getName());
          }
        });
    for (UpgradeType type : this.typeProperty.func_177700_c()) {
      ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, type.getName()) });
      if (type.directional)
        for (EnumFacing dir : EnumFacing.field_82609_l) {
          ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, type.getName() + '_' + dir.func_176610_l()) });
        }  
    } 
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add("Machine Upgrade");
    return info;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    String side;
    super.func_77624_a(stack, world, tooltip, advanced);
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return; 
    switch (type) {
      case IGNORED:
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.time", new Object[] { decimalformat
                .format(100.0D * Math.pow(getProcessTimeMultiplier(stack, (IUpgradableBlock)null), StackUtil.getSize(stack))) }));
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.overclocker.power", new Object[] { decimalformat
                .format(100.0D * Math.pow(getEnergyDemandMultiplier(stack, (IUpgradableBlock)null), StackUtil.getSize(stack))) }));
        break;
      case FUZZY:
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.transformer", new Object[] { Integer.valueOf(getExtraTier(stack, (IUpgradableBlock)null) * StackUtil.getSize(stack)) }));
        break;
      case EXACT:
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.storage", new Object[] { Integer.valueOf(getExtraEnergyStorage(stack, (IUpgradableBlock)null) * StackUtil.getSize(stack)) }));
        break;
      case null:
      case null:
        side = getSideName(stack);
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", new Object[] { Localization.translate(side) }));
        break;
      case null:
      case null:
        side = getSideName(stack);
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", new Object[] { Localization.translate(side) }));
        break;
      case null:
        side = getSideName(stack);
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", new Object[] { Localization.translate(side) }));
        break;
      case null:
        side = getSideName(stack);
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", new Object[] { Localization.translate(side) }));
        break;
      case null:
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.redstone"));
        break;
      case null:
        tooltip.add(Localization.translate("ic2.tooltip.upgrade.remote_interface", new Object[] { Integer.valueOf(StackUtil.getSize(stack)) }));
        break;
    } 
  }
  
  private static String getSideName(ItemStack stack) {
    EnumFacing dir = getDirection(stack);
    if (dir == null)
      return "ic2.tooltip.upgrade.ejector.anyside"; 
    switch (dir) {
      case IGNORED:
        return "ic2.dir.west";
      case FUZZY:
        return "ic2.dir.east";
      case EXACT:
        return "ic2.dir.bottom";
      case null:
        return "ic2.dir.top";
      case null:
        return "ic2.dir.north";
      case null:
        return "ic2.dir.south";
    } 
    throw new RuntimeException("invalid dir: " + dir);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
    ItemStack stack = StackUtil.get(player, hand);
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return EnumActionResult.PASS; 
    if (type.directional) {
      int dir = 1 + side.ordinal();
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      if (nbtData.func_74771_c("dir") == dir) {
        nbtData.func_74774_a("dir", (byte)0);
      } else {
        nbtData.func_74774_a("dir", (byte)dir);
      } 
      if (IC2.platform.isRendering())
        switch (type) {
          case null:
          case null:
            IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", new Object[] { Localization.translate(getSideName(stack)) }), new Object[0]);
            break;
          case null:
          case null:
            IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", new Object[] { Localization.translate(getSideName(stack)) }), new Object[0]);
            break;
          case null:
            IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", new Object[] { Localization.translate(getSideName(stack)) }), new Object[0]);
            break;
          case null:
            IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", new Object[] { Localization.translate(getSideName(stack)) }), new Object[0]);
            break;
        }  
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type != null)
      switch (type) {
        case null:
        case null:
          if (!(player.getEntityWorld()).isRemote && !StackUtil.isEmpty(stack) && player.field_71070_bA instanceof DynamicHandHeldContainer) {
            HandHeldInventory base = (HandHeldInventory)((DynamicHandHeldContainer)player.field_71070_bA).base;
            if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack)) {
              base.saveAsThrown(stack);
              player.func_71053_j();
            } 
          } 
          break;
      }  
    return true;
  }
  
  public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return false; 
    switch (type) {
      case null:
      case null:
        return types.contains(UpgradableProperty.ItemProducing);
      case null:
      case null:
        return types.contains(UpgradableProperty.ItemConsuming);
      case null:
        return types.contains(UpgradableProperty.FluidProducing);
      case null:
        return types.contains(UpgradableProperty.FluidConsuming);
      case EXACT:
        return types.contains(UpgradableProperty.EnergyStorage);
      case IGNORED:
        return (types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable));
      case null:
        return types.contains(UpgradableProperty.RedstoneSensitive);
      case FUZZY:
        return types.contains(UpgradableProperty.Transformer);
      case null:
        return types.contains(UpgradableProperty.RemotelyAccessible);
    } 
    return false;
  }
  
  public int getAugmentation(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return 0; 
    switch (type) {
      case IGNORED:
        return 1;
    } 
    return 0;
  }
  
  public int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent) {
    return 0;
  }
  
  public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return 1.0D; 
    switch (type) {
      case IGNORED:
        return 0.7D;
    } 
    return 1.0D;
  }
  
  public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent) {
    return 0;
  }
  
  public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return 1.0D; 
    switch (type) {
      case IGNORED:
        return 1.6D;
    } 
    return 1.0D;
  }
  
  public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return 0; 
    switch (type) {
      case EXACT:
        return 10000;
    } 
    return 0;
  }
  
  public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent) {
    return 1.0D;
  }
  
  public int getExtraTier(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return 0; 
    switch (type) {
      case FUZZY:
        return 1;
    } 
    return 0;
  }
  
  public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return false; 
    switch (type) {
      case null:
        return true;
    } 
    return false;
  }
  
  public int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return externalInput; 
    switch (type) {
      case null:
        return 15 - externalInput;
    } 
    return externalInput;
  }
  
  public int getRangeAmplification(ItemStack stack, IUpgradableBlock parent, int existingRange) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return existingRange; 
    switch (type) {
      case null:
        return existingRange << 1;
    } 
    return existingRange;
  }
  
  public boolean onTick(ItemStack stack, IUpgradableBlock parent) {
    int amount;
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return false; 
    int size = StackUtil.getSize(stack);
    TileEntity te = (TileEntity)parent;
    boolean ret = false;
    switch (type) {
      case null:
        amount = (int)Math.pow(4.0D, Math.min(4, size - 1));
        for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
          StackUtil.transfer(te, inv.te, inv.dir, amount); 
        return ret;
      case null:
        amount = (int)Math.pow(4.0D, Math.min(4, size - 1));
        for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
          StackUtil.transfer(te, inv.te, inv.dir, amount, stackChecker(stack)); 
        return ret;
      case null:
        amount = (int)Math.pow(4.0D, Math.min(4, size - 1));
        for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
          StackUtil.transfer(inv.te, te, inv.dir.func_176734_d(), amount); 
        return ret;
      case null:
        amount = (int)Math.pow(4.0D, Math.min(4, size - 1));
        for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
          StackUtil.transfer(inv.te, te, inv.dir.func_176734_d(), amount, stackChecker(stack)); 
        return ret;
      case null:
        if (!LiquidUtil.isFluidTile(te, null))
          return false; 
        amount = (int)(50.0D * Math.pow(4.0D, Math.min(4, size - 1)));
        for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
          LiquidUtil.transfer(te, fh.dir, fh.handler, amount); 
        return ret;
      case null:
        if (!LiquidUtil.isFluidTile(te, null))
          return false; 
        amount = (int)(50.0D * Math.pow(4.0D, Math.min(4, size - 1)));
        for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
          LiquidUtil.transfer(fh.handler, fh.dir.func_176734_d(), te, amount); 
        return ret;
    } 
    return false;
  }
  
  private static Predicate<ItemStack> stackChecker(final ItemStack stack) {
    return new Predicate<ItemStack>() {
        private boolean hasInitialised = false;
        
        private Set<ItemStack> filters;
        
        private Settings meta;
        
        private Settings damage;
        
        private Settings energy;
        
        private NbtSettings nbt;
        
        private void initalise() {
          assert !this.hasInitialised;
          NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
          this.filters = getFilterStacks(tag);
          this.meta = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "meta"));
          this.damage = null;
          this.nbt = NbtSettings.getFromNBT(HandHeldAdvancedUpgrade.getTag(tag, "nbt").func_74771_c("type"));
          this.energy = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "energy"));
          this.hasInitialised = true;
        }
        
        private Set<ItemStack> getFilterStacks(NBTTagCompound nbt) {
          Set<ItemStack> ret = new HashSet<>();
          NBTTagList contentList = nbt.func_150295_c("Items", 10);
          for (int tag = 0; tag < contentList.func_74745_c(); tag++) {
            NBTTagCompound slotNbt = contentList.func_150305_b(tag);
            int slot = slotNbt.func_74771_c("Slot");
            if (slot >= 0 && slot < 9) {
              ItemStack filter = new ItemStack(slotNbt);
              if (!StackUtil.isEmpty(filter))
                ret.add(filter); 
            } 
          } 
          return ret;
        }
        
        private boolean checkMeta(ItemStack stack, ItemStack filter) {
          assert this.meta.active;
          assert this.meta.comparison == ComparisonType.DIRECT;
          return (stack.func_77960_j() == filter.func_77960_j());
        }
        
        private boolean checkDamage(ItemStack stack, ItemStack filter, boolean customStack) {
          assert this.damage.active;
          assert this.damage.comparison == ComparisonType.DIRECT;
          return (customStack && filter.getItem() instanceof ICustomDamageItem) ? (
            
            (((ICustomDamageItem)stack.getItem()).getCustomDamage(stack) == ((ICustomDamageItem)filter.getItem()).getCustomDamage(filter))) : (
            
            (filter.func_77952_i() == stack.func_77952_i()));
        }
        
        private boolean checkNBT(ItemStack stack, ItemStack filter) {
          switch (this.nbt) {
            case IGNORED:
              return true;
            case FUZZY:
              return StackUtil.checkNbtEquality(stack.func_77978_p(), filter.func_77978_p());
            case EXACT:
              return StackUtil.checkNbtEqualityStrict(stack, filter);
          } 
          throw new IllegalStateException("Unexpected NBT state: " + this.nbt);
        }
        
        private boolean checkEnergy(ItemStack stack, ItemStack filter) {
          assert this.energy.active;
          assert this.energy.comparison == ComparisonType.DIRECT;
          return (filter.getItem() instanceof ic2.api.item.IElectricItem && 
            Util.isSimilar(ElectricItem.manager.getCharge(stack), ElectricItem.manager.getCharge(filter)));
        }
        
        public boolean apply(ItemStack stack) {
          boolean checkMeta, checkEnergy;
          if (!this.hasInitialised)
            initalise(); 
          if (!this.meta.comparison.ignoreFilters()) {
            if (this.meta.doComparison(stack.func_77960_j())) {
              checkMeta = false;
            } else {
              return false;
            } 
          } else {
            checkMeta = this.meta.active;
          } 
          boolean customStack = stack.getItem() instanceof ICustomDamageItem;
          boolean checkDamage = false;
          if (!this.energy.comparison.ignoreFilters()) {
            if (stack.getItem() instanceof ic2.api.item.IElectricItem && this.energy.doComparison((int)ElectricItem.manager.getCharge(stack))) {
              checkEnergy = false;
            } else {
              return false;
            } 
          } else {
            checkEnergy = this.energy.active;
            if (checkEnergy && !(stack.getItem() instanceof ic2.api.item.IElectricItem))
              return false; 
          } 
          for (ItemStack filter : this.filters) {
            if (filter.getItem() == stack.getItem() && (!checkMeta || 
              checkMeta(stack, filter)) && (!checkDamage || 
              checkDamage(stack, filter, customStack)) && 
              checkNBT(stack, filter) && (!checkEnergy || 
              checkEnergy(stack, filter)))
              return true; 
          } 
          return (this.filters.isEmpty() && this.meta.active && !checkMeta && this.energy.active && !checkEnergy);
        }
      };
  }
  
  private static List<StackUtil.AdjacentInv> getTargetInventories(ItemStack stack, TileEntity parent) {
    EnumFacing dir = getDirection(stack);
    if (dir == null)
      return StackUtil.getAdjacentInventories(parent); 
    StackUtil.AdjacentInv inv = StackUtil.getAdjacentInventory(parent, dir);
    if (inv == null)
      return emptyInvList; 
    return Collections.singletonList(inv);
  }
  
  private static List<LiquidUtil.AdjacentFluidHandler> getTargetFluidHandlers(ItemStack stack, TileEntity parent) {
    EnumFacing dir = getDirection(stack);
    if (dir == null)
      return LiquidUtil.getAdjacentHandlers(parent); 
    LiquidUtil.AdjacentFluidHandler fh = LiquidUtil.getAdjacentHandler(parent, dir);
    if (fh == null)
      return emptyFhList; 
    return Collections.singletonList(fh);
  }
  
  public Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock parent, Collection<ItemStack> output) {
    return output;
  }
  
  public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return null; 
    switch (type) {
      case null:
      case null:
        return (IHasGui)new HandHeldAdvancedUpgrade(player, stack);
    } 
    return null;
  }
  
  public IHasGui getSubInventory(EntityPlayer player, ItemStack stack, int ID) {
    UpgradeType type = (UpgradeType)getType(stack);
    if (type == null)
      return null; 
    switch (type) {
      case null:
      case null:
        return HandHeldAdvancedUpgrade.delegate(player, stack, ID);
    } 
    return null;
  }
  
  private static EnumFacing getDirection(ItemStack stack) {
    int rawDir = StackUtil.getOrCreateNbtData(stack).func_74771_c("dir");
    if (rawDir < 1 || rawDir > 6)
      return null; 
    return EnumFacing.field_82609_l[rawDir - 1];
  }
  
  public enum UpgradeType implements IIdProvider {
    overclocker(false),
    transformer(false),
    energy_storage(false),
    redstone_inverter(false),
    ejector(true),
    advanced_ejector(true),
    pulling(true),
    advanced_pulling(true),
    fluid_ejector(true),
    fluid_pulling(true),
    remote_interface(false);
    
    public final boolean directional;
    
    UpgradeType(boolean directional) {
      this.directional = directional;
    }
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
  
  private static final DecimalFormat decimalformat = new DecimalFormat("0.##");
  
  private static final List<StackUtil.AdjacentInv> emptyInvList = Collections.emptyList();
  
  private static final List<LiquidUtil.AdjacentFluidHandler> emptyFhList = Collections.emptyList();
}
