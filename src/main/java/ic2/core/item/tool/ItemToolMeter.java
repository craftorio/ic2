package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemToolMeter extends ItemIC2 implements IBoxable, IHandHeldInventory {
   public ItemToolMeter() {
      super(ItemName.meter);
      this.maxStackSize = 1;
      this.setMaxDamage(0);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      if (world.isRemote) {
         return EnumActionResult.PASS;
      }

      IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
      if (!(tile instanceof IEnergySource) && !(tile instanceof IEnergyConductor) && !(tile instanceof IEnergySink)) {
         IC2.platform.messagePlayer(player, "Not an energy net tile");
      } else if (IC2.platform.launchGui(player, this.getInventory(player, StackUtil.get(player, hand)))) {
         ContainerMeter container = (ContainerMeter)player.openContainer;
         container.setUut(tile);
         return EnumActionResult.SUCCESS;
      }

      return EnumActionResult.SUCCESS;
   }

   public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
      if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerMeter) {
         HandHeldMeter euReader = ((ContainerMeter)player.openContainer).base;
         if (euReader.isThisContainer(stack)) {
            euReader.saveAsThrown(stack);
            player.closeScreen();
         }
      }

      return true;
   }

   @Override
   public boolean canBeStoredInToolbox(ItemStack itemstack) {
      return true;
   }

   @Override
   public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
      return new HandHeldMeter(player, stack);
   }
}
