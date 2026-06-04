// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import ic2.core.init.Localization;
import java.util.Iterator;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Map;
import com.google.common.base.Supplier;

public class TextProvider
{
    public static ITextProvider of(final String text) {
        return text.isEmpty() ? new ConstantEmpty() : new Constant(text);
    }
    
    public static ITextProvider of(final Supplier<String> supplier) {
        return new AbstractTextProvider() {
            public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
                return (String)supplier.get();
            }
            
            public String getConstant(final Class<?> baseClass) {
                return (String)supplier.get();
            }
        };
    }
    
    public static ITextProvider ofTranslated(final String key) {
        return new Translate(new Constant(key));
    }
    
    public static ITextProvider parse(final String text, final Class<?> baseClass) {
        final Queue<List<AbstractTextProvider>> continuations = Collections.asLifoQueue(new ArrayDeque<List<AbstractTextProvider>>());
        final StringBuilder continuationTypes = new StringBuilder();
        char currentType = '\0';
        List<AbstractTextProvider> providers = new ArrayList<AbstractTextProvider>();
        final StringBuilder part = new StringBuilder(text.length());
        boolean escaped = false;
        for (int i = 0; i < text.length(); ++i) {
            final char c = text.charAt(i);
            if (escaped) {
                part.append(c);
                escaped = false;
            }
            else if (c == '\\') {
                escaped = true;
            }
            else if (c == '{') {
                finish(part, providers);
                continuations.add(providers);
                continuationTypes.append(currentType);
                currentType = c;
                providers = new ArrayList<AbstractTextProvider>();
            }
            else if (currentType == '{' && c == ',') {
                finish(part, providers);
                providers.add(null);
            }
            else if (currentType == '{' && c == '}') {
                finish(part, providers);
                AbstractTextProvider format = null;
                final List<AbstractTextProvider> args = new ArrayList<AbstractTextProvider>();
                int j;
                int start;
                for (start = (j = 0); j < providers.size(); ++j) {
                    if (providers.get(j) == null) {
                        final AbstractTextProvider provider = getProvider(providers, start, j);
                        if (format == null) {
                            format = provider;
                        }
                        else {
                            args.add(provider);
                        }
                        start = j + 1;
                    }
                }
                AbstractTextProvider provider2 = getProvider(providers, start, providers.size());
                if (format == null) {
                    format = provider2;
                }
                else {
                    args.add(provider2);
                }
                if (args.isEmpty()) {
                    provider2 = new Translate(format);
                }
                else {
                    provider2 = new TranslateFormat(format, args);
                }
                providers = continuations.remove();
                currentType = continuationTypes.charAt(continuationTypes.length() - 1);
                continuationTypes.setLength(continuationTypes.length() - 1);
                providers.add(provider2);
            }
            else if (c == '%') {
                if (currentType != '%') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '%') {
                        part.append('%');
                        ++i;
                    }
                    else {
                        finish(part, providers);
                        continuations.add(providers);
                        continuationTypes.append(currentType);
                        currentType = c;
                        providers = new ArrayList<AbstractTextProvider>();
                    }
                }
                else {
                    finish(part, providers);
                    final AbstractTextProvider provider3 = getResolver(getProvider(providers, 0, providers.size()), baseClass);
                    providers = continuations.remove();
                    currentType = continuationTypes.charAt(continuationTypes.length() - 1);
                    continuationTypes.setLength(continuationTypes.length() - 1);
                    providers.add(provider3);
                }
            }
            else {
                part.append(c);
            }
        }
        finish(part, providers);
        if (currentType != '\0') {
            return new Constant("ERROR: unfinished token " + currentType + " in " + text);
        }
        if (escaped) {
            return new Constant("ERROR: unfinished escape sequence in " + text);
        }
        return getProvider(providers, 0, providers.size());
    }
    
    private static void finish(final StringBuilder part, final List<AbstractTextProvider> providers) {
        if (part.length() == 0) {
            return;
        }
        providers.add(new Constant(part.toString()));
        part.setLength(0);
    }
    
    private static AbstractTextProvider getProvider(final List<AbstractTextProvider> providers, final int start, final int end) {
        assert start <= end;
        if (start == end) {
            return new ConstantEmpty();
        }
        if (start + 1 == end) {
            return providers.get(start);
        }
        return new Merge(new ArrayList<AbstractTextProvider>(providers.subList(start, end)));
    }
    
    private static AbstractTextProvider getResolver(final AbstractTextProvider token, final Class<?> baseClass) {
        final String staticToken = token.getConstant(baseClass);
        if (staticToken == null) {
            return new TokenResolverDynamic(token);
        }
        final String staticResult = resolveToken(staticToken, baseClass, null, emptyTokens());
        if (staticResult != null) {
            return new Constant(staticResult);
        }
        return new TokenResolverStatic(staticToken);
    }
    
    private static String resolveToken(final String token, final Class<?> baseClass, final Object base, final Map<String, ITextProvider> tokens) {
        final ITextProvider ret = tokens.get(token);
        if (ret != null) {
            if (ret instanceof AbstractTextProvider) {
                return ((AbstractTextProvider)ret).getRaw(base, tokens);
            }
            return ret.get(base, tokens);
        }
        else {
            if (baseClass == null) {
                return null;
            }
            if (token.startsWith("base.")) {
                final Object value = retrieve(token, "base.".length(), baseClass, base);
                return toString(value);
            }
            return null;
        }
    }
    
    private static Object retrieve(final String path, int start, Class<?> subjectClass, Object subject) {
        int end;
        do {
            end = path.indexOf(46, start);
            if (end == -1) {
                end = path.length();
            }
            String part = path.substring(start, end);
            if (part.endsWith("()")) {
                part = part.substring(0, part.length() - "()".length());
                final Method method = getMethodOptional(subjectClass, part);
                if (method == null) {
                    return null;
                }
                subject = invokeMethodOptional(method, subject);
                if (subject == null) {
                    return null;
                }
                subjectClass = subject.getClass();
            }
            else {
                final Field field = getFieldOptional(subjectClass, part);
                if (field == null) {
                    return null;
                }
                subject = getFieldValueOptional(field, subject);
                if (subject == null) {
                    return null;
                }
                subjectClass = subject.getClass();
            }
            start = end + 1;
        } while (end != path.length());
        return subject;
    }
    
    private static Method getMethodOptional(final Class<?> cls, final String name) {
        try {
            return cls.getMethod(name, (Class<?>[])new Class[0]);
        }
        catch (final NoSuchMethodException e) {
            return null;
        }
        catch (final SecurityException e2) {
            throw new RuntimeException(e2);
        }
    }
    
    private static Object invokeMethodOptional(final Method method, final Object obj) {
        if (obj == null && !Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        Object ret;
        try {
            ret = method.invoke(obj, new Object[0]);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (ret == null) {}
        return ret;
    }
    
    private static Field getFieldOptional(final Class<?> cls, final String name) {
        try {
            return cls.getField(name);
        }
        catch (final NoSuchFieldException e) {
            return null;
        }
        catch (final SecurityException e2) {
            throw new RuntimeException(e2);
        }
    }
    
    private static Object getFieldValueOptional(final Field field, final Object obj) {
        if (obj == null && !Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        Object ret;
        try {
            ret = field.get(obj);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (ret == null) {}
        return ret;
    }
    
    private static String toString(final Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }
    
    public static Map<String, ITextProvider> emptyTokens() {
        return Collections.emptyMap();
    }
    
    private abstract static class AbstractTextProvider implements ITextProvider
    {
        @Override
        public final String get(final Object base, final Map<String, ITextProvider> tokens) {
            final String result = this.getRaw(base, tokens);
            if (result != null) {
                return result;
            }
            return "ERROR";
        }
        
        @Override
        public final String getOptional(final Object base, final Map<String, ITextProvider> tokens) {
            return this.getRaw(base, tokens);
        }
        
        protected abstract String getRaw(final Object p0, final Map<String, ITextProvider> p1);
        
        protected abstract String getConstant(final Class<?> p0);
    }
    
    private static class Constant extends AbstractTextProvider
    {
        private final String text;
        
        public Constant(final String text) {
            this.text = text;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            return this.text;
        }
        
        public String getConstant(final Class<?> baseClass) {
            return this.text;
        }
    }
    
    private static class ConstantEmpty extends AbstractTextProvider
    {
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            return "";
        }
        
        public String getConstant(final Class<?> baseClass) {
            return "";
        }
    }
    
    private static class Merge extends AbstractTextProvider
    {
        private final List<AbstractTextProvider> providers;
        
        public Merge(final List<AbstractTextProvider> providers) {
            this.providers = providers;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            final StringBuilder ret = new StringBuilder();
            for (final AbstractTextProvider provider : this.providers) {
                final String part = provider.getRaw(base, tokens);
                if (part == null) {
                    return null;
                }
                ret.append(part);
            }
            return ret.toString();
        }
        
        public String getConstant(final Class<?> baseClass) {
            final StringBuilder ret = new StringBuilder();
            for (final AbstractTextProvider provider : this.providers) {
                final String part = provider.getConstant(baseClass);
                if (part == null) {
                    return null;
                }
                ret.append(part);
            }
            return ret.toString();
        }
    }
    
    private static class Translate extends AbstractTextProvider
    {
        private final AbstractTextProvider key;
        
        public Translate(final AbstractTextProvider key) {
            this.key = key;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            final String key = this.key.getRaw(base, tokens);
            if (key == null) {
                return null;
            }
            return Localization.translate(key);
        }
        
        public String getConstant(final Class<?> baseClass) {
            return null;
        }
    }
    
    private static class TranslateFormat extends AbstractTextProvider
    {
        private final AbstractTextProvider format;
        private final List<AbstractTextProvider> args;
        
        public TranslateFormat(final AbstractTextProvider format, final List<AbstractTextProvider> args) {
            this.format = format;
            this.args = args;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            final String format = this.format.getRaw(base, tokens);
            if (format == null) {
                return null;
            }
            final Object[] cArgs = new Object[this.args.size()];
            for (int i = 0; i < this.args.size(); ++i) {
                final String arg = this.args.get(i).getRaw(base, tokens);
                if (arg == null) {
                    return null;
                }
                cArgs[i] = arg;
            }
            return Localization.translate(format, cArgs);
        }
        
        public String getConstant(final Class<?> baseClass) {
            return null;
        }
    }
    
    private static class TokenResolverDynamic extends AbstractTextProvider
    {
        private final AbstractTextProvider token;
        
        public TokenResolverDynamic(final AbstractTextProvider token) {
            this.token = token;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            final String token = this.token.getRaw(base, tokens);
            if (token == null) {
                return null;
            }
            return resolveToken(token, (base != null) ? base.getClass() : null, base, tokens);
        }
        
        public String getConstant(final Class<?> baseClass) {
            final String token = this.token.getConstant(baseClass);
            if (token == null) {
                return null;
            }
            return resolveToken(token, baseClass, null, TextProvider.emptyTokens());
        }
    }
    
    private static class TokenResolverStatic extends AbstractTextProvider
    {
        private final String token;
        
        public TokenResolverStatic(final String token) {
            this.token = token;
        }
        
        public String getRaw(final Object base, final Map<String, ITextProvider> tokens) {
            return resolveToken(this.token, (base != null) ? base.getClass() : null, base, tokens);
        }
        
        public String getConstant(final Class<?> baseClass) {
            return resolveToken(this.token, baseClass, null, TextProvider.emptyTokens());
        }
    }
    
    public interface ITextProvider
    {
        String get(final Object p0, final Map<String, ITextProvider> p1);
        
        String getOptional(final Object p0, final Map<String, ITextProvider> p1);
    }
}
