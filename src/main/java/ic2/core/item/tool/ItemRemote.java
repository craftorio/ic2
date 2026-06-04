// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import ic2.core.audio.PositionSpec;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.IC2;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.BlockDynamite;
import ic2.core.util.StackUtil;
import ic2.core.ref.BlockName;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.item.ItemIC2;

public class ItemRemote extends ItemIC2
{
    public ItemRemote() {
        super(ItemName.remote);
        this.setMaxStackSize(1);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block != BlockName.dynamite.getInstance()) {
            return EnumActionResult.SUCCESS;
        }
        final ItemStack stack = StackUtil.get(player, hand);
        if (!(boolean)state.getValue((IProperty)BlockDynamite.linked)) {
            addRemote(pos, stack);
            world.setBlockState(pos, state.withProperty((IProperty)BlockDynamite.linked, (Comparable)true));
        }
        else {
            final int index = hasRemote(pos, stack);
            if (index > -1) {
                world.setBlockState(pos, state.withProperty((IProperty)BlockDynamite.linked, (Comparable)false));
                removeRemote(index, stack);
            }
            else {
                IC2.platform.messagePlayer(player, "This dynamite stick is not linked to this remote, cannot unlink.", new Object[0]);
            }
        }
        return EnumActionResult.SUCCESS;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (world.isRemote) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/dynamiteomote.ogg", true, IC2.audioManager.getDefaultVolume());
        launchRemotes(world, stack, player);
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    public static void addRemote(final BlockPos pos, final ItemStack freq) {
        final NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.hasKey("coords")) {
            compound.setTag("coords", (NBTBase)new NBTTagList());
        }
        final NBTTagList coords = compound.getTagList("coords", 10);
        final NBTTagCompound coord = new NBTTagCompound();
        coord.setInteger("x", pos.getX());
        coord.setInteger("y", pos.getY());
        coord.setInteger("z", pos.getZ());
        coords.appendTag((NBTBase)coord);
        compound.setTag("coords", (NBTBase)coords);
        freq.setItemDamage(coords.tagCount());
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        if (stack.getItemDamage() > 0) {
            tooltip.add("Linked to " + stack.getItemDamage() + " dynamite");
        }
    }
    
    public static void launchRemotes(final World world, final ItemStack freq, final EntityPlayer player) {
        final NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.hasKey("coords")) {
            return;
        }
        final NBTTagList coords = compound.getTagList("coords", 10);
        int i = 0;
        while (i < coords.tagCount()) {
            final NBTTagCompound coord = coords.getCompoundTagAt(i);
            final BlockPos pos = new BlockPos(coord.getInteger("x"), coord.getInteger("y"), coord.getInteger("z"));
            if (world.isBlockLoaded(pos)) {
                final IBlockState state = world.getBlockState(pos);
                if (state.getBlock() == BlockName.dynamite.getInstance() && (boolean)state.getValue((IProperty)BlockDynamite.linked)) {
                    state.getBlock().removedByPlayer(state, world, pos, player, false);
                    world.setBlockToAir(pos);
                }
                coords.removeTag(i);
            }
            else {
                ++i;
            }
        }
        freq.setItemDamage(0);
    }
    
    public static int hasRemote(final BlockPos pos, final ItemStack freq) {
        final NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.hasKey("coords")) {
            return -1;
        }
        final NBTTagList coords = compound.getTagList("coords", 10);
        for (int i = 0; i < coords.tagCount(); ++i) {
            final NBTTagCompound coord = coords.getCompoundTagAt(i);
            if (coord.getInteger("x") == pos.getX() && coord.getInteger("y") == pos.getY() && coord.getInteger("z") == pos.getZ()) {
                return i;
            }
        }
        return -1;
    }
    
    public static void removeRemote(final int index, final ItemStack freq) {
        final NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.hasKey("coords")) {
            return;
        }
        final NBTTagList coords = compound.getTagList("coords", 10);
        final NBTTagList newCoords = new NBTTagList();
        for (int i = 0; i < coords.tagCount(); ++i) {
            if (i != index) {
                newCoords.appendTag((NBTBase)coords.getCompoundTagAt(i));
            }
        }
        compound.setTag("coords", (NBTBase)newCoords);
        freq.setItemDamage(newCoords.tagCount());
    }
}
