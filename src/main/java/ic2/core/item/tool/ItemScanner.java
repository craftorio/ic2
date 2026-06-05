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
import java.util.Map.Entry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemScanner extends BaseElectricItem implements IBoxable, IHandHeldInventory {
   public ItemScanner() {
      this(ItemName.scanner, 100000.0, 128.0, 1);
   }

   public ItemScanner(ItemName name, double maxCharge, double transferLimit, int tier) {
      super(name, maxCharge, transferLimit, tier);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
      super.addInformation(stack, world, tooltip, advanced);
      tooltip.add(Localization.translate("ic2.scanner.range", "" + this.getScanRange()));
   }

   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if ((this.tier != 1 || ElectricItem.manager.use(stack, 50.0, player)) && (this.tier != 2 || ElectricItem.manager.use(stack, 250.0, player))) {
         if (!world.isRemote) {
            if (IC2.platform.launchGui(player, this.getInventory(player, stack)) && player.openContainer instanceof ContainerToolScanner) {
               ContainerToolScanner container = (ContainerToolScanner)player.openContainer;
               Map<ItemComparableItemStack, Integer> scanResult = this.scan(player.getEntityWorld(), player.getPosition(), this.getScanRange());
               container.setResults(this.scanMapToSortedList(scanResult));
            }
         } else {
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/ODScanner.ogg", true, IC2.audioManager.getDefaultVolume());
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      } else {
         return new ActionResult(EnumActionResult.FAIL, stack);
      }
   }

   public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
      if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerToolScanner) {
         HandHeldScanner scanner = ((ContainerToolScanner)player.openContainer).base;
         if (scanner.isThisContainer(stack)) {
            scanner.saveAsThrown(stack);
            player.closeScreen();
         }
      }

      return true;
   }

   public int startLayerScan(ItemStack stack) {
      return ElectricItem.manager.use(stack, 50.0, null) ? this.getScanRange() / 2 : 0;
   }

   public int getScanRange() {
      return 6;
   }

   @Override
   public boolean canBeStoredInToolbox(ItemStack itemstack) {
      return true;
   }

   @Override
   public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
      return new HandHeldScanner(player, stack);
   }

   private Map<ItemComparableItemStack, Integer> scan(World world, BlockPos center, int range) {
      Map<ItemComparableItemStack, Integer> ret = new HashMap<>();
      ChunkCache cache = new ChunkCache(world, center.add(-range, -range, -range), center.add(range, range, range), 0);
      EntityPlayer player = Ic2Player.get(world);
      MutableBlockPos tmpPos = new MutableBlockPos();

      for (int y = center.getY() - range; y <= center.getY() + range; y++) {
         for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
            for (int x = center.getX() - range; x <= center.getX() + range; x++) {
               tmpPos.setPos(x, y, z);
               IBlockState state = cache.getBlockState(tmpPos);
               if (!state.getBlock().isAir(state, cache, tmpPos)) {
                  ItemStack pickStack = StackUtil.getPickStack(world, tmpPos, state, player);
                  List<ItemStack> drops;
                  if (pickStack != null && OreValues.get(pickStack) > 0) {
                     drops = Arrays.asList(pickStack);
                  } else {
                     drops = StackUtil.getDrops(cache, tmpPos, state, 0);
                     if (drops.isEmpty() || OreValues.get(drops) <= 0) {
                        continue;
                     }
                  }

                  for (ItemStack drop : drops) {
                     ItemComparableItemStack key = new ItemComparableItemStack(drop, true);
                     Integer count = ret.get(key);
                     if (count == null) {
                        count = 0;
                     }

                     count = count + StackUtil.getSize(drop);
                     ret.put(key, count);
                  }
               }
            }
         }
      }

      return ret;
   }

   private List<Tuple.T2<ItemStack, Integer>> scanMapToSortedList(Map<ItemComparableItemStack, Integer> map) {
      List<Tuple.T2<ItemStack, Integer>> ret = new ArrayList<>(map.size());

      for (Entry<ItemComparableItemStack, Integer> entry : map.entrySet()) {
         ret.add(new Tuple.T2<>(entry.getKey().toStack(), entry.getValue()));
      }

      Collections.sort(ret, new Comparator<Tuple.T2<ItemStack, Integer>>() {
         public int compare(Tuple.T2<ItemStack, Integer> a, Tuple.T2<ItemStack, Integer> b) {
            return b.b - a.b;
         }
      });
      return ret;
   }
}
