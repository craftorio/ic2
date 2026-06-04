package ic2.api.energy.prefab;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

abstract class BasicEnergyTile implements ILocatable, IEnergyTile
{
	private final Object locationProvider;

	protected World world;

	protected BlockPos pos;

	protected double capacity;

	protected double energyStored;

	protected boolean addedToEnet;

	protected BasicEnergyTile(TileEntity parent, double capacity)
	{
		this((Object) parent, capacity);
	}

	protected BasicEnergyTile(ILocatable parent, double capacity)
	{
		this((Object) parent, capacity);
	}
  
	private BasicEnergyTile(Object locationProvider, double capacity)
	{
		this.locationProvider = locationProvider;
		this.capacity = capacity;
	}

	protected BasicEnergyTile(World world, BlockPos pos, double capacity)
	{
		if (world == null)
			throw new NullPointerException("null world");
		if (pos == null)
			throw new NullPointerException("null pos");
		this.locationProvider = null;
		this.world = world;
		this.pos = pos;
		this.capacity = capacity;
	}

	public void update()
	{
		if (!this.addedToEnet)
			onLoad();
	}

	public void onLoad()
	{
		if (!this.addedToEnet &&
			!(getWorldObj()).isRemote &&
			Info.isIc2Available())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnet = true;
		}
	}

	public void invalidate()
	{
		onChunkUnload();
	}

	public void onChunkUnload()
	{
		if (this.addedToEnet &&
			!(getWorldObj()).isRemote &&
			Info.isIc2Available())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnet = false;
		}
	}

	public void readFromNBT(NBTTagCompound tag)
	{
		NBTTagCompound data = tag.getCompoundTag(getNbtTagName());
		setEnergyStored(data.getDouble("energy"));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("energy", getEnergyStored());
		tag.setTag(getNbtTagName(), data);
		return tag;
	}

	public double getCapacity()
	{
		return this.capacity;
	}

	public void setCapacity(double capacity)
	{
		this.capacity = capacity;
	}

	public double getEnergyStored()
	{
		return this.energyStored;
	}

	public void setEnergyStored(double amount)
	{
		this.energyStored = amount;
	}

	public double addEnergy(double amount)
	{
		if ((getWorldObj()).isRemote)
			return 0.0D;
		double energyStored = getEnergyStored();
		double capacity = getCapacity();
		if (amount > capacity - energyStored)
			amount = capacity - energyStored;
		setEnergyStored(energyStored + amount);
		return amount;
	}

	public boolean canUseEnergy(double amount)
	{
		return (getEnergyStored() >= amount);
	}

	public boolean useEnergy(double amount)
	{
		if (!canUseEnergy(amount) || (getWorldObj()).isRemote)
			return false;
		setEnergyStored(getEnergyStored() - amount);
		return true;
	}

	public boolean charge(ItemStack stack)
	{
		if (stack == null || !Info.isIc2Available() || (getWorldObj()).isRemote)
			return false;
		double energyStored = getEnergyStored();
		double amount = ElectricItem.manager.charge(stack, energyStored, Math.max(getSinkTier(), getSourceTier()), false, false);
		setEnergyStored(energyStored - amount);
		return (amount > 0.0D);
	}

	public boolean discharge(ItemStack stack, double limit)
	{
		if (stack == null || !Info.isIc2Available() || (getWorldObj()).isRemote)
			return false;
		double energyStored = getEnergyStored();
		double amount = getCapacity() - energyStored;
		if (amount <= 0.0D)
			return false;
		if (limit > 0.0D && limit < amount)
			amount = limit;
		amount = ElectricItem.manager.discharge(stack, amount, Math.max(getSinkTier(), getSourceTier()), (limit > 0.0D), true, false);
		setEnergyStored(energyStored + amount);
		return (amount > 0.0D);
	}

	public World getWorldObj()
	{
		if (this.world == null)
			initLocation();
		return this.world;
	}

	public BlockPos getPosition()
	{
		if (this.pos == null)
			initLocation();
		return this.pos;
	}

	private void initLocation()
	{
		if (this.locationProvider instanceof ILocatable)
		{
			ILocatable provider = (ILocatable) this.locationProvider;
			this.world = provider.getWorldObj();
			this.pos = provider.getPosition();
		} else if (this.locationProvider instanceof TileEntity)
		{
			TileEntity provider = (TileEntity) this.locationProvider;
			this.world = provider.getWorld();
			this.pos = provider.getPos();
		} else
		{
			throw new IllegalStateException("no/incompatible location provider");
		}
	}

	protected abstract String getNbtTagName();

	protected int getSinkTier()
	{
		return 0;
	}

	protected int getSourceTier()
	{
		return 0;
	}
}
