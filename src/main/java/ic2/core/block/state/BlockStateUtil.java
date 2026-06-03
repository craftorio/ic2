package ic2.core.block.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public class BlockStateUtil {
  public static String getVariantString(IBlockState state) {
    ImmutableMap<IProperty<?>, Comparable<?>> properties = state.func_177228_b();
    if (properties.isEmpty())
      return "normal"; 
    StringBuilder ret = new StringBuilder();
    for (UnmodifiableIterator<Map.Entry<IProperty<?>, Comparable<?>>> unmodifiableIterator = properties.entrySet().iterator(); unmodifiableIterator.hasNext(); ) {
      Map.Entry<IProperty<?>, Comparable<?>> entry = unmodifiableIterator.next();
      IProperty property = entry.getKey();
      if (ret.length() > 0)
        ret.append(','); 
      ret.append(property.func_177701_a());
      ret.append('=');
      ret.append(property.func_177702_a(entry.getValue()));
    } 
    return ret.toString();
  }
  
  public static IBlockState getState(Block block, String variant) {
    IBlockState ret = block.func_176223_P();
    if (variant.isEmpty() || variant.equals("normal"))
      return ret; 
    int pos = 0;
    while (pos < variant.length()) {
      int nextPos = variant.indexOf(',', pos);
      if (nextPos == -1)
        nextPos = variant.length(); 
      int sepPos = variant.indexOf('=', pos);
      if (sepPos == -1 || sepPos >= nextPos)
        return null; 
      String name = variant.substring(pos, sepPos);
      String value = variant.substring(sepPos + 1, nextPos);
      ret = applyProperty(ret, name, value);
      pos = nextPos + 1;
    } 
    return ret;
  }
  
  private static <T extends Comparable<T>> IBlockState applyProperty(IBlockState state, String name, String value) {
    IProperty<T> property = null;
    for (IProperty<?> cProperty : (Iterable<IProperty<?>>)state.func_177227_a()) {
      if (cProperty.func_177701_a().equals(name)) {
        property = (IProperty)cProperty;
        break;
      } 
    } 
    if (property == null)
      return state; 
    for (Comparable comparable : property.func_177700_c()) {
      if (value.equals(property.func_177702_a(comparable)))
        return state.func_177226_a(property, comparable); 
    } 
    return state;
  }
}
