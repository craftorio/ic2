package ic2.core.proxy;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import ic2.core.fluid.Ic2FluidStack;

import java.io.File;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ClientEnvProxy
{
	<H extends AbstractContainerMenu> void registerScreen(MenuType<H> var1, ClientEnvProxy.ScreenFactory<H> var2);

	File getMinecraftDir();

	void registerColorProvider(BlockColor var1, Block... var2);

	void registerColorProvider(ItemColor var1, ItemLike... var2);

	void registerKeyBinding(KeyMapping var1);

	<E extends BlockEntity> void registerBer(BlockEntityType<E> var1, BlockEntityRendererProvider<? super E> var2);

	void registerModelPredicateProvider(ResourceLocation var1, ClampedItemPropertyFunction var2);

	void registerModelPredicateProvider(Item var1, ResourceLocation var2, ClampedItemPropertyFunction var3);

	void registerBlockLayer(RenderType var1, Block... var2);

	<E extends Entity> void registerEntityRenderer(EntityType<? extends E> var1, EntityRendererProvider<E> var2);

	<E extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends E> var1, BlockEntityRendererProvider<E> var2);

	TextureAtlasSprite getFluidStillSprite(Ic2FluidStack var1);

	int getFluidColor(Ic2FluidStack var1);

	String getFluidName(Ic2FluidStack var1);

	default ClientEnvProxy.QuadData getQuadData(BakedQuad quad)
	{
		VertexFormat format = DefaultVertexFormat.f_85811_;
		int[] data = quad.m_111303_();
		int stride = format.m_86017_();
		int posOffset = 0;
		int uvOffset = 4;
		final float[] positions = new float[12];
		final float[] uvs = new float[8];

		for (int i = 0; i < 4; i++)
		{
			int offset = i * stride;

			for (int j = 0; j < 3; j++)
			{
				positions[i * 4 + j] = Float.intBitsToFloat(data[offset + posOffset + j]);
			}

			for (int j = 0; j < 2; j++)
			{
				uvs[i * 4 + j] = Float.intBitsToFloat(data[offset + uvOffset + j]);
			}
		}

		return new ClientEnvProxy.QuadData()
		{
			@Override
			public float[] positions()
			{
				return positions;
			}

			@Override
			public float[] uvs()
			{
				return uvs;
			}

			@Override
			public int tint()
			{
				return quad.m_111305_();
			}

			@Override
			public TextureAtlasSprite sprite()
			{
				return quad.m_173410_();
			}
		};
	}

	interface QuadData
	{
		float[] positions();

		float[] uvs();

		int tint();

		TextureAtlasSprite sprite();
	}

	interface ScreenFactory<H extends AbstractContainerMenu>
	{
		AbstractContainerScreen<H> create(H var1, Inventory var2, Component var3);
	}
}
