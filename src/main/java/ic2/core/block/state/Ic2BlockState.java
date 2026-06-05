package ic2.core.block.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer.StateImplementation;
import net.minecraftforge.common.property.IUnlistedProperty;

public class Ic2BlockState extends BlockStateContainer {
   private final Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockState.Ic2BlockStateInstance> index = this.createIndex();

   public Ic2BlockState(Block blockIn, IProperty<?>... properties) {
      super(blockIn, properties);
   }

   protected StateImplementation createState(
      Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties
   ) {
      return new Ic2BlockState.Ic2BlockStateInstance(block, properties);
   }

   private Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockState.Ic2BlockStateInstance> createIndex() {
      Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockState.Ic2BlockStateInstance> ret = new HashMap<>(this.getValidStates().size());
      UnmodifiableIterator var2 = this.getValidStates().iterator();

      while (var2.hasNext()) {
         IBlockState rawState = (IBlockState)var2.next();
         Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
         ret.put(createMap(rawState.getProperties()), state);
         state.clearPropertyValueTable();
      }

      return ret;
   }

   private static Map<IProperty<?>, Comparable<?>> createMap(Map<IProperty<?>, Comparable<?>> src) {
      return new HashMap<>(src);
   }

   public class Ic2BlockStateInstance extends StateImplementation {
      private final Map<IUnlistedProperty<?>, Object> extraProperties;
      private final ThreadLocal<Map<IProperty<?>, Comparable<?>>> tlProperties = new ThreadLocal<Map<IProperty<?>, Comparable<?>>>() {
         protected Map<IProperty<?>, Comparable<?>> initialValue() {
            return Ic2BlockState.createMap(Ic2BlockStateInstance.this.getProperties());
         }
      };

      private Ic2BlockStateInstance(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties) {
         super(block, properties, null);
         this.extraProperties = Collections.emptyMap();
      }

      private Ic2BlockStateInstance(Ic2BlockState.Ic2BlockStateInstance parent, Map<IUnlistedProperty<?>, Object> extraProperties) {
         super(parent.getBlock(), parent.getProperties(), parent.propertyValueTable);
         this.extraProperties = extraProperties;
      }

      public <T extends Comparable<T>, V extends T> Ic2BlockState.Ic2BlockStateInstance withProperty(IProperty<T> property, V value) {
         V prevValue = (V)((Comparable)this.getProperties().get(property));
         if (prevValue == value) {
            return this;
         }

         if (prevValue == null) {
            throw new IllegalArgumentException("invalid property for this state: " + property);
         }

         if (!property.getAllowedValues().contains(value)) {
            throw new IllegalArgumentException("invalid property value " + value + " for property " + property + " (" + property.getName(value) + ')');
         }

         Map<IProperty<?>, Comparable<?>> lookup = this.tlProperties.get();
         lookup.put(property, value);
         Ic2BlockState.Ic2BlockStateInstance ret = Ic2BlockState.this.index.get(lookup);
         lookup.put(property, prevValue);
         if (!this.extraProperties.isEmpty()) {
            ret = Ic2BlockState.this.new Ic2BlockStateInstance(ret, this.extraProperties);
         }

         return ret;
      }

      public <T> Ic2BlockState.Ic2BlockStateInstance withProperty(IUnlistedProperty<T> property, T value) {
         if (property == null) {
            throw new NullPointerException("null property");
         }

         if (this.extraProperties.get(property) == value) {
            return this;
         }

         if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("The value " + value + " (" + value.getClass().getName() + ") is not applicable for " + property);
         }

         Map<IUnlistedProperty<?>, Object> newExtraProperties = new IdentityHashMap<>(this.extraProperties);
         newExtraProperties.put(property, value);
         return Ic2BlockState.this.new Ic2BlockStateInstance(this, newExtraProperties);
      }

      public <T> Ic2BlockState.Ic2BlockStateInstance withProperties(Object... properties) {
         if (properties.length % 2 != 0) {
            throw new IllegalArgumentException("property pairs expected");
         }

         Map<IUnlistedProperty<?>, Object> newExtraProperties = new IdentityHashMap<>(this.extraProperties);

         for (int i = 0; i < properties.length; i += 2) {
            IUnlistedProperty<T> property = (IUnlistedProperty<T>)properties[i];
            if (property == null) {
               throw new NullPointerException("null property");
            }

            T value = (T)properties[i + 1];
            if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
               throw new IllegalArgumentException("The value " + value + " (" + value.getClass().getName() + ") is not applicable for " + property);
            }

            newExtraProperties.put(property, value);
         }

         return newExtraProperties.size() == this.extraProperties.size() && newExtraProperties.equals(this.extraProperties)
            ? this
            : Ic2BlockState.this.new Ic2BlockStateInstance(this, newExtraProperties);
      }

      public boolean hasValue(IUnlistedProperty<?> property) {
         return this.extraProperties.containsKey(property);
      }

      public <T> T getValue(IUnlistedProperty<T> property) {
         return (T)this.extraProperties.get(property);
      }

      public String toString() {
         String ret = super.toString();
         if (!this.extraProperties.isEmpty()) {
            StringBuilder sb = new StringBuilder(ret);
            sb.setCharAt(sb.length() - 1, ';');
            List<Entry<IUnlistedProperty<?>, Object>> entries = new ArrayList<>(this.extraProperties.entrySet());
            Collections.sort(entries, new Comparator<Entry<IUnlistedProperty<?>, Object>>() {
               public int compare(Entry<IUnlistedProperty<?>, Object> a, Entry<IUnlistedProperty<?>, Object> b) {
                  return a.getKey().getName().compareTo(b.getKey().getName());
               }
            });

            for (Entry<IUnlistedProperty<?>, Object> entry : entries) {
               sb.append(entry.getKey().getName());
               sb.append('=');
               sb.append(entry.getValue());
               sb.append(',');
            }

            sb.setCharAt(sb.length() - 1, ']');
            ret = sb.toString();
         }

         return ret;
      }

      public boolean hasExtraProperties() {
         return !this.extraProperties.isEmpty();
      }

      private void clearPropertyValueTable() {
         this.propertyValueTable = null;
      }
   }
}
