package ic2.core.block.invslot;

import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.network.IRpcProvider;
import ic2.core.network.Rpc;
import ic2.core.network.RpcHandler;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuGraph;
import ic2.core.uu.UuIndex;
import java.util.concurrent.TimeUnit;
import net.minecraft.item.ItemStack;

public class InvSlotScannable extends InvSlotConsumable {
  public InvSlotScannable(IInventorySlotHolder<?> base1, String name1, int count) {
    super(base1, name1, count);
    setStackSizeLimit(1);
  }
  
  public boolean accepts(ItemStack stack) {
    if (IC2.platform.isSimulating())
      return isValidStack(stack); 
    Rpc<Boolean> rpc = RpcHandler.run(ServerScannableCheck.class, new Object[] { stack });
    try {
      return ((Boolean)rpc.get(1L, TimeUnit.SECONDS)).booleanValue();
    } catch (Exception e) {
      IC2.log.debug(LogCategory.Block, e, "Scannability check failed.");
      return false;
    } 
  }
  
  private static boolean isValidStack(ItemStack stack) {
    stack = UuGraph.find(stack);
    return (!StackUtil.isEmpty(stack) && UuIndex.instance.get(stack) < Double.POSITIVE_INFINITY);
  }
  
  public static class ServerScannableCheck implements IRpcProvider<Boolean> {
    public Boolean executeRpc(Object... args) {
      ItemStack stack = (ItemStack)args[0];
      return Boolean.valueOf(InvSlotScannable.isValidStack(stack));
    }
  }
  
  static {
    RpcHandler.registerProvider(new ServerScannableCheck());
  }
}
