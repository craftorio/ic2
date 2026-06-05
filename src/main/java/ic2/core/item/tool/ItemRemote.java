package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.BlockDynamite;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRemote extends ItemIC2 {
   public ItemRemote() {
      super(ItemName.remote);
      this.setMaxStackSize(1);
   }

   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (world.isRemote) {
         return EnumActionResult.SUCCESS;
      }

      IBlockState state = world.getBlockState(pos);
      Block block = state.getBlock();
      if (block != BlockName.dynamite.getInstance()) {
         return EnumActionResult.SUCCESS;
      }

      ItemStack stack = StackUtil.get(player, hand);
      if (!(Boolean)state.getValue(BlockDynamite.linked)) {
         addRemote(pos, stack);
         world.setBlockState(pos, state.withProperty(BlockDynamite.linked, true));
      } else {
         int index = hasRemote(pos, stack);
         if (index > -1) {
            world.setBlockState(pos, state.withProperty(BlockDynamite.linked, false));
            removeRemote(index, stack);
         } else {
            IC2.platform.messagePlayer(player, "This dynamite stick is not linked to this remote, cannot unlink.");
         }
      }

      return EnumActionResult.SUCCESS;
   }

   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (world.isRemote) {
         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }

      IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/dynamiteomote.ogg", true, IC2.audioManager.getDefaultVolume());
      launchRemotes(world, stack, player);
      return new ActionResult(EnumActionResult.SUCCESS, stack);
   }

   public static void addRemote(BlockPos pos, ItemStack freq) {
      NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
      if (!compound.hasKey("coords")) {
         compound.setTag("coords", new NBTTagList());
      }

      NBTTagList coords = compound.getTagList("coords", 10);
      NBTTagCompound coord = new NBTTagCompound();
      coord.setInteger("x", pos.getX());
      coord.setInteger("y", pos.getY());
      coord.setInteger("z", pos.getZ());
      coords.appendTag(coord);
      compound.setTag("coords", coords);
      freq.setItemDamage(coords.tagCount());
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
      if (stack.getItemDamage() > 0) {
         tooltip.add("Linked to " + stack.getItemDamage() + " dynamite");
      }
   }

   public static void launchRemotes(World world, ItemStack freq, EntityPlayer player) {
      NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
      if (compound.hasKey("coords")) {
         NBTTagList coords = compound.getTagList("coords", 10);
         int i = 0;

         while (i < coords.tagCount()) {
            NBTTagCompound coord = coords.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(coord.getInteger("x"), coord.getInteger("y"), coord.getInteger("z"));
            if (world.isBlockLoaded(pos)) {
               IBlockState state = world.getBlockState(pos);
               if (state.getBlock() == BlockName.dynamite.getInstance() && (Boolean)state.getValue(BlockDynamite.linked)) {
                  state.getBlock().removedByPlayer(state, world, pos, player, false);
                  world.setBlockToAir(pos);
               }

               coords.removeTag(i);
            } else {
               i++;
            }
         }

         freq.setItemDamage(0);
      }
   }

   public static int hasRemote(BlockPos pos, ItemStack freq) {
      NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
      if (!compound.hasKey("coords")) {
         return -1;
      }

      NBTTagList coords = compound.getTagList("coords", 10);

      for (int i = 0; i < coords.tagCount(); i++) {
         NBTTagCompound coord = coords.getCompoundTagAt(i);
         if (coord.getInteger("x") == pos.getX() && coord.getInteger("y") == pos.getY() && coord.getInteger("z") == pos.getZ()
            )
          {
            return i;
         }
      }

      return -1;
   }

   public static void removeRemote(int index, ItemStack freq) {
      NBTTagCompound compound = StackUtil.getOrCreateNbtData(freq);
      if (compound.hasKey("coords")) {
         NBTTagList coords = compound.getTagList("coords", 10);
         NBTTagList newCoords = new NBTTagList();

         for (int i = 0; i < coords.tagCount(); i++) {
            if (i != index) {
               newCoords.appendTag(coords.getCompoundTagAt(i));
            }
         }

         compound.setTag("coords", newCoords);
         freq.setItemDamage(newCoords.tagCount());
      }
   }
}
