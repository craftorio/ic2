// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.reactor;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ic2.api.info.ILocatable;

public interface IReactor extends ILocatable
{
    TileEntity getCoreTe();
    
    int getHeat();
    
    void setHeat(final int p0);
    
    int addHeat(final int p0);
    
    int getMaxHeat();
    
    void setMaxHeat(final int p0);
    
    void addEmitHeat(final int p0);
    
    float getHeatEffectModifier();
    
    void setHeatEffectModifier(final float p0);
    
    float getReactorEnergyOutput();
    
    double getReactorEUEnergyOutput();
    
    float addOutput(final float p0);
    
    ItemStack getItemAt(final int p0, final int p1);
    
    void setItemAt(final int p0, final int p1, final ItemStack p2);
    
    void explode();
    
    int getTickRate();
    
    boolean produceEnergy();
    
    boolean isFluidCooled();
}
