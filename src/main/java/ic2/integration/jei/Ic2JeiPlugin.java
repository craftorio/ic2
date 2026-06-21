package ic2.integration.jei;

import ic2.api.recipe.IRecipeInput;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.integration.jei.recipe.machine.CannerBottleCategory;
import ic2.integration.jei.recipe.machine.CannerBottleLiquidCategory;
import ic2.integration.jei.recipe.machine.CannerBottleLiquidRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerBottleRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerEmptyLiquidCategory;
import ic2.integration.jei.recipe.machine.CannerEmptyLiquidRecipeWrapper;
import ic2.integration.jei.recipe.machine.CannerEnrichCategory;
import ic2.integration.jei.recipe.machine.CannerEnrichRecipeWrapper;
import ic2.integration.jei.recipe.machine.DynamicCategory;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import ic2.integration.jei.recipe.machine.MetalFormerCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class Ic2JeiPlugin implements IModPlugin
{
	private final RecipeType<IORecipeWrapper> BLAST_FURNACE = RecipeType.create("ic2", "blast_furnace", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> BLOCK_CUTTER = RecipeType.create("ic2", "block_cutter", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> CENTRIFUGE = RecipeType.create("ic2", "centrifuge", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> COMPRESSOR = RecipeType.create("ic2", "compressor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> EXTRACTOR = RecipeType.create("ic2", "extractor", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> MACERATOR = RecipeType.create("ic2", "macerator", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_EXTRUDING = RecipeType.create("ic2", "metal_former_extruding", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_ROLLING = RecipeType.create("ic2", "metal_former_rolling", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> METAL_FORMER_CUTTING = RecipeType.create("ic2", "metal_former_cutting", IORecipeWrapper.class);
	private final RecipeType<IORecipeWrapper> ORE_WASHER = RecipeType.create("ic2", "ore_washer", IORecipeWrapper.class);
	private final RecipeType<CannerBottleRecipeWrapper> CANNER_BOTTLE = RecipeType.create("ic2", "canner_bottle", CannerBottleRecipeWrapper.class);
	private final RecipeType<CannerEnrichRecipeWrapper> CANNER_ENRICH = RecipeType.create("ic2", "canner_enrich", CannerEnrichRecipeWrapper.class);
	private final RecipeType<CannerBottleLiquidRecipeWrapper> CANNER_BOTTLE_LIQUID = RecipeType.create("ic2", "canner_bottle_liquid", CannerBottleLiquidRecipeWrapper.class);
	private final RecipeType<CannerEmptyLiquidRecipeWrapper> CANNER_EMPTY_LIQUID = RecipeType.create("ic2", "canner_empty_liquid", CannerEmptyLiquidRecipeWrapper.class);

	public @NotNull ResourceLocation getPluginUid()
	{
		return IC2.getIdentifier("plugin");
	}

	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLAST_FURNACE, this.BLAST_FURNACE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.BLOCK_CUTTER, this.BLOCK_CUTTER, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.CENTRIFUGE, this.CENTRIFUGE, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.COMPRESSOR, this.COMPRESSOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.EXTRACTOR, this.EXTRACTOR, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.MACERATOR, this.MACERATOR, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_EXTRUDING, 0, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_ROLLING, 1, guiHelper));
		registration.addRecipeCategories(new MetalFormerCategory(this.METAL_FORMER_CUTTING, 2, guiHelper));
		registration.addRecipeCategories(new DynamicCategory(Ic2Blocks.ORE_WASHING_PLANT, this.ORE_WASHER, guiHelper));
		registration.addRecipeCategories(new CannerBottleCategory((ic2.core.block.tileentity.Ic2TileEntityBlock) Ic2Blocks.SOLID_CANNER, this.CANNER_BOTTLE, guiHelper));
		registration.addRecipeCategories(new CannerEnrichCategory(this.CANNER_ENRICH, guiHelper));
		registration.addRecipeCategories(new CannerBottleLiquidCategory(this.CANNER_BOTTLE_LIQUID, guiHelper));
		registration.addRecipeCategories(new CannerEmptyLiquidCategory(this.CANNER_EMPTY_LIQUID));
	}

	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLAST_FURNACE), this.BLAST_FURNACE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLOCK_CUTTER), this.BLOCK_CUTTER);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CENTRIFUGE), this.CENTRIFUGE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.COMPRESSOR), this.COMPRESSOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.EXTRACTOR), this.EXTRACTOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.MACERATOR), this.MACERATOR);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.METAL_FORMER), this.METAL_FORMER_CUTTING, this.METAL_FORMER_EXTRUDING, this.METAL_FORMER_ROLLING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ORE_WASHING_PLANT), this.ORE_WASHER);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.SOLID_CANNER), this.CANNER_BOTTLE);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CANNER), this.CANNER_BOTTLE, this.CANNER_ENRICH, this.CANNER_BOTTLE_LIQUID, this.CANNER_EMPTY_LIQUID);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.INDUSTRIAL_WORKBENCH), RecipeTypes.CRAFTING);
		registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BATCH_CRAFTER), RecipeTypes.CRAFTING);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{
		registration.addRecipeTransferHandler(new CraftingTransferInfo<>(ContainerIndustrialWorkbench.class, Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH, 36, 37, 45));
		registration.addRecipeTransferHandler(new CraftingTransferInfo<>(ContainerBatchCrafter.class, Ic2ScreenHandlers.BATCH_CRAFTER, 46, 37, 45));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_EXTRUDING, List.of(37), List.of(38), 0, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_ROLLING, List.of(37), List.of(38), 0, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerMetalFormer.class, Ic2ScreenHandlers.METAL_FORMER, this.METAL_FORMER_CUTTING, List.of(37), List.of(38), 0, 36));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.MACERATOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.COMPRESSOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.EXTRACTOR, List.of(0), List.of(1), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.CENTRIFUGE, List.of(0), List.of(2, 3, 4), 9, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.BLOCK_CUTTER, List.of(0), List.of(1), 8, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.BLAST_FURNACE, List.of(0), List.of(1, 2), 7, 36));
		registration.addRecipeTransferHandler(new IOTransferInfo<>(DynamicContainer.class, Ic2ScreenHandlers.DYNAMIC_BE, this.ORE_WASHER, List.of(0), List.of(1, 2, 3), 11, 36));

		registration.addRecipeTransferHandler(new IOTransferInfo<>(ContainerCanner.class, Ic2ScreenHandlers.CANNER, this.CANNER_BOTTLE, List.of(37, 43), List.of(38), 0, 36));

		IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();
		registration.addRecipeTransferHandler(new EnrichLiquidTransferHandler(this.CANNER_ENRICH, transferHelper), this.CANNER_ENRICH);
		registration.addRecipeTransferHandler(new BottleLiquidTransferHandler(this.CANNER_BOTTLE_LIQUID, transferHelper), this.CANNER_BOTTLE_LIQUID);
		registration.addRecipeTransferHandler(new EmptyLiquidTransferHandler(this.CANNER_EMPTY_LIQUID, transferHelper), this.CANNER_EMPTY_LIQUID);
	}

	public void registerRecipes(@NotNull IRecipeRegistration registration)
	{
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		BiConsumer<RecipeType<IORecipeWrapper>, net.minecraft.world.item.crafting.RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>>> registerBasic = (id, type) ->
		{
			List<IORecipeWrapper> recipeList = recipeManager.getAllRecipesFor(type).stream().map(r -> new IORecipeWrapper(r.recipe())).toList();
			registration.addRecipes(id, recipeList);
		};
		registerBasic.accept(this.BLAST_FURNACE, Ic2RecipeTypes.BLAST_FURNACE);
		registerBasic.accept(this.BLOCK_CUTTER, Ic2RecipeTypes.BLOCK_CUTTER);
		registerBasic.accept(this.CENTRIFUGE, Ic2RecipeTypes.CENTRIFUGE);
		registerBasic.accept(this.COMPRESSOR, Ic2RecipeTypes.COMPRESSOR);
		registerBasic.accept(this.EXTRACTOR, Ic2RecipeTypes.EXTRACTOR);
		registerBasic.accept(this.MACERATOR, Ic2RecipeTypes.MACERATOR);
		registerBasic.accept(this.METAL_FORMER_CUTTING, Ic2RecipeTypes.METAL_FORMER_CUTTING);
		registerBasic.accept(this.METAL_FORMER_EXTRUDING, Ic2RecipeTypes.METAL_FORMER_EXTRUDING);
		registerBasic.accept(this.METAL_FORMER_ROLLING, Ic2RecipeTypes.METAL_FORMER_ROLLING);
		registerBasic.accept(this.ORE_WASHER, Ic2RecipeTypes.ORE_WASHER);

		List<CannerBottleRecipeWrapper> cannerBottleRecipes = recipeManager.getAllRecipesFor(Ic2RecipeTypes.CANNER_BOTTLE).stream().map(r -> new CannerBottleRecipeWrapper(r.recipe())).toList();
		registration.addRecipes(this.CANNER_BOTTLE, cannerBottleRecipes);

		List<CannerEnrichRecipeWrapper> cannerEnrichRecipes = recipeManager.getAllRecipesFor(Ic2RecipeTypes.CANNER_ENRICH).stream().map(r -> new CannerEnrichRecipeWrapper(r.recipe())).toList();
		registration.addRecipes(this.CANNER_ENRICH, cannerEnrichRecipes);

		List<CannerEmptyLiquidRecipeWrapper> emptyLiquidRecipes = new ArrayList<>();
		List<CannerBottleLiquidRecipeWrapper> bottleLiquidRecipes = new ArrayList<>();
		Map<String, CannerBottleLiquidRecipeWrapper> bottleLiquidDedup = new HashMap<>();
		generateCannerFluidContainerRecipes(emptyLiquidRecipes, bottleLiquidRecipes, bottleLiquidDedup);
		registration.addRecipes(this.CANNER_EMPTY_LIQUID, emptyLiquidRecipes);
		registration.addRecipes(this.CANNER_BOTTLE_LIQUID, bottleLiquidRecipes);
	}

	private static void generateCannerFluidContainerRecipes(List<CannerEmptyLiquidRecipeWrapper> emptyLiquidRecipes, List<CannerBottleLiquidRecipeWrapper> bottleLiquidRecipes, Map<String, CannerBottleLiquidRecipeWrapper> bottleLiquidDedup)
	{
		for (Item item : ForgeRegistries.ITEMS)
		{
			ItemStack stack = new ItemStack(item);
			if (!LiquidUtil.isDrainableFluidContainer(stack))
			{
				continue;
			}

			ItemStack filledStack = stack.copy();
			LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(filledStack, null, Integer.MAX_VALUE, FluidContainerOutputMode.EmptyFullToOutput);
			if (result == null)
			{
				continue;
			}

			List<ItemStack> drainedContainers = new ArrayList<>();
			if (!StackUtil.isEmpty(result.inPlaceOutput))
			{
				drainedContainers.add(result.inPlaceOutput.copy());
			}
			if (result.extraOutput != null && !StackUtil.isEmpty(result.extraOutput))
			{
				drainedContainers.add(result.extraOutput.copy());
			}

			Ic2FluidStack fluid = result.fluidChange;
			if (fluid == null || fluid.isEmpty())
			{
				continue;
			}

			emptyLiquidRecipes.add(new CannerEmptyLiquidRecipeWrapper(stack.copy(), drainedContainers, fluid.copy()));

			for (ItemStack drainedContainer : drainedContainers)
			{
				String dedupKey = ForgeRegistries.ITEMS.getKey(drainedContainer.getItem()) + "|" + ForgeRegistries.FLUIDS.getKey(fluid.getFluid());
				if (!bottleLiquidDedup.containsKey(dedupKey))
				{
					List<ItemStack> emptyInputs = List.of(drainedContainer.copy());
					CannerBottleLiquidRecipeWrapper wrapper = new CannerBottleLiquidRecipeWrapper(emptyInputs, fluid.copy(), stack.copy());
					bottleLiquidRecipes.add(wrapper);
					bottleLiquidDedup.put(dedupKey, wrapper);
				}
			}
		}
	}

	private static class CraftingTransferInfo<C extends AbstractContainerMenu> implements IRecipeTransferInfo<C, CraftingRecipe>
	{
		private final Class<C> containerClass;
		private final MenuType<C> menuType;
		private final int outputSlot;
		private final int inputStart;
		private final int inputEnd;

		CraftingTransferInfo(Class<C> containerClass, MenuType<C> menuType, int outputSlot, int inputStart, int inputEnd)
		{
			this.containerClass = containerClass;
			this.menuType = menuType;
			this.outputSlot = outputSlot;
			this.inputStart = inputStart;
			this.inputEnd = inputEnd;
		}

		@Override
		public Class<? extends C> getContainerClass()
		{
			return this.containerClass;
		}

		@Override
		public Optional<MenuType<C>> getMenuType()
		{
			return Optional.of(this.menuType);
		}

		@Override
		public RecipeType<CraftingRecipe> getRecipeType()
		{
			return RecipeTypes.CRAFTING;
		}

		@Override
		public boolean canHandle(C container, CraftingRecipe recipe)
		{
			return true;
		}

		@Override
		public List<Slot> getRecipeSlots(C container, CraftingRecipe recipe)
		{
			List<Slot> slots = new ArrayList<>(10);
			slots.add(container.getSlot(this.outputSlot));
			for (int i = this.inputStart; i <= this.inputEnd; i++)
			{
				slots.add(container.getSlot(i));
			}
			return slots;
		}

		@Override
		public List<Slot> getInventorySlots(C container, CraftingRecipe recipe)
		{
			List<Slot> slots = new ArrayList<>(36);
			for (int i = 0; i < 36; i++)
			{
				slots.add(container.getSlot(i));
			}
			return slots;
		}
	}

	private static class IOTransferInfo<R> implements IRecipeTransferInfo<AbstractContainerMenu, R>
	{
		private final Class<? extends AbstractContainerMenu> containerClass;
		private final MenuType<?> menuType;
		private final RecipeType<R> recipeType;
		private final List<Integer> inputSlots;
		private final List<Integer> outputSlots;
		private final int inventoryStart;
		private final int inventoryCount;

		IOTransferInfo(Class<? extends AbstractContainerMenu> containerClass, MenuType<?> menuType, RecipeType<R> recipeType, List<Integer> inputSlots, List<Integer> outputSlots, int inventoryStart, int inventoryCount)
		{
			this.containerClass = containerClass;
			this.menuType = menuType;
			this.recipeType = recipeType;
			this.inputSlots = inputSlots;
			this.outputSlots = outputSlots;
			this.inventoryStart = inventoryStart;
			this.inventoryCount = inventoryCount;
		}

		@Override
		public Class<? extends AbstractContainerMenu> getContainerClass()
		{
			return this.containerClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Optional<MenuType<AbstractContainerMenu>> getMenuType()
		{
			return Optional.of((MenuType<AbstractContainerMenu>) this.menuType);
		}

		@Override
		public RecipeType<R> getRecipeType()
		{
			return this.recipeType;
		}

		@Override
		public boolean canHandle(AbstractContainerMenu container, R recipe)
		{
			return true;
		}

		@Override
		public List<Slot> getRecipeSlots(AbstractContainerMenu container, R recipe)
		{
			List<Slot> slots = new ArrayList<>(this.inputSlots.size() + this.outputSlots.size());
			for (int idx : this.inputSlots)
			{
				slots.add(container.getSlot(idx));
			}
			for (int idx : this.outputSlots)
			{
				slots.add(container.getSlot(idx));
			}
			return slots;
		}

		@Override
		public List<Slot> getInventorySlots(AbstractContainerMenu container, R recipe)
		{
			List<Slot> slots = new ArrayList<>(this.inventoryCount);
			for (int i = this.inventoryStart; i < this.inventoryStart + this.inventoryCount; i++)
			{
				slots.add(container.getSlot(i));
			}
			return slots;
		}
	}

	private abstract static class CannerTransferHandler<R> implements IRecipeTransferHandler<AbstractContainerMenu, R>
	{
		protected final RecipeType<R> recipeType;
		protected final IRecipeTransferHandlerHelper transferHelper;

		CannerTransferHandler(RecipeType<R> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			this.recipeType = recipeType;
			this.transferHelper = transferHelper;
		}

		@Override
		public Class<? extends AbstractContainerMenu> getContainerClass()
		{
			return ContainerCanner.class;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Optional<MenuType<AbstractContainerMenu>> getMenuType()
		{
			return Optional.of((MenuType<AbstractContainerMenu>) (MenuType<?>) Ic2ScreenHandlers.CANNER);
		}

		@Override
		public RecipeType<R> getRecipeType()
		{
			return this.recipeType;
		}

		@SuppressWarnings("resource")
		protected TileEntityCanner getCanner(AbstractContainerMenu container)
		{
			return (TileEntityCanner) ((ic2.core.ContainerBase<?>) container).base;
		}

		protected boolean transferItemToSlot(Player player, AbstractContainerMenu container, IRecipeSlotView slotView, int targetSlotIndex, boolean doTransfer)
		{
			Slot targetSlot = container.getSlot(targetSlotIndex);
			if (!targetSlot.getItem().isEmpty())
			{
				return true; // slot already has an item, consider it OK
			}

			List<ItemStack> possibleStacks = slotView.getItemStacks().toList();

			// Find matching item in player inventory
			for (int invIdx = 0; invIdx < 36; invIdx++)
			{
				ItemStack invStack = container.getSlot(invIdx).getItem();
				for (ItemStack needed : possibleStacks)
				{
					if (ItemStack.isSameItemSameTags(invStack, needed))
					{
						if (doTransfer)
						{
							ItemStack toMove = invStack.copyWithCount(1);
							container.getSlot(invIdx).remove(1);
							targetSlot.set(toMove);
						}
						return true;
					}
				}
			}
			return false;
		}
	}

	private static final class EnrichLiquidTransferHandler extends CannerTransferHandler<CannerEnrichRecipeWrapper>
	{
		EnrichLiquidTransferHandler(RecipeType<CannerEnrichRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(AbstractContainerMenu container, CannerEnrichRecipeWrapper recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			TileEntityCanner canner = this.getCanner(container);
			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: fluid input — validate tank has matching fluid
			IRecipeSlotView fluidInView = slotViews.get(0);
			FluidStack neededFluid = fluidInView.getDisplayedIngredient(ForgeTypes.FLUID_STACK).orElse(null);
			if (neededFluid != null && !canner.inputTank.isEmpty())
			{
				Ic2FluidStack tankFluid = canner.inputTank.getFluidStack();
				if (tankFluid != null && tankFluid.getFluid() != neededFluid.getFluid())
				{
					return this.transferHelper.createUserErrorWithTooltip(
						Component.translatable("ic2.jei.transfer.error.fluid_mismatch", neededFluid.getDisplayName())
					);
				}
			}

			// Slot 1: additive item → inputSlot (37)
			if (!this.transferItemToSlot(player, container, slotViews.get(1), 37, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_additive"), List.of(slotViews.get(1))
				);
			}

			return null;
		}
	}

	private static final class BottleLiquidTransferHandler extends CannerTransferHandler<CannerBottleLiquidRecipeWrapper>
	{
		BottleLiquidTransferHandler(RecipeType<CannerBottleLiquidRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(AbstractContainerMenu container, CannerBottleLiquidRecipeWrapper recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: fluid input — handled by machine's inputTank
			// Slot 1: empty container → canInputSlot (43)
			if (!this.transferItemToSlot(player, container, slotViews.get(1), 43, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_container"), List.of(slotViews.get(1))
				);
			}

			return null;
		}
	}

	private static final class EmptyLiquidTransferHandler extends CannerTransferHandler<CannerEmptyLiquidRecipeWrapper>
	{
		EmptyLiquidTransferHandler(RecipeType<CannerEmptyLiquidRecipeWrapper> recipeType, IRecipeTransferHandlerHelper transferHelper)
		{
			super(recipeType, transferHelper);
		}

		@Override
		public IRecipeTransferError transferRecipe(AbstractContainerMenu container, CannerEmptyLiquidRecipeWrapper recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer)
		{
			if (!(container instanceof ContainerCanner))
			{
				return this.transferHelper.createInternalError();
			}

			List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews();

			// Slot 0: filled container → canInputSlot (43)
			if (!this.transferItemToSlot(player, container, slotViews.get(0), 43, doTransfer))
			{
				return this.transferHelper.createUserErrorForMissingSlots(
					Component.translatable("ic2.jei.transfer.error.missing_filled_container"), List.of(slotViews.get(0))
				);
			}

			return null;
		}
	}
}
