package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import ic2.api.item.ElectricItem;
import ic2.api.item.ICustomDamageItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.upgrade.IFullUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.state.IIdProvider;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.init.Localization;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUpgradeModule extends ItemMulti<ItemUpgradeModule.UpgradeType> implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo
{
	private static final DecimalFormat decimalformat = new DecimalFormat("0.##");
	private static final List<StackUtil.AdjacentInv> emptyInvList = Collections.emptyList();
	private static final List<LiquidUtil.AdjacentFluidHandler> emptyFhList = Collections.emptyList();

	public ItemUpgradeModule()
	{
		super(ItemName.upgrade, ItemUpgradeModule.UpgradeType.class);
		this.setHasSubtypes(true);

		for (ItemUpgradeModule.UpgradeType type : ItemUpgradeModule.UpgradeType.values())
		{
			UpgradeRegistry.register(new ItemStack(this, 1, type.getId()));
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(final ItemName name)
	{
		ModelLoader.setCustomMeshDefinition(
			this,
			new ItemMeshDefinition()
			{
				public ModelResourceLocation getModelLocation(ItemStack stack)
				{
					ItemUpgradeModule.UpgradeType type = ItemUpgradeModule.this.getType(stack);
					if (type == null)
					{
						return new ModelResourceLocation("builtin/missing", "missing");
					}

					EnumFacing dir;
					return type.directional && (dir = ItemUpgradeModule.getDirection(stack)) != null
						? ItemIC2.getModelLocation(name, type.getName() + '_' + dir.getName())
						: ItemIC2.getModelLocation(name, type.getName());
				}
			}
		);

		for (ItemUpgradeModule.UpgradeType type : this.typeProperty.getAllowedValues())
		{
			ModelBakery.registerItemVariants(this, new ResourceLocation[] { getModelLocation(name, type.getName()) });
			if (type.directional)
			{
				for (EnumFacing dir : EnumFacing.VALUES)
				{
					ModelBakery.registerItemVariants(this, new ResourceLocation[] { getModelLocation(name, type.getName() + '_' + dir.getName()) });
				}
			}
		}
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add("Machine Upgrade");
		return info;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, world, tooltip, advanced);
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type != null)
		{
			switch (type)
			{
				case overclocker:
					tooltip.add(
						Localization.translate(
							"ic2.tooltip.upgrade.overclocker.time",
							decimalformat.format(100.0 * Math.pow(this.getProcessTimeMultiplier(stack, null), StackUtil.getSize(stack)))
						)
					);
					tooltip.add(
						Localization.translate(
							"ic2.tooltip.upgrade.overclocker.power",
							decimalformat.format(100.0 * Math.pow(this.getEnergyDemandMultiplier(stack, null), StackUtil.getSize(stack)))
						)
					);
					break;
				case transformer:
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.transformer", this.getExtraTier(stack, null) * StackUtil.getSize(stack)));
					break;
				case energy_storage:
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.storage", this.getExtraEnergyStorage(stack, null) * StackUtil.getSize(stack)));
					break;
				case ejector:
				case advanced_ejector:
				{
					String side = getSideName(stack);
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
					break;
				}
				case pulling:
				case advanced_pulling:
				{
					String side = getSideName(stack);
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
					break;
				}
				case fluid_ejector:
				{
					String side = getSideName(stack);
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(side)));
					break;
				}
				case fluid_pulling:
				{
					String side = getSideName(stack);
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(side)));
					break;
				}
				case redstone_inverter:
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.redstone"));
					break;
				case remote_interface:
					tooltip.add(Localization.translate("ic2.tooltip.upgrade.remote_interface", StackUtil.getSize(stack)));
			}
		}
	}

	private static String getSideName(ItemStack stack)
	{
		EnumFacing dir = getDirection(stack);
		if (dir == null)
		{
			return "ic2.tooltip.upgrade.ejector.anyside";
		}

		switch (dir)
		{
			case WEST:
				return "ic2.dir.west";
			case EAST:
				return "ic2.dir.east";
			case DOWN:
				return "ic2.dir.bottom";
			case UP:
				return "ic2.dir.top";
			case NORTH:
				return "ic2.dir.north";
			case SOUTH:
				return "ic2.dir.south";
			default:
				throw new RuntimeException("invalid dir: " + dir);
		}
	}

	@Override
	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset
	)
	{
		ItemStack stack = StackUtil.get(player, hand);
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return EnumActionResult.PASS;
		}

		if (type.directional)
		{
			int dir = 1 + side.ordinal();
			NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
			if (nbtData.getByte("dir") == dir)
			{
				nbtData.setByte("dir", (byte) 0);
			} else
			{
				nbtData.setByte("dir", (byte) dir);
			}

			if (IC2.platform.isRendering())
			{
				switch (type)
				{
					case ejector:
					case advanced_ejector:
						IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))));
						break;
					case pulling:
					case advanced_pulling:
						IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))));
						break;
					case fluid_ejector:
						IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))));
						break;
					case fluid_pulling:
						IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))));
				}
			}

			return EnumActionResult.SUCCESS;
		} else
		{
			return EnumActionResult.PASS;
		}
	}

	public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type != null)
		{
			switch (type)
			{
				case advanced_ejector:
				case advanced_pulling:
					if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof DynamicHandHeldContainer)
					{
						HandHeldInventory base = (HandHeldInventory) ((DynamicHandHeldContainer) player.openContainer).base;
						if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack))
						{
							base.saveAsThrown(stack);
							player.closeScreen();
						}
					}
			}
		}

		return true;
	}

	@Override
	public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return false;
		}

		switch (type)
		{
			case overclocker:
				return types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable);
			case transformer:
				return types.contains(UpgradableProperty.Transformer);
			case energy_storage:
				return types.contains(UpgradableProperty.EnergyStorage);
			case ejector:
			case advanced_ejector:
				return types.contains(UpgradableProperty.ItemProducing);
			case pulling:
			case advanced_pulling:
				return types.contains(UpgradableProperty.ItemConsuming);
			case fluid_ejector:
				return types.contains(UpgradableProperty.FluidProducing);
			case fluid_pulling:
				return types.contains(UpgradableProperty.FluidConsuming);
			case redstone_inverter:
				return types.contains(UpgradableProperty.RedstoneSensitive);
			case remote_interface:
				return types.contains(UpgradableProperty.RemotelyAccessible);
			default:
				return false;
		}
	}

	@Override
	public int getAugmentation(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return 0;
		}

		switch (type)
		{
			case overclocker:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent)
	{
		return 0;
	}

	@Override
	public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return 1.0;
		}

		switch (type)
		{
			case overclocker:
				return 0.7;
			default:
				return 1.0;
		}
	}

	@Override
	public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent)
	{
		return 0;
	}

	@Override
	public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return 1.0;
		}

		switch (type)
		{
			case overclocker:
				return 1.6;
			default:
				return 1.0;
		}
	}

	@Override
	public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return 0;
		}

		switch (type)
		{
			case energy_storage:
				return 10000;
			default:
				return 0;
		}
	}

	@Override
	public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		return 1.0;
	}

	@Override
	public int getExtraTier(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return 0;
		}

		switch (type)
		{
			case transformer:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return false;
		}

		switch (type)
		{
			case redstone_inverter:
				return true;
			default:
				return false;
		}
	}

	@Override
	public int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return externalInput;
		}

		switch (type)
		{
			case redstone_inverter:
				return 15 - externalInput;
			default:
				return externalInput;
		}
	}

	@Override
	public int getRangeAmplification(ItemStack stack, IUpgradableBlock parent, int existingRange)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return existingRange;
		}

		switch (type)
		{
			case remote_interface:
				return existingRange << 1;
			default:
				return existingRange;
		}
	}

	@Override
	public boolean onTick(ItemStack stack, IUpgradableBlock parent)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return false;
		}

		int size = StackUtil.getSize(stack);
		TileEntity te = (TileEntity) parent;
		boolean ret = false;
		switch (type)
		{
			case ejector:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
				{
					StackUtil.transfer(te, inv.te, inv.dir, amount);
				}
				break;
			}
			case advanced_ejector:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
				{
					StackUtil.transfer(te, inv.te, inv.dir, amount, stackChecker(stack));
				}
				break;
			}
			case pulling:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
				{
					StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount);
				}
				break;
			}
			case advanced_pulling:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (StackUtil.AdjacentInv inv : getTargetInventories(stack, te))
				{
					StackUtil.transfer(inv.te, te, inv.dir.getOpposite(), amount, stackChecker(stack));
				}
				break;
			}
			case fluid_ejector:
			{
				if (!LiquidUtil.isFluidTile(te, null))
				{
					return false;
				}

				int amount = (int) (50.0 * Math.pow(4.0, Math.min(4, size - 1)));

				for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
				{
					LiquidUtil.transfer(te, fh.dir, fh.handler, amount);
				}
				break;
			}
			case fluid_pulling:
			{
				if (!LiquidUtil.isFluidTile(te, null))
				{
					return false;
				}

				int amount = (int) (50.0 * Math.pow(4.0, Math.min(4, size - 1)));

				for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
				{
					LiquidUtil.transfer(fh.handler, fh.dir.getOpposite(), te, amount);
				}
				break;
			}
			default:
				return false;
		}

		return ret;
	}

	private static Predicate<ItemStack> stackChecker(final ItemStack stack)
	{
		return new Predicate<ItemStack>()
		{
			private boolean hasInitialised = false;
			private Set<ItemStack> filters;
			private Settings meta;
			private Settings damage;
			private Settings energy;
			private NbtSettings nbt;

			private void initalise()
			{
				assert !this.hasInitialised;
				NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
				this.filters = this.getFilterStacks(tag);
				this.meta = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "meta"));
				this.damage = null;
				this.nbt = NbtSettings.getFromNBT(HandHeldAdvancedUpgrade.getTag(tag, "nbt").getByte("type"));
				this.energy = new Settings(HandHeldAdvancedUpgrade.getTag(tag, "energy"));
				this.hasInitialised = true;
			}

			private Set<ItemStack> getFilterStacks(NBTTagCompound nbt)
			{
				Set<ItemStack> ret = new HashSet<>();
				NBTTagList contentList = nbt.getTagList("Items", 10);

				for (int tag = 0; tag < contentList.tagCount(); tag++)
				{
					NBTTagCompound slotNbt = contentList.getCompoundTagAt(tag);
					int slot = slotNbt.getByte("Slot");
					if (slot >= 0 && slot < 9)
					{
						ItemStack filter = new ItemStack(slotNbt);
						if (!StackUtil.isEmpty(filter))
						{
							ret.add(filter);
						}
					}
				}

				return ret;
			}

			private boolean checkMeta(ItemStack stackx, ItemStack filter)
			{
				assert this.meta.active;
				assert this.meta.comparison == ComparisonType.DIRECT;
				return stack.getMetadata() == filter.getMetadata();
			}

			private boolean checkDamage(ItemStack stackx, ItemStack filter, boolean customStack)
			{
				assert this.damage.active;
				assert this.damage.comparison == ComparisonType.DIRECT;
				return customStack && filter.getItem() instanceof ICustomDamageItem
					? ((ICustomDamageItem) stack.getItem()).getCustomDamage(stack) == ((ICustomDamageItem) filter.getItem()).getCustomDamage(filter)
					: filter.getItemDamage() == stack.getItemDamage();
			}

			private boolean checkNBT(ItemStack stackx, ItemStack filter)
			{
				switch (this.nbt)
				{
					case IGNORED:
						return true;
					case FUZZY:
						return StackUtil.checkNbtEquality(stack.getTagCompound(), filter.getTagCompound());
					case EXACT:
						return StackUtil.checkNbtEqualityStrict(stack, filter);
					default:
						throw new IllegalStateException("Unexpected NBT state: " + this.nbt);
				}
			}

			private boolean checkEnergy(ItemStack stackx, ItemStack filter)
			{
				assert this.energy.active;
				assert this.energy.comparison == ComparisonType.DIRECT;
				return filter.getItem() instanceof IElectricItem
					&& Util.isSimilar(ElectricItem.manager.getCharge(stack), ElectricItem.manager.getCharge(filter));
			}

			public boolean apply(ItemStack stackx)
			{
				if (!this.hasInitialised)
				{
					this.initalise();
				}

				boolean checkMeta;
				if (!this.meta.comparison.ignoreFilters())
				{
					if (!this.meta.doComparison(stack.getMetadata()))
					{
						return false;
					}

					checkMeta = false;
				} else
				{
					checkMeta = this.meta.active;
				}

				boolean customStack = stack.getItem() instanceof ICustomDamageItem;
				boolean checkDamage = false;
				boolean checkEnergy;
				if (!this.energy.comparison.ignoreFilters())
				{
					if (!(stack.getItem() instanceof IElectricItem) || !this.energy.doComparison((int) ElectricItem.manager.getCharge(stack)))
					{
						return false;
					}

					checkEnergy = false;
				} else
				{
					checkEnergy = this.energy.active;
					if (checkEnergy && !(stack.getItem() instanceof IElectricItem))
					{
						return false;
					}
				}

				for (ItemStack filter : this.filters)
				{
					if (filter.getItem() == stack.getItem()
						&& (!checkMeta || this.checkMeta(stack, filter))
						&& (!checkDamage || this.checkDamage(stack, filter, customStack))
						&& this.checkNBT(stack, filter)
						&& (!checkEnergy || this.checkEnergy(stack, filter)))
					{
						return true;
					}
				}

				return this.filters.isEmpty() && this.meta.active && !checkMeta && this.energy.active && !checkEnergy;
			}
		};
	}

	private static List<StackUtil.AdjacentInv> getTargetInventories(ItemStack stack, TileEntity parent)
	{
		EnumFacing dir = getDirection(stack);
		if (dir == null)
		{
			return StackUtil.getAdjacentInventories(parent);
		}

		StackUtil.AdjacentInv inv = StackUtil.getAdjacentInventory(parent, dir);
		return inv == null ? emptyInvList : Collections.singletonList(inv);
	}

	private static List<LiquidUtil.AdjacentFluidHandler> getTargetFluidHandlers(ItemStack stack, TileEntity parent)
	{
		EnumFacing dir = getDirection(stack);
		if (dir == null)
		{
			return LiquidUtil.getAdjacentHandlers(parent);
		}

		LiquidUtil.AdjacentFluidHandler fh = LiquidUtil.getAdjacentHandler(parent, dir);
		return fh == null ? emptyFhList : Collections.singletonList(fh);
	}

	@Override
	public Collection<ItemStack> onProcessEnd(ItemStack stack, IUpgradableBlock parent, Collection<ItemStack> output)
	{
		return output;
	}

	@Override
	public IHasGui getInventory(EntityPlayer player, ItemStack stack)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return null;
		}

		switch (type)
		{
			case advanced_ejector:
			case advanced_pulling:
				return new HandHeldAdvancedUpgrade(player, stack);
			default:
				return null;
		}
	}

	@Override
	public IHasGui getSubInventory(EntityPlayer player, ItemStack stack, int ID)
	{
		ItemUpgradeModule.UpgradeType type = this.getType(stack);
		if (type == null)
		{
			return null;
		}

		switch (type)
		{
			case advanced_ejector:
			case advanced_pulling:
				return HandHeldAdvancedUpgrade.delegate(player, stack, ID);
			default:
				return null;
		}
	}

	private static EnumFacing getDirection(ItemStack stack)
	{
		int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
		return rawDir >= 1 && rawDir <= 6 ? EnumFacing.VALUES[rawDir - 1] : null;
	}

	public enum UpgradeType implements IIdProvider
	{
		overclocker(false),
		transformer(false),
		energy_storage(false),
		redstone_inverter(false),
		ejector(true),
		@NotClassic
		advanced_ejector(true),
		pulling(true),
		@NotClassic
		advanced_pulling(true),
		fluid_ejector(true),
		fluid_pulling(true),
		@NotClassic
		remote_interface(false);

		public final boolean directional;

		UpgradeType(boolean directional)
		{
			this.directional = directional;
		}

		@Override
		public String getName()
		{
			return this.name();
		}

		@Override
		public int getId()
		{
			return this.ordinal();
		}
	}
}
