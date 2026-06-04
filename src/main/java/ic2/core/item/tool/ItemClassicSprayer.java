package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockScaffold;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.ItemGradualInt;
import ic2.core.item.armor.ItemArmorClassicCFPack;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemClassicSprayer extends ItemGradualInt {
  public ItemClassicSprayer() {
    super(ItemName.foam_sprayer, 1602);
    setMaxStackSize(1);
  }
  
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (!IC2.platform.isSimulating())
      return EnumActionResult.SUCCESS; 
    ItemStack stack = StackUtil.get(player, hand);
    ItemStack pack = (ItemStack)player.inventory.armorInventory.get(2);
    boolean pulledFromCFPack = (StackUtil.check(pack) && pack.getItem() == ItemName.cf_pack.getInstance() && ((ItemArmorClassicCFPack)pack.getItem()).getCFPellet(player, pack));
    if (!pulledFromCFPack && getCustomDamage(stack) < 100)
      return EnumActionResult.FAIL; 
    if (world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance()) {
      sprayFoam(world, pos, calculateDirectionsFromPlayer(player), true);
      if (!pulledFromCFPack)
        applyCustomDamage(stack, 100, (EntityLivingBase)player); 
      return EnumActionResult.SUCCESS;
    } 
    if (sprayFoam(world, pos.offset(facing), calculateDirectionsFromPlayer(player), false)) {
      if (!pulledFromCFPack)
        applyCustomDamage(stack, 100, (EntityLivingBase)player); 
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  private static boolean[] calculateDirectionsFromPlayer(EntityPlayer player) {
    float yaw = player.rotationYaw % 360.0F;
    float pitch = player.rotationPitch;
    boolean[] r = { true, true, true, true, true, true };
    if (pitch >= -65.0F && pitch <= 65.0F) {
      if ((yaw >= 300.0F && yaw <= 360.0F) || (yaw >= 0.0F && yaw <= 60.0F))
        r[2] = false; 
      if (yaw >= 30.0F && yaw <= 150.0F)
        r[5] = false; 
      if (yaw >= 120.0F && yaw <= 240.0F)
        r[3] = false; 
      if (yaw >= 210.0F && yaw <= 330.0F)
        r[4] = false; 
    } 
    if (pitch <= -40.0F)
      r[0] = false; 
    if (pitch >= 40.0F)
      r[1] = false; 
    return r;
  }
  
  public boolean sprayFoam(World world, BlockPos start, boolean[] directions, boolean scaffold) {
    if (!canFoam(world, start, scaffold))
      return false; 
    Queue<BlockPos> check = new ArrayDeque<>();
    Set<BlockPos> place = new LinkedHashSet<>();
    int foamcount = getSprayMass();
    check.add(start);
    BlockPos set;
    while ((set = check.poll()) != null && foamcount > 0) {
      if (canFoam(world, set, scaffold) && place.add(set)) {
        for (int i : generateRngSpread(IC2.random)) {
          if (scaffold || directions[i])
            check.add(set.offset(EnumFacing.getFront(i))); 
        } 
        foamcount--;
      } 
    } 
    for (BlockPos pos : place) {
      IBlockState state = world.getBlockState(pos);
      Block targetBlock = state.getBlock();
      if (targetBlock == BlockName.scaffold.getInstance()) {
        BlockScaffold block = (BlockScaffold)targetBlock;
        switch ((BlockScaffold.ScaffoldType)state.getValue((IProperty)block.getTypeProperty())) {
          case wood:
          case reinforced_wood:
            block.dropBlockAsItem(world, pos, state, 0);
            world.setBlockState(pos, BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal));
            continue;
        } 
        continue;
      } 
      if (targetBlock == BlockName.te.getInstance()) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCable)
          ((TileEntityCable)te).foam(); 
        continue;
      } 
      world.setBlockState(pos, BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal));
    } 
    return true;
  }
  
  private static boolean canFoam(World world, BlockPos pos, boolean scaffold) {
    if (!scaffold) {
      if (BlockName.foam.getInstance().canPlaceBlockOnSide(world, pos, EnumFacing.DOWN))
        return true; 
      if (world.getBlockState(pos).getBlock() != BlockName.te.getInstance())
        return false; 
      TileEntity te = world.getTileEntity(pos);
      return (te instanceof TileEntityCable && !((TileEntityCable)te).isFoamed());
    } 
    return (world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance());
  }
  
  private static int[] generateRngSpread(Random random) {
    int[] re = { 0, 1, 2, 3, 4, 5 };
    for (int i = 0; i < 16; i++) {
      int first = random.nextInt(6);
      int second = random.nextInt(6);
      int temp = re[first];
      re[first] = re[second];
      re[second] = temp;
    } 
    return re;
  }
  
  public static int getSprayMass() {
    return 13;
  }
  
  public double getDurabilityForDisplay(ItemStack stack) {
    return 1.0D - super.getDurabilityForDisplay(stack);
  }
}
