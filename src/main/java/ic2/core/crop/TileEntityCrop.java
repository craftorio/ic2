package ic2.core.crop;

import ic2.api.crops.BaseSeed;
import ic2.api.crops.CropCard;
import ic2.api.crops.CropSoilType;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropTile;
import ic2.api.network.NetworkHelper;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.ItemCropSeed;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.BiomeUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

public class TileEntityCrop extends Ic2TileEntity implements ICropTile
{
	public boolean dirty = true;
	public static int tickRate = 256;
	private CropCard crop = null;
	private byte statGrowth;
	private byte statGain;
	private byte statResistance;
	private short storageNutrients;
	private short storageWater;
	private short storageWeedEX;
	private byte terrainAirQuality;
	private byte terrainHumidity;
	private byte terrainNutrients;
	private byte currentAge;
	private short growthPoints = 0;
	private byte scanLevel;
	private CompoundTag customData = new CompoundTag();
	public static final boolean debug = System.getProperty("ic2.crops.debug") != null;
	public static final boolean debugChance = debug && System.getProperty("ic2.crops.debug").contains("chance");
	public static final boolean debugGrowth = debug && System.getProperty("ic2.crops.debug").contains("growth");
	public static final boolean debugWeedWork = debug && System.getProperty("ic2.crops.debug").contains("weedwork");
	public static final boolean debugCollision = debug && System.getProperty("ic2.crops.debug").contains("collision");
	public static final boolean debugTerrain = debug && System.getProperty("ic2.crops.debug").contains("terrain");

	public TileEntityCrop(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.get(BuiltInRegistries.BLOCK.getKey(state.getBlock())), pos, state);
		this.crop = Crops.instance.getCropCard(this.getBlockType());
		if (debug)
		{
			IC2.log.info(LogCategory.Block, "Debug mode is running");
		}
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("statGrowth") && nbt.contains("statGain"))
		{
			this.statGrowth = nbt.getByte("statGrowth");
			this.statGain = nbt.getByte("statGain");
			this.statResistance = nbt.getByte("statResistance");
			this.storageNutrients = nbt.getShort("storageNutrients");
			this.storageWater = nbt.getShort("storageWater");
			this.storageWeedEX = nbt.getShort("storageWeedEX");
			this.terrainHumidity = nbt.getByte("terrainHumidity");
			this.terrainNutrients = nbt.getByte("terrainNutrients");
			this.terrainAirQuality = nbt.getByte("terrainAirQuality");
			this.currentAge = nbt.getByte("currentAge");
			this.growthPoints = nbt.getShort("growthPoints");
			this.scanLevel = nbt.getByte("scanLevel");
			this.customData = nbt.getCompound("customData");
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (this.crop != null)
		{
			nbt.putByte("statGrowth", this.statGrowth);
			nbt.putByte("statGain", this.statGain);
			nbt.putByte("statResistance", this.statResistance);
			nbt.putShort("storageNutrients", this.storageNutrients);
			nbt.putShort("storageWater", this.storageWater);
			nbt.putShort("storageWeedEX", this.storageWeedEX);
			nbt.putByte("terrainHumidity", this.terrainHumidity);
			nbt.putByte("terrainNutrients", this.terrainNutrients);
			nbt.putByte("terrainAirQuality", this.terrainAirQuality);
			nbt.putByte("currentAge", this.currentAge);
			nbt.putShort("growthPoints", this.growthPoints);
			nbt.putByte("scanLevel", this.scanLevel);
			nbt.put("customData", this.customData.copy());
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("crop");
		ret.add("currentAge");
		ret.add("statGrowth");
		ret.add("statGain");
		ret.add("statResistance");
		ret.add("storageNutrients");
		ret.add("storageWater");
		ret.add("storageWeedEX");
		ret.add("terrainHumidity");
		ret.add("terrainNutrients");
		ret.add("terrainAirQuality");
		ret.add("currentAge");
		ret.add("growthPoints");
		ret.add("scanLevel");
		ret.add("customData");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		this.rerender();
		super.onNetworkUpdate(field);
	}

	@Override
	public void updateEntityServer()
	{
		if (this.level.getGameTime() % tickRate == 0L)
		{
			this.performTick();
		}

		if (this.dirty)
		{
			this.dirty = false;
			Level world = this.getLevel();
			if (world == null)
			{
				return;
			}

			BlockState state = world.getBlockState(this.worldPosition);
			world.sendBlockUpdated(this.worldPosition, state, state, 3);
			world.blockUpdated(this.worldPosition, this.getBlockType());
			world.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
			if (!world.isClientSide)
			{
				for (String field : this.getNetworkedFields())
				{
					IC2.network.get(true).updateTileEntityField(this, field);
				}
			}
		}
	}

	public void performTick()
	{
		assert !this.getLevel().isClientSide;
		long ticker = this.level.getGameTime();
		if (ticker % ((long) tickRate << 2) == 0L)
		{
			this.updateTerrainHumidity();
			if (debug)
			{
				IC2.log.info(LogCategory.Block, "Crop at %s - terrain humidity: %s", this.worldPosition, this.terrainHumidity);
			}
		}

		if ((ticker + tickRate) % ((long) tickRate << 2) == 0L)
		{
			this.updateTerrainNutrients();
			if (debug)
			{
				IC2.log.info(LogCategory.Block, "Crop at %s - terrain nutrients: %s", this.worldPosition, this.terrainNutrients);
			}
		}

		if ((ticker + tickRate * 2L) % ((long) tickRate << 2) == 0L)
		{
			this.updateTerrainAirQuality();
			if (debug)
			{
				IC2.log.info(LogCategory.Block, "Crop at %s - terrain air quality: %s", this.worldPosition, this.terrainAirQuality);
			}
		}

		if (this.crop == null && (!this.isCrossingBase() || !this.attemptCrossing()) && (!this.isCrossingBase() || !this.attemptSpreading()))
		{
			if (IC2.random.nextInt(100) != 0 || this.getStorageWeedEX() > 0)
			{
				if (this.getStorageWeedEX() > 0 && IC2.random.nextInt(10) == 0)
				{
					this.storageWeedEX--;
				}

				return;
			}

			this.transformCropBlock(Ic2Crops.weed, 0);
		}

		if (this.crop != null)
		{
			this.crop.tick(this);
			if (debug)
			{
				System.out.println("Plant: " + this.getCrop().getUnlocalizedName());
			}

			if (this.crop.canGrow(this))
			{
				this.performGrowthTick();
				if (this.crop == null)
				{
					return;
				}

				if (this.growthPoints >= this.crop.getGrowthDuration(this))
				{
					this.growthPoints = 0;
					this.setCurrentAge(this.getCurrentAge() + 1);
					this.dirty = true;
				}
			}

			if (this.storageNutrients > 0)
			{
				this.storageNutrients--;
			}

			if (this.storageWater > 0)
			{
				this.storageWater--;
			}

			if (this.crop.isWeed(this) && IC2.random.nextInt(50) - this.getStatGrowth() <= 2)
			{
				this.performWeedWork();
			}
		}
	}

	public void performGrowthTick()
	{
		if (this.crop != null)
		{
			if (debugGrowth)
			{
				IC2.log.info(LogCategory.Block, "Crop at %s - growth points (before): %s", this.worldPosition, this.growthPoints);
			}

			int totalGrowth = 0;
			int baseGrowth = 3 + IC2.random.nextInt(7) + this.getStatGrowth();
			int minimumQuality = (this.crop.getProperties().getTier() - 1) * 4 + this.getStatGrowth() + this.statGain + this.statResistance;
			minimumQuality = Math.max(minimumQuality, 0);
			int providedQuality = this.crop.getWeightInfluences(this, this.getTerrainHumidity(), this.getTerrainNutrients(), this.getTerrainAirQuality()) * 5;
			if (providedQuality >= minimumQuality)
			{
				totalGrowth = baseGrowth * (100 + (providedQuality - minimumQuality)) / 100;
			} else
			{
				int aux = (minimumQuality - providedQuality) * 4;
				if (aux > 100 && IC2.random.nextInt(32) > this.statResistance)
				{
					this.reset();
					totalGrowth = 0;
				} else
				{
					totalGrowth = baseGrowth * (100 - aux) / 100;
					totalGrowth = Math.max(totalGrowth, 0);
				}
			}

			this.growthPoints = (short) (this.growthPoints + totalGrowth);
			if (debugGrowth)
			{
				IC2.log.info(LogCategory.Block, "Crop at %s - base growth: %s", this.worldPosition, baseGrowth);
				IC2.log.info(LogCategory.Block, "Crop at %s - minimum quality: %s", this.worldPosition, minimumQuality);
				IC2.log.info(LogCategory.Block, "Crop at %s - provided quality: %s", this.worldPosition, providedQuality);
				IC2.log.info(LogCategory.Block, "Crop at %s - total growth: %s", this.worldPosition, totalGrowth);
				IC2.log.info(LogCategory.Block, "Crop at %s - growth points (after): %s", this.worldPosition, this.growthPoints);
			}
		}
	}

	public void performWeedWork()
	{
		Level world = this.getLevel();
		if (world != null)
		{
			BlockPos dstPos = this.worldPosition.relative(Util.HORIZONTAL_DIRS[IC2.random.nextInt(4)]);
			if (world.getBlockEntity(dstPos) instanceof TileEntityCrop tileEntityCrop)
			{
				if (debugWeedWork)
				{
					IC2.log.info(LogCategory.Block, "Crop at %s - trying to generate weed", dstPos);
				}

				CropCard neighborCrop = tileEntityCrop.getCrop();
				if (neighborCrop == null
					|| !neighborCrop.isWeed(tileEntityCrop) && IC2.random.nextInt(32) >= tileEntityCrop.getStatResistance() && !tileEntityCrop.hasWeedEX())
				{
					if (debugWeedWork)
					{
						IC2.log.info(LogCategory.Block, "Crop at %s - weed generated", dstPos);
					}

					int newGrowth = Math.max(this.getStatGrowth(), tileEntityCrop.getStatGrowth());
					if (newGrowth < 31 && IC2.random.nextBoolean())
					{
						newGrowth++;
					}

					TileEntityCrop var7 = tileEntityCrop.transformCropBlock(Crops.weed, 0);
					var7.setStatGrowth(newGrowth);
				}
			} else if (world.isEmptyBlock(dstPos))
			{
				if (debugWeedWork)
				{
					IC2.log.info(LogCategory.Block, "Block at %s - trying to generate grass", dstPos);
				}

				BlockPos soilPos = dstPos.below();
				Block block = world.getBlockState(soilPos).getBlock();
				if (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.FARMLAND)
				{
					world.setBlock(soilPos, Blocks.GRASS.defaultBlockState(), 7);
					world.setBlock(dstPos, Blocks.TALL_GRASS.defaultBlockState(), 7);
				}
			}
		}
	}

	public boolean hasWeedEX()
	{
		if (this.storageWeedEX > 0)
		{
			this.storageWeedEX = (short) (this.storageWeedEX - 5);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (this.getLevel().isClientSide)
		{
			return InteractionResult.CONSUME;
		} else
		{
			return this.rightClick(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
		}
	}

	@Override
	protected InteractionResult onClicked(Player player)
	{
		if (player.getAbilities().instabuild)
		{
			return InteractionResult.PASS;
		} else if (this.crop != null)
		{
			System.out.println();
			return this.crop.onLeftClick(this, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		} else if (this.isCrossingBase() && !this.getLevel().isClientSide)
		{
			this.setCrossingBase(false);
			this.dirty = true;
			StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, new ItemStack(Ic2Items.CROP_STICK));
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	@Override
	protected void onBlockBreak()
	{
		if (!this.getLevel().isClientSide)
		{
			this.pick();
		}
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		List<AABB> ret = new ArrayList<>();
		if (forCollision)
		{
			ret.add(new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		} else
		{
			ret.add(new AABB(0.2F, 0.0, 0.2F, 0.8F, 0.85F, 0.8F));
		}

		return ret;
	}

	public boolean rightClick(Player player, InteractionHand hand)
	{
		ItemStack heldItem = StackUtil.get(player, hand);
		boolean creative = player.getAbilities().instabuild;
		if (!StackUtil.isEmpty(heldItem))
		{
			if (this.crop == null && !this.isCrossingBase() && heldItem.getItem() == Ic2Items.CROP_STICK)
			{
				if (!creative)
				{
					StackUtil.consumeOrError(player, hand, 1);
				}

				this.setCrossingBase(true);
				this.dirty = true;
				return true;
			}

			if (this.crop != null && StackUtil.checkItemEquality(heldItem, Ic2Items.FERTILIZER))
			{
				if (this.applyFertilizer(true))
				{
					this.dirty = true;
				}

				if (!creative)
				{
					StackUtil.consumeOrError(player, hand, 1);
				}

				return true;
			}

			Ic2FluidStack fs = Ic2FluidStack.create(Fluids.WATER, Integer.MAX_VALUE);
			int amount = FluidHandler.drainMb(heldItem, fs, true, null);
			if (amount > 0)
			{
				amount = this.applyHydration(amount, true);
				if (amount > 0)
				{
					fs.setAmountMb(amount);
					MutableObject<ItemStack> newStack = new MutableObject();
					amount = FluidHandler.drainMb(heldItem, fs, false, newStack);
					this.applyHydration(amount, false);
					StackUtil.set(player, hand, newStack.getValue());
					this.dirty = true;
				}

				return true;
			}

			fs = Ic2FluidStack.create(Ic2Fluids.WEED_EX.still(), Integer.MAX_VALUE);
			amount = FluidHandler.drainMb(heldItem, fs, true, null);
			if (amount > 0)
			{
				amount = this.applyWeedEx(amount, false, true, true);
				if (amount > 0)
				{
					fs.setAmountMb(amount);
					MutableObject<ItemStack> newStack = new MutableObject();
					amount = FluidHandler.drainMb(heldItem, fs, false, newStack);
					this.applyWeedEx(amount, false, true, false);
					StackUtil.set(player, hand, newStack.getValue());
					this.dirty = true;
				}

				return true;
			}

			if (this.crop == null && !this.isCrossingBase() && Crops.instance.getBaseSeed(heldItem) != null)
			{
				BaseSeed bs = Crops.instance.getBaseSeed(heldItem);
				if (!creative)
				{
					StackUtil.consumeOrError(player, hand, 1);
				}

				TileEntityCrop tileEntityCrop = this.transformCropBlock(bs.crop, bs.size);
				tileEntityCrop.setStatGain(bs.statGain);
				tileEntityCrop.setStatGrowth(bs.statGrowth);
				tileEntityCrop.setStatResistance(bs.statResistance);
				return true;
			}
		}

		return this.crop != null && this.crop.onRightClick(this, player);
	}

	public boolean tryPlantIn(CropCard crop, int size, int statGr, int statGa, int statRe, int scan)
	{
		if (crop == null || crop == Ic2Crops.weed || this.isCrossingBase())
		{
			return false;
		}

		if (!crop.canGrow(this))
		{
			return false;
		}

		TileEntityCrop tileEntityCrop = this.transformCropBlock(crop, size);
		tileEntityCrop.setStatGain(statGa);
		tileEntityCrop.setStatGrowth(statGr);
		tileEntityCrop.setStatResistance(statRe);
		tileEntityCrop.setScanLevel(scan);
		NetworkHelper.sendInitialData(this);
		return true;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if (this.crop != null)
		{
			if (this.crop.onEntityCollision(this, entity))
			{
				Level world = this.getLevel();
				if (world.isClientSide)
				{
					return;
				}

				if (IC2.random.nextInt(100) == 0 && IC2.random.nextInt(40) > this.statResistance)
				{
					this.reset();
					world.setBlock(this.worldPosition.below(), Blocks.DIRT.defaultBlockState(), 7);
					if (debugCollision)
					{
						IC2.log.info(LogCategory.Block, "Crop at %s - crop was trampled", this.worldPosition);
					}
				}
			}
		}
	}

	public void updateTerrainAirQuality()
	{
		Level world = this.getLevel();
		int value = 0;
		int height = (int) Math.floor((this.worldPosition.getY() - 40) / 15.0);
		if (height > 2)
		{
			height = 2;
		}

		if (height < 0)
		{
			height = 0;
		}

		value += height;
		int fresh = 9;

		for (int x = this.worldPosition.getX() - 1; x < this.worldPosition.getX() + 1 && fresh > 0; x++)
		{
			for (int z = this.worldPosition.getZ() - 1; z < this.worldPosition.getZ() + 1 && fresh > 0; z++)
			{
				BlockPos cPos = new BlockPos(x, this.worldPosition.getY(), z);
				if (world.getBlockState(cPos).isCollisionShapeFullBlock(world, cPos) || world.getBlockEntity(new BlockPos(x, this.worldPosition.getY(), z)) instanceof TileEntityCrop)
				{
					fresh--;
				}
			}
		}

		value += fresh / 2;
		if (world.canSeeSky(this.worldPosition.above()))
		{
			value += 4;
		}

		this.setTerrainAirQuality(value);
	}

	public void updateTerrainHumidity()
	{
		Level world = this.getLevel();
		int humidity = Crops.instance.getHumidityBiomeBonus(BiomeUtil.getBiome(world, this.worldPosition));
		if (world.getBlockState(this.worldPosition.below()).getValue(FarmBlock.MOISTURE) >= 7)
		{
			humidity += 2;
		}

		if (this.getStorageWater() >= 5)
		{
			humidity += 2;
		}

		humidity += (this.getStorageWater() + 24) / 25;
		this.setTerrainHumidity(humidity);
	}

	public void updateTerrainNutrients()
	{
		Level world = this.getLevel();
		int nutrients = Crops.instance.getNutrientBiomeBonus(BiomeUtil.getBiome(world, this.worldPosition));

		for (int i = 1; i < 5 && world.getBlockState(this.worldPosition.below(i)).getBlock() == Blocks.DIRT; i++)
		{
			nutrients++;
		}

		nutrients += (this.getStorageNutrients() + 19) / 20;
		this.setTerrainNutrients(nutrients);
	}

	@Override
	public CropCard getCrop()
	{
		return this.crop;
	}

	@Override
	public void setCrop(CropCard crop)
	{
		TileEntityCrop tileEntityCrop = this.transformCropBlock(crop.getCropBlock());
		tileEntityCrop.updateTerrainHumidity();
		tileEntityCrop.updateTerrainNutrients();
		tileEntityCrop.updateTerrainNutrients();
	}

	@Override
	public int getCurrentAge()
	{
		return this.crop == null ? 0 : this.getBlockState().getValue(this.getBlockType().getAgeProperty());
	}

	@Override
	public void setCurrentAge(int size)
	{
		this.currentAge = (byte) size;
		this.withCropAge(size);
	}

	@Override
	public int getStatGrowth()
	{
		return this.statGrowth;
	}

	@Override
	public void setStatGrowth(int growth)
	{
		this.statGrowth = (byte) growth;
	}

	@Override
	public int getStatGain()
	{
		return this.statGain;
	}

	@Override
	public void setStatGain(int gain)
	{
		this.statGain = (byte) gain;
	}

	@Override
	public int getStatResistance()
	{
		return this.statResistance;
	}

	@Override
	public void setStatResistance(int resistance)
	{
		this.statResistance = (byte) resistance;
	}

	@Override
	public int getStorageNutrients()
	{
		return this.storageNutrients;
	}

	@Override
	public void setStorageNutrients(int nutrients)
	{
		this.storageNutrients = (short) nutrients;
	}

	@Override
	public int getStorageWater()
	{
		return this.storageWater;
	}

	@Override
	public void setStorageWater(int water)
	{
		this.storageWater = (short) water;
	}

	@Override
	public int getStorageWeedEX()
	{
		return this.storageWeedEX;
	}

	@Override
	public void setStorageWeedEX(int weedEX)
	{
		this.storageWeedEX = (short) weedEX;
	}

	@Override
	public int getTerrainAirQuality()
	{
		return this.terrainAirQuality;
	}

	public void setTerrainAirQuality(int value)
	{
		this.terrainAirQuality = (byte) value;
	}

	@Override
	public int getTerrainHumidity()
	{
		return this.terrainHumidity;
	}

	public void setTerrainHumidity(int humidity)
	{
		this.terrainHumidity = (byte) humidity;
	}

	@Override
	public int getTerrainNutrients()
	{
		return this.terrainNutrients;
	}

	public void setTerrainNutrients(int nutrients)
	{
		this.terrainNutrients = (byte) nutrients;
	}

	@Override
	public int getScanLevel()
	{
		return this.scanLevel;
	}

	@Override
	public void setScanLevel(int scanLevel)
	{
		this.scanLevel = (byte) scanLevel;
	}

	@Override
	public int getGrowthPoints()
	{
		return this.growthPoints;
	}

	@Override
	public void setGrowthPoints(int growthPoints)
	{
		this.growthPoints = (short) growthPoints;
	}

	@Override
	public boolean isCrossingBase()
	{
		if (this.crop != null)
		{
			return false;
		} else
		{
			return this.level != null && this.level.getBlockState(this.worldPosition).getValue(Ic2TileEntityBlock.CROSSING_BASE);
		}
	}

	@Override
	public void setCrossingBase(boolean crossingBase)
	{
		if (this.crop == null)
		{
			if (this.level != null)
			{
				this.level.setBlockAndUpdate(this.worldPosition, Ic2Blocks.CROP_STICK.defaultBlockState().setValue(Ic2TileEntityBlock.CROSSING_BASE, crossingBase));
			}
		}
	}

	@Override
	public CompoundTag getCustomData()
	{
		return this.customData;
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
	public Level getWorld() { return this.getLevel(); }

	@Deprecated
	@Override
	public BlockPos getLocation()
	{
		return this.worldPosition;
	}

	@Override
	public int getLightLevel()
	{
		return this.getLevel().getMaxLocalRawBrightness(this.worldPosition);
	}

	@Override
	public int getLightValue()
	{
		return this.crop == null ? 0 : this.crop.getEmittedLight(this);
	}

	@Override
	public boolean pick()
	{
		if (this.crop == null)
		{
			return false;
		}

		Level world = this.getLevel();
		if (world == null)
		{
			return false;
		}

		boolean bonus = this.crop.canBeHarvested(this);
		float firstchance = this.crop.dropSeedChance(this);
		firstchance = (float) (firstchance * Math.pow(1.1, this.statResistance));
		int dropCount = 0;
		if (bonus)
		{
			if (world.random.nextFloat() <= (firstchance + 1.0F) * 0.8F)
			{
				dropCount++;
			}

			float chance = this.crop.dropSeedChance(this) + this.getStatGrowth() / 100.0F;

			for (int i = 23; i < this.statGain; i++)
			{
				chance *= 0.95F;
			}

			if (world.random.nextFloat() <= chance)
			{
				dropCount++;
			}
		} else if (world.random.nextFloat() <= firstchance * 1.5F)
		{
			dropCount++;
		}

		ItemStack[] drops = new ItemStack[dropCount];

		for (int i = 0; i < dropCount; i++)
		{
			drops[i] = this.crop.getSeeds(this);
		}

		this.reset();
		if (!world.isClientSide)
		{
			for (ItemStack drop : drops)
			{
				if (drop.getItem() != Ic2Items.CROP_SEED_BACK)
				{
					drop.setTag(null);
				}

				StackUtil.dropAsEntity(world, this.worldPosition, drop);
			}
		}

		return true;
	}

	@Override
	public boolean performManualHarvest()
	{
		List<ItemStack> dropItems = this.performHarvest();
		if (dropItems != null && !dropItems.isEmpty())
		{
			Level world = this.getLevel();
			dropItems.forEach(stack -> StackUtil.dropAsEntity(world, this.worldPosition, stack));
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public List<ItemStack> performHarvest()
	{
		if (this.crop != null && this.crop.canBeHarvested(this))
		{
			double chance = this.crop.dropGainChance();
			chance *= Math.pow(1.03, this.getStatGain());
			if (debug)
			{
				System.out.println("chance: " + chance);
				int simCount = 200;
				int sum = 0;

				for (int i = 0; i < 200; i++)
				{
					int dropCount = (int) Math.max(0L, Math.round(IC2.random.nextGaussian() * chance * 0.6827 + chance));
					sum += dropCount;
					System.out.print(dropCount + " ");
				}

				System.out.println();
				System.out.println("sum: " + sum + ", avg: " + sum / 200.0);
			}

			int dropCount = (int) Math.max(0L, Math.round(IC2.random.nextGaussian() * chance * 0.6827 + chance));
			List<ItemStack> ret = IntStream.range(0, dropCount)
				.mapToObj(
					ix ->
					{
						ItemStack[] drops = this.crop.getGains(this);
						return Arrays.stream(drops)
							.map(drop -> !StackUtil.isEmpty(drop) && IC2.random.nextInt(100) <= this.getStatGain() ? StackUtil.incSize(drop) : drop);
					}
				)
				.flatMap(Function.identity())
				.collect(Collectors.toList());
			this.setCurrentAge(this.crop.getAgeAfterHarvest(this));
			this.dirty = true;
			return ret;
		} else
		{
			return null;
		}
	}

	@Override
	public void reset()
	{
		if (this.level != null)
		{
			this.level.setBlockAndUpdate(this.worldPosition, Ic2Blocks.CROP_STICK.defaultBlockState().setValue(Ic2TileEntityBlock.CROSSING_BASE, false));
		}
	}

	public void resetData()
	{
		this.customData = new CompoundTag();
		this.statGain = 0;
		this.statResistance = 0;
		this.statGrowth = 0;
		this.terrainAirQuality = -1;
		this.terrainHumidity = -1;
		this.terrainNutrients = -1;
		this.growthPoints = 0;
		this.scanLevel = 0;
		this.currentAge = 0;
		this.dirty = true;
	}

	@Override
	public void updateState()
	{
		BlockState state = this.getBlockState();
		Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.worldPosition, state, state, 2);
	}

	@Override
	public boolean isBlockBelow(Block reqBlock)
	{
		if (this.crop == null)
		{
			return false;
		}

		Level world = this.getLevel();

		for (int i = 1; i < this.crop.getRootsLength(this); i++)
		{
			BlockPos blockPos = this.worldPosition.below(i);
			BlockState state = world.getBlockState(blockPos);
			Block block = state.getBlock();
			if (state.isAir())
			{
				return false;
			}

			if (block == reqBlock)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isBlockBelow(TagKey<Block> tag)
	{
		if (this.crop == null)
		{
			return false;
		}

		Level world = this.getLevel();

		for (int i = 1; i < this.crop.getRootsLength(this); i++)
		{
			BlockPos blockPos = this.worldPosition.below(i);
			BlockState state = world.getBlockState(blockPos);
			if (state.isAir())
			{
				return false;
			}

			if (state.is(tag))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack generateSeeds(CropCard crop, int growth, int gain, int resistance, int scan)
	{
		return ItemCropSeed.generateItemStackFromValues(crop, growth, gain, resistance, scan);
	}

	@Override
	protected int getLightOpacity()
	{
		return 0;
	}

	public TileEntityCrop transformCropBlock(CropCard crop, int age)
	{
		if (this.level == null)
		{
			return null;
		}

		if (crop.getCropBlock() instanceof Ic2TileEntityBlock cropBlock)
		{
			BlockState newState = cropBlock.defaultBlockState();
			if (!newState.is(Ic2Blocks.CROP_STICK))
			{
				newState = newState.setValue(cropBlock.getAgeProperty(), age);
			}

			this.level.setBlockAndUpdate(this.worldPosition, newState);
			return (TileEntityCrop) this.level.getBlockEntity(this.worldPosition);
		} else
		{
			return null;
		}
	}

	public TileEntityCrop transformCropBlock(Block cropBlock)
	{
		if (this.level == null)
		{
			return null;
		}

		if (cropBlock instanceof Ic2TileEntityBlock ic2CropBlock)
		{
			BlockState newState = cropBlock.defaultBlockState();
			if (!newState.is(Ic2Blocks.CROP_STICK))
			{
				newState = newState.setValue(ic2CropBlock.getAgeProperty(), Integer.valueOf(this.currentAge));
			}

			this.level.setBlockAndUpdate(this.worldPosition, newState);
			return (TileEntityCrop) this.level.getBlockEntity(this.worldPosition);
		} else
		{
			return null;
		}
	}

	public boolean resetCropBlock(Block cropBlock)
	{
		if (this.level == null)
		{
			return false;
		}

		this.level.setBlockAndUpdate(this.worldPosition, cropBlock.defaultBlockState());
		return true;
	}

	public boolean withCropAge(Integer age)
	{
		if (this.level == null)
		{
			return false;
		}

		this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(this.getBlockType().getAgeProperty(), age));
		return true;
	}

	@Override
	public boolean wrenchCanRemove(Player player)
	{
		return false;
	}

	@Override
	protected ItemStack getPickBlock(Player player, BlockHitResult target)
	{
		return this.crop == null
			? new ItemStack(Ic2Items.CROP_STICK)
			: this.generateSeeds(this.crop, this.statGrowth, this.statGain, this.statResistance, this.scanLevel);
	}

	// $VF: Could not resugar all assert statements!
	// Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
	private boolean attemptCrossing()
	{
		if (IC2.random.nextInt(3) != 0)
		{
			return false;
		}

		List<TileEntityCrop> neighbours = new ArrayList<>(4);
		this.checkCrossingAvailability(this.worldPosition.north(), neighbours);
		this.checkCrossingAvailability(this.worldPosition.south(), neighbours);
		this.checkCrossingAvailability(this.worldPosition.east(), neighbours);
		this.checkCrossingAvailability(this.worldPosition.west(), neighbours);
		if (debug)
		{
			System.out.print("Attempted cross with " + neighbours.size() + " plants: ");
			neighbours.stream().map(neighbor -> neighbor.getCrop().getUnlocalizedName() + " ").forEach(System.out::print);
			System.out.println();
		}

		if (neighbours.size() < 2)
		{
			return false;
		}

		CropCard[] crops = Crops.instance.getCrops().toArray(new CropCard[0]);
		if (crops.length == 0)
		{
			return false;
		}

		int[] ratios = new int[crops.length];
		int total = 0;

		for (int i = 0; i < ratios.length; i++)
		{
			CropCard crop = crops[i];
			if (crop.canGrow(this))
			{
				for (TileEntityCrop te : neighbours)
				{
					total += this.calculateRatioFor(crop, te.getCrop());
				}
			}

			ratios[i] = total;
		}

		if (debugChance)
		{
			int lastChance = 0;

			for (int i = 0; i < crops.length; i++)
			{
				int currentChance = ratios[i];
				System.out.printf("%s: %.1f%% %d%n%n", crops[i].getUnlocalizedName(), (currentChance - lastChance) * 100.0 / total, ratios[i]);
				lastChance = currentChance;
			}
		}

		int search = IC2.random.nextInt(total);
		if (debug)
		{
			System.out.printf("rnd: %d / %d%n", search, total);
		}

		int min = 0;
		int max = ratios.length - 1;

		while (min < max)
		{
			int cur = (min + max) / 2;
			int value = ratios[cur];
			if (debug)
			{
				System.out.printf("min: %d, max: %d, cur: %d, value: %d%n", min, max, cur, value);
			}

			if (search < value)
			{
				max = cur;
			} else
			{
				min = cur + 1;
			}
		}

		if (debug)
		{
			System.out.printf("result: %s (%d %d)%n", crops[min].getUnlocalizedName(), min, max);
		}

		assert min == max;
		assert ratios[min] > search;
		assert min == 0 || ratios[min - 1] <= search;
		this.statGrowth = 0;
		this.statResistance = 0;
		this.statGain = 0;

		for (TileEntityCrop te : neighbours)
		{
			this.statGrowth = (byte) (this.statGrowth + te.statGrowth);
			this.statResistance = (byte) (this.statResistance + te.statResistance);
			this.statGain = (byte) (this.statGain + te.statGain);
		}

		int count = neighbours.size();
		this.statGrowth = (byte) (this.statGrowth / count);
		this.statResistance = (byte) (this.statResistance / count);
		this.statGain = (byte) (this.statGain / count);
		this.statGrowth = (byte) (this.statGrowth + (IC2.random.nextInt(1 + 2 * count) - count));
		this.statGain = (byte) (this.statGain + (IC2.random.nextInt(1 + 2 * count) - count));
		this.statResistance = (byte) (this.statResistance + (IC2.random.nextInt(1 + 2 * count) - count));
		this.statGrowth = (byte) Util.limit(this.statGrowth, 0, 31);
		this.statGain = (byte) Util.limit(this.statGain, 0, 31);
		this.statResistance = (byte) Util.limit(this.statResistance, 0, 31);
		TileEntityCrop tileEntityCrop = this.transformCropBlock(crops[min], 0);
		tileEntityCrop.setCurrentAge(0);
		tileEntityCrop.setStatResistance(this.statResistance);
		tileEntityCrop.setStatGain(this.statGain);
		tileEntityCrop.setStatGrowth(this.statGrowth);
		this.dirty = true;
		return true;
	}

	private boolean attemptSpreading()
	{
		List<TileEntityCrop> neighbours = new ArrayList<>(4);

		for (Direction direction : Util.HORIZONTAL_DIRS)
		{
			if (this.getLevel().getBlockEntity(this.worldPosition.relative(direction)) instanceof TileEntityCrop sideCrop)
			{
				neighbours.add(sideCrop);
			}
		}

		if (neighbours.size() != 1)
		{
			return false;
		}

		TileEntityCrop sideCrop = neighbours.get(0);
		CropCard neighborCrop = sideCrop.getCrop();
		if (neighborCrop == null)
		{
			return false;
		}

		if (neighborCrop.canGrow(this) && neighborCrop.canCross(sideCrop))
		{
			int base = 4;
			if (sideCrop.statGrowth >= 16)
			{
				base++;
			}

			if (sideCrop.statGrowth >= 30)
			{
				base++;
			}

			if (sideCrop.statResistance >= 28)
			{
				base += 27 - sideCrop.statResistance;
			}

			if (base < IC2.random.nextInt(16))
			{
				return false;
			}

			TileEntityCrop tileEntityCrop = this.transformCropBlock(sideCrop.crop, 0);
			tileEntityCrop.setStatGrowth(sideCrop.statGrowth);
			tileEntityCrop.setStatResistance(sideCrop.statResistance);
			tileEntityCrop.setStatGain(sideCrop.statGain);
			this.dirty = true;
			return true;
		} else
		{
			return false;
		}
	}

	private int calculateRatioFor(CropCard newCrop, CropCard oldCrop)
	{
		if (newCrop == oldCrop)
		{
			return 500;
		}

		int value = 0;
		int[] propOld = oldCrop.getProperties().getAllProperties();
		int[] propNew = newCrop.getProperties().getAllProperties();
		assert propOld.length == propNew.length;

		for (int i = 0; i < 5; i++)
		{
			int delta = Math.abs(propOld[i] - propNew[i]);
			value += -delta + 2;
		}

		for (String attributeNew : newCrop.getAttributes())
		{
			for (String attributeOld : oldCrop.getAttributes())
			{
				if (attributeNew.equalsIgnoreCase(attributeOld))
				{
					value += 5;
				}
			}
		}

		int diff = newCrop.getProperties().getTier() - oldCrop.getProperties().getTier();
		if (diff > 1)
		{
			value -= 2 * diff;
		}

		if (diff < -3)
		{
			value -= -diff;
		}

		return Math.max(value, 0);
	}

	private void checkCrossingAvailability(BlockPos pos, List<TileEntityCrop> crops)
	{
		if (this.getLevel().getBlockEntity(pos) instanceof TileEntityCrop sideCrop)
		{
			CropCard neighborCrop = sideCrop.getCrop();
			if (neighborCrop != null)
			{
				if (neighborCrop.canGrow(this) && neighborCrop.canCross(sideCrop))
				{
					int base = 4;
					if (sideCrop.statGrowth >= 16)
					{
						base++;
					}

					if (sideCrop.statGrowth >= 30)
					{
						base++;
					}

					if (sideCrop.statResistance >= 28)
					{
						base += 27 - sideCrop.statResistance;
					}

					if (base >= IC2.random.nextInt(16))
					{
						crops.add(sideCrop);
					}
				}
			}
		}
	}

	private void checkSpreadingAvailability(BlockPos pos, TileEntityCrop crop)
	{
		if (this.getLevel().getBlockEntity(pos) instanceof TileEntityCrop sideCrop)
		{
			CropCard neighborCrop = sideCrop.getCrop();
			if (neighborCrop != null)
			{
				if (neighborCrop.canGrow(this) && neighborCrop.canCross(sideCrop))
				{
					int base = 4;
					if (sideCrop.statGrowth >= 16)
					{
						base++;
					}

					if (sideCrop.statGrowth >= 30)
					{
						base++;
					}

					if (sideCrop.statResistance >= 28)
					{
						base += 27 - sideCrop.statResistance;
					}

					if (base >= IC2.random.nextInt(16))
					{
					}
				}
			}
		}
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		Level world = this.getLevel();
		if (!CropSoilType.contains(world.getBlockState(this.worldPosition.below()).getBlock()))
		{
			this.pick();
			world.removeBlock(this.worldPosition, false);
		}
	}

	public int applyHydration(int amount, boolean simulate)
	{
		int space = 200 - this.storageWater;
		if (space <= 0)
		{
			return 0;
		}

		amount = Math.min(amount, space);
		if (!simulate)
		{
			this.storageWater = (short) (this.storageWater + amount);
		}

		return amount;
	}

	public int applyWeedEx(int amount, boolean fixedAmount, boolean manual, boolean simulate)
	{
		int space = (manual ? 100 : 150) - this.storageWeedEX;
		if (fixedAmount)
		{
			if (space <= amount)
			{
				return 0;
			}
		} else
		{
			if (space <= 0)
			{
				return 0;
			}

			amount = Math.min(amount, space);
		}

		if (!simulate)
		{
			this.storageWeedEX = (short) (this.storageWeedEX + amount);
		}

		return amount;
	}

	public boolean applyFertilizer(boolean manual)
	{
		if (this.storageNutrients >= 100)
		{
			return false;
		}

		this.storageNutrients = (short) (this.storageNutrients + (manual ? 100 : 90));
		return true;
	}
}
