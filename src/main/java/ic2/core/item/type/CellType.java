package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.crop.TileEntityCrop;
import ic2.core.profile.NotExperimental;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

@NotExperimental
public enum CellType implements IIdProvider {
   empty(0),
   water(1, FluidRegistry.WATER),
   lava(2, FluidRegistry.LAVA),
   air(3, FluidName.air.getInstance()),
   electrolyzed_water(4),
   biofuel(5),
   coalfuel(6),
   bio(7),
   hydrated_coal(8),
   weed_ex(9),
   hydration(10);

   private final int id;
   final Fluid fluid;

   CellType(int id) {
      this(id, null);
   }

   CellType(int id, Fluid fluid) {
      this.id = id;
      this.fluid = fluid;
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public int getId() {
      return this.id;
   }

   public int getStackSize() {
      return this != weed_ex && this != hydration ? 64 : 1;
   }

   public boolean isFluidContainer() {
      return this.fluid != null || this == empty;
   }

   public boolean hasCropAction() {
      return this == water || this == weed_ex || this == hydration;
   }

   public int getUsage(ItemStack stack) {
      switch (this) {
         case weed_ex:
            return stack.hasTagCompound() ? stack.getTagCompound().getInteger("weedEX") : 0;
         case hydration:
            return stack.hasTagCompound() ? stack.getTagCompound().getInteger("hydration") : 0;
         default:
            return 0;
      }
   }

   public int getMaximum(ItemStack stack) {
      switch (this) {
         case weed_ex:
            return 64;
         case hydration:
            return 10000;
         default:
            return 0;
      }
   }

   public EnumActionResult doCropAction(ItemStack stack, Consumer<ItemStack> result, TileEntityCrop crop, boolean manual) {
      assert this.hasCropAction();
      switch (this) {
         case weed_ex: {
            IFluidHandlerItem handler = new CellType.WeedExHandler(stack);
            if (crop.applyWeedEx(handler, manual)) {
               result.accept(handler.getContainer());
               return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.FAIL;
         }
         case hydration: {
            IFluidHandlerItem handler = new CellType.HydrationHandler(stack, manual);
            if (crop.applyHydration(handler)) {
               result.accept(handler.getContainer());
               return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.FAIL;
         }
         case water:
            if (crop.getStorageWater() < 10) {
               crop.setStorageWater(10);
               return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.FAIL;
         default:
            throw new IllegalStateException("Type was " + this);
      }
   }

   public static class CellFluidHandler extends FluidBucketWrapper {
      private static final Map<Fluid, CellType> VALID_FLUIDS = new IdentityHashMap<>(
         Arrays.stream(CellType.values()).filter(type -> type.fluid != null).collect(Collectors.toMap(type -> type.fluid, Function.identity()))
      );
      protected final Supplier<CellType> typeGetter;

      public CellFluidHandler(ItemStack container, Function<ItemStack, CellType> typeGetter) {
         super(container);
         this.typeGetter = () -> typeGetter.apply(this.container);
      }

      @Override
      public FluidStack getFluid() {
         CellType type = this.typeGetter.get();
         assert type.isFluidContainer();
         return type != null && type.fluid != null ? new FluidStack(type.fluid, 1000) : null;
      }

      @Override
      public boolean canFillFluidType(FluidStack fluid) {
         assert fluid != null;
         assert fluid.getFluid() != null;
         return this.typeGetter.get() == CellType.empty && VALID_FLUIDS.containsKey(fluid.getFluid());
      }

      @Override
      protected void setFluid(FluidStack stack) {
         if (stack == null) {
            assert this.typeGetter.get() != CellType.empty;
            this.container = ItemName.cell.getItemStack(CellType.empty);
         } else {
            assert this.typeGetter.get() == CellType.empty;
            assert VALID_FLUIDS.containsKey(stack.getFluid());
            this.container = ItemName.cell.getItemStack(VALID_FLUIDS.get(stack.getFluid()));
         }
      }
   }

   private static class HydrationHandler implements IFluidHandlerItem {
      public static final String NBT = "hydration";
      public static final int CHARGES = 10000;
      protected ItemStack container;
      protected final boolean manual;

      public HydrationHandler(ItemStack stack, boolean manual) {
         this.container = stack;
         this.manual = manual;
      }

      public ItemStack getContainer() {
         return this.container;
      }

      @Override
      public int fill(FluidStack resource, boolean doFill) {
         return 0;
      }

      @Override
      public FluidStack drain(FluidStack resource, boolean doDrain) {
         return resource != null && resource.getFluid() == FluidRegistry.WATER ? this.drain(resource.amount, doDrain) : null;
      }

      @Override
      public FluidStack drain(int maxDrain, boolean doDrain) {
         int remaining;
         if (this.container.hasTagCompound()) {
            remaining = 10000 - this.container.getTagCompound().getInteger("hydration");
         } else {
            remaining = 10000;
         }

         int target = Math.min(maxDrain, remaining);
         if (!this.manual && target > 180) {
            target = 180;
         }

         if (doDrain) {
            NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.container);
            int amount = nbt.getInteger("hydration") + target;
            if (amount >= 10000) {
               this.container = StackUtil.decSize(this.container);
            } else {
               nbt.setInteger("hydration", amount);
            }
         }

         return new FluidStack(FluidRegistry.WATER, target);
      }

      @Override
      public IFluidTankProperties[] getTankProperties() {
         return new IFluidTankProperties[]{
            new FluidTankProperties(new FluidStack(FluidRegistry.WATER, (10000 - this.container.getItemDamage()) / 200), 50, false, true)
         };
      }
   }

   private static class WeedExHandler implements IFluidHandlerItem {
      public static final String NBT = "weedEX";
      public static final int CHARGES = 64;
      private static final int DRAIN = 50;
      protected ItemStack container;

      public WeedExHandler(ItemStack stack) {
         this.container = stack;
      }

      public ItemStack getContainer() {
         return this.container;
      }

      @Override
      public int fill(FluidStack resource, boolean doFill) {
         return 0;
      }

      @Override
      public FluidStack drain(FluidStack resource, boolean doDrain) {
         return resource != null && resource.getFluid() == FluidName.weed_ex.getInstance() ? this.drain(resource.amount, doDrain) : null;
      }

      @Override
      public FluidStack drain(int maxDrain, boolean doDrain) {
         if (maxDrain < 50) {
            return null;
         }

         if (doDrain) {
            NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.container);
            int amount = nbt.getInteger("weedEX") + 1;
            if (amount >= 64) {
               this.container = StackUtil.decSize(this.container);
            } else {
               nbt.setInteger("weedEX", amount);
            }
         }

         return new FluidStack(FluidName.weed_ex.getInstance(), 50);
      }

      @Override
      public IFluidTankProperties[] getTankProperties() {
         return new IFluidTankProperties[]{
            new FluidTankProperties(new FluidStack(FluidName.weed_ex.getInstance(), (64 - this.container.getItemDamage()) * 50), 3200, false, true)
         };
      }
   }
}
