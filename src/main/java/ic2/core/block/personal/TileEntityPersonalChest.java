// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.Arrays;
import ic2.core.util.Util;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.Entity;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.util.DelegatingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Set;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import com.mojang.authlib.GameProfile;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityPersonalChest extends TileEntityInventory implements IPersonalBlock, IHasGui
{
    private GameProfile owner;
    private static final int openingSteps = 10;
    private static final List<AxisAlignedBB> aabbs;
    public final InvSlot contentSlot;
    private final Set<EntityPlayer> usingPlayers;
    private int usingPlayerCount;
    private byte lidAngle;
    private byte prevLidAngle;
    
    public TileEntityPersonalChest() {
        this.owner = null;
        this.usingPlayers = Collections.newSetFromMap(new WeakHashMap<EntityPlayer, Boolean>());
        this.contentSlot = new InvSlot(this, "content", InvSlot.Access.NONE, 54);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("ownerGameProfile")) {
            this.owner = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("ownerGameProfile"));
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.owner != null) {
            final NBTTagCompound ownerNbt = new NBTTagCompound();
            NBTUtil.writeGameProfile(ownerNbt, this.owner);
            nbt.setTag("ownerGameProfile", (NBTBase)ownerNbt);
        }
        return nbt;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        this.prevLidAngle = this.lidAngle;
        if (this.usingPlayerCount > 0 && this.lidAngle <= 0) {
            final World world = this.getWorld();
            world.playSound((EntityPlayer)null, this.pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f);
        }
        if ((this.usingPlayerCount == 0 && this.lidAngle > 0) || (this.usingPlayerCount > 0 && this.lidAngle < 10)) {
            if (this.usingPlayerCount > 0) {
                ++this.lidAngle;
            }
            else {
                --this.lidAngle;
            }
            final int closeThreshold = 5;
            if (this.lidAngle < closeThreshold && this.prevLidAngle >= closeThreshold) {
                final World world2 = this.getWorld();
                world2.playSound((EntityPlayer)null, this.pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, world2.rand.nextFloat() * 0.1f + 0.9f);
            }
        }
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        return TileEntityPersonalChest.aabbs;
    }
    
    @Override
    public void openInventory(final EntityPlayer player) {
        if (!this.getWorld().isRemote) {
            this.usingPlayers.add(player);
            this.updateUsingPlayerCount();
        }
    }
    
    @Override
    public void closeInventory(final EntityPlayer player) {
        if (!this.getWorld().isRemote) {
            this.usingPlayers.remove(player);
            this.updateUsingPlayerCount();
        }
    }
    
    private void updateUsingPlayerCount() {
        this.usingPlayerCount = this.usingPlayers.size();
        IC2.network.get(true).updateTileEntityField(this, "usingPlayerCount");
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("owner");
        ret.add("usingPlayerCount");
        return ret;
    }
    
    public boolean wrenchCanRemove(final EntityPlayer player) {
        if (!this.permitsAccess(player.getGameProfile())) {
            IC2.platform.messagePlayer(player, "This safe is owned by " + this.owner.getName(), new Object[0]);
            return false;
        }
        if (!this.contentSlot.isEmpty()) {
            IC2.platform.messagePlayer(player, "Can't wrench non-empty safe", new Object[0]);
            return false;
        }
        return true;
    }
    
    @Override
    public boolean permitsAccess(final GameProfile profile) {
        return checkAccess(this, profile);
    }
    
    @Override
    public IInventory getPrivilegedInventory(final GameProfile accessor) {
        if (!this.permitsAccess(accessor)) {
            return (IInventory)this;
        }
        return (IInventory)new DelegatingInventory(this) {
            @Override
            public int getSizeInventory() {
                return TileEntityPersonalChest.this.contentSlot.size();
            }
            
            @Override
            public ItemStack getStackInSlot(final int index) {
                return TileEntityPersonalChest.this.contentSlot.get(index);
            }
            
            @Override
            public ItemStack decrStackSize(final int index, int amount) {
                ItemStack stack = this.getStackInSlot(index);
                if (StackUtil.isEmpty(stack)) {
                    return StackUtil.emptyStack;
                }
                if (amount >= StackUtil.getSize(stack)) {
                    this.setInventorySlotContents(index, StackUtil.emptyStack);
                    return stack;
                }
                if (amount != 0) {
                    if (amount < 0) {
                        final int space = Math.min(TileEntityPersonalChest.this.contentSlot.getStackSizeLimit(), stack.getMaxStackSize()) - StackUtil.getSize(stack);
                        amount = Math.max(amount, -space);
                    }
                    stack = StackUtil.decSize(stack, amount);
                    this.setInventorySlotContents(index, stack);
                }
                final ItemStack ret = StackUtil.copyWithSize(stack, amount);
                return ret;
            }
            
            @Override
            public ItemStack removeStackFromSlot(final int index) {
                final ItemStack ret = this.getStackInSlot(index);
                if (!StackUtil.isEmpty(ret)) {
                    this.setInventorySlotContents(index, StackUtil.emptyStack);
                }
                return ret;
            }
            
            @Override
            public void setInventorySlotContents(final int index, final ItemStack stack) {
                TileEntityPersonalChest.this.contentSlot.put(index, stack);
                this.markDirty();
            }
            
            @Override
            public int getInventoryStackLimit() {
                return TileEntityPersonalChest.this.contentSlot.getStackSizeLimit();
            }
            
            @Override
            public boolean isItemValidForSlot(final int index, final ItemStack stack) {
                return TileEntityPersonalChest.this.contentSlot.accepts(stack);
            }
        };
    }
    
    public static <T extends TileEntity & IPersonalBlock> boolean checkAccess(final T te, final GameProfile profile) {
        if (profile == null) {
            return te.getOwner() == null;
        }
        final GameProfile teOwner = te.getOwner();
        if (!te.getWorld().isRemote) {
            if (teOwner == null) {
                te.setOwner(profile);
                IC2.network.get(true).updateTileEntityField(te, "owner");
                return true;
            }
            if (te.getWorld().getMinecraftServer().getPlayerList().canSendCommands(profile)) {
                return true;
            }
        }
        else if (teOwner == null) {
            return true;
        }
        return (teOwner.getId() != null) ? teOwner.getId().equals(profile.getId()) : teOwner.getName().equals(profile.getName());
    }
    
    @Override
    public GameProfile getOwner() {
        return this.owner;
    }
    
    @Override
    public void setOwner(final GameProfile owner) {
        this.owner = owner;
    }
    
    @Override
    protected boolean canEntityDestroy(final Entity entity) {
        return false;
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (!this.getWorld().isRemote && !this.permitsAccess(player.getGameProfile())) {
            IC2.platform.messagePlayer(player, "This safe is owned by " + this.getOwner().getName(), new Object[0]);
            return false;
        }
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    @Override
    public ContainerBase<TileEntityPersonalChest> getGuiContainer(final EntityPlayer player) {
        this.openInventory(player);
        return new DynamicContainer<TileEntityPersonalChest>(this, player, GuiParser.parse(this.teBlock)) {
            public void onContainerClosed(final EntityPlayer player) {
                ((TileEntityPersonalChest)this.base).onGuiClosed(player);
                super.onContainerClosed(player);
            }
        };
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
        this.closeInventory(player);
    }
    
    public float getLidAngle(final float partialTicks) {
        return Util.lerp(this.prevLidAngle, this.lidAngle, partialTicks) / 10.0f;
    }
    
    static {
        aabbs = Arrays.asList(new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375));
    }
}
