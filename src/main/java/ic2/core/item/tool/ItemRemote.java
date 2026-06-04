package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.BlockDynamite;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRemote extends ItemIC2 {
  public ItemRemote() {
    super(ItemName.remote);
    func_77625_d(1);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (world.isRemote)
      return EnumActionResult.SUCCESS; 
    IBlockState state = world.func_180495_p(pos);
    Block block = state.func_177230_c();
    if (block != BlockName.dynamite.getInstance())
      return EnumActionResult.SUCCESS; 
    ItemStack stack = StackUtil.get(player, hand);
    if (!((Boolean)state.func_177229_b(BlockDynamite.linked)).booleanValue()) {
      addRemote(pos, stack);
      world.func_175656_a(pos, state.func_177226_a(BlockDynamite.linked, Boolean.valueOf(true)));
    } else {
      int index = hasRemote(pos, stack);
      if (index > -1) {
        world.func_175656_a(pos, state.func_177226_a(BlockDynamite.linked, Boolean.valueOf(false)));
        removeRemote(index, stack);
      } else {
        IC2.platform.messagePlayer(player, "This dynamite stick is not linked to this remote, cannot unlink.", new Object[0]);
      } 
    } 
    return EnumActionResult.SUCCESS;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (world.isRemote)
      return new ActionResult(EnumActionResult.SUCCESS, stack); 
    IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/dynamiteomote.ogg", true, IC2.audioManager.getDefaultVolume());
    launchRemotes(world, stack, player);
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public static void addRemote(BlockPos pos, ItemStack freq) {
    NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
    if (!compound.func_74764_b("coords"))
      compound.setTag("coords", (NBTBase)new NBTTagList()); 
    NBTTagList coords = compound.func_150295_c("coords", 10);
    NBTTagCompound coord = new NBTTagCompound();
    coord.func_74768_a("x", pos.func_177958_n());
    coord.func_74768_a("y", pos.func_177956_o());
    coord.func_74768_a("z", pos.func_177952_p());
    coords.func_74742_a((NBTBase)coord);
    compound.setTag("coords", (NBTBase)coords);
    freq.func_77964_b(coords.func_74745_c());
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    if (stack.func_77952_i() > 0)
      tooltip.add("Linked to " + stack.func_77952_i() + " dynamite"); 
  }
  
  public static void launchRemotes(World world, ItemStack freq, EntityPlayer player) {
    NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
    if (!compound.func_74764_b("coords"))
      return; 
    NBTTagList coords = compound.func_150295_c("coords", 10);
    for (int i = 0; i < coords.func_74745_c(); ) {
      NBTTagCompound coord = coords.func_150305_b(i);
      BlockPos pos = new BlockPos(coord.func_74762_e("x"), coord.func_74762_e("y"), coord.func_74762_e("z"));
      if (world.func_175667_e(pos)) {
        IBlockState state = world.func_180495_p(pos);
        if (state.func_177230_c() == BlockName.dynamite.getInstance() && ((Boolean)state
          .func_177229_b(BlockDynamite.linked)).booleanValue()) {
          state.func_177230_c().removedByPlayer(state, world, pos, player, false);
          world.func_175698_g(pos);
        } 
        coords.func_74744_a(i);
        continue;
      } 
      i++;
    } 
    freq.func_77964_b(0);
  }
  
  public static int hasRemote(BlockPos pos, ItemStack freq) {
    NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
    if (!compound.func_74764_b("coords"))
      return -1; 
    NBTTagList coords = compound.func_150295_c("coords", 10);
    for (int i = 0; i < coords.func_74745_c(); i++) {
      NBTTagCompound coord = coords.func_150305_b(i);
      if (coord.func_74762_e("x") == pos.func_177958_n() && coord
        .func_74762_e("y") == pos.func_177956_o() && coord
        .func_74762_e("z") == pos.func_177952_p())
        return i; 
    } 
    return -1;
  }
  
  public static void removeRemote(int index, ItemStack freq) {
    NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
    if (!compound.func_74764_b("coords"))
      return; 
    NBTTagList coords = compound.func_150295_c("coords", 10);
    NBTTagList newCoords = new NBTTagList();
    for (int i = 0; i < coords.func_74745_c(); i++) {
      if (i != index)
        newCoords.func_74742_a((NBTBase)coords.func_150305_b(i)); 
    } 
    compound.setTag("coords", (NBTBase)newCoords);
    freq.func_77964_b(newCoords.func_74745_c());
  }
}
