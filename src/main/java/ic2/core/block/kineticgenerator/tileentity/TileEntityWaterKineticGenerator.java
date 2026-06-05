package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.api.item.IKineticRotor;
import ic2.api.tile.IRotorProvider;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableKineticRotor;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.profile.NotClassic;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWaterKineticGenerator extends TileEntityInventory implements IKineticSource, IRotorProvider, IHasGui {
   public InvSlotConsumableClass rotorSlot;
   public TileEntityWaterKineticGenerator.BiomeState type = TileEntityWaterKineticGenerator.BiomeState.UNKNOWN;
   protected int updateTicker;
   private boolean rightFacing;
   private int distanceToNormalBiome;
   private int crossSection;
   private int obstructedCrossSection;
   private int waterFlow;
   private long lastcheck;
   private float angle = 0.0F;
   private float rotationSpeed;
   private static final float rotationModifier = 0.1F;
   private static final double efficiencyRollOffExponent = 2.0;
   private static final float outputModifier = 0.2F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/water");
   private static final ResourceLocation woodenRotorTexture = new ResourceLocation("ic2", "textures/items/rotor/wood_rotor_model.png");

   public TileEntityWaterKineticGenerator() {
      this.updateTicker = IC2.random.nextInt(this.getTickRate());
      this.rotorSlot = new InvSlotConsumableKineticRotor(
         this, "rotorslot", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, IKineticRotor.GearboxType.WATER, "rotorSlot"
      );
   }

   protected int getTickRate() {
      return 20;
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      this.updateSeaInfo();
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      if (this.updateTicker++ % this.getTickRate() == 0) {
         World world = this.getWorld();
         if (this.type == TileEntityWaterKineticGenerator.BiomeState.UNKNOWN) {
            Biome biome = BiomeUtil.getBiome(world, this.pos);
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) {
               this.type = TileEntityWaterKineticGenerator.BiomeState.OCEAN;
            } else {
               if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)) {
                  this.type = TileEntityWaterKineticGenerator.BiomeState.INVALID;
                  return;
               }

               this.type = TileEntityWaterKineticGenerator.BiomeState.RIVER;
            }
         }

         boolean nextActive = this.getActive();
         boolean needsInvUpdate = false;
         if (!this.rotorSlot.isEmpty() && this.checkSpace(1, true) == 0) {
            if (!nextActive) {
               needsInvUpdate = true;
               nextActive = true;
            }
         } else if (nextActive) {
            nextActive = false;
            needsInvUpdate = true;
         }

         if (nextActive) {
            this.crossSection = Util.square(this.getRotorDiameter() / 2 * 2 * 2 + 1);
            this.obstructedCrossSection = this.checkSpace(this.getRotorDiameter() * 3, false);
            if (this.obstructedCrossSection > 0 && this.obstructedCrossSection <= (this.getRotorDiameter() + 1) / 2) {
               this.obstructedCrossSection = 0;
            }

            int rotorDamage = 0;
            if (this.obstructedCrossSection < 0) {
               this.stopSpinning();
            } else if (this.type == TileEntityWaterKineticGenerator.BiomeState.OCEAN) {
               float diff = (float)Math.sin(world.getWorldTime() * Math.PI / 6000.0);
               diff *= Math.abs(diff);
               this.rotationSpeed = (float)(
                  diff * this.distanceToNormalBiome / 100.0F * (1.0 - Math.pow((double)this.obstructedCrossSection / this.crossSection, 2.0))
               );
               this.waterFlow = (int)(this.rotationSpeed * 3000.0F);
               if (this.rightFacing) {
                  this.rotationSpeed *= -1.0F;
               }

               IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
               this.waterFlow = (int)(this.waterFlow * this.getEfficiency());
               rotorDamage = 2;
            } else if (this.type == TileEntityWaterKineticGenerator.BiomeState.RIVER) {
               this.rotationSpeed = Util.limit(this.distanceToNormalBiome, 20, 50) / 50.0F;
               this.waterFlow = (int)(this.rotationSpeed * 1000.0F);
               if (this.getFacing() == EnumFacing.EAST || this.getFacing() == EnumFacing.NORTH) {
                  this.rotationSpeed *= -1.0F;
               }

               IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
               this.waterFlow = (int)(
                  this.waterFlow
                     * (
                        this.getEfficiency()
                           * (1.0F - 0.3F * world.rand.nextFloat() - 0.1F * ((float)this.obstructedCrossSection / this.crossSection))
                     )
               );
               rotorDamage = 1;
            }

            this.rotorSlot.damage(rotorDamage, false);
         } else {
            this.stopSpinning();
         }

         this.setActive(nextActive);
         if (needsInvUpdate) {
            this.markDirty();
         }
      }
   }

   protected void stopSpinning() {
      boolean update = this.rotationSpeed != 0.0F;
      this.rotationSpeed = 0.0F;
      this.waterFlow = 0;
      if (update) {
         IC2.network.get(true).updateTileEntityField(this, "rotationSpeed");
      }
   }

   @Override
   public void setFacing(EnumFacing side) {
      super.setFacing(side);
      this.updateSeaInfo();
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = super.getNetworkedFields();
      ret.add("rotationSpeed");
      ret.add("rotorSlot");
      return ret;
   }

   @Override
   public int getRotorDiameter() {
      ItemStack stack = this.rotorSlot.get();
      if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof IKineticRotor)) {
         return 0;
      } else {
         return this.type == TileEntityWaterKineticGenerator.BiomeState.OCEAN
            ? ((IKineticRotor)stack.getItem()).getDiameter(stack)
            : (((IKineticRotor)stack.getItem()).getDiameter(stack) + 1) * 2 / 3;
      }
   }

   public int checkSpace(int length, boolean onlyrotor) {
      int box = this.getRotorDiameter() / 2;
      int lentemp = 0;
      if (onlyrotor) {
         length = 1;
         lentemp = length + 1;
      } else {
         box *= 2;
      }

      EnumFacing fwdDir = this.getFacing();
      EnumFacing rightDir = fwdDir.rotateAround(EnumFacing.DOWN.getAxis());
      int ret = 0;
      int xCoord = this.pos.getX();
      int yCoord = this.pos.getY();
      int zCoord = this.pos.getZ();
      World world = this.getWorld();
      MutableBlockPos pos = new MutableBlockPos();

      for (int up = -box; up <= box; up++) {
         int y = yCoord + up;

         for (int right = -box; right <= box; right++) {
            boolean occupied = false;

            for (int fwd = lentemp - length; fwd <= length; fwd++) {
               int x = xCoord + fwd * fwdDir.getFrontOffsetX() + right * rightDir.getFrontOffsetX();
               int z = zCoord + fwd * fwdDir.getFrontOffsetZ() + right * rightDir.getFrontOffsetZ();
               pos.setPos(x, y, z);
               if (world.getBlockState(pos).getBlock() != Blocks.WATER) {
                  occupied = true;
                  if ((up != 0 || right != 0 || fwd != 0) && world.getTileEntity(pos) instanceof TileEntityWaterKineticGenerator && !onlyrotor) {
                     return -1;
                  }
               }
            }

            if (occupied) {
               ret++;
            }
         }
      }

      return ret;
   }

   public void updateSeaInfo() {
      World world = this.getWorld();
      EnumFacing facing = this.getFacing();

      for (int distance = 1; distance < 200; distance++) {
         Biome biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, distance));
         if (!this.isValidBiome(biomeTemp)) {
            this.distanceToNormalBiome = distance;
            this.rightFacing = true;
            return;
         }

         biomeTemp = BiomeUtil.getBiome(world, this.pos.offset(facing, -distance));
         if (!this.isValidBiome(biomeTemp)) {
            this.distanceToNormalBiome = distance;
            this.rightFacing = false;
            return;
         }
      }

      this.distanceToNormalBiome = 200;
      this.rightFacing = true;
   }

   public boolean isValidBiome(Biome biome) {
      return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
   }

   @Override
   public int maxrequestkineticenergyTick(EnumFacing directionFrom) {
      return this.getConnectionBandwidth(directionFrom);
   }

   @Override
   public int getConnectionBandwidth(EnumFacing side) {
      return side.getOpposite() == this.getFacing() ? this.getKuOutput() : 0;
   }

   @Override
   public int requestkineticenergy(EnumFacing directionFrom, int requestkineticenergy) {
      return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
   }

   @Override
   public int drawKineticEnergy(EnumFacing side, int request, boolean simulate) {
      return side.getOpposite() == this.getFacing() ? Math.min(request, this.getKuOutput()) : 0;
   }

   public int getKuOutput() {
      return this.getActive() ? (int)Math.abs(this.waterFlow * outputModifier) : 0;
   }

   public float getEfficiency() {
      ItemStack stack = this.rotorSlot.get();
      return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor ? ((IKineticRotor)stack.getItem()).getEfficiency(stack) : 0.0F;
   }

   public ContainerWaterKineticGenerator getGuiContainer(EntityPlayer player) {
      return new ContainerWaterKineticGenerator(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiWaterKineticGenerator(this.getGuiContainer(player));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   public String getRotorHealth() {
      return !this.rotorSlot.isEmpty()
         ? Localization.translate(
            "ic2.WaterKineticGenerator.gui.rotorhealth",
            (int)(100.0F - (float)this.rotorSlot.get().getItemDamage() / this.rotorSlot.get().getMaxDamage() * 100.0F)
         )
         : "";
   }

   @Override
   public ResourceLocation getRotorRenderTexture() {
      ItemStack stack = this.rotorSlot.get();
      return !StackUtil.isEmpty(stack) && stack.getItem() instanceof IKineticRotor
         ? ((IKineticRotor)stack.getItem()).getRotorRenderTexture(stack)
         : woodenRotorTexture;
   }

   @Override
   public float getAngle() {
      if (this.rotationSpeed != 0.0F) {
         this.angle = this.angle + (float)(System.currentTimeMillis() - this.lastcheck) * this.rotationSpeed * 0.1F;
         this.angle %= 360.0F;
      }

      this.lastcheck = System.currentTimeMillis();
      return this.angle;
   }

   public enum BiomeState {
      UNKNOWN,
      OCEAN,
      RIVER,
      INVALID;
   }
}
