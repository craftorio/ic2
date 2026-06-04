// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.gui.ItemStackImage;
import ic2.core.util.StackUtil;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiBatchCrafter extends GuiIC2<ContainerBatchCrafter>
{
    private static final ResourceLocation TEXTURE;
    
    public GuiBatchCrafter(final ContainerBatchCrafter container) {
        super(container, 206);
        this.addElement(EnergyGauge.asBolt(this, 12, 45, (TileEntityBlock)container.base));
        this.addElement(new LinkedGauge(this, 90, 35, (IGuiValueProvider)container.base, "progress", Gauge.GaugeStyle.ProgressArrow));
        this.addElement(new ItemStackImage(this, 94, 14, (Supplier<ItemStack>)new Supplier<ItemStack>() {
            public ItemStack get() {
                return StackUtil.wrapEmpty(((TileEntityBatchCrafter)((ContainerBatchCrafter)GuiBatchCrafter.this.container).base).recipeOutput);
            }
        }));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiBatchCrafter.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIBatchCrafter.png");
    }
}
