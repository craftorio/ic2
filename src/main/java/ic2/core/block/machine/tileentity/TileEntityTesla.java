// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.Random;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraft.util.DamageSource;
import ic2.core.IC2DamageSource;
import ic2.core.item.armor.ItemArmorHazmat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.TileEntityBlock;

public class TileEntityTesla extends TileEntityBlock
{
    protected final Redstone redstone;
    protected final Energy energy;
    private int ticker;
    
    public TileEntityTesla() {
        this.ticker = IC2.random.nextInt(32);
        this.redstone = this.addComponent(new Redstone(this));
        this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 2));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (!this.redstone.hasRedstoneInput()) {
            return;
        }
        if (this.energy.useEnergy(1.0) && ++this.ticker % 32 == 0) {
            final int damage = (int)this.energy.getEnergy() / 400;
            if (damage > 0 && this.shock(damage)) {
                this.energy.useEnergy(damage * 400);
            }
        }
    }
    
    protected boolean shock(final int damage) {
        final int r = 4;
        final World world = this.getWorld();
        final List<EntityLivingBase> entities = world.getEntitiesWithinAABB((Class)EntityLivingBase.class, new AxisAlignedBB((double)(this.pos.getX() - 4), (double)(this.pos.getY() - 4), (double)(this.pos.getZ() - 4), (double)(this.pos.getX() + 4 + 1), (double)(this.pos.getY() + 4 + 1), (double)(this.pos.getZ() + 4 + 1)));
        for (final EntityLivingBase entity : entities) {
            if (ItemArmorHazmat.hasCompleteHazmat(entity)) {
                continue;
            }
            if (entity.attackEntityFrom((DamageSource)IC2DamageSource.electricity, (float)damage)) {
                if (world instanceof WorldServer) {
                    final WorldServer worldServer = (WorldServer)world;
                    final Random rnd = world.rand;
                    for (int i = 0; i < damage; ++i) {
                        worldServer.spawnParticle(EnumParticleTypes.REDSTONE, true, entity.posX + rnd.nextFloat() - 0.5, entity.posY + rnd.nextFloat() * 2.0f - 1.0, entity.posZ + rnd.nextFloat() - 0.5, 0, 0.1, 0.1, 1.0, 1.0, new int[0]);
                    }
                }
                return true;
            }
        }
        return false;
    }
}
