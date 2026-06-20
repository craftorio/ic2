package ic2.forge;

import com.mojang.blaze3d.shaders.FogShape;
import ic2.core.event.EventHandlerClient;
import ic2.core.event.TickHandler;
import ic2.core.proxy.SideProxyClient;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ClientEventHandlerForge
{
	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Pre event)
	{
		TickHandler.onClientTick();
	}

	@SubscribeEvent
	public void onSoundSetup(SoundEngineLoadEvent event)
	{
		EventHandlerClient.onSoundSetup();
	}

	@SubscribeEvent
	public void livingEntityPreRender(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event)
	{
		EventHandlerClient.livingEntityPreRender(event.getEntity(), event.getRenderer());
	}

	@SubscribeEvent
	public void livingEntityPostRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event)
	{
		EventHandlerClient.livingEntityPostRender(event.getEntity(), event.getRenderer());
	}

	@SubscribeEvent
	public void onSetupFogDensity(ViewportEvent.RenderFog event)
	{
		float newDensity = EventHandlerClient.onSetupFogDensity(event.getCamera().getBlockAtCamera());
		if (newDensity >= 0.0F)
		{
			event.setCanceled(true);
			event.setNearPlaneDistance(-8.0F);
			event.setFarPlaneDistance(newDensity * 0.5F);
			event.setFogShape(FogShape.SPHERE);
		}
	}

	@SubscribeEvent
	public void onRenderFogColor(ViewportEvent.ComputeFogColor event)
	{
		int color = EventHandlerClient.onRenderFogColor(event.getCamera().getBlockAtCamera());
		if (color >= 0)
		{
			event.setRed((color >>> 16 & 0xFF) / 255.0F);
			event.setGreen((color >>> 8 & 0xFF) / 255.0F);
			event.setBlue((color & 0xFF) / 255.0F);
		}
	}

	@SubscribeEvent
	public void onDrawBlockHighlight(RenderHighlightEvent.Block event)
	{
		EventHandlerClient.onDrawBlockHighlight(
			SideProxyClient.mc.player, event.getTarget(), event.getDeltaTracker().getGameTimeDeltaPartialTick(false), event.getPoseStack(), event.getMultiBufferSource()
		);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDrawBlockHighlightLast(RenderHighlightEvent.Block event)
	{
		if (EventHandlerClient.onDrawBlockHighlightLast(
			SideProxyClient.mc.player, event.getTarget(), event.getDeltaTracker().getGameTimeDeltaPartialTick(false), event.getPoseStack(), event.getMultiBufferSource()
		))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onGuiCreate(ScreenEvent.Init event)
	{
		EventHandlerClient.onGuiCreate(event.getScreen(), event.getListenersList(), event::addListener);
	}

	@SubscribeEvent
	public void onDrawTooltip(ItemTooltipEvent event)
	{
		EventHandlerClient.onDrawTooltip(event.getItemStack(), event.getToolTip());
	}

	@SubscribeEvent
	public void onRenderHotBar(RenderGuiLayerEvent.Post event)
	{
		if (event.getName().equals(VanillaGuiLayers.HOTBAR))
		{
			EventHandlerClient.onRenderHotBar();
		}
	}

	@SubscribeEvent
	public void onSoundPlayed(PlaySoundEvent event)
	{
		SoundInstance sound = event.getSound();
		SoundInstance newSound = EventHandlerClient.onSoundPlayed(sound);
		if (newSound != sound)
		{
			event.setSound(newSound);
		}
	}

	@SubscribeEvent
	public void onDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
	{
		EventHandlerClient.onDisconnect();
	}
}
