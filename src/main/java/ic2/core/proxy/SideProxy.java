package ic2.core.proxy;

import ic2.api.tile.IRotorProvider;
import ic2.core.sound.SoundManager;
import ic2.core.util.Keyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface SideProxy
{
	void preInit();

	void onPostInit();

	SoundManager getSoundManager();

	Keyboard getKeyboard();

	boolean isSimulating();

	boolean isRendering();

	void requestTick(boolean var1, Runnable var2);

	void onServerAvailable(MinecraftServer var1);

	void displayError(String var1, Object... var2);

	void displayError(Exception var1, String var2, Object... var3);

	void playSoundSp(SoundEvent var1, SoundSource var2, float var3, float var4);

	void playSoundOnce(Entity var1, SoundEvent var2, float var3, float var4);

	Player getPlayerInstance();

	Level getWorld(MinecraftServer var1, ResourceLocation var2);

	Level getPlayerWorld();

	RecipeManager getRecipeManager();

	File getMinecraftDir();

	void messagePlayer(Player player, String translatable, Object... args);

	void messagePlayer(Player player, Component translatable);

	<T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> var1);
}
