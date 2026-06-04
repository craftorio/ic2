// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBase;

public class KineticGeneratorRotor extends ModelBase
{
    ModelRenderer rotor1;
    ModelRenderer rotor2;
    ModelRenderer rotor3;
    ModelRenderer rotor4;
    
    public KineticGeneratorRotor(final int radius) {
        this.textureWidth = 32;
        this.textureHeight = 256;
        (this.rotor1 = new ModelRenderer((ModelBase)this, 0, 0)).addBox(0.0f, 0.0f, -4.0f, 1, radius * 8, 8);
        this.rotor1.setRotationPoint(-8.0f, 0.0f, 0.0f);
        this.rotor1.setTextureSize(32, 256);
        this.rotor1.mirror = true;
        setRotation(this.rotor1, 0.0f, -0.5f, 0.0f);
        (this.rotor2 = new ModelRenderer((ModelBase)this, 0, 0)).addBox(0.0f, 0.0f, -4.0f, 1, radius * 8, 8);
        this.rotor2.setRotationPoint(-8.0f, 0.0f, 0.0f);
        this.rotor2.setTextureSize(32, 256);
        this.rotor2.mirror = true;
        setRotation(this.rotor2, 3.1f, 0.5f, 0.0f);
        (this.rotor3 = new ModelRenderer((ModelBase)this, 0, 0)).addBox(0.0f, 0.0f, -4.0f, 1, radius * 8, 8);
        this.rotor3.setRotationPoint(-8.0f, 0.0f, 0.0f);
        this.rotor3.setTextureSize(32, 256);
        this.rotor3.mirror = true;
        setRotation(this.rotor3, 4.7f, 0.0f, 0.5f);
        (this.rotor4 = new ModelRenderer((ModelBase)this, 0, 0)).addBox(0.0f, 0.0f, -4.0f, 1, radius * 8, 8);
        this.rotor4.setRotationPoint(-8.0f, 0.0f, 0.0f);
        this.rotor4.setTextureSize(32, 256);
        this.rotor4.mirror = true;
        setRotation(this.rotor4, 1.5f, 0.0f, -0.5f);
    }
    
    public void render(final Entity entity, final float f, final float f1, final float f2, final float f3, final float f4, final float scale) {
        this.rotor1.render(scale);
        this.rotor2.render(scale);
        this.rotor3.render(scale);
        this.rotor4.render(scale);
    }
    
    private static void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
