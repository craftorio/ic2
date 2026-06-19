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
import ic2.core.item.EnvItemHandler;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.text.DecimalFormat;
import java.util.*;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.NotNull;
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
	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		super.appendHoverText(stack, world, tooltip, advanced);
		switch (this.type)
		{
			case overclocker:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.time", decimalformat.format(100.0 * Math.pow(this.getProcessTimeMultiplier(stack, null), StackUtil.getSize(stack)))).withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.power", decimalformat.format(100.0 * Math.pow(this.getEnergyDemandMultiplier(stack, null), StackUtil.getSize(stack)))).withStyle(ChatFormatting.GRAY));
				break;
			case transformer:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.transformer", this.getExtraTier(stack, null) * StackUtil.getSize(stack)).withStyle(ChatFormatting.GRAY));
				break;
			case energy_storage:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.storage", this.getExtraEnergyStorage(stack, null) * StackUtil.getSize(stack)).withStyle(ChatFormatting.GRAY));
				break;
			case ejector:
			case advanced_ejector:
			case fluid_ejector:
			{
				String side = getSideName(stack);
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.ejector", Component.translatable(side)).withStyle(ChatFormatting.GRAY));
				break;
			}
			case pulling:
			case advanced_pulling:
			case fluid_pulling:
			{
				String side = getSideName(stack);
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.pulling", Component.translatable(side)).withStyle(ChatFormatting.GRAY));
				break;
			}
			case redstone_inverter:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.redstone").withStyle(ChatFormatting.GRAY));
				break;
			case remote_interface:
				tooltip.add(Component.translatable("ic2.tooltip.upgrade.remote_interface", StackUtil.getSize(stack)).withStyle(ChatFormatting.GRAY));
		}
	}

	private static String getSideName(ItemStack stack)
	{
		Direction dir = getDirection(stack);
		if (dir == null)
		{
			return "ic2.tooltip.upgrade.ejector.anyside";
		}

		return switch (dir)
		{
			case WEST -> "ic2.dir.west";
			case EAST -> "ic2.dir.east";
			case DOWN -> "ic2.dir.bottom";
			case UP -> "ic2.dir.top";
			case NORTH -> "ic2.dir.north";
			case SOUTH -> "ic2.dir.south";
		};
	}

	public @NotNull InteractionResult useOn(UseOnContext context)
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
					case advanced_ejector, fluid_ejector:
						IC2.sideProxy.messagePlayer(context.getPlayer(), Component.translatable("ic2.tooltip.upgrade.ejector", Component.translatable(getSideName(stack))).getString());
						break;
					case pulling:
					case advanced_pulling, fluid_pulling:
						IC2.sideProxy.messagePlayer(context.getPlayer(), Component.translatable("ic2.tooltip.upgrade.pulling", Component.translatable(getSideName(stack))).getString());
						break;
				}
			}

			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		return switch (this.type)
		{
			case advanced_ejector, advanced_pulling ->
			{
				if (!world.isClientSide)
				{
					this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
				}

				yield new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}
			default -> new InteractionResultHolder<>(InteractionResult.PASS, stack);
		};
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		switch (this.type)
		{
			case advanced_ejector:
			case advanced_pulling:
				if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof DynamicHandHeldContainer)
				{
					HandHeldInventory base = ((DynamicHandHeldContainer<?>) player.containerMenu).base;
					if (base instanceof HandHeldAdvancedUpgrade && base.isThisContainer(stack))
					{
						base.saveAsThrown(stack);
						player.closeContainer();
					}
				}
			default:
				return true;
		}
	}

	@Override
	public boolean isSuitableFor(ItemStack stack, Set<UpgradableProperty> types)
	{
		return switch (this.type)
		{
			case overclocker ->
				types.contains(UpgradableProperty.Processing) || types.contains(UpgradableProperty.Augmentable);
			case transformer -> types.contains(UpgradableProperty.Transformer);
			case energy_storage -> types.contains(UpgradableProperty.EnergyStorage);
			case ejector, advanced_ejector -> types.contains(UpgradableProperty.ItemProducing);
			case fluid_ejector -> types.contains(UpgradableProperty.FluidProducing);
			case pulling, advanced_pulling -> types.contains(UpgradableProperty.ItemConsuming);
			case fluid_pulling -> types.contains(UpgradableProperty.FluidConsuming);
			case redstone_inverter -> types.contains(UpgradableProperty.RedstoneSensitive);
			case remote_interface -> types.contains(UpgradableProperty.RemotelyAccessible);
		};
	}

	@Override
	public int getAugmentation(ItemStack stack, IUpgradableBlock parent)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.overclocker)
		{
			return 1;
		}
		return 0;
	}

	@Override
	public int getExtraProcessTime(ItemStack stack, IUpgradableBlock parent)
	{
		return 0;
	}

	@Override
	public double getProcessTimeMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.overclocker)
		{
			return 0.7;
		}
		return 1.0;
	}

	@Override
	public int getExtraEnergyDemand(ItemStack stack, IUpgradableBlock parent)
	{
		return 0;
	}

	@Override
	public double getEnergyDemandMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.overclocker)
		{
			return 1.6;
		}
		return 1.0;
	}

	@Override
	public int getExtraEnergyStorage(ItemStack stack, IUpgradableBlock parent)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.energy_storage)
		{
			return 10000;
		}
		return 0;
	}

	@Override
	public double getEnergyStorageMultiplier(ItemStack stack, IUpgradableBlock parent)
	{
		return 1.0;
	}

	@Override
	public int getExtraTier(ItemStack stack, IUpgradableBlock parent)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.transformer)
		{
			return 1;
		}
		return 0;
	}

	@Override
	public boolean modifiesRedstoneInput(ItemStack stack, IUpgradableBlock parent)
	{
		return Objects.requireNonNull(this.type) == UpgradeType.redstone_inverter;
	}

	@Override
	public int getRedstoneInput(ItemStack stack, IUpgradableBlock parent, int externalInput)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.redstone_inverter)
		{
			return 15 - externalInput;
		}
		return externalInput;
	}

	@Override
	public int getRangeAmplification(ItemStack stack, IUpgradableBlock parent, int existingRange)
	{
		if (Objects.requireNonNull(this.type) == UpgradeType.remote_interface)
		{
			return existingRange << 1;
		}
		return existingRange;
	}

	@Override
	public boolean onTick(ItemStack stack, IUpgradableBlock parent)
	{
		int size = StackUtil.getSize(stack);
		BlockEntity te = (BlockEntity) parent;
		boolean ret = false;
		double pow = Math.pow(4.0, Math.min(4, size - 1));
		switch (this.type)
		{
			case ejector:
			{
				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(te, inv.getSide()), inv, (int) pow);
				}
				break;
			}
			case advanced_ejector:
			{
				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(StackUtil.ENV.wrapInventory(te, inv.getSide()), inv, (int) pow, stackChecker(stack));
				}
				break;
			}
			case fluid_ejector:
			{
				if (!LiquidUtil.isFluidTile(te, null))
				{
					return false;
				}

				for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
				{
					if (LiquidUtil.transfer(te, fh.dir, fh.handler, (int) (50.0 * pow)) != null)
					{
						ret = true;
					}
				}
				break;
			}
			case pulling:
			{
				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(inv, StackUtil.ENV.wrapInventory(te, inv.getSide()), (int) pow);
				}
				break;
			}
			case advanced_pulling:
			{
				for (EnvItemHandler.AdjacentInventory inv : getTargetInventories(stack, te))
				{
					StackUtil.ENV.transfer(inv, StackUtil.ENV.wrapInventory(te, inv.getSide()), (int) pow, stackChecker(stack));
				}
				break;
			}
			case fluid_pulling:
			{
				if (!LiquidUtil.isFluidTile(te, null))
				{
					return false;
				}
				for (LiquidUtil.AdjacentFluidHandler fh : getTargetFluidHandlers(stack, te))
				{
					if (LiquidUtil.transfer(fh.handler, fh.dir.getOpposite(), te, (int) (50.0 * pow)) != null)
					{
						ret = true;
					}
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
		return new Predicate<>()
		{
			private boolean hasInitialised = false;
			private Set<ItemStack> filters;
			private UpgradeSettings energy;
			private NbtSettings nbt;

			private void initialise()
			{
				assert !this.hasInitialised;
				CompoundTag tag = StackUtil.getOrCreateNbtData(stack);
				this.filters = this.getFilterStacks(tag);
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
			private boolean checkNBT(ItemStack stack, ItemStack filter)
			{
				return switch (this.nbt)
				{
					case IGNORED -> true;
					case FUZZY -> StackUtil.checkNbtEquality(stack.getTag(), filter.getTag());
					case EXACT -> StackUtil.checkNbtEqualityStrict(stack, filter);
				};
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
					this.initialise();
				}
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
					if (filter.getItem() == stack.getItem())
					{
						if (this.checkNBT(stack, filter) && (!checkEnergy || this.checkEnergy(stack, filter)))
						{
							return true;
						}
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
		return switch (this.type)
		{
			case advanced_ejector, advanced_pulling -> new HandHeldAdvancedUpgrade(player, hand, stack);
			default -> null;
		};
	}

	@Override
	public IHasGui getSubInventory(Player player, InteractionHand hand, ItemStack stack, int ID)
	{
		return switch (this.type)
		{
			case advanced_ejector, advanced_pulling -> HandHeldAdvancedUpgrade.delegate(player, hand, stack, ID);
			default -> null;
		};
	}

	@Nullable
	public static Direction getDirection(ItemStack stack)
	{
		int rawDir = StackUtil.getOrCreateNbtData(stack).getByte("dir");
		return rawDir >= 1 && rawDir <= 6 ? Util.ALL_DIRS[rawDir - 1] : null;
	}

	public enum UpgradeType
	{
		overclocker(false), transformer(false), energy_storage(false), redstone_inverter(false), ejector(true), advanced_ejector(true), pulling(true), advanced_pulling(true), fluid_ejector(true), fluid_pulling(true), remote_interface(false);

		public final boolean directional;

		UpgradeType(boolean directional)
		{
			this.directional = directional;
		}
	}
}
