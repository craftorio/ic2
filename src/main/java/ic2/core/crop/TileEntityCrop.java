// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import ic2.core.block.state.UnlistedProperty;
import net.minecraft.world.biome.Biome;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import ic2.core.util.Util;
import net.minecraft.util.math.RayTraceResult;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.item.ItemCropSeed;
import net.minecraftforge.oredict.OreDictionary;
import ic2.core.Ic2Player;
import net.minecraft.world.IBlockAccess;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.Arrays;
import java.util.stream.IntStream;
import ic2.core.util.BiomeUtil;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockFarmland;
import ic2.api.network.NetworkHelper;
import ic2.api.crops.BaseSeed;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import ic2.core.item.type.CropResItemType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import ic2.core.util.StackUtil;
import ic2.core.ref.ItemName;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.block.Block;
import net.minecraftforge.common.EnumPlantType;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTBase;
import ic2.api.crops.Crops;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.crops.CropCard;
import ic2.api.crops.ICropTile;
import ic2.core.block.TileEntityBlock;

public class TileEntityCrop extends TileEntityBlock implements ICropTile
{
    private long ticker;
    public boolean dirty;
    public static int tickrate;
    private CropCard crop;
    private byte biomeHumidityBonus;
    private byte statGrowth;
    private byte statGain;
    private byte statResistance;
    private short storageNutrients;
    private short storageWater;
    private short storageWeedEX;
    private byte terrainAirQuality;
    private byte terrainHumidity;
    private byte terrainNutrients;
    private byte currentSize;
    private short growthPoints;
    private byte scanLevel;
    private boolean crossingBase;
    private NBTTagCompound customData;
    public static final IUnlistedProperty<CropRenderState> renderStateProperty;
    private volatile CropRenderState cropRenderState;
    public static final boolean debug;
    public static final boolean debugChance;
    public static final boolean debugGrowth;
    public static final boolean debugWeedWork;
    public static final boolean debugCollision;
    public static final boolean debugTerrain;
    
    public TileEntityCrop() {
        this.ticker = 0L;
        this.dirty = true;
        this.crop = null;
        this.biomeHumidityBonus = 0;
        this.growthPoints = 0;
        this.customData = new NBTTagCompound();
        if (TileEntityCrop.debug) {
            IC2.log.info(LogCategory.Crop, "Debug mode is running");
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.crossingBase = nbt.getBoolean("crossingBase");
        if (nbt.hasKey("cropOwner") && nbt.hasKey("cropId")) {
            this.crop = Crops.instance.getCropCard(nbt.getString("cropOwner"), nbt.getString("cropId"));
            this.statGrowth = nbt.getByte("statGrowth");
            this.statGain = nbt.getByte("statGain");
            this.statResistance = nbt.getByte("statResistance");
            this.storageNutrients = nbt.getShort("storageNutrients");
            this.storageWater = nbt.getShort("storageWater");
            this.storageWeedEX = nbt.getShort("storageWeedEX");
            this.terrainHumidity = nbt.getByte("terrainHumidity");
            this.terrainNutrients = nbt.getByte("terrainNutrients");
            this.terrainAirQuality = nbt.getByte("terrainAirQuality");
            this.currentSize = nbt.getByte("currentSize");
            this.growthPoints = nbt.getShort("growthPoints");
            this.scanLevel = nbt.getByte("scanLevel");
            this.customData = nbt.getCompoundTag("customData");
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("crossingBase", this.crossingBase);
        if (this.crop != null) {
            nbt.setString("cropOwner", this.crop.getOwner());
            nbt.setString("cropId", this.crop.getId());
            nbt.setByte("statGrowth", this.statGrowth);
            nbt.setByte("statGain", this.statGain);
            nbt.setByte("statResistance", this.statResistance);
            nbt.setShort("storageNutrients", this.storageNutrients);
            nbt.setShort("storageWater", this.storageWater);
            nbt.setShort("storageWeedEX", this.storageWeedEX);
            nbt.setByte("terrainHumidity", this.terrainHumidity);
            nbt.setByte("terrainNutrients", this.terrainNutrients);
            nbt.setByte("terrainAirQuality", this.terrainAirQuality);
            nbt.setByte("currentSize", this.currentSize);
            nbt.setShort("growthPoints", this.growthPoints);
            nbt.setByte("scanLevel", this.scanLevel);
            nbt.setTag("customData", (NBTBase)this.customData.copy());
        }
        return nbt;
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>();
        ret.add("crop");
        ret.add("currentSize");
        ret.add("statGrowth");
        ret.add("statGain");
        ret.add("statResistance");
        ret.add("storageNutrients");
        ret.add("storageWater");
        ret.add("storageWeedEX");
        ret.add("terrainHumidity");
        ret.add("terrainNutrients");
        ret.add("terrainAirQuality");
        ret.add("currentSize");
        ret.add("growthPoints");
        ret.add("scanLevel");
        ret.add("crossingBase");
        ret.add("customData");
        ret.addAll(super.getNetworkedFields());
        return ret;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        this.updateRenderState();
        this.rerender();
        super.onNetworkUpdate(field);
    }
    
    @Override
    protected EnumPlantType getPlantType() {
        return EnumPlantType.Crop;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.isInvalid()) {
            this.ticker = IC2.random.nextInt(256);
        }
        this.updateBiomeHumidityBonus();
        if (this.getWorld().isRemote) {
            this.updateRenderState();
        }
    }
    
    public void updateEntityServer() {
        super.updateEntityServer();
        if (!this.isInvalid()) {
            ++this.ticker;
        }
        if (this.ticker % TileEntityCrop.tickrate == 0L) {
            this.performTick();
        }
        if (this.dirty) {
            this.dirty = false;
            final IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
            this.world.updateObservingBlocksAt(this.pos, (Block)this.getBlockType());
            this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);
            if (!this.world.isRemote) {
                for (final String field : this.getNetworkedFields()) {
                    IC2.network.get(true).updateTileEntityField(this, field);
                }
            }
        }
    }
    
    public void performTick() {
        assert !this.world.isRemote;
        if (this.ticker % (TileEntityCrop.tickrate * 10 << 2) == 0L) {
            this.updateBiomeHumidityBonus();
            if (TileEntityCrop.debug) {
                IC2.log.info(LogCategory.Crop, "Crop at %s - biomeHumidityBonus: %s", this.pos, this.biomeHumidityBonus);
            }
        }
        if (this.ticker % (TileEntityCrop.tickrate << 2) == 0L) {
            this.updateTerrainHumidity();
            if (TileEntityCrop.debug) {
                IC2.log.info(LogCategory.Crop, "Crop at %s - terrain humidity: %s", this.pos, this.terrainHumidity);
            }
        }
        if ((this.ticker + TileEntityCrop.tickrate) % (TileEntityCrop.tickrate << 2) == 0L) {
            this.updateTerrainNutrients();
            if (TileEntityCrop.debug) {
                IC2.log.info(LogCategory.Crop, "Crop at %s - terrain nutrients: %s", this.pos, this.terrainNutrients);
            }
        }
        if ((this.ticker + TileEntityCrop.tickrate * 2) % (TileEntityCrop.tickrate << 2) == 0L) {
            this.updateTerrainAirQuality();
            if (TileEntityCrop.debug) {
                IC2.log.info(LogCategory.Crop, "Crop at %s - terrain air quality: %s", this.pos, this.terrainAirQuality);
            }
        }
        if (this.crop == null && (!this.isCrossingBase() || !this.attemptCrossing()) && (!this.isCrossingBase() || !this.attemptSpreading())) {
            if (IC2.random.nextInt(100) != 0 || this.getStorageWeedEX() > 0) {
                if (this.getStorageWeedEX() > 0 && IC2.random.nextInt(10) == 0) {
                    --this.storageWeedEX;
                }
                return;
            }
            this.reset();
            this.crop = IC2Crops.weed;
            this.setCurrentSize(1);
        }
        this.crop.tick(this);
        if (TileEntityCrop.debug) {
            System.out.println("Plant: " + this.getCrop().getUnlocalizedName());
        }
        if (this.crop.canGrow(this)) {
            this.performGrowthTick();
            if (this.crop == null) {
                return;
            }
            if (this.growthPoints >= this.crop.getGrowthDuration(this)) {
                this.growthPoints = 0;
                this.setCurrentSize(this.getCurrentSize() + 1);
                this.dirty = true;
            }
        }
        if (this.storageNutrients > 0) {
            --this.storageNutrients;
        }
        if (this.storageWater > 0) {
            --this.storageWater;
        }
        if (this.crop.isWeed(this) && IC2.random.nextInt(50) - this.getStatGrowth() <= 2) {
            this.performWeedWork();
        }
    }
    
    public void performGrowthTick() {
        if (this.crop == null) {
            return;
        }
        if (TileEntityCrop.debugGrowth) {
            IC2.log.info(LogCategory.Crop, "Crop at %s - growth points (before): %s", this.pos, this.growthPoints);
        }
        int totalGrowth = 0;
        final int baseGrowth = 3 + IC2.random.nextInt(7) + this.getStatGrowth();
        int minimumQuality = (this.crop.getProperties().getTier() - 1) * 4 + this.getStatGrowth() + this.statGain + this.statResistance;
        minimumQuality = ((minimumQuality < 0) ? 0 : minimumQuality);
        final int providedQuality = this.crop.getWeightInfluences(this, this.getTerrainHumidity(), this.getTerrainNutrients(), this.getTerrainAirQuality()) * 5;
        if (providedQuality >= minimumQuality) {
            totalGrowth = baseGrowth * (100 + (providedQuality - minimumQuality)) / 100;
        }
        else {
            final int aux = (minimumQuality - providedQuality) * 4;
            if (aux > 100 && IC2.random.nextInt(32) > this.statResistance) {
                this.reset();
                totalGrowth = 0;
            }
            else {
                totalGrowth = baseGrowth * (100 - aux) / 100;
                totalGrowth = ((totalGrowth < 0) ? 0 : totalGrowth);
            }
        }
        this.growthPoints += (short)totalGrowth;
        if (TileEntityCrop.debugGrowth) {
            IC2.log.info(LogCategory.Crop, "Crop at %s - base growth: %s", this.pos, baseGrowth);
            IC2.log.info(LogCategory.Crop, "Crop at %s - minimum quality: %s", this.pos, minimumQuality);
            IC2.log.info(LogCategory.Crop, "Crop at %s - provided quality: %s", this.pos, providedQuality);
            IC2.log.info(LogCategory.Crop, "Crop at %s - total growth: %s", this.pos, totalGrowth);
            IC2.log.info(LogCategory.Crop, "Crop at %s - growth points (after): %s", this.pos, this.growthPoints);
        }
    }
    
    public void performWeedWork() {
        final World world = this.getWorld();
        final BlockPos dstPos = this.pos.offset(EnumFacing.HORIZONTALS[IC2.random.nextInt(4)]);
        final TileEntity dstRaw = world.getTileEntity(dstPos);
        if (dstRaw instanceof TileEntityCrop) {
            if (TileEntityCrop.debugWeedWork) {
                IC2.log.info(LogCategory.Crop, "Crop at %s - trying to generate weed", dstPos);
            }
            final TileEntityCrop tileEntityCrop = (TileEntityCrop)dstRaw;
            final CropCard neighborCrop = tileEntityCrop.getCrop();
            if (neighborCrop == null || (!neighborCrop.isWeed(tileEntityCrop) && IC2.random.nextInt(32) >= tileEntityCrop.getStatResistance() && !tileEntityCrop.hasWeedEX())) {
                if (TileEntityCrop.debugWeedWork) {
                    IC2.log.info(LogCategory.Crop, "Crop at %s - weed generated", dstPos);
                }
                int newGrowth = Math.max(this.getStatGrowth(), tileEntityCrop.getStatGrowth());
                if (newGrowth < 31 && IC2.random.nextBoolean()) {
                    ++newGrowth;
                }
                tileEntityCrop.reset();
                tileEntityCrop.crop = Crops.weed;
                tileEntityCrop.setCurrentSize(1);
                tileEntityCrop.setStatGrowth(newGrowth);
            }
        }
        else if (world.isAirBlock(dstPos)) {
            if (TileEntityCrop.debugWeedWork) {
                IC2.log.info(LogCategory.Crop, "Block at %s - trying to generate grass", dstPos);
            }
            final BlockPos soilPos = dstPos.down();
            final Block block = world.getBlockState(soilPos).getBlock();
            if (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.FARMLAND) {
                world.setBlockState(soilPos, Blocks.GRASS.getDefaultState(), 7);
                world.setBlockState(dstPos, Blocks.TALLGRASS.getStateFromMeta(1), 7);
            }
        }
    }
    
    public boolean hasWeedEX() {
        if (this.storageWeedEX > 0) {
            this.storageWeedEX -= 5;
            return true;
        }
        return false;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return this.getWorld().isRemote || this.rightClick(player, hand);
    }
    
    @Override
    protected void onClicked(final EntityPlayer player) {
        if (this.crop != null) {
            this.crop.onLeftClick(this, player);
        }
        else if (this.crossingBase && !this.getWorld().isRemote) {
            this.crossingBase = false;
            this.dirty = true;
            StackUtil.dropAsEntity(this.getWorld(), this.pos, ItemName.crop_stick.getItemStack());
        }
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.PLANT;
    }
    
    @Override
    protected void onBlockBreak() {
        if (!this.getWorld().isRemote) {
            this.pick();
        }
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        final List<AxisAlignedBB> ret = new ArrayList<AxisAlignedBB>();
        if (forCollision) {
            ret.add(new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        }
        else {
            ret.add(new AxisAlignedBB(0.20000000298023224, -0.0625, 0.20000000298023224, 0.800000011920929, 0.8500000238418579, 0.800000011920929));
        }
        return ret;
    }
    
    public boolean rightClick(final EntityPlayer player, final EnumHand hand) {
        final ItemStack heldItem = StackUtil.get(player, hand);
        final boolean creative = player.capabilities.isCreativeMode;
        if (!StackUtil.isEmpty(heldItem)) {
            if (this.crop == null && !this.crossingBase && heldItem.getItem() == ItemName.crop_stick.getInstance()) {
                if (!creative) {
                    StackUtil.consumeOrError(player, hand, 1);
                }
                this.crossingBase = true;
                return this.dirty = true;
            }
            if (this.crop != null && StackUtil.checkItemEquality(heldItem, ItemName.crop_res.getItemStack(CropResItemType.fertilizer))) {
                if (this.applyFertilizer(true)) {
                    this.dirty = true;
                }
                if (!creative) {
                    StackUtil.consumeOrError(player, hand, 1);
                }
                return true;
            }
            final IFluidHandler handler = (IFluidHandler)FluidUtil.getFluidHandler(heldItem);
            if (handler != null) {
                if (this.applyHydration(handler) || this.applyWeedEx(handler, true)) {
                    this.dirty = true;
                }
                return true;
            }
            if (this.crop == null && !this.crossingBase && Crops.instance.getBaseSeed(heldItem) != null) {
                this.reset();
                final BaseSeed baseSeed = Crops.instance.getBaseSeed(heldItem);
                this.setCrop(baseSeed.crop);
                this.currentSize = (byte)baseSeed.size;
                this.statGain = (byte)baseSeed.statGain;
                this.statGrowth = (byte)baseSeed.statGrowth;
                this.statResistance = (byte)baseSeed.statResistance;
                if (!creative) {
                    StackUtil.consumeOrError(player, hand, 1);
                }
                return true;
            }
        }
        return this.crop != null && this.crop.onRightClick(this, player);
    }
    
    public boolean tryPlantIn(final CropCard crop, final int size, final int statGr, final int statGa, final int statRe, final int scan) {
        if (crop == null || crop == IC2Crops.weed || this.isCrossingBase()) {
            return false;
        }
        if (!crop.canGrow(this)) {
            return false;
        }
        this.reset();
        this.setCrop(crop);
        this.setCurrentSize(size);
        this.setStatGain(statGa);
        this.setStatGrowth(statGr);
        this.setStatResistance(statRe);
        this.setScanLevel(scan);
        NetworkHelper.sendInitialData(this);
        return true;
    }
    
    public void onEntityCollision(final Entity entity) {
        if (this.crop == null) {
            return;
        }
        if (this.crop.onEntityCollision(this, entity)) {
            if (this.world.isRemote) {
                return;
            }
            if (IC2.random.nextInt(100) == 0 && IC2.random.nextInt(40) > this.statResistance) {
                this.reset();
                this.world.setBlockState(this.pos.down(), Blocks.DIRT.getDefaultState(), 7);
                if (TileEntityCrop.debugCollision) {
                    IC2.log.info(LogCategory.Crop, "Crop at %s - crop was trampled", this.pos);
                }
            }
        }
    }
    
    public void updateTerrainAirQuality() {
        int value = 0;
        int height = (int)Math.floor((this.pos.getY() - 40) / 15.0);
        if (height > 2) {
            height = 2;
        }
        if (height < 0) {
            height = 0;
        }
        value += height;
        int fresh = 9;
        for (int x = this.pos.getX() - 1; x < this.pos.getX() + 1 && fresh > 0; ++x) {
            for (int z = this.pos.getZ() - 1; z < this.pos.getZ() + 1 && fresh > 0; ++z) {
                if (this.world.isBlockNormalCube(new BlockPos(x, this.pos.getY(), z), false) || this.world.getTileEntity(new BlockPos(x, this.pos.getY(), z)) instanceof TileEntityCrop) {
                    --fresh;
                }
            }
        }
        value += fresh / 2;
        if (this.world.canSeeSky(this.pos.up())) {
            value += 4;
        }
        this.setTerrainAirQuality(value);
    }
    
    public void updateTerrainHumidity() {
        int humidity = this.biomeHumidityBonus;
        if ((int)this.world.getBlockState(this.pos.down()).getValue((IProperty)BlockFarmland.MOISTURE) >= 7) {
            humidity += 2;
        }
        if (this.storageWater >= 5) {
            humidity += 2;
        }
        humidity += (this.storageWater + 24) / 25;
        this.setTerrainHumidity(humidity);
    }
    
    public void updateTerrainNutrients() {
        int nutrients = Crops.instance.getNutrientBiomeBonus(BiomeUtil.getBiome(this.world, this.pos));
        for (int i = 1; i < 5 && this.world.getBlockState(this.pos.down(i)).getBlock() == Blocks.DIRT; ++i) {
            ++nutrients;
        }
        nutrients += (this.storageNutrients + 19) / 20;
        this.setTerrainNutrients(nutrients);
    }
    
    @Override
    public CropCard getCrop() {
        return this.crop;
    }
    
    @Override
    public void setCrop(final CropCard crop) {
        this.crop = crop;
        this.updateTerrainHumidity();
        this.updateTerrainNutrients();
        this.updateTerrainAirQuality();
    }
    
    @Override
    public int getCurrentSize() {
        return this.currentSize;
    }
    
    @Override
    public void setCurrentSize(final int size) {
        this.currentSize = (byte)size;
    }
    
    @Override
    public int getStatGrowth() {
        return this.statGrowth;
    }
    
    @Override
    public void setStatGrowth(final int growth) {
        this.statGrowth = (byte)growth;
    }
    
    @Override
    public int getStatGain() {
        return this.statGain;
    }
    
    @Override
    public void setStatGain(final int gain) {
        this.statGain = (byte)gain;
    }
    
    @Override
    public int getStatResistance() {
        return this.statResistance;
    }
    
    @Override
    public void setStatResistance(final int resistance) {
        this.statResistance = (byte)resistance;
    }
    
    @Override
    public int getStorageNutrients() {
        return this.storageNutrients;
    }
    
    @Override
    public void setStorageNutrients(final int nutrients) {
        this.storageNutrients = (short)nutrients;
    }
    
    @Override
    public int getStorageWater() {
        return this.storageWater;
    }
    
    @Override
    public void setStorageWater(final int water) {
        this.storageWater = (short)water;
    }
    
    @Override
    public int getStorageWeedEX() {
        return this.storageWeedEX;
    }
    
    @Override
    public void setStorageWeedEX(final int weedEX) {
        this.storageWeedEX = (short)weedEX;
    }
    
    @Override
    public int getTerrainAirQuality() {
        return this.terrainAirQuality;
    }
    
    public void setTerrainAirQuality(final int value) {
        this.terrainAirQuality = (byte)value;
    }
    
    @Override
    public int getTerrainHumidity() {
        return this.terrainHumidity;
    }
    
    public void setTerrainHumidity(final int humidity) {
        this.terrainHumidity = (byte)humidity;
    }
    
    @Override
    public int getTerrainNutrients() {
        return this.terrainNutrients;
    }
    
    public void setTerrainNutrients(final int nutrients) {
        this.terrainNutrients = (byte)nutrients;
    }
    
    @Override
    public int getScanLevel() {
        return this.scanLevel;
    }
    
    @Override
    public void setScanLevel(final int scanLevel) {
        this.scanLevel = (byte)scanLevel;
    }
    
    @Override
    public int getGrowthPoints() {
        return this.growthPoints;
    }
    
    @Override
    public void setGrowthPoints(final int growthPoints) {
        this.growthPoints = (short)growthPoints;
    }
    
    @Override
    public boolean isCrossingBase() {
        return this.crossingBase;
    }
    
    @Override
    public void setCrossingBase(final boolean crossingBase) {
        this.crossingBase = crossingBase;
    }
    
    @Override
    public NBTTagCompound getCustomData() {
        return this.customData;
    }
    
    public BlockPos getPosition() {
        return this.pos;
    }
    
    public World getWorldObj() {
        return this.world;
    }
    
    @Deprecated
    @Override
    public BlockPos getLocation() {
        return this.pos;
    }
    
    @Override
    public int getLightLevel() {
        return this.world.getLight(this.pos);
    }
    
    public int getLightValue() {
        return (this.crop == null) ? 0 : this.crop.getEmittedLight(this);
    }
    
    @Override
    public boolean pick() {
        if (this.crop == null) {
            return false;
        }
        final boolean bonus = this.crop.canBeHarvested(this);
        float firstchance = this.crop.dropSeedChance(this);
        firstchance *= (float)Math.pow(1.1, this.statResistance);
        int dropCount = 0;
        if (bonus) {
            if (this.world.rand.nextFloat() <= (firstchance + 1.0f) * 0.8f) {
                ++dropCount;
            }
            float chance = this.crop.dropSeedChance(this) + this.getStatGrowth() / 100.0f;
            for (int index = 23; index < this.statGain; ++index) {
                chance *= 0.95f;
            }
            if (this.world.rand.nextFloat() <= chance) {
                ++dropCount;
            }
        }
        else if (this.world.rand.nextFloat() <= firstchance * 1.5f) {
            ++dropCount;
        }
        final ItemStack[] drops = new ItemStack[dropCount];
        for (int index = 0; index < dropCount; ++index) {
            drops[index] = this.crop.getSeeds(this);
        }
        this.reset();
        if (!this.world.isRemote && drops.length > 0) {
            for (final ItemStack drop : drops) {
                if (drop.getItem() != ItemName.crop_seed_bag.getInstance()) {
                    drop.setTagCompound((NBTTagCompound)null);
                }
                StackUtil.dropAsEntity(this.world, this.pos, drop);
            }
        }
        return true;
    }
    
    @Override
    public boolean performManualHarvest() {
        final List<ItemStack> dropItems = this.performHarvest();
        if (dropItems != null && !dropItems.isEmpty()) {
            for (final ItemStack stack : dropItems) {
                StackUtil.dropAsEntity(this.world, this.pos, stack);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public List<ItemStack> performHarvest() {
        if (this.crop == null || !this.crop.canBeHarvested(this)) {
            return null;
        }
        double chance = this.crop.dropGainChance();
        chance *= Math.pow(1.03, this.getStatGain());
        if (TileEntityCrop.debug) {
            System.out.println("chance: " + chance);
            final int simCount = 200;
            int sum = 0;
            for (int i = 0; i < 200; ++i) {
                final int dropCount = (int)Math.max(0L, Math.round(IC2.random.nextGaussian() * chance * 0.6827 + chance));
                sum += dropCount;
                System.out.print(dropCount + " ");
            }
            System.out.println();
            System.out.println("sum: " + sum + ", avg: " + sum / 200.0);
        }
        final int dropCount2 = (int)Math.max(0L, Math.round(IC2.random.nextGaussian() * chance * 0.6827 + chance));
        final List<ItemStack> ret = IntStream.range(0, dropCount2).mapToObj(i -> {
            final ItemStack[] drops = this.crop.getGains(this);
            return Arrays.stream(drops).map(drop -> (!StackUtil.isEmpty(drop) && IC2.random.nextInt(100) <= this.getStatGain()) ? StackUtil.incSize(drop) : drop);
        }).flatMap((Function<? super Object, ? extends Stream<?>>)Function.identity()).collect((Collector<? super Object, ?, List<ItemStack>>)Collectors.toList());
        this.setCurrentSize(this.crop.getSizeAfterHarvest(this));
        this.dirty = true;
        return ret;
    }
    
    @Override
    public void reset() {
        this.crop = null;
        this.customData = new NBTTagCompound();
        this.statGain = 0;
        this.statResistance = 0;
        this.statGrowth = 0;
        this.terrainAirQuality = -1;
        this.terrainHumidity = -1;
        this.terrainNutrients = -1;
        this.growthPoints = 0;
        this.scanLevel = 0;
        this.currentSize = 1;
        this.dirty = true;
    }
    
    @Override
    public void updateState() {
        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }
    
    @Override
    public boolean isBlockBelow(final Block required) {
        if (this.crop == null) {
            return false;
        }
        for (int index = 1; index < this.crop.getRootsLength(this); ++index) {
            final BlockPos blockPos = this.pos.down(index);
            final IBlockState state = this.world.getBlockState(blockPos);
            final Block block = state.getBlock();
            if (block.isAir(state, (IBlockAccess)this.world, blockPos)) {
                return false;
            }
            if (block == required) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isBlockBelow(final String oreDictionaryEntry) {
        if (this.crop == null) {
            return false;
        }
        for (int index = 1; index < this.crop.getRootsLength(this); ++index) {
            final BlockPos blockPos = this.pos.down(index);
            final IBlockState state = this.world.getBlockState(blockPos);
            final Block block = state.getBlock();
            if (block.isAir(state, (IBlockAccess)this.world, blockPos)) {
                return false;
            }
            final ItemStack stackBelow = StackUtil.getPickStack(this.world, blockPos, state, Ic2Player.get(this.world));
            for (final ItemStack stack : OreDictionary.getOres(oreDictionaryEntry)) {
                if (StackUtil.checkItemEquality(stackBelow, stack)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public ItemStack generateSeeds(final CropCard crop, final int growth, final int gain, final int resistance, final int scan) {
        return ItemCropSeed.generateItemStackFromValues(crop, growth, gain, resistance, scan);
    }
    
    @Override
    protected int getLightOpacity() {
        return 0;
    }
    
    public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
        state = super.getExtendedState(state);
        final CropRenderState renderState = this.cropRenderState;
        if (renderState != null) {
            state = state.withProperties(TileEntityCrop.renderStateProperty, renderState);
        }
        return state;
    }
    
    private void updateRenderState() {
        this.cropRenderState = new CropRenderState(this.crop, this.getCurrentSize(), this.crossingBase);
    }
    
    public boolean wrenchCanRemove(final EntityPlayer player) {
        return false;
    }
    
    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return (this.crop == null) ? ItemName.crop_stick.getItemStack() : this.generateSeeds(this.crop, this.statGrowth, this.statGain, this.statResistance, this.scanLevel);
    }
    
    private boolean attemptCrossing() {
        if (IC2.random.nextInt(3) != 0) {
            return false;
        }
        final List<TileEntityCrop> neighbours = new ArrayList<TileEntityCrop>(4);
        this.checkCrossingAvailability(this.pos.north(), neighbours);
        this.checkCrossingAvailability(this.pos.south(), neighbours);
        this.checkCrossingAvailability(this.pos.east(), neighbours);
        this.checkCrossingAvailability(this.pos.west(), neighbours);
        if (TileEntityCrop.debug) {
            System.out.print("Attempted cross with " + neighbours.size() + " plants: ");
            for (final TileEntityCrop neighbour : neighbours) {
                System.out.println(neighbour.getCrop().getUnlocalizedName());
            }
            System.out.println();
        }
        if (neighbours.size() < 2) {
            return false;
        }
        final CropCard[] crops = Crops.instance.getCrops().toArray(new CropCard[0]);
        if (crops.length == 0) {
            return false;
        }
        final int[] ratios = new int[crops.length];
        int total = 0;
        for (int index = 0; index < ratios.length; ++index) {
            final CropCard crop = crops[index];
            if (crop.canGrow(this)) {
                for (final TileEntityCrop neighbour2 : neighbours) {
                    total += this.calculateRatioFor(crop, neighbour2.getCrop());
                }
            }
            ratios[index] = total;
        }
        if (TileEntityCrop.debugChance) {
            int lastChance = 0;
            for (int i = 0; i < crops.length; ++i) {
                final int currentChance = ratios[i];
                System.out.println(String.format("%s: %.1f%% %d%n", crops[i].getUnlocalizedName(), (currentChance - lastChance) * 100.0 / total, ratios[i]));
                lastChance = currentChance;
            }
        }
        final int search = IC2.random.nextInt(total);
        if (TileEntityCrop.debug) {
            System.out.printf("rnd: %d / %d%n", search, total);
        }
        int min = 0;
        int max = ratios.length - 1;
        while (min < max) {
            final int cur = (min + max) / 2;
            final int value = ratios[cur];
            if (TileEntityCrop.debug) {
                System.out.printf("min: %d, max: %d, cur: %d, value: %d%n", min, max, cur, value);
            }
            if (search < value) {
                max = cur;
            }
            else {
                min = cur + 1;
            }
        }
        if (TileEntityCrop.debug) {
            System.out.printf("result: %s (%d %d)%n", crops[min].getUnlocalizedName(), min, max);
        }
        assert min == max;
        assert min >= 0 && min < ratios.length;
        assert ratios[min] > search;
        assert ratios[min - 1] <= search;
        this.setCrossingBase(false);
        this.setCrop(crops[min]);
        this.dirty = true;
        this.setCurrentSize(1);
        this.statGrowth = 0;
        this.statResistance = 0;
        this.statGain = 0;
        for (final TileEntityCrop neighbour3 : neighbours) {
            this.statGrowth += neighbour3.statGrowth;
            this.statResistance += neighbour3.statResistance;
            this.statGain += neighbour3.statGain;
        }
        final int count = neighbours.size();
        this.statGrowth /= (byte)count;
        this.statResistance /= (byte)count;
        this.statGain /= (byte)count;
        this.statGrowth += (byte)(IC2.random.nextInt(1 + 2 * count) - count);
        this.statGain += (byte)(IC2.random.nextInt(1 + 2 * count) - count);
        this.statResistance += (byte)(IC2.random.nextInt(1 + 2 * count) - count);
        this.statGrowth = (byte)Util.limit(this.statGrowth, 0, 31);
        this.statGain = (byte)Util.limit(this.statGain, 0, 31);
        this.statResistance = (byte)Util.limit(this.statResistance, 0, 31);
        return true;
    }
    
    private boolean attemptSpreading() {
        final List<TileEntityCrop> neighbours = new ArrayList<TileEntityCrop>(4);
        for (final EnumFacing direction : EnumFacing.HORIZONTALS) {
            final TileEntity tileEntity = this.getWorld().getTileEntity(this.pos.offset(direction));
            if (tileEntity instanceof TileEntityCrop) {
                final TileEntityCrop sideCrop = (TileEntityCrop)tileEntity;
                neighbours.add(sideCrop);
            }
        }
        if (neighbours.size() != 1) {
            return false;
        }
        final TileEntityCrop sideCrop2 = neighbours.get(0);
        final CropCard neighborCrop = sideCrop2.getCrop();
        if (neighborCrop == null) {
            return false;
        }
        if (!neighborCrop.canGrow(this) || !neighborCrop.canCross(sideCrop2)) {
            return false;
        }
        int base = 4;
        if (sideCrop2.statGrowth >= 16) {
            ++base;
        }
        if (sideCrop2.statGrowth >= 30) {
            ++base;
        }
        if (sideCrop2.statResistance >= 28) {
            base += 27 - sideCrop2.statResistance;
        }
        if (base < IC2.random.nextInt(16)) {
            return false;
        }
        this.setCrossingBase(false);
        this.setCrop(sideCrop2.crop);
        this.dirty = true;
        this.setCurrentSize(1);
        this.statGrowth = sideCrop2.statGrowth;
        this.statResistance = sideCrop2.statResistance;
        this.statGain = sideCrop2.statGain;
        return true;
    }
    
    private int calculateRatioFor(final CropCard newCrop, final CropCard oldCrop) {
        if (newCrop == oldCrop) {
            return 500;
        }
        int value = 0;
        final int[] propOld = oldCrop.getProperties().getAllProperties();
        final int[] propNew = newCrop.getProperties().getAllProperties();
        assert propOld.length == propNew.length;
        for (int i = 0; i < 5; ++i) {
            final int delta = Math.abs(propOld[i] - propNew[i]);
            value += -delta + 2;
        }
        for (final String attributeNew : newCrop.getAttributes()) {
            for (final String attributeOld : oldCrop.getAttributes()) {
                if (attributeNew.equalsIgnoreCase(attributeOld)) {
                    value += 5;
                }
            }
        }
        final int diff = newCrop.getProperties().getTier() - oldCrop.getProperties().getTier();
        if (diff > 1) {
            value -= 2 * diff;
        }
        if (diff < -3) {
            value -= -diff;
        }
        return Math.max(value, 0);
    }
    
    private void checkCrossingAvailability(final BlockPos pos, final List<TileEntityCrop> crops) {
        final TileEntity tile = this.getWorld().getTileEntity(pos);
        if (!(tile instanceof TileEntityCrop)) {
            return;
        }
        final TileEntityCrop sideCrop = (TileEntityCrop)tile;
        final CropCard neighborCrop = sideCrop.getCrop();
        if (neighborCrop == null) {
            return;
        }
        if (!neighborCrop.canGrow(this) || !neighborCrop.canCross(sideCrop)) {
            return;
        }
        int base = 4;
        if (sideCrop.statGrowth >= 16) {
            ++base;
        }
        if (sideCrop.statGrowth >= 30) {
            ++base;
        }
        if (sideCrop.statResistance >= 28) {
            base += 27 - sideCrop.statResistance;
        }
        if (base >= IC2.random.nextInt(16)) {
            crops.add(sideCrop);
        }
    }
    
    private void checkSpreadingAvailability(final BlockPos pos, TileEntityCrop crop) {
        final TileEntity tile = this.getWorld().getTileEntity(pos);
        if (!(tile instanceof TileEntityCrop)) {
            return;
        }
        final TileEntityCrop sideCrop = (TileEntityCrop)tile;
        final CropCard neighborCrop = sideCrop.getCrop();
        if (neighborCrop == null) {
            return;
        }
        if (!neighborCrop.canGrow(this) || !neighborCrop.canCross(sideCrop)) {
            return;
        }
        int base = 4;
        if (sideCrop.statGrowth >= 16) {
            ++base;
        }
        if (sideCrop.statGrowth >= 30) {
            ++base;
        }
        if (sideCrop.statResistance >= 28) {
            base += 27 - sideCrop.statResistance;
        }
        if (base >= IC2.random.nextInt(16)) {
            crop = sideCrop;
        }
    }
    
    @Override
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        if (this.world.getBlockState(this.pos.down()).getBlock() != Blocks.FARMLAND) {
            this.pick();
            this.world.setBlockToAir(this.pos);
        }
    }
    
    public boolean applyHydration(final IFluidHandler handler) {
        final int limit = 200;
        if (this.storageWater >= limit) {
            return false;
        }
        final FluidStack stack = handler.drain(new FluidStack(FluidRegistry.WATER, limit - this.storageWater), true);
        if (stack == null || stack.amount <= 0) {
            return false;
        }
        this.storageWater += (short)stack.amount;
        return true;
    }
    
    public boolean applyWeedEx(final IFluidHandler handler, final boolean manual) {
        final int limit = manual ? 100 : 150;
        if (this.storageWeedEX >= limit) {
            return false;
        }
        final FluidStack stack = handler.drain(new FluidStack(FluidName.weed_ex.getInstance(), limit - this.storageWeedEX), true);
        if (stack == null || stack.amount <= 0) {
            return false;
        }
        this.storageWeedEX += (short)stack.amount;
        return true;
    }
    
    public boolean applyFertilizer(final boolean manual) {
        if (this.storageNutrients >= 100) {
            return false;
        }
        this.storageNutrients += (short)(manual ? 100 : 90);
        return true;
    }
    
    private void updateBiomeHumidityBonus() {
        final Biome biome = BiomeUtil.getBiome(this.world, this.pos);
        final float rainfall = biome.getRainfall();
        int rainfallBonus = (int)(25.0f * rainfall - 12.5);
        rainfallBonus = ((rainfallBonus > 10) ? 10 : ((rainfallBonus < -10) ? -10 : rainfallBonus));
        final float temperature = biome.getTemperature(this.pos);
        int coefficientBonus = (int)(Math.abs(rainfallBonus) * (-2.0 * Math.pow(temperature, 2.0) + 4.0f * temperature - 1.0));
        coefficientBonus = ((coefficientBonus > 10) ? 10 : ((coefficientBonus < -10) ? -10 : coefficientBonus));
        if (TileEntityCrop.debug) {
            IC2.log.info(LogCategory.Crop, "Crop at %s - r bonus %d, t/r coefficient bonus %d", this.pos, rainfallBonus, coefficientBonus);
        }
        this.biomeHumidityBonus = (byte)(rainfallBonus + coefficientBonus);
    }
    
    static {
        TileEntityCrop.tickrate = 256;
        renderStateProperty = (IUnlistedProperty)new UnlistedProperty("renderstate", (Class<Object>)CropRenderState.class);
        debug = (System.getProperty("ic2.crops.debug") != null);
        debugChance = (TileEntityCrop.debug && System.getProperty("ic2.crops.debug").contains("chance"));
        debugGrowth = (TileEntityCrop.debug && System.getProperty("ic2.crops.debug").contains("growth"));
        debugWeedWork = (TileEntityCrop.debug && System.getProperty("ic2.crops.debug").contains("weedwork"));
        debugCollision = (TileEntityCrop.debug && System.getProperty("ic2.crops.debug").contains("collision"));
        debugTerrain = (TileEntityCrop.debug && System.getProperty("ic2.crops.debug").contains("terrain"));
    }
    
    public static class CropRenderState
    {
        public final CropCard crop;
        public final int size;
        public final boolean crosscrop;
        
        public CropRenderState(final CropCard crop, final int size, final boolean crosscrop) {
            this.crop = crop;
            this.size = size;
            this.crosscrop = crosscrop;
        }
        
        @Override
        public int hashCode() {
            int ret = (this.crop != null) ? this.crop.hashCode() : 1;
            ret = ret * 31 + (this.size + 1) * 5;
            ret = ret * 31 + (this.crosscrop ? 1 : 0);
            return ret;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CropRenderState)) {
                return false;
            }
            final CropRenderState other = (CropRenderState)obj;
            return other.crop == this.crop && other.size == this.size && other.crosscrop == this.crosscrop;
        }
        
        @Override
        public String toString() {
            return "CropState<" + this.crop + ", " + this.size + ", " + this.crosscrop + '>';
        }
    }
}
