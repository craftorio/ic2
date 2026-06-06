package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.DelegatingInventory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPersonalChest extends TileEntityInventory implements IPersonalBlock, IHasGui
{
	private GameProfile owner = null;
	private static final int openingSteps = 10;
	private static final List<AxisAlignedBB> aabbs = Arrays.asList(new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375));
	public final InvSlot contentSlot;
	private final Set<EntityPlayer> usingPlayers = Collections.newSetFromMap(new WeakHashMap<>());
	private int usingPlayerCount;
	private byte lidAngle;
	private byte prevLidAngle;

	public TileEntityPersonalChest()
	{
		this.contentSlot = new InvSlot(this, "content", InvSlot.Access.NONE, 54);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("ownerGameProfile"))
		{
			this.owner = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("ownerGameProfile"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.owner != null)
		{
			NBTTagCompound ownerNbt = new NBTTagCompound();
			NBTUtil.writeGameProfile(ownerNbt, this.owner);
			nbt.setTag("ownerGameProfile", ownerNbt);
		}

		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		this.prevLidAngle = this.lidAngle;
		if (this.usingPlayerCount > 0 && this.lidAngle <= 0)
		{
			World world = this.getWorld();
			world.playSound(null, this.pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		}

		if (this.usingPlayerCount == 0 && this.lidAngle > 0 || this.usingPlayerCount > 0 && this.lidAngle < 10)
		{
			if (this.usingPlayerCount > 0)
			{
				this.lidAngle++;
			} else
			{
				this.lidAngle--;
			}

			int closeThreshold = 5;
			if (this.lidAngle < closeThreshold && this.prevLidAngle >= closeThreshold)
			{
				World world = this.getWorld();
				world.playSound(
					null, this.pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F
				);
			}
		}
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		return aabbs;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
		if (!this.getWorld().isRemote)
		{
			this.usingPlayers.add(player);
			this.updateUsingPlayerCount();
		}
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
		if (!this.getWorld().isRemote)
		{
			this.usingPlayers.remove(player);
			this.updateUsingPlayerCount();
		}
	}

	private void updateUsingPlayerCount()
	{
		this.usingPlayerCount = this.usingPlayers.size();
		IC2.network.get(true).updateTileEntityField(this, "usingPlayerCount");
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("owner");
		ret.add("usingPlayerCount");
		return ret;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer player)
	{
		if (!this.permitsAccess(player.getGameProfile()))
		{
			IC2.platform.messagePlayer(player, "This safe is owned by " + this.owner.getName());
			return false;
		} else if (!this.contentSlot.isEmpty())
		{
			IC2.platform.messagePlayer(player, "Can't wrench non-empty safe");
			return false;
		} else
		{
			return true;
		}
	}

	@Override
	public boolean permitsAccess(GameProfile profile)
	{
		return checkAccess(this, profile);
	}

	@Override
	public IInventory getPrivilegedInventory(GameProfile accessor)
	{
		return (IInventory) (!this.permitsAccess(accessor) ? this : new DelegatingInventory(this)
		{
			@Override
			public int getSizeInventory()
			{
				return TileEntityPersonalChest.this.contentSlot.size();
			}

			@Override
			public ItemStack getStackInSlot(int index)
			{
				return TileEntityPersonalChest.this.contentSlot.get(index);
			}

			@Override
			public ItemStack decrStackSize(int index, int amount)
			{
				ItemStack stack = this.getStackInSlot(index);
				if (StackUtil.isEmpty(stack))
				{
					return StackUtil.emptyStack;
				}

				if (amount >= StackUtil.getSize(stack))
				{
					this.setInventorySlotContents(index, StackUtil.emptyStack);
					return stack;
				}

				if (amount != 0)
				{
					if (amount < 0)
					{
						int space = Math.min(TileEntityPersonalChest.this.contentSlot.getStackSizeLimit(), stack.getMaxStackSize()) - StackUtil.getSize(stack);
						amount = Math.max(amount, -space);
					}

					stack = StackUtil.decSize(stack, amount);
					this.setInventorySlotContents(index, stack);
				}

				return StackUtil.copyWithSize(stack, amount);
			}

			@Override
			public ItemStack removeStackFromSlot(int index)
			{
				ItemStack ret = this.getStackInSlot(index);
				if (!StackUtil.isEmpty(ret))
				{
					this.setInventorySlotContents(index, StackUtil.emptyStack);
				}

				return ret;
			}

			@Override
			public void setInventorySlotContents(int index, ItemStack stack)
			{
				TileEntityPersonalChest.this.contentSlot.put(index, stack);
				this.markDirty();
			}

			@Override
			public int getInventoryStackLimit()
			{
				return TileEntityPersonalChest.this.contentSlot.getStackSizeLimit();
			}

			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack)
			{
				return TileEntityPersonalChest.this.contentSlot.accepts(stack);
			}
		});
	}

	public static <T extends TileEntity & IPersonalBlock> boolean checkAccess(T te, GameProfile profile)
	{
		if (profile == null)
		{
			return te.getOwner() == null;
		}

		GameProfile teOwner = te.getOwner();
		if (!te.getWorld().isRemote)
		{
			if (teOwner == null)
			{
				te.setOwner(profile);
				IC2.network.get(true).updateTileEntityField(te, "owner");
				return true;
			}

			if (te.getWorld().getMinecraftServer().getPlayerList().canSendCommands(profile))
			{
				return true;
			}
		} else if (teOwner == null)
		{
			return true;
		}

		return teOwner.getId() != null ? teOwner.getId().equals(profile.getId()) : teOwner.getName().equals(profile.getName());
	}

	@Override
	public GameProfile getOwner()
	{
		return this.owner;
	}

	@Override
	public void setOwner(GameProfile owner)
	{
		this.owner = owner;
	}

	@Override
	protected boolean canEntityDestroy(Entity entity)
	{
		return false;
	}

	@Override
	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!this.getWorld().isRemote && !this.permitsAccess(player.getGameProfile()))
		{
			IC2.platform.messagePlayer(player, "This safe is owned by " + this.getOwner().getName());
			return false;
		} else
		{
			return super.onActivated(player, hand, side, hitX, hitY, hitZ);
		}
	}

	@Override
	public ContainerBase<TileEntityPersonalChest> getGuiContainer(EntityPlayer player)
	{
		this.openInventory(player);
		return new DynamicContainer<TileEntityPersonalChest>(this, player, GuiParser.parse(this.teBlock))
		{
			public void onContainerClosed(EntityPlayer player)
			{
				this.base.onGuiClosed(player);
				super.onContainerClosed(player);
			}
		};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityPersonalChest>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
		this.closeInventory(player);
	}

	public float getLidAngle(float partialTicks)
	{
		return Util.lerp(this.prevLidAngle, this.lidAngle, partialTicks) / 10.0F;
	}
}
