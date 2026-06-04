// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;
import ic2.core.init.Localization;
import ic2.core.util.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemCrystalMemory extends ItemIC2
{
    public ItemCrystalMemory() {
        super(ItemName.crystal_memory);
        this.setMaxStackSize(1);
    }
    
    public boolean isRepairable() {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final ItemStack recorded = this.readItemStack(stack);
        if (!StackUtil.isEmpty(recorded)) {
            tooltip.add(Localization.translate("ic2.item.CrystalMemory.tooltip.Item") + " " + recorded.getDisplayName());
            tooltip.add(Localization.translate("ic2.item.CrystalMemory.tooltip.UU-Matter") + " " + Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4) + "B");
        }
        else {
            tooltip.add(Localization.translate("ic2.item.CrystalMemory.tooltip.Empty"));
        }
    }
    
    public ItemStack readItemStack(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final NBTTagCompound contentTag = nbt.getCompoundTag("Pattern");
        final ItemStack Item = new ItemStack(contentTag);
        return Item;
    }
    
    public void writecontentsTag(final ItemStack stack, final ItemStack recorded) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final NBTTagCompound contentTag = new NBTTagCompound();
        recorded.writeToNBT(contentTag);
        nbt.setTag("Pattern", (NBTBase)contentTag);
    }
}
