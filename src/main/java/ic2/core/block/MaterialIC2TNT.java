// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialIC2TNT extends Material
{
    public static Material instance;
    
    public MaterialIC2TNT() {
        super(MapColor.TNT);
        this.setAdventureModeExempt();
        this.setBurning();
    }
    
    public boolean isOpaque() {
        return false;
    }
    
    static {
        MaterialIC2TNT.instance = new MaterialIC2TNT();
    }
}
