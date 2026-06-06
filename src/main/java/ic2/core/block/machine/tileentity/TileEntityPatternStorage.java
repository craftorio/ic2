package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.block.machine.gui.GuiPatternStorage;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuIndex;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityPatternStorage extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IPatternStorage
{
	public final InvSlotConsumableId diskSlot;
	private final List<ItemStack> patterns = new ArrayList<>();
	public int index = 0;
	public int maxIndex;
	public ItemStack pattern;
	public double patternUu;
	public double patternEu;

	public TileEntityPatternStorage()
	{
		this.diskSlot = new InvSlotConsumableId(this, "SaveSlot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, ItemName.crystal_memory.getInstance());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.readContents(nbttagcompound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.writeContentsAsNbtList(nbt);
		return nbt;
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getWorld().isRemote)
		{
			NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
			this.readContents(nbt);
		}
	}

	@Override
	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self)
		{
			NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
			this.writeContentsAsNbtList(nbt);
		}

		return drop;
	}

	public void readContents(NBTTagCompound nbt)
	{
		NBTTagList patternList = nbt.getTagList("patterns", 10);

		for (int i = 0; i < patternList.tagCount(); i++)
		{
			NBTTagCompound contentTag = patternList.getCompoundTagAt(i);
			ItemStack Item = new ItemStack(contentTag);
			this.addPattern(Item);
		}

		this.refreshInfo();
	}

	private void writeContentsAsNbtList(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();

		for (ItemStack stack : this.patterns)
		{
			NBTTagCompound contentTag = new NBTTagCompound();
			stack.writeToNBT(contentTag);
			list.appendTag(contentTag);
		}

		nbt.setTag("patterns", list);
	}

	@Override
	public ContainerBase<TileEntityPatternStorage> getGuiContainer(EntityPlayer player)
	{
		return new ContainerPatternStorage(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiPatternStorage(new ContainerPatternStorage(player, this));
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		switch (event)
		{
			case 0:
				if (!this.patterns.isEmpty())
				{
					if (this.index <= 0)
					{
						this.index = this.patterns.size() - 1;
					} else
					{
						this.index--;
					}

					this.refreshInfo();
				}
				break;
			case 1:
				if (!this.patterns.isEmpty())
				{
					if (this.index >= this.patterns.size() - 1)
					{
						this.index = 0;
					} else
					{
						this.index++;
					}

					this.refreshInfo();
				}
				break;
			case 2:
				if (this.index >= 0 && this.index < this.patterns.size() && !this.diskSlot.isEmpty())
				{
					ItemStack crystalMemory = this.diskSlot.get();
					if (crystalMemory.getItem() instanceof ItemCrystalMemory)
					{
						((ItemCrystalMemory) crystalMemory.getItem()).writecontentsTag(crystalMemory, this.patterns.get(this.index));
					}
				}
				break;
			case 3:
				if (!this.diskSlot.isEmpty())
				{
					ItemStack crystalMemory = this.diskSlot.get();
					if (crystalMemory.getItem() instanceof ItemCrystalMemory)
					{
						ItemStack record = ((ItemCrystalMemory) crystalMemory.getItem()).readItemStack(crystalMemory);
						if (record != null)
						{
							this.addPattern(record);
						}
					}
				}
		}
	}

	public void refreshInfo()
	{
		if (this.index < 0 || this.index >= this.patterns.size())
		{
			this.index = 0;
		}

		this.maxIndex = this.patterns.size();
		if (this.patterns.isEmpty())
		{
			this.pattern = null;
		} else
		{
			this.pattern = this.patterns.get(this.index);
			this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
		}
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public boolean addPattern(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			throw new IllegalArgumentException("empty stack: " + StackUtil.toStringSafe(stack));
		}

		for (ItemStack pattern : this.patterns)
		{
			if (StackUtil.checkItemEquality(pattern, stack))
			{
				return false;
			}
		}

		this.patterns.add(stack);
		this.refreshInfo();
		return true;
	}

	@Override
	public List<ItemStack> getPatterns()
	{
		return this.patterns;
	}
}
