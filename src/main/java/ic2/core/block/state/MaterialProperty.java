// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import java.lang.reflect.Field;
import java.util.HashMap;
import ic2.core.block.ITeBlock;
import java.util.Locale;
import ic2.core.ref.IC2Material;
import java.util.Map;
import com.google.common.base.Optional;
import java.util.Iterator;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import java.util.Collection;
import java.util.List;
import net.minecraft.block.properties.PropertyHelper;

public class MaterialProperty extends PropertyHelper<WrappedMaterial> implements ISkippableProperty
{
    private final int length;
    private final List<WrappedMaterial> values;
    
    public MaterialProperty(final Collection<Material> materials) {
        super("material", (Class)WrappedMaterial.class);
        this.values = new ArrayList<WrappedMaterial>(materials.size());
        for (final Material material : materials) {
            this.values.add(WrappedMaterial.get(material));
        }
        this.length = this.values.size();
    }
    
    public Collection<WrappedMaterial> getAllowedValues() {
        return this.values;
    }
    
    public Optional<WrappedMaterial> parseValue(final String value) {
        for (final WrappedMaterial material : WrappedMaterial.MATERIAL_TO_WRAP.values()) {
            if (material.getName().equals(value)) {
                return (Optional<WrappedMaterial>)Optional.of((Object)material);
            }
        }
        return (Optional<WrappedMaterial>)Optional.absent();
    }
    
    public String getName(final WrappedMaterial value) {
        return value.getName();
    }
    
    public int getId(final WrappedMaterial material) {
        assert this.values.contains(material);
        return this.values.indexOf(material);
    }
    
    public WrappedMaterial getMaterial(final int ID) {
        assert ID >= 0 && ID < this.length;
        return this.values.get(ID % this.length);
    }
    
    public static final class WrappedMaterial implements Comparable<WrappedMaterial>
    {
        private final int id;
        private final String name;
        private final Material material;
        private static int nextId;
        private static final Map<Material, WrappedMaterial> MATERIAL_TO_WRAP;
        
        private WrappedMaterial(final Material material, String name) {
            if (material instanceof IC2Material) {
                name = ((IC2Material)material).name;
            }
            this.material = material;
            this.name = name.toLowerCase(Locale.ENGLISH);
            this.id = WrappedMaterial.nextId++;
        }
        
        public Material getMaterial() {
            return this.material;
        }
        
        public String getName() {
            return this.name;
        }
        
        @Override
        public int compareTo(final WrappedMaterial other) {
            return this.id - other.id;
        }
        
        public static WrappedMaterial get(final Material material) {
            WrappedMaterial ret = WrappedMaterial.MATERIAL_TO_WRAP.get(material);
            if (ret == null) {
                ret = new WrappedMaterial(material, material.getClass().getName());
                WrappedMaterial.MATERIAL_TO_WRAP.put(material, ret);
            }
            return ret;
        }
        
        public static boolean check(final WrappedMaterial state, final ITeBlock teBlock) {
            return teBlock.getMaterial() == state.getMaterial();
        }
        
        static {
            MATERIAL_TO_WRAP = new HashMap<Material, WrappedMaterial>();
            try {
                for (final Field field : Material.class.getFields()) {
                    if (field.getType() == Material.class) {
                        final Material material = (Material)field.get(null);
                        WrappedMaterial.MATERIAL_TO_WRAP.put(material, new WrappedMaterial(material, field.getName()));
                    }
                }
                assert !WrappedMaterial.MATERIAL_TO_WRAP.isEmpty();
            }
            catch (final Exception e) {
                throw new RuntimeException("Error building materials name map", e);
            }
        }
    }
}
