package ic2.core.block.steam;

import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.network.GrowingBuffer;
import ic2.core.recipe.dynamic.DynamicRecipeManager;

import java.io.DataInput;
import java.io.IOException;
import java.util.function.BooleanSupplier;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class ProcessingComponent extends TileEntityComponent
{
	protected final DynamicRecipeManager recipeManager;
	protected final TileEntityComponent[] lookup;
	protected final BooleanSupplier canOperateCallable;
	protected final int tickRate = 20;
	protected int updateTicker;
	protected int progress = 0;
	protected final int operationLength = 200;
	protected AudioSource audioSource;
	protected String startSoundFile = null;
	protected String interruptSoundFile = null;
	protected static final int EventStart = 0;
	protected static final int EventInterrupt = 1;
	protected static final int EventFinish = 2;
	protected static final int EventStop = 3;

	public static ProcessingComponent asKineticMachine(TileEntityBlock parent, DynamicRecipeManager recipeManager)
	{
		return new ProcessingComponent(parent, recipeManager, () -> parent.getWorld().isRemote);
	}

	public ProcessingComponent(TileEntityBlock parent, DynamicRecipeManager recipeManager, BooleanSupplier canOperateCallable)
	{
		super(parent);
		this.recipeManager = recipeManager;
		this.lookup = null;
		this.updateTicker = IC2.random.nextInt(this.tickRate);
		this.canOperateCallable = canOperateCallable;
	}

	@Override
	public void readFromNbt(NBTTagCompound nbt)
	{
		this.progress = nbt.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNbt()
	{
		NBTTagCompound ret = new NBTTagCompound();
		ret.setInteger("progress", this.progress);
		return ret;
	}

	@Override
	public void onUnloaded()
	{
		super.onUnloaded();
		if (this.parent.getWorld().isRemote && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this.parent);
			this.audioSource = null;
		}
	}

	@Override
	public void onContainerUpdate(EntityPlayerMP player)
	{
		GrowingBuffer buffer = new GrowingBuffer(16);
		buffer.writeInt(this.progress);
		buffer.flip();
		this.setNetworkUpdate(player, buffer);
	}

	@Override
	public void onNetworkUpdate(DataInput is) throws IOException
	{
		this.progress = is.readInt();
	}

	@Override
	public boolean enableWorldTick()
	{
		return !this.parent.getWorld().isRemote;
	}

	@Override
	public void onWorldTick()
	{
		if (this.updateTicker++ % this.tickRate == 0)
		{
			boolean needsInventoryUpdate = false;
			if (!this.hasValidInput() && this.searchForValidInput())
			{
				needsInventoryUpdate = true;
			}

			int power = 0;
			if (this.canOperate())
			{
				this.parent.setActive(true);
				if (this.progress == 0)
				{
					IC2.network.get(true).initiateTileEntityEvent(this.parent, 0, true);
				}

				if (this.progress < this.operationLength)
				{
					this.progress += 5 * power;
				}

				if (this.progress >= this.operationLength)
				{
					this.operateOnce();
					needsInventoryUpdate = true;
					this.progress = 0;
					IC2.network.get(true).initiateTileEntityEvent(this.parent, 2, true);
				}
			} else
			{
				if (this.parent.getActive())
				{
					if (this.progress != 0)
					{
						IC2.network.get(true).initiateTileEntityEvent(this.parent, 1, true);
					} else
					{
						IC2.network.get(true).initiateTileEntityEvent(this.parent, 3, true);
					}
				}

				if (!this.hasValidInput())
				{
					this.progress = 0;
				}

				this.parent.setActive(false);
			}

			if (needsInventoryUpdate)
			{
				this.parent.markDirty();
			}
		}
	}

	public boolean canOperate()
	{
		return this.canOperateCallable.getAsBoolean();
	}

	protected boolean hasValidInput()
	{
		return false;
	}

	protected boolean searchForValidInput()
	{
		return false;
	}

	protected void operateOnce()
	{
	}

	public float getGuiProgress()
	{
		return this.progress != 0 && this.operationLength != 0 ? (float) this.progress / this.operationLength : 0.0F;
	}
}
