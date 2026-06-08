package ic2.core.event;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.item.ElectricItem;
import net.minecraft.core.registries.BuiltInRegistries;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.fluid.FluidHandler;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
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

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;

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
					IC2.getIdentifier(direction.getName()),
					(stack, world, entity, seed) -> stack.getItem() instanceof ItemUpgradeModule && ItemUpgradeModule.getDirection(stack) == direction
						? 1.0F
						: 0.0F
				);
		}

		SideProxyClient.envProxy
			.registerModelPredicateProvider(
				IC2.getIdentifier("nano_saber_active"),
				(stack, world, entity, seed) -> stack.getItem() instanceof AbstractItemNanoSaber nanoSaber ? nanoSaber.getActiveData(stack, world) : 0.0F
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
						boolean open = player.containerMenu instanceof ContainerToolbox && (
							StackUtil.checkItemEquality(mainHandItem = player.getMainHandItem(), stack) || StackUtil.checkItemEquality(player.getOffhandItem(), stack) && !(mainHandItem.getItem() instanceof IHandHeldInventory)
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
		if (fluid != null && "ic2".equals(ForgeRegistries.FLUIDS.getKey(fluid).getNamespace()))
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
		return fluid != null && "ic2".equals(ForgeRegistries.FLUIDS.getKey(fluid).getNamespace()) ? FluidHandler.getColor(fluid) : -1;
	}

	// TODO
	/*
	意图分析：

  onDrawBlockHighlight 是绘制玩家准星对准方块时的高亮边框的方法。空 if 的逻辑本应是：

  1. 检查玩家手中物品是否实现了 IEnhancedOverlayProvider（如 ItemToolCutter 线缆剪、ItemToolCrowbar 撬棍）                                                                                                            
  2. 调用 providesEnhancedOverlay(world, pos, side, player, stack) 判断该物品是否需要为当前目标方块绘制自定义高亮覆盖层                                                                    
  3. 如果返回 true，则渲染一个增强的方块选择指示器（比如高亮线缆的连接点、可拆卸的面板方向等）

  但目前这个 if 体是空的 —— 作者只写了前半段判断逻辑，后半段渲染代码还没来得及写，是个 WIP（Work in progress）。

  从结构上看，它和下面的 onDrawBlockHighlightLast 是一对方法对 —— 前者负责覆盖/替换默认高亮，后者负责在默认高亮之后追加绘制。onDrawBlockHighlightLast 也是半成品（取了 BlockEntity 但什么都没做就 return false 了）。

  简单说：想给特殊工具（线缆剪、撬棍等）在指向方块时加自定义准星高亮效果，但还没写完。
	 */
	public static void onDrawBlockHighlight(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		assert target.getType() == Type.BLOCK;
		ItemStack inHand = StackUtil.get(player, InteractionHand.MAIN_HAND);
		if (inHand.getItem() instanceof IEnhancedOverlayProvider)
		{
		}
	}

	public static boolean onDrawBlockHighlightLast(Player player, BlockHitResult target, float partialTicks, PoseStack matrix, MultiBufferSource buffers)
	{
		assert target.getType() == Type.BLOCK;
		Level world = player.getCommandSenderWorld();
		BlockPos pos = target.getBlockPos();
		if (!world.getWorldBorder().isWithinBounds(pos))
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
		if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Ic2TileEntityBlock block)
		{
			Ic2TileEntity dummyTe = block.getDummyTe();
			if (dummyTe.hasComponent(Energy.class))
			{
				Energy energy = dummyTe.getComponent(Energy.class);
				if (!energy.getSourceDirs().isEmpty())
				{
					out.add(Component.translatable("ic2.item.tooltip.PowerTier", energy.getSourceTier()).withStyle(ChatFormatting.GRAY));
				}
				else if (!energy.getSinkDirs().isEmpty())
				{
					out.add(Component.translatable("ic2.item.tooltip.PowerTier", energy.getSinkTier()).withStyle(ChatFormatting.GRAY));
				}
			}
		}
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
