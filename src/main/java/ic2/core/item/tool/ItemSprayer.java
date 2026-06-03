package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockIC2Fence;
import ic2.core.block.BlockScaffold;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.ItemIC2FluidContainer;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemSprayer extends ItemIC2FluidContainer implements IBoxable {
  public ItemSprayer() {
    super(ItemName.foam_sprayer, 8000);
    func_77625_d(1);
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    subItems.add(new ItemStack((Item)this));
    subItems.add(getItemStack(FluidName.construction_foam));
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    if (IC2.platform.isSimulating() && 
      IC2.keyboard.isModeSwitchKeyDown(player)) {
      ItemStack stack = StackUtil.get(player, hand);
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      int mode = nbtData.func_74762_e("mode");
      mode = (mode == 0) ? 1 : 0;
      nbtData.func_74768_a("mode", mode);
      String sMode = (mode == 0) ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single";
      IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { sMode });
    } 
    return super.func_77659_a(world, player, hand);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
    Target target;
    if (IC2.keyboard.isModeSwitchKeyDown(player))
      return EnumActionResult.PASS; 
    if (!IC2.platform.isSimulating())
      return EnumActionResult.SUCCESS; 
    RayTraceResult rtResult = func_77621_a(world, player, true);
    if (rtResult == null)
      return EnumActionResult.PASS; 
    if (rtResult.field_72313_a == RayTraceResult.Type.BLOCK && !pos.equals(rtResult.func_178782_a())) {
      BlockPos fluidPos = rtResult.func_178782_a();
      if (LiquidUtil.drainBlockToContainer(world, fluidPos, player, hand))
        return EnumActionResult.SUCCESS; 
    } 
    int maxFoamBlocks = 0;
    ItemStack stack = StackUtil.get(player, hand);
    FluidStack fluid = FluidUtil.getFluidContained(stack);
    if (fluid != null && fluid.amount > 0)
      maxFoamBlocks += fluid.amount / getFluidPerFoam(); 
    ItemStack pack = (ItemStack)player.field_71071_by.field_70460_b.get(2);
    if (pack != null && pack.func_77973_b() == ItemName.cf_pack.getInstance()) {
      fluid = FluidUtil.getFluidContained(pack);
      if (fluid != null && fluid.amount > 0) {
        maxFoamBlocks += fluid.amount / getFluidPerFoam();
      } else {
        pack = null;
      } 
    } else {
      pack = null;
    } 
    if (maxFoamBlocks == 0)
      return EnumActionResult.FAIL; 
    maxFoamBlocks = Math.min(maxFoamBlocks, getMaxFoamBlocks(stack));
    if (canPlaceFoam(world, pos, Target.Scaffold)) {
      target = Target.Scaffold;
    } else if (canPlaceFoam(world, pos, Target.Cable)) {
      target = Target.Cable;
    } else {
      pos = pos.func_177972_a(side);
      target = Target.Any;
    } 
    Vec3d viewVec = player.func_70040_Z();
    EnumFacing playerViewFacing = EnumFacing.func_176737_a((float)viewVec.field_72450_a, (float)viewVec.field_72448_b, (float)viewVec.field_72449_c);
    int amount = sprayFoam(world, pos, playerViewFacing.func_176734_d(), target, maxFoamBlocks);
    amount *= getFluidPerFoam();
    if (amount > 0) {
      if (pack != null) {
        IFluidHandlerItem packHandler = FluidUtil.getFluidHandler(pack);
        assert packHandler != null;
        fluid = packHandler.drain(amount, true);
        amount -= fluid.amount;
        player.field_71071_by.field_70460_b.set(2, packHandler.getContainer());
      } 
      if (amount > 0) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        assert handler != null;
        handler.drain(amount, true);
        StackUtil.set(player, hand, handler.getContainer());
      } 
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  public int sprayFoam(World world, BlockPos pos, EnumFacing excludedDir, Target target, int maxFoamBlocks) {
    if (!canPlaceFoam(world, pos, target))
      return 0; 
    Queue<BlockPos> toCheck = new ArrayDeque<>();
    Set<BlockPos> positions = new HashSet<>();
    toCheck.add(pos);
    BlockPos cPos;
    while ((cPos = toCheck.poll()) != null && positions.size() < maxFoamBlocks) {
      if (!canPlaceFoam(world, cPos, target))
        continue; 
      if (positions.add(cPos))
        for (EnumFacing dir : EnumFacing.field_82609_l) {
          if (dir != excludedDir)
            toCheck.add(cPos.func_177972_a(dir)); 
        }  
    } 
    toCheck.clear();
    int failedPlacements = 0;
    for (BlockPos targetPos : positions) {
      IBlockState state = world.func_180495_p(targetPos);
      Block targetBlock = state.func_177230_c();
      if (targetBlock == BlockName.scaffold.getInstance()) {
        BlockScaffold scaffold = (BlockScaffold)targetBlock;
        switch ((BlockScaffold.ScaffoldType)state.func_177229_b((IProperty)scaffold.getTypeProperty())) {
          case Any:
          case Scaffold:
            scaffold.func_176226_b(world, targetPos, state, 0);
            world.func_175656_a(targetPos, BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal));
            continue;
          case Cable:
            StackUtil.dropAsEntity(world, targetPos, BlockName.fence.getItemStack((Enum)BlockIC2Fence.IC2FenceType.iron));
          case null:
            world.func_175656_a(targetPos, BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.reinforced));
            continue;
        } 
        continue;
      } 
      if (targetBlock == BlockName.te.getInstance()) {
        TileEntity te = world.func_175625_s(targetPos);
        if (te instanceof TileEntityCable && 
          !((TileEntityCable)te).foam())
          failedPlacements++; 
        continue;
      } 
      if (!world.func_175656_a(targetPos, BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal)))
        failedPlacements++; 
    } 
    return positions.size() - failedPlacements;
  }
  
  protected int getMaxFoamBlocks(ItemStack stack) {
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    if (nbtData.func_74762_e("mode") == 0)
      return 10; 
    return 1;
  }
  
  protected int getFluidPerFoam() {
    return 100;
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  public boolean canfill(Fluid fluid) {
    return (fluid == FluidName.construction_foam.getInstance());
  }
  
  private static boolean canPlaceFoam(World world, BlockPos pos, Target target) {
    TileEntity te;
    switch (target) {
      case Any:
        return BlockName.foam.getInstance().func_176198_a(world, pos, EnumFacing.DOWN);
      case Scaffold:
        return (world.func_180495_p(pos).func_177230_c() == BlockName.scaffold.getInstance());
      case Cable:
        if (world.func_180495_p(pos).func_177230_c() != BlockName.te.getInstance())
          return false; 
        te = world.func_175625_s(pos);
        if (te instanceof TileEntityCable)
          return !((TileEntityCable)te).isFoamed(); 
        return false;
    } 
    assert false;
    return false;
  }
  
  private enum Target {
    Any, Scaffold, Cable;
  }
}
