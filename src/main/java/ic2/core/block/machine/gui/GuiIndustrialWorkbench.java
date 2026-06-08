package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.gui.Area;
import ic2.core.gui.CustomButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import ic2.core.gui.MouseButton;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import ic2.core.proxy.SideProxyClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GuiIndustrialWorkbench extends Ic2Gui<ContainerIndustrialWorkbench>
{
	public static Predicate<Screen> jeiScreenRecipesGuiCheck;
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiindustrialworkbench.png");

	public GuiIndustrialWorkbench(ContainerIndustrialWorkbench container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 194, 228);
		this.addElement((new Area(this, 173, 3, 18, 108)
		{
			@Override
			protected boolean suppressTooltip(int mouseX, int mouseY)
			{
				for (GuiElement<?> element : GuiIndustrialWorkbench.this.elements)
				{
					if (element.isEnabled() && element != this && element.contains(mouseX, mouseY))
					{
						return true;
					}
				}

				return false;
			}
		}).withTooltip("ic2.IndustrialWorkbench.gui.adjacent"));

		for (final Direction side : Util.ALL_DIRS)
		{
			this.addElement(
				new CustomButton(
					this,
					173,
					3 + (side.get3DDataValue() + 5) % 6 * 18,
					18,
					18,
					new IClickHandler()
					{
						private boolean firstOpen = true;
						private boolean jei = false;

						@Override
						public void onClick(MouseButton button)
						{
							TileEntityIndustrialWorkbench base = ((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).base;
							assert base.hasLevel();
							BlockEntity neighbour = base.getLevel().getBlockEntity(base.getBlockPos().relative(side));
							assert neighbour instanceof IHasGui;
							if (neighbour instanceof IPersonalBlock
								&& !((IPersonalBlock) neighbour)
								.permitsAccess(((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).player.getGameProfile()))
							{
								IC2.sideProxy
									.messagePlayer(
										((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).player,
										"Owned by " + ((IPersonalBlock) neighbour).getOwner().getName()
									);
							} else
							{
								GuiIndustrialWorkbench.closeHandler = this::onScreenClose;
								IC2.network.get(false).requestGUI((IHasGui) neighbour);
							}
						}

						private void onScreenClose()
						{
							if (!this.keepOpen(SideProxyClient.mc.screen))
							{
								if (!this.firstOpen)
								{
									GuiIndustrialWorkbench.closeHandler = null;
									IC2.network.get(false).requestGUI(((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).base);
								} else
								{
									this.firstOpen = false;
								}
							}
						}

						private boolean keepOpen(Screen screen)
						{
							if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck == null)
							{
								return false;
							} else if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck.test(screen))
							{
								this.jei = true;
								return true;
							} else if (this.jei)
							{
								this.jei = false;
								return true;
							} else
							{
								return false;
							}
						}
					}
				)
					.withEnableHandler(new IEnableHandler()
					{
						@Override
						public boolean isEnabled()
						{
							TileEntityIndustrialWorkbench base = ((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).base;
							return base.hasLevel() && base.getLevel().getBlockEntity(base.getBlockPos().relative(side)) instanceof IHasGui;
						}
					})
					.withIcon(new Supplier<ItemStack>()
					{
						public ItemStack get()
						{
							TileEntityIndustrialWorkbench base = ((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).base;
							assert base.hasLevel();
							BlockPos pos = base.getBlockPos().relative(side);
							BlockState state = base.getLevel().getBlockState(pos);
							return StackUtil.getPickStack(base.getLevel(), pos, state, ((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).player);
						}
					})
					.withTooltip(new Supplier<String>()
					{
						private String getSideName()
						{
							switch (side)
							{
								case WEST:
									return "ic2.dir.West";
								case EAST:
									return "ic2.dir.East";
								case DOWN:
									return "ic2.dir.Bottom";
								case UP:
									return "ic2.dir.Top";
								case NORTH:
									return "ic2.dir.North";
								case SOUTH:
									return "ic2.dir.South";
								default:
									throw new IllegalStateException("Unexpected direction: " + side);
							}
						}

						public String get()
						{
							TileEntityIndustrialWorkbench base = ((ContainerIndustrialWorkbench) GuiIndustrialWorkbench.this.menu).base;
							assert base.hasLevel();
							BlockEntity neighbour = base.getLevel().getBlockEntity(base.getBlockPos().relative(side));
							assert neighbour instanceof IHasGui;
							return IHasGui.getBeName(neighbour).getString() + "\n" + ChatFormatting.DARK_GRAY + Localization.translate(this.getSideName());
						}
					})
			);
		}

		int cancelX = 93;
		int cancelY = 42;
		this.addElement(new VanillaButton(this, 93, 42, 16, 16, new IClickHandler()
		{
			@Override
			public void onClick(MouseButton button)
			{
				IC2.network.get(false).sendContainerEvent((ContainerBase<?>) GuiIndustrialWorkbench.this.menu, "clear");
			}
		}).withTooltip("Clear"));
		this.addElement(Image.create(this, 94, 43, 14, 14, GuiElement.commonTexture, 256, 256, 210, 47, 224, 61));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}
