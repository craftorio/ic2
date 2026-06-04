// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import java.util.List;
import ic2.core.gui.Image;
import ic2.core.gui.VanillaButton;
import ic2.core.ContainerBase;
import net.minecraft.util.text.TextFormatting;
import ic2.core.init.Localization;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.IEnableHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.IHasGui;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.gui.MouseButton;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.CustomButton;
import net.minecraft.util.EnumFacing;
import java.util.Iterator;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Area;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.gui.GuiScreen;
import com.google.common.base.Predicate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiIndustrialWorkbench extends GuiIC2<ContainerIndustrialWorkbench>
{
    public static Predicate<GuiScreen> jeiScreenRecipesGuiCheck;
    private static final ResourceLocation TEXTURE;
    
    public GuiIndustrialWorkbench(final ContainerIndustrialWorkbench container) {
        super(container, 194, 228);
        this.addElement(((GuiElement<GuiElement<?>>)new Area(this, 173, 3, 18, 108) {
            @Override
            protected boolean suppressTooltip(final int mouseX, final int mouseY) {
                for (final GuiElement<?> element : GuiIndustrialWorkbench.this.elements) {
                    if (element.isEnabled() && element != this && element.contains(mouseX, mouseY)) {
                        return true;
                    }
                }
                return false;
            }
        }).withTooltip("ic2.IndustrialWorkbench.gui.adjacent"));
        for (final EnumFacing side : EnumFacing.VALUES) {
            this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 173, 3 + (side.getIndex() + 5) % 6 * 18, 18, 18, new IClickHandler() {
                private boolean firstOpen = true;
                private boolean jei = false;
                
                @Override
                public void onClick(final MouseButton button) {
                    final TileEntityIndustrialWorkbench base = (TileEntityIndustrialWorkbench)((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).base;
                    assert base.hasWorld();
                    final TileEntity neighbour = base.getWorld().getTileEntity(base.getPos().offset(side));
                    assert neighbour instanceof IHasGui;
                    if (!(neighbour instanceof IPersonalBlock) || ((IPersonalBlock)neighbour).permitsAccess(((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).player.getGameProfile())) {
                        IC2.network.get(false).requestGUI((IHasGui)neighbour);
                        MinecraftForge.EVENT_BUS.register((Object)this);
                    }
                    else {
                        IC2.platform.messagePlayer(((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).player, "Owned by " + ((IPersonalBlock)neighbour).getOwner().getName(), new Object[0]);
                    }
                }
                
                @SubscribeEvent
                public void waitForClose(final GuiOpenEvent event) {
                    if (this.keepOpen(event.getGui())) {
                        return;
                    }
                    if (!this.firstOpen) {
                        IC2.network.get(false).requestGUI((IHasGui)((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).base);
                        event.setGui((GuiScreen)GuiIndustrialWorkbench.this);
                        MinecraftForge.EVENT_BUS.unregister((Object)this);
                    }
                    else {
                        this.firstOpen = false;
                    }
                }
                
                private boolean keepOpen(final GuiScreen screen) {
                    if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck == null) {
                        return false;
                    }
                    if (GuiIndustrialWorkbench.jeiScreenRecipesGuiCheck.apply((Object)screen)) {
                        return this.jei = true;
                    }
                    if (this.jei) {
                        this.jei = false;
                        return true;
                    }
                    return false;
                }
            }).withEnableHandler(new IEnableHandler() {
                @Override
                public boolean isEnabled() {
                    final TileEntityIndustrialWorkbench base = (TileEntityIndustrialWorkbench)((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).base;
                    return base.hasWorld() && base.getWorld().getTileEntity(base.getPos().offset(side)) instanceof IHasGui;
                }
            }).withIcon((Supplier<ItemStack>)new Supplier<ItemStack>() {
                public ItemStack get() {
                    final TileEntityIndustrialWorkbench base = (TileEntityIndustrialWorkbench)((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).base;
                    assert base.hasWorld();
                    final BlockPos pos = base.getPos().offset(side);
                    final IBlockState state = base.getWorld().getBlockState(pos);
                    return state.getBlock().getPickBlock(state, (RayTraceResult)null, base.getWorld(), pos, ((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).player);
                }
            })).withTooltip((Supplier<String>)new Supplier<String>() {
                private String getSideName() {
                    switch (side) {
                        case WEST: {
                            return "ic2.dir.West";
                        }
                        case EAST: {
                            return "ic2.dir.East";
                        }
                        case DOWN: {
                            return "ic2.dir.Bottom";
                        }
                        case UP: {
                            return "ic2.dir.Top";
                        }
                        case NORTH: {
                            return "ic2.dir.North";
                        }
                        case SOUTH: {
                            return "ic2.dir.South";
                        }
                        default: {
                            throw new IllegalStateException("Unexpected direction: " + side);
                        }
                    }
                }
                
                public String get() {
                    final TileEntityIndustrialWorkbench base = (TileEntityIndustrialWorkbench)((ContainerIndustrialWorkbench)GuiIndustrialWorkbench.this.container).base;
                    assert base.hasWorld();
                    final TileEntity neighbour = base.getWorld().getTileEntity(base.getPos().offset(side));
                    assert neighbour instanceof IHasGui;
                    return Localization.translate(((IHasGui)neighbour).getName()) + '\n' + TextFormatting.DARK_GRAY + Localization.translate(this.getSideName());
                }
            }));
        }
        final int cancelX = 93;
        final int cancelY = 42;
        this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 93, 42, 16, 16, new IClickHandler() {
            @Override
            public void onClick(final MouseButton button) {
                IC2.network.get(false).sendContainerEvent(GuiIndustrialWorkbench.this.container, "clear");
            }
        })).withTooltip("Clear"));
        this.addElement(Image.create(this, 94, 43, 14, 14, GuiElement.commonTexture, 256, 256, 210, 47, 224, 61));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiIndustrialWorkbench.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIIndustrialWorkbench.png");
    }
}
