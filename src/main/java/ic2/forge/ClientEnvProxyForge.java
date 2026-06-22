package ic2.forge;

import ic2.core.fluid.Ic2FluidStack;
import ic2.core.proxy.ClientEnvProxy;
import ic2.core.proxy.SideProxyClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ClientEnvProxyForge implements ClientEnvProxy
{
	static List<ClientEnvProxyForge.BlockColorProviderRegistration> blockColorProviderRegistrations = new ArrayList<>();
	static List<ClientEnvProxyForge.ItemColorProviderRegistration> itemColorProviderRegistrations = new ArrayList<>();
	static List<KeyMapping> keyBindingRegistrations = new ArrayList<>();
	static List<ClientEnvProxyForge.BerRegistration<?>> berRegistrations = new ArrayList<>();
	static List<ClientEnvProxyForge.BlockLayerRegistration> blockLayerRegistrations = new ArrayList<>();
	static List<ClientEnvProxyForge.EntityRendererRegistration<?>> entityRendererRegistrations = new ArrayList<>();
	static List<ClientEnvProxyForge.BlockEntityRendererRegistration<?>> blockEntityRendererRegistrations = new ArrayList<>();

	@Override
	public <H extends AbstractContainerMenu> void registerScreen(MenuType<H> type, ClientEnvProxy.ScreenFactory<H> factory)
	{
		MenuScreens.register(type, factory::create);
	}

	@Override
	public File getMinecraftDir()
	{
		return FMLPaths.GAMEDIR.get().toFile();
	}

	@Override
	public void registerColorProvider(BlockColor provider, Block... blocks)
	{
		blockColorProviderRegistrations.add(new ClientEnvProxyForge.BlockColorProviderRegistration(provider, blocks));
	}

	@Override
	public void registerColorProvider(ItemColor provider, ItemLike... items)
	{
		itemColorProviderRegistrations.add(new ClientEnvProxyForge.ItemColorProviderRegistration(provider, items));
	}

	@Override
	public void registerKeyBinding(KeyMapping keyBinding)
	{
		keyBindingRegistrations.add(keyBinding);
	}

	@Override
	public <E extends BlockEntity> void registerBer(BlockEntityType<E> type, BlockEntityRendererProvider<? super E> rendererFactory)
	{
		berRegistrations.add(new ClientEnvProxyForge.BerRegistration<>(type, rendererFactory));
	}

	@Override
	public void registerModelPredicateProvider(ResourceLocation id, ClampedItemPropertyFunction predicateProvider)
	{
		ItemProperties.registerGeneric(id, predicateProvider);
	}

	@Override
	public void registerModelPredicateProvider(Item item, ResourceLocation id, ClampedItemPropertyFunction predicateProvider)
	{
		ItemProperties.register(item, id, predicateProvider);
	}

	@Override
	public void registerBlockLayer(RenderType layer, Block... blocks)
	{
		blockLayerRegistrations.add(new ClientEnvProxyForge.BlockLayerRegistration(layer, blocks));
	}

	@Override
	public <E extends Entity> void registerEntityRenderer(EntityType<? extends E> type, EntityRendererProvider<E> factory)
	{
		entityRendererRegistrations.add(new ClientEnvProxyForge.EntityRendererRegistration<>(type, factory));
	}

	@Override
	public <E extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends E> type, BlockEntityRendererProvider<E> factory)
	{
		blockEntityRendererRegistrations.add(new ClientEnvProxyForge.BlockEntityRendererRegistration<>(type, factory));
	}

	IClientFluidTypeExtensions getAttributes(Ic2FluidStack stack)
	{
		return IClientFluidTypeExtensions.of(stack.getFluid());
	}

	@Override
	public TextureAtlasSprite getFluidStillSprite(Ic2FluidStack stack)
	{
		ResourceLocation id = this.getAttributes(stack).getStillTexture(EnvFluidHandlerForge.getForgeFs(stack));
		return SideProxyClient.mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(id);
	}

	@Override
	public int getFluidColor(Ic2FluidStack stack)
	{
		return this.getAttributes(stack).getTintColor(EnvFluidHandlerForge.getForgeFs(stack));
	}

	@Override
	public String getFluidName(Ic2FluidStack stack)
	{
		return Component.translatable(stack.getFluid().getFluidType().getDescriptionId(EnvFluidHandlerForge.getForgeFs(stack))).getString();
	}

	record BerRegistration<T extends BlockEntity>(BlockEntityType<? extends T> blockEntityType,
	                                              BlockEntityRendererProvider<T> blockEntityRendererProvider)
	{
	}

	record BlockColorProviderRegistration(BlockColor provider, Block... blocks)
	{
	}

	record BlockEntityRendererRegistration<B extends BlockEntity>(BlockEntityType<? extends B> type,
	                                                              BlockEntityRendererProvider<B> factory)
	{
	}

	record BlockLayerRegistration(RenderType layer, Block... blocks)
	{
	}

	record EntityRendererRegistration<E extends Entity>(EntityType<? extends E> type, EntityRendererProvider<E> factory)
	{
	}

	record ItemColorProviderRegistration(ItemColor provider, ItemLike... items)
	{
	}
}
