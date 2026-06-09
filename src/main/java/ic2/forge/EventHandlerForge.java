package ic2.forge;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.event.RetextureEvent;
import ic2.api.tile.RetexturableBlock;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.EventHandler;
import ic2.core.event.TickHandler;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public final class EventHandlerForge
{
	private static final ResourceLocation fluidCapId = IC2.getIdentifier("fluid");
	private static final ResourceLocation itemCapId = IC2.getIdentifier("item");

	@SubscribeEvent
	public void serverStart(ServerStartingEvent event)
	{
		EventHandler.onServerStart(event.getServer());
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
	{
		EventHandler.onPlayerLogout(event.getEntity());
	}

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event)
	{
		EventHandler.onWorldLoad((Level) event.getLevel());
	}

	@SubscribeEvent
	public void onWorldUnload(LevelEvent.Unload event)
	{
		EventHandler.onWorldUnload((Level) event.getLevel());
	}

	@SubscribeEvent
	public void onChunkDataLoad(ChunkDataEvent.Load event)
	{
		ChunkAccess chunk = event.getChunk();
		if (chunk instanceof LevelChunk)
		{
			EventHandler.onChunkDataLoad((LevelChunk) chunk, event.getData());
		}
	}

	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event)
	{
		ChunkAccess chunk = event.getChunk();
		if (chunk instanceof LevelChunk)
		{
			EventHandler.onChunkSave((LevelChunk) chunk, event.getData());
		}
	}

	public void onChunkLoad(ChunkEvent.Load event)
	{
		ChunkAccess chunk = event.getChunk();
		if (chunk instanceof LevelChunk)
		{
			EventHandler.onChunkLoad((LevelChunk) chunk);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChunkUnload(ChunkEvent.Unload event)
	{
		ChunkAccess chunk = event.getChunk();
		if (chunk instanceof LevelChunk)
		{
			EventHandler.onChunkUnload((LevelChunk) chunk);
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.LevelTickEvent event)
	{
		Level world = event.level;
		if (event.phase == TickEvent.Phase.START)
		{
			TickHandler.onWorldTickStart(world);
		} else
		{
			TickHandler.onWorldTickEnd(world);
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			TickHandler.onServerTick();
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			EventHandler.onPlayerTick(event.player);
		}
	}

	@SubscribeEvent
	public void onLivingSpecialSpawn(MobSpawnEvent.FinalizeSpawn event)
	{
		EventHandler.onLivingSpecialSpawn(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingFall(LivingFallEvent event)
	{
		if (EventHandler.onLivingFall(event.getEntity(), event.getDistance()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event)
	{
		if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		if (EventHandler.onEntitySwingHand(event.getEntity(), event.getHand()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		if (EventHandler.onEntityInteract(event.getEntity(), event.getHand(), event.getTarget()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onEntityAttacked(LivingAttackEvent event)
	{
		if (EventHandler.onEntityAttacked(event.getEntity(), event.getSource(), event.getAmount()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onAttackEntity(AttackEntityEvent event)
	{
		if (!EventHandler.onAttackEntity(event.getEntity(), event.getTarget()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onBlockStartBreak(PlayerInteractEvent.LeftClickBlock event)
	{
		if (EventHandler.onBlockStartBreak(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace()) == InteractionResult.FAIL)
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void beforeBlockBreak(BlockEvent.BreakEvent event)
	{
		Level world = (Level) event.getLevel();
		BlockPos pos = event.getPos();
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!EventHandler.beforeBlockBreak(world, event.getPlayer(), pos, event.getState(), blockEntity))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onGetBurnTime(FurnaceFuelBurnTimeEvent event)
	{
		Item item = event.getItemStack().getItem();
		if (EnvProxyForge.burnTimeRecord.containsKey(item))
		{
			event.setBurnTime(EnvProxyForge.burnTimeRecord.get(item));
		}
	}

	@SubscribeEvent
	public void onRetexture(RetextureEvent event)
	{
		Block block = event.state.getBlock();
		if (block instanceof RetexturableBlock
			&& ((RetexturableBlock) block)
			.retexture(
				event.state,
				(Level) event.getLevel(),
				event.pos,
				event.side,
				event.player,
				event.refState,
				event.refVariant,
				event.refSide,
				event.refColorMultipliers
			))
		{
			event.applied = true;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEnergyTileLoad(EnergyTileLoadEvent event)
	{
		if (event.getLevel().isClientSide())
		{
			IC2.log
				.warn(
					LogCategory.EnergyNet,
					"EnergyTileLoadEvent: posted for %s client-side, aborting",
					Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile))
				);
		} else
		{
			EnergyNet.instance.addTileUnchecked(event.tile);
		}
	}

	@SubscribeEvent
	public void onEnergyTileUnload(EnergyTileUnloadEvent event)
	{
		if (event.getLevel().isClientSide())
		{
			IC2.log
				.warn(
					LogCategory.EnergyNet,
					"EnergyTileUnloadEvent: posted for %s client-side, aborting",
					Util.toString(event.tile, event.getLevel(), EnergyNet.instance.getPos(event.tile))
				);
		} else
		{
			EnergyNet.instance.removeTile(event.tile);
		}
	}

	@SubscribeEvent
	public void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event)
	{
		final BlockEntity be = event.getObject();
		if (be instanceof Ic2TileEntity ic2te)
		{
			if (be instanceof FluidBeBridge bridge)
			{
				Ic2FluidBlock fb = bridge.getFluidBlock();
				if (fb != null && fb.isFluidBlock(null, null, null, be))
				{
					event.addCapability(fluidCapId, new BlockFluidCapImpl(fb, be));
				}
			} else if (ic2te.hasComponent(ic2.core.block.comp.Fluids.class))
			{
				ic2.core.block.comp.Fluids fluids = ic2te.getComponent(ic2.core.block.comp.Fluids.class);
				event.addCapability(fluidCapId, new BlockFluidCapImpl(fluids, be));
			}

			if (be instanceof WorldlyContainer)
			{
				event.addCapability(itemCapId, new ICapabilityProvider()
				{
					private final LazyOptional<IItemHandlerModifiable>[] caps = SidedInvWrapper.create((WorldlyContainer) be, Util.ALL_DIRS);

					@Override
					public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
					{
						return (LazyOptional<T>) (facing != null && capability == ForgeCapabilities.ITEM_HANDLER ? this.caps[facing.ordinal()] : LazyOptional.empty());
					}
				});
			} else if (be instanceof Container)
			{
				event.addCapability(itemCapId, new ICapabilityProvider()
				{
					private final LazyOptional<IItemHandler> cap = LazyOptional.of(() -> new InvWrapper((Container) be));

					@Override
					public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
					{
						return (LazyOptional<T>) (capability == ForgeCapabilities.ITEM_HANDLER ? this.cap : LazyOptional.empty());
					}
				});
			}
		}
	}

	@SubscribeEvent
	public void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event)
	{
		ItemStack stack = event.getObject();
		Item item = stack.getItem();
		if (item instanceof Ic2FluidItem)
		{
			event.addCapability(fluidCapId, new ItemFluidCapImpl(stack));
		}
	}
}
