// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import java.util.IdentityHashMap;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import ic2.core.network.GuiSynced;
import net.minecraft.tileentity.TileEntity;
import java.util.ArrayList;
import ic2.core.block.invslot.InvSlot;
import java.util.Iterator;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.inventory.Slot;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import java.util.Map;
import ic2.core.ContainerBase;
import net.minecraft.inventory.IInventory;

public class DynamicContainer<T extends IInventory> extends ContainerBase<T>
{
    private static Map<Class<?>, List<String>> networkedFieldCache;
    
    public static <T extends IInventory> DynamicContainer<T> create(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        return new DynamicContainer<T>(base, player, guiNode);
    }
    
    protected DynamicContainer(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        super(base);
        this.initialize(player, guiNode, guiNode);
    }
    
    private void initialize(final EntityPlayer player, final GuiParser.GuiNode guiNode, final GuiParser.ParentNode parentNode) {
        for (final GuiParser.Node rawNode : parentNode.getNodes()) {
            InvSlot slot;
            switch (rawNode.getType()) {
                case environment: {
                    if (((GuiParser.EnvironmentNode)rawNode).environment != GuiEnvironment.GAME) {
                        continue;
                    }
                    break;
                }
                case playerinventory: {
                    final GuiParser.PlayerInventoryNode node = (GuiParser.PlayerInventoryNode)rawNode;
                    final int xOffset = (node.style.width - 16) / 2;
                    final int yOffset = (node.style.height - 16) / 2;
                    final int width = node.style.width + node.spacing;
                    final int height = node.style.height + node.spacing;
                    for (int row = 0; row < 3; ++row) {
                        for (int col = 0; col < 9; ++col) {
                            this.addSlotToContainer(new Slot((IInventory)player.inventory, col + row * 9 + 9, node.x + col * width + xOffset, node.y + row * height + yOffset));
                        }
                    }
                    for (int col2 = 0; col2 < 9; ++col2) {
                        this.addSlotToContainer(new Slot((IInventory)player.inventory, col2, node.x + col2 * width + xOffset, node.y + node.hotbarOffset + yOffset));
                    }
                    break;
                }
                case slot: {
                    if (!(this.base instanceof IInventorySlotHolder)) {
                        throw new RuntimeException("Invalid base " + this.base + " for slot elements");
                    }
                    final GuiParser.SlotNode node2 = (GuiParser.SlotNode)rawNode;
                    slot = ((IInventorySlotHolder)this.base).getInventorySlot(node2.name);
                    if (slot == null) {
                        throw new RuntimeException("Invalid InvSlot name " + node2.name + " for base " + this.base);
                    }
                    final int x = node2.x + (node2.style.width - 16) / 2;
                    final int y = node2.y + (node2.style.height - 16) / 2;
                    this.addSlotToContainer((Slot)new SlotInvSlot(slot, node2.index, x, y));
                    break;
                }
                case slotgrid: {
                    if (!(this.base instanceof IInventorySlotHolder)) {
                        throw new RuntimeException("Invalid base " + this.base + " for slot elements");
                    }
                    final GuiParser.SlotGridNode node3 = (GuiParser.SlotGridNode)rawNode;
                    slot = ((IInventorySlotHolder)this.base).getInventorySlot(node3.name);
                    if (slot == null) {
                        throw new RuntimeException("Invalid InvSlot name " + node3.name + " for base " + this.base);
                    }
                    final int size = slot.size();
                    if (size > node3.offset) {
                        final int x2 = node3.x + (node3.style.width - 16) / 2;
                        final int y2 = node3.y + (node3.style.height - 16) / 2;
                        final GuiParser.SlotGridNode.SlotGridDimension dim = node3.getDimension(size);
                        final int rows = dim.rows;
                        final int cols = dim.cols;
                        final int width2 = node3.style.width + node3.spacing;
                        final int height2 = node3.style.height + node3.spacing;
                        int idx = node3.offset;
                        if (!node3.vertical) {
                            int y3 = y2;
                            for (int row2 = 0; row2 < rows && idx < size; ++row2) {
                                for (int x3 = x2, col3 = 0; col3 < cols && idx < size; ++idx, x3 += width2, ++col3) {
                                    this.addSlotToContainer((Slot)new SlotInvSlot(slot, idx, x3, y3));
                                }
                                y3 += height2;
                            }
                        }
                        else {
                            int x4 = x2;
                            for (int col4 = 0; col4 < cols && idx < size; ++col4) {
                                for (int y4 = y2, row3 = 0; row3 < rows && idx < size; ++idx, y4 += height2, ++row3) {
                                    this.addSlotToContainer((Slot)new SlotInvSlot(slot, idx, x4, y4));
                                }
                                x4 += width2;
                            }
                        }
                        break;
                    }
                    break;
                }
                case slothologram: {
                    if (!(this.base instanceof IHolographicSlotProvider)) {
                        throw new RuntimeException("Invalid base " + this.base + " for holographic slot elements");
                    }
                    final GuiParser.SlotHologramNode node4 = (GuiParser.SlotHologramNode)rawNode;
                    final int x5 = node4.x + (node4.style.width - 16) / 2;
                    final int y5 = node4.y + (node4.style.height - 16) / 2;
                    this.addSlotToContainer((Slot)new SlotHologramSlot(((IHolographicSlotProvider)this.base).getStacksForName(node4.name), node4.index, x5, y5, node4.stackSizeLimit, this.getCallback()));
                    break;
                }
            }
            if (rawNode instanceof GuiParser.ParentNode) {
                this.initialize(player, guiNode, (GuiParser.ParentNode)rawNode);
            }
        }
    }
    
    protected SlotHologramSlot.ChangeCallback getCallback() {
        return null;
    }
    
    @Override
    public List<String> getNetworkedFields() {
        List<String> ret = DynamicContainer.networkedFieldCache.get(this.base.getClass());
        if (ret != null) {
            return ret;
        }
        ret = new ArrayList<String>();
        Class<?> cls = this.base.getClass();
        do {
            for (final Field field : cls.getDeclaredFields()) {
                if (field.getAnnotation(GuiSynced.class) != null) {
                    ret.add(field.getName());
                }
            }
            cls = cls.getSuperclass();
        } while (cls != TileEntity.class && cls != Object.class);
        if (ret.isEmpty()) {
            ret = Collections.emptyList();
        }
        else {
            ret = new ArrayList<String>(ret);
        }
        DynamicContainer.networkedFieldCache.put(this.base.getClass(), ret);
        return ret;
    }
    
    static {
        DynamicContainer.networkedFieldCache = new IdentityHashMap<Class<?>, List<String>>();
    }
}
