package ic2.core.block.machine.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.gui.*;
import ic2.core.init.Localization;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIndustrialWorkbench extends GuiIC2<ContainerIndustrialWorkbench>
{
	public static Predicate<GuiScreen> jeiScreenRecipesGuiCheck;

	public GuiIndustrialWorkbench(ContainerIndustrialWorkbench container)
	{
		super(container, 194, 228);
		addElement((new Area(this, 173, 3, 18, 108)
		{
			protected boolean suppressTooltip(int mouseX, int mouseY)
			{
				for (GuiElement<?> element : GuiIndustrialWorkbench.this.elements)
				{
					if (element.isEnabled() && element != this && element.contains(mouseX, mouseY))
						return true;
				}
				return false;
			}
		}).withTooltip("ic2.IndustrialWorkbench.gui.adjacent"));
		for (EnumFacing side : EnumFacing.VALUES)
		{
			addElement((new CustomButton(this, 173, 3 + (side.getIndex() + 5) % 6 * 18, 18, 18, new IClickHandler()
			{
				public void onClick(MouseButton button)
				{
					TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.container.base;
					assert base.hasWorld();
					TileEntity neighbour = base.getWorld().getTileEntity(base.getPos().offset(side));
					assert neighbour instanceof IHasGui;
					if (!(neighbour instanceof IPersonalBlock) || ((IPersonalBlock) neighbour).permitsAccess(GuiIndustrialWorkbench.this.container.player.getGameProfile()))
					{
						IC2.network.get(false).requestGUI((IHasGui) neighbour);
						MinecraftForge.EVENT_BUS.register(this);
					} else
					{
						IC2.platform.messagePlayer(GuiIndustrialWorkbench.this.container.player, "Owned by " + ((IPersonalBlock) neighbour).getOwner().getName());
					}
				}

				@SubscribeEvent
				public void waitForClose(GuiOpenEvent event)
				{
					if (keepOpen(event.getGui()))
						return;
					if (!this.firstOpen)
					{
						IC2.network.get(false).requestGUI(GuiIndustrialWorkbench.this.container.base);
						event.setGui(GuiIndustrialWorkbench.this);
						MinecraftForge.EVENT_BUS.unregister(this);
					} else
					{
						this.firstOpen = false;
					}
				}

				private boolean keepOpen(GuiScreen screen)
				{
					if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck == null)
						return false;
					if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck.apply(screen))
					{
						this.jei = true;
						return true;
					}
					if (this.jei)
					{
						this.jei = false;
						return true;
					}
					return false;
				}

				private boolean firstOpen = true;

				private boolean jei = false;
			})).withEnableHandler(() ->
			{
				TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.container.base;
				return (base.hasWorld() && base.getWorld().getTileEntity(base.getPos().offset(side)) instanceof IHasGui);
			}).withIcon(() ->
			{
				TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.container.base;
				assert base.hasWorld();
				BlockPos pos = base.getPos().offset(side);
				IBlockState state = base.getWorld().getBlockState(pos);
				return state.getBlock().getPickBlock(state, null, base.getWorld(), pos, GuiIndustrialWorkbench.this.container.player);
			}).withTooltip(new Supplier<String>()
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
					}
					throw new IllegalStateException("Unexpected direction: " + side);
				}

				public String get()
				{
					TileEntityIndustrialWorkbench base = GuiIndustrialWorkbench.this.container.base;
					assert base.hasWorld();
					TileEntity neighbour = base.getWorld().getTileEntity(base.getPos().offset(side));
					assert neighbour instanceof IHasGui;
					return Localization.translate(((IHasGui) neighbour).getName()) + '\n' + TextFormatting.DARK_GRAY + Localization.translate(getSideName());
				}
			}));
		}
		addElement((new VanillaButton(this, 93, 42, 16, 16, button -> IC2.network.get(false).sendContainerEvent(GuiIndustrialWorkbench.this.container, "clear"))).withTooltip("Clear"));
		addElement(Image.create(this, 94, 43, 14, 14, GuiElement.commonTexture, 256, 256, 210, 47, 224, 61));
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIIndustrialWorkbench.png");
}
