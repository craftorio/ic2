package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.GuiElement;
import ic2.core.ref.TeBlock;
import ic2.core.util.Tuple;
import ic2.jeiIntegration.SlotPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class CannerCategory<T> extends IORecipeCategory<T> implements IDrawable
{
	protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
	protected final List<Tuple.T2<IDrawable, SlotPosition>> progress = new ArrayList<>();
	private final List<SlotPosition> inputs;
	private final List<SlotPosition> outputs;
	private final CannerCategory.CanningActivity.Tank[] tanks;
	private final Set<CannerCategory.CanningActivity.Tank> notTanks;
	private final String name;
	private final IDrawable emptyTank;
	private final IDrawable tankBackground;
	private final IDrawable tankOverlay;

	public static CannerCategory<ICannerEnrichRecipeManager> enriching(IGuiHelper guiHelper)
	{
		return new CannerCategory<>(CannerCategory.CanningActivity.ENRICHING, guiHelper);
	}

	public static CannerCategory<ICannerBottleRecipeManager> bottling(IGuiHelper guiHelper)
	{
		return new CannerCategory<>(CannerCategory.CanningActivity.CANNING, guiHelper);
	}

	protected CannerCategory(CannerCategory.CanningActivity activity, IGuiHelper guiHelper)
	{
		super(TeBlock.canner, (T) activity.manager);
		activity.createBackground(this.elements, guiHelper);
		this.elements.add(new Tuple.T2<>(guiHelper.createDrawable(GuiCanner.texture, 176, activity.overlayV, 50, 14), new SlotPosition(23, 65)));
		this.emptyTank = guiHelper.createDrawable(GuiElement.commonTexture, 70, 100, 20, 55);
		this.tankBackground = guiHelper.createDrawable(GuiElement.commonTexture, 6, 100, 20, 55);
		this.tankOverlay = guiHelper.createDrawable(GuiElement.commonTexture, 38, 100, 20, 55);
		this.name = activity.mode.name();
		this.progress
			.add(
				new Tuple.T2<>(
					guiHelper.createAnimatedDrawable(guiHelper.createDrawable(GuiCanner.texture, 233, 0, 23, 14), 66, StartDirection.LEFT, false),
					new SlotPosition(34, 6)
				)
			);
		List<SlotPosition> inputs = new ArrayList<>(2);
		List<SlotPosition> outputs = Collections.emptyList();

		for (CannerCategory.CanningActivity.Slot slot : activity.slots)
		{
			switch (slot)
			{
				case ADDITIVE:
					inputs.add(new SlotPosition(39, 27));
					break;
				case CAN:
					inputs.add(new SlotPosition(0, 0));
					break;
				case OUTPUT:
					outputs = Collections.singletonList(new SlotPosition(78, 0));
			}
		}

		this.inputs = inputs;
		this.outputs = outputs;
		this.tanks = activity.tanks;
		this.notTanks = this.tanks.length == 0
			? EnumSet.allOf(CannerCategory.CanningActivity.Tank.class)
			: EnumSet.complementOf(EnumSet.copyOf(Arrays.asList(this.tanks)));
	}

	@Override
	public String getUid()
	{
		return super.getUid() + '_' + this.name;
	}

	public IDrawable getBackground()
	{
		return this;
	}

	@Override
	protected List<SlotPosition> getInputSlotPos()
	{
		return this.inputs;
	}

	@Override
	protected List<SlotPosition> getOutputSlotPos()
	{
		return this.outputs;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		super.setRecipe(recipeLayout, recipeWrapper, ingredients);
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		int id = 0;

		for (CannerCategory.CanningActivity.Tank tank : this.tanks)
		{
			switch (tank)
			{
				case INPUT:
					fluidStacks.init(id, true, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
					List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
					if (!inputs.isEmpty())
					{
						fluidStacks.set(id, inputs.get(0));
					}
					break;
				case OUTPUT:
					fluidStacks.init(id, false, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
					List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);
					if (!outputs.isEmpty())
					{
						fluidStacks.set(id, outputs.get(0));
					}
			}

			id++;
		}
	}

	public void draw(@Nonnull Minecraft minecraft)
	{
		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			element.a.draw(minecraft, element.b.getX(), element.b.getY());
		}

		for (CannerCategory.CanningActivity.Tank tank : this.tanks)
		{
			this.tankBackground.draw(minecraft, tank.x, tank.y);
		}

		for (CannerCategory.CanningActivity.Tank tank : this.notTanks)
		{
			this.emptyTank.draw(minecraft, tank.x, tank.y);
		}
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		for (Tuple.T2<IDrawable, SlotPosition> bar : this.progress)
		{
			bar.a.draw(minecraft, bar.b.getX(), bar.b.getY());
		}
	}

	public void draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset)
	{
	}

	public int getWidth()
	{
		return 96;
	}

	public int getHeight()
	{
		return 81;
	}

	public enum CanningActivity
	{
		ENRICHING(
			Recipes.cannerEnrich,
			TileEntityCanner.Mode.EnrichLiquid,
			60,
			new CannerCategory.CanningActivity.Slot[] { CannerCategory.CanningActivity.Slot.ADDITIVE },
			CannerCategory.CanningActivity.Tank.values()
		),
		CANNING(Recipes.cannerBottle, TileEntityCanner.Mode.BottleSolid, 18, CannerCategory.CanningActivity.Slot.values())
			{
				@Override
				void createBackground(List<Tuple.T2<IDrawable, SlotPosition>> elements, IGuiHelper guiHelper)
				{
					super.createBackground(elements, guiHelper);
					elements.add(new Tuple.T2<>(guiHelper.createDrawable(GuiCanner.texture, 3, 4, 9, 18), new SlotPosition(19, 37)));
					elements.add(new Tuple.T2<>(guiHelper.createDrawable(GuiCanner.texture, 3, 4, 18, 23), new SlotPosition(59, 37)));
				}
			};

		final IMachineRecipeManager<?, ?, ?> manager;
		final TileEntityCanner.Mode mode;
		final int overlayV;
		final CannerCategory.CanningActivity.Slot[] slots;
		final CannerCategory.CanningActivity.Tank[] tanks;

		CanningActivity(
			IMachineRecipeManager<?, ?, ?> manager,
			TileEntityCanner.Mode mode,
			int overlayV,
			CannerCategory.CanningActivity.Slot[] slots,
			CannerCategory.CanningActivity.Tank... tanks
		)
		{
			this.manager = manager;
			this.mode = mode;
			this.overlayV = overlayV;
			this.slots = slots;
			this.tanks = tanks;
		}

		void createBackground(List<Tuple.T2<IDrawable, SlotPosition>> elements, IGuiHelper guiHelper)
		{
			elements.add(new Tuple.T2<>(guiHelper.createDrawable(GuiCanner.texture, 40, 16, 96, 81), new SlotPosition(0, 0)));
		}

		enum Slot
		{
			ADDITIVE,
			CAN,
			OUTPUT;
		}

		enum Tank
		{
			INPUT(39, 42),
			OUTPUT(117, 42);

			final int x;
			final int y;

			Tank(int x, int y)
			{
				this.x = -40 + x;
				this.y = -16 + y;
			}
		}
	}
}
