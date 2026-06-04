// 
// Decompiled by Procyon v0.6.0
// 

package ic2.bcIntegration;

import buildcraft.lib.fluid.BCFluid;
import net.minecraftforge.fluids.Fluid;
import ic2.api.recipe.Recipes;
import buildcraft.energy.BCEnergyFluids;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;

public class SubModule
{
    public static boolean init() {
        if (ConfigUtil.getBool(MainConfig.get(), "compat/buildcraft/enableBuildcraftFuels")) {
            TileEntityCanner.addBottleRecipe(ItemName.crop_res.getItemStack(CropResItemType.oil_berry), 4, ItemName.fluid_cell.getItemStack(), ItemName.fluid_cell.getItemStack("oil"));
            for (final BCFluid bcFluid : BCEnergyFluids.crudeOil) {
                if (Recipes.semiFluidGenerator.getFuelProperty((Fluid)bcFluid) == null) {
                    Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 16L, 8L);
                }
                if (Recipes.fluidHeatGenerator.getBurnProperty((Fluid)bcFluid) == null) {
                    Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 32);
                }
            }
            for (final BCFluid bcFluid : BCEnergyFluids.oilDense) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 10L, 16L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 32);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.oilDistilled) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 10L, 16L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 32);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.oilHeavy) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 10L, 16L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 32);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.fuelDense) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 128L, 32L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 768);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.fuelLight) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 128L, 32L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 768);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.fuelMixedHeavy) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 128L, 32L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 768);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.fuelMixedLight) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 128L, 32L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 768);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.fuelGaseous) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 45L, 16L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 90);
            }
            for (final BCFluid bcFluid : BCEnergyFluids.oilResidue) {
                Recipes.semiFluidGenerator.addFluid(bcFluid.getName(), 3L, 8L);
                Recipes.fluidHeatGenerator.addFluid(bcFluid.getName(), 10, 6);
            }
        }
        return true;
    }
}
