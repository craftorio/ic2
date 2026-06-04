package ic2.core.block.state;

import com.google.common.base.Optional;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ic2.core.profile.Version;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.properties.PropertyHelper;

public class EnumProperty<T extends Enum<T> & IIdProvider> extends PropertyHelper<T> {
  private final List<T> values;
  
  private final TIntObjectMap<T> reverseMap;
  
  public EnumProperty(String name, Class<T> cls) {
    super(name, cls);
    Enum[] arrayOfEnum = (Enum[])cls.getEnumConstants();
    if (arrayOfEnum == null || arrayOfEnum.length == 0)
      throw new IllegalArgumentException("No enum constants for " + cls); 
    this.values = Arrays.asList((T[])arrayOfEnum);
    boolean idsMatchOrdinal = true;
    for (int i = 0; i < arrayOfEnum.length; i++) {
      if (((IIdProvider)arrayOfEnum[i]).getId() != i) {
        idsMatchOrdinal = false;
        break;
      } 
    } 
    if (idsMatchOrdinal) {
      this.reverseMap = null;
    } else {
      this.reverseMap = (TIntObjectMap<T>)new TIntObjectHashMap(arrayOfEnum.length);
      for (Enum enum_ : arrayOfEnum)
        this.reverseMap.put(((IIdProvider)enum_).getId(), enum_); 
      if (this.reverseMap.size() != arrayOfEnum.length)
        throw new IllegalArgumentException("The enum " + cls + " provides non-unique ids"); 
    } 
  }
  
  public List<T> func_177700_c() {
    return this.values;
  }
  
  public List<T> getShownValues() {
    Class<T> valueClass = func_177699_b();
    boolean defaultState = Version.shouldEnable(valueClass);
    return (List<T>)this.values.stream().filter(value -> {
          try {
            return Version.shouldEnable(valueClass.getField(value.name()), defaultState);
          } catch (NoSuchFieldException e) {
            throw new RuntimeException("Impossible missing enum field!", e);
          } 
        }).collect(Collectors.toList());
  }
  
  public String func_177702_a(T value) {
    return ((IIdProvider)value).getName();
  }
  
  public Optional<T> func_185929_b(String value) {
    return Optional.fromNullable(getValue(value));
  }
  
  public T getValue(int id) {
    if (this.reverseMap == null) {
      if (id >= 0 && id < this.values.size())
        return this.values.get(id); 
      return null;
    } 
    return (T)this.reverseMap.get(id);
  }
  
  public T getValueOrDefault(int id) {
    T ret = getValue(id);
    return (ret != null) ? ret : getDefault();
  }
  
  public T getValue(String name) {
    for (Enum enum_ : this.values) {
      if (((IIdProvider)enum_).getName().equals(name))
        return (T)enum_; 
    } 
    return null;
  }
  
  public T getValueOrDefault(String name) {
    T ret = getValue(name);
    return (ret != null) ? ret : getDefault();
  }
  
  public T getDefault() {
    return this.values.get(0);
  }
}
