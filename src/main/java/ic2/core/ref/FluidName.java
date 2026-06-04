// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import ic2.core.profile.Both;
import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;

@NotClassic
public enum FluidName implements IIdProvider
{
    air(false), 
    biogas(false), 
    biomass, 
    construction_foam, 
    @Both
    coolant, 
    distilled_water, 
    hot_coolant, 
    hot_water, 
    pahoehoe_lava(false), 
    steam(false), 
    superheated_steam(false), 
    uu_matter, 
    weed_ex(false), 
    oxygen(false), 
    hydrogen(false), 
    heavy_water, 
    deuterium(false), 
    creosote, 
    molten_brass(false), 
    molten_bronze(false), 
    molten_copper(false), 
    molten_gold(false), 
    molten_iron(false), 
    molten_lead(false), 
    molten_silver(false), 
    molten_steel(false), 
    molten_tin(false), 
    molten_zinc(false), 
    milk;
    
    public static final FluidName[] values;
    private final boolean hasFlowTexture;
    private Fluid instance;
    
    private FluidName() {
        this(true);
    }
    
    private FluidName(final boolean hasFlowTexture) {
        this.hasFlowTexture = hasFlowTexture;
    }
    
    @Override
    public String getName() {
        return "ic2" + this.name();
    }
    
    @Override
    public int getId() {
        throw new UnsupportedOperationException();
    }
    
    public ResourceLocation getTextureLocation(final boolean flowing) {
        if (this.name().startsWith("molten_")) {
            return new ResourceLocation("ic2", "blocks/fluid/molten_metal");
        }
        final String type = (flowing && this.hasFlowTexture) ? "flow" : "still";
        return new ResourceLocation("ic2", "blocks/fluid/" + this.name() + "_" + type);
    }
    
    public boolean hasInstance() {
        return this.instance != null;
    }
    
    public Fluid getInstance() {
        if (this.instance == null) {
            throw new IllegalStateException("the requested fluid instance for " + this.name() + " isn't set (yet)");
        }
        return this.instance;
    }
    
    public void setInstance(final Fluid fluid) {
        if (fluid == null) {
            throw new NullPointerException("null fluid");
        }
        if (this.instance != null) {
            throw new IllegalStateException("conflicting instance");
        }
        this.instance = fluid;
    }
    
    static {
        values = values();
    }
}
