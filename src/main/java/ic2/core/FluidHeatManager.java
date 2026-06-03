package ic2.core;

import ic2.api.recipe.IFluidHeatManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidHeatManager implements IFluidHeatManager {
  public void addFluid(String fluidName, int amount, int heat) {
    if (this.burnProperties.containsKey(fluidName))
      throw new RuntimeException("The fluid " + fluidName + " does already have a burn property assigned."); 
    this.burnProperties.put(fluidName, new IFluidHeatManager.BurnProperty(amount, heat));
  }
  
  public IFluidHeatManager.BurnProperty getBurnProperty(Fluid fluid) {
    if (fluid == null)
      return null; 
    return this.burnProperties.get(fluid.getName());
  }
  
  public boolean acceptsFluid(Fluid fluid) {
    return this.burnProperties.containsKey(fluid.getName());
  }
  
  public Set<Fluid> getAcceptedFluids() {
    Set<Fluid> ret = new HashSet<>();
    for (String fluidName : this.burnProperties.keySet()) {
      Fluid fluid = FluidRegistry.getFluid(fluidName);
      if (fluid != null)
        ret.add(fluid); 
    } 
    return ret;
  }
  
  public Map<String, IFluidHeatManager.BurnProperty> getBurnProperties() {
    return this.burnProperties;
  }
  
  private final Map<String, IFluidHeatManager.BurnProperty> burnProperties = new HashMap<>();
}
