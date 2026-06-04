// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Profile
{
    public final String name;
    public final Set<TextureStyle> textures;
    public final Version style;
    public final Map<String, List<RecipeChange>> recipeConfigs;
    public final Map<String, List<Object>> recipeRemovals;
    
    public Profile(final String name, final Set<TextureStyle> textures, final Version style, final RecipeChange... changes) {
        this.name = name;
        this.textures = textures;
        this.style = style;
        if (changes.length == 0) {
            this.recipeConfigs = Collections.emptyMap();
            this.recipeRemovals = Collections.emptyMap();
        }
        else {
            final Map<String, List<RecipeChange>> recipeConfigs = new HashMap<String, List<RecipeChange>>();
            final Map<String, List<Object>> recipeRemovals = new HashMap<String, List<Object>>();
            for (final RecipeChange change : changes) {
                if (change.type != RecipeChange.ChangeType.REMOVAL) {
                    recipeConfigs.computeIfAbsent(change.name, k -> new ArrayList()).add(change);
                }
                else {
                    recipeRemovals.computeIfAbsent(change.name, k -> new ArrayList()).add(change);
                }
            }
            this.recipeConfigs = (recipeConfigs.isEmpty() ? Collections.emptyMap() : recipeConfigs);
            this.recipeRemovals = (recipeRemovals.isEmpty() ? Collections.emptyMap() : recipeRemovals);
        }
    }
    
    public List<RecipeChange> processRecipeConfigs(final String name) {
        final List<RecipeChange> configs = this.recipeConfigs.get(name);
        if (configs == null) {
            return Collections.emptyList();
        }
        final List<RecipeChange> ret = new ArrayList<RecipeChange>();
        for (final RecipeChange change : configs) {
            switch (change.type) {
                case EXTENSION: {
                    ret.addAll(ProfileManager.getOrError(((RecipeChange.RecipeExtension)change).profile).processRecipeConfigs(name));
                    continue;
                }
                case ADDITION:
                case REPLACEMENT: {
                    ret.add(change);
                    continue;
                }
                default: {
                    throw new IllegalStateException("Unexpected recipe change " + change + " for " + name);
                }
            }
        }
        return ret;
    }
    
    @Override
    public String toString() {
        return "Profile: " + this.name;
    }
}
