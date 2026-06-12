package ic2.core.event;

import ic2.api.block.BreakableBlock;
import ic2.api.energy.EnergyNet;
import ic2.api.item.BlockBreakableItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEntityAttackableItem;
import ic2.api.recipe.Recipes;
import ic2.api.sound.item.ISwingSoundItem;
import ic2.core.ChunkLoaderLogic;
import ic2.core.IC2;
import ic2.core.apihelper.CoreAccessImpl;
import ic2.core.block.ChunkLoadAwareBlockHandler;
import ic2.core.block.comp.Components;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.crop.Ic2Crops;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.init.BlocksItems;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.recipe.input.RecipeInputFactory;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2EntityTags;
import ic2.core.ref.Ic2GameEvents;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuIndex;
import ic2.core.world.Ic2WorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class EventHandler
{
	public static void onInitGameEvents()
	{
		Ic2SoundEvents.init();
		Ic2GameEvents.init();
	}

	public static void onInitEarly()
	{
		long startTime = System.nanoTime();
		IC2.log.debug(LogCategory.General, "Starting pre-init.");
		MainConfig.load();
		CoreAccessImpl.init();
		Recipes.inputFactory = new RecipeInputFactory();
		EnergyNet.instance = EnergyNetGlobal.create();
		ElectricItem.manager = new GatewayElectricItemManager();
		ElectricItem.rawManager = new ElectricItemManager();
		Components.init();
		BlocksItems.init();
		Ic2BlockTags.init();
		Ic2ItemTags.init();
		Ic2EntityTags.init();
		Ic2BoatTypes.init();
		Ic2Entities.init();
		Ic2RecipeTypes.init();
		Ic2RecipeSerializers.init();
		Ic2WorldGen.init();
		TileEntityRecycler.init();
		TileEntityMatter.init();
		TileEntitySemifluidGenerator.init();
		TileEntityFluidHeatGenerator.init();
		TileEntityLiquidHeatExchanger.init();
		TileEntityFermenter.init();
		TileEntityElectrolyzer.init();
		Rezepte.registerRecipes();
		Ic2Crops.init();
		IC2.sideProxy.preInit();
		IC2.initialized = true;
		IC2.log.debug(LogCategory.General, "Finished pre-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
	}

	public static void onInit()
	{
	}

	public static void onInitLate()
	{
		long startTime = System.nanoTime();
		UuIndex.instance.init();
		UuIndex.instance.refresh(true);
		IC2.sideProxy.onPostInit();
		IC2.sideProxy.requestTick(!IC2.envProxy.isClientEnv(), ChunkLoadAwareBlockHandler::init);
		IC2.log.debug(LogCategory.General, "Finished post-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
	}

	private static boolean loadSubModule(String name)
	{
		IC2.log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", "ic2", name);

		try
		{
			Class<?> subModuleClass = IC2.class.getClassLoader().loadClass("ic2." + name + ".SubModule");
			return (Boolean) subModuleClass.getMethod("init").invoke(null);
		} catch (Throwable t)
		{
			IC2.log.debug(LogCategory.SubModule, "Submodule %s not loaded.", name);
			return false;
		}
	}

	public static void onServerStart(MinecraftServer server)
	{
		IC2.sideProxy.onServerAvailable(server);
	}

	public static void onPlayerLogout(Player player)
	{
		if (IC2.sideProxy.isSimulating())
		{
			IC2.sideProxy.getKeyboard().removePlayerReferences(player);
		}
	}

	public static void onWorldLoad(Level world)
	{
		if (!world.isClientSide)
		{
			ServerLevel serverWorld = (ServerLevel) world;
			ChunkLoaderLogic.onWorldLoad(serverWorld);
		}
	}

	public static void onWorldUnload(Level world)
	{
		WorldData.onWorldUnload(world);
	}

	public static void onChunkDataLoad(LevelChunk chunk, CompoundTag data)
	{
	}

	public static void onChunkSave(LevelChunk chunk, CompoundTag data)
	{
	}

	public static void onChunkLoad(LevelChunk chunk)
	{
		ChunkLoadAwareBlockHandler.onChunkLoad(chunk);
	}

	public static void onChunkUnload(LevelChunk chunk)
	{
		ChunkLoadAwareBlockHandler.onChunkUnload(chunk);
		if (!chunk.getLevel().isClientSide)
		{
			ChunkLoaderLogic.onChunkUnload(chunk);
		}
	}

	public static InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		if (player.getItemInHand(hand).getItem() instanceof BlockBreakableItem blockBreakableItem)
		{
			InteractionResult actionResult = blockBreakableItem.onBlockStartBreak(player, world, hand, pos, direction);
			if (actionResult != InteractionResult.PASS)
			{
				return actionResult;
			}
		}

		BlockState blockState = world.getBlockState(pos);
		return blockState.getBlock() instanceof BreakableBlock breakableBlock
			? breakableBlock.startBreak(player, world, hand, pos, blockState, direction)
			: InteractionResult.PASS;
	}

	public static boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		Item item = player.getMainHandItem().getItem();
		return !(item instanceof BlockBreakableItem) || ((BlockBreakableItem) item).beforeBlockBreak(world, player, pos, state, blockEntity);
	}

	public static void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		Item item = player.getMainHandItem().getItem();
		if (item instanceof BlockBreakableItem)
		{
			((BlockBreakableItem) item).afterBlockBreak(world, player, pos, state, blockEntity);
		}
	}

	public static void onPlayerTick(Player player)
	{
		JetpackHandler.onPlayerTick(player);
	}

	public static void onLivingSpecialSpawn(LivingEntity rawEntity)
	{
		if (IC2.seasonal && (rawEntity instanceof Zombie || rawEntity instanceof Skeleton) && rawEntity.getCommandSenderWorld().random.nextFloat() < 0.1F)
		{
			Mob entity = (Mob) rawEntity;

			for (EquipmentSlot slot : EquipmentSlot.values())
			{
				entity.setDropChance(slot, Float.NEGATIVE_INFINITY);
			}
		}
	}

	public static boolean onLivingFall(LivingEntity entity, float distance)
	{
		if (entity.getCommandSenderWorld().isClientSide)
		{
			return false;
		} else
		{
			ItemStack armor = entity.getItemBySlot(EquipmentSlot.FEET);
			if (StackUtil.isEmpty(armor))
			{
				return false;
			} else
			{
				Item armorItem = armor.getItem();
				if (armorItem == Ic2Items.RUBBER_BOOTS)
				{
					return ((ItemArmorHazmat) armorItem).absorbFall(armor, entity, distance);
				} else if (armorItem == Ic2Items.NANO_BOOTS)
				{
					return ((ItemArmorNanoSuit) armorItem).absorbFall(armor, entity, distance);
				} else
				{
					return armorItem == Ic2Items.QUANTUM_BOOTS && ((ItemArmorQuantumSuit) armorItem).absorbFall(armor, entity, distance);
				}
			}
		}
	}

	public static boolean onEntitySwingHand(LivingEntity entity, InteractionHand hand)
	{
		ItemStack stack = entity.getItemInHand(hand);
		if (stack.getItem() instanceof ISwingSoundItem swingSoundItem)
		{
			SoundEvent swingSound = swingSoundItem.getSwingSound(entity, hand);
			if (swingSound != null)
			{
				entity.playSound(swingSound, 1.0F, 1.0F);
			}

		}
		return false;
	}

	public static boolean onEntityInteract(Player player, InteractionHand hand, Entity target)
	{
		if (player.getCommandSenderWorld().isClientSide)
		{
			return false;
		}

		ItemStack stack = StackUtil.get(player, hand);
		StackUtil.isEmpty(stack);
		return false;
	}

	public static boolean onAttackEntity(Player player, Entity target)
	{
		Item item = player.getMainHandItem().getItem();
		return !(item instanceof IEntityAttackableItem) || ((IEntityAttackableItem) item).onAttackEntity(player, target);
	}

	// True means cancelling this damage event
	public static boolean onEntityAttacked(LivingEntity victim, DamageSource source, float amount)
	{
		if (victim instanceof Player player)
		{
			return ItemArmorElectric.damageArmor(player, source, amount);
		}
		return false;
	}
}
