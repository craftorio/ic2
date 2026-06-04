// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradeRegistry;
import java.util.ArrayList;
import java.util.Iterator;
import ic2.core.init.Localization;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.GuiIC2;
import net.minecraft.item.ItemStack;
import java.util.List;

public class UpgradesWidget extends GuiElement<UpgradesWidget>
{
    private final List<ItemStack> compatibleUpgrades;
    private static final int xCoord = 96;
    private static final int yCoord = 128;
    private static final int iWidth = 10;
    private static final int iHeight = 10;
    
    public UpgradesWidget(final GuiIC2<?> gui, final int x, final int y, final IUpgradableBlock te) {
        super(gui, x, y, 10, 10);
        this.compatibleUpgrades = getCompatibleUpgrades(te);
    }
    
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
        bindCommonTexture();
        this.gui.drawTexturedRect(this.x, this.y, this.width, this.height, 96.0, 128.0);
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> ret = super.getToolTip();
        ret.add(Localization.translate("ic2.generic.text.upgrade"));
        for (final ItemStack itemstack : this.compatibleUpgrades) {
            ret.add(itemstack.getDisplayName());
        }
        return ret;
    }
    
    private static List<ItemStack> getCompatibleUpgrades(final IUpgradableBlock block) {
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        final Set<UpgradableProperty> properties = block.getUpgradableProperties();
        for (final ItemStack stack : UpgradeRegistry.getUpgrades()) {
            final IUpgradeItem item = (IUpgradeItem)stack.getItem();
            if (item.isSuitableFor(stack, properties)) {
                ret.add(stack);
            }
        }
        return ret;
    }
}
