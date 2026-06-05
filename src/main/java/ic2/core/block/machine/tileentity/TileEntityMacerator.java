package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.recipe.BasicMachineRecipeManager;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMacerator extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
   public static List<Entry<ItemStack, ItemStack>> recipes = new Vector<>();

   public TileEntityMacerator() {
      super(2, 300, 1);
      this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.macerator);
   }

   public static void init() {
      Recipes.macerator = new BasicMachineRecipeManager();
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected void updateEntityClient() {
      super.updateEntityClient();
      World world = this.getWorld();
      if (this.getActive() && world.rand.nextInt(8) == 0) {
         for (int i = 0; i < 4; i++) {
            double x = this.pos.getX() + 0.5 + world.rand.nextFloat() * 0.6 - 0.3;
            double y = this.pos.getY() + 1 + world.rand.nextFloat() * 0.2 - 0.1;
            double z = this.pos.getZ() + 0.5 + world.rand.nextFloat() * 0.6 - 0.3;
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0, new int[0]);
         }
      }
   }

   @Override
   public String getStartSoundFile() {
      return "Machines/MaceratorOp.ogg";
   }

   @Override
   public String getInterruptSoundFile() {
      return "Machines/InterruptOne.ogg";
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(
         UpgradableProperty.Processing,
         UpgradableProperty.Transformer,
         UpgradableProperty.EnergyStorage,
         UpgradableProperty.ItemConsuming,
         UpgradableProperty.ItemProducing
      );
   }
}
