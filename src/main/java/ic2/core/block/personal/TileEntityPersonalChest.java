package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.NetworkManager;
import ic2.core.util.DelegatingInventory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPersonalChest extends TileEntityInventory implements IPersonalBlock, IHasGui {
  public final InvSlot contentSlot = new InvSlot((IInventorySlotHolder)this, "content", InvSlot.Access.NONE, 54);
  
  private GameProfile owner = null;
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    if (nbt.func_74764_b("ownerGameProfile"))
      this.owner = NBTUtil.func_152459_a(nbt.getCompoundTag("ownerGameProfile")); 
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    if (this.owner != null) {
      NBTTagCompound ownerNbt = new NBTTagCompound();
      NBTUtil.func_180708_a(ownerNbt, this.owner);
      nbt.setTag("ownerGameProfile", (NBTBase)ownerNbt);
    } 
    return nbt;
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    this.prevLidAngle = this.lidAngle;
    if (this.usingPlayerCount > 0 && this.lidAngle <= 0) {
      World world = getWorld();
      world.func_184133_a(null, this.field_174879_c, SoundEvents.field_187657_V, SoundCategory.BLOCKS, 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
    } 
    if ((this.usingPlayerCount == 0 && this.lidAngle > 0) || (this.usingPlayerCount > 0 && this.lidAngle < 10)) {
      if (this.usingPlayerCount > 0) {
        this.lidAngle = (byte)(this.lidAngle + 1);
      } else {
        this.lidAngle = (byte)(this.lidAngle - 1);
      } 
      int closeThreshold = 5;
      if (this.lidAngle < closeThreshold && this.prevLidAngle >= closeThreshold) {
        World world = getWorld();
        world.func_184133_a(null, this.field_174879_c, SoundEvents.field_187651_T, SoundCategory.BLOCKS, 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
      } 
    } 
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    return aabbs;
  }
  
  public void func_174889_b(EntityPlayer player) {
    if (!(getWorld()).isRemote) {
      this.usingPlayers.add(player);
      updateUsingPlayerCount();
    } 
  }
  
  public void func_174886_c(EntityPlayer player) {
    if (!(getWorld()).isRemote) {
      this.usingPlayers.remove(player);
      updateUsingPlayerCount();
    } 
  }
  
  private void updateUsingPlayerCount() {
    this.usingPlayerCount = this.usingPlayers.size();
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "usingPlayerCount");
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("owner");
    ret.add("usingPlayerCount");
    return ret;
  }
  
  public boolean wrenchCanRemove(EntityPlayer player) {
    if (!permitsAccess(player.func_146103_bH())) {
      IC2.platform.messagePlayer(player, "This safe is owned by " + this.owner.getName(), new Object[0]);
      return false;
    } 
    if (!this.contentSlot.isEmpty()) {
      IC2.platform.messagePlayer(player, "Can't wrench non-empty safe", new Object[0]);
      return false;
    } 
    return true;
  }
  
  public boolean permitsAccess(GameProfile profile) {
    return checkAccess(this, profile);
  }
  
  public IInventory getPrivilegedInventory(GameProfile accessor) {
    if (!permitsAccess(accessor))
      return (IInventory)this; 
    return (IInventory)new DelegatingInventory((IInventory)this) {
        public int func_70302_i_() {
          return TileEntityPersonalChest.this.contentSlot.size();
        }
        
        public ItemStack func_70301_a(int index) {
          return TileEntityPersonalChest.this.contentSlot.get(index);
        }
        
        public ItemStack func_70298_a(int index, int amount) {
          ItemStack stack = func_70301_a(index);
          if (StackUtil.isEmpty(stack))
            return StackUtil.emptyStack; 
          if (amount >= StackUtil.getSize(stack)) {
            func_70299_a(index, StackUtil.emptyStack);
            return stack;
          } 
          if (amount != 0) {
            if (amount < 0) {
              int space = Math.min(TileEntityPersonalChest.this.contentSlot.getStackSizeLimit(), stack.func_77976_d()) - StackUtil.getSize(stack);
              amount = Math.max(amount, -space);
            } 
            stack = StackUtil.decSize(stack, amount);
            func_70299_a(index, stack);
          } 
          ItemStack ret = StackUtil.copyWithSize(stack, amount);
          return ret;
        }
        
        public ItemStack func_70304_b(int index) {
          ItemStack ret = func_70301_a(index);
          if (!StackUtil.isEmpty(ret))
            func_70299_a(index, StackUtil.emptyStack); 
          return ret;
        }
        
        public void func_70299_a(int index, ItemStack stack) {
          TileEntityPersonalChest.this.contentSlot.put(index, stack);
          func_70296_d();
        }
        
        public int func_70297_j_() {
          return TileEntityPersonalChest.this.contentSlot.getStackSizeLimit();
        }
        
        public boolean func_94041_b(int index, ItemStack stack) {
          return TileEntityPersonalChest.this.contentSlot.accepts(stack);
        }
      };
  }
  
  public static <T extends TileEntity & IPersonalBlock> boolean checkAccess(T te, GameProfile profile) {
    if (profile == null)
      return (((IPersonalBlock)te).getOwner() == null); 
    GameProfile teOwner = ((IPersonalBlock)te).getOwner();
    if (!(te.getWorld()).isRemote) {
      if (teOwner == null) {
        ((IPersonalBlock)te).setOwner(profile);
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)te, "owner");
        return true;
      } 
      if (te.getWorld().func_73046_m().func_184103_al().func_152596_g(profile))
        return true; 
    } else if (teOwner == null) {
      return true;
    } 
    return (teOwner.getId() != null) ? teOwner.getId().equals(profile.getId()) : teOwner.getName().equals(profile.getName());
  }
  
  public GameProfile getOwner() {
    return this.owner;
  }
  
  public void setOwner(GameProfile owner) {
    this.owner = owner;
  }
  
  protected boolean canEntityDestroy(Entity entity) {
    return false;
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!(getWorld()).isRemote && !permitsAccess(player.func_146103_bH())) {
      IC2.platform.messagePlayer(player, "This safe is owned by " + getOwner().getName(), new Object[0]);
      return false;
    } 
    return super.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  public ContainerBase<TileEntityPersonalChest> getGuiContainer(EntityPlayer player) {
    func_174889_b(player);
    return (ContainerBase<TileEntityPersonalChest>)new DynamicContainer<TileEntityPersonalChest>(this, player, GuiParser.parse(this.teBlock)) {
        public void func_75134_a(EntityPlayer player) {
          ((TileEntityPersonalChest)this.base).onGuiClosed(player);
          super.func_75134_a(player);
        }
      };
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {
    func_174886_c(player);
  }
  
  public float getLidAngle(float partialTicks) {
    return Util.lerp(this.prevLidAngle, this.lidAngle, partialTicks) / 10.0F;
  }
  
  private static final List<AxisAlignedBB> aabbs = Arrays.asList(new AxisAlignedBB[] { new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D) });
  
  private final Set<EntityPlayer> usingPlayers = Collections.newSetFromMap(new WeakHashMap<>());
  
  private static final int openingSteps = 10;
  
  private int usingPlayerCount;
  
  private byte lidAngle;
  
  private byte prevLidAngle;
}
