package ic2.core.item.tool;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;

@NotClassic
public class ItemToolHammer extends ItemToolCrafting {
  public ItemToolHammer() {
    super(ItemName.forge_hammer, 80);
  }
}
