package ic2.core.gui.dynamic;

import com.google.common.base.Supplier;
import ic2.core.init.Localization;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TextProvider {
  public static ITextProvider of(String text) {
    return text.isEmpty() ? new ConstantEmpty() : new Constant(text);
  }
  
  public static ITextProvider of(final Supplier<String> supplier) {
    return new AbstractTextProvider() {
        public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
          return (String)supplier.get();
        }
        
        public String getConstant(Class<?> baseClass) {
          return (String)supplier.get();
        }
      };
  }
  
  public static ITextProvider ofTranslated(String key) {
    return new Translate(new Constant(key));
  }
  
  public static ITextProvider parse(String text, Class<?> baseClass) {
    Queue<List<AbstractTextProvider>> continuations = Collections.asLifoQueue(new ArrayDeque<>());
    StringBuilder continuationTypes = new StringBuilder();
    char currentType = Character.MIN_VALUE;
    List<AbstractTextProvider> providers = new ArrayList<>();
    StringBuilder part = new StringBuilder(text.length());
    boolean escaped = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (escaped) {
        part.append(c);
        escaped = false;
      } else if (c == '\\') {
        escaped = true;
      } else if (c == '{') {
        finish(part, providers);
        continuations.add(providers);
        continuationTypes.append(currentType);
        currentType = c;
        providers = new ArrayList<>();
      } else if (currentType == '{' && c == ',') {
        finish(part, providers);
        providers.add(null);
      } else if (currentType == '{' && c == '}') {
        finish(part, providers);
        AbstractTextProvider format = null;
        List<AbstractTextProvider> args = new ArrayList<>();
        int start = 0;
        for (int j = start; j < providers.size(); j++) {
          if (providers.get(j) == null) {
            AbstractTextProvider abstractTextProvider = getProvider(providers, start, j);
            if (format == null) {
              format = abstractTextProvider;
            } else {
              args.add(abstractTextProvider);
            } 
            start = j + 1;
          } 
        } 
        AbstractTextProvider provider = getProvider(providers, start, providers.size());
        if (format == null) {
          format = provider;
        } else {
          args.add(provider);
        } 
        if (args.isEmpty()) {
          provider = new Translate(format);
        } else {
          provider = new TranslateFormat(format, args);
        } 
        providers = continuations.remove();
        currentType = continuationTypes.charAt(continuationTypes.length() - 1);
        continuationTypes.setLength(continuationTypes.length() - 1);
        providers.add(provider);
      } else if (c == '%') {
        if (currentType != '%') {
          if (i + 1 < text.length() && text.charAt(i + 1) == '%') {
            part.append('%');
            i++;
          } else {
            finish(part, providers);
            continuations.add(providers);
            continuationTypes.append(currentType);
            currentType = c;
            providers = new ArrayList<>();
          } 
        } else {
          finish(part, providers);
          AbstractTextProvider provider = getResolver(getProvider(providers, 0, providers.size()), baseClass);
          providers = continuations.remove();
          currentType = continuationTypes.charAt(continuationTypes.length() - 1);
          continuationTypes.setLength(continuationTypes.length() - 1);
          providers.add(provider);
        } 
      } else {
        part.append(c);
      } 
    } 
    finish(part, providers);
    if (currentType != '\000')
      return new Constant("ERROR: unfinished token " + currentType + " in " + text); 
    if (escaped)
      return new Constant("ERROR: unfinished escape sequence in " + text); 
    return getProvider(providers, 0, providers.size());
  }
  
  private static void finish(StringBuilder part, List<AbstractTextProvider> providers) {
    if (part.length() == 0)
      return; 
    providers.add(new Constant(part.toString()));
    part.setLength(0);
  }
  
  private static AbstractTextProvider getProvider(List<AbstractTextProvider> providers, int start, int end) {
    assert start <= end;
    if (start == end)
      return new ConstantEmpty(); 
    if (start + 1 == end)
      return providers.get(start); 
    return new Merge(new ArrayList<>(providers.subList(start, end)));
  }
  
  private static AbstractTextProvider getResolver(AbstractTextProvider token, Class<?> baseClass) {
    String staticToken = token.getConstant(baseClass);
    if (staticToken == null)
      return new TokenResolverDynamic(token); 
    String staticResult = resolveToken(staticToken, baseClass, null, emptyTokens());
    if (staticResult != null)
      return new Constant(staticResult); 
    return new TokenResolverStatic(staticToken);
  }
  
  private static String resolveToken(String token, Class<?> baseClass, Object base, Map<String, ITextProvider> tokens) {
    ITextProvider ret = tokens.get(token);
    if (ret != null) {
      if (ret instanceof AbstractTextProvider)
        return ((AbstractTextProvider)ret).getRaw(base, tokens); 
      return ret.get(base, tokens);
    } 
    if (baseClass == null)
      return null; 
    if (token.startsWith("base.")) {
      Object value = retrieve(token, "base.".length(), baseClass, base);
      return toString(value);
    } 
    return null;
  }
  
  private static Object retrieve(String path, int start, Class<?> subjectClass, Object subject) {
    int end;
    do {
      end = path.indexOf('.', start);
      if (end == -1)
        end = path.length(); 
      String part = path.substring(start, end);
      if (part.endsWith("()")) {
        part = part.substring(0, part.length() - "()".length());
        Method method = getMethodOptional(subjectClass, part);
        if (method == null)
          return null; 
        subject = invokeMethodOptional(method, subject);
        if (subject == null)
          return null; 
        subjectClass = subject.getClass();
      } else {
        Field field = getFieldOptional(subjectClass, part);
        if (field == null)
          return null; 
        subject = getFieldValueOptional(field, subject);
        if (subject == null)
          return null; 
        subjectClass = subject.getClass();
      } 
      start = end + 1;
    } while (end != path.length());
    return subject;
  }
  
  private static Method getMethodOptional(Class<?> cls, String name) {
    try {
      return cls.getMethod(name, new Class[0]);
    } catch (NoSuchMethodException e) {
      return null;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static Object invokeMethodOptional(Method method, Object obj) {
    Object ret;
    if (obj == null && !Modifier.isStatic(method.getModifiers()))
      return null; 
    try {
      ret = method.invoke(obj, new Object[0]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
    if (ret == null);
    return ret;
  }
  
  private static Field getFieldOptional(Class<?> cls, String name) {
    try {
      return cls.getField(name);
    } catch (NoSuchFieldException e) {
      return null;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static Object getFieldValueOptional(Field field, Object obj) {
    Object ret;
    if (obj == null && !Modifier.isStatic(field.getModifiers()))
      return null; 
    try {
      ret = field.get(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
    if (ret == null);
    return ret;
  }
  
  private static String toString(Object o) {
    if (o == null)
      return null; 
    return o.toString();
  }
  
  public static Map<String, ITextProvider> emptyTokens() {
    return Collections.emptyMap();
  }
  
  public static interface ITextProvider {
    String get(Object param1Object, Map<String, ITextProvider> param1Map);
    
    String getOptional(Object param1Object, Map<String, ITextProvider> param1Map);
  }
  
  private static abstract class AbstractTextProvider implements ITextProvider {
    private AbstractTextProvider() {}
    
    public final String get(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      String result = getRaw(base, tokens);
      if (result != null)
        return result; 
      return "ERROR";
    }
    
    public final String getOptional(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      return getRaw(base, tokens);
    }
    
    protected abstract String getRaw(Object param1Object, Map<String, TextProvider.ITextProvider> param1Map);
    
    protected abstract String getConstant(Class<?> param1Class);
  }
  
  private static class Constant extends AbstractTextProvider {
    private final String text;
    
    public Constant(String text) {
      this.text = text;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      return this.text;
    }
    
    public String getConstant(Class<?> baseClass) {
      return this.text;
    }
  }
  
  private static class ConstantEmpty extends AbstractTextProvider {
    private ConstantEmpty() {}
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      return "";
    }
    
    public String getConstant(Class<?> baseClass) {
      return "";
    }
  }
  
  private static class Merge extends AbstractTextProvider {
    private final List<TextProvider.AbstractTextProvider> providers;
    
    public Merge(List<TextProvider.AbstractTextProvider> providers) {
      this.providers = providers;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      StringBuilder ret = new StringBuilder();
      for (TextProvider.AbstractTextProvider provider : this.providers) {
        String part = provider.getRaw(base, tokens);
        if (part == null)
          return null; 
        ret.append(part);
      } 
      return ret.toString();
    }
    
    public String getConstant(Class<?> baseClass) {
      StringBuilder ret = new StringBuilder();
      for (TextProvider.AbstractTextProvider provider : this.providers) {
        String part = provider.getConstant(baseClass);
        if (part == null)
          return null; 
        ret.append(part);
      } 
      return ret.toString();
    }
  }
  
  private static class Translate extends AbstractTextProvider {
    private final TextProvider.AbstractTextProvider key;
    
    public Translate(TextProvider.AbstractTextProvider key) {
      this.key = key;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      String key = this.key.getRaw(base, tokens);
      if (key == null)
        return null; 
      return Localization.translate(key);
    }
    
    public String getConstant(Class<?> baseClass) {
      return null;
    }
  }
  
  private static class TranslateFormat extends AbstractTextProvider {
    private final TextProvider.AbstractTextProvider format;
    
    private final List<TextProvider.AbstractTextProvider> args;
    
    public TranslateFormat(TextProvider.AbstractTextProvider format, List<TextProvider.AbstractTextProvider> args) {
      this.format = format;
      this.args = args;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      String format = this.format.getRaw(base, tokens);
      if (format == null)
        return null; 
      Object[] cArgs = new Object[this.args.size()];
      for (int i = 0; i < this.args.size(); i++) {
        String arg = ((TextProvider.AbstractTextProvider)this.args.get(i)).getRaw(base, tokens);
        if (arg == null)
          return null; 
        cArgs[i] = arg;
      } 
      return Localization.translate(format, cArgs);
    }
    
    public String getConstant(Class<?> baseClass) {
      return null;
    }
  }
  
  private static class TokenResolverDynamic extends AbstractTextProvider {
    private final TextProvider.AbstractTextProvider token;
    
    public TokenResolverDynamic(TextProvider.AbstractTextProvider token) {
      this.token = token;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      String token = this.token.getRaw(base, tokens);
      if (token == null)
        return null; 
      return TextProvider.resolveToken(token, (base != null) ? base.getClass() : null, base, tokens);
    }
    
    public String getConstant(Class<?> baseClass) {
      String token = this.token.getConstant(baseClass);
      if (token == null)
        return null; 
      return TextProvider.resolveToken(token, baseClass, null, TextProvider.emptyTokens());
    }
  }
  
  private static class TokenResolverStatic extends AbstractTextProvider {
    private final String token;
    
    public TokenResolverStatic(String token) {
      this.token = token;
    }
    
    public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens) {
      return TextProvider.resolveToken(this.token, (base != null) ? base.getClass() : null, base, tokens);
    }
    
    public String getConstant(Class<?> baseClass) {
      return TextProvider.resolveToken(this.token, baseClass, null, TextProvider.emptyTokens());
    }
  }
}
