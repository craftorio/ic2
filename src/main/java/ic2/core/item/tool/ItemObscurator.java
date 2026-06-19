package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.tile.RetexturableBlock;
import ic2.core.IC2;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.PriorityUsableItem;
import ic2.core.model.ModelUtil;
import ic2.core.network.IPlayerItemDataListener;
import ic2.core.proxy.ClientEnvProxy;
import ic2.core.proxy.SideProxyClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemObscurator extends BaseElectricItem implements PriorityUsableItem, IPlayerItemDataListener
{
	private static final int[] noTint = new int[] { -1 };
	private static final int[] zeroTint = new int[] { 0 };
	private static final int[] defaultColorMultiplier = new int[] { 16777215 };
	private static final int[] colorMultiplierOpaqueWhite = new int[] { -1 };

	public ItemObscurator(Properties settings)
	{
		super(settings, 100000.0, 250.0, 2);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		Player player = context.getPlayer();
		if (!player.isShiftKeyDown() && !world.isClientSide && ElectricItem.manager.canUse(stack, 5000.0))
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			BlockState refState;
			Direction refSide;
			int[] colorMultipliers;
			if ((refState = getState(nbt)) != null && (refSide = getSide(nbt)) != null && (colorMultipliers = getColorMultipliers(nbt)) != null)
			{
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				String refVariant = getVariant(nbt);
				boolean applied;
				if (block instanceof RetexturableBlock)
				{
					applied = ((RetexturableBlock) block).retexture(state, world, pos, side, player, refState, refVariant, refSide, colorMultipliers);
				} else
				{
					applied = IC2.envProxy.announceRetexture(world, pos, state, side, player, refState, refVariant, refSide, colorMultipliers);
				}

				if (applied)
				{
					ElectricItem.manager.use(stack, 5000.0, player);
					return InteractionResult.SUCCESS;
				} else
				{
					return InteractionResult.PASS;
				}
			} else
			{
				clear(nbt);
				return InteractionResult.PASS;
			}
		} else if (player.isShiftKeyDown() && world.isClientSide && ElectricItem.manager.canUse(stack, 20000.0))
		{
			return this.scanBlock(stack, player, world, pos, side) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	private boolean scanBlock(ItemStack stack, Player player, Level world, BlockPos pos, Direction side)
	{
		assert world.isClientSide;
		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return false;
		}

		ItemObscurator.ObscuredRenderInfo renderInfo = getRenderInfo(state, side);
		if (renderInfo == null)
		{
			return false;
		}

		String variant = ModelUtil.getVariant(state);
		int[] colorMultipliers = new int[renderInfo.tints.length];

		for (int i = 0; i < renderInfo.tints.length; i++)
		{
			colorMultipliers[i] = SideProxyClient.mc.getBlockColors().getColor(state, world, pos, renderInfo.tints[i]);
		}

		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		if (getState(nbt) == state && variant.equals(getVariant(nbt)) && getSide(nbt) == side && Arrays.equals(getColorMultipliers(nbt), colorMultipliers))
		{
			return false;
		}

		IC2.network.get(false).sendPlayerItemData(player, player.getInventory().selected, state.getBlock(), variant, side, colorMultipliers);
		return true;
	}

	@Override
	public void onPlayerItemNetworkData(Player player, int slot, Object... data)
	{
		if (data[0] instanceof Block)
		{
			if (data[1] instanceof String)
			{
				if (data[2] instanceof Integer)
				{
					if (data[3] instanceof int[])
					{
						ItemStack stack = player.getInventory().items.get(slot);
						if (ElectricItem.manager.use(stack, 20000.0, player))
						{
							CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
							setState(nbt, (Block) data[0], (String) data[1]);
							setSide(nbt, (Integer) data[2]);
							setColorMultipliers(nbt, (int[]) data[3]);
						}
					}
				}
			}
		}
	}

	public static BlockState getState(CompoundTag nbt)
	{
		String blockName = nbt.getString("refBlock");
		if (blockName.isEmpty())
		{
			return null;
		}

		Block block = Util.getBlock(blockName);
		if (block == null)
		{
			return null;
		}

		String variant = getVariant(nbt);
		return BlockStateUtil.getState(block, variant);
	}

	public static String getVariant(CompoundTag nbt)
	{
		return nbt.getString("refVariant");
	}

	private static void setState(CompoundTag nbt, Block block, String variant)
	{
		nbt.putString("refBlock", Util.getName(block).toString());
		nbt.putString("refVariant", variant);
	}

	public static Direction getSide(CompoundTag nbt)
	{
		int ordinal = nbt.getByte("refSide");
		return ordinal >= 0 && ordinal < Util.ALL_DIRS.length ? Util.ALL_DIRS[ordinal] : null;
	}

	private static void setSide(CompoundTag nbt, int side)
	{
		nbt.putByte("refSide", (byte) side);
	}

	public static int[] getColorMultipliers(CompoundTag nbt)
	{
		int[] ret = nbt.getIntArray("refColorMuls");
		return ret.length == 0 ? null : internColorMultipliers(ret);
	}

	public static void setColorMultipliers(CompoundTag nbt, int[] colorMultipliers)
	{
		if (colorMultipliers.length == 0)
		{
			throw new IllegalArgumentException();
		}

		nbt.putIntArray("refColorMuls", colorMultipliers);
	}

	private static void clear(CompoundTag nbt)
	{
		nbt.remove("refBlock");
		nbt.remove("refVariant");
		nbt.remove("refSide");
		nbt.remove("refColorMul");
	}

	// TODO
	public static ItemObscurator.ObscuredRenderInfo getRenderInfo(BlockState state, Direction side)
	{
		if (ItemBlockRenderTypes.getChunkRenderType(state) == RenderType.translucent())
		{
			return null;
		}

		BakedModel model = ModelUtil.getBlockModel(state);

		RandomSource rand = RandomSource.create(42L);
		// @Nullable BlockState state,
		//    @Nullable Direction side,
		//    @NotNull RandomSource rand,
		//    @NotNull ModelData data,
		//    @Nullable RenderType renderType
		List<BakedQuad> faceQuads = model.getQuads(state, side, rand);
		if (faceQuads.isEmpty())
		{
			return null;
		}

		float[] uvs = new float[faceQuads.size() * 4];
		int uvsOffset = 0;
		int[] tints = new int[faceQuads.size()];
		TextureAtlasSprite[] sprites = new TextureAtlasSprite[faceQuads.size()];

		for (BakedQuad faceQuad : faceQuads)
		{
			ClientEnvProxy.QuadData data = SideProxyClient.envProxy.getQuadData(faceQuad);
			float[] positions = data.positions();
			int dx = side.getStepX();
			int dy = side.getStepY();
			int dz = side.getStepZ();
			int xS = (dx + 1) / 2;
			int yS = (dy + 1) / 2;
			int zS = (dz + 1) / 2;
			int firstVertex = -1;

			for (int v = 0; v < 4; v++)
			{
				int vo = v * 3;
				if (Util.isSimilar(positions[vo], xS) && Util.isSimilar(positions[vo + 1], yS) && Util.isSimilar(positions[vo + 2], zS))
				{
					firstVertex = v;
					break;
				}
			}

			if (firstVertex != -1)
			{
				Vector3 v1 = new Vector3(positions[3] - positions[0], positions[4] - positions[1], positions[5] - positions[2]);
				if (Util.isSimilar(v1.lengthSquared(), 1.0))
				{
					Vector3 v4 = new Vector3(positions[9] - positions[0], positions[10] - positions[1], positions[11] - positions[2]);
					if (Util.isSimilar(v4.lengthSquared(), 1.0))
					{
						Vector3 v3 = new Vector3(positions[9] - positions[6], positions[10] - positions[7], positions[11] - positions[8]);
						if (Util.isSimilar(v3.copy().add(v1).lengthSquared(), 0.0))
						{
							Vector3 normal = v1.copy().cross(v4);
							if (Util.isSimilar(normal.copy().sub(dx, dy, dz).lengthSquared(), 0.0))
							{
								tints[uvsOffset / 4] = data.tint();
								sprites[uvsOffset / 4] = data.sprite();
								float[] uvsIn = data.uvs();
								uvs[uvsOffset++] = uvsIn[firstVertex * 2];
								uvs[uvsOffset++] = uvsIn[firstVertex * 2 + 1];
								uvs[uvsOffset++] = uvsIn[(firstVertex + 2) % 4 * 2];
								uvs[uvsOffset++] = uvsIn[(firstVertex + 2) % 4 * 2 + 1];
							}
						}
					}
				}
			}
		}

		if (uvsOffset == 0)
		{
			return null;
		}

		if (uvsOffset < uvs.length)
		{
			uvs = Arrays.copyOf(uvs, uvsOffset);
			tints = Arrays.copyOf(tints, uvsOffset / 4);
		}

		tints = internTints(tints);
		return new ItemObscurator.ObscuredRenderInfo(uvs, tints, sprites);
	}

	public static int[] internTints(int[] tints)
	{
		if (tints.length == 1)
		{
			if (tints[0] == noTint[0])
			{
				return noTint;
			}

			if (tints[0] == zeroTint[0])
			{
				return zeroTint;
			}
		}

		return tints;
	}

	public static int[] internColorMultipliers(int[] colorMultipliers)
	{
		if (colorMultipliers.length == 1)
		{
			if (colorMultipliers[0] == defaultColorMultiplier[0])
			{
				return defaultColorMultiplier;
			}

			if (colorMultipliers[0] == colorMultiplierOpaqueWhite[0])
			{
				return colorMultiplierOpaqueWhite;
			}
		}

		return colorMultipliers;
	}

	public static class ObscuredRenderInfo
	{
		public final float[] uvs;
		public final int[] tints;
		public final TextureAtlasSprite[] sprites;

		private ObscuredRenderInfo(float[] uvs, int[] tints, TextureAtlasSprite[] sprites)
		{
			this.uvs = uvs;
			this.tints = tints;
			this.sprites = sprites;
		}
	}
}
