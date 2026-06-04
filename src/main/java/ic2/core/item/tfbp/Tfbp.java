package ic2.core.item.tfbp;

import ic2.api.item.ITerraformingBP;
import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.ItemMulti;
import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class Tfbp extends ItemMulti<Tfbp.TfbpType> implements ITerraformingBP {
  public static void init() {
    for (TfbpType tfbp : TfbpType.values()) {
      if (tfbp.logic != null)
        tfbp.logic.init(); 
    } 
  }
  
  public Tfbp() {
    super(ItemName.tfbp, TfbpType.class);
    func_77625_d(1);
  }
  
  public double getConsume(ItemStack stack) {
    TfbpType type = (TfbpType)getType(stack);
    return (type == null) ? 0.0D : type.consume;
  }
  
  public int getRange(ItemStack stack) {
    TfbpType type = (TfbpType)getType(stack);
    return (type == null) ? 0 : type.range;
  }
  
  public boolean canInsert(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
    TfbpType type = (TfbpType)getType(stack);
    if (type == null)
      return false; 
    if (type == TfbpType.cultivation && world.provider
      .func_186058_p() == DimensionType.THE_END)
      IC2.achievements.issueAchievement(player, "terraformEndCultivation"); 
    return true;
  }
  
  public boolean terraform(ItemStack stack, World world, BlockPos pos) {
    TfbpType type = (TfbpType)getType(stack);
    if (type == null)
      return false; 
    if (type.logic == null)
      return false; 
    return type.logic.terraform(world, pos);
  }
  
  public enum TfbpType implements IIdProvider {
    blank(0.0D, 0, null),
    chilling(2000.0D, 50, new Chilling()),
    cultivation(4000.0D, 40, new Cultivation()),
    desertification(2500.0D, 40, new Desertification()),
    flatification(4000.0D, 40, new Flatification()),
    irrigation(3000.0D, 60, new Irrigation()),
    mushroom(8000.0D, 25, new Mushroom());
    
    public final double consume;
    
    public final int range;
    
    final TerraformerBase logic;
    
    TfbpType(double consume, int range, TerraformerBase logic) {
      this.consume = consume;
      this.range = range;
      this.logic = logic;
    }
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
}
