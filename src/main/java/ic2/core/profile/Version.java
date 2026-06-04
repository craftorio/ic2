// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import ic2.core.IC2;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public enum Version
{
    NEW, 
    BOTH, 
    OLD;
    
    public boolean isExperimental() {
        return this == Version.NEW;
    }
    
    public boolean isClassic() {
        return this == Version.OLD;
    }
    
    public static boolean shouldEnable(final AnnotatedElement e) {
        return shouldEnable(e, true);
    }
    
    public static boolean shouldEnable(final AnnotatedElement e, final boolean defaultState) {
        if (e.isAnnotationPresent(NotExperimental.class)) {
            return !IC2.version.isExperimental();
        }
        if (e.isAnnotationPresent(NotClassic.class)) {
            return !IC2.version.isClassic();
        }
        return e.isAnnotationPresent(Both.class) || defaultState;
    }
}
