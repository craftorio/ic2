// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.WeakHashMap;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntitySkull;
import ic2.core.GuiIC2;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import com.mojang.authlib.GameProfile;
import java.util.Map;

public class PlayerHead extends ItemImage
{
    private static final Map<GameProfile, Supplier<ItemStack>> IMAGE_MAKER;
    private final GameProfile player;
    
    public PlayerHead(final GuiIC2<?> gui, final int x, final int y, final GameProfile player) {
        this(gui, x, y, TileEntitySkull.updateGameprofile(player), null);
    }
    
    private PlayerHead(final GuiIC2<?> gui, final int x, final int y, final GameProfile player, final Void skip) {
        super(gui, x, y, PlayerHead.IMAGE_MAKER.computeIfAbsent(player, profile -> {
            final ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
            StackUtil.getOrCreateNbtData(skull).setTag("SkullOwner", (NBTBase)NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
            return () -> skull;
        }));
        this.player = player;
        assert player.equals((Object)NBTUtil.readGameProfileFromNBT(((ItemStack)PlayerHead.IMAGE_MAKER.get(player).get()).getTagCompound().getCompoundTag("SkullOwner")));
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> tooltip = super.getToolTip();
        if (StringUtils.isNotBlank((CharSequence)this.player.getName())) {
            tooltip.add(this.player.getName());
        }
        return tooltip;
    }
    
    static {
        IMAGE_MAKER = new WeakHashMap<GameProfile, Supplier<ItemStack>>();
    }
}
