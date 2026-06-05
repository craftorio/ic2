package ic2.core.block.comp;

import ic2.api.event.RetextureEvent;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.ref.BlockName;
import ic2.core.util.Util;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Obscuration extends TileEntityComponent {
   private final Runnable changeHandler;
   private Obscuration.ObscurationData[] dataMap;

   public Obscuration(TileEntityBlock parent, Runnable changeHandler) {
      super(parent);
      this.changeHandler = changeHandler;
   }

   @Override
   public void readFromNbt(NBTTagCompound nbt) {
      if (!nbt.hasNoTags()) {
         for (EnumFacing facing : EnumFacing.VALUES) {
            if (nbt.hasKey(facing.getName(), 10)) {
               NBTTagCompound cNbt = nbt.getCompoundTag(facing.getName());
               Block block = Util.getBlock(cNbt.getString("block"));
               if (block != null) {
                  String variant = cNbt.getString("variant");
                  IBlockState state = BlockStateUtil.getState(block, variant);
                  if (state != null) {
                     int rawSide = cNbt.getByte("side");
                     if (rawSide >= 0 && rawSide < EnumFacing.VALUES.length) {
                        EnumFacing side = EnumFacing.VALUES[rawSide];
                        int[] colorMultipliers = ItemObscurator.internColorMultipliers(cNbt.getIntArray("colorMuls"));
                        Obscuration.ObscurationData data = new Obscuration.ObscurationData(state, variant, side, colorMultipliers);
                        if (this.dataMap == null) {
                           this.dataMap = new Obscuration.ObscurationData[EnumFacing.VALUES.length];
                        }

                        this.dataMap[facing.ordinal()] = data.intern();
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public NBTTagCompound writeToNbt() {
      if (this.dataMap == null) {
         return null;
      }

      NBTTagCompound ret = new NBTTagCompound();

      for (EnumFacing facing : EnumFacing.VALUES) {
         Obscuration.ObscurationData data = this.dataMap[facing.ordinal()];
         if (data != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("block", Util.getName(data.state.getBlock()).toString());
            nbt.setString("variant", data.variant);
            nbt.setByte("side", (byte)data.side.ordinal());
            nbt.setIntArray("colorMuls", data.colorMultipliers);
            ret.setTag(facing.getName(), nbt);
         }
      }

      return ret;
   }

   public boolean applyObscuration(EnumFacing side, Obscuration.ObscurationData data) {
      if (this.dataMap != null && data.equals(this.dataMap[side.ordinal()])) {
         return false;
      }

      if (this.dataMap == null) {
         this.dataMap = new Obscuration.ObscurationData[EnumFacing.VALUES.length];
      }

      this.dataMap[side.ordinal()] = data.intern();
      this.changeHandler.run();
      return true;
   }

   public void clear() {
      this.dataMap = null;
      this.changeHandler.run();
   }

   public boolean hasObscuration() {
      return this.dataMap != null;
   }

   public Obscuration.ObscurationData[] getRenderState() {
      return this.dataMap == null ? null : Arrays.copyOf(this.dataMap, this.dataMap.length);
   }

   public static class ObscurationComponentEventHandler {
      public static void init() {
         new Obscuration.ObscurationComponentEventHandler();
      }

      private ObscurationComponentEventHandler() {
         MinecraftForge.EVENT_BUS.register(this);
      }

      @SubscribeEvent
      public void onObscuration(RetextureEvent event) {
         if (event.state.getBlock() == BlockName.te.getInstance()) {
            TileEntity teRaw = event.getWorld().getTileEntity(event.pos);
            if (teRaw instanceof TileEntityBlock) {
               Obscuration obscuration = ((TileEntityBlock)teRaw).getComponent(Obscuration.class);
               if (obscuration != null) {
                  Obscuration.ObscurationData data = new Obscuration.ObscurationData(event.refState, event.refVariant, event.refSide, event.refColorMultipliers);
                  if (obscuration.applyObscuration(event.side, data)) {
                     event.applied = true;
                     event.setCanceled(true);
                  }
               }
            }
         }
      }
   }

   public static class ObscurationData {
      public final IBlockState state;
      public final String variant;
      public final EnumFacing side;
      public final int[] colorMultipliers;

      public ObscurationData(IBlockState state, String variant, EnumFacing side, int[] colorMultipliers) {
         this.state = state;
         this.variant = variant;
         this.side = side;
         this.colorMultipliers = colorMultipliers;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         }

         if (!(obj instanceof Obscuration.ObscurationData)) {
            return false;
         }

         Obscuration.ObscurationData o = (Obscuration.ObscurationData)obj;
         return o.state.equals(this.state) && o.variant.equals(this.variant) && o.side == this.side && Arrays.equals(o.colorMultipliers, this.colorMultipliers);
      }

      @Override
      public int hashCode() {
         return (this.state.hashCode() * 7 + this.side.ordinal()) * 23;
      }

      public Obscuration.ObscurationData intern() {
         return this;
      }
   }
}
