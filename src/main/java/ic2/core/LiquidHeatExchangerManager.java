package ic2.core;

import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class LiquidHeatExchangerManager implements ILiquidHeatExchangerManager {
  private final boolean heatup;
  
  private final SingleDirectionManager singleDirectionManager;
  
  private Map<String, ILiquidHeatExchangerManager.HeatExchangeProperty> map;
  
  public LiquidHeatExchangerManager(boolean heatup) {
    this.map = new HashMap<>();
    this.heatup = heatup;
    this.singleDirectionManager = new SingleDirectionManager();
  }
  
  public boolean acceptsFluid(Fluid fluid) {
    return this.map.containsKey(fluid.getName());
  }
  
  public Set<Fluid> getAcceptedFluids() {
    Set<Fluid> fluidSet = new HashSet<>();
    for (String fluidName : this.map.keySet())
      fluidSet.add(FluidRegistry.getFluid(fluidName)); 
    return fluidSet;
  }
  
  public void addFluid(String fluidName, String fluidOutput, int huPerMB) {
    if (this.map.containsKey(fluidName)) {
      displayError("The fluid " + fluidName + " does already have a HeatExchangerProperty assigned.");
      return;
    } 
    if (huPerMB == 0) {
      displayError("A mod tried to register a Fluid for the HeatExchanging recipe, without having an Energy value. Ignoring...");
      return;
    } 
    Fluid liquid1 = FluidRegistry.getFluid(fluidName);
    Fluid liquid2 = FluidRegistry.getFluid(fluidOutput);
    if (liquid1 == null || liquid2 == null) {
      displayError("Could not get both fluids for " + fluidName + " and " + fluidOutput + ".");
      return;
    } 
    if (this.heatup) {
      if (liquid1.getTemperature() >= liquid2.getTemperature())
        displayError("Cannot heat up a warm liquid into a cold one. " + fluidName + " -> " + fluidOutput); 
    } else if (liquid1.getTemperature() <= liquid2.getTemperature()) {
      displayError("Cannot cool down a cold liquid into a warm one. " + fluidName + " -> " + fluidOutput);
    } 
    this.map.put(fluidName, new ILiquidHeatExchangerManager.HeatExchangeProperty(FluidRegistry.getFluid(fluidOutput), Math.abs(huPerMB)));
  }
  
  public ILiquidHeatExchangerManager.HeatExchangeProperty getHeatExchangeProperty(Fluid fluid) {
    if (this.map.containsKey(fluid.getName()))
      return this.map.get(fluid.getName()); 
    return null;
  }
  
  public Map<String, ILiquidHeatExchangerManager.HeatExchangeProperty> getHeatExchangeProperties() {
    return this.map;
  }
  
  private void displayError(String msg) {
    if (MainConfig.ignoreInvalidRecipes) {
      IC2.log.warn(LogCategory.Recipe, msg);
    } else {
      throw new RuntimeException(msg);
    } 
  }
  
  public ILiquidAcceptManager getSingleDirectionLiquidManager() {
    return this.singleDirectionManager;
  }
  
  public ILiquidHeatExchangerManager getOpposite() {
    return this.heatup ? Recipes.liquidCooldownManager : Recipes.liquidHeatupManager;
  }
  
  public class SingleDirectionManager implements ILiquidAcceptManager {
    public boolean acceptsFluid(Fluid fluid) {
      if (!LiquidHeatExchangerManager.this.acceptsFluid(fluid))
        return false; 
      ILiquidHeatExchangerManager.HeatExchangeProperty property = LiquidHeatExchangerManager.this.getHeatExchangeProperty(fluid);
      return !LiquidHeatExchangerManager.this.getOpposite().acceptsFluid(property.outputFluid);
    }
    
    public Set<Fluid> getAcceptedFluids() {
      Set<Fluid> ret = new HashSet<>();
      ILiquidHeatExchangerManager opposite = LiquidHeatExchangerManager.this.getOpposite();
      for (Map.Entry<String, ILiquidHeatExchangerManager.HeatExchangeProperty> e : (Iterable<Map.Entry<String, ILiquidHeatExchangerManager.HeatExchangeProperty>>)LiquidHeatExchangerManager.this.map.entrySet()) {
        if (!opposite.acceptsFluid(((ILiquidHeatExchangerManager.HeatExchangeProperty)e.getValue()).outputFluid))
          ret.add(FluidRegistry.getFluid(e.getKey())); 
      } 
      return ret;
    }
  }
}
