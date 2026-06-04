// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.util.Enumeration;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Arrays;
import java.util.Collection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class RecipeChange
{
    public final String name;
    public final ChangeType type;
    
    public RecipeChange(final String name, final ChangeType type) {
        this.name = name;
        this.type = type;
    }
    
    public abstract InputStream getStream();
    
    static InputStream asStream(final ProfileTarget target) {
        try {
            return target.asStream();
        }
        catch (final IOException e) {
            throw new RuntimeException("Error getting replacement stream for " + target, e);
        }
    }
    
    public enum ChangeType
    {
        EXTENSION, 
        ADDITION, 
        REMOVAL, 
        REPLACEMENT;
    }
    
    public static class RecipeExtension extends RecipeChange
    {
        public final String profile;
        
        public RecipeExtension(final String name, final String profile) {
            super(name, ChangeType.EXTENSION);
            this.profile = profile;
        }
        
        @Override
        public InputStream getStream() {
            throw new UnsupportedOperationException();
        }
    }
    
    public static class RecipeReplacement extends RecipeChange
    {
        protected final ProfileTarget[] targets;
        
        public RecipeReplacement(final String name, final ProfileTarget... targets) {
            super(name, ChangeType.REPLACEMENT);
            this.targets = targets;
        }
        
        @Override
        public InputStream getStream() {
            switch (this.targets.length) {
                case 0: {
                    return new ByteArrayInputStream(new byte[0]);
                }
                case 1: {
                    return RecipeChange.asStream(this.targets[0]);
                }
                default: {
                    return new SequenceInputStream((Enumeration<? extends InputStream>)Collections.enumeration((Collection<Object>)Arrays.stream(this.targets).map((Function<? super ProfileTarget, ?>)RecipeChange::asStream).collect((Collector<? super Object, ?, Collection<T>>)Collectors.toList())));
                }
            }
        }
    }
    
    public static class RecipeAddition extends RecipeChange
    {
        protected final ProfileTarget[] targets;
        
        public RecipeAddition(final String name, final ProfileTarget... targets) {
            super(name, ChangeType.ADDITION);
            this.targets = targets;
        }
        
        @Override
        public InputStream getStream() {
            switch (this.targets.length) {
                case 0: {
                    return null;
                }
                case 1: {
                    return RecipeChange.asStream(this.targets[0]);
                }
                default: {
                    return new SequenceInputStream((Enumeration<? extends InputStream>)Collections.enumeration((Collection<Object>)Arrays.stream(this.targets).map((Function<? super ProfileTarget, ?>)RecipeChange::asStream).collect((Collector<? super Object, ?, Collection<T>>)Collectors.toList())));
                }
            }
        }
    }
}
