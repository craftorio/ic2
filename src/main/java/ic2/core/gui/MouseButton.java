package ic2.core.gui;

public enum MouseButton {
   left(0),
   right(1);

   public final int id;
   private static final MouseButton[] map = createMap();

   MouseButton(int id) {
      this.id = id;
   }

   public static MouseButton get(int id) {
      return id >= 0 && id < map.length ? map[id] : null;
   }

   private static MouseButton[] createMap() {
      MouseButton[] values = values();
      int max = -1;

      for (MouseButton button : values) {
         if (button.id > max) {
            max = button.id;
         }
      }

      if (max < 0) {
         return new MouseButton[0];
      }

      MouseButton[] ret = new MouseButton[max + 1];

      for (MouseButton button : values) {
         ret[button.id] = button;
      }

      return ret;
   }
}
