// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Iterator;
import java.text.ParseException;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Config;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class UuIndex
{
    public static final UuIndex instance;
    protected final List<IRecipeResolver> resolvers;
    protected final List<ILateRecipeResolver> lateResolvers;
    
    private UuIndex() {
        this.resolvers = new ArrayList<IRecipeResolver>();
        this.lateResolvers = new ArrayList<ILateRecipeResolver>();
    }
    
    public void addResolver(final IRecipeResolver resolver) {
        this.resolvers.add(resolver);
    }
    
    public void addResolver(final ILateRecipeResolver resolver) {
        this.lateResolvers.add(resolver);
    }
    
    public void add(final ItemStack stack, final double value) {
        if (stack == null || stack.getItem() == null) {
            throw new NullPointerException("invalid itemstack to add");
        }
        UuGraph.set(stack, value);
    }
    
    public double get(final ItemStack request) {
        return UuGraph.get(request);
    }
    
    public double getInBuckets(final ItemStack request) {
        double ret = UuGraph.get(request);
        ret *= 1.0E-5;
        return ret;
    }
    
    public void init() {
        this.addResolver(new VanillaSmeltingResolver());
        this.addResolver(new RecipeResolver());
        this.addResolver(new MachineRecipeResolver(Recipes.macerator));
        this.addResolver(new MachineRecipeResolver(Recipes.extractor));
        this.addResolver(new MachineRecipeResolver(Recipes.compressor));
        this.addResolver(new MachineRecipeResolver(Recipes.centrifuge));
        this.addResolver(new MachineRecipeResolver(Recipes.blockcutter));
        this.addResolver(new MachineRecipeResolver(Recipes.blastfurnace));
        this.addResolver(new MachineRecipeResolver(Recipes.metalformerExtruding));
        this.addResolver(new MachineRecipeResolver(Recipes.metalformerCutting));
        this.addResolver(new MachineRecipeResolver(Recipes.metalformerRolling));
        this.addResolver(new MachineRecipeResolver(Recipes.oreWashing));
        this.addResolver(new CannerBottleSolidResolver());
        this.addResolver(new ScrapBoxResolver());
        this.addResolver(new ManualRecipeResolver());
        this.addResolver(new RecyclerResolver());
    }
    
    public void refresh(final boolean reset) {
        Config config = MainConfig.get().getSub("balance/uu-values/world scan");
        Label_0071: {
            if (config == null) {
                IC2.log.info(LogCategory.Uu, "Loading predefined UU world scan values, run /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.");
                config = new Config("uu scan values");
                try {
                    config.load(IC2.class.getResourceAsStream("/assets/ic2/config/uu_scan_values.ini"));
                    break Label_0071;
                }
                catch (final Exception e) {
                    throw new RuntimeException("Error loading base config", e);
                }
            }
            IC2.log.debug(LogCategory.Uu, "Loading UU world scan values from the user config.");
        }
        Iterator<Config.Value> it = config.valueIterator();
        while (it.hasNext()) {
            final Config.Value value = it.next();
            ItemStack stack;
            try {
                stack = ConfigUtil.asStack(value.name);
            }
            catch (final ParseException e2) {
                throw new Config.ParseException("invalid key", value, e2);
            }
            if (stack == null) {
                IC2.log.warn(LogCategory.Uu, "UU world-scan config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
            }
            else {
                this.add(stack, value.getDouble());
            }
        }
        it = MainConfig.get().getSub("balance/uu-values/predefined").valueIterator();
        while (it.hasNext()) {
            final Config.Value value = it.next();
            ItemStack stack;
            try {
                stack = ConfigUtil.asStack(value.name);
            }
            catch (final ParseException e2) {
                throw new Config.ParseException("invalid key", value, e2);
            }
            if (stack == null) {
                IC2.log.warn(LogCategory.Uu, "UU predefined config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
            }
            else {
                this.add(stack, value.getDouble());
            }
        }
        UuGraph.build(reset);
    }
    
    static {
        instance = new UuIndex();
    }
}
