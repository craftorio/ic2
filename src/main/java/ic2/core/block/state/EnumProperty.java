package ic2.core.block.state;

import com.google.common.base.Optional;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ic2.core.profile.Version;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.properties.PropertyHelper;

public class EnumProperty<T extends Enum<T> & IIdProvider> extends PropertyHelper<T> {
   private final List<T> values;
   private final TIntObjectMap<T> reverseMap;

   public EnumProperty(String name, Class<T> cls) {
      super(name, cls);
      T[] values = cls.getEnumConstants();
      if (values != null && values.length != 0) {
         this.values = Arrays.asList(values);
         boolean idsMatchOrdinal = true;

         for (int i = 0; i < values.length; i++) {
            if (values[i].getId() != i) {
               idsMatchOrdinal = false;
               break;
            }
         }

         if (idsMatchOrdinal) {
            this.reverseMap = null;
         } else {
            this.reverseMap = new TIntObjectHashMap(values.length);

            for (T value : values) {
               this.reverseMap.put(value.getId(), value);
            }

            if (this.reverseMap.size() != values.length) {
               throw new IllegalArgumentException("The enum " + cls + " provides non-unique ids");
            }
         }
      } else {
         throw new IllegalArgumentException("No enum constants for " + cls);
      }
   }

   public List<T> getAllowedValues() {
      return this.values;
   }

   public List<T> getShownValues() {
      Class<T> valueClass = this.getValueClass();
      boolean defaultState = Version.shouldEnable(valueClass);
      return this.values.stream().filter(value -> {
         try {
            return Version.shouldEnable(valueClass.getField(value.name()), defaultState);
         } catch (NoSuchFieldException e) {
            throw new RuntimeException("Impossible missing enum field!", e);
         }
      }).collect(Collectors.toList());
   }

   public String getName(T value) {
      return value.getName();
   }

   public Optional<T> parseValue(String value) {
      return Optional.fromNullable(this.getValue(value));
   }

   public T getValue(int id) {
      if (this.reverseMap == null) {
         return id >= 0 && id < this.values.size() ? this.values.get(id) : null;
      } else {
         return (T)this.reverseMap.get(id);
      }
   }

   public T getValueOrDefault(int id) {
      T ret = this.getValue(id);
      return ret != null ? ret : this.getDefault();
   }

   public T getValue(String name) {
      for (T value : this.values) {
         if (value.getName().equals(name)) {
            return value;
         }
      }

      return null;
   }

   public T getValueOrDefault(String name) {
      T ret = this.getValue(name);
      return ret != null ? ret : this.getDefault();
   }

   public T getDefault() {
      return this.values.get(0);
   }
}
