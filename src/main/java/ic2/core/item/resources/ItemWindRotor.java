package ic2.core.item.resources;

import ic2.api.item.IKineticRotor;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import ic2.core.profile.NotClassic;

import java.util.List;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemWindRotor extends Item implements IKineticRotor
{
	private final int maxWindStrength;
	private final int minWindStrength;
	private final int radius;
	private final float efficiency;
	private final ResourceLocation renderTexture;
	private final boolean water;

	public ItemWindRotor(
		Properties settings, int Radius, boolean water, float efficiency, int minWindStrength, int maxWindStrength, ResourceLocation RenderTexture
	)
	{
		super(settings);
		this.radius = Radius;
		this.efficiency = efficiency;
		this.renderTexture = RenderTexture;
		this.minWindStrength = minWindStrength;
		this.maxWindStrength = maxWindStrength;
		this.water = water;
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		tooltip.add(Component.translatable("ic2.itemrotor.wind.info", this.minWindStrength, this.maxWindStrength).withStyle(ChatFormatting.GRAY));
		IKineticRotor.GearboxType type = null;
		if (Minecraft.getInstance().screen instanceof GuiWaterKineticGenerator)
		{
			type = IKineticRotor.GearboxType.WATER;
		} else if (Minecraft.getInstance().screen instanceof GuiWindKineticGenerator)
		{
			type = IKineticRotor.GearboxType.WIND;
		}

		if (type != null)
		{
			tooltip.add(Component.translatable("ic2.itemrotor.fitsin." + this.isAcceptedType(stack, type)).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public int getDiameter(ItemStack stack)
	{
		return this.radius;
	}

	@Override
	public ResourceLocation getRotorRenderTexture(ItemStack stack)
	{
		return this.renderTexture;
	}

	@Override
	public float getEfficiency(ItemStack stack)
	{
		return this.efficiency;
	}

	@Override
	public int getMinWindStrength(ItemStack stack)
	{
		return this.minWindStrength;
	}

	@Override
	public int getMaxWindStrength(ItemStack stack)
	{
		return this.maxWindStrength;
	}

	@Override
	public boolean isAcceptedType(ItemStack stack, IKineticRotor.GearboxType type)
	{
		return type == IKineticRotor.GearboxType.WIND || this.water;
	}
}
