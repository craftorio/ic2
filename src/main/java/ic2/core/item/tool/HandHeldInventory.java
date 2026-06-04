package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public abstract class HandHeldInventory implements IHasGui {
  protected ItemStack containerStack;
  
  protected final ItemStack[] inventory;
  
  protected final EntityPlayer player;
  
  private boolean cleared;
  
  public HandHeldInventory(EntityPlayer player, ItemStack containerStack, int inventorySize) {
    this.containerStack = containerStack;
    this.inventory = new ItemStack[inventorySize];
    this.player = player;
    if (IC2.platform.isSimulating()) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(containerStack);
      if (!nbt.hasKey("uid", 3))
        nbt.setInteger("uid", IC2.random.nextInt()); 
      NBTTagList contentList = nbt.getTagList("Items", 10);
      for (int i = 0; i < contentList.tagCount(); i++) {
        NBTTagCompound slotNbt = contentList.getCompoundTagAt(i);
        int slot = slotNbt.getByte("Slot");
        if (slot >= 0 && slot < this.inventory.length)
          this.inventory[slot] = new ItemStack(slotNbt); 
      } 
    } 
  }
  
  public int func_70302_i_() {
    return this.inventory.length;
  }
  
  public boolean func_191420_l() {
    for (ItemStack stack : this.inventory) {
      if (!StackUtil.isEmpty(stack))
        return false; 
    } 
    return true;
  }
  
  public ItemStack func_70301_a(int slot) {
    return StackUtil.wrapEmpty(this.inventory[slot]);
  }
  
  public ItemStack func_70298_a(int index, int amount) {
    ItemStack stack;
    if (index >= 0 && index < this.inventory.length && !StackUtil.isEmpty(stack = this.inventory[index])) {
      ItemStack ret;
      if (amount >= StackUtil.getSize(stack)) {
        ret = stack;
        this.inventory[index] = StackUtil.emptyStack;
      } else {
        ret = StackUtil.copyWithSize(stack, amount);
        this.inventory[index] = StackUtil.decSize(stack, amount);
      } 
      save();
      return ret;
    } 
    return StackUtil.emptyStack;
  }
  
  public void func_70299_a(int slot, ItemStack stack) {
    if (!StackUtil.isEmpty(stack) && StackUtil.getSize(stack) > func_70297_j_())
      stack = StackUtil.copyWithSize(stack, func_70297_j_()); 
    if (StackUtil.isEmpty(stack)) {
      this.inventory[slot] = StackUtil.emptyStack;
    } else {
      this.inventory[slot] = stack;
    } 
    save();
  }
  
  public int func_70297_j_() {
    return 64;
  }
  
  public boolean func_94041_b(int slot, ItemStack stack1) {
    return false;
  }
  
  public void markDirty() {
    save();
  }
  
  public boolean func_70300_a(EntityPlayer player) {
    return (player == this.player && getPlayerInventoryIndex() >= -1);
  }
  
  public void func_174889_b(EntityPlayer player) {}
  
  public void func_174886_c(EntityPlayer player) {}
  
  public ItemStack func_70304_b(int index) {
    ItemStack ret = func_70301_a(index);
    if (!StackUtil.isEmpty(ret))
      func_70299_a(index, null); 
    return ret;
  }
  
  public int func_174887_a_(int id) {
    return 0;
  }
  
  public void func_174885_b(int id, int value) {}
  
  public int func_174890_g() {
    return 0;
  }
  
  public ITextComponent func_145748_c_() {
    return (ITextComponent)new TextComponentString(func_70005_c_());
  }
  
  public void onGuiClosed(EntityPlayer player) {
    save();
    if (!(player.getEntityWorld()).isRemote)
      if (PLAYERS_IN_GUI.contains(player)) {
        PLAYERS_IN_GUI.remove(player);
      } else {
        StackUtil.getOrCreateNbtData(this.containerStack).func_82580_o("uid");
      }  
  }
  
  public boolean isThisContainer(ItemStack stack) {
    if (StackUtil.isEmpty(stack) || stack.getItem() != this.containerStack.getItem())
      return false; 
    NBTTagCompound nbt = stack.func_77978_p();
    return (nbt != null && nbt.getInteger("uid") == getUid());
  }
  
  protected int getUid() {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.containerStack);
    return nbt.getInteger("uid");
  }
  
  protected int getPlayerInventoryIndex() {
    for (int i = -1; i < this.player.inventory.func_70302_i_(); i++) {
      ItemStack stack = (i == -1) ? this.player.inventory.func_70445_o() : this.player.inventory.func_70301_a(i);
      if (isThisContainer(stack))
        return i; 
    } 
    return Integer.MIN_VALUE;
  }
  
  protected void save() {
    if (!IC2.platform.isSimulating())
      return; 
    if (this.cleared)
      return; 
    boolean dropItself = false;
    for (int i = 0; i < this.inventory.length; i++) {
      if (isThisContainer(this.inventory[i])) {
        this.inventory[i] = null;
        dropItself = true;
      } 
    } 
    NBTTagList contentList = new NBTTagList();
    for (int j = 0; j < this.inventory.length; j++) {
      if (!StackUtil.isEmpty(this.inventory[j])) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Slot", (byte)j);
        this.inventory[j].writeToNBT(nbt);
        contentList.appendTag((NBTBase)nbt);
      } 
    } 
    StackUtil.getOrCreateNbtData(this.containerStack).setTag("Items", (NBTBase)contentList);
    try {
      this.containerStack = StackUtil.copyWithSize(this.containerStack, 1);
    } catch (IllegalArgumentException e) {
      CrashReport crash = new CrashReport("Hand held container stack vanished", e);
      CrashReportCategory category = crash.func_85058_a("Container stack");
      category.func_71507_a("Stack", StackUtil.toStringSafe(this.containerStack));
      category.func_71507_a("NBT", this.containerStack.func_77978_p());
      category.func_71507_a("Position", Integer.valueOf(getPlayerInventoryIndex()));
      category.func_71507_a("Had thrown", Boolean.valueOf(dropItself));
      category = crash.func_85058_a("Container info");
      category.func_71507_a("Type", getClass().getName());
      category.func_71507_a("Container", (this.player.field_71070_bA == null) ? null : this.player.field_71070_bA.getClass().getName());
      if (this.player.field_70170_p.isRemote)
        category.func_189529_a("GUI", new ICrashReportDetail<String>() {
              public String call() throws Exception {
                GuiScreen gui = (Minecraft.getMinecraft()).field_71462_r;
                return (gui == null) ? null : gui.getClass().getName();
              }
            }); 
      category.func_71507_a("Opened by", this.player);
      throw new ReportedException(crash);
    } 
    if (dropItself) {
      StackUtil.dropAsEntity(this.player.getEntityWorld(), this.player.func_180425_c(), this.containerStack);
      func_174888_l();
    } else {
      int idx = getPlayerInventoryIndex();
      if (idx < -1) {
        IC2.log.warn(LogCategory.Item, "Handheld inventory saving failed for player " + this.player.func_145748_c_().func_150260_c() + '.');
        func_174888_l();
      } else if (idx == -1) {
        this.player.inventory.func_70437_b(this.containerStack);
      } else {
        this.player.inventory.func_70299_a(idx, this.containerStack);
      } 
    } 
  }
  
  public void saveAsThrown(ItemStack stack) {
    assert IC2.platform.isSimulating();
    NBTTagList contentList = new NBTTagList();
    for (int i = 0; i < this.inventory.length; i++) {
      if (!StackUtil.isEmpty(this.inventory[i]) && !isThisContainer(this.inventory[i])) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Slot", (byte)i);
        this.inventory[i].writeToNBT(nbt);
        contentList.appendTag((NBTBase)nbt);
      } 
    } 
    StackUtil.getOrCreateNbtData(stack).setTag("Items", (NBTBase)contentList);
    assert StackUtil.getOrCreateNbtData(stack).getInteger("uid") == 0;
    func_174888_l();
  }
  
  public void func_174888_l() {
    for (int i = 0; i < this.inventory.length; i++)
      this.inventory[i] = null; 
    this.cleared = true;
  }
  
  public SlotHologramSlot.ChangeCallback makeSaveCallback() {
    return new SlotHologramSlot.ChangeCallback() {
        public void onChanged(int index) {
          HandHeldInventory.this.save();
        }
      };
  }
  
  public void onEvent(String event) {}
  
  public static void addMaintainedPlayer(EntityPlayer player) {
    PLAYERS_IN_GUI.add(player);
  }
  
  private static final Set<EntityPlayer> PLAYERS_IN_GUI = new HashSet<>();
}
