// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderBoat;

@SideOnly(Side.CLIENT)
public class RenderIC2Boat extends RenderBoat
{
    public RenderIC2Boat(final RenderManager manager) {
        super(manager);
    }
    
    protected ResourceLocation getEntityTexture(final EntityBoat entity) {
        return new ResourceLocation("ic2", ((EntityIC2Boat)entity).getTexture());
    }
}
