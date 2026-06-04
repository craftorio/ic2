// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.audio.PositionSpec;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;
import ic2.core.util.Ic2BlockPos;
import net.minecraft.item.Item;
import com.google.common.base.Predicate;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.item.ITerraformingBP;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.audio.AudioSource;
import net.minecraft.util.math.BlockPos;

public class TileEntityTerra extends TileEntityElectricMachine
{
    public int failedAttempts;
    private BlockPos lastPos;
    public AudioSource audioSource;
    public int inactiveTicks;
    public final InvSlotConsumableClass tfbpSlot;
    
    public TileEntityTerra() {
        super(100000, 4);
        this.failedAttempts = 0;
        this.inactiveTicks = 0;
        this.tfbpSlot = new InvSlotConsumableClass(this, "tfbp", 1, ITerraformingBP.class);
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        super.onUnloaded();
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean newActive = false;
        final ItemStack stack = this.tfbpSlot.get();
        if (!StackUtil.isEmpty(stack)) {
            final ITerraformingBP tfbp = (ITerraformingBP)stack.getItem();
            if (this.energy.getEnergy() >= tfbp.getConsume(stack)) {
                newActive = true;
                final World world = this.getWorld();
                BlockPos nextPos;
                if (this.lastPos != null) {
                    final int range = tfbp.getRange(stack) / 10;
                    nextPos = new BlockPos(this.lastPos.getX() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1), this.pos.getY(), this.lastPos.getZ() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1));
                }
                else {
                    if (this.failedAttempts > 4) {
                        this.failedAttempts = 4;
                    }
                    final int range = tfbp.getRange(stack) * (this.failedAttempts + 1) / 5;
                    nextPos = new BlockPos(this.pos.getX() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1), this.pos.getY(), this.pos.getZ() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1));
                }
                if (tfbp.terraform(stack, world, nextPos)) {
                    this.energy.useEnergy(tfbp.getConsume(stack));
                    this.failedAttempts = 0;
                    this.lastPos = nextPos;
                }
                else {
                    this.energy.useEnergy(tfbp.getConsume(stack) / 10.0);
                    ++this.failedAttempts;
                    this.lastPos = null;
                }
            }
        }
        if (newActive) {
            this.inactiveTicks = 0;
            this.setActive(true);
        }
        else if (!newActive && this.getActive() && this.inactiveTicks++ > 30) {
            this.setActive(false);
        }
    }
    
    public boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final World world = this.getWorld();
        if (!player.isSneaking() && !world.isRemote) {
            if (this.ejectBlueprint()) {
                return true;
            }
            final ItemStack stack = StackUtil.consumeAndGet(player, hand, (Predicate<ItemStack>)new Predicate<ItemStack>() {
                public boolean apply(final ItemStack input) {
                    final Item item = input.getItem();
                    return item instanceof ITerraformingBP && ((ITerraformingBP)item).canInsert(input, player, world, TileEntityTerra.this.pos);
                }
            }, 1);
            if (!StackUtil.isEmpty(stack)) {
                this.insertBlueprint(stack);
                return true;
            }
        }
        return true;
    }
    
    private boolean ejectBlueprint() {
        final ItemStack stack = this.tfbpSlot.get();
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        StackUtil.dropAsEntity(this.getWorld(), this.pos, stack);
        this.tfbpSlot.clear();
        return true;
    }
    
    private void insertBlueprint(final ItemStack tfbp) {
        if (!this.tfbpSlot.isEmpty()) {
            throw new IllegalStateException("not empty");
        }
        this.tfbpSlot.put(tfbp);
    }
    
    public static BlockPos getFirstSolidBlockFrom(final World world, final BlockPos pos, final int yOffset) {
        final Ic2BlockPos ret = new Ic2BlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());
        while (ret.getY() >= 0) {
            if (world.isBlockNormalCube((BlockPos)ret, false)) {
                return new BlockPos((Vec3i)ret);
            }
            ret.moveDown();
        }
        return null;
    }
    
    public static BlockPos getFirstBlockFrom(final World world, final BlockPos pos, final int yOffset) {
        final BlockPos.MutableBlockPos ret = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());
        while (ret.getY() >= 0) {
            if (!world.isAirBlock((BlockPos)ret)) {
                return new BlockPos((Vec3i)ret);
            }
            ret.setPos(ret.getX(), ret.getY() - 1, ret.getZ());
        }
        return null;
    }
    
    public static boolean switchGround(final World world, final BlockPos pos, final Block from, final IBlockState to, final boolean upwards) {
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        while (cPos.getY() >= 0) {
            final Block block = world.getBlockState((BlockPos)cPos).getBlock();
            if (upwards && block != from) {
                break;
            }
            if (!upwards && block == from) {
                break;
            }
            cPos.setPos(cPos.getX(), cPos.getY() - 1, cPos.getZ());
        }
        if ((upwards && cPos.getY() == pos.getY()) || (!upwards && cPos.getY() < 0)) {
            return false;
        }
        world.setBlockState(upwards ? cPos.up() : new BlockPos((Vec3i)cPos), to);
        return true;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        if (field.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Terraformers/TerraformerGenericloop.ogg", true, false, IC2.audioManager.getDefaultVolume());
            }
            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            }
            else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }
        super.onNetworkUpdate(field);
    }
}
