// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.resources;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import net.minecraft.client.Minecraft;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import ic2.core.profile.NotClassic;
import ic2.api.item.IKineticRotor;
import ic2.core.item.ItemGradualInt;

@NotClassic
public class ItemWindRotor extends ItemGradualInt implements IKineticRotor
{
    private final int maxWindStrength;
    private final int minWindStrength;
    private final int radius;
    private final float efficiency;
    private final ResourceLocation renderTexture;
    private final boolean water;
    
    public ItemWindRotor(final ItemName name, final int Radius, final int durability, final float efficiency, final int minWindStrength, final int maxWindStrength, final ResourceLocation RenderTexture) {
        super(name, durability);
        this.setMaxStackSize(1);
        this.radius = Radius;
        this.efficiency = efficiency;
        this.renderTexture = RenderTexture;
        this.minWindStrength = minWindStrength;
        this.maxWindStrength = maxWindStrength;
        this.water = (name != ItemName.rotor_wood);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add(Localization.translate("ic2.itemrotor.wind.info", this.minWindStrength, this.maxWindStrength));
        GearboxType type = null;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiWaterKineticGenerator) {
            type = GearboxType.WATER;
        }
        else if (Minecraft.getMinecraft().currentScreen instanceof GuiWindKineticGenerator) {
            type = GearboxType.WIND;
        }
        if (type != null) {
            tooltip.add(Localization.translate("ic2.itemrotor.fitsin." + this.isAcceptedType(stack, type)));
        }
    }
    
    @Override
    public int getDiameter(final ItemStack stack) {
        return this.radius;
    }
    
    @Override
    public ResourceLocation getRotorRenderTexture(final ItemStack stack) {
        return this.renderTexture;
    }
    
    @Override
    public float getEfficiency(final ItemStack stack) {
        return this.efficiency;
    }
    
    @Override
    public int getMinWindStrength(final ItemStack stack) {
        return this.minWindStrength;
    }
    
    @Override
    public int getMaxWindStrength(final ItemStack stack) {
        return this.maxWindStrength;
    }
    
    @Override
    public boolean isAcceptedType(final ItemStack stack, final GearboxType type) {
        return type == GearboxType.WIND || this.water;
    }
}
