package ic2.core.block;

import ic2.api.event.RetextureEvent;
import ic2.core.Ic2Player;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2Color;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockWall extends BlockMultiID<Ic2Color>
{
	public static final Ic2Color defaultColor = Ic2Color.light_gray;

	public static BlockWall create()
	{
		return BlockMultiID.create(BlockWall.class, Ic2Color.class);
	}

	private BlockWall()
	{
		super(BlockName.wall, Material.ROCK);
		this.setHardness(3.0F);
		this.setResistance(30.0F);
		this.setSoundType(SoundType.STONE);
		this.setDefaultState(this.blockState.getBaseState().withProperty(this.typeProperty, defaultColor));
		MinecraftForge.EVENT_BUS.register(this);
	}

	public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color)
	{
		IBlockState state = world.getBlockState(pos);
		Ic2Color type = this.getType(state);
		if (type == null)
		{
			return false;
		} else
		{
			Ic2Color newColor = Ic2Color.get(color);
			if (type != newColor)
			{
				world.setBlockState(pos, state.withProperty(this.typeProperty, newColor));
				return true;
			} else
			{
				return false;
			}
		}
	}

	@SubscribeEvent
	public void onRetexture(RetextureEvent event)
	{
		if (event.state.getBlock() == this)
		{
			World world = event.getWorld();
			Ic2Color color = (Ic2Color) event.state.getValue(this.typeProperty);
			if (ItemBlockTileEntity.placeTeBlock(null, Ic2Player.get(world), world, event.pos, EnumFacing.DOWN, new TileEntityWall(color)))
			{
				IBlockState newState = BlockName.te.getInstance().getDefaultState();
				RetextureEvent event2 = new RetextureEvent(
					world, event.pos, newState, event.side, event.player, event.refState, event.refVariant, event.refSide, event.refColorMultipliers
				);
				MinecraftForge.EVENT_BUS.post(event2);
				if (event2.applied)
				{
					event.applied = true;
					event.setCanceled(true);
				} else
				{
					world.setBlockState(event.pos, event.state);
				}
			}
		}
	}
}
