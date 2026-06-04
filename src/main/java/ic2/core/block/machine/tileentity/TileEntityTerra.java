package ic2.core.block.machine.tileentity;

import com.google.common.base.Predicate;
import ic2.api.item.ITerraformingBP;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.util.Ic2BlockPos;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TileEntityTerra extends TileEntityElectricMachine {
  public int failedAttempts;
  
  private BlockPos lastPos;
  
  public AudioSource audioSource;
  
  public int inactiveTicks;
  
  public final InvSlotConsumableClass tfbpSlot;
  
  public TileEntityTerra() {
    super(100000, 4);
    this.failedAttempts = 0;
    this.inactiveTicks = 0;
    this.tfbpSlot = new InvSlotConsumableClass((IInventorySlotHolder)this, "tfbp", 1, ITerraformingBP.class);
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isRendering() && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
    super.onUnloaded();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean newActive = false;
    ItemStack stack = this.tfbpSlot.get();
    if (!StackUtil.isEmpty(stack)) {
      ITerraformingBP tfbp = (ITerraformingBP)stack.getItem();
      if (this.energy.getEnergy() >= tfbp.getConsume(stack)) {
        BlockPos nextPos;
        newActive = true;
        World world = getWorld();
        if (this.lastPos != null) {
          int range = tfbp.getRange(stack) / 10;
          nextPos = new BlockPos(this.lastPos.getX() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1), this.pos.getY(), this.lastPos.getZ() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1));
        } else {
          if (this.failedAttempts > 4)
            this.failedAttempts = 4; 
          int range = tfbp.getRange(stack) * (this.failedAttempts + 1) / 5;
          nextPos = new BlockPos(this.pos.getX() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1), this.pos.getY(), this.pos.getZ() - world.rand.nextInt(range + 1) + world.rand.nextInt(range + 1));
        } 
        if (tfbp.terraform(stack, world, nextPos)) {
          this.energy.useEnergy(tfbp.getConsume(stack));
          this.failedAttempts = 0;
          this.lastPos = nextPos;
        } else {
          this.energy.useEnergy(tfbp.getConsume(stack) / 10.0D);
          this.failedAttempts++;
          this.lastPos = null;
        } 
      } 
    } 
    if (newActive) {
      this.inactiveTicks = 0;
      setActive(true);
    } else if (!newActive && getActive() && this.inactiveTicks++ > 30) {
      setActive(false);
    } 
  }
  
  public boolean onActivated(final EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    final World world = getWorld();
    if (!player.func_70093_af() && !world.isRemote) {
      if (ejectBlueprint())
        return true; 
      ItemStack stack = StackUtil.consumeAndGet(player, hand, new Predicate<ItemStack>() {
            public boolean apply(ItemStack input) {
              Item item = input.getItem();
              return (item instanceof ITerraformingBP && ((ITerraformingBP)item).canInsert(input, player, world, TileEntityTerra.this.pos));
            }
          }1);
      if (!StackUtil.isEmpty(stack)) {
        insertBlueprint(stack);
        return true;
      } 
    } 
    return true;
  }
  
  private boolean ejectBlueprint() {
    ItemStack stack = this.tfbpSlot.get();
    if (StackUtil.isEmpty(stack))
      return false; 
    StackUtil.dropAsEntity(getWorld(), this.pos, stack);
    this.tfbpSlot.clear();
    return true;
  }
  
  private void insertBlueprint(ItemStack tfbp) {
    if (!this.tfbpSlot.isEmpty())
      throw new IllegalStateException("not empty"); 
    this.tfbpSlot.put(tfbp);
  }
  
  public static BlockPos getFirstSolidBlockFrom(World world, BlockPos pos, int yOffset) {
    Ic2BlockPos ret = new Ic2BlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());
    while (ret.getY() >= 0) {
      if (world.func_175677_d((BlockPos)ret, false))
        return new BlockPos((Vec3i)ret); 
      ret.moveDown();
    } 
    return null;
  }
  
  public static BlockPos getFirstBlockFrom(World world, BlockPos pos, int yOffset) {
    BlockPos.MutableBlockPos ret = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());
    while (ret.getY() >= 0) {
      if (!world.isAirBlock((BlockPos)ret))
        return new BlockPos((Vec3i)ret); 
      ret.setPos(ret.getX(), ret.getY() - 1, ret.getZ());
    } 
    return null;
  }
  
  public static boolean switchGround(World world, BlockPos pos, Block from, IBlockState to, boolean upwards) {
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
    while (cPos.getY() >= 0) {
      Block block = world.getBlockState((BlockPos)cPos).getBlock();
      if ((upwards && block != from) || (!upwards && block == from))
        break; 
      cPos.setPos(cPos.getX(), cPos.getY() - 1, cPos.getZ());
    } 
    if ((upwards && cPos.getY() == pos.getY()) || (!upwards && cPos.getY() < 0))
      return false; 
    world.func_175656_a(upwards ? cPos.up() : new BlockPos((Vec3i)cPos), to);
    return true;
  }
  
  public void onNetworkUpdate(String field) {
    if (field.equals("active")) {
      if (this.audioSource == null)
        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Terraformers/TerraformerGenericloop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
      if (getActive()) {
        if (this.audioSource != null)
          this.audioSource.play(); 
      } else if (this.audioSource != null) {
        this.audioSource.stop();
      } 
    } 
    super.onNetworkUpdate(field);
  }
}
