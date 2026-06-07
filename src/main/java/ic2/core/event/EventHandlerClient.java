package ic2.core.event;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.fluid.FluidHandler;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.network.RpcHandler;
import ic2.core.proxy.SideProxyClient;
import ic2.core.sound.SoundManagerClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class EventHandlerClient
{
	public static void onClientSetup()
	{
		SideProxyClient.envProxy
			.registerModelPredicateProvider(IC2.getIdentifier("charge"), (stack, world, entity, seed) -> (float) ElectricItem.manager.getChargeLevel(stack));

		for (Direction direction : Util.ALL_DIRS)
		{
			SideProxyClient.envProxy
				.registerModelPredicateProvider(
					IC2.getIdentifier(direction.m_122433_()),
					(stack, world, entity, seed) -> stack.getItem() instanceof ItemUpgradeModule && ItemUpgradeModule.getDirection(stack) == direction
						? 1.0F
						: 0.0F
				);
		}

		SideProxyClient.envProxy
			.registerModelPredicateProvider(
				IC2.getIdentifier("nano_saber_active"),
				(stack, world, entity, seed) -> stack.getItem() instanceof AbstractItemNanoSaber nanoSaber ? nanoSaber.getActiveData() : 0.0F
			);
		SideProxyClient.envProxy
			.registerModelPredicateProvider(
				IC2.getIdentifier("tool_box_open"),
				(stack, world, entity, seed) ->
				{
					if (!(entity instanceof Player player))
					{
						return 0.0F;
					} else
					{
						ItemStack mainHandItem;
						boolean open = player.f_36096_ instanceof ContainerToolbox
							&& (
							StackUtil.checkItemEquality(mainHandItem = player.m_21205_(), stack)
								|| StackUtil.checkItemEquality(player.m_21206_(), stack)
								&& (mainHandItem == null || !(mainHandItem.getItem() instanceof IHandHeldInventory))
						);
						return open ? 1.0F : 0.0F;
					}
				}
			);
	}

	public static void onSoundSetup()
	{
	}

	public static void livingEntityPreRender(LivingEntity entity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer)
	{
	}

	public static void livingEntityPostRender(LivingEntity entity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer)
	{
	}

	public static float onSetupFogDensity(BlockState state)
	{
		Fluid fluid = FluidHandler.getWorldFluid(state);
		if (fluid != null && "ic2".equals(Registry.f_122822_.getKey(fluid).m_135827_()))
		{
			int density = FluidHandler.getDensity(fluid);
			return (float) Util.map(Math.abs(density), 20000.0, 2.0);
		} else
		{
			return -1.0F;
		}
	}

	public static int onRenderFogColor(BlockState state)
	{
		Fluid fluid = FluidHandler.getWorldFluid(state);
		return fluid != null && "ic2".equals(Registry.f_122822_.getKey(fluid).m_135827_()) ? FluidHandler.getColor(fluid) : -1;
	}

	public static void onDrawBlockHighlight(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		assert target.m_6662_() == Type.BLOCK;
		ItemStack inHand = StackUtil.get(player, InteractionHand.MAIN_HAND);
		if (inHand.getItem() instanceof IEnhancedOverlayProvider)
		{
		}
	}

	public static boolean onDrawBlockHighlightLast(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		assert target.m_6662_() == Type.BLOCK;
		Level world = player.getCommandSenderWorld();
		BlockPos pos = target.m_82425_();
		if (!world.m_6857_().m_61937_(pos))
		{
			return false;
		}

		BlockEntity te = world.getBlockEntity(pos);
		return false;
	}

	public static void onGuiCreate(Screen screen, List<GuiEventListener> widgets, Consumer<GuiEventListener> widgetAdder)
	{
	}

	public static void onDrawTooltip(ItemStack stack, List<Component> out)
	{
	}

	public static void onRenderHotBar()
	{
	}

	public static SoundInstance onSoundPlayed(SoundInstance sound)
	{
		return SoundManagerClient.onSoundPlayed(sound);
	}

	public static void onDisconnect()
	{
		RpcHandler.onDisconnect();
	}
}
