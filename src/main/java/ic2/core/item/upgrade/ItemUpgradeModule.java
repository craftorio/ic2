package ic2.core.item.upgrade;

import com.google.common.base.Predicate;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.upgrade.IFullUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.init.Localization;
import ic2.core.item.EnvItemHandler;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.tool.HandHeldInventory;
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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class ItemUpgradeModule extends Item implements IFullUpgrade, IHandHeldSubInventory, IItemHudInfo
{
	private static final DecimalFormat decimalformat = new DecimalFormat("0.##");
	private static final List<EnvItemHandler.AdjacentInventory> emptyInvList = Collections.emptyList();
	private static final List<LiquidUtil.AdjacentFluidHandler> emptyFhList = Collections.emptyList();
	public final ItemUpgradeModule.UpgradeType type;

	public ItemUpgradeModule(Properties settings, ItemUpgradeModule.UpgradeType type)
	{
		super(settings);
		this.type = type;
		IC2.envProxy.runAfterRegistryInit(() -> UpgradeRegistry.register(new ItemStack(this, 1)));
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add("Machine Upgrade");
		return info;
	}

	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		switch (this.type)
		{
			case overclocker:
				tooltip.add(
					Component.translatable(
							"ic2.tooltip.upgrade.overclocker.time",
							new Object[] { decimalformat.format(100.0 * Math.pow(this.getProcessTimeMultiplier(stack, null), StackUtil.getSize(stack))) }
						)
						.withStyle(ChatFormatting.GRAY)
				);
				tooltip.add(
					Component.translatable(
							"ic2.tooltip.upgrade.overclocker.power",
							new Object[] { decimalformat.format(100.0 * Math.pow(this.getEnergyDemandMultiplier(stack, null), StackUtil.getSize(stack))) }
						)
						.withStyle(ChatFormatting.GRAY)
				);
				break;
			case transformer:
				tooltip.add(
					Component.translatable("ic2.tooltip.upgrade.transformer", new Object[] { this.getExtraTier(stack, null) * StackUtil.getSize(stack) })
						.withStyle(ChatFormatting.GRAY)
				);
				break;
			case energy_storage:
				tooltip.add(
					Component.translatable("ic2.tooltip.upgrade.storage", new Object[] { this.getExtraEnergyStorage(stack, null) * StackUtil.getSize(stack) })
						.withStyle(ChatFormatting.GRAY)
				);
				break;
			case ejector:
			case advanced_ejector:
			case fluid_ejector:
			{
				String side = getSideName(stack);
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.ejector", new Object[] { Localization.translate(side) }).withStyle(ChatFormatting.GRAY));
				break;
			}
			case pulling:
			case advanced_pulling:
			case fluid_pulling:
			{
				String side = getSideName(stack);
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.pulling", new Object[] { Localization.translate(side) }).withStyle(ChatFormatting.GRAY));
				break;
			}
			case redstone_inverter:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.redstone").withStyle(ChatFormatting.GRAY));
				break;
			case remote_interface:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.remote_interface", new Object[] { StackUtil.getSize(stack) }).withStyle(ChatFormatting.GRAY));
		}
	}

	private static String getSideName(ItemStack stack)
	{
		Direction dir = getDirection(stack);
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

	public InteractionResult useOn(UseOnContext context)
	{
		ItemStack stack = context.getItemInHand();
		if (this.type.directional)
		{
			int dir = 1 + context.getClickedFace().ordinal();
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			if (nbtData.getByte("dir") == dir)
			{
				nbtData.putByte("dir", (byte) 0);
			} else
			{
				nbtData.putByte("dir", (byte) dir);
			}

			if (context.getLevel().isClientSide())
			{
				switch (this.type)
				{
					case ejector:
					case advanced_ejector:
						IC2.sideProxy
							.messagePlayer(context.getPlayer(), Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))));
						break;
					case fluid_ejector:
						IC2.sideProxy
							.messagePlayer(context.getPlayer(), Localization.translate("ic2.tooltip.upgrade.ejector", Localization.translate(getSideName(stack))));
						break;
					case pulling:
					case advanced_pulling:
						IC2.sideProxy
							.messagePlayer(context.getPlayer(), Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))));
						break;
					case fluid_pulling:
						IC2.sideProxy
							.messagePlayer(context.getPlayer(), Localization.translate("ic2.tooltip.upgrade.pulling", Localization.translate(getSideName(stack))));
				}
			}

			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		switch (this.type)
		{
			case advanced_ejector:
			case advanced_pulling:
				if (!world.isClientSide)
				{
					this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
				}

				return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
			default:
				return new InteractionResultHolder(InteractionResult.PASS, stack);
		}
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		switch (this.type)
		{
			case advanced_ejector:
			case advanced_pulling:
				if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof DynamicHandHeldContainer)
				{
					HandHeldInventory base = (HandHeldInventory) ((DynamicHandHeldContainer) player.containerMenu).base;
					if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack))
					{
						base.saveAsThrown(stack);
						((ServerPlayer) player).closeContainer();
					}
				}
			default:
				return true;
		}
	}

	@Override
	public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types)
	{
		switch (this.type)
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
			case fluid_ejector:
				return types.contains(UpgradableProperty.FluidProducing);
			case pulling:
			case advanced_pulling:
				return types.contains(UpgradableProperty.ItemConsuming);
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		switch (this.type)
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
		int size = StackUtil.getSize(stack);
		BlockEntity te = (BlockEntity) parent;
		boolean ret = false;
		switch (this.type)
		{
			case ejector:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(te, inv.getSide()), inv, amount);
				}
				break;
			}
			case advanced_ejector:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(te, inv.getSide()), inv, amount, stackChecker(stack));
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
			case pulling:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(inv, StackUtil.ENV.wrapInventory(te, inv.getSide()), amount);
				}
				break;
			}
			case advanced_pulling:
			{
				int amount = (int) Math.pow(4.0, Math.min(4, size - 1));

				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(inv, StackUtil.ENV.wrapInventory(te, inv.getSide()), amount, stackChecker(stack));
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

	private static Predicate<ItemStack> stackChecker(ItemStack stack)
	{
		return new Predicate<ItemStack>()
		{
			private boolean hasInitialised = false;
			private Set<ItemStack> filters;
			private UpgradeSettings damage;
			private UpgradeSettings energy;
			private NbtSettings nbt;

			private void initalise()
			{
				assert !this.hasInitialised;
				CompoundTag tag = StackUtil.getOrCreateNbtData(stack);
				this.filters = this.getFilterStacks(tag);
				this.damage = null;
				this.nbt = NbtSettings.getFromNBT(HandHeldAdvancedUpgrade.getTag(tag, "nbt").getByte("type"));
				this.energy = new UpgradeSettings(HandHeldAdvancedUpgrade.getTag(tag, "energy"));
				this.hasInitialised = true;
			}

			private Set<ItemStack> getFilterStacks(CompoundTag nbt)
			{
				Set<ItemStack> ret = new HashSet<>();
				ListTag contentList = nbt.getList("Items", 10);

				for (int tag = 0; tag < contentList.size(); tag++)
				{
					CompoundTag slotNbt = contentList.getCompound(tag);
					int slot = slotNbt.getByte("Slot");
					if (slot >= 0 && slot < 9)
					{
						ItemStack filter = ItemStack.of(slotNbt);
						if (!StackUtil.isEmpty(filter))
						{
							ret.add(filter);
						}
					}
				}

				return ret;
			}

			private boolean checkDamage(ItemStack stack, ItemStack filter)
			{
				assert this.damage.active;
				assert this.damage.comparison == ComparisonType.DIRECT;
				return filter.getDamageValue() == stack.getDamageValue();
			}

			private boolean checkNBT(ItemStack stack, ItemStack filter)
			{
				switch (this.nbt)
				{
					case IGNORED:
						return true;
					case FUZZY:
						return StackUtil.checkNbtEquality(stack.getTag(), filter.getTag());
					case EXACT:
						return StackUtil.checkNbtEqualityStrict(stack, filter);
					default:
						throw new IllegalStateException("Unexpected NBT state: " + this.nbt);
				}
			}

			private boolean checkEnergy(ItemStack stack, ItemStack filter)
			{
				assert this.energy.active;
				assert this.energy.comparison == ComparisonType.DIRECT;
				return filter.getItem() instanceof IElectricItem && Util.isSimilar(ElectricItem.manager.getCharge(stack), ElectricItem.manager.getCharge(filter));
			}

			public boolean apply(ItemStack stack)
			{
				if (!this.hasInitialised)
				{
					this.initalise();
				}

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
						&& (!checkDamage || this.checkDamage(stack, filter))
						&& this.checkNBT(stack, filter)
						&& (!checkEnergy || this.checkEnergy(stack, filter)))
					{
						return true;
					}
				}

				return this.filters.isEmpty() && this.energy.active && !checkEnergy;
			}
		};
	}

	private static List<? extends EnvItemHandler.AdjacentInventory> getTargetInventories(ItemStack stack, BlockEntity parent)
	{
		Direction dir = getDirection(stack);
		if (dir == null)
		{
			return StackUtil.ENV.getAdjacentInventories(parent);
		}

		EnvItemHandler.AdjacentInventory inv = StackUtil.ENV.getAdjacentInventory(parent, dir);
		return inv == null ? emptyInvList : Collections.singletonList(inv);
	}

	private static List<LiquidUtil.AdjacentFluidHandler> getTargetFluidHandlers(ItemStack stack, BlockEntity parent)
	{
		Direction dir = getDirection(stack);
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
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		switch (this.type)
		{
			case advanced_ejector:
			case advanced_pulling:
				return new HandHeldAdvancedUpgrade(player, hand, stack);
			default:
				return null;
		}
	}

	@Override
	public IHasGui getSubInventory(Player player, InteractionHand hand, ItemStack stack, int ID)
	{
		switch (this.type)
		{
			case advanced_ejector:
			case advanced_pulling:
				return HandHeldAdvancedUpgrade.delegate(player, hand, stack, ID);
			default:
				return null;
		}
	}

	@Nullable
	public static Direction getDirection(ItemStack stack)
	{
		int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
		return rawDir >= 1 && rawDir <= 6 ? Util.ALL_DIRS[rawDir - 1] : null;
	}

	public enum UpgradeType
	{
		overclocker(false),
		transformer(false),
		energy_storage(false),
		redstone_inverter(false),
		ejector(true),
		advanced_ejector(true),
		pulling(true),
		advanced_pulling(true),
		fluid_ejector(true),
		fluid_pulling(true),
		remote_interface(false);

		public final boolean directional;

		UpgradeType(boolean directional)
		{
			this.directional = directional;
		}
	}
}
