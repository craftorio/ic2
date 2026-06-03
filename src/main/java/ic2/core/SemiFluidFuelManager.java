package ic2.core;

import ic2.api.recipe.ISemiFluidFuelManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class SemiFluidFuelManager implements ISemiFluidFuelManager {
  public void addFluid(String fluidName, long energyPerMb, long energyPerTick) {
    if (this.fuelProperties.containsKey(fluidName))
      throw new RuntimeException("The fluid " + fluidName + " does already have a fuel property assigned."); 
    this.fuelProperties.put(fluidName, new ISemiFluidFuelManager.FuelProperty(energyPerMb, energyPerTick));
  }
  
  public void removeFluid(String fluidName) {
    this.fuelProperties.remove(fluidName);
  }
  
  public ISemiFluidFuelManager.FuelProperty getFuelProperty(Fluid fluid) {
    if (fluid == null)
      return null; 
    return this.fuelProperties.get(fluid.getName());
  }
  
  public boolean acceptsFluid(Fluid fluid) {
    return (fluid != null && this.fuelProperties.containsKey(fluid.getName()));
  }
  
  public Set<Fluid> getAcceptedFluids() {
    Set<Fluid> ret = new HashSet<>(this.fuelProperties.size() * 2, 0.5F);
    for (String fluidName : this.fuelProperties.keySet()) {
      Fluid fluid = FluidRegistry.getFluid(fluidName);
      if (fluid != null)
        ret.add(fluid); 
    } 
    return ret;
  }
  
  public Map<String, ISemiFluidFuelManager.FuelProperty> getFuelProperties() {
    return Collections.unmodifiableMap(this.fuelProperties);
  }
  
  private final Map<String, ISemiFluidFuelManager.FuelProperty> fuelProperties = new HashMap<>();
}
