package ic2.api.event;

import java.util.Set;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class ProfileEvent extends Event {
   public static class Load extends ProfileEvent {
      public final Set<String> loaded;
      public final String active;

      public Load(Set<String> loaded, String active) {
         this.loaded = loaded;
         this.active = active;
      }
   }

   public static class Switch extends ProfileEvent {
      public final String from;
      public final String to;

      public Switch(String from, String to) {
         this.from = from;
         this.to = to;
      }
   }
}
