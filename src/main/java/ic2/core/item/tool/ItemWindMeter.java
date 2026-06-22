package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.event.WorldData;
import ic2.core.item.PriorityUsableItem;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

@NotClassic
public class ItemWindMeter extends ItemElectricTool implements PriorityUsableItem
{
	public ItemWindMeter(Properties settings)
	{
		super(settings, 50);
		this.maxCharge = 10000;
		this.transferLimit = 100;
		this.tier = 1;
	}

	private static float roundWind(double windStrength)
	{
		return (float) Math.round(windStrength * 100.0) / 100.0F;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		tooltip.add(Component.translatable("ic2.wind_meter.tooltipA"));
		tooltip.add(Component.translatable("ic2.wind_meter.tooltipB"));
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (world.isClientSide || player == null || player.isShiftKeyDown())
		{
			return InteractionResult.PASS;
		}

		if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost))
		{
			return InteractionResult.PASS;
		}

		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof TileEntityWindKineticGenerator windyTE)
		{
			if (!windyTE.getActive())
			{
				if (windyTE.hasRotor())
				{
					IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.rotor.blocked").getString());
				} else
				{
					IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.rotor.none").getString());
				}

				return InteractionResult.FAIL;
			} else
			{
				this.consumeEnergy(stack, this.operationEnergyCost, player);
				if (windyTE.getObstructions() >= 0)
				{
					float displayWind = roundWind(windyTE.calcWindStrength());
					if (displayWind <= 0.0F)
					{
						IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.obstructed", windyTE.getObstructions()).getString());
					} else
					{
						IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.effective", displayWind).getString());
					}
				} else
				{
					IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.blocked", windyTE.getRotorDiameter() * 3).getString());
				}

				return InteractionResult.SUCCESS;
			}
		} else if (te instanceof TileEntityWindGenerator windyTE)
		{
			this.consumeEnergy(stack, this.operationEnergyCost, player);
			double obstructiveFactor = windyTE.getObstructions() / 567.0;
			double wind = obstructiveFactor >= 1.0 ? 0.0 : WorldData.get(world).windSim.getWindAt(pos.getY()) * (1.0 - obstructiveFactor);
			float displayWind = roundWind(wind);
			if (displayWind <= 0.0F)
			{
				IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.obstructed", windyTE.getObstructions()).getString());
			} else
			{
				IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info.effective", displayWind).getString());
			}

			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!IC2.sideProxy.isSimulating())
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		if (!this.consumeEnergy(stack, this.operationEnergyCost, player))
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		double windStrength = WorldData.get(world).windSim.getWindAt(player.getY());
		if (windStrength < 0.0)
		{
			windStrength = 0.0;
		}

		IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.wind_meter.info", roundWind(windStrength)).getString());
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}
}
