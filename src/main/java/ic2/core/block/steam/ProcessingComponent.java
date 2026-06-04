package ic2.core.block.steam;

import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.NetworkManager;
import ic2.core.recipe.dynamic.DynamicRecipeManager;
import java.io.DataInput;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ProcessingComponent extends TileEntityComponent {
  protected final DynamicRecipeManager recipeManager;
  
  protected final TileEntityComponent[] lookup;
  
  protected final BooleanSupplier canOperateCallable;
  
  protected int tickRate;
  
  protected int updateTicker;
  
  protected int progress;
  
  protected int operationLength;
  
  protected AudioSource audioSource;
  
  protected String startSoundFile;
  
  protected String interruptSoundFile;
  
  protected static final int EventStart = 0;
  
  protected static final int EventInterrupt = 1;
  
  protected static final int EventFinish = 2;
  
  protected static final int EventStop = 3;
  
  public static ProcessingComponent asKineticMachine(TileEntityBlock parent, DynamicRecipeManager recipeManager) {
    return new ProcessingComponent(parent, recipeManager, () -> (parent.getWorld()).isRemote);
  }
  
  public ProcessingComponent(TileEntityBlock parent, DynamicRecipeManager recipeManager, BooleanSupplier canOperateCallable) {
    super(parent);
    this.tickRate = 20;
    this.progress = 0;
    this.operationLength = 200;
    this.startSoundFile = null;
    this.interruptSoundFile = null;
    this.recipeManager = recipeManager;
    this.lookup = null;
    this.updateTicker = IC2.random.nextInt(this.tickRate);
    this.canOperateCallable = canOperateCallable;
  }
  
  public void readFromNbt(NBTTagCompound nbt) {
    this.progress = nbt.func_74762_e("progress");
  }
  
  public NBTTagCompound writeToNbt() {
    NBTTagCompound ret = new NBTTagCompound();
    ret.func_74768_a("progress", this.progress);
    return ret;
  }
  
  public void onUnloaded() {
    super.onUnloaded();
    if ((this.parent.getWorld()).isRemote && this.audioSource != null) {
      IC2.audioManager.removeSources(this.parent);
      this.audioSource = null;
    } 
  }
  
  public void onContainerUpdate(EntityPlayerMP player) {
    GrowingBuffer buffer = new GrowingBuffer(16);
    buffer.writeInt(this.progress);
    buffer.flip();
    setNetworkUpdate(player, buffer);
  }
  
  public void onNetworkUpdate(DataInput is) throws IOException {
    this.progress = is.readInt();
  }
  
  public boolean enableWorldTick() {
    return !(this.parent.getWorld()).isRemote;
  }
  
  public void onWorldTick() {
    if (this.updateTicker++ % this.tickRate != 0)
      return; 
    boolean needsInventoryUpdate = false;
    if (!hasValidInput() && searchForValidInput())
      needsInventoryUpdate = true; 
    int power = 0;
    if (canOperate()) {
      this.parent.setActive(true);
      if (this.progress == 0)
        ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this.parent, 0, true); 
      if (this.progress < this.operationLength)
        this.progress += 5 * power; 
      if (this.progress >= this.operationLength) {
        operateOnce();
        needsInventoryUpdate = true;
        this.progress = 0;
        ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this.parent, 2, true);
      } 
    } else {
      if (this.parent.getActive())
        if (this.progress != 0) {
          ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this.parent, 1, true);
        } else {
          ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this.parent, 3, true);
        }  
      if (!hasValidInput())
        this.progress = 0; 
      this.parent.setActive(false);
    } 
    if (needsInventoryUpdate)
      this.parent.func_70296_d(); 
  }
  
  public boolean canOperate() {
    return this.canOperateCallable.getAsBoolean();
  }
  
  protected boolean hasValidInput() {
    return false;
  }
  
  protected boolean searchForValidInput() {
    return false;
  }
  
  protected void operateOnce() {}
  
  public float getGuiProgress() {
    if (this.progress == 0 || this.operationLength == 0)
      return 0.0F; 
    return this.progress / this.operationLength;
  }
}
