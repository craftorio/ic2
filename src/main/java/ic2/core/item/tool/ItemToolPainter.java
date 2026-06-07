package ic2.core.item.tool;

import com.google.common.collect.UnmodifiableIterator;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.Ic2Color;
import ic2.core.util.StackUtil;
import ic2.core.util.VanillaColorBlockId;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemToolPainter extends ItemToolCrafting implements IBoxable
{
	Ic2Color color = null;
	private static final int maxDamage = 32;

	public ItemToolPainter(Properties settings, Ic2Color color)
	{
		super(settings);
		this.color = color;
	}

	public InteractionResult m_6225_(UseOnContext context)
	{
		if (this.color == null)
		{
			return InteractionResult.PASS;
		}

		ItemStack stack = context.m_43722_();
		Level world = context.m_43725_();
		BlockPos pos = context.m_8083_();
		Player player = context.m_43723_();
		InteractionHand hand = context.m_43724_();
		if (!(stack.getItem() instanceof ItemToolPainter))
		{
			return InteractionResult.PASS;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (this.colorBlock(world, pos, block, state, this.color))
		{
			this.damagePainter(stack, player, hand, this.color);
			if (world.isClientSide && player != null)
			{
				player.m_5496_(Ic2SoundEvents.ITEM_PAINTER_USE, 1.0F, 1.0F);
			}

			return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	private boolean colorBlock(Level world, BlockPos pos, Block block, BlockState state, Ic2Color color)
	{
		DyeColor newColor = color.dyeColor;
		UnmodifiableIterator tagList = state.m_61148_().keySet().iterator();

		while (tagList.hasNext())
		{
			Property<?> property = (Property<?>) tagList.next();
			if (property.m_61709_() == DyeColor.class)
			{
				Property<DyeColor> typedProperty = (Property<DyeColor>) property;
				DyeColor oldColor = (DyeColor) state.getValue(typedProperty);
				if (oldColor != newColor && typedProperty.m_6908_().contains(newColor))
				{
					world.setBlockAndUpdate(pos, (BlockState) state.setValue(typedProperty, newColor));
					return true;
				}

				return false;
			}
		}

		if (!canColor(block, color.dyeColor))
		{
			return false;
		}

		List<TagKey<Block>> tagListx = block.defaultBlockState().m_204343_().toList();
		if (tagListx.contains(BlockTags.f_13089_))
		{
			world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.WOOL));
			return true;
		}

		if (block instanceof StainedGlassBlock || block.defaultBlockState().m_60713_(Blocks.f_50058_))
		{
			world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.STAINED_GLASS));
			return true;
		}

		if (block instanceof StainedGlassPaneBlock || block.defaultBlockState().m_60713_(Blocks.f_50185_))
		{
			world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.STAINED_GLASS_PANE, state));
			return true;
		}

		if (tagListx.contains(BlockTags.f_13038_))
		{
			BedBlock bedBlock = (BedBlock) block;
			BlockPos bedBlockPos2 = pos.relative(BedBlock.m_49557_(state));
			BlockState bedBlockState2 = world.getBlockState(bedBlockPos2);
			if (bedBlockState2.m_60713_(bedBlock))
			{
				world.m_7731_(pos, Blocks.f_50016_.defaultBlockState(), 48);
				world.m_7731_(bedBlockPos2, Blocks.f_50016_.defaultBlockState(), 48);
				world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BED, state));
				world.setBlockAndUpdate(bedBlockPos2, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BED, bedBlockState2));
			}

			return true;
		} else
		{
			if (tagListx.contains(BlockTags.f_144265_))
			{
				world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.CANDLE, state));
				return true;
			}

			if (block instanceof BannerBlock)
			{
				world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.BANNER, state));
				return true;
			}

			if (block instanceof WallBannerBlock)
			{
				world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.WALL_BANNER, state));
				return true;
			}

			if (tagListx.contains(BlockTags.f_198156_))
			{
				world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.TERRACOTTA));
				return true;
			}

			if (block instanceof GlazedTerracottaBlock)
			{
				world.setBlockAndUpdate(pos, getBlockStateWithProperties(color.dyeColor, VanillaColorBlockId.GLAZED_TERRACOTTA, state));
				return true;
			}

			if (block instanceof ConcretePowderBlock)
			{
				world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CONCRETE_POWDER));
				return true;
			}

			if (tagListx.contains(BlockTags.f_215838_))
			{
				world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CARPET));
				return true;
			}

			if (tagListx.contains(BlockTags.f_13083_))
			{
				BlockEntity shulkerBlockEntity = world.getBlockEntity(pos);
				if (shulkerBlockEntity == null)
				{
					return false;
				}

				CompoundTag shulkerNbt = shulkerBlockEntity.m_187481_();
				BlockState newShulkerBoxState = ShulkerBoxBlock.m_56190_(color.dyeColor).m_152465_(state);
				world.setBlockAndUpdate(pos, newShulkerBoxState);
				BlockEntity newShulkerBlockEntity = BlockEntity.m_155241_(pos, newShulkerBoxState, shulkerNbt);
				world.m_151523_(newShulkerBlockEntity);
				return true;
			} else
			{
				ResourceLocation identifier = Registry.BLOCK.getKey(block);
				if (identifier.m_135827_().equals("minecraft") && identifier.m_135815_().contains("concrete"))
				{
					world.setBlockAndUpdate(pos, getColorBlockState(color.dyeColor, VanillaColorBlockId.CONCRETE));
					return true;
				} else
				{
					return false;
				}
			}
		}
	}

	public static boolean canColor(Block block, DyeColor color)
	{
		ResourceLocation identifier = Registry.BLOCK.getKey(block);
		return !identifier.m_135815_().contains(color.m_41065_());
	}

	public static BlockState getColorBlockState(DyeColor color, VanillaColorBlockId vanillaColorBlock)
	{
		ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath("minecraft", color.m_41065_() + "_" + vanillaColorBlock.id);
		return ((Block) Registry.BLOCK.m_7745_(identifier)).defaultBlockState();
	}

	public static BlockState getBlockStateWithProperties(DyeColor color, VanillaColorBlockId vanillaColorBlock, BlockState state)
	{
		ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath("minecraft", color.m_41065_() + "_" + vanillaColorBlock.id);
		return ((Block) Registry.BLOCK.m_7745_(identifier)).m_152465_(state);
	}

	public InteractionResult m_6880_(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand)
	{
		if (this.color == null)
		{
			return InteractionResult.PASS;
		} else if (entity instanceof Sheep sheep && sheep.m_29874_() != this.color.dyeColor)
		{
			sheep.m_29855_(this.color.dyeColor);
			this.damagePainter(stack, user, user.m_7655_(), this.color);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			boolean newValue = !nbtData.getBoolean("autoRefill");
			nbtData.putBoolean("autoRefill", newValue);
			if (newValue)
			{
				IC2.sideProxy.messagePlayer(player, "Painter automatic refill mode enabled");
			} else
			{
				IC2.sideProxy.messagePlayer(player, "Painter automatic refill mode disabled");
			}

			return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
		} else
		{
			return new InteractionResultHolder(InteractionResult.PASS, stack);
		}
	}

	public void damagePainter(ItemStack stack, Player player, InteractionHand hand, Ic2Color color)
	{
		assert color != null;
		stack.m_220157_(1, player.m_217043_(), player instanceof ServerPlayer ? (ServerPlayer) player : null);
		if (stack.getDamageValue() >= stack.m_41776_())
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			if (!nbtData.getBoolean("autoRefill"))
			{
				player.m_21008_(hand, new ItemStack(Ic2Items.PAINTER, 1));
				return;
			}

			ItemStack consumedStack = StackUtil.consumeFromPlayerInventoryAndGet(player, StackUtil.sameItem(stack.getItem()), 1, true);
			if (consumedStack.m_41619_())
			{
				player.m_21008_(hand, new ItemStack(Ic2Items.PAINTER, 1));
			} else
			{
				player.m_21008_(hand, consumedStack);
				StackUtil.addToPlayerInventory(player, new ItemStack(Ic2Items.PAINTER, 1));
			}
		}
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return super.canBeStoredInToolbox(itemstack);
	}
}
