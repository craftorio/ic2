package ic2.api.upgrade;

import java.util.Set;

public interface IUpgradableBlock {
   double getEnergy();

   boolean useEnergy(double var1);

   Set<UpgradableProperty> getUpgradableProperties();
}
