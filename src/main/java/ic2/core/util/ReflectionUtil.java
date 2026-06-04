// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.lang.reflect.Method;
import ic2.core.IC2;
import ic2.core.network.DataEncoder;
import java.lang.reflect.Field;

public class ReflectionUtil
{
    public static Field getField(final Class<?> clazz, final String... names) {
        for (final String name : names) {
            try {
                final Field ret = clazz.getDeclaredField(name);
                ret.setAccessible(true);
                return ret;
            }
            catch (final NoSuchFieldException e) {}
            catch (final SecurityException e2) {
                throw new RuntimeException(e2);
            }
        }
        return null;
    }
    
    public static Field getField(final Class<?> clazz, final Class<?> type) {
        Field ret = null;
        for (final Field field : clazz.getDeclaredFields()) {
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
    
    public static Field getFieldRecursive(Class<?> clazz, final String fieldName) {
        Field ret = null;
        do {
            try {
                ret = clazz.getDeclaredField(fieldName);
                ret.setAccessible(true);
            }
            catch (final NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        } while (ret == null && clazz != null);
        return ret;
    }
    
    public static Field getFieldRecursive(Class<?> clazz, final Class<?> type, final boolean requireUnique) {
        Field ret = null;
        do {
            for (final Field field : clazz.getDeclaredFields()) {
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
    
    public static <T> T getFieldValue(final Field field, final Object obj) {
        try {
            return (T)field.get(obj);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T getValue(final Object object, final Class<?> type) {
        final Field field = getField(object.getClass(), type);
        if (field == null) {
            return null;
        }
        return getFieldValue(field, object);
    }
    
    public static <T> T getValueRecursive(final Object object, final String fieldName) throws NoSuchFieldException {
        final Field field = getFieldRecursive(object.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        return getFieldValue(field, object);
    }
    
    public static <T> T getValueRecursive(final Object object, final Class<?> type, final boolean requireUnique) throws NoSuchFieldException {
        final Field field = getFieldRecursive(object.getClass(), type, requireUnique);
        if (field == null) {
            throw new NoSuchFieldException(type.getName());
        }
        return getFieldValue(field, object);
    }
    
    public static void setValue(final Object object, final Field field, Object value) {
        if (field.getType().isEnum() && value instanceof Integer) {
            value = field.getType().getEnumConstants()[(int)value];
        }
        try {
            final Object oldValue = field.get(object);
            if (!DataEncoder.copyValue(value, oldValue)) {
                field.set(object, value);
            }
        }
        catch (final Exception e) {
            throw new RuntimeException("can't set field " + field.getName() + " in " + object + " to " + value, e);
        }
    }
    
    public static boolean setValueRecursive(final Object object, final String fieldName, final Object value) {
        final Field field = getFieldRecursive(object.getClass(), fieldName);
        if (field == null) {
            IC2.log.warn(LogCategory.General, "Can't find field %s in %s to set it to %s.", fieldName, object, value);
            return false;
        }
        setValue(object, field, value);
        return true;
    }
    
    public static Method getMethod(final Class<?> clazz, final String[] names, final Class<?>... parameterTypes) {
        for (final String name : names) {
            try {
                final Method ret = clazz.getDeclaredMethod(name, parameterTypes);
                ret.setAccessible(true);
                return ret;
            }
            catch (final NoSuchMethodException e) {}
            catch (final SecurityException e2) {
                throw new RuntimeException(e2);
            }
        }
        return null;
    }
}
