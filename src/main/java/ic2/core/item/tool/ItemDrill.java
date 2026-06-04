// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.util.StackUtil;
import java.util.Iterator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.client.entity.EntityPlayerSP;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.ref.ItemName;
import ic2.core.IHitSoundOverride;
import ic2.api.item.IMiningDrill;

public class ItemDrill extends ItemElectricTool implements IMiningDrill, IHitSoundOverride
{
    public ItemDrill(final ItemName name, final int operationEnergyCost, final HarvestLevel harvestLevel, final int maxCharge, final int transferLimit, final int tier, final float efficiency) {
        super(name, operationEnergyCost, harvestLevel, EnumSet.of(ToolClass.Pickaxe, ToolClass.Shovel));
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.efficiency = efficiency;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getHitSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getBreakSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        if (player.capabilities.isCreativeMode) {
            return null;
        }
        final IBlockState state = world.getBlockState(pos);
        final float hardness = state.getBlockHardness(world, pos);
        return (hardness > 1.0f || hardness < 0.0f) ? "Tools/Drill/DrillHard.ogg" : "Tools/Drill/DrillSoft.ogg";
    }
    
    @Override
    public float getDestroySpeed(final ItemStack stack, final IBlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        final EntityPlayer player = getPlayerHoldingItem(stack);
        if (player != null) {
            if (player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier((EntityLivingBase)player)) {
                speed *= 5.0f;
            }
            if (!player.onGround) {
                speed *= 5.0f;
            }
        }
        return speed;
    }
    
    private static EntityPlayer getPlayerHoldingItem(final ItemStack stack) {
        if (IC2.platform.isRendering()) {
            final EntityPlayer player = IC2.platform.getPlayerInstance();
            if (player != null && player.inventory.getCurrentItem() == stack) {
                return player;
            }
        }
        else {
            for (final EntityPlayer player2 : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                if (player2.inventory.getCurrentItem() == stack) {
                    return player2;
                }
            }
        }
        return null;
    }
    
    @Override
    public int energyUse(final ItemStack stack, final World world, final BlockPos pos, final IBlockState state) {
        if (stack.getItem() == ItemName.drill.getInstance()) {
            return 6;
        }
        if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
            return 20;
        }
        if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
            return 200;
        }
        throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
    
    @Override
    public int breakTime(final ItemStack stack, final World world, final BlockPos pos, final IBlockState state) {
        if (stack.getItem() == ItemName.drill.getInstance()) {
            return 200;
        }
        if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
            return 50;
        }
        if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
            return 20;
        }
        throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
    
    @Override
    public boolean breakBlock(final ItemStack stack, final World world, final BlockPos pos, final IBlockState state) {
        if (stack.getItem() == ItemName.drill.getInstance()) {
            return this.tryUsePower(stack, 50.0);
        }
        if (stack.getItem() == ItemName.diamond_drill.getInstance()) {
            return this.tryUsePower(stack, 80.0);
        }
        if (stack.getItem() == ItemName.iridium_drill.getInstance()) {
            return this.tryUsePower(stack, 800.0);
        }
        throw new IllegalArgumentException("Invalid drill: " + StackUtil.toStringSafe(stack));
    }
}
