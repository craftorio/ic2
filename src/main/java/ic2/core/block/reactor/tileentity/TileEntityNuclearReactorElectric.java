package ic2.core.block.reactor.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.reactor.IBaseReactorComponent;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.reactor.IReactorComponent;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2DamageSource;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotReactor;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.IC2Config;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.WorldUtil;

import java.util.ArrayList;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class TileEntityNuclearReactorElectric extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IGuiValueProvider
{
	public final Fluids.InternalFluidTank inputTank;
	public final Fluids.InternalFluidTank outputTank;
	public final InvSlotReactor reactorSlot;
	public final InvSlotOutput coolantoutputSlot;
	public final InvSlotOutput hotcoolantoutputSlot;
	public final InvSlotConsumableLiquidByManager coolantinputSlot;
	public final InvSlotConsumableLiquidByTank hotcoolinputSlot;
	public final Redstone redstone;
	protected final Fluids fluids;
	private final List<IEnergyTile> subTiles = new ArrayList<>();
	public Sound soundMain;
	public Sound soundGeiger;
	public float output = 0.0F;
	public int updateTicker;
	public int heat = 0;
	public int maxHeat = 10000;
	public float hem = 1.0F;
	public int EmitHeat = 0;
	public boolean addedToEnergyNet = false;
	private float lastOutput = 0.0F;
	private int EmitHeatbuffer = 0;
	private boolean fluidCooled = false;

	public TileEntityNuclearReactorElectric(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.NUCLEAR_REACTOR, pos, state);
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
		this.fluids = this.addComponent(new Fluids(this));
		this.inputTank = this.fluids.addTank("inputTank", 10000, InvSlot.Access.NONE, InvSlot.InvSide.ANY, Fluids.fluidPredicate(Recipes.liquidHeatUpManager));
		this.outputTank = this.fluids.addTank("outputTank", 10000, InvSlot.Access.NONE);
		this.reactorSlot = new InvSlotReactor(this, "reactor", 54);
		this.coolantinputSlot = new InvSlotConsumableLiquidByManager(
			this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidHeatUpManager
		);
		this.hotcoolinputSlot = new InvSlotConsumableLiquidByTank(
			this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.coolantoutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1);
		this.hotcoolantoutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
		this.redstone = this.addComponent(new Redstone(this));
	}

	public static void showHeatEffects(Level world, BlockPos pos, int heat)
	{
		RandomSource rnd = RandomSource.create();
		if (rnd.nextInt(8) == 0)
		{
			int puffs = heat / 1000;
			if (puffs > 0)
			{
				puffs = rnd.nextInt(puffs);

				for (int n = 0; n < puffs; n++)
				{
					world.addParticle(
						ParticleTypes.SMOKE, pos.getX() + rnd.nextFloat(), pos.getY() + 0.95F, pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0
					);
				}

				puffs -= rnd.nextInt(4) + 3;

				for (int n = 0; n < puffs; n++)
				{
					world.addParticle(ParticleTypes.FLAME, pos.getX() + rnd.nextFloat(), pos.getY() + 1, pos.getZ() + rnd.nextFloat(), 0.0, 0.0, 0.0);
				}
			}
		}
	}

	private static boolean isFluidChamberBlock(BlockGetter world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() == Ic2Blocks.REACTOR_VESSEL)
		{
			return true;
		}

		BlockEntity te = world.getBlockEntity(pos);
		return te == null ? false : te instanceof IReactorChamber && ((IReactorChamber) te).isWall();
	}

	private float getHuOutputModifier()
	{
		return 40.0F * IC2Config.balance.energy.fluidReactor.outputModifier.get().floatValue();
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide && !this.isFluidCooled())
		{
			this.refreshChambers();
			EnergyNet.instance.addBlockEntityTile(this);
			this.addedToEnergyNet = true;
		}

		this.createChamberRedstoneLinks();
		if (this.isFluidCooled())
		{
			this.createCasingRedstoneLinks();
			this.openTanks();
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.sideProxy.isRendering())
		{
			IC2.soundManager.removeAllSound(this);
			this.soundMain = null;
			this.soundGeiger = null;
		}

		if (IC2.sideProxy.isSimulating() && this.addedToEnergyNet)
		{
			EnergyNet.instance.removeTile(this);
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	public int gaugeHeatScaled(int i)
	{
		return i * this.heat / (this.maxHeat / 100 * 85);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.heat = nbt.getInt("heat");
		this.output = nbt.getShort("output");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("heat", this.heat);
		nbt.putShort("output", (short) this.getReactorEnergyOutput());
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (this.addedToEnergyNet)
		{
			this.refreshChambers();
		}
	}

	@Override
	public void drawEnergy(double amount)
	{
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction direction)
	{
		return true;
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.getReactorEnergyOutput() * 5.0F * IC2Config.balance.energy.generator.nuclear.get().floatValue();
	}

	@Override
	public int getSourceTier()
	{
		return 5;
	}

	@Override
	public double getReactorEUEnergyOutput()
	{
		return this.getOfferedEnergy();
	}

	@Override
	public List<IEnergyTile> getSubTiles()
	{
		return List.copyOf(this.subTiles);
	}

	private void processFluidsSlots()
	{
		this.coolantinputSlot.processIntoTank(this.inputTank, this.coolantoutputSlot);
		this.hotcoolinputSlot.processFromTank(this.outputTank, this.hotcoolantoutputSlot);
	}

	public void refreshChambers()
	{
		Level world = this.getLevel();
		List<IEnergyTile> newSubTiles = new ArrayList<>();
		newSubTiles.add(this);

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityReactorChamberElectric && !te.isRemoved())
			{
				newSubTiles.add((TileEntityReactorChamberElectric) te);
			}
		}

		if (!newSubTiles.equals(this.subTiles))
		{
			if (this.addedToEnergyNet)
			{
				EnergyNet.instance.removeTile(this);
			}

			this.subTiles.clear();
			this.subTiles.addAll(newSubTiles);
			if (this.addedToEnergyNet)
			{
				EnergyNet.instance.addBlockEntityTile(this);
			}
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (!Util.isAreaLoaded(this.getLevel(), this.worldPosition, 8))
			{
				this.output = 0.0F;
			} else
			{
				boolean toFluidCooled = this.isFluidReactor();
				if (this.fluidCooled != toFluidCooled)
				{
					if (toFluidCooled)
					{
						this.enableFluidMode();
					} else
					{
						this.disableFluidMode();
					}

					this.fluidCooled = toFluidCooled;
				}

				this.dropAllUnfittingStuff();
				this.output = 0.0F;
				this.maxHeat = 10000;
				this.hem = 1.0F;
				this.processChambers();
				if (this.fluidCooled)
				{
					this.processFluidsSlots();
					Ic2FluidStack inputFluid = this.inputTank.getFluidStack();
					assert inputFluid == null || Recipes.liquidHeatUpManager.acceptsFluid(this.inputTank.getFluidStack().getFluid());
					int huOtput = (int) (getHuOutputModifier() * this.EmitHeatbuffer);
					int outputRoom = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
					this.EmitHeatbuffer = 0;
					if (outputRoom > 0 && inputFluid != null)
					{
						ILiquidHeatExchangerManager.HeatExchangeProperty prop = Recipes.liquidHeatUpManager.getHeatExchangeProperty(inputFluid.getFluid());
						int fluidOutput = huOtput / prop.huPerMB;
						Ic2FluidStack draincoolant;
						if (fluidOutput < outputRoom)
						{
							this.EmitHeatbuffer = (int) (huOtput % prop.huPerMB / getHuOutputModifier());
							this.EmitHeat = (int) (huOtput / getHuOutputModifier());
							draincoolant = this.inputTank.drainMbUnchecked(fluidOutput, true);
						} else
						{
							this.EmitHeat = outputRoom * prop.huPerMB;
							draincoolant = this.inputTank.drainMbUnchecked(outputRoom, true);
						}

						if (draincoolant != null)
						{
							this.EmitHeat = draincoolant.getAmountMb() * prop.huPerMB;
							huOtput -= this.inputTank.drainMbUnchecked(draincoolant.getAmountMb(), false).getAmountMb() * prop.huPerMB;
							this.outputTank.fillMbUnchecked(Ic2FluidStack.create(prop.outputFluid, draincoolant.getAmountMb()), false);
						} else
						{
							this.EmitHeat = 0;
						}
					} else
					{
						this.EmitHeat = 0;
					}

					this.addHeat((int) (huOtput / getHuOutputModifier()));
				}

				if (this.calculateHeatEffects())
				{
					return;
				}

				this.setActive(this.heat >= 1000 || this.output > 0.0F);
				this.setChanged();
			}

			IC2.network.get(true).updateTileEntityField(this, "output");
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		showHeatEffects(this.getLevel(), this.worldPosition, this.heat);
	}

	public void dropAllUnfittingStuff()
	{
		for (int i = 0; i < this.reactorSlot.size(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && !this.isUsefulItem(stack, false))
			{
				this.reactorSlot.put(i, null);
				this.eject(stack);
			}
		}

		for (int i = this.reactorSlot.size(); i < this.reactorSlot.rawSize(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			this.reactorSlot.put(i, null);
			this.eject(stack);
		}
	}

	public boolean isUsefulItem(ItemStack stack, boolean forInsertion)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return false;
		} else
		{
			return (!forInsertion || !this.fluidCooled || item.getClass() != ItemReactorHeatStorage.class || stack.getDamageValue() <= 0) && item instanceof IBaseReactorComponent && (!forInsertion || ((IBaseReactorComponent) item).canBePlacedIn(stack, this));
		}
	}

	public void eject(ItemStack drop)
	{
		if (IC2.sideProxy.isSimulating() && drop != null)
		{
			StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, drop);
		}
	}

	public boolean calculateHeatEffects()
	{
     RandomSource rng = RandomSource.create();
		if (this.heat >= 4000 && IC2.sideProxy.isSimulating() && !(IC2Config.protection.reactorExplosionPowerLimit.get() <= 0.0F))
		{
			float power = (float) this.heat / this.maxHeat;
			if (power >= 1.0F)
			{
				this.explode();
				return true;
			}

			Level world = this.getLevel();
			if (power >= 0.85F && rng.nextFloat() <= 0.2F * this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				BlockState state = world.getBlockState(coordination);
				if (state.isAir())
				{
					world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
				} else if (state.getDestroySpeed(world, coordination) >= 0.0F && world.getBlockEntity(coordination) == null)
				{
					if (state.canOcclude() || state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA))
					{
						world.setBlockAndUpdate(coordination, net.minecraft.world.level.material.Fluids.LAVA.defaultFluidState().createLegacyBlock());
					} else
					{
						world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
					}
				}
			}

			if (power >= 0.7F)
			{
				for (LivingEntity entity : world.getEntitiesOfClass(
					LivingEntity.class,
					new AABB(
						this.worldPosition.getX() - 3,
						this.worldPosition.getY() - 3,
						this.worldPosition.getZ() - 3,
						this.worldPosition.getX() + 4,
						this.worldPosition.getY() + 4,
						this.worldPosition.getZ() + 4
					),
					EntitySelector.NO_CREATIVE_OR_SPECTATOR
				))
				{
        entity.hurt(Ic2DamageSource.radiation, (int) (rng.nextInt(4) * this.hem));
				}
			}

			if (power >= 0.5F && rng.nextFloat() <= this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				BlockState state = world.getBlockState(coordination);
				if (state.getFluidState().is(net.minecraft.world.level.material.Fluids.WATER))
				{
					world.removeBlock(coordination, false);
				}
			}

			if (power >= 0.4F && rng.nextFloat() <= this.hem)
			{
				BlockPos coordination = this.getRandCoordination(2);
				if (world.getBlockEntity(coordination) == null)
				{
					BlockState state = world.getBlockState(coordination);
					if (state.isFlammable(world, coordination, Direction.UP))
					{
						world.setBlockAndUpdate(coordination, Blocks.FIRE.defaultBlockState());
					}
				}
			}

		}
		return false;
	}

	public BlockPos getRandCoordination(int radius)
	{
     RandomSource rng = RandomSource.create();
		if (radius <= 0)
		{
			return null;
		}

		Level world = this.getLevel();

		BlockPos ret;
		do
		{
			ret = this.worldPosition
				.offset(
					rng.nextInt(2 * radius + 1) - radius,
					rng.nextInt(2 * radius + 1) - radius,
					rng.nextInt(2 * radius + 1) - radius
				);
		} while (ret.equals(this.worldPosition));

		return ret;
	}

	public void processChambers()
	{
		int size = this.getReactorSize();

		for (int pass = 0; pass < 2; pass++)
		{
			for (int y = 0; y < 6; y++)
			{
				for (int x = 0; x < size; x++)
				{
					ItemStack stack = this.reactorSlot.get(x, y);
					if (stack != null && stack.getItem() instanceof IReactorComponent)
					{
						IReactorComponent comp = (IReactorComponent) stack.getItem();
						comp.processChamber(stack, this, x, y, pass == 0);
					}
				}
			}
		}
	}

	@Override
	public boolean produceEnergy()
	{
		return this.redstone.hasRedstoneInput() && IC2Config.balance.energy.generator.nuclear.get() > 0.0F;
	}

	public int getReactorSize()
	{
		Level world = this.getLevel();
		if (world == null)
		{
			return 9;
		}

		int cols = 3;

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
			if (target instanceof TileEntityReactorChamberElectric)
			{
				cols++;
			}
		}

		return cols;
	}

	private boolean isFullSize()
	{
		return this.getReactorSize() == 9;
	}

	@Override
	public int getTickRate()
	{
		return 20;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		return StackUtil.checkItemEquality(StackUtil.get(player, hand), new ItemStack(Ic2Items.REACTOR_CHAMBER))
			? InteractionResult.PASS
			: super.onActivated(player, hand, side, hit);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerNuclearReactor(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerNuclearReactor(syncId, inventory, this);
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("output"))
		{
			if (this.output > 0.0F)
			{
				if (this.lastOutput <= 0.0F)
				{
					if (this.soundMain == null)
					{
						this.soundMain = IC2.soundManager.createSound(this, Ic2SoundEvents.GENERATOR_NUCLEAR_LOOP, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
					}

					if (this.soundMain != null)
					{
						this.soundMain.play();
					}
				}

				if (this.output < 40.0F)
				{
					if (this.lastOutput <= 0.0F || this.lastOutput >= 40.0F)
					{
						if (this.soundGeiger != null)
						{
							IC2.soundManager.removeSound(this, this.soundGeiger);
						}

						this.soundGeiger = IC2.soundManager
							.createSound(this, Ic2SoundEvents.GENERATOR_NUCLEAR_LOW_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
						if (this.soundGeiger != null)
						{
							this.soundGeiger.play();
						}
					}
				} else if (this.output < 80.0F)
				{
					if (this.lastOutput < 40.0F || this.lastOutput >= 80.0F)
					{
						if (this.soundGeiger != null)
						{
							IC2.soundManager.removeSound(this, this.soundGeiger);
						}

						this.soundGeiger = IC2.soundManager
							.createSound(this, Ic2SoundEvents.GENERATOR_NUCLEAR_MEDIUM_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
						if (this.soundGeiger != null)
						{
							this.soundGeiger.play();
						}
					}
				} else if (this.output >= 80.0F && this.lastOutput < 80.0F)
				{
					if (this.soundGeiger != null)
					{
						IC2.soundManager.removeSound(this, this.soundGeiger);
					}

					this.soundGeiger = IC2.soundManager
						.createSound(this, Ic2SoundEvents.GENERATOR_NUCLEAR_HIGH_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0F, 1.0F);
					if (this.soundGeiger != null)
					{
						this.soundGeiger.play();
					}
				}
			} else if (this.lastOutput > 0.0F)
			{
				if (this.soundMain != null)
				{
					this.soundMain.stop();
				}

				if (this.soundGeiger != null)
				{
					this.soundGeiger.stop();
				}
			}

			this.lastOutput = this.output;
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public BlockEntity getCoreTe()
	{
		return this;
	}

	@Override
	public BlockPos getPosition()
	{
		return this.worldPosition;
	}

	@Override
	public Level getWorldObj()
	{
		return this.getLevel();
	}

	@Override
	public int getHeat()
	{
		return this.heat;
	}

	@Override
	public void setHeat(int heat)
	{
		this.heat = heat;
	}

	@Override
	public int addHeat(int amount)
	{
		this.heat += amount;
		return this.heat;
	}

	@Override
	public ItemStack getItemAt(int x, int y)
	{
		return x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6 ? this.reactorSlot.get(x, y) : null;
	}

	@Override
	public void setItemAt(int x, int y, ItemStack item)
	{
		if (x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6)
		{
			this.reactorSlot.put(x, y, item);
		}
	}

	@Override
	public void explode()
	{
		float boomPower = 10.0F;
		float boomMod = 1.0F;

		for (int i = 0; i < this.reactorSlot.size(); i++)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && stack.getItem() instanceof IReactorComponent)
			{
				float f = ((IReactorComponent) stack.getItem()).influenceExplosion(stack, this);
				if (f > 0.0F && f < 1.0F)
				{
					boomMod *= f;
				} else
				{
					boomPower += f;
				}
			}

			this.reactorSlot.put(i, null);
		}

		boomPower *= this.hem * boomMod;
		IC2.log
			.log(
				LogCategory.PlayerActivity,
				org.apache.logging.log4j.Level.INFO,
				"Nuclear Reactor at %s melted (raw explosion power %f)",
				Util.formatPosition(this),
				boomPower
			);
		boomPower = Math.min(boomPower, IC2Config.protection.reactorExplosionPowerLimit.get().floatValue());
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
			if (target instanceof TileEntityReactorChamberElectric)
			{
				world.removeBlock(target.getBlockPos(), false);
			}
		}

		world.removeBlock(this.worldPosition, false);
		Ic2Explosion explosion = new Ic2Explosion(world, null, this.worldPosition, boomPower, 0.01F, Ic2Explosion.Type.ReactorMeltdown);
		explosion.doExplosion();
	}

	@Override
	public void addEmitHeat(int heat)
	{
		this.EmitHeatbuffer += heat;
	}

	@Override
	public int getMaxHeat()
	{
		return this.maxHeat;
	}

	@Override
	public void setMaxHeat(int newMaxHeat)
	{
		this.maxHeat = newMaxHeat;
	}

	@Override
	public float getHeatEffectModifier()
	{
		return this.hem;
	}

	@Override
	public void setHeatEffectModifier(float newHEM)
	{
		this.hem = newHEM;
	}

	@Override
	public float getReactorEnergyOutput()
	{
		return this.output;
	}

	@Override
	public float addOutput(float energy)
	{
		return this.output += energy;
	}

	@Override
	public boolean isFluidCooled()
	{
		return this.fluidCooled;
	}

	private void createChamberRedstoneLinks()
	{
		Level world = this.getLevel();

		for (Direction facing : Util.ALL_DIRS)
		{
			BlockPos cPos = this.worldPosition.relative(facing);
			if (world.getBlockEntity(cPos) instanceof TileEntityReactorChamberElectric chamber)
			{
				if (chamber.redstone.isLinked() && chamber.redstone.getLinkReceiver() != this.redstone)
				{
					chamber.destroyChamber(true);
				} else
				{
					chamber.redstone.linkTo(this.redstone);
				}
			}
		}
	}

	private void createCasingRedstoneLinks()
	{
		WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 2, te ->
		{
			if (te instanceof TileEntityReactorRedstonePort)
			{
				((TileEntityReactorRedstonePort) te).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone);
			}

			return false;
		});
	}

	private void removeCasingRedstoneLinks()
	{
		for (Redstone rs : this.redstone.getLinkedOrigins())
		{
			if (rs.getParent() instanceof TileEntityReactorRedstonePort)
			{
				rs.unlinkOutbound();
			}
		}
	}

	private void enableFluidMode()
	{
		if (this.addedToEnergyNet)
		{
			EnergyNet.instance.removeTile(this);
			this.addedToEnergyNet = false;
		}

		this.createCasingRedstoneLinks();
		this.openTanks();
	}

	private void disableFluidMode()
	{
		if (!this.addedToEnergyNet)
		{
			this.refreshChambers();
			EnergyNet.instance.addBlockEntityTile(this);
			this.addedToEnergyNet = true;
		}

		this.removeCasingRedstoneLinks();
		this.closeTanks();
	}

	private void openTanks()
	{
		this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.I, InvSlot.InvSide.ANY);
		this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.O, InvSlot.InvSide.ANY);
	}

	private void closeTanks()
	{
		this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
		this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
	}

	private boolean isFluidReactor()
	{
		if (!this.isFullSize())
		{
			return false;
		}

		if (!this.hasFluidChamber())
		{
			return false;
		}

		int range = 2;
		final MutableBoolean foundConflict = new MutableBoolean();
		WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 4, new WorldUtil.ITileEntityResultHandler()
		{
			@Override
			public boolean onMatch(BlockEntity te)
			{
				if (!(te instanceof TileEntityNuclearReactorElectric))
				{
					return false;
				} else if (te == TileEntityNuclearReactorElectric.this)
				{
					return false;
				} else
				{
					TileEntityNuclearReactorElectric reactor = (TileEntityNuclearReactorElectric) te;
					if (reactor.isFullSize() && reactor.hasFluidChamber())
					{
						foundConflict.setTrue();
						return true;
					} else
					{
						return false;
					}
				}
			}
		});
		return !foundConflict.getValue();
	}

	private boolean hasFluidChamber()
	{
		int range = 2;
		PathNavigationRegion cache = new PathNavigationRegion(this.getLevel(), this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2));
		MutableBlockPos cPos = new MutableBlockPos();

		for (int i = 0; i < 2; i++)
		{
			int y = this.worldPosition.getY() + 2 * (i * 2 - 1);

			for (int z = this.worldPosition.getZ() - 2; z <= this.worldPosition.getZ() + 2; z++)
			{
				for (int x = this.worldPosition.getX() - 2; x <= this.worldPosition.getX() + 2; x++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		for (int i = 0; i < 2; i++)
		{
			int z = this.worldPosition.getZ() + 2 * (i * 2 - 1);

			for (int y = this.worldPosition.getY() - 2 + 1; y <= this.worldPosition.getY() + 2 - 1; y++)
			{
				for (int x = this.worldPosition.getX() - 2; x <= this.worldPosition.getX() + 2; x++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		for (int i = 0; i < 2; i++)
		{
			int x = this.worldPosition.getX() + 2 * (i * 2 - 1);

			for (int y = this.worldPosition.getY() - 2 + 1; y <= this.worldPosition.getY() + 2 - 1; y++)
			{
				for (int z = this.worldPosition.getZ() - 2 + 1; z <= this.worldPosition.getZ() + 2 - 1; z++)
				{
					cPos.set(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
		{
			return this.maxHeat == 0 ? 0.0 : (double) this.heat / this.maxHeat;
		} else
		{
			throw new IllegalArgumentException("Invalid value: " + name);
		}
	}

	public Ic2FluidTank getinputtank()
	{
		return this.inputTank;
	}

	public Ic2FluidTank getoutputtank()
	{
		return this.outputTank;
	}

	@Override
	public int getMaxStackSize()
	{
		return 1;
	}
}
