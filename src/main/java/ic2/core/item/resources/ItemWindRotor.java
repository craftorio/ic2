package ic2.core.item.resources;

import ic2.api.item.IKineticRotor;
import ic2.core.init.Localization;
import ic2.core.item.ItemGradualInt;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemWindRotor extends ItemGradualInt implements IKineticRotor {
  private final int maxWindStrength;
  
  private final int minWindStrength;
  
  private final int radius;
  
  private final float efficiency;
  
  private final ResourceLocation renderTexture;
  
  private final boolean water;
  
  public ItemWindRotor(ItemName name, int Radius, int durability, float efficiency, int minWindStrength, int maxWindStrength, ResourceLocation RenderTexture) {
    super(name, durability);
    setMaxStackSize(1);
    this.radius = Radius;
    this.efficiency = efficiency;
    this.renderTexture = RenderTexture;
    this.minWindStrength = minWindStrength;
    this.maxWindStrength = maxWindStrength;
    this.water = (name != ItemName.rotor_wood);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add(Localization.translate("ic2.itemrotor.wind.info", new Object[] { Integer.valueOf(this.minWindStrength), Integer.valueOf(this.maxWindStrength) }));
    IKineticRotor.GearboxType type = null;
    if ((Minecraft.getMinecraft()).currentScreen instanceof ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator) {
      type = IKineticRotor.GearboxType.WATER;
    } else if ((Minecraft.getMinecraft()).currentScreen instanceof ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator) {
      type = IKineticRotor.GearboxType.WIND;
    } 
    if (type != null)
      tooltip.add(Localization.translate("ic2.itemrotor.fitsin." + isAcceptedType(stack, type))); 
  }
  
  public int getDiameter(ItemStack stack) {
    return this.radius;
  }
  
  public ResourceLocation getRotorRenderTexture(ItemStack stack) {
    return this.renderTexture;
  }
  
  public float getEfficiency(ItemStack stack) {
    return this.efficiency;
  }
  
  public int getMinWindStrength(ItemStack stack) {
    return this.minWindStrength;
  }
  
  public int getMaxWindStrength(ItemStack stack) {
    return this.maxWindStrength;
  }
  
  public boolean isAcceptedType(ItemStack stack, IKineticRotor.GearboxType type) {
    return (type == IKineticRotor.GearboxType.WIND || this.water);
  }
}
