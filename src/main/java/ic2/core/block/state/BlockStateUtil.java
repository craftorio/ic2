package ic2.core.block.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public class BlockStateUtil {
   public static String getVariantString(IBlockState state) {
      ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
      if (properties.isEmpty()) {
         return "normal";
      }

      StringBuilder ret = new StringBuilder();
      UnmodifiableIterator var3 = properties.entrySet().iterator();

      while (var3.hasNext()) {
         Entry<IProperty<?>, Comparable<?>> entry = (Entry<IProperty<?>, Comparable<?>>)var3.next();
         IProperty property = entry.getKey();
         if (ret.length() > 0) {
            ret.append(',');
         }

         ret.append(property.getName());
         ret.append('=');
         ret.append(property.getName(entry.getValue()));
      }

      return ret.toString();
   }

   public static IBlockState getState(Block block, String variant) {
      IBlockState ret = block.getDefaultState();
      if (!variant.isEmpty() && !variant.equals("normal")) {
         int pos = 0;

         while (pos < variant.length()) {
            int nextPos = variant.indexOf(44, pos);
            if (nextPos == -1) {
               nextPos = variant.length();
            }

            int sepPos = variant.indexOf(61, pos);
            if (sepPos == -1 || sepPos >= nextPos) {
               return null;
            }

            String name = variant.substring(pos, sepPos);
            String value = variant.substring(sepPos + 1, nextPos);
            ret = applyProperty(ret, name, value);
            pos = nextPos + 1;
         }

         return ret;
      } else {
         return ret;
      }
   }

   private static <T extends Comparable<T>> IBlockState applyProperty(IBlockState state, String name, String value) {
      IProperty<T> property = null;

      for (IProperty<?> cProperty : state.getPropertyKeys()) {
         if (cProperty.getName().equals(name)) {
            property = (IProperty<T>)cProperty;
            break;
         }
      }

      if (property == null) {
         return state;
      }

      for (T cValue : property.getAllowedValues()) {
         if (value.equals(property.getName(cValue))) {
            return state.withProperty(property, cValue);
         }
      }

      return state;
   }
}
