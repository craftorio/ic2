package ic2.core.block;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.item.ItemBooze;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBarrel extends TileEntityBlock {
  public TileEntityBarrel() {}
  
  public TileEntityBarrel(int value) {
    this.type = ItemBooze.getTypeOfValue(value);
    if (this.type > 0)
      this.boozeAmount = ItemBooze.getAmountOfValue(value); 
    if (this.type == 1) {
      this.opened = true;
      this.hopsRatio = (byte)ItemBooze.getHopsRatioOfBeerValue(value);
      this.solidRatio = (byte)ItemBooze.getSolidRatioOfBeerValue(value);
      this.timeRatio = (byte)ItemBooze.getTimeRatioOfBeerValue(value);
    } 
    if (this.type == 2) {
      this.opened = false;
      this.age = timeNedForRum(this.boozeAmount) * ItemBooze.getProgressOfRumValue(value) / 100;
    } 
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.type = nbt.getByte("type");
    this.boozeAmount = nbt.getByte("waterCount");
    this.age = nbt.getInteger("age");
    this.opened = nbt.getBoolean("opened");
    if (this.type == 1) {
      if (!this.opened) {
        this.hopsCount = nbt.getByte("hopsCount");
        this.wheatCount = nbt.getByte("wheatCount");
      } 
      this.solidRatio = nbt.getByte("solidRatio");
      this.hopsRatio = nbt.getByte("hopsRatio");
      this.timeRatio = nbt.getByte("timeRatio");
    } 
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setByte("type", (byte)this.type);
    nbt.setByte("waterCount", (byte)this.boozeAmount);
    nbt.setInteger("age", this.age);
    nbt.setBoolean("opened", this.opened);
    if (this.type == 1) {
      if (!this.opened) {
        nbt.setByte("hopsCount", this.hopsCount);
        nbt.setByte("wheatCount", this.wheatCount);
      } 
      nbt.setByte("solidRatio", this.solidRatio);
      nbt.setByte("hopsRatio", this.hopsRatio);
      nbt.setByte("timeRatio", this.timeRatio);
    } 
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (!isEmpty() && !getActive()) {
      this.age++;
      if (this.type == 1 && this.timeRatio < 5) {
        int x = this.timeRatio;
        if (x == 4)
          x += 2; 
        if (this.age >= 24000.0D * Math.pow(3.0D, x)) {
          this.age = 0;
          this.timeRatio = (byte)(this.timeRatio + 1);
        } 
      } 
    } 
  }
  
  public boolean isEmpty() {
    return (this.type == 0 || this.boozeAmount <= 0);
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = StackUtil.get(player, hand);
    if (stack == null)
      return false; 
    if (side.getAxis() != EnumFacing.Axis.Y && 
      !getActive() && 
      StackUtil.consume(player, hand, StackUtil.sameStack(ItemName.treetap.getItemStack()), 1)) {
      if (!(getWorld()).isRemote)
        setActive(true); 
      if (getFacing() != side)
        setFacing(side); 
      return true;
    } 
    if (!this.opened) {
      if (this.type == 0 || this.type == 1) {
        int minAmount = 1000;
        int space = (32 - this.boozeAmount) * 1000;
        if (player.isSneaking())
          space = Math.min(space, 1000); 
        FluidStack fs;
        if (space >= 1000 && (
          fs = LiquidUtil.drainContainer(player, hand, FluidRegistry.WATER, space, FluidContainerOutputMode.InPlacePreferred, true)) != null && fs.amount >= 1000) {
          int amount = fs.amount / 1000 * 1000;
          fs = LiquidUtil.drainContainer(player, hand, FluidRegistry.WATER, amount, FluidContainerOutputMode.InPlacePreferred, true);
          if (fs.amount != amount)
            return false; 
          LiquidUtil.drainContainer(player, hand, FluidRegistry.WATER, amount, FluidContainerOutputMode.InPlacePreferred, false);
          this.type = 1;
          this.boozeAmount += amount / 1000;
          return true;
        } 
        if (stack.getItem() == Items.WHEAT) {
          this.type = 1;
          int amount = StackUtil.getSize(stack);
          if (player.isSneaking())
            amount = 1; 
          if (amount > 64 - this.wheatCount)
            amount = 64 - this.wheatCount; 
          if (amount <= 0)
            return false; 
          this.wheatCount = (byte)(this.wheatCount + amount);
          StackUtil.consumeOrError(player, hand, amount);
          alterComposition();
          return true;
        } 
        if (StackUtil.checkItemEquality(stack, ItemName.crop_res.getItemStack((Enum)CropResItemType.hops))) {
          this.type = 1;
          int amount = StackUtil.getSize(stack);
          if (player.isSneaking())
            amount = 1; 
          if (amount > 64 - this.hopsCount)
            amount = 64 - this.hopsCount; 
          if (amount <= 0)
            return false; 
          this.hopsCount = (byte)(this.hopsCount + amount);
          StackUtil.consumeOrError(player, hand, amount);
          alterComposition();
          return true;
        } 
      } 
      if ((this.type == 0 || this.type == 2) && 
        stack.getItem() == Items.REEDS) {
        if (this.age > 600)
          return false; 
        this.type = 2;
        int amount = StackUtil.getSize(stack);
        if (player.isSneaking())
          amount = 1; 
        if (this.boozeAmount + amount > 32)
          amount = 32 - this.boozeAmount; 
        if (amount <= 0)
          return false; 
        this.boozeAmount += amount;
        StackUtil.consumeOrError(player, hand, amount);
        return true;
      } 
    } 
    return false;
  }
  
  protected void onClicked(EntityPlayer player) {
    super.onClicked(player);
    World world = getWorld();
    if (getActive()) {
      if (!world.isRemote) {
        StackUtil.dropAsEntity(world, this.pos, ItemName.treetap.getItemStack());
        setActive(false);
      } 
      drainLiquid(1);
      return;
    } 
    if (!world.isRemote)
      StackUtil.dropAsEntity(world, this.pos, new ItemStack(ItemName.barrel.getInstance(), 1, calculateMetaValue())); 
    world.setBlockState(this.pos, BlockName.scaffold.getBlockState(BlockScaffold.ScaffoldType.wood));
  }
  
  private void alterComposition() {
    if (this.timeRatio <= 0) {
      this.age = 0;
    } else if (this.timeRatio == 1) {
      World world = getWorld();
      if (world.rand.nextBoolean()) {
        this.timeRatio = 0;
      } else if (world.rand.nextBoolean()) {
        this.timeRatio = 5;
      } 
    } else if (this.timeRatio == 2) {
      if ((getWorld()).rand.nextBoolean())
        this.timeRatio = 5; 
    } else {
      this.timeRatio = 5;
    } 
  }
  
  public boolean drainLiquid(int amount) {
    if (isEmpty())
      return false; 
    if (amount > this.boozeAmount)
      return false; 
    open();
    if (this.type == 2) {
      int progress = this.age * 100 / timeNedForRum(this.boozeAmount);
      this.boozeAmount -= amount;
      this.age = progress / 100 * timeNedForRum(this.boozeAmount);
    } else {
      this.boozeAmount -= amount;
    } 
    if (this.boozeAmount <= 0) {
      if (this.type == 1) {
        this.hopsCount = 0;
        this.wheatCount = 0;
        this.hopsRatio = 0;
        this.solidRatio = 0;
        this.timeRatio = 0;
      } 
      this.type = 0;
      this.opened = false;
      this.boozeAmount = 0;
    } 
    return true;
  }
  
  private void open() {
    if (this.opened)
      return; 
    this.opened = true;
    if (this.type == 1) {
      float ratio;
      if (this.hopsCount <= 0) {
        ratio = 0.0F;
      } else {
        ratio = this.hopsCount / this.wheatCount;
      } 
      if (ratio <= 0.25F) {
        this.hopsRatio = 0;
      } else if (ratio <= 0.33333334F) {
        this.hopsRatio = 1;
      } else if (ratio <= 0.5F) {
        this.hopsRatio = 2;
      } else if (ratio < 2.0F) {
        this.hopsRatio = 3;
      } else {
        this.hopsRatio = (byte)(int)Math.min(6.0D, Math.floor(ratio) + 2.0D);
        if (ratio >= 5.0F)
          this.timeRatio = 5; 
      } 
      if (this.boozeAmount <= 0) {
        ratio = Float.POSITIVE_INFINITY;
      } else {
        ratio = (this.hopsCount + this.wheatCount) / this.boozeAmount;
      } 
      if (ratio <= 0.41666666F) {
        this.solidRatio = 0;
      } else if (ratio <= 0.5F) {
        this.solidRatio = 1;
      } else if (ratio < 1.0F) {
        this.solidRatio = 2;
      } else if (ratio == 1.0F) {
        this.solidRatio = 3;
      } else if (ratio < 2.0F) {
        this.solidRatio = 4;
      } else if (ratio < 2.4F) {
        this.solidRatio = 5;
      } else {
        this.solidRatio = 6;
        if (ratio >= 4.0F)
          this.timeRatio = 5; 
      } 
    } 
  }
  
  public int calculateMetaValue() {
    if (isEmpty())
      return 0; 
    if (this.type == 1) {
      open();
      int value = 0;
      value |= this.timeRatio;
      value <<= 3;
      value |= this.hopsRatio;
      value <<= 3;
      value |= this.solidRatio;
      value <<= 5;
      value |= this.boozeAmount - 1;
      value <<= 2;
      value |= this.type;
      return value;
    } 
    if (this.type == 2) {
      open();
      int value = 0;
      int progress = this.age * 100 / timeNedForRum(this.boozeAmount);
      if (progress > 100)
        progress = 100; 
      value |= progress;
      value <<= 5;
      value |= this.boozeAmount - 1;
      value <<= 2;
      value |= this.type;
      return value;
    } 
    return 0;
  }
  
  public int timeNedForRum(int amount) {
    return (int)((1200 * amount) * Math.pow(0.95D, (amount - 1)));
  }
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return BlockName.scaffold.getItemStack(BlockScaffold.ScaffoldType.wood);
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    List<ItemStack> ret = new ArrayList<>(super.getAuxDrops(fortune));
    ret.add(ItemName.barrel.getItemStack());
    return ret;
  }
  
  private int type = 0;
  
  private int boozeAmount = 0;
  
  private int age = 0;
  
  private boolean opened;
  
  private byte hopsCount = 0;
  
  private byte wheatCount = 0;
  
  private byte solidRatio = 0;
  
  private byte hopsRatio = 0;
  
  private byte timeRatio = 0;
}
