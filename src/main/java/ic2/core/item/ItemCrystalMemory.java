package ic2.core.item;

import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemCrystalMemory extends ItemIC2 {
   public ItemCrystalMemory() {
      super(ItemName.crystal_memory);
      this.setMaxStackSize(1);
   }

   public boolean isRepairable() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
      ItemStack recorded = this.readItemStack(stack);
      if (!StackUtil.isEmpty(recorded)) {
         tooltip.add(Localization.translate("ic2.item.CrystalMemory.tooltip.Item") + " " + recorded.getDisplayName());
         tooltip.add(
            Localization.translate("ic2.item.CrystalMemory.tooltip.UU-Matter") + " " + Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4) + "B"
         );
      } else {
         tooltip.add(Localization.translate("ic2.item.CrystalMemory.tooltip.Empty"));
      }
   }

   public ItemStack readItemStack(ItemStack stack) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      NBTTagCompound contentTag = nbt.getCompoundTag("Pattern");
      return new ItemStack(contentTag);
   }

   public void writecontentsTag(ItemStack stack, ItemStack recorded) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      NBTTagCompound contentTag = new NBTTagCompound();
      recorded.writeToNBT(contentTag);
      nbt.setTag("Pattern", contentTag);
   }
}
