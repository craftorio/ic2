// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import ic2.core.util.Tuple;
import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import java.util.Arrays;
import ic2.core.init.OreValues;
import net.minecraft.world.IBlockAccess;
import ic2.core.Ic2Player;
import net.minecraft.world.ChunkCache;
import java.util.HashMap;
import net.minecraft.util.math.BlockPos;
import ic2.core.IHasGui;
import ic2.core.util.ItemComparableItemStack;
import java.util.Map;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.item.IHandHeldInventory;
import ic2.api.item.IBoxable;
import ic2.core.item.BaseElectricItem;

public class ItemScanner extends BaseElectricItem implements IBoxable, IHandHeldInventory
{
    public ItemScanner() {
        this(ItemName.scanner, 100000.0, 128.0, 1);
    }
    
    public ItemScanner(final ItemName name, final double maxCharge, final double transferLimit, final int tier) {
        super(name, maxCharge, transferLimit, tier);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)tooltip, advanced);
        tooltip.add(Localization.translate("ic2.scanner.range", "" + this.getScanRange()));
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if ((this.tier == 1 && !ElectricItem.manager.use(stack, 50.0, (EntityLivingBase)player)) || (this.tier == 2 && !ElectricItem.manager.use(stack, 250.0, (EntityLivingBase)player))) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.FAIL, (Object)stack);
        }
        if (!world.isRemote) {
            if (IC2.platform.launchGui(player, this.getInventory(player, stack)) && player.openContainer instanceof ContainerToolScanner) {
                final ContainerToolScanner container = (ContainerToolScanner)player.openContainer;
                final Map<ItemComparableItemStack, Integer> scanResult = this.scan(player.getEntityWorld(), player.getPosition(), this.getScanRange());
                container.setResults(this.scanMapToSortedList(scanResult));
            }
        }
        else {
            IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/ODScanner.ogg", true, IC2.audioManager.getDefaultVolume());
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    public boolean onDroppedByPlayer(final ItemStack stack, final EntityPlayer player) {
        if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerToolScanner) {
            final HandHeldScanner scanner = (HandHeldScanner)((ContainerToolScanner)player.openContainer).base;
            if (scanner.isThisContainer(stack)) {
                scanner.saveAsThrown(stack);
                player.closeScreen();
            }
        }
        return true;
    }
    
    public int startLayerScan(final ItemStack stack) {
        return ElectricItem.manager.use(stack, 50.0, null) ? (this.getScanRange() / 2) : 0;
    }
    
    public int getScanRange() {
        return 6;
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    @Override
    public IHasGui getInventory(final EntityPlayer player, final ItemStack stack) {
        return new HandHeldScanner(player, stack);
    }
    
    private Map<ItemComparableItemStack, Integer> scan(final World world, final BlockPos center, final int range) {
        final Map<ItemComparableItemStack, Integer> ret = new HashMap<ItemComparableItemStack, Integer>();
        final ChunkCache cache = new ChunkCache(world, center.add(-range, -range, -range), center.add(range, range, range), 0);
        final EntityPlayer player = Ic2Player.get(world);
        final BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
        for (int y = center.getY() - range; y <= center.getY() + range; ++y) {
            for (int z = center.getZ() - range; z <= center.getZ() + range; ++z) {
                for (int x = center.getX() - range; x <= center.getX() + range; ++x) {
                    tmpPos.setPos(x, y, z);
                    final IBlockState state = cache.getBlockState((BlockPos)tmpPos);
                    if (!state.getBlock().isAir(state, (IBlockAccess)cache, (BlockPos)tmpPos)) {
                        final ItemStack pickStack = StackUtil.getPickStack(world, (BlockPos)tmpPos, state, player);
                        List<ItemStack> drops;
                        if (pickStack != null && OreValues.get(pickStack) > 0) {
                            drops = Arrays.asList(pickStack);
                        }
                        else {
                            drops = StackUtil.getDrops((IBlockAccess)cache, (BlockPos)tmpPos, state, 0);
                            if (drops.isEmpty()) {
                                continue;
                            }
                            if (OreValues.get(drops) <= 0) {
                                continue;
                            }
                        }
                        for (final ItemStack drop : drops) {
                            final ItemComparableItemStack key = new ItemComparableItemStack(drop, true);
                            Integer count = ret.get(key);
                            if (count == null) {
                                count = 0;
                            }
                            count += StackUtil.getSize(drop);
                            ret.put(key, count);
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    private List<Tuple.T2<ItemStack, Integer>> scanMapToSortedList(final Map<ItemComparableItemStack, Integer> map) {
        final List<Tuple.T2<ItemStack, Integer>> ret = new ArrayList<Tuple.T2<ItemStack, Integer>>(map.size());
        for (final Map.Entry<ItemComparableItemStack, Integer> entry : map.entrySet()) {
            ret.add(new Tuple.T2<ItemStack, Integer>(entry.getKey().toStack(), entry.getValue()));
        }
        Collections.sort(ret, new Comparator<Tuple.T2<ItemStack, Integer>>() {
            @Override
            public int compare(final Tuple.T2<ItemStack, Integer> a, final Tuple.T2<ItemStack, Integer> b) {
                return b.b - a.b;
            }
        });
        return ret;
    }
}
