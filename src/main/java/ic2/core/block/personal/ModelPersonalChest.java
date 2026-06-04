// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;

@SideOnly(Side.CLIENT)
public class ModelPersonalChest extends ModelBase
{
    private final ModelRenderer door;
    
    public ModelPersonalChest() {
        this.textureWidth = 64;
        this.textureHeight = 64;
        (this.door = new ModelRenderer((ModelBase)this, 30, 0)).addBox(2.0f, 1.0f, 2.0f, 12, 14, 1, true);
        this.door.setTextureSize(64, 64);
    }
    
    public void render() {
        this.door.render(0.0625f);
    }
}
