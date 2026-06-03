package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Player;
import ic2.core.audio.PositionSpec;
import ic2.core.init.Localization;
import ic2.core.init.OreValues;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.ref.ItemName;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemScanner extends BaseElectricItem implements IBoxable, IHandHeldInventory {
  public ItemScanner() {
    this(ItemName.scanner, 100000.0D, 128.0D, 1);
  }
  
  public ItemScanner(ItemName name, double maxCharge, double transferLimit, int tier) {
    super(name, maxCharge, transferLimit, tier);
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    super.func_77624_a(stack, world, tooltip, advanced);
    tooltip.add(Localization.translate("ic2.scanner.range", new Object[] { "" + getScanRange() }));
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if ((this.tier == 1 && !ElectricItem.manager.use(stack, 50.0D, (EntityLivingBase)player)) || (this.tier == 2 && 
      !ElectricItem.manager.use(stack, 250.0D, (EntityLivingBase)player)))
      return new ActionResult(EnumActionResult.FAIL, stack); 
    if (!world.field_72995_K) {
      if (IC2.platform.launchGui(player, getInventory(player, stack)) && player.field_71070_bA instanceof ContainerToolScanner) {
        ContainerToolScanner container = (ContainerToolScanner)player.field_71070_bA;
        Map<ItemComparableItemStack, Integer> scanResult = scan(player.func_130014_f_(), player
            .func_180425_c(), 
            getScanRange());
        container.setResults(scanMapToSortedList(scanResult));
      } 
    } else {
      IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/ODScanner.ogg", true, IC2.audioManager.getDefaultVolume());
    } 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    if (!(player.func_130014_f_()).field_72995_K && !StackUtil.isEmpty(stack) && player.field_71070_bA instanceof ContainerToolScanner) {
      HandHeldScanner scanner = (HandHeldScanner)((ContainerToolScanner)player.field_71070_bA).base;
      if (scanner.isThisContainer(stack)) {
        scanner.saveAsThrown(stack);
        player.func_71053_j();
      } 
    } 
    return true;
  }
  
  public int startLayerScan(ItemStack stack) {
    return ElectricItem.manager.use(stack, 50.0D, null) ? (getScanRange() / 2) : 0;
  }
  
  public int getScanRange() {
    return 6;
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
    return new HandHeldScanner(player, stack);
  }
  
  private Map<ItemComparableItemStack, Integer> scan(World world, BlockPos center, int range) {
    Map<ItemComparableItemStack, Integer> ret = new HashMap<>();
    ChunkCache cache = new ChunkCache(world, center.func_177982_a(-range, -range, -range), center.func_177982_a(range, range, range), 0);
    EntityPlayer player = Ic2Player.get(world);
    BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
    for (int y = center.func_177956_o() - range; y <= center.func_177956_o() + range; y++) {
      for (int z = center.func_177952_p() - range; z <= center.func_177952_p() + range; z++) {
        for (int x = center.func_177958_n() - range; x <= center.func_177958_n() + range; x++) {
          List<ItemStack> drops;
          tmpPos.func_181079_c(x, y, z);
          IBlockState state = cache.func_180495_p((BlockPos)tmpPos);
          if (state.func_177230_c().isAir(state, (IBlockAccess)cache, (BlockPos)tmpPos))
            continue; 
          ItemStack pickStack = StackUtil.getPickStack(world, (BlockPos)tmpPos, state, player);
          if (pickStack != null && OreValues.get(pickStack) > 0) {
            drops = Arrays.asList(new ItemStack[] { pickStack });
          } else {
            drops = StackUtil.getDrops((IBlockAccess)cache, (BlockPos)tmpPos, state, 0);
            if (drops.isEmpty() || OreValues.get(drops) <= 0)
              continue; 
          } 
          for (ItemStack drop : drops) {
            ItemComparableItemStack key = new ItemComparableItemStack(drop, true);
            Integer count = ret.get(key);
            if (count == null)
              count = Integer.valueOf(0); 
            count = Integer.valueOf(count.intValue() + StackUtil.getSize(drop));
            ret.put(key, count);
          } 
          continue;
        } 
      } 
    } 
    return ret;
  }
  
  private List<Tuple.T2<ItemStack, Integer>> scanMapToSortedList(Map<ItemComparableItemStack, Integer> map) {
    List<Tuple.T2<ItemStack, Integer>> ret = new ArrayList<>(map.size());
    for (Map.Entry<ItemComparableItemStack, Integer> entry : map.entrySet())
      ret.add(new Tuple.T2(((ItemComparableItemStack)entry.getKey()).toStack(), entry.getValue())); 
    Collections.sort(ret, new Comparator<Tuple.T2<ItemStack, Integer>>() {
          public int compare(Tuple.T2<ItemStack, Integer> a, Tuple.T2<ItemStack, Integer> b) {
            return ((Integer)b.b).intValue() - ((Integer)a.b).intValue();
          }
        });
    return ret;
  }
}
