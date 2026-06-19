package ic2.forge;

import ic2.core.event.EventHandlerClient;
import ic2.forge.model.BeModelLoader;
import ic2.forge.model.CableModelLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientModEventHandlerForge
{
	@SubscribeEvent
	public void onRegisterBlockColorProviders(RegisterColorHandlersEvent.Block event)
	{
		for (ClientEnvProxyForge.BlockColorProviderRegistration reg : ClientEnvProxyForge.blockColorProviderRegistrations)
		{
			event.getBlockColors().register(reg.provider(), reg.blocks());
		}
	}

	@SubscribeEvent
	public void onRegisterItemColorProviders(RegisterColorHandlersEvent.Item event)
	{
		for (ClientEnvProxyForge.ItemColorProviderRegistration reg : ClientEnvProxyForge.itemColorProviderRegistrations)
		{
			event.getItemColors().register(reg.provider(), reg.items());
		}
	}

	@SubscribeEvent
	public void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		for (ClientEnvProxyForge.BerRegistration<?> reg : ClientEnvProxyForge.berRegistrations)
		{
			registerBer(reg, event);
		}

		for (ClientEnvProxyForge.EntityRendererRegistration<?> reg : ClientEnvProxyForge.entityRendererRegistrations)
		{
			registerEntityRenderer(reg, event);
		}

		for (ClientEnvProxyForge.BlockEntityRendererRegistration<?> reg : ClientEnvProxyForge.blockEntityRendererRegistrations)
		{
			registerBlockEntityRenderer(reg, event);
		}
	}

	private static <T extends BlockEntity> void registerBer(ClientEnvProxyForge.BerRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(reg.blockEntityType(), reg.blockEntityRendererProvider());
	}

	private static <T extends Entity> void registerEntityRenderer(
		ClientEnvProxyForge.EntityRendererRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event
	)
	{
		event.registerEntityRenderer(reg.type(), reg.factory());
	}

	private static <T extends BlockEntity> void registerBlockEntityRenderer(
		ClientEnvProxyForge.BlockEntityRendererRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event
	)
	{
		event.registerBlockEntityRenderer(reg.type(), reg.factory());
	}

	@SubscribeEvent
	public void onModelRegistry(ModelEvent.RegisterGeometryLoaders event)
	{
		event.register("be", new BeModelLoader());
		event.register("cable", new CableModelLoader());
	}

	@SubscribeEvent
	public void onRegisterKeybindings(RegisterKeyMappingsEvent event)
	{
		for (KeyMapping keybinding : ClientEnvProxyForge.keyBindingRegistrations)
		{
			event.register(keybinding);
		}
	}

	// TODO
	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event)
	{
		for (ClientEnvProxyForge.BlockLayerRegistration reg : ClientEnvProxyForge.blockLayerRegistrations)
		{
			for (var block : reg.blocks())
			{
				ItemBlockRenderTypes.setRenderLayer(block, reg.layer());
			}
		}

		EventHandlerClient.onClientSetup();
	}
}
