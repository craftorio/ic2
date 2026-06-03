package ic2.core.util;

import ic2.core.IC2;
import ic2.core.network.DataEncoder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
  public static Field getField(Class<?> clazz, String... names) {
    for (String name : names) {
      try {
        Field ret = clazz.getDeclaredField(name);
        ret.setAccessible(true);
        return ret;
      } catch (NoSuchFieldException e) {
      
      } catch (SecurityException e) {
        throw new RuntimeException(e);
      } 
    } 
    return null;
  }
  
  public static Field getField(Class<?> clazz, Class<?> type) {
    Field ret = null;
    for (Field field : clazz.getDeclaredFields()) {
      if (type.isAssignableFrom(field.getType())) {
        if (ret != null)
          return null; 
        field.setAccessible(true);
        ret = field;
      } 
    } 
    return ret;
  }
  
  public static Field getFieldRecursive(Class<?> clazz, String fieldName) {
    Field ret = null;
    do {
      try {
        ret = clazz.getDeclaredField(fieldName);
        ret.setAccessible(true);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } 
    } while (ret == null && clazz != null);
    return ret;
  }
  
  public static Field getFieldRecursive(Class<?> clazz, Class<?> type, boolean requireUnique) {
    Field ret = null;
    do {
      for (Field field : clazz.getDeclaredFields()) {
        if (type.isAssignableFrom(field.getType())) {
          if (!requireUnique) {
            field.setAccessible(true);
            return field;
          } 
          if (ret != null)
            return null; 
          field.setAccessible(true);
          ret = field;
        } 
      } 
      clazz = clazz.getSuperclass();
    } while (ret == null && clazz != null);
    return ret;
  }
  
  public static <T> T getFieldValue(Field field, Object obj) {
    try {
      return (T)field.get(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static <T> T getValue(Object object, Class<?> type) {
    Field field = getField(object.getClass(), type);
    if (field == null)
      return null; 
    return getFieldValue(field, object);
  }
  
  public static <T> T getValueRecursive(Object object, String fieldName) throws NoSuchFieldException {
    Field field = getFieldRecursive(object.getClass(), fieldName);
    if (field == null)
      throw new NoSuchFieldException(fieldName); 
    return getFieldValue(field, object);
  }
  
  public static <T> T getValueRecursive(Object object, Class<?> type, boolean requireUnique) throws NoSuchFieldException {
    Field field = getFieldRecursive(object.getClass(), type, requireUnique);
    if (field == null)
      throw new NoSuchFieldException(type.getName()); 
    return getFieldValue(field, object);
  }
  
  public static void setValue(Object object, Field field, Object value) {
    if (field.getType().isEnum() && value instanceof Integer)
      value = field.getType().getEnumConstants()[((Integer)value).intValue()]; 
    try {
      Object oldValue = field.get(object);
      if (!DataEncoder.copyValue(value, oldValue))
        field.set(object, value); 
    } catch (Exception e) {
      throw new RuntimeException("can't set field " + field.getName() + " in " + object + " to " + value, e);
    } 
  }
  
  public static boolean setValueRecursive(Object object, String fieldName, Object value) {
    Field field = getFieldRecursive(object.getClass(), fieldName);
    if (field == null) {
      IC2.log.warn(LogCategory.General, "Can't find field %s in %s to set it to %s.", new Object[] { fieldName, object, value });
      return false;
    } 
    setValue(object, field, value);
    return true;
  }
  
  public static Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) {
    for (String name : names) {
      try {
        Method ret = clazz.getDeclaredMethod(name, parameterTypes);
        ret.setAccessible(true);
        return ret;
      } catch (NoSuchMethodException e) {
      
      } catch (SecurityException e) {
        throw new RuntimeException(e);
      } 
    } 
    return null;
  }
}
