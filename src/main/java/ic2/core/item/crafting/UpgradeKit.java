package ic2.core.item.crafting;

import ic2.core.IC2;
import ic2.core.block.wiring.TileEntityChargepadMFSU;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.block.wiring.TileEntityElectricMFSU;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.item.type.UpdateKitType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UpgradeKit extends ItemMulti<UpdateKitType> {
  public UpgradeKit() {
    super(ItemName.upgrade_kit, UpdateKitType.class);
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    if (!IC2.platform.isSimulating())
      return EnumActionResult.PASS; 
    UpdateKitType type = (UpdateKitType)getType(StackUtil.get(player, hand));
    if (type == null)
      return EnumActionResult.PASS; 
    boolean ret = false;
    switch (type) {
      case mfsu:
        ret = upgradeToMfsu(world, pos);
        break;
    } 
    if (!ret)
      return EnumActionResult.PASS; 
    StackUtil.consumeOrError(player, hand, 1);
    return EnumActionResult.SUCCESS;
  }
  
  private static boolean upgradeToMfsu(World world, BlockPos pos) {
    TileEntityChargepadMFSU tileEntityChargepadMFSU;
    TileEntity te = world.getTileEntity(pos);
    if (!(te instanceof ic2.core.block.TileEntityBlock))
      return false; 
    TileEntityElectricBlock replacement = null;
    if (te instanceof ic2.core.block.wiring.TileEntityElectricMFE) {
      TileEntityElectricMFSU tileEntityElectricMFSU = new TileEntityElectricMFSU();
    } else if (te instanceof ic2.core.block.wiring.TileEntityChargepadMFE) {
      tileEntityChargepadMFSU = new TileEntityChargepadMFSU();
    } 
    if (tileEntityChargepadMFSU != null) {
      NBTTagCompound nbt = new NBTTagCompound();
      te.writeToNBT(nbt);
      tileEntityChargepadMFSU.readFromNBT(nbt);
      world.func_175690_a(pos, (TileEntity)tileEntityChargepadMFSU);
      tileEntityChargepadMFSU.onUpgraded();
      tileEntityChargepadMFSU.markDirty();
      return true;
    } 
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    UpdateKitType type = (UpdateKitType)getType(stack);
    if (type == null)
      return; 
    switch (type) {
      case mfsu:
        tooltip.add(Localization.translate("ic2.upgrade_kit.mfsu.info"));
        break;
    } 
  }
}
