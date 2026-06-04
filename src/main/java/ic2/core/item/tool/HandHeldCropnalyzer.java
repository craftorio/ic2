// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.init.Localization;
import ic2.api.crops.Crops;
import ic2.api.crops.CropCard;
import ic2.core.util.Util;
import ic2.api.crops.ICropSeed;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.IWorldTickCallback;

public class HandHeldCropnalyzer extends HandHeldInventory implements IWorldTickCallback
{
    public HandHeldCropnalyzer(final EntityPlayer player, final ItemStack stack) {
        super(player, stack, 3);
        if (IC2.platform.isSimulating()) {
            IC2.tickHandler.requestContinuousWorldTick(player.getEntityWorld(), this);
        }
    }
    
    public String getName() {
        if (this.hasCustomName()) {
            return this.containerStack.getTagCompound().getString("display");
        }
        return "Cropnalyzer";
    }
    
    public boolean hasCustomName() {
        return StackUtil.getOrCreateNbtData(this.containerStack).hasKey("display");
    }
    
    @Override
    public ContainerBase<HandHeldCropnalyzer> getGuiContainer(final EntityPlayer player) {
        return new ContainerCropnalyzer(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiCropnalyzer(new ContainerCropnalyzer(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
        super.onGuiClosed(player);
        if (IC2.platform.isSimulating()) {
            IC2.tickHandler.removeContinuousWorldTick(player.getEntityWorld(), this);
        }
    }
    
    @Override
    public void onTick(final World world) {
        ItemStack battery = this.inventory[2];
        final ItemStack input = this.inventory[0];
        final ItemStack output = this.inventory[1];
        if (!StackUtil.isEmpty(battery)) {
            final double need = ElectricItem.manager.charge(this.containerStack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
            if (need > 0.0) {
                double get = Info.itemInfo.getEnergyValue(battery);
                if (get > 0.0) {
                    battery = (this.inventory[2] = StackUtil.decSize(battery));
                }
                else {
                    get = ElectricItem.manager.discharge(battery, need, Integer.MAX_VALUE, false, true, false);
                }
                if (get > 0.0) {
                    ElectricItem.manager.charge(this.containerStack, get, 3, true, false);
                }
            }
        }
        if (StackUtil.isEmpty(output) && !StackUtil.isEmpty(input) && input.getItem() instanceof ICropSeed) {
            final int level = ((ICropSeed)input.getItem()).getScannedFromStack(this.inventory[0]);
            if (level < 4) {
                final double ned = energyForLevel(level);
                final double got = ElectricItem.manager.discharge(this.containerStack, ned, 2, true, false, false);
                if (!Util.isSimilar(got, ned)) {
                    return;
                }
                ((ICropSeed)input.getItem()).incrementScannedFromStack(this.inventory[0]);
            }
            this.inventory[1] = input;
            this.inventory[0] = null;
        }
    }
    
    public static int energyForLevel(final int i) {
        switch (i) {
            default: {
                return 10;
            }
            case 1: {
                return 90;
            }
            case 2: {
                return 900;
            }
            case 3: {
                return 9000;
            }
        }
    }
    
    public CropCard crop() {
        return Crops.instance.getCropCard(this.inventory[1]);
    }
    
    public int getScannedLevel() {
        final ItemStack output = this.inventory[1];
        if (output == null || !(output.getItem() instanceof ICropSeed)) {
            return -1;
        }
        return ((ICropSeed)output.getItem()).getScannedFromStack(output);
    }
    
    public String getSeedName() {
        return Localization.translate(this.crop().getUnlocalizedName());
    }
    
    public String getSeedTier() {
        switch (this.crop().getProperties().getTier()) {
            default: {
                return "0";
            }
            case 1: {
                return "I";
            }
            case 2: {
                return "II";
            }
            case 3: {
                return "III";
            }
            case 4: {
                return "IV";
            }
            case 5: {
                return "V";
            }
            case 6: {
                return "VI";
            }
            case 7: {
                return "VII";
            }
            case 8: {
                return "VIII";
            }
            case 9: {
                return "IX";
            }
            case 10: {
                return "X";
            }
            case 11: {
                return "XI";
            }
            case 12: {
                return "XII";
            }
            case 13: {
                return "XIII";
            }
            case 14: {
                return "XIV";
            }
            case 15: {
                return "XV";
            }
            case 16: {
                return "XVI";
            }
        }
    }
    
    public String getSeedDiscovered() {
        return this.crop().getDiscoveredBy();
    }
    
    public String getSeedDesc(final int i) {
        return this.crop().desc(i);
    }
    
    public int getSeedGrowth() {
        return ((ICropSeed)this.inventory[1].getItem()).getGrowthFromStack(this.inventory[1]);
    }
    
    public int getSeedGain() {
        return ((ICropSeed)this.inventory[1].getItem()).getGainFromStack(this.inventory[1]);
    }
    
    public int getSeedResistence() {
        return ((ICropSeed)this.inventory[1].getItem()).getResistanceFromStack(this.inventory[1]);
    }
}
