// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import java.util.Iterator;
import net.minecraft.block.Block;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import java.util.Map;
import net.minecraft.block.state.IBlockState;

public class BlockStateUtil
{
    public static String getVariantString(final IBlockState state) {
        final ImmutableMap<IProperty<?>, Comparable<?>> properties = (ImmutableMap<IProperty<?>, Comparable<?>>)state.getProperties();
        if (properties.isEmpty()) {
            return "normal";
        }
        final StringBuilder ret = new StringBuilder();
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
            final IProperty property = entry.getKey();
            if (ret.length() > 0) {
                ret.append(',');
            }
            ret.append(property.getName());
            ret.append('=');
            ret.append(property.getName((Comparable)entry.getValue()));
        }
        return ret.toString();
    }
    
    public static IBlockState getState(final Block block, final String variant) {
        IBlockState ret = block.getDefaultState();
        if (variant.isEmpty() || variant.equals("normal")) {
            return ret;
        }
        int nextPos;
        for (int pos = 0; pos < variant.length(); pos = nextPos + 1) {
            nextPos = variant.indexOf(44, pos);
            if (nextPos == -1) {
                nextPos = variant.length();
            }
            final int sepPos = variant.indexOf(61, pos);
            if (sepPos == -1 || sepPos >= nextPos) {
                return null;
            }
            final String name = variant.substring(pos, sepPos);
            final String value = variant.substring(sepPos + 1, nextPos);
            ret = applyProperty(ret, name, value);
        }
        return ret;
    }
    
    private static <T extends Comparable<T>> IBlockState applyProperty(final IBlockState state, final String name, final String value) {
        IProperty<T> property = null;
        for (final IProperty<?> cProperty : state.getPropertyKeys()) {
            if (cProperty.getName().equals(name)) {
                property = (IProperty<T>)cProperty;
                break;
            }
        }
        if (property == null) {
            return state;
        }
        for (final T cValue : property.getAllowedValues()) {
            if (value.equals(property.getName((Comparable)cValue))) {
                return state.withProperty((IProperty)property, (Comparable)cValue);
            }
        }
        return state;
    }
}
