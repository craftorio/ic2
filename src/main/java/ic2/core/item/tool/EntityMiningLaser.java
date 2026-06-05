package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.util.Vector3;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityMiningLaser extends Entity implements IThrowableEntity {
   public float range = 0.0F;
   public float power = 0.0F;
   public int blockBreaks = 0;
   public boolean explosive = false;
   public static final double laserSpeed = 1.0;
   public EntityLivingBase owner;
   public boolean headingSet = false;
   public boolean smelt = false;
   private int ticksInAir = 0;

   public EntityMiningLaser(World world) {
      super(world);
      this.setSize(0.8F, 0.8F);
   }

   public EntityMiningLaser(World world, Vector3 start, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive) {
      super(world);
      this.owner = owner;
      this.setSize(0.8F, 0.8F);
      this.setPosition(start.x, start.y, start.z);
      this.setLaserHeading(dir.x, dir.y, dir.z, 1.0);
      this.range = range;
      this.power = power;
      this.blockBreaks = blockBreaks;
      this.explosive = explosive;
   }

   protected void entityInit() {
   }

   public void setLaserHeading(double motionX, double motionY, double motionZ, double speed) {
      double currentSpeed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
      this.motionX = motionX / currentSpeed * speed;
      this.motionY = motionY / currentSpeed * speed;
      this.motionZ = motionZ / currentSpeed * speed;
      this.prevRotationYaw = this.rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ));
      this.prevRotationPitch = this.rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, Math.sqrt(motionX * motionX + motionZ * motionZ)));
      this.headingSet = true;
   }

   public void setVelocity(double motionX, double motionY, double motionZ) {
      this.setLaserHeading(motionX, motionY, motionZ, 1.0);
   }

   public void onUpdate() {
      super.onUpdate();
      if (!IC2.platform.isSimulating() || !(this.range < 1.0F) && !(this.power <= 0.0F) && this.blockBreaks > 0) {
         this.ticksInAir++;
         Vec3d oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d newPosition = new Vec3d(
            this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ
         );
         World world = this.getEntityWorld();
         RayTraceResult result = world.rayTraceBlocks(oldPosition, newPosition, false, true, false);
         oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
         if (result != null) {
            newPosition = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
         } else {
            newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         }

         Entity hitEntity = null;
         List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(
            this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0)
         );
         double distance = 0.0;

         for (Entity entity : list) {
            if (entity.canBeCollidedWith() && (entity != this.owner || this.ticksInAir >= 5)) {
               AxisAlignedBB hitBox = entity.getEntityBoundingBox().grow(0.3);
               RayTraceResult intercept = hitBox.calculateIntercept(oldPosition, newPosition);
               if (intercept != null) {
                  double newDistance = oldPosition.distanceTo(intercept.hitVec);
                  if (newDistance < distance || distance == 0.0) {
                     hitEntity = entity;
                     distance = newDistance;
                  }
               }
            }
         }

         RayTraceResult blockHit = result;
         if (hitEntity != null) {
            result = new RayTraceResult(hitEntity);
         }

         if (result != null && result.typeOfHit != Type.MISS && !world.isRemote) {
            if (this.explosive) {
               this.explode();
               this.setDead();
               return;
            }

            switch (result.typeOfHit) {
               case ENTITY:
                  if (this.hitEntity(result.entityHit)) {
                     break;
                  }

                  if (blockHit == null) {
                     this.power -= 0.5F;
                     break;
                  } else {
                     result = blockHit;
                  }
               case BLOCK:
                  if (!this.hitBlock(result.getBlockPos(), result.sideHit)) {
                     this.power -= 0.5F;
                  }
                  break;
               default:
                  throw new RuntimeException("invalid hit type: " + result.typeOfHit);
            }
         } else {
            this.power -= 0.5F;
         }

         this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         this.range = (float)(
            this.range - Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ)
         );
         if (this.isInWater()) {
            this.setDead();
         }
      } else {
         if (this.explosive) {
            this.explode();
         }

         this.setDead();
      }
   }

   private void explode() {
      World world = this.getEntityWorld();
      LaserEvent.LaserExplodesEvent event = new LaserEvent.LaserExplodesEvent(
         world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0F, 0.85F, 0.55F
      );
      if (MinecraftForge.EVENT_BUS.post(event)) {
         this.setDead();
      } else {
         this.copyDataFromEvent(event);
         ExplosionIC2 explosion = new ExplosionIC2(
            world, this, this.posX, this.posY, this.posZ, event.explosionPower, event.explosionDropRate
         );
         explosion.doExplosion();
      }
   }

   private boolean hitEntity(Entity entity) {
      LaserEvent.LaserHitsEntityEvent event = new LaserEvent.LaserHitsEntityEvent(
         this.getEntityWorld(), this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, entity
      );
      if (MinecraftForge.EVENT_BUS.post(event)) {
         if (event.passThrough) {
            return false;
         }

         this.setDead();
         return true;
      } else {
         this.copyDataFromEvent(event);
         entity = event.hitEntity;
         int damage = (int)this.power;
         if (damage > 0) {
            entity.setFire(damage * (this.smelt ? 2 : 1));
            if (entity.attackEntityFrom(new EntityDamageSourceIndirect("arrow", this, this.owner).setProjectile(), damage)
               && (
                  this.owner instanceof EntityPlayer && entity instanceof EntityDragon && ((EntityDragon)entity).getHealth() <= 0.0F
                     || entity instanceof MultiPartEntityPart
                        && ((MultiPartEntityPart)entity).parent instanceof EntityDragon
                        && ((EntityLivingBase)((MultiPartEntityPart)entity).parent).getHealth() <= 0.0F
               )) {
               IC2.achievements.issueAchievement((EntityPlayer)this.owner, "killDragonMiningLaser");
            }
         }

         this.setDead();
         return true;
      }
   }

   private boolean hitBlock(BlockPos pos, EnumFacing side) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: Constructor net/minecraftforge/event/world/BlockEvent$BreakEvent.<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;)V not found
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil.getSyntheticParametersMask(ExprUtil.java:49)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:982)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent.toJava(NewExprent.java:462)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1054)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1151)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:921)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.IfExprent.toJava(IfExprent.java:95)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:210)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1351)
      //
      // Bytecode:
      // 000: aload 0
      // 001: invokevirtual ic2/core/item/tool/EntityMiningLaser.getEntityWorld ()Lnet/minecraft/world/World;
      // 004: astore 3
      // 005: new ic2/api/event/LaserEvent$LaserHitsBlockEvent
      // 008: dup
      // 009: aload 3
      // 00a: aload 0
      // 00b: aload 0
      // 00c: getfield ic2/core/item/tool/EntityMiningLaser.owner Lnet/minecraft/entity/EntityLivingBase;
      // 00f: aload 0
      // 010: getfield ic2/core/item/tool/EntityMiningLaser.range F
      // 013: aload 0
      // 014: getfield ic2/core/item/tool/EntityMiningLaser.power F
      // 017: aload 0
      // 018: getfield ic2/core/item/tool/EntityMiningLaser.blockBreaks I
      // 01b: aload 0
      // 01c: getfield ic2/core/item/tool/EntityMiningLaser.explosive Z
      // 01f: aload 0
      // 020: getfield ic2/core/item/tool/EntityMiningLaser.smelt Z
      // 023: aload 1
      // 024: aload 2
      // 025: ldc_w 0.9
      // 028: bipush 1
      // 029: bipush 1
      // 02a: invokespecial ic2/api/event/LaserEvent$LaserHitsBlockEvent.<init> (Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EntityLivingBase;FFIZZLnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FZZ)V
      // 02d: astore 4
      // 02f: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 032: aload 4
      // 034: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.post (Lnet/minecraftforge/fml/common/eventhandler/Event;)Z
      // 037: ifeq 040
      // 03a: aload 0
      // 03b: invokevirtual ic2/core/item/tool/EntityMiningLaser.setDead ()V
      // 03e: bipush 1
      // 03f: ireturn
      // 040: aload 0
      // 041: aload 4
      // 043: invokevirtual ic2/core/item/tool/EntityMiningLaser.copyDataFromEvent (Lic2/api/event/LaserEvent;)V
      // 046: aload 3
      // 047: aload 4
      // 049: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 04c: invokevirtual net/minecraft/world/World.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
      // 04f: astore 5
      // 051: aload 5
      // 053: invokeinterface net/minecraft/block/state/IBlockState.getBlock ()Lnet/minecraft/block/Block; 1
      // 058: astore 6
      // 05a: aload 0
      // 05b: getfield ic2/core/item/tool/EntityMiningLaser.owner Lnet/minecraft/entity/EntityLivingBase;
      // 05e: instanceof net/minecraft/entity/player/EntityPlayer
      // 061: ifeq 06e
      // 064: aload 0
      // 065: getfield ic2/core/item/tool/EntityMiningLaser.owner Lnet/minecraft/entity/EntityLivingBase;
      // 068: checkcast net/minecraft/entity/player/EntityPlayer
      // 06b: goto 072
      // 06e: aload 3
      // 06f: invokestatic ic2/core/Ic2Player.get (Lnet/minecraft/world/World;)Lnet/minecraft/entity/player/EntityPlayer;
      // 072: astore 7
      // 074: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 077: new net/minecraftforge/event/world/BlockEvent$BreakEvent
      // 07a: dup
      // 07b: aload 3
      // 07c: aload 1
      // 07d: aload 5
      // 07f: aload 7
      // 081: invokespecial net/minecraftforge/event/world/BlockEvent$BreakEvent.<init> (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;)V
      // 084: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.post (Lnet/minecraftforge/fml/common/eventhandler/Event;)Z
      // 087: ifeq 090
      // 08a: aload 0
      // 08b: invokevirtual ic2/core/item/tool/EntityMiningLaser.setDead ()V
      // 08e: bipush 1
      // 08f: ireturn
      // 090: aload 6
      // 092: aload 5
      // 094: aload 3
      // 095: aload 4
      // 097: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 09a: invokevirtual net/minecraft/block/Block.isAir (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z
      // 09d: ifne 0bb
      // 0a0: aload 6
      // 0a2: getstatic net/minecraft/init/Blocks.GLASS Lnet/minecraft/block/Block;
      // 0a5: if_acmpeq 0bb
      // 0a8: aload 6
      // 0aa: getstatic net/minecraft/init/Blocks.GLASS_PANE Lnet/minecraft/block/Block;
      // 0ad: if_acmpeq 0bb
      // 0b0: aload 6
      // 0b2: getstatic ic2/core/ref/BlockName.glass Lic2/core/ref/BlockName;
      // 0b5: invokevirtual ic2/core/ref/BlockName.getInstance ()Lnet/minecraft/block/Block;
      // 0b8: if_acmpne 0bd
      // 0bb: bipush 0
      // 0bc: ireturn
      // 0bd: aload 3
      // 0be: getfield net/minecraft/world/World.isRemote Z
      // 0c1: ifeq 0c6
      // 0c4: bipush 1
      // 0c5: ireturn
      // 0c6: aload 5
      // 0c8: aload 3
      // 0c9: aload 4
      // 0cb: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 0ce: invokeinterface net/minecraft/block/state/IBlockState.getBlockHardness (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F 3
      // 0d3: fstore 8
      // 0d5: fload 8
      // 0d7: fconst_0
      // 0d8: fcmpg
      // 0d9: ifge 0e2
      // 0dc: aload 0
      // 0dd: invokevirtual ic2/core/item/tool/EntityMiningLaser.setDead ()V
      // 0e0: bipush 1
      // 0e1: ireturn
      // 0e2: aload 0
      // 0e3: dup
      // 0e4: getfield ic2/core/item/tool/EntityMiningLaser.power F
      // 0e7: fload 8
      // 0e9: ldc_w 1.5
      // 0ec: fdiv
      // 0ed: fsub
      // 0ee: putfield ic2/core/item/tool/EntityMiningLaser.power F
      // 0f1: aload 0
      // 0f2: getfield ic2/core/item/tool/EntityMiningLaser.power F
      // 0f5: fconst_0
      // 0f6: fcmpg
      // 0f7: ifge 0fc
      // 0fa: bipush 1
      // 0fb: ireturn
      // 0fc: new java/util/ArrayList
      // 0ff: dup
      // 100: invokespecial java/util/ArrayList.<init> ()V
      // 103: astore 9
      // 105: aload 5
      // 107: invokeinterface net/minecraft/block/state/IBlockState.getMaterial ()Lnet/minecraft/block/material/Material; 1
      // 10c: getstatic net/minecraft/block/material/Material.TNT Lnet/minecraft/block/material/Material;
      // 10f: if_acmpeq 11f
      // 112: aload 5
      // 114: invokeinterface net/minecraft/block/state/IBlockState.getMaterial ()Lnet/minecraft/block/material/Material; 1
      // 119: getstatic ic2/core/block/MaterialIC2TNT.instance Lnet/minecraft/block/material/Material;
      // 11c: if_acmpne 160
      // 11f: aload 6
      // 121: aload 3
      // 122: aload 4
      // 124: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 127: new net/minecraft/world/Explosion
      // 12a: dup
      // 12b: aload 3
      // 12c: aload 0
      // 12d: aload 4
      // 12f: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 132: invokevirtual net/minecraft/util/math/BlockPos.getX ()I
      // 135: i2d
      // 136: ldc2_w 0.5
      // 139: dadd
      // 13a: aload 4
      // 13c: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 13f: invokevirtual net/minecraft/util/math/BlockPos.getY ()I
      // 142: i2d
      // 143: ldc2_w 0.5
      // 146: dadd
      // 147: aload 4
      // 149: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 14c: invokevirtual net/minecraft/util/math/BlockPos.getZ ()I
      // 14f: i2d
      // 150: ldc2_w 0.5
      // 153: dadd
      // 154: fconst_1
      // 155: bipush 0
      // 156: bipush 1
      // 157: invokespecial net/minecraft/world/Explosion.<init> (Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZZ)V
      // 15a: invokevirtual net/minecraft/block/Block.onBlockDestroyedByExplosion (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/Explosion;)V
      // 15d: goto 1d3
      // 160: aload 0
      // 161: getfield ic2/core/item/tool/EntityMiningLaser.smelt Z
      // 164: ifeq 1d3
      // 167: aload 5
      // 169: invokeinterface net/minecraft/block/state/IBlockState.getMaterial ()Lnet/minecraft/block/material/Material; 1
      // 16e: getstatic net/minecraft/block/material/Material.WOOD Lnet/minecraft/block/material/Material;
      // 171: if_acmpne 17d
      // 174: aload 4
      // 176: bipush 0
      // 177: putfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.dropBlock Z
      // 17a: goto 1d3
      // 17d: aload 3
      // 17e: aload 4
      // 180: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 183: aload 5
      // 185: aload 6
      // 187: bipush 0
      // 188: invokestatic ic2/core/util/StackUtil.getDrops (Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;I)Ljava/util/List;
      // 18b: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 190: astore 10
      // 192: aload 10
      // 194: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 199: ifeq 1c7
      // 19c: aload 10
      // 19e: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 1a3: checkcast net/minecraft/item/ItemStack
      // 1a6: astore 11
      // 1a8: invokestatic net/minecraft/item/crafting/FurnaceRecipes.instance ()Lnet/minecraft/item/crafting/FurnaceRecipes;
      // 1ab: aload 11
      // 1ad: invokevirtual net/minecraft/item/crafting/FurnaceRecipes.getSmeltingResult (Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;
      // 1b0: astore 12
      // 1b2: aload 12
      // 1b4: invokestatic ic2/core/util/StackUtil.isEmpty (Lnet/minecraft/item/ItemStack;)Z
      // 1b7: ifne 1c4
      // 1ba: aload 9
      // 1bc: aload 12
      // 1be: invokeinterface java/util/List.add (Ljava/lang/Object;)Z 2
      // 1c3: pop
      // 1c4: goto 192
      // 1c7: aload 4
      // 1c9: aload 9
      // 1cb: invokeinterface java/util/List.isEmpty ()Z 1
      // 1d0: putfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.dropBlock Z
      // 1d3: aload 4
      // 1d5: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.removeBlock Z
      // 1d8: ifeq 269
      // 1db: aload 4
      // 1dd: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.dropBlock Z
      // 1e0: ifeq 1f6
      // 1e3: aload 6
      // 1e5: aload 3
      // 1e6: aload 4
      // 1e8: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 1eb: aload 5
      // 1ed: aload 4
      // 1ef: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.dropChance F
      // 1f2: bipush 0
      // 1f3: invokevirtual net/minecraft/block/Block.dropBlockAsItemWithChance (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V
      // 1f6: aload 3
      // 1f7: aload 4
      // 1f9: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 1fc: invokevirtual net/minecraft/world/World.setBlockToAir (Lnet/minecraft/util/math/BlockPos;)Z
      // 1ff: pop
      // 200: aload 9
      // 202: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 207: astore 10
      // 209: aload 10
      // 20b: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 210: ifeq 240
      // 213: aload 10
      // 215: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 21a: checkcast net/minecraft/item/ItemStack
      // 21d: astore 11
      // 21f: aload 11
      // 221: aload 3
      // 222: aload 4
      // 224: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 227: invokestatic ic2/core/util/StackUtil.placeBlock (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z
      // 22a: ifne 238
      // 22d: aload 3
      // 22e: aload 4
      // 230: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 233: aload 11
      // 235: invokestatic ic2/core/util/StackUtil.dropAsEntity (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V
      // 238: aload 0
      // 239: fconst_0
      // 23a: putfield ic2/core/item/tool/EntityMiningLaser.power F
      // 23d: goto 209
      // 240: aload 3
      // 241: getfield net/minecraft/world/World.rand Ljava/util/Random;
      // 244: bipush 10
      // 246: invokevirtual java/util/Random.nextInt (I)I
      // 249: ifne 269
      // 24c: aload 5
      // 24e: invokeinterface net/minecraft/block/state/IBlockState.getMaterial ()Lnet/minecraft/block/material/Material; 1
      // 253: invokevirtual net/minecraft/block/material/Material.getCanBurn ()Z
      // 256: ifeq 269
      // 259: aload 3
      // 25a: aload 4
      // 25c: getfield ic2/api/event/LaserEvent$LaserHitsBlockEvent.pos Lnet/minecraft/util/math/BlockPos;
      // 25f: getstatic net/minecraft/init/Blocks.FIRE Lnet/minecraft/block/BlockFire;
      // 262: invokevirtual net/minecraft/block/BlockFire.getDefaultState ()Lnet/minecraft/block/state/IBlockState;
      // 265: invokevirtual net/minecraft/world/World.setBlockState (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z
      // 268: pop
      // 269: aload 0
      // 26a: dup
      // 26b: getfield ic2/core/item/tool/EntityMiningLaser.blockBreaks I
      // 26e: bipush 1
      // 26f: isub
      // 270: putfield ic2/core/item/tool/EntityMiningLaser.blockBreaks I
      // 273: bipush 1
      // 274: ireturn
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
   }

   void copyDataFromEvent(LaserEvent event) {
      this.owner = event.owner;
      this.range = event.range;
      this.power = event.power;
      this.blockBreaks = event.blockBreaks;
      this.explosive = event.explosive;
      this.smelt = event.smelt;
   }

   public Entity getThrower() {
      return this.owner;
   }

   public void setThrower(Entity entity) {
      if (entity instanceof EntityLivingBase) {
         this.owner = (EntityLivingBase)entity;
      }
   }
}
