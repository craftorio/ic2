package ic2.core.gui;

import com.mojang.authlib.GameProfile;
import ic2.core.Ic2Gui;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHead extends ItemImage
{
	private static final Map<GameProfile, ItemStack> IMAGE_MAKER = Collections.synchronizedMap(new WeakHashMap<>());
	private final GameProfile player;

	public PlayerHead(Ic2Gui<?> gui, int x, int y, GameProfile player)
	{
		super(gui, x, y, new PlayerHead.PlayerHeadSupplier(player));
		this.player = player;
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> tooltip = super.getToolTip();
		if (StringUtils.isNotBlank(this.player.getName()))
		{
			tooltip.add(Component.literal(this.player.getName()));
		}

		return tooltip;
	}

	private static final class PlayerHeadSupplier implements Supplier<ItemStack>
	{
		private final GameProfile profile;

		PlayerHeadSupplier(GameProfile profile)
		{
			this.profile = profile;
		}

		public ItemStack get()
		{
			try
			{
				GameProfile resolvedProfile = SkullBlockEntity.fetchGameProfile(this.profile.getName())
					.get()
					.orElse(this.profile);
				return PlayerHead.IMAGE_MAKER.computeIfAbsent(resolvedProfile, rp ->
				{
					ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
					CompoundTag profileTag = new CompoundTag();
					profileTag.putUUID("Id", rp.getId());
					profileTag.putString("Name", rp.getName());
					StackUtil.getOrCreateNbtData(skull).put("SkullOwner", profileTag);
					return skull;
				});
			} catch (InterruptedException | ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
