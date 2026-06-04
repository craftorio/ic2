// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.core.uu.UuIndex;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuGraph;
import ic2.core.network.Rpc;
import ic2.core.util.LogCategory;
import java.util.concurrent.TimeUnit;
import ic2.core.network.IRpcProvider;
import ic2.core.network.RpcHandler;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotScannable extends InvSlotConsumable
{
    public InvSlotScannable(final IInventorySlotHolder<?> base1, final String name1, final int count) {
        super(base1, name1, count);
        this.setStackSizeLimit(1);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        if (IC2.platform.isSimulating()) {
            return isValidStack(stack);
        }
        final Rpc<Boolean> rpc = RpcHandler.run((Class<? extends IRpcProvider<Boolean>>)ServerScannableCheck.class, new Object[] { stack });
        try {
            return rpc.get(1L, TimeUnit.SECONDS);
        }
        catch (final Exception e) {
            IC2.log.debug(LogCategory.Block, e, "Scannability check failed.");
            return false;
        }
    }
    
    private static boolean isValidStack(ItemStack stack) {
        stack = UuGraph.find(stack);
        return !StackUtil.isEmpty(stack) && UuIndex.instance.get(stack) < Double.POSITIVE_INFINITY;
    }
    
    static {
        RpcHandler.registerProvider(new ServerScannableCheck());
    }
    
    public static class ServerScannableCheck implements IRpcProvider<Boolean>
    {
        @Override
        public Boolean executeRpc(final Object... args) {
            final ItemStack stack = (ItemStack)args[0];
            return isValidStack(stack);
        }
    }
}
