package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Redstone extends TileEntityComponent
{
	private int redstoneInput;
	private Set<Redstone.IRedstoneChangeHandler> changeSubscribers;
	private Set<Redstone.IRedstoneModifier> modifiers;
	private Redstone.LinkHandler outboundLink;

	public Redstone(TileEntityBlock parent)
	{
		super(parent);
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		this.update();
	}

	@Override
	public void onUnloaded()
	{
		this.unlinkOutbound();
		this.unlinkInbound();
		super.onUnloaded();
	}

	@Override
	public void onNeighborChange(Block srcBlock, BlockPos neighborPos)
	{
		super.onNeighborChange(srcBlock, neighborPos);
		this.update();
	}

	public void update()
	{
		World world = this.parent.getWorld();
		if (world != null)
		{
			int input = world.isBlockIndirectlyGettingPowered(this.parent.getPos());
			if (this.modifiers != null)
			{
				for (Redstone.IRedstoneModifier modifier : this.modifiers)
				{
					input = modifier.getRedstoneInput(input);
				}
			}

			if (input != this.redstoneInput)
			{
				this.redstoneInput = input;
				if (this.changeSubscribers != null)
				{
					for (Redstone.IRedstoneChangeHandler subscriber : this.changeSubscribers)
					{
						subscriber.onRedstoneChange(input);
					}
				}
			}
		}
	}

	public int getRedstoneInput()
	{
		return this.redstoneInput;
	}

	public boolean hasRedstoneInput()
	{
		return this.redstoneInput > 0;
	}

	public void subscribe(Redstone.IRedstoneChangeHandler handler)
	{
		if (handler == null)
		{
			throw new NullPointerException("null handler");
		}

		if (this.changeSubscribers == null)
		{
			this.changeSubscribers = new HashSet<>();
		}

		this.changeSubscribers.add(handler);
	}

	public void unsubscribe(Redstone.IRedstoneChangeHandler handler)
	{
		if (handler == null)
		{
			throw new NullPointerException("null handler");
		}

		if (this.changeSubscribers != null)
		{
			this.changeSubscribers.remove(handler);
			if (this.changeSubscribers.isEmpty())
			{
				this.changeSubscribers = null;
			}
		}
	}

	public void addRedstoneModifier(Redstone.IRedstoneModifier modifier)
	{
		if (this.modifiers == null)
		{
			this.modifiers = new HashSet<>();
		}

		this.modifiers.add(modifier);
	}

	public void addRedstoneModifiers(Collection<Redstone.IRedstoneModifier> modifiers)
	{
		if (this.modifiers == null)
		{
			this.modifiers = new HashSet<>(modifiers);
		} else
		{
			this.modifiers.addAll(modifiers);
		}
	}

	public void removeRedstoneModifier(Redstone.IRedstoneModifier modifier)
	{
		if (this.modifiers != null)
		{
			this.modifiers.remove(modifier);
		}
	}

	public void removeRedstoneModifiers(Collection<Redstone.IRedstoneModifier> modifiers)
	{
		if (this.modifiers != null)
		{
			this.modifiers.removeAll(modifiers);
			if (this.modifiers.isEmpty())
			{
				this.modifiers = null;
			}
		}
	}

	public boolean isLinked()
	{
		return this.outboundLink != null;
	}

	public Redstone getLinkReceiver()
	{
		return this.outboundLink != null ? this.outboundLink.receiver : null;
	}

	public Collection<Redstone> getLinkedOrigins()
	{
		if (this.modifiers == null)
		{
			return Collections.emptyList();
		}

		List<Redstone> ret = new ArrayList<>(this.modifiers.size());

		for (Redstone.IRedstoneModifier modifier : this.modifiers)
		{
			if (modifier instanceof Redstone.LinkHandler)
			{
				ret.add(((Redstone.LinkHandler) modifier).origin);
			}
		}

		return Collections.unmodifiableList(ret);
	}

	public void linkTo(Redstone receiver)
	{
		if (receiver == null)
		{
			throw new NullPointerException("null receiver");
		}

		if (this.outboundLink != null)
		{
			if (this.outboundLink.receiver != receiver)
			{
				throw new IllegalStateException("already linked");
			}
		} else
		{
			this.outboundLink = new Redstone.LinkHandler(this, receiver);
			this.outboundLink.receiver.addRedstoneModifier(this.outboundLink);
			this.subscribe(this.outboundLink);
			receiver.update();
		}
	}

	public void unlinkOutbound()
	{
		if (this.outboundLink != null)
		{
			this.outboundLink.receiver.removeRedstoneModifier(this.outboundLink);
			this.unsubscribe(this.outboundLink);
			this.outboundLink = null;
		}
	}

	public void unlinkInbound()
	{
		for (Redstone origin : this.getLinkedOrigins())
		{
			origin.unlinkOutbound();
		}
	}

	public interface IRedstoneChangeHandler
	{
		void onRedstoneChange(int var1);
	}

	public interface IRedstoneModifier
	{
		int getRedstoneInput(int var1);
	}

	private static class LinkHandler implements Redstone.IRedstoneChangeHandler, Redstone.IRedstoneModifier
	{
		private final Redstone origin;
		private final Redstone receiver;

		public LinkHandler(Redstone origin, Redstone receiver)
		{
			this.origin = origin;
			this.receiver = receiver;
		}

		@Override
		public void onRedstoneChange(int newLevel)
		{
			this.receiver.update();
		}

		@Override
		public int getRedstoneInput(int redstoneInput)
		{
			return Math.max(redstoneInput, this.origin.redstoneInput);
		}
	}
}
