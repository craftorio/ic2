package ic2.core.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Profile {
   public final String name;
   public final Set<TextureStyle> textures;
   public final Version style;
   public final Map<String, List<RecipeChange>> recipeConfigs;
   public final Map<String, List<Object>> recipeRemovals;

   public Profile(String name, Set<TextureStyle> textures, Version style, RecipeChange... changes) {
      this.name = name;
      this.textures = textures;
      this.style = style;
      if (changes.length == 0) {
         this.recipeConfigs = Collections.emptyMap();
         this.recipeRemovals = Collections.emptyMap();
      } else {
         Map<String, List<RecipeChange>> recipeConfigs = new HashMap<>();
         Map<String, List<Object>> recipeRemovals = new HashMap<>();

         for (RecipeChange change : changes) {
            if (change.type != RecipeChange.ChangeType.REMOVAL) {
               recipeConfigs.computeIfAbsent(change.name, k -> new ArrayList<>()).add(change);
            } else {
               recipeRemovals.computeIfAbsent(change.name, k -> new ArrayList<>()).add(change);
            }
         }

         this.recipeConfigs = !recipeConfigs.isEmpty() ? recipeConfigs : Collections.emptyMap();
         this.recipeRemovals = !recipeRemovals.isEmpty() ? recipeRemovals : Collections.emptyMap();
      }
   }

   public List<RecipeChange> processRecipeConfigs(String name) {
      List<RecipeChange> configs = this.recipeConfigs.get(name);
      if (configs == null) {
         return Collections.emptyList();
      }

      List<RecipeChange> ret = new ArrayList<>();

      for (RecipeChange change : configs) {
         switch (change.type) {
            case EXTENSION:
               ret.addAll(ProfileManager.getOrError(((RecipeChange.RecipeExtension)change).profile).processRecipeConfigs(name));
               break;
            case ADDITION:
            case REPLACEMENT:
               ret.add(change);
               break;
            case REMOVAL:
            default:
               throw new IllegalStateException("Unexpected recipe change " + change + " for " + name);
         }
      }

      return ret;
   }

   @Override
   public String toString() {
      return "Profile: " + this.name;
   }
}
