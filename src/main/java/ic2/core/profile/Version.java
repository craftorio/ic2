package ic2.core.profile;

import ic2.core.IC2;
import java.lang.reflect.AnnotatedElement;

public enum Version {
  NEW, BOTH, OLD;
  
  public boolean isExperimental() {
    return (this == NEW);
  }
  
  public boolean isClassic() {
    return (this == OLD);
  }
  
  public static boolean shouldEnable(AnnotatedElement e) {
    return shouldEnable(e, true);
  }
  
  public static boolean shouldEnable(AnnotatedElement e, boolean defaultState) {
    if (e.isAnnotationPresent((Class)NotExperimental.class))
      return !IC2.version.isExperimental(); 
    if (e.isAnnotationPresent((Class)NotClassic.class))
      return !IC2.version.isClassic(); 
    return (e.isAnnotationPresent((Class)Both.class) || defaultState);
  }
}
