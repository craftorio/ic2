package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.ITeBlock;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.init.MainConfig;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityNuke extends TileEntityBridgeNuke implements IHasGui {
  public int RadiationRange;
  
  public final InvSlotConsumable outsideSlot;
  
  public final InvSlotConsumable insideSlot;
  
  public static Class<? extends TileEntityBridgeNuke> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityBridgeNuke.TileEntityClassicNuke.class : (Class)TileEntityNuke.class;
  }
  
  public TileEntityNuke() {
    this
      
      .insideSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "insideSlot", 1, new ItemStack[] { BlockName.resource.getItemStack((Enum)ResourceBlock.uranium_block), ItemName.nuclear.getItemStack((Enum)NuclearResourceType.uranium_238), ItemName.nuclear.getItemStack((Enum)NuclearResourceType.uranium_235), ItemName.nuclear.getItemStack((Enum)NuclearResourceType.small_uranium_235), ItemName.nuclear.getItemStack((Enum)NuclearResourceType.plutonium), ItemName.nuclear.getItemStack((Enum)NuclearResourceType.small_plutonium) });
    this.outsideSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "outsideSlot", 1, new ItemStack[] { getBlockType().getItemStack((ITeBlock)TeBlock.itnt) });
  }
  
  public int getRadiationRange() {
    return this.RadiationRange;
  }
  
  public void setRadiationRange(int range) {
    if (range != this.RadiationRange)
      this.RadiationRange = range; 
  }
  
  public float getNukeExplosivePower() {
    if (this.outsideSlot.isEmpty())
      return -1.0F; 
    int itntCount = StackUtil.getSize(this.outsideSlot.get());
    double ret = 5.0D * Math.pow(itntCount, 0.3333333333333333D);
    if (this.insideSlot.isEmpty()) {
      setRadiationRange(0);
    } else {
      ItemStack inside = this.insideSlot.get();
      int insideCount = StackUtil.getSize(inside);
      if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.uranium_238))) {
        setRadiationRange(itntCount);
      } else if (StackUtil.checkItemEquality(inside, BlockName.resource.getItemStack((Enum)ResourceBlock.uranium_block))) {
        setRadiationRange(itntCount * 6);
      } else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.small_uranium_235))) {
        setRadiationRange(itntCount * 2);
        if (itntCount >= 64)
          ret += 0.05555555555555555D * Math.pow(insideCount, 1.6D); 
      } else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.uranium_235))) {
        setRadiationRange(itntCount * 2);
        if (itntCount >= 32)
          ret += 0.5D * Math.pow(insideCount, 1.4D); 
      } else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.small_plutonium))) {
        setRadiationRange(itntCount * 3);
        if (itntCount >= 32)
          ret += 0.05555555555555555D * Math.pow(insideCount, 2.0D); 
      } else if (StackUtil.checkItemEquality(inside, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.plutonium))) {
        setRadiationRange(itntCount * 4);
        if (itntCount >= 16)
          ret += 0.5D * Math.pow(insideCount, 1.8D); 
      } 
    } 
    ret = Math.min(ret, ConfigUtil.getFloat(MainConfig.get(), "protection/nukeExplosionPowerLimit"));
    return (float)ret;
  }
  
  public ContainerBase<TileEntityNuke> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityNuke>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  protected void onIgnite(EntityLivingBase igniter) {
    super.onIgnite(igniter);
    this.outsideSlot.clear();
    this.insideSlot.clear();
  }
}
