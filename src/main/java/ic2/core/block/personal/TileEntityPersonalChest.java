package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.DelegatingInventory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.RandomSource;

public class TileEntityPersonalChest extends TileEntityInventory implements IPersonalBlock, IHasGui
{
	private static final List<AABB> aabbs = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	public final InvSlot contentSlot;
	private final Set<Player> usingPlayers = Collections.newSetFromMap(new WeakHashMap<>());
	private GameProfile owner = null;
	private int usingPlayerCount;
	private byte lidAngle;
	private byte prevLidAngle;

	public TileEntityPersonalChest(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.PERSONAL_CHEST, pos, state);
		this.contentSlot = new InvSlot(this, "content", InvSlot.Access.NONE, 54);
	}

	public static <T extends BlockEntity & IPersonalBlock> boolean checkAccess(T te, GameProfile profile)
	{
		if (profile == null)
		{
			return te.getOwner() == null;
		}

		GameProfile teOwner = te.getOwner();
		if (!te.getLevel().isClientSide)
		{
			if (teOwner == null)
			{
				te.setOwner(profile);
				IC2.network.get(true).updateTileEntityField(te, "owner");
				return true;
			}

			if (te.getLevel().getServer().getPlayerList().isOp(profile))
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
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("ownerGameProfile"))
		{
			this.owner = NbtUtils.readGameProfile(nbt.getCompound("ownerGameProfile"));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (this.owner != null)
		{
			CompoundTag ownerNbt = new CompoundTag();
			NbtUtils.writeGameProfile(ownerNbt, this.owner);
			nbt.put("ownerGameProfile", ownerNbt);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
     RandomSource rng = RandomSource.create();
		super.updateEntityClient();
		this.prevLidAngle = this.lidAngle;
		if (this.usingPlayerCount > 0 && this.lidAngle <= 0)
		{
			Level world = this.getLevel();
			world.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, rng.nextFloat() * 0.1F + 0.9F);
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
				Level world = this.getLevel();
				world.playSound(null, this.worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, rng.nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return aabbs;
	}

	@Override
	public void startOpen(Player player)
	{
		if (!this.getLevel().isClientSide)
		{
			this.usingPlayers.add(player);
			this.updateUsingPlayerCount();
		}
	}

	@Override
	public void stopOpen(Player player)
	{
		if (!this.getLevel().isClientSide)
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
	public boolean wrenchCanRemove(Player player)
	{
		if (!this.permitsAccess(player.getGameProfile()))
		{
			IC2.sideProxy.messagePlayer(player, "This safe is owned by " + this.owner.getName());
			return false;
		} else if (!this.contentSlot.isEmpty())
		{
			IC2.sideProxy.messagePlayer(player, "Can't wrench non-empty safe");
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
	public Container getPrivilegedInventory(GameProfile accessor)
	{
		return (Container) (!this.permitsAccess(accessor) ? this : new DelegatingInventory(this)
		{
			@Override
			public int getContainerSize()
			{
				return TileEntityPersonalChest.this.contentSlot.size();
			}

			@Override
			public ItemStack getItem(int index)
			{
				return TileEntityPersonalChest.this.contentSlot.get(index);
			}

			@Override
			public ItemStack removeItem(int index, int amount)
			{
				ItemStack stack = this.getItem(index);
				if (StackUtil.isEmpty(stack))
				{
					return StackUtil.emptyStack;
				}

				if (amount >= StackUtil.getSize(stack))
				{
					this.setItem(index, StackUtil.emptyStack);
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
					this.setItem(index, stack);
				}

				return StackUtil.copyWithSize(stack, amount);
			}

			@Override
			public ItemStack removeItemNoUpdate(int index)
			{
				ItemStack ret = this.getItem(index);
				if (!StackUtil.isEmpty(ret))
				{
					this.setItem(index, StackUtil.emptyStack);
				}

				return ret;
			}

			@Override
			public void setItem(int index, ItemStack stack)
			{
				TileEntityPersonalChest.this.contentSlot.put(index, stack);
				this.setChanged();
			}

			@Override
			public int getMaxStackSize()
			{
				return TileEntityPersonalChest.this.contentSlot.getStackSizeLimit();
			}

			@Override
			public boolean canPlaceItem(int index, ItemStack stack)
			{
				return TileEntityPersonalChest.this.contentSlot.accepts(stack);
			}
		});
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
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!this.getLevel().isClientSide && !this.permitsAccess(player.getGameProfile()))
		{
			IC2.sideProxy.messagePlayer(player, "This safe is owned by " + this.getOwner().getName());
			return InteractionResult.FAIL;
		} else
		{
			return super.onActivated(player, hand, side, hit);
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		this.startOpen(player);
		return new DynamicContainer<>(
			Ic2ScreenHandlers.DYNAMIC_BE, syncId, player.getInventory(), this, GuiParser.parse(Util.getName(this.getBlockType()), this.getClass())
		)
		{
			public void removed(Player player)
			{
				this.base.stopOpen(player);
				super.removed(player);
			}
		};
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public void onScreenClosed(Player player)
	{
		this.stopOpen(player);
	}

	public float getLidAngle(float partialTicks)
	{
		return Util.lerp(this.prevLidAngle, this.lidAngle, partialTicks) / 10.0F;
	}
}
