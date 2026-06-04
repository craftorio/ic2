// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;

public class PlasmaLauncher extends ItemElectricTool
{
    public PlasmaLauncher() {
        super(ItemName.plasma_launcher, 100);
        this.maxCharge = 40000;
        this.transferLimit = 128;
        this.tier = 3;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (!IC2.platform.isSimulating()) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)StackUtil.get(player, hand));
        }
        final EntityParticle particle = new EntityParticle(world, (EntityLivingBase)player, 8.0f, 1.0, 2.0);
        world.spawnEntity((Entity)particle);
        return super.onItemRightClick(world, player, hand);
    }
}
