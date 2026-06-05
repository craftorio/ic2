package ic2.core.util;

import ic2.core.IC2;
import ic2.core.network.DataEncoder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
   public static Field getField(Class<?> clazz, String... names) {
      String[] var2 = names;
      int var3 = var2.length;
      int var4 = 0;

      while (var4 < var3) {
         String name = var2[var4];

         try {
            Field ret = clazz.getDeclaredField(name);
            ret.setAccessible(true);
            return ret;
         } catch (NoSuchFieldException e) {
            var4++;
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
            if (ret != null) {
               return null;
            }

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

               if (ret != null) {
                  return null;
               }

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
      return field == null ? null : getFieldValue(field, object);
   }

   public static <T> T getValueRecursive(Object object, String fieldName) throws NoSuchFieldException {
      Field field = getFieldRecursive(object.getClass(), fieldName);
      if (field == null) {
         throw new NoSuchFieldException(fieldName);
      } else {
         return getFieldValue(field, object);
      }
   }

   public static <T> T getValueRecursive(Object object, Class<?> type, boolean requireUnique) throws NoSuchFieldException {
      Field field = getFieldRecursive(object.getClass(), type, requireUnique);
      if (field == null) {
         throw new NoSuchFieldException(type.getName());
      } else {
         return getFieldValue(field, object);
      }
   }

   public static void setValue(Object object, Field field, Object value) {
      if (field.getType().isEnum() && value instanceof Integer) {
         value = field.getType().getEnumConstants()[(Integer)value];
      }

      try {
         Object oldValue = field.get(object);
         if (!DataEncoder.copyValue(value, oldValue)) {
            field.set(object, value);
         }
      } catch (Exception e) {
         throw new RuntimeException("can't set field " + field.getName() + " in " + object + " to " + value, e);
      }
   }

   public static boolean setValueRecursive(Object object, String fieldName, Object value) {
      Field field = getFieldRecursive(object.getClass(), fieldName);
      if (field == null) {
         IC2.log.warn(LogCategory.General, "Can't find field %s in %s to set it to %s.", fieldName, object, value);
         return false;
      } else {
         setValue(object, field, value);
         return true;
      }
   }

   public static Method getMethod(Class<?> clazz, String[] names, Class<?>... parameterTypes) {
      String[] var3 = names;
      int var4 = var3.length;
      int var5 = 0;

      while (var5 < var4) {
         String name = var3[var5];

         try {
            Method ret = clazz.getDeclaredMethod(name, parameterTypes);
            ret.setAccessible(true);
            return ret;
         } catch (NoSuchMethodException e) {
            var5++;
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         }
      }

      return null;
   }
}
