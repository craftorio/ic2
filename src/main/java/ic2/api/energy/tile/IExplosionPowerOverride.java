package ic2.api.energy.tile;

public interface IExplosionPowerOverride {
   boolean shouldExplode();

   float getExplosionPower(int var1, float var2);
}
