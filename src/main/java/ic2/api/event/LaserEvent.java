package ic2.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LaserEvent extends WorldEvent {
   public final Entity lasershot;
   public EntityLivingBase owner;
   public float range;
   public float power;
   public int blockBreaks;
   public boolean explosive;
   public boolean smelt;

   public LaserEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
      super(world);
      this.lasershot = lasershot;
      this.owner = owner;
      this.range = range;
      this.power = power;
      this.blockBreaks = blockBreaks;
      this.explosive = explosive;
      this.smelt = smelt;
   }

   public static class LaserExplodesEvent extends LaserEvent {
      public float explosionPower;
      public float explosionDropRate;
      public float explosionEntityDamage;

      public LaserExplodesEvent(
         World world,
         Entity lasershot,
         EntityLivingBase owner,
         float range,
         float power,
         int blockBreaks,
         boolean explosive,
         boolean smelt,
         float explosionpower1,
         float explosiondroprate1,
         float explosionentitydamage1
      ) {
         super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
         this.explosionPower = explosionpower1;
         this.explosionDropRate = explosiondroprate1;
         this.explosionEntityDamage = explosionentitydamage1;
      }
   }

   public static class LaserHitsBlockEvent extends LaserEvent {
      public BlockPos pos;
      public final EnumFacing side;
      public boolean removeBlock;
      public boolean dropBlock;
      public float dropChance;

      public LaserHitsBlockEvent(
         World world,
         Entity lasershot,
         EntityLivingBase owner,
         float range,
         float power,
         int blockBreaks,
         boolean explosive1,
         boolean smelt1,
         BlockPos pos,
         EnumFacing side,
         float dropChance,
         boolean removeBlock,
         boolean dropBlock
      ) {
         super(world, lasershot, owner, range, power, blockBreaks, explosive1, smelt1);
         this.pos = pos;
         this.side = side;
         this.removeBlock = removeBlock;
         this.dropBlock = dropBlock;
         this.dropChance = dropChance;
      }
   }

   public static class LaserHitsEntityEvent extends LaserEvent {
      public Entity hitEntity;
      public boolean passThrough;

      public LaserHitsEntityEvent(
         World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, Entity hitentity
      ) {
         super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
         this.hitEntity = hitentity;
      }
   }

   public static class LaserShootEvent extends LaserEvent {
      public final ItemStack laserItem;

      public LaserShootEvent(
         World world,
         Entity lasershot,
         EntityLivingBase owner,
         float range,
         float power,
         int blockBreaks,
         boolean explosive,
         boolean smelt,
         ItemStack laseritem
      ) {
         super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
         this.laserItem = laseritem;
      }
   }
}
