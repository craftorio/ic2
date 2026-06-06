package ic2.core.gui;

import com.google.common.base.Supplier;
import com.mojang.authlib.GameProfile;
import ic2.core.GuiIC2;
import ic2.core.util.StackUtil;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import org.apache.commons.lang3.StringUtils;

public class PlayerHead extends ItemImage
{
	private static final Map<GameProfile, Supplier<ItemStack>> IMAGE_MAKER = new WeakHashMap<>();
	private final GameProfile player;

	public PlayerHead(GuiIC2<?> gui, int x, int y, GameProfile player)
	{
		this(gui, x, y, TileEntitySkull.updateGameprofile(player), null);
	}

	private PlayerHead(GuiIC2<?> gui, int x, int y, GameProfile player, Void skip)
	{
		super(gui, x, y, IMAGE_MAKER.computeIfAbsent(player, profile ->
		{
			ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
			StackUtil.getOrCreateNbtData(skull).setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
			return () -> skull;
		}));
		this.player = player;
		assert player.equals(NBTUtil.readGameProfileFromNBT(((ItemStack) IMAGE_MAKER.get(player).get()).getTagCompound().getCompoundTag("SkullOwner")));
	}

	@Override
	protected List<String> getToolTip()
	{
		List<String> tooltip = super.getToolTip();
		if (StringUtils.isNotBlank(this.player.getName()))
		{
			tooltip.add(this.player.getName());
		}

		return tooltip;
	}
}
