package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.transport.TileEntityFluidPipe;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFluidPipe extends ItemIC2 implements IMultiItem<PipeType>, IBoxable
{
	private final List<ItemStack> variants = new ArrayList<>();

	public ItemFluidPipe()
	{
		super(ItemName.pipe);
		this.setHasSubtypes(true);

		for (PipeType type : PipeType.values)
		{
			for (PipeSize pipeSize : PipeSize.values)
			{
				this.variants.add(getPipe(type, pipeSize));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ItemName name)
	{
		ResourceLocation loc = Util.getName(this);
		ModelLoader.setCustomMeshDefinition(this, stackx -> getModelLocation(loc, stackx));

		for (ItemStack stack : this.variants)
		{
			ModelBakery.registerItemVariants(this, new ResourceLocation[] { getModelLocation(loc, stack) });
		}
	}

	private static ModelResourceLocation getModelLocation(ResourceLocation loc, ItemStack itemStack)
	{
		return new ModelResourceLocation(new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "/pipe_" + getSize(itemStack).name()), null);
	}

	public ItemStack getItemStack(PipeType type)
	{
		return getPipe(type, PipeSize.small);
	}

	@Override
	public ItemStack getItemStack(String variant)
	{
		int pos = 0;
		PipeType type = null;
		PipeSize size = null;

		while (pos < variant.length())
		{
			int nextPos = variant.indexOf(44, pos);
			if (nextPos == -1)
			{
				nextPos = variant.length();
			}

			int sepPos = variant.indexOf(58, pos);
			if (sepPos == -1 || sepPos >= nextPos)
			{
				return null;
			}

			String key = variant.substring(pos, sepPos);
			String value = variant.substring(sepPos + 1, nextPos);
			if (key.equals("type"))
			{
				type = PipeType.get(value);
				if (type == null)
				{
					IC2.log.warn(LogCategory.Item, "Invalid pipe type: %s", value);
				}
			} else if (key.equals("size"))
			{
				size = PipeSize.get(value);
				if (size == null)
				{
					IC2.log.warn(LogCategory.Item, "Invalid pipe size: %s", value);
				}
			}

			pos = nextPos + 1;
		}

		if (type == null)
		{
			return null;
		} else
		{
			return size == null ? null : getPipe(type, size);
		}
	}

	@Override
	public String getVariant(ItemStack itemStack)
	{
		if (itemStack == null)
		{
			throw new NullPointerException("null stack");
		}

		if (itemStack.getItem() != this)
		{
			throw new IllegalArgumentException("The stack " + itemStack + " doesn't match " + this);
		}

		PipeType type = getPipeType(itemStack);
		PipeSize size = getSize(itemStack);
		return "type:" + type.getName() + ", size:" + size.getName();
	}

	public static ItemStack getPipe(PipeType type, PipeSize size)
	{
		ItemStack ret = new ItemStack(ItemName.pipe.getInstance(), 1, type.getId());
		NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
		nbt.setByte("type", (byte) type.ordinal());
		nbt.setByte("size", (byte) size.ordinal());
		return ret;
	}

	public static PipeType getPipeType(ItemStack stack)
	{
		NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
		int type = nbt.getByte("type") & 255;
		return type < PipeType.values.length ? PipeType.values[type] : PipeType.bronze;
	}

	private static PipeSize getSize(ItemStack stack)
	{
		NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
		int size = nbt.getByte("size") & 255;
		return size < PipeSize.values.length ? PipeSize.values[size] : PipeSize.small;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return super.getUnlocalizedName(stack) + '.' + getPipeType(stack).getName(getSize(stack));
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List<String> info, ITooltipFlag b)
	{
		PipeType type = getPipeType(itemStack);
		PipeSize size = getSize(itemStack);
		info.add(TextFormatting.WHITE + "Transfer rate: " + (int) (type.transferRate * size.multiplier) + " mb/sec");
		info.add(TextFormatting.WHITE + "Inner capacity: " + (int) (type.transferRate * size.multiplier) + " mb");
		info.add(TextFormatting.GOLD + "Make connections with a wrench");
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack itemStack = StackUtil.get(player, hand);
		IBlockState oldState = world.getBlockState(pos);
		Block oldBlock = oldState.getBlock();
		if (!oldBlock.isReplaceable(world, pos))
		{
			pos = pos.offset(side);
		}

		Block newBlock = BlockName.te.getInstance();
		if (!StackUtil.isEmpty(itemStack)
			&& player.canPlayerEdit(pos, side, itemStack)
			&& world.mayPlace(newBlock, pos, false, side, player)
			&& ((BlockTileEntity) newBlock).canReplace(world, pos, side, BlockName.te.getItemStack(TeBlock.fluid_pipe)))
		{
			newBlock.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, player, hand);
			PipeType type = getPipeType(itemStack);
			PipeSize size = getSize(itemStack);
			TileEntityFluidPipe tileEntity = new TileEntityFluidPipe(type, size);
			if (ItemBlockTileEntity.placeTeBlock(itemStack, player, world, pos, side, tileEntity))
			{
				SoundType soundtype = newBlock.getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(
					player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F
				);
				StackUtil.consumeOrError(player, hand, 1);
			}

			return EnumActionResult.SUCCESS;
		} else
		{
			return EnumActionResult.PASS;
		}
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> itemList)
	{
		if (this.isInCreativeTab(tab))
		{
			List<ItemStack> variants = new ArrayList<>(this.variants);
			itemList.addAll(variants);
		}
	}

	@Override
	public Set<PipeType> getAllTypes()
	{
		return EnumSet.allOf(PipeType.class);
	}

	@Override
	public Set<ItemStack> getAllStacks()
	{
		return new HashSet<>(this.variants);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}
}
