package ic2.core.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class KineticGeneratorRotor extends ModelBase {
  ModelRenderer rotor1;
  
  ModelRenderer rotor2;
  
  ModelRenderer rotor3;
  
  ModelRenderer rotor4;
  
  public KineticGeneratorRotor(int radius) {
    this.field_78090_t = 32;
    this.field_78089_u = 256;
    this.rotor1 = new ModelRenderer(this, 0, 0);
    this.rotor1.func_78789_a(0.0F, 0.0F, -4.0F, 1, radius * 8, 8);
    this.rotor1.func_78793_a(-8.0F, 0.0F, 0.0F);
    this.rotor1.func_78787_b(32, 256);
    this.rotor1.field_78809_i = true;
    setRotation(this.rotor1, 0.0F, -0.5F, 0.0F);
    this.rotor2 = new ModelRenderer(this, 0, 0);
    this.rotor2.func_78789_a(0.0F, 0.0F, -4.0F, 1, radius * 8, 8);
    this.rotor2.func_78793_a(-8.0F, 0.0F, 0.0F);
    this.rotor2.func_78787_b(32, 256);
    this.rotor2.field_78809_i = true;
    setRotation(this.rotor2, 3.1F, 0.5F, 0.0F);
    this.rotor3 = new ModelRenderer(this, 0, 0);
    this.rotor3.func_78789_a(0.0F, 0.0F, -4.0F, 1, radius * 8, 8);
    this.rotor3.func_78793_a(-8.0F, 0.0F, 0.0F);
    this.rotor3.func_78787_b(32, 256);
    this.rotor3.field_78809_i = true;
    setRotation(this.rotor3, 4.7F, 0.0F, 0.5F);
    this.rotor4 = new ModelRenderer(this, 0, 0);
    this.rotor4.func_78789_a(0.0F, 0.0F, -4.0F, 1, radius * 8, 8);
    this.rotor4.func_78793_a(-8.0F, 0.0F, 0.0F);
    this.rotor4.func_78787_b(32, 256);
    this.rotor4.field_78809_i = true;
    setRotation(this.rotor4, 1.5F, 0.0F, -0.5F);
  }
  
  public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float scale) {
    this.rotor1.func_78785_a(scale);
    this.rotor2.func_78785_a(scale);
    this.rotor3.func_78785_a(scale);
    this.rotor4.func_78785_a(scale);
  }
  
  private static void setRotation(ModelRenderer model, float x, float y, float z) {
    model.field_78795_f = x;
    model.field_78796_g = y;
    model.field_78808_h = z;
  }
}
