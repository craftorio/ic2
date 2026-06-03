package ic2.core.gui;

public enum MouseButton {
  left(0),
  right(1);
  
  public final int id;
  
  private static final MouseButton[] map;
  
  MouseButton(int id) {
    this.id = id;
  }
  
  public static MouseButton get(int id) {
    if (id < 0 || id >= map.length)
      return null; 
    return map[id];
  }
  
  private static MouseButton[] createMap() {
    MouseButton[] values = values();
    int max = -1;
    for (MouseButton button : values) {
      if (button.id > max)
        max = button.id; 
    } 
    if (max < 0)
      return new MouseButton[0]; 
    MouseButton[] ret = new MouseButton[max + 1];
    for (MouseButton button : values)
      ret[button.id] = button; 
    return ret;
  }
  
  static {
    map = createMap();
  }
}
