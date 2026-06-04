// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import java.util.Random;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import java.util.Iterator;
import java.util.Set;
import java.util.Queue;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.block.BlockFoam;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.BlockScaffold;
import java.util.LinkedHashSet;
import java.util.ArrayDeque;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.ref.BlockName;
import ic2.core.item.armor.ItemArmorClassicCFPack;
import net.minecraft.item.ItemStack;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.item.ItemGradualInt;

public class ItemClassicSprayer extends ItemGradualInt
{
    public ItemClassicSprayer() {
        super(ItemName.foam_sprayer, 1602);
        this.setMaxStackSize(1);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!IC2.platform.isSimulating()) {
            return EnumActionResult.SUCCESS;
        }
        final ItemStack stack = StackUtil.get(player, hand);
        final ItemStack pack = (ItemStack)player.inventory.armorInventory.get(2);
        final boolean pulledFromCFPack = StackUtil.check(pack) && pack.getItem() == ItemName.cf_pack.getInstance() && ((ItemArmorClassicCFPack)pack.getItem()).getCFPellet(player, pack);
        if (!pulledFromCFPack && this.getCustomDamage(stack) < 100) {
            return EnumActionResult.FAIL;
        }
        if (world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance()) {
            this.sprayFoam(world, pos, calculateDirectionsFromPlayer(player), true);
            if (!pulledFromCFPack) {
                this.applyCustomDamage(stack, 100, (EntityLivingBase)player);
            }
            return EnumActionResult.SUCCESS;
        }
        if (this.sprayFoam(world, pos.offset(facing), calculateDirectionsFromPlayer(player), false)) {
            if (!pulledFromCFPack) {
                this.applyCustomDamage(stack, 100, (EntityLivingBase)player);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    private static boolean[] calculateDirectionsFromPlayer(final EntityPlayer player) {
        final float yaw = player.rotationYaw % 360.0f;
        final float pitch = player.rotationPitch;
        final boolean[] r = { true, true, true, true, true, true };
        if (pitch >= -65.0f && pitch <= 65.0f) {
            if ((yaw >= 300.0f && yaw <= 360.0f) || (yaw >= 0.0f && yaw <= 60.0f)) {
                r[2] = false;
            }
            if (yaw >= 30.0f && yaw <= 150.0f) {
                r[5] = false;
            }
            if (yaw >= 120.0f && yaw <= 240.0f) {
                r[3] = false;
            }
            if (yaw >= 210.0f && yaw <= 330.0f) {
                r[4] = false;
            }
        }
        if (pitch <= -40.0f) {
            r[0] = false;
        }
        if (pitch >= 40.0f) {
            r[1] = false;
        }
        return r;
    }
    
    public boolean sprayFoam(final World world, final BlockPos start, final boolean[] directions, final boolean scaffold) {
        if (!canFoam(world, start, scaffold)) {
            return false;
        }
        final Queue<BlockPos> check = new ArrayDeque<BlockPos>();
        final Set<BlockPos> place = new LinkedHashSet<BlockPos>();
        int foamcount = getSprayMass();
        check.add(start);
        BlockPos set;
        while ((set = check.poll()) != null && foamcount > 0) {
            if (canFoam(world, set, scaffold) && place.add(set)) {
                for (final int i : generateRngSpread(IC2.random)) {
                    if (scaffold || directions[i]) {
                        check.add(set.offset(EnumFacing.getFront(i)));
                    }
                }
                --foamcount;
            }
        }
        for (final BlockPos pos : place) {
            final IBlockState state = world.getBlockState(pos);
            final Block targetBlock = state.getBlock();
            if (targetBlock == BlockName.scaffold.getInstance()) {
                final BlockScaffold block = (BlockScaffold)targetBlock;
                switch ((BlockScaffold.ScaffoldType)state.getValue((IProperty)block.getTypeProperty())) {
                    case wood:
                    case reinforced_wood: {
                        block.dropBlockAsItem(world, pos, state, 0);
                        world.setBlockState(pos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal));
                        continue;
                    }
                }
            }
            else if (targetBlock == BlockName.te.getInstance()) {
                final TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TileEntityCable)) {
                    continue;
                }
                ((TileEntityCable)te).foam();
            }
            else {
                world.setBlockState(pos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal));
            }
        }
        return true;
    }
    
    private static boolean canFoam(final World world, final BlockPos pos, final boolean scaffold) {
        if (scaffold) {
            return world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance();
        }
        if (BlockName.foam.getInstance().canPlaceBlockOnSide(world, pos, EnumFacing.DOWN)) {
            return true;
        }
        if (world.getBlockState(pos).getBlock() != BlockName.te.getInstance()) {
            return false;
        }
        final TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityCable && !((TileEntityCable)te).isFoamed();
    }
    
    private static int[] generateRngSpread(final Random random) {
        final int[] re = { 0, 1, 2, 3, 4, 5 };
        for (int i = 0; i < 16; ++i) {
            final int first = random.nextInt(6);
            final int second = random.nextInt(6);
            final int temp = re[first];
            re[first] = re[second];
            re[second] = temp;
        }
        return re;
    }
    
    public static int getSprayMass() {
        return 13;
    }
    
    @Override
    public double getDurabilityForDisplay(final ItemStack stack) {
        return 1.0 - super.getDurabilityForDisplay(stack);
    }
}
