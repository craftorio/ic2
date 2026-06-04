// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiChargepadBlock extends GuiIC2<ContainerChargepadBlock>
{
    private static final ResourceLocation background;
    
    public GuiChargepadBlock(final ContainerChargepadBlock container) {
        super(container, 161);
        this.addElement(EnergyGauge.asBar(this, 79, 38, (TileEntityBlock)container.base));
        this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon((Supplier<ItemStack>)new Supplier<ItemStack>() {
            public ItemStack get() {
                return new ItemStack(Items.REDSTONE);
            }
        })).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return ((TileEntityChargepadBlock)container.base).getRedstoneMode();
            }
        }));
        this.addElement(Text.create(this, 79, 25, TextProvider.ofTranslated("ic2.EUStorage.gui.info.level"), 4210752, false));
        this.addElement(Text.create(this, 110, 35, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return " " + (int)Math.min(((TileEntityChargepadBlock)container.base).energy.getEnergy(), ((TileEntityChargepadBlock)container.base).energy.getCapacity());
            }
        }), 4210752, false));
        this.addElement(Text.create(this, 110, 45, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return "/" + (int)((TileEntityChargepadBlock)container.base).energy.getCapacity();
            }
        }), 4210752, false));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiChargepadBlock.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIChargepadBlock.png");
    }
}
