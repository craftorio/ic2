// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fluids.Fluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import java.util.Iterator;
import java.util.Set;
import java.util.Queue;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.block.BlockIC2Fence;
import ic2.core.block.BlockFoam;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.BlockScaffold;
import ic2.core.ref.BlockName;
import java.util.HashSet;
import java.util.ArrayDeque;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import ic2.core.util.LiquidUtil;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.FluidName;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.ref.ItemName;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2FluidContainer;

public class ItemSprayer extends ItemIC2FluidContainer implements IBoxable
{
    public ItemSprayer() {
        super(ItemName.foam_sprayer, 8000);
        this.setMaxStackSize(1);
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        subItems.add((Object)new ItemStack((Item)this));
        subItems.add((Object)this.getItemStack(FluidName.construction_foam));
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (IC2.platform.isSimulating() && IC2.keyboard.isModeSwitchKeyDown(player)) {
            final ItemStack stack = StackUtil.get(player, hand);
            final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            int mode = nbtData.getInteger("mode");
            mode = ((mode == 0) ? 1 : 0);
            nbtData.setInteger("mode", mode);
            final String sMode = (mode == 0) ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single";
            IC2.platform.messagePlayer(player, "ic2.tooltip.mode", sMode);
        }
        return (ActionResult<ItemStack>)super.onItemRightClick(world, player, hand);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            return EnumActionResult.PASS;
        }
        if (!IC2.platform.isSimulating()) {
            return EnumActionResult.SUCCESS;
        }
        final RayTraceResult rtResult = this.rayTrace(world, player, true);
        if (rtResult == null) {
            return EnumActionResult.PASS;
        }
        if (rtResult.typeOfHit == RayTraceResult.Type.BLOCK && !pos.equals((Object)rtResult.getBlockPos())) {
            final BlockPos fluidPos = rtResult.getBlockPos();
            if (LiquidUtil.drainBlockToContainer(world, fluidPos, player, hand)) {
                return EnumActionResult.SUCCESS;
            }
        }
        int maxFoamBlocks = 0;
        final ItemStack stack = StackUtil.get(player, hand);
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid != null && fluid.amount > 0) {
            maxFoamBlocks += fluid.amount / this.getFluidPerFoam();
        }
        ItemStack pack = (ItemStack)player.inventory.armorInventory.get(2);
        if (pack != null && pack.getItem() == ItemName.cf_pack.getInstance()) {
            fluid = FluidUtil.getFluidContained(pack);
            if (fluid != null && fluid.amount > 0) {
                maxFoamBlocks += fluid.amount / this.getFluidPerFoam();
            }
            else {
                pack = null;
            }
        }
        else {
            pack = null;
        }
        if (maxFoamBlocks == 0) {
            return EnumActionResult.FAIL;
        }
        maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));
        Target target;
        if (canPlaceFoam(world, pos, Target.Scaffold)) {
            target = Target.Scaffold;
        }
        else if (canPlaceFoam(world, pos, Target.Cable)) {
            target = Target.Cable;
        }
        else {
            pos = pos.offset(side);
            target = Target.Any;
        }
        final Vec3d viewVec = player.getLookVec();
        final EnumFacing playerViewFacing = EnumFacing.getFacingFromVector((float)viewVec.x, (float)viewVec.y, (float)viewVec.z);
        int amount = this.sprayFoam(world, pos, playerViewFacing.getOpposite(), target, maxFoamBlocks);
        amount *= this.getFluidPerFoam();
        if (amount > 0) {
            if (pack != null) {
                final IFluidHandlerItem packHandler = FluidUtil.getFluidHandler(pack);
                assert packHandler != null;
                fluid = packHandler.drain(amount, true);
                amount -= fluid.amount;
                player.inventory.armorInventory.set(2, (Object)packHandler.getContainer());
            }
            if (amount > 0) {
                final IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                assert handler != null;
                handler.drain(amount, true);
                StackUtil.set(player, hand, handler.getContainer());
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    public int sprayFoam(final World world, final BlockPos pos, final EnumFacing excludedDir, final Target target, final int maxFoamBlocks) {
        if (!canPlaceFoam(world, pos, target)) {
            return 0;
        }
        final Queue<BlockPos> toCheck = new ArrayDeque<BlockPos>();
        final Set<BlockPos> positions = new HashSet<BlockPos>();
        toCheck.add(pos);
        BlockPos cPos;
        while ((cPos = toCheck.poll()) != null && positions.size() < maxFoamBlocks) {
            if (!canPlaceFoam(world, cPos, target)) {
                continue;
            }
            if (!positions.add(cPos)) {
                continue;
            }
            for (final EnumFacing dir : EnumFacing.VALUES) {
                if (dir != excludedDir) {
                    toCheck.add(cPos.offset(dir));
                }
            }
        }
        toCheck.clear();
        int failedPlacements = 0;
        for (final BlockPos targetPos : positions) {
            final IBlockState state = world.getBlockState(targetPos);
            final Block targetBlock = state.getBlock();
            if (targetBlock == BlockName.scaffold.getInstance()) {
                final BlockScaffold scaffold = (BlockScaffold)targetBlock;
                switch ((BlockScaffold.ScaffoldType)state.getValue((IProperty)scaffold.getTypeProperty())) {
                    case wood:
                    case reinforced_wood: {
                        scaffold.dropBlockAsItem(world, targetPos, state, 0);
                        world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal));
                        continue;
                    }
                    case reinforced_iron: {
                        StackUtil.dropAsEntity(world, targetPos, BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
                    }
                    case iron: {
                        world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.reinforced));
                        continue;
                    }
                }
            }
            else if (targetBlock == BlockName.te.getInstance()) {
                final TileEntity te = world.getTileEntity(targetPos);
                if (!(te instanceof TileEntityCable) || ((TileEntityCable)te).foam()) {
                    continue;
                }
                ++failedPlacements;
            }
            else {
                if (world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal))) {
                    continue;
                }
                ++failedPlacements;
            }
        }
        return positions.size() - failedPlacements;
    }
    
    protected int getMaxFoamBlocks(final ItemStack stack) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        if (nbtData.getInteger("mode") == 0) {
            return 10;
        }
        return 1;
    }
    
    protected int getFluidPerFoam() {
        return 100;
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    @Override
    public boolean canfill(final Fluid fluid) {
        return fluid == FluidName.construction_foam.getInstance();
    }
    
    private static boolean canPlaceFoam(final World world, final BlockPos pos, final Target target) {
        switch (target) {
            case Any: {
                return BlockName.foam.getInstance().canPlaceBlockOnSide(world, pos, EnumFacing.DOWN);
            }
            case Scaffold: {
                return world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance();
            }
            case Cable: {
                if (world.getBlockState(pos).getBlock() != BlockName.te.getInstance()) {
                    return false;
                }
                final TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityCable) {
                    return !((TileEntityCable)te).isFoamed();
                }
                break;
            }
            default: {
                assert false;
                break;
            }
        }
        return false;
    }
    
    private enum Target
    {
        Any, 
        Scaffold, 
        Cable;
    }
}
