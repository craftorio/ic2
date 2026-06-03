package ic2.core;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHitSoundOverride {
  @SideOnly(Side.CLIENT)
  String getHitSoundForBlock(EntityPlayerSP paramEntityPlayerSP, World paramWorld, BlockPos paramBlockPos, ItemStack paramItemStack);
  
  @SideOnly(Side.CLIENT)
  String getBreakSoundForBlock(EntityPlayerSP paramEntityPlayerSP, World paramWorld, BlockPos paramBlockPos, ItemStack paramItemStack);
}
