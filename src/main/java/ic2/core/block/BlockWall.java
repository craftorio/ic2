package ic2.core.block;

import ic2.api.event.RetextureEvent;
import ic2.core.Ic2Player;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2Color;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockWall extends BlockMultiID<Ic2Color> {
  public static BlockWall create() {
    return BlockMultiID.<Ic2Color, BlockWall>create(BlockWall.class, Ic2Color.class, new Object[0]);
  }
  
  private BlockWall() {
    super(BlockName.wall, Material.field_151576_e);
    func_149711_c(3.0F);
    func_149752_b(30.0F);
    func_149672_a(SoundType.field_185851_d);
    func_180632_j(this.field_176227_L.func_177621_b()
        .func_177226_a((IProperty)this.typeProperty, (Comparable)defaultColor));
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
    IBlockState state = world.getBlockState(pos);
    Ic2Color type = getType(state);
    if (type == null)
      return false; 
    Ic2Color newColor = Ic2Color.get(color);
    if (type != newColor) {
      world.func_175656_a(pos, state.func_177226_a((IProperty)this.typeProperty, (Comparable)newColor));
      return true;
    } 
    return false;
  }
  
  @SubscribeEvent
  public void onRetexture(RetextureEvent event) {
    if (event.state.getBlock() != this)
      return; 
    World world = event.getWorld();
    Ic2Color color = (Ic2Color)event.state.func_177229_b((IProperty)this.typeProperty);
    if (!ItemBlockTileEntity.placeTeBlock(null, (EntityLivingBase)Ic2Player.get(world), world, event.pos, EnumFacing.DOWN, new TileEntityWall(color)))
      return; 
    IBlockState newState = BlockName.te.getInstance().getDefaultState();
    RetextureEvent event2 = new RetextureEvent(world, event.pos, newState, event.side, event.player, event.refState, event.refVariant, event.refSide, event.refColorMultipliers);
    MinecraftForge.EVENT_BUS.post((Event)event2);
    if (event2.applied) {
      event.applied = true;
      event.setCanceled(true);
    } else {
      world.func_175656_a(event.pos, event.state);
    } 
  }
  
  public static final Ic2Color defaultColor = Ic2Color.light_gray;
}
